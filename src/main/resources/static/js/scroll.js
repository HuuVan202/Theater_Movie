document.addEventListener('DOMContentLoaded', () => {
    const containers = document.querySelectorAll('.horizontal-scroll-container');
    containers.forEach(container => {
        const leftBtn = container.parentElement.querySelector('.scroll-left');
        const rightBtn = container.parentElement.querySelector('.scroll-right');

        // Function to toggle button visibility
        const toggleButtons = () => {
            leftBtn.style.display = container.scrollLeft > 0 ? 'flex' : 'none';
            rightBtn.style.display = container.scrollWidth > container.clientWidth + container.scrollLeft ? 'flex' : 'none';
        };

        // Initial check
        toggleButtons();

        // Update on scroll
        container.addEventListener('scroll', toggleButtons);

        // Update on window resize
        window.addEventListener('resize', toggleButtons);

        leftBtn.addEventListener('click', () => {
            container.scrollBy({ left: -350, behavior: 'smooth' });
        });

        rightBtn.addEventListener('click', () => {
            container.scrollBy({ left: 350, behavior: 'smooth' });
        });
    });
});