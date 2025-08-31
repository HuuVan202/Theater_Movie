function toggleDropdown() {
    const dropdownMenu = document.getElementById('dropdownMenu');
    const userAvatar = document.getElementById('userAvatar');
    const userDropdown = document.querySelector('.user-dropdown');

    // Toggle dropdown
    dropdownMenu.classList.toggle('show');
    userAvatar.classList.toggle('active');

    if (dropdownMenu.classList.contains('show')) {
        adjustDropdownPosition();
    }
}

function adjustDropdownPosition() {
    const userDropdown = document.querySelector('.user-dropdown');
    const dropdownMenu = document.getElementById('dropdownMenu');
    const userAvatar = document.getElementById('userAvatar');

    // Get viewport dimensions
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    // Get avatar position
    const avatarRect = userAvatar.getBoundingClientRect();

    // Reset classes
    userDropdown.classList.remove('position-left', 'position-center', 'near-edge', 'full-width');
    dropdownMenu.classList.remove('dropdown-up', 'dropdown-menu-start', 'dropdown-menu-end');

    // Check horizontal position
    const dropdownWidth = 220; // Default dropdown width
    const rightSpace = viewportWidth - avatarRect.right;
    const leftSpace = avatarRect.left;

    if (viewportWidth <= 400) {
        // Very small screens - full width
        userDropdown.classList.add('full-width');
    } else if (viewportWidth <= 576) {
        // Small screens
        if (rightSpace < dropdownWidth && leftSpace > rightSpace) {
            userDropdown.classList.add('position-left');
            dropdownMenu.classList.add('dropdown-menu-start');
        } else {
            dropdownMenu.classList.add('dropdown-menu-end');
        }
    } else if (rightSpace < dropdownWidth) {
        // Not enough space on right
        if (leftSpace >= dropdownWidth) {
            userDropdown.classList.add('position-left');
            dropdownMenu.classList.add('dropdown-menu-start');
        } else {
            userDropdown.classList.add('position-center');
        }
    }

    // Check vertical position
    const bottomSpace = viewportHeight - avatarRect.bottom;
    const topSpace = avatarRect.top;
    const dropdownHeight = 300; // Approximate dropdown height

    if (bottomSpace < dropdownHeight && topSpace > bottomSpace) {
        dropdownMenu.classList.add('dropdown-up');
    }
}

// Close dropdown when clicking outside
document.addEventListener('click', function(event) {
    const userDropdown = document.querySelector('.user-dropdown');
    if (!userDropdown.contains(event.target)) {
        document.getElementById('dropdownMenu').classList.remove('show');
        document.getElementById('userAvatar').classList.remove('active');
    }
});

// Adjust position on window resize
window.addEventListener('resize', function() {
    const dropdownMenu = document.getElementById('dropdownMenu');
    if (dropdownMenu.classList.contains('show')) {
        adjustDropdownPosition();
    }
});

// Adjust position on scroll
window.addEventListener('scroll', function() {
    const dropdownMenu = document.getElementById('dropdownMenu');
    if (dropdownMenu.classList.contains('show')) {
        adjustDropdownPosition();
    }
});
