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
                const res = await fetch('/api/categories', { credentials: 'include' });
                console.log('Load categories status:', res.status, 'OK:', res.ok);
                if (!res.ok) {
                    const text = await res.text();
                    console.log('Load categories response:', text);
                    throw new Error('فشل في جلب التصنيفات');
                }
                this.categories = await res.json();
            } catch (err) {
                console.error('Load categories error:', err);
                this.showAlert(err.message, 'error');
            }
        },

        handleFileSelect(event) {
            const file = event.target.files[0];
            if (file) {
                this.selectedFile = file;
                const reader = new FileReader();
                reader.onload = e => this.imagePreview = e.target.result;
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
                let data;
                if (this.selectedFile) {
                    const formData = new FormData();
                    formData.append('name', this.newCategory.name);
                    formData.append('image', this.selectedFile);

                    const res = await fetch('/api/categories/with-image', {
                        method: 'POST',
                        body: formData,
                        credentials: 'include'
                    });
                    console.log('Add category with image status:', res.status);
                    if (!res.ok) {
                        const text = await res.text();
                        console.log('Add category with image response:', text);
                        throw new Error(text || 'فشل في إضافة التصنيف');
                    }
                    data = await res.json();
                } else {
                    const res = await fetch('/api/categories', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(this.newCategory),
                        credentials: 'include'
                    });
                    console.log('Add category status:', res.status);
                    if (!res.ok) {
                        const text = await res.text();
                        console.log('Add category response:', text);
                        throw new Error(text || 'فشل في إضافة التصنيف');
                    }
                    data = await res.json();
                }

                if (data.data) this.categories.push(data.data);
                this.newCategory = { name: '' };
                this.clearImage();
                this.showAlert('✅ تمت الإضافة بنجاح', 'success');
            } catch (err) {
                console.error('Add category error:', err);
                this.showAlert(err.message, 'error');
            }
        },

        async importCSV(event) {
            const file = event.target.files[0];
            if (!file) return;

            try {
                const formData = new FormData();
                formData.append('file', file);

                const res = await fetch('/api/categories/import', {
                    method: 'POST',
                    body: formData,
                    credentials: 'include'
                });
                console.log('Import CSV status:', res.status);
                if (!res.ok) {
                    const text = await res.text();
                    console.log('Import CSV response:', text);
                    throw new Error(text || 'فشل في استيراد الملف');
                }
                const data = await res.json();
                this.categories.push(...data.data);
                this.showAlert('✅ تم استيراد الملف بنجاح', 'success');
            } catch (err) {
                console.error('Import CSV error:', err);
                this.showAlert(err.message, 'error');
            }
        },

        openEditModal(category) {
            this.modal = { show: true, category: { ...category } };
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
                    body: JSON.stringify(this.modal.category),
                    credentials: 'include'
                });
                console.log('Update category status:', res.status);
                if (!res.ok) {
                    const text = await res.text();
                    console.log('Update category response:', text);
                    throw new Error(text || 'فشل في تحديث التصنيف');
                }
                const data = await res.json();
                const index = this.categories.findIndex(c => c._id === this.modal.category._id);
                if (index !== -1) this.categories[index] = { ...this.modal.category };
                this.modal.show = false;
                this.showAlert('✅ تم التحديث بنجاح', 'success');
            } catch (err) {
                console.error('Update category error:', err);
                this.showAlert(err.message, 'error');
            }
        },

        async deleteCategory(id) {
            if (!confirm('هل أنت متأكد من حذف هذا التصنيف؟')) return;
            try {
                const res = await fetch(`/api/categories/${id}`, {
                    method: 'DELETE',
                    credentials: 'include'
                });
                console.log('Delete category status:', res.status);
                if (res.status === 204) {
                    this.categories = this.categories.filter(c => c._id !== id);
                    this.showAlert('✅ تم الحذف بنجاح', 'success');
                    return;
                }
                if (!res.ok) {
                    const text = await res.text();
                    console.log('Delete category response:', text);
                    throw new Error(text || 'فشل في حذف التصنيف');
                }
                const data = await res.json();
                this.categories = this.categories.filter(c => c._id !== id);
                this.showAlert('✅ تم الحذف بنجاح', 'success');
            } catch (err) {
                console.error('Delete category error:', err);
                this.showAlert(err.message, 'error');
            }
        },

        showAlert(message, type = 'success') {
            this.alert = { show: true, message, type };
            setTimeout(() => (this.alert.show = false), 4000);
        },

        get filteredCategories() {
            const query = this.searchQuery.toLowerCase().trim();
            if (!query) return this.categories;
            return this.categories.filter(c => c.name.toLowerCase().includes(query));
        }
    };
}