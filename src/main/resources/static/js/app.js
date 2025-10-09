function productApp() {
    return {
        products: [],
        newProduct: { name: '', price: 0, description: '', category: 'عام' },
        modal: { show: false, product: {} },
        alert: { show: false, message: '', type: 'success' },

        async init() {
            await this.loadProducts();
        },

        async loadProducts() {
            try {
                const res = await fetch('/api/products');
                if (!res.ok) throw new Error('فشل في جلب المنتجات');
                this.products = await res.json();
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        async addProduct() {
            if (!this.validateProduct(this.newProduct)) return;

            try {
                const res = await fetch('/api/products', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.newProduct)
                });

                const data = await res.json();

                if (!res.ok) {
                    throw new Error(data.message || 'فشل في إضافة المنتج');
                }

                if (data.data) {
                    this.products.push(data.data);
                } else {
                    await this.loadProducts();
                }

                this.newProduct = { name: '', price: 0, description: '', category: 'عام' };
                this.showAlert('✅ تمت الإضافة بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        openEditModal(product) {
            this.modal = {
                show: true,
                product: { ...product }
            };
        },

        async updateProduct() {
            if (!this.validateProduct(this.modal.product)) return;

            try {
                const res = await fetch(`/api/products/${this.modal.product._id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.modal.product)
                });

                const data = await res.json();

                if (!res.ok) {
                    throw new Error(data.message || 'فشل في تحديث المنتج');
                }

                const index = this.products.findIndex(p => p._id === this.modal.product._id);
                if (index !== -1) {
                    this.products[index] = { ...this.modal.product };
                }

                this.modal.show = false;
                this.showAlert('✅ تم التحديث بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        async deleteProduct(id) {
            if (!confirm('هل أنت متأكد من حذف هذا المنتج؟')) return;

            try {
                const res = await fetch(`/api/products/${id}`, {
                    method: 'DELETE'
                });

                const data = await res.json();

                if (!res.ok) {
                    throw new Error(data.message || 'فشل في حذف المنتج');
                }

                this.products = this.products.filter(p => p._id !== id);
                this.showAlert('✅ تم الحذف بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        validateProduct(product) {
            if (!product.name.trim()) {
                this.showAlert('⚠️ اسم المنتج مطلوب', 'error');
                return false;
            }
            if (!product.price || product.price <= 0) {
                this.showAlert('⚠️ السعر يجب أن يكون أكبر من صفر', 'error');
                return false;
            }
            if (!product.description.trim()) {
                this.showAlert('⚠️ وصف المنتج مطلوب', 'error');
                return false;
            }
            return true;
        },

        showAlert(message, type = 'success') {
            this.alert = { show: true, message, type };
            setTimeout(() => this.alert.show = false, 4000);
        }
    }
}
