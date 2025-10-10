function clientLogin() {
    return {
        phone: '',
        password: '',
        user: null,
        loading: false,
        loggedIn: false,
        error: '',

        async login() {
            this.error = '';
            if (!this.phone || !this.password) {
                this.error = 'يرجى إدخال جميع البيانات';
                return;
            }

            this.loading = true;
            try {
                // 🔹 طلب login للسيرفر (بدون reload)
                const res = await fetch('/api/client/login', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        phone: this.phone,
                        password: this.password
                    })
                });

                const data = await res.json();
                this.loading = false;

                if (res.ok && data?.token) {
                    this.user = data.user;
                    this.loggedIn = true;

                    // حفظ التوكن محليًا
                    localStorage.setItem('client_token', data.token);
                } else {
                    this.error = data.message || 'بيانات الدخول غير صحيحة';
                }

            } catch (err) {
                this.loading = false;
                this.error = 'حدث خطأ أثناء الاتصال بالسيرفر';
            }
        },

        logout() {
            localStorage.removeItem('client_token');
            this.loggedIn = false;
            this.user = null;
            this.phone = '';
            this.password = '';
        },

        async init() {
            // عند تحميل الصفحة، لو المستخدم مسجل قبل كده
            const token = localStorage.getItem('client_token');
            if (token) {
                try {
                    const res = await fetch('/api/client/me', {
                        headers: {'Authorization': `Bearer ${token}`}
                    });
                    if (res.ok) {
                        const data = await res.json();
                        this.user = data.user;
                        this.loggedIn = true;
                    }
                } catch (_) {
                }
            }
        }
    }
}
