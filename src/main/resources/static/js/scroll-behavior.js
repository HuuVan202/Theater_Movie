document.addEventListener('DOMContentLoaded', () => {
    const bookTicketBtn = document.querySelector('.book-ticket-btn');
    if (bookTicketBtn) {
        bookTicketBtn.addEventListener('click', (e) => {
            e.preventDefault(); // Prevent default anchor behavior
            const target = document.querySelector('#featured-movies');
            if (target) {
                const header = document.querySelector('header'); // Adjust selector if header has a specific class/ID
                const headerHeight = header ? header.offsetHeight : 0;
                const targetPosition = target.getBoundingClientRect().top + window.scrollY - headerHeight;
                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
            }
        });
    }
});