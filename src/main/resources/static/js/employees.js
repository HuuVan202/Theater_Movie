// static/js/admin.js

document.addEventListener('DOMContentLoaded', function() {
    // Xác nhận trước khi xóa nhân viên
    const deleteButtons = document.querySelectorAll('.dropdown-item.text-danger');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(event) {
            const confirmed = confirm('Bạn có chắc muốn xóa nhân viên này?');
            if (!confirmed) {
                event.preventDefault();
            }
        });
    });

    // Prevent row click when interacting with dropdowns
    document.querySelectorAll('.dropdown-toggle').forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });

    // Add click event listeners to all dropdown items
    document.querySelectorAll('.dropdown-menu a').forEach(link => {
        link.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });

    // Prevent dropdown menu clicks from bubbling up to the row
    document.querySelectorAll('.dropdown-menu').forEach(menu => {
        menu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });
});

// Hiệu ứng sóng cho nút Thêm Nhân viên
const addBtn = document.querySelector('.add-employee-btn');
if(addBtn) {
    addBtn.addEventListener('click', function(e) {
        const wave = document.createElement('span');
        wave.className = 'wave';
        const rect = addBtn.getBoundingClientRect();
        wave.style.left = (e.clientX - rect.left) + 'px';
        wave.style.top = (e.clientY - rect.top) + 'px';
        wave.style.width = wave.style.height = Math.max(rect.width, rect.height) + 'px';
        wave.style.position = 'absolute';
        wave.style.borderRadius = '50%';
        wave.style.background = 'rgba(99,102,241,0.18)';
        wave.style.transform = 'scale(0)';
        wave.style.animation = 'wave-anim 0.6s linear';
        addBtn.style.position = 'relative';
        addBtn.appendChild(wave);
        setTimeout(() => wave.remove(), 600);
    });
    // Tooltip
    addBtn.setAttribute('title', 'Thêm nhân viên');
}
// CSS động cho hiệu ứng sóng
const style = document.createElement('style');
style.innerHTML = `
@keyframes wave-anim {
    to {
        transform: scale(3.5);
        opacity: 0;
    }
}`;
document.head.appendChild(style);
