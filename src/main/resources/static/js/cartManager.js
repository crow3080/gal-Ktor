function cartManager() {
    return {
        cartItems: [],
        completedOrder: [],
        showToast: false,
        toastMessage: '',
        toastType: 'success',
        showCheckoutModal: false,
        orderNumber: '',
        customerWhatsApp: '',
        customerName: '',
        customerNotes: '',

        // إعدادات التاجر
        merchantWhatsAppRaw: '01104391245',
        merchantWhatsAppIntl: '201104391245',
        merchantTelegramUsername: 'GalTrading', // ضع username تليجرام هنا
        merchantTelegramChatId: '', // اختياري: إذا كان لديك Chat ID

        init() {
            this.loadCart();
        },

        loadCart() {
            const cart = localStorage.getItem('cart');
            this.cartItems = cart ? JSON.parse(cart) : [];
        },

        saveCart() {
            localStorage.setItem('cart', JSON.stringify(this.cartItems));
        },

        increaseQuantity(index) {
            this.cartItems[index].quantity++;
            this.saveCart();
        },

        decreaseQuantity(index) {
            if (this.cartItems[index].quantity > 1) {
                this.cartItems[index].quantity--;
                this.saveCart();
            }
        },

        removeItem(index) {
            const itemName = this.cartItems[index].name;
            this.cartItems.splice(index, 1);
            this.saveCart();
            this.showToastMessage(`تم حذف "${itemName}" من السلة`, 'success');
        },

        clearCart() {
            if (confirm('هل أنت متأكد من إفراغ السلة؟')) {
                this.cartItems = [];
                this.saveCart();
                this.showToastMessage('تم إفراغ السلة بنجاح', 'success');
            }
        },

        getTotalItems() {
            return this.cartItems.reduce((sum, item) => sum + item.quantity, 0);
        },

        getSubtotal() {
            return this.cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        },

        getOrderSubtotal() {
            if (this.completedOrder.length > 0) {
                return this.completedOrder.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            }
            return this.getSubtotal();
        },

        async checkout() {
            if (this.cartItems.length === 0) {
                this.showToastMessage('السلة فارغة!', 'error');
                return;
            }

            this.orderNumber = 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase();
            this.completedOrder = JSON.parse(JSON.stringify(this.cartItems));
            this.showCheckoutModal = true;
        },

        closeCheckoutModal() {
            this.showCheckoutModal = false;
            this.cartItems = [];
            this.saveCart();
            this.customerWhatsApp = '';
            this.customerName = '';
            this.customerNotes = '';
        },

        showToastMessage(message, type = 'success') {
            this.toastMessage = message;
            this.toastType = type;
            this.showToast = true;
            setTimeout(() => this.showToast = false, 3000);
        },

        normalizeWhatsAppNumber(input) {
            if (!input) return '';
            let n = input.toString().trim();
            n = n.replace(/[^0-9]/g, '');
            if (n.startsWith('00')) n = n.replace(/^00/, '');
            if (n.startsWith('0')) n = '20' + n.slice(1);
            return n;
        },

        buildOrderMessage() {
            const lines = [];
            const emoji = {
                box: '📦',
                check: '✅',
                phone: '📱',
                user: '👤',
                note: '📝',
                money: '💰',
                cart: '🛒',
                line: '─────────────────'
            };

            lines.push(`${emoji.box} طلب جديد من جال للتجارة`);
            lines.push(emoji.line);
            lines.push(`${emoji.cart} رقم الطلب: ${this.orderNumber}`);
            lines.push('');

            // معلومات العميل
            if (this.customerName) {
                lines.push(`${emoji.user} العميل: ${this.customerName}`);
            }
            if (this.customerWhatsApp) {
                lines.push(`${emoji.phone} الهاتف: ${this.customerWhatsApp}`);
            }
            if (this.customerName || this.customerWhatsApp) {
                lines.push('');
            }

            // المنتجات
            lines.push('📋 المنتجات المطلوبة:');
            lines.push('');

            const orderItems = this.completedOrder.length > 0 ? this.completedOrder : this.cartItems;

            orderItems.forEach((item, idx) => {
                const itemTotal = item.price * item.quantity;
                lines.push(`${idx + 1}. ${item.name}`);
                lines.push(`   الكمية: ${item.quantity} × ${item.price.toFixed(2)} جنيه`);
                lines.push(`   الإجمالي: ${itemTotal.toFixed(2)} جنيه`);
                lines.push('');
            });

            // الملخص المالي
            const total = this.getOrderSubtotal();

            lines.push(emoji.line);
            lines.push(`${emoji.money} الملخص المالي:`);
            lines.push(`   ${emoji.check} الإجمالي النهائي: ${total.toFixed(2)} جنيه`);

            // ملاحظات
            if (this.customerNotes) {
                lines.push('');
                lines.push(`${emoji.note} ملاحظات العميل:`);
                lines.push(`   ${this.customerNotes}`);
            }

            lines.push('');
            lines.push(emoji.line);
            lines.push('⏰ يرجى التواصل مع العميل لتأكيد الطلب');

            return lines.join('\n');
        },

        sendOrderViaWhatsApp() {
            const input = this.customerWhatsApp ? this.customerWhatsApp.trim() : '';
            if (input) {
                const normalized = this.normalizeWhatsAppNumber(input);
                if (!/^[0-9]{8,15}$/.test(normalized)) {
                    this.showToastMessage('رقم واتساب غير صحيح. استخدم مثال: 01101234567', 'error');
                    return;
                }
            }

            const merchant = this.merchantWhatsAppIntl;
            const message = encodeURIComponent(this.buildOrderMessage());
            const waLink = `https://wa.me/${merchant}?text=${message}`;

            window.open(waLink, '_blank');
            this.showToastMessage('تم فتح واتساب لإرسال الطلب ✅', 'success');

            setTimeout(() => {
                this.closeCheckoutModal();
            }, 1500);
        },

        sendOrderViaTelegram() {
            const message = encodeURIComponent(this.buildOrderMessage());

            // يمكنك استخدام أحد الطريقتين:
            // 1. إرسال إلى username معين
            const telegramLink = `https://t.me/${this.merchantTelegramUsername}?text=${message}`;

            // 2. أو إذا كان لديك bot وتريد إرسال مباشر إلى chat_id
            // const telegramLink = `https://t.me/share/url?url=&text=${message}`;

            window.open(telegramLink, '_blank');
            this.showToastMessage('تم فتح تليجرام لإرسال الطلب ✅', 'success');

            setTimeout(() => {
                this.closeCheckoutModal();
            }, 1500);
        }
    }
}