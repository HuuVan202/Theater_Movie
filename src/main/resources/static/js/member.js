// static/js/member.js

document.addEventListener('DOMContentLoaded', () => {
    // 1. Confirm khi nhấn nút Xóa
    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', event => {
            const ok = confirm('Bạn có chắc muốn xóa thành viên này?');
            if (!ok) {
                event.preventDefault();
            }
        });
    });

    // 2. Đóng offcanvas sidebar sau khi click menu (trên mobile)
    const offcanvasEl = document.getElementById('offcanvasSidebar');
    if (offcanvasEl) {
        // Bootstrap Offcanvas instance
        const bsOffcanvas = bootstrap.Offcanvas.getOrCreateInstance(offcanvasEl);

        // Khi click vào bất kỳ link trong offcanvas, auto hide
        offcanvasEl.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                bsOffcanvas.hide();
            });
        });
    }
});
