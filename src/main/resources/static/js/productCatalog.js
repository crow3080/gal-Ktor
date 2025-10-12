function productCatalog() {
    return {
        products: [],
        categories: [],
        filteredProducts: [],
        loading: true,
        error: '',
        searchQuery: '',
        selectedCategory: '',
        sortBy: '',
        showModal: false,
        selectedProduct: null,
        showToast: false,
        toastMessage: '',

        async init() {
            await this.loadCategories();
            await this.loadProducts();
        },

        async loadProducts() {
            try {
                this.loading = true;
                this.error = '';

                const response = await fetch('/api/products');
                if (!response.ok) throw new Error('فشل تحميل المنتجات');

                const data = await response.json();
                this.products = data;
                this.filteredProducts = data;
            } catch (err) {
                this.error = 'حدث خطأ في تحميل المنتجات';
                console.error(err);
            } finally {
                this.loading = false;
            }
        },

        async loadCategories() {
            try {
                const response = await fetch('/api/categories');
                if (!response.ok) throw new Error('فشل تحميل التصنيفات');

                const data = await response.json();
                this.categories = data;
            } catch (err) {
                console.error('خطأ في تحميل التصنيفات:', err);
            }
        },

        filterProducts() {
            let result = [...this.products];

            // فلترة حسب البحث
            if (this.searchQuery) {
                const query = this.searchQuery.toLowerCase();
                result = result.filter(p =>
                    p.name.toLowerCase().includes(query) ||
                    p.description.toLowerCase().includes(query)
                );
            }

            // فلترة حسب التصنيف
            if (this.selectedCategory) {
                result = result.filter(p => p.categoryId === this.selectedCategory);
            }

            this.filteredProducts = result;
            this.sortProducts();
        },

        sortProducts() {
            if (!this.sortBy) return;

            const sorted = [...this.filteredProducts];

            switch(this.sortBy) {
                case 'price-asc':
                    sorted.sort((a, b) => a.price - b.price);
                    break;
                case 'price-desc':
                    sorted.sort((a, b) => b.price - a.price);
                    break;
                case 'name':
                    sorted.sort((a, b) => a.name.localeCompare(b.name, 'ar'));
                    break;
            }

            this.filteredProducts = sorted;
        },

        getCategoryName(categoryId) {
            const category = this.categories.find(c => c._id === categoryId);
            return category ? category.name : 'غير محدد';
        },

        getCategoryProductCount(categoryId) {
            return this.products.filter(p => p.categoryId === categoryId).length;
        },

        scrollCategories(direction) {
            const container = this.$refs.categoriesScroll;
            const scrollAmount = 300;

            if (direction === 'left') {
                container.scrollLeft -= scrollAmount;
            } else {
                container.scrollLeft += scrollAmount;
            }
        },

        showProductDetails(product) {
            this.selectedProduct = product;
            this.showModal = true;
        },

        addToCart(product) {
            // حفظ في localStorage
            let cart = JSON.parse(localStorage.getItem('cart') || '[]');

            const existingItem = cart.find(item => item._id === product._id);
            if (existingItem) {
                existingItem.quantity += 1;
            } else {
                cart.push({...product, quantity: 1});
            }

            localStorage.setItem('cart', JSON.stringify(cart));

            // إظهار Toast
            this.toastMessage = `تمت إضافة "${product.name}" للسلة`;
            this.showToast = true;
            setTimeout(() => this.showToast = false, 3000);
        }
    }
}
