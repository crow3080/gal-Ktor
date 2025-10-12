function cartManager() {
    return {
        cartItems: [],
        showToast: false,
        toastMessage: '',
        toastType: 'success',
        showCheckoutModal: false,
        orderNumber: '',

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

        getTax() {
            return this.getSubtotal() * 0.14;
        },

        getTotal() {
            return this.getSubtotal() + this.getTax();
        },

        async checkout() {
            if (this.cartItems.length === 0) {
                this.showToastMessage('السلة فارغة!', 'error');
                return;
            }

            // توليد رقم طلب عشوائي
            this.orderNumber = 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase();

            // في حالة وجود API للطلبات، يمكن إرسال البيانات هنا
            // const response = await fetch('/api/orders', {
            //     method: 'POST',
            //     headers: {'Content-Type': 'application/json'},
            //     body: JSON.stringify({
            //         items: this.cartItems,
            //         total: this.getTotal()
            //     })
            // });

            // إظهار modal التأكيد
            this.showCheckoutModal = true;

            // إفراغ السلة
            setTimeout(() => {
                this.cartItems = [];
                this.saveCart();
            }, 1000);
        },

        showToastMessage(message, type = 'success') {
            this.toastMessage = message;
            this.toastType = type;
            this.showToast = true;
            setTimeout(() => this.showToast = false, 3000);
        }
    }
}
