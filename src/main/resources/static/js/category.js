function categoryApp() {
    return {
        categories: [],
        newCategory: { name: '' },
        selectedFile: null,
        imagePreview: null,
        modal: { show: false, category: {} },
        alert: { show: false, message: '', type: 'success' },
        searchQuery: '',

        async init() {
            await this.loadCategories();
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

        async addCategory() {
            if (!this.newCategory.name.trim()) {
                this.showAlert('⚠️ اسم التصنيف مطلوب', 'error');
                return;
            }

            try {
                if (this.selectedFile) {
                    const formData = new FormData();
                    formData.append('name', this.newCategory.name);
                    formData.append('image', this.selectedFile);

                    const res = await fetch('/api/categories/with-image', {
                        method: 'POST',
                        body: formData
                    });

                    const data = await res.json();
                    if (!res.ok) throw new Error(data.message || 'فشل في إضافة التصنيف');

                    if (data.data) {
                        this.categories.push(data.data);
                    }
                } else {
                    const res = await fetch('/api/categories', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(this.newCategory)
                    });

                    const data = await res.json();
                    if (!res.ok) throw new Error(data.message || 'فشل في إضافة التصنيف');

                    if (data.data) {
                        this.categories.push(data.data);
                    }
                }

                this.newCategory = { name: '' };
                this.clearImage();
                this.showAlert('✅ تمت الإضافة بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        openEditModal(category) {
            this.modal = {
                show: true,
                category: { ...category }
            };
        },

        async updateCategory() {
            if (!this.modal.category.name.trim()) {
                this.showAlert('⚠️ اسم التصنيف مطلوب', 'error');
                return;
            }

            try {
                const res = await fetch(`/api/categories/${this.modal.category._id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.modal.category)
                });

                const data = await res.json();
                if (!res.ok) throw new Error(data.message || 'فشل في تحديث التصنيف');

                const index = this.categories.findIndex(c => c._id === this.modal.category._id);
                if (index !== -1) {
                    this.categories[index] = { ...this.modal.category };
                }

                this.modal.show = false;
                this.showAlert('✅ تم التحديث بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        async deleteCategory(id) {
            if (!confirm('هل أنت متأكد من حذف هذا التصنيف؟')) return;

            try {
                const res = await fetch(`/api/categories/${id}`, {
                    method: 'DELETE'
                });

                const data = await res.json();
                if (!res.ok) throw new Error(data.message || 'فشل في حذف التصنيف');

                this.categories = this.categories.filter(c => c._id !== id);
                this.showAlert('✅ تم الحذف بنجاح', 'success');
            } catch (err) {
                this.showAlert(err.message, 'error');
            }
        },

        showAlert(message, type = 'success') {
            this.alert = { show: true, message, type };
            setTimeout(() => this.alert.show = false, 4000);
        } ,
        get filteredCategories() {
            const query = this.searchQuery.toLowerCase().trim();
            if (!query) return this.categories;

            return this.categories.filter(c => c.name.toLowerCase().includes(query));
        }


    }


}
