function handleSubmit(event) {
    event.preventDefault();
    const toast = document.createElement('div');
    toast.className = 'toast toast-top toast-center';
    toast.innerHTML = `
        <div class="alert shadow-2xl" style="background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%); color: #1a1a2e; border: none;">
            <i class="fas fa-check-circle text-2xl"></i>
            <span class="font-bold">تم إرسال رسالتك بنجاح! سنتواصل معك قريباً ✨</span>
        </div>`;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
    event.target.reset();
}