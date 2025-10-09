function productApp() {
    return {
        products: [],
        categories: [],
        newProduct: { name: '', price: 0, description: '', categoryId: '' },
        selectedFile: null,
        imagePreview: null,
        modal: { show: false, product: {} },
        alert: { show: false, message: '', type: 'success' },
        searchQuery: '',

        async init() {
            await this.loadCategories();
            await this.loadProducts();
        },

        async loadCategories() {
            try {
                const res = await fetch('/api/categories');
                if (!res.ok) throw new Error('فشل في جلب التصنيفات');
                this.categories = await res.json();
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
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

        handleFileSelect(event) {
            const file = event.target.files[0];
            if (file) {
                this.selectedFile = file;
                const reader = new FileReader();
                reader.onload = (e) => {
                    this.imagePreview = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        },

        clearImage() {
            this.selectedFile = null;
            this.imagePreview = null;
        },

        async addProduct() {
            if (!this.validateProduct(this.newProduct)) return;

            try {
                if (this.selectedFile) {
                    const formData = new FormData();
                    formData.append('name', this.newProduct.name);
                    formData.append('price', this.newProduct.price);
                    formData.append('description', this.newProduct.description);
                    formData.append('categoryId', this.newProduct.categoryId);
                    formData.append('image', this.selectedFile);

                    const res = await fetch('/api/products/with-image', {
                        method: 'POST',
                        body: formData
                    });

                    const data = await res.json();
                    if (!res.ok) throw new Error(data.message || 'فشل في إضافة المنتج');

                    if (data.data) {
                        this.products.push(data.data);
                    } else {
                        await this.loadProducts();
                    }
                } else {
                    const res = await fetch('/api/products', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(this.newProduct)
                    });

                    const data = await res.json();
                    if (!res.ok) throw new Error(data.message || 'فشل في إضافة المنتج');

                    if (data.data) {
                        this.products.push(data.data);
                    } else {
                        await this.loadProducts();
                    }
                }

                this.newProduct = { name: '', price: 0, description: '', categoryId: '' };
                this.clearImage();
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
                if (!res.ok) throw new Error(data.message || 'فشل في تحديث المنتج');

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
                if (!res.ok) throw new Error(data.message || 'فشل في حذف المنتج');

                this.products = this.products.filter(p => p._id !== id);
                this.showAlert('✅ تم الحذف بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },
        get filteredProducts() {
            const query = this.searchQuery.toLowerCase().trim();
            if (!query) return this.products;

            return this.products.filter(p => {
                const nameMatch = p.name.toLowerCase().includes(query);
                const category = this.categories.find(c => c._id === p.categoryId);
                const categoryMatch = category?.name?.toLowerCase().includes(query);
                return nameMatch || categoryMatch;
            });
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
            if (!product.categoryId) {
                this.showAlert('⚠️ يجب اختيار تصنيف', 'error');
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