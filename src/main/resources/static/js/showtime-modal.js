// document.addEventListener('DOMContentLoaded', () => {
//     const showtimeModal = document.getElementById('showtimeModal');
//     const dateContainer = document.getElementById('dateContainer');
//     const versionContainer = document.getElementById('versionContainer');
//     const showtimeContainer = document.getElementById('showtimeContainer');
//
//     // Tạo 10 ngày chiếu tính từ hôm nay
//     const today = new Date();
//     for (let i = 0; i < 10; i++) {
//         const showDate = new Date(today);
//         showDate.setDate(today.getDate() + i);
//
//         const fullDate = showDate.toISOString().split('T')[0]; // yyyy-mm-dd
//         const label = showDate.toLocaleDateString('vi-VN', {
//             weekday: 'short',
//             day: '2-digit',
//             month: '2-digit'
//         });
//
//         const btn = document.createElement('button');
//         btn.className = 'btn btn-outline-primary date-btn';
//         btn.dataset.fullDate = fullDate;
//         btn.textContent = label;
//
//         dateContainer.appendChild(btn);
//     }
//
//     showtimeModal?.addEventListener('show.bs.modal', e => {
//         const btn = e.relatedTarget;
//         showtimeModal.dataset.currentMovieId = btn.dataset.movieId;
//         showtimeModal.querySelector('.modal-title').textContent = btn.dataset.movieName + ' - Suất Chiếu';
//         showtimeModal.querySelector('#modalMovieInfo').textContent = 'Mã phim ' + btn.dataset.movieId;
//
//         // Reset trạng thái
//         dateContainer.querySelectorAll('.date-btn').forEach(b => b.classList.remove('active'));
//         versionContainer.innerHTML = '';
//         showtimeContainer.innerHTML = '';
//     });
//
//     dateContainer?.addEventListener('click', e => {
//         const btn = e.target.closest('.date-btn');
//         if (!btn) return;
//
//         dateContainer.querySelectorAll('.date-btn').forEach(b => b.classList.remove('active'));
//         btn.classList.add('active');
//
//         const date = btn.dataset.fullDate;
//         const movieId = showtimeModal.dataset.currentMovieId;
//
//         versionContainer.innerHTML = '';
//         showtimeContainer.innerHTML = '';
//
//         fetch(`/api/showtimes/versions?movieId=${movieId}&date=${date}`)
//             .then(res => res.ok ? res.json() : Promise.reject(res.status))
//             .then(vers => {
//                 if (!vers.length) {
//                     versionContainer.innerHTML = `
//                         <span class="text-warning">
//                             <i class="bi bi-exclamation-triangle me-2"></i>
//                             Xin lỗi, không có phiên bản vào ngày này, xin hãy chọn một ngày khác.
//                         </span>
//                     `;
//                     showtimeContainer.innerHTML = `
//                         <span class="text-warning">
//                             <i class="bi bi-clock-history me-2"></i>
//                             Xin lỗi, không có suất chiếu vào ngày này, xin hãy chọn một ngày khác.
//                         </span>
//                     `;
//                     return;
//                 }
//
//                 vers.forEach(v => {
//                     const vb = document.createElement('button');
//                     vb.className = 'btn btn-outline-warning version-btn fw-bold';
//                     vb.textContent = v.versionName;
//                     vb.dataset.versionId = v.versionId;
//                     versionContainer.appendChild(vb);
//                 });
//             })
//             .catch(console.error);
//     });
//
//     versionContainer?.addEventListener('click', e => {
//         const btn = e.target.closest('.version-btn');
//         if (!btn) return;
//
//         versionContainer.querySelectorAll('.version-btn').forEach(b => b.classList.remove('active'));
//         btn.classList.add('active');
//
//         const versionId = btn.dataset.versionId;
//         const movieId = showtimeModal.dataset.currentMovieId;
//         const date = dateContainer.querySelector('.date-btn.active')?.dataset.fullDate;
//
//         fetch(`/api/showtimes/showtime?movieId=${movieId}&date=${date}&versionId=${versionId}`)
//             .then(res => res.ok ? res.json() : Promise.reject(res.status))
//             .then(sts => {
//                 showtimeContainer.innerHTML = '';
//
//                 if (!sts.length) {
//                     showtimeContainer.innerHTML += `
//                         <p class="text-warning">
//                             <i class="bi bi-info-circle me-2"></i>
//                             Xin lỗi, không có suất chiếu cho phiên bản này vào ngày này.
//                         </p>`;
//                     return;
//                 }
//
//                 const wrap = document.createElement('div');
//                 wrap.className = 'd-flex flex-wrap gap-2 mt-2';
//
//                 sts.forEach(st => {
//                     const tb = document.createElement('button');
//                     tb.className = 'btn btn-outline-success time-btn fw-bold px-3 py-2 rounded-pill';
//                     tb.textContent = st.scheduleTime.slice(0, 5);
//                     tb.dataset.scheduleId = st.scheduleId;
//                     tb.dataset.time = st.scheduleTime;
//
//                     tb.addEventListener('click', () => {
//                         wrap.querySelectorAll('.time-btn').forEach(b => b.classList.remove('active'));
//                         tb.classList.add('active');
//
//                         // Lưu thông tin suất chiếu được chọn vào dataset
//                         showtimeModal.dataset.scheduleId = st.scheduleId;
//                         showtimeModal.dataset.scheduleTime = st.scheduleTime;
//                     });
//
//                     wrap.appendChild(tb);
//                 });
//
//                 showtimeContainer.appendChild(wrap);
//             })
//             .catch(console.error);
//     });
// });


document.addEventListener('DOMContentLoaded', () => {
    const bookingModal = document.getElementById('bookingModal');
    const dateContainer = document.getElementById('dateContainer');
    const versionContainer = document.getElementById('versionContainer');
    const showtimeContainer = document.getElementById('showtimeContainer');

    let currentMovieData = null;

    // Helper: Hiển thị cảnh báo mượt mà
    function showWarning(container, message, icon = 'exclamation-triangle') {
        container.innerHTML = '';
        const div = document.createElement('div');
        div.className = 'alert alert-warning fade show d-flex align-items-center gap-2';
        div.innerHTML = `<i class="bi bi-${icon}"></i> <span>${message}</span>`;
        container.appendChild(div);
    }

    // Tạo 10 ngày chiếu từ hôm nay
    const today = new Date();
    for (let i = 0; i < 10; i++) {
        const showDate = new Date(today);
        showDate.setDate(today.getDate() + i);

        const fullDate = showDate.toISOString().split('T')[0]; // yyyy-mm-dd
        const label = showDate.toLocaleDateString('vi-VN', {
            weekday: 'short',
            day: '2-digit',
            month: '2-digit'
        });

        const btn = document.createElement('button');
        btn.className = 'btn btn-outline-primary date-btn';
        btn.dataset.fullDate = fullDate;
        btn.textContent = label;

        dateContainer.appendChild(btn);
    }

    bookingModal?.addEventListener('show.bs.modal', e => {
        const btn = e.relatedTarget;
        const movieId = btn.dataset.movieId;
        const movieName = btn.dataset.movieName;

        if (!movieId) {
            console.error('Không tìm thấy movieId từ nút.');
            return;
        }

        // Gán dữ liệu
        bookingModal.dataset.currentMovieId = movieId;
        document.getElementById('modalMovieInfo').textContent = `Mã phim ${movieId}`;
        document.getElementById('modalMovieTitle').textContent = `${movieName} - Suất Chiếu`;

        // Reset UI
        dateContainer.querySelectorAll('.date-btn').forEach(b => b.classList.remove('active'));
        versionContainer.innerHTML = '';
        showtimeContainer.innerHTML = '';

        // Lấy thông tin phim từ API
        fetch(`/movie/${movieId}`)
            .then(res => res.ok ? res.json() : Promise.reject(res.status))
            .then(data => {
                currentMovieData = data;
            })
            .catch(console.error);
    });

    // Chọn ngày
    dateContainer?.addEventListener('click', e => {
        const btn = e.target.closest('.date-btn');
        if (!btn || !currentMovieData) return;

        // Active ngày
        dateContainer.querySelectorAll('.date-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const selectedDate = btn.dataset.fullDate;
        versionContainer.innerHTML = '';
        showtimeContainer.innerHTML = '';

        // Hiển thị tất cả phiên bản
        const versions = currentMovieData.versions;
        if (!versions.length) {
            showWarning(versionContainer, 'Không có phiên bản nào cho phim này.');
            return;
        }

        versions.forEach(v => {
            const vb = document.createElement('button');
            vb.className = 'btn btn-outline-warning version-btn fw-bold';
            vb.textContent = v.versionName;
            vb.dataset.versionId = v.versionId;
            vb.dataset.versionName = v.versionName;
            versionContainer.appendChild(vb);
        });

        // Nếu tất cả phiên bản không có suất chiếu hôm đó
        const hasShowtime = versions.some(v =>
            v.showDates.some(d => d.showDate === selectedDate && d.schedules.length)
        );

        if (!hasShowtime) {
            showWarning(showtimeContainer, 'Không có suất chiếu vào ngày này.', 'clock-history');
        }
    });

    // Chọn phiên bản
    versionContainer?.addEventListener('click', e => {
        const btn = e.target.closest('.version-btn');
        if (!btn || !currentMovieData) return;

        versionContainer.querySelectorAll('.version-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const versionId = parseInt(btn.dataset.versionId);
        const selectedDate = dateContainer.querySelector('.date-btn.active')?.dataset.fullDate;
        const version = currentMovieData.versions.find(v => v.versionId === versionId);
        const showDate = version?.showDates.find(d => d.showDate === selectedDate);

        showtimeContainer.innerHTML = '';

        if (!showDate || !showDate.schedules.length) {
            showWarning(showtimeContainer, 'Không có suất chiếu cho phiên bản này vào ngày này.', 'info-circle');
            return;
        }

        const wrap = document.createElement('div');
        wrap.className = 'd-flex flex-wrap gap-2 mt-2';

        showDate.schedules.forEach(st => {
            const tb = document.createElement('button');
            tb.className = 'btn btn-outline-success time-btn fw-bold px-3 py-2 rounded-pill';
            tb.textContent = st.scheduleTime.slice(0, 5);
            tb.dataset.scheduleId = st.scheduleId;
            tb.dataset.time = st.scheduleTime;

            tb.addEventListener('click', () => {
                wrap.querySelectorAll('.time-btn').forEach(b => b.classList.remove('active'));
                tb.classList.add('active');

                // Gán dữ liệu vào form
                bookingModal.dataset.scheduleId = st.scheduleId;
                bookingModal.dataset.scheduleTime = st.scheduleTime;
                document.getElementById('selectedVersionIdInput').value = versionId;
                document.getElementById('selectedScheduleIdInput').value = st.scheduleId;
                document.getElementById('selectedMovieId').value = currentMovieData.movieId;
            });

            wrap.appendChild(tb);
        });

        showtimeContainer.appendChild(wrap);
    });
});

// Đặt tên phim + ID khi nhấn nút “Đặt Vé”
document.querySelectorAll('[data-bs-target="#bookingModal"]').forEach(btn => {
    btn.addEventListener('click', () => {
        const movieId = btn.getAttribute('data-movie-id');
        const movieName = btn.getAttribute('data-movie-name');

        document.getElementById('modalMovieInfo').innerText = movieId;
        document.getElementById('modalMovieTitle').innerText = movieName;
        document.getElementById('selectedMovieIdInput').value = movieId;
    });
});
