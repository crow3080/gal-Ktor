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
        merchantTelegramUsername: 'GalTrading',
        merchantTelegramChatId: '',

        init() {
            this.loadCart();
            const lang = localStorage.getItem('lang') || 'ar';
            if (typeof applyTranslations === 'function') applyTranslations(lang);
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
            const lang = localStorage.getItem('lang') || 'ar';
            this.cartItems.splice(index, 1);
            this.saveCart();
            this.showToastMessage(langs[lang].toastSuccess, 'success');
        },

        clearCart() {
            const lang = localStorage.getItem('lang') || 'ar';
            if (confirm(langs[lang].confirmClearCart)) {
                this.cartItems = [];
                this.saveCart();
                this.showToastMessage(langs[lang].toastSuccess, 'success');
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
            const lang = localStorage.getItem('lang') || 'ar';
            if (this.cartItems.length === 0) {
                this.showToastMessage(langs[lang].toastError, 'error');
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
            const lang = localStorage.getItem('lang') || 'ar';
            const t = langs[lang];
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

            lines.push(`${emoji.box} ${t.orderDetailsTitle}`);
            lines.push(emoji.line);
            lines.push(`${emoji.cart} ${t.orderNumberLabel} ${this.orderNumber}`);
            lines.push('');

            // معلومات العميل
            if (this.customerName) {
                lines.push(`${emoji.user} ${t.nameLabel} ${this.customerName}`);
            }
            if (this.customerWhatsApp) {
                lines.push(`${emoji.phone} ${t.whatsappLabel} ${this.customerWhatsApp}`);
            }
            if (this.customerName || this.customerWhatsApp) {
                lines.push('');
            }

            // المنتجات
            lines.push(`${t.orderDetailsTitle}:`);
            lines.push('');

            const orderItems = this.completedOrder.length > 0 ? this.completedOrder : this.cartItems;

            orderItems.forEach((item, idx) => {
                const itemTotal = item.price * item.quantity;
                lines.push(`${idx + 1}. ${item.name}`);
                lines.push(`   ${t.quantityLabel || 'الكمية'}: ${item.quantity} × ${item.price.toFixed(2)} ${t.currency}`);
                lines.push(`   ${t.itemTotalLabel} ${itemTotal.toFixed(2)} ${t.currency}`);
                lines.push('');
            });

            // الملخص المالي
            const total = this.getOrderSubtotal();

            lines.push(emoji.line);
            lines.push(`${emoji.money} ${t.totalLabelModal}`);
            lines.push(`   ${emoji.check} ${t.totalLabel} ${total.toFixed(2)} ${t.currency}`);

            // ملاحظات
            if (this.customerNotes) {
                lines.push('');
                lines.push(`${emoji.note} ${t.notesLabel}:`);
                lines.push(`   ${this.customerNotes}`);
            }

            lines.push('');
            lines.push(emoji.line);
            lines.push(t.orderSuccessDesc);

            return lines.join('\n');
        },

        sendOrderViaWhatsApp() {
            const lang = localStorage.getItem('lang') || 'ar';
            const input = this.customerWhatsApp ? this.customerWhatsApp.trim() : '';
            if (input) {
                const normalized = this.normalizeWhatsAppNumber(input);
                if (!/^[0-9]{8,15}$/.test(normalized)) {
                    this.showToastMessage(langs[lang].toastError, 'error');
                    return;
                }
            }

            const merchant = this.merchantWhatsAppIntl;
            const message = encodeURIComponent(this.buildOrderMessage());
            const waLink = `https://wa.me/${merchant}?text=${message}`;

            window.open(waLink, '_blank');
            this.showToastMessage(langs[lang].toastSuccess, 'success');

            setTimeout(() => {
                this.closeCheckoutModal();
            }, 1500);
        },

        sendOrderViaTelegram() {
            const lang = localStorage.getItem('lang') || 'ar';
            const message = encodeURIComponent(this.buildOrderMessage());
            const telegramLink = `https://t.me/${this.merchantTelegramUsername}?text=${message}`;

            window.open(telegramLink, '_blank');
            this.showToastMessage(langs[lang].toastSuccess, 'success');

            setTimeout(() => {
                this.closeCheckoutModal();
            }, 1500);
        }
    };
}