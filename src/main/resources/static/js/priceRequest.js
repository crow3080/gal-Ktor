// Translations
const langs = {
    ar: {
        brandName: "جال لتوريدات المصانع",
        pageTitle: "طلب عرض سعر - جال للتجارة",
        mainTitle: "طلب عرض سعر",
        mainDesc: "اختر المنتجات التي تحتاجها وسنرسل لك عرض سعر مفصل",
        selectedLabel: "تم اختيار",
        productLabel: "منتج",
        clearBtn: "مسح الكل",
        filterTitle: "تصفية حسب التصنيف",
        allCat: "جميع التصنيفات",
        selectedBadge: "محدد",
        noProductsTitle: "لا توجد منتجات",
        noProductsDesc: "جرب اختيار تصنيف آخر أو تعديل البحث",
        submitBtn: "إرسال الطلب",
        dialogTitle: "إرسال الطلب عبر واتساب",
        nameLabel: "الاسم",
        nameInput: "اسمك الكامل",
        phoneLabel: "رقم الهاتف",
        phoneInput: "01xxxxxxxxx",
        notesLabel: "ملاحظات إضافية (اختياري)",
        notesInput: "أي تفاصيل إضافية...",
        selectedItemsTitle: "المنتجات المحددة",
        sendWhatsAppBtn: "إرسال عبر واتساب",
        cancelBtn: "إلغاء",
        navProducts: "المنتجات",
        navCart: "السلة",
        navContact: "تواصل معنا",
        mnavProducts: "المنتجات",
        mnavCart: "السلة",
        mnavContact: "تواصل معنا",
        fnavProducts: "المنتجات",
        fnavCart: "السلة",
        fnavContact: "تواصل معنا",
        footerBrand: "جال للتجارة",
        footerDesc: "شريكك الموثوق في توريد المعدات والمنتجات الصناعية",
        copyrightText: "© 2025 جال للتجارة والتوريدات — جميع الحقوق محفوظة",
        searchLabel: "بحث عن المنتجات",
        searchInput: "ابحث باسم المنتج أو الوصف..."
    },
    en: {
        brandName: "Gal Industrial Supplies",
        pageTitle: "Price Request - Gal Trading",
        mainTitle: "Request a Quote",
        mainDesc: "Select the products you need and we'll send you a detailed quote",
        selectedLabel: "Selected",
        productLabel: "product(s)",
        clearBtn: "Clear All",
        filterTitle: "Filter by Category",
        allCat: "All Categories",
        selectedBadge: "Selected",
        noProductsTitle: "No Products Found",
        noProductsDesc: "Try selecting another category or modifying your search",
        submitBtn: "Submit Request",
        dialogTitle: "Send Request via WhatsApp",
        nameLabel: "Name",
        nameInput: "Your full name",
        phoneLabel: "Phone Number",
        phoneInput: "01xxxxxxxxx",
        notesLabel: "Additional Notes (Optional)",
        notesInput: "Any additional details...",
        selectedItemsTitle: "Selected Products",
        sendWhatsAppBtn: "Send via WhatsApp",
        cancelBtn: "Cancel",
        navProducts: "Products",
        navCart: "Cart",
        navContact: "Contact",
        mnavProducts: "Products",
        mnavCart: "Cart",
        mnavContact: "Contact",
        fnavProducts: "Products",
        fnavCart: "Cart",
        fnavContact: "Contact",
        footerBrand: "Gal Trading",
        footerDesc: "Your trusted partner in industrial supplies",
        copyrightText: "© 2025 Gal Trading & Supplies — All rights reserved",
        searchLabel: "Search Products",
        searchInput: "Search by product name or description..."
    }
};

function applyTranslations(lang) {
    const t = langs[lang] || langs.ar;
    const map = {
        brandName: t.brandName,
        pageTitle: t.pageTitle,
        mainTitle: t.mainTitle,
        mainDesc: t.mainDesc,
        selectedLabel: t.selectedLabel,
        productLabel: t.productLabel,
        clearBtn: t.clearBtn,
        filterTitle: t.filterTitle,
        allCat: t.allCat,
        selectedBadge: t.selectedBadge,
        noProductsTitle: t.noProductsTitle,
        noProductsDesc: t.noProductsDesc,
        submitBtn: t.submitBtn,
        dialogTitle: t.dialogTitle,
        nameLabel: t.nameLabel,
        phoneLabel: t.phoneLabel,
        notesLabel: t.notesLabel,
        selectedItemsTitle: t.selectedItemsTitle,
        sendWhatsAppBtn: t.sendWhatsAppBtn,
        cancelBtn: t.cancelBtn,
        navProducts: t.navProducts,
        navCart: t.navCart,
        navContact: t.navContact,
        mnavProducts: t.mnavProducts,
        mnavCart: t.mnavCart,
        mnavContact: t.mnavContact,
        fnavProducts: t.fnavProducts,
        fnavCart: t.fnavCart,
        fnavContact: t.fnavContact,
        footerBrand: t.footerBrand,
        footerDesc: t.footerDesc,
        copyrightText: t.copyrightText,
        searchLabel: t.searchLabel,
        searchInput: t.searchInput
    };

    for (const id in map) {
        const el = document.getElementById(id);
        if (el && map[id]) el.innerText = map[id];
    }

    // Placeholders
    const nameInput = document.getElementById('nameInput');
    const phoneInput = document.getElementById('phoneInput');
    const notesInput = document.getElementById('notesInput');
    const searchInput = document.getElementById('searchInput');
    if (nameInput) nameInput.placeholder = t.nameInput;
    if (phoneInput) phoneInput.placeholder = t.phoneInput;
    if (notesInput) notesInput.placeholder = t.notesInput;
    if (searchInput) searchInput.placeholder = t.searchInput;

    document.title = t.pageTitle;
    document.documentElement.lang = lang;
    document.documentElement.dir = lang === "ar" ? "rtl" : "ltr";
    document.getElementById("current-lang").innerText = lang.toUpperCase();
    localStorage.setItem("lang", lang);
}

function changeLang(lang) {
    applyTranslations(lang);
}

document.addEventListener('DOMContentLoaded', () => {
    const saved = localStorage.getItem("lang") || "ar";
    applyTranslations(saved);
});

function headerManager() {
    return {
        cartCount: 0,
        init() {
            this.loadCartCount();
            window.addEventListener('storage', () => this.loadCartCount());
            window.addEventListener('cartUpdated', () => this.loadCartCount());
        },
        loadCartCount() {
            const cart = localStorage.getItem('cart');
            const items = cart ? JSON.parse(cart) : [];
            this.cartCount = items.reduce((sum, item) => sum + item.quantity, 0);
        }
    };
}

function priceRequestApp() {
    return {
        products: [],
        categories: [],
        filteredProducts: [],
        selectedProducts: [],
        selectedCategory: '',
        searchQuery: '',
        loading: true,
        showDialog: false,
        customerName: '',
        customerPhone: '',
        countryCode: '+20',
        customerNotes: '',

        async init() {
            await this.loadCategories();
            await this.loadProducts();
        },

        async loadProducts() {
            try {
                this.loading = true;
                const response = await fetch('/api/products');
                if (!response.ok) throw new Error('Failed to load products');
                this.products = await response.json();
                this.filteredProducts = this.products;
            } catch (err) {
                console.error('Error loading products:', err);
            } finally {
                this.loading = false;
            }
        },

        async loadCategories() {
            try {
                const response = await fetch('/api/categories');
                if (!response.ok) throw new Error('Failed to load categories');
                this.categories = await response.json();
            } catch (err) {
                console.error('Error loading categories:', err);
            }
        },

        filterProducts() {
            let filtered = this.products;

            // Filter by category
            if (this.selectedCategory) {
                filtered = filtered.filter(p => p.categoryId === this.selectedCategory);
            }

            // Filter by search query
            if (this.searchQuery) {
                const query = this.searchQuery.toLowerCase();
                filtered = filtered.filter(p =>
                    p.name.toLowerCase().includes(query) ||
                    (p.description && p.description.toLowerCase().includes(query))
                );
            }

            this.filteredProducts = filtered;
        },

        toggleProduct(product) {
            const index = this.selectedProducts.findIndex(p => p._id === product._id);
            if (index > -1) {
                this.selectedProducts.splice(index, 1);
            } else {
                this.selectedProducts.push(product);
            }
        },

        isSelected(productId) {
            return this.selectedProducts.some(p => p._id === productId);
        },

        clearSelection() {
            this.selectedProducts = [];
        },

        getCategoryName(categoryId) {
            const cat = this.categories.find(c => c._id === categoryId);
            return cat ? cat.name : (localStorage.getItem('lang') === 'en' ? 'Unspecified' : 'غير محدد');
        },

        getCategoryCount(categoryId) {
            return this.products.filter(p => p.categoryId === categoryId).length;
        },

        openWhatsAppDialog() {
            this.showDialog = true;
        },

        sendToWhatsApp() {
            if (!this.customerName || !this.customerPhone) {
                const lang = localStorage.getItem('lang') || 'ar';
                alert(lang === 'en' ? 'Please fill in name and phone number' : 'يرجى ملء الاسم ورقم الهاتف');
                return;
            }

            // Build WhatsApp message
            const lang = localStorage.getItem('lang') || 'ar';
            let message = lang === 'en'
                ? `*Price Request*\n\n*Customer Name:* ${this.customerName}\n*Phone:* ${this.countryCode}${this.customerPhone}\n\n*Selected Products:*\n\n`
                : `*طلب عرض سعر*\n\n*اسم العميل:* ${this.customerName}\n*الهاتف:* ${this.countryCode}${this.customerPhone}\n\n*المنتجات المطلوبة:*\n\n`;

            this.selectedProducts.forEach((product, index) => {
                message += `${index + 1}. ${product.name}\n`;
                message += `   ${lang === 'en' ? 'Category' : 'التصنيف'}: ${this.getCategoryName(product.categoryId)}\n`;
                if (product.description) {
                    message += `   ${lang === 'en' ? 'Details' : 'الوصف'}: ${product.description}\n`;
                }
                message += `\n`;
            });

            if (this.customerNotes) {
                message += lang === 'en'
                    ? `\n*Additional Notes:*\n${this.customerNotes}`
                    : `\n*ملاحظات إضافية:*\n${this.customerNotes}`;
            }

            // Company WhatsApp number
            const whatsappNumber = '+201104391245';
            const url = `https://wa.me/${whatsappNumber}?text=${encodeURIComponent(message)}`;

            window.open(url, '_blank');

            // Reset form
            this.showDialog = false;
            this.customerName = '';
            this.customerPhone = '';
            this.countryCode = '+966';
            this.customerNotes = '';
            this.selectedProducts = [];
        }
    };
}