document.addEventListener('DOMContentLoaded', function () {
    const backToTopBtn = document.getElementById('back-to-top-btn');

    if (backToTopBtn) {
        // Show/hide button based on scroll position
        window.addEventListener('scroll', function () {
            if (window.scrollY > 300) {
                backToTopBtn.classList.add('show');
            } else {
                backToTopBtn.classList.remove('show');
            }
        });

        // Smooth scroll to top on click
        backToTopBtn.addEventListener('click', function (e) {
            e.preventDefault();
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });

        // Apply translation for tooltip
        const lang = localStorage.getItem('language') || 'vi';
        const key = backToTopBtn.getAttribute('data-i18n');
        if (translations[lang] && translations[lang][key]) {
            backToTopBtn.setAttribute('title', translations[lang][key]);
        }
    }
});