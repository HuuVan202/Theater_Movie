//
// var movies = window.appData.movies || [];
//
// function adjustTime(suggestedTimeStr, hasExisting, currentTimeStr) {
//     var timeParts = suggestedTimeStr.split(':').map(Number);
//     var hours = timeParts[0];
//     var minutes = timeParts[1];
//
//     if (!hasExisting && !currentTimeStr) {
//         // For first showtime in empty room, enforce 08:00-09:00 and round minutes to nearest 5
//         if (hours < 8 || hours > 9) hours = 8;
//         if (minutes % 5 !== 0) {
//             minutes = Math.ceil(minutes / 5) * 5;
//             if (minutes >= 60) {
//                 minutes = 0;
//                 hours = hours < 9 ? 9 : hours;
//             }
//         }
//     } else if (currentTimeStr) {
//         // Use the previous end time as the base for the next start time
//         timeParts = currentTimeStr.split(':').map(Number);
//         hours = timeParts[0];
//         minutes = timeParts[1];
//
//         // Add 20 minutes as the base gap
//         var totalMinutes = hours * 60 + minutes + 20;
//         hours = Math.floor(totalMinutes / 60);
//         minutes = totalMinutes % 60;
//
//         // Round minutes to the nearest multiple of 5
//         var remainder = minutes % 5;
//         if (remainder !== 0) {
//             minutes += (5 - remainder);
//             if (minutes >= 60) {
//                 minutes -= 60;
//                 hours += 1;
//             }
//         }
//
//         // Ensure within 08:00-23:00
//         if (hours > 23 || (hours === 23 && minutes > 0)) {
//             return null; // Exceeds 23:00, stop generation
//         }
//     }
//
//     return padTime(hours) + ':' + padTime(minutes);
// }
//
// function formatTime(date) {
//     return padTime(date.getHours()) + ':' + padTime(date.getMinutes());
// }
//
// function padTime(num) {
//     return num < 10 ? '0' + num : num;
// }
//
// document.addEventListener('DOMContentLoaded', function () {
//     var calendarEl = document.getElementById('calendar');
//     var selectedRoomId = window.appData.selectedRoomId || null;
//     var viewType = window.appData.viewType || 'week';
//
//     // Xử lý initialDate an toàn
//     var initialDate = window.appData.initialDate;
//     if (!initialDate || initialDate === 'not set' || initialDate === '') {
//         initialDate = new Date().toISOString().split('T')[0]; // Dùng ngày hiện tại
//     }
//
//
//     var calendar = new FullCalendar.Calendar(calendarEl, {
//         initialView: viewType === 'month' ? 'dayGridMonth' : 'timeGridWeek',
//         headerToolbar: {
//             left: 'prev today next',
//             center: 'title',
//             right: 'timeGridDay timeGridWeek dayGridMonth listWeek'
//         },
//         buttonText: {
//             today: 'Hôm nay',
//             timeGridWeek: 'Tuần',
//             dayGridMonth: 'Tháng',
//             timeGridDay: 'Ngày',
//             listWeek: 'Danh sách'
//         },
//         locale: 'vi',
//         slotLabelFormat: {hour: '2-digit', minute: '2-digit', hour12: false},
//         allDaySlot: false,
//         slotDuration: '00:30:00',
//         slotLabelInterval: '01:00:00',
//         slotMinTime: '08:00:00',
//         slotMaxTime: '24:00:00',
//         height: 'auto',
//         // initialDate: /*[[${startDate}]]*/ '',
//         initialDate:window.appData.initialDate,
//         events: function (fetchInfo, successCallback, failureCallback) {
//             $.get('/admin/showtime/events', {
//                 startDate: fetchInfo.startStr.split('T')[0],
//                 endDate: fetchInfo.endStr.split('T')[0],
//                 roomId: selectedRoomId
//             }, function (newEvents) {
//                 successCallback(newEvents);
//             }).fail(function () {
//                 failureCallback(new Error('Không thể tải lịch chiếu'));
//             });
//         },
//         datesSet: function (info) {
//             var startDate = info.startStr.split('T')[0];
//             var endDate = info.endStr.split('T')[0];
//             var newViewType = info.view.type === 'dayGridMonth' ? 'month' : 'week';
//             $('input[name="startDate"]').val(startDate);
//             $('input[name="endDate"]').val(endDate);
//             $('select[name="viewType"]').val(newViewType);
//         },
//         dateClick: function (info) {
//             $('#showDate').val(info.dateStr.split('T')[0]);
//             $('#scheduleTime').val(info.dateStr.split('T')[1]?.substring(0, 5) || '08:00');
//             if (selectedRoomId) {
//                 $('#roomId').val(selectedRoomId);
//             }
//             $('#createShowtimeModal').modal('show');
//         },
//         eventClick: function (info) {
//             var startDate = /*[[${startDate}]]*/ '';
//             var endDate = /*[[${endDate}]]*/ '';
//             var today = new Date();
//             var show = new Date(info.event.start);
//             var daysUntilShow = Math.ceil((show - today) / (1000 * 60 * 60 * 24));
//             if (daysUntilShow < 3) {
//                 alert('Không thể xóa suất chiếu vì chỉ còn ' + daysUntilShow + ' ngày đến ngày chiếu.');
//                 return;
//             }
//             if (confirm('Xóa suất chiếu này?')) {
//                 $.post('/admin/showtime/delete/' + info.event.id, {
//                     _csrf: /*[[${_csrf.token}]]*/ '',
//                     startDate: startDate,
//                     endDate: endDate,
//                     roomId: selectedRoomId,
//                     viewType: viewType
//                 }, function () {
//                     window.location.href = '/admin/showtime?startDate=' + startDate +
//                         '&endDate=' + endDate +
//                         (selectedRoomId ? '&roomId=' + selectedRoomId : '') +
//                         '&viewType=' + viewType;
//                 }).fail(function (xhr) {
//                     alert('Không thể xóa suất chiếu: ' + (xhr.responseJSON?.message || 'Lỗi không xác định'));
//                 });
//             }
//         }
//     });
//     calendar.render();
//
//     $('#movieId').change(function () {
//         var movieId = $(this).val();
//         var versionSelect = $('#versionId');
//         versionSelect.empty();
//         versionSelect.append('<option value="">Chọn phiên bản</option>');
//         if (movieId) {
//             var selectedMovie = movies.find(function (movie) {
//                 return movie.movieId == movieId;
//             });
//             if (selectedMovie && selectedMovie.versions) {
//                 selectedMovie.versions.forEach(function (version) {
//                     versionSelect.append(
//                         '<option value="' + version.versionId + '">' + version.versionName + '</option>'
//                     );
//                 });
//             }
//         }
//     });
//
//     $('#suggestTimeBtn').click(function () {
//         var showDate = $('#showDate').val();
//         var roomId = $('#roomId').val();
//         var movieId = $('#movieId').val();
//         if (showDate && roomId && movieId) {
//             $.get('/admin/showtime/show-date-id', {showDate: showDate}, function (data) {
//                 $(data).appendTo('body').hide();
//                 var showDateId = $('#showDateId').val();
//                 if (showDateId == -1) {
//                     alert('Ngày chiếu không hợp lệ');
//                     return;
//                 }
//                 $.get('/admin/showtime/suggest', {
//                     roomId: roomId,
//                     showDateId: showDateId,
//                     movieId: movieId
//                 }, function (suggestedTime) {
//                     if (suggestedTime) {
//                         $('#scheduleTime').val(suggestedTime);
//                     } else {
//                         alert('Không thể lấy gợi ý giờ');
//                     }
//                 }).fail(function () {
//                     alert('Không thể lấy gợi ý giờ');
//                 });
//             }).fail(function () {
//                 alert('Không thể lấy showDateId');
//             });
//         } else {
//             alert('Vui lòng chọn ngày, phòng và phim trước khi gợi ý giờ');
//         }
//     });
//
//     $('#confirmSubmitBtn').click(function () {
//         const showDate = $('#showDate').val();
//         const scheduleTime = $('#scheduleTime').val();
//         const movieId = $('#movieId').val();
//         const roomId = $('#roomId').val();
//         const versionId = $('#versionId').val();
//         if (!showDate || !scheduleTime || !movieId || !roomId || !versionId) {
//             alert('Vui lòng điền đầy đủ thông tin');
//             return;
//         }
//         const movieName = $('#movieId option:selected').text();
//         const roomName = $('#roomId option:selected').text();
//         const versionName = $('#versionId option:selected').text();
//         $('#confirmDetails').html(`
//             Vui lòng xác nhận suất chiếu:<br/>
//             Ngày: ${showDate}<br/>
//             Giờ: ${scheduleTime}<br/>
//             Phim: ${movieName}<br/>
//             Phòng: ${roomName}<br/>
//             Phiên bản: ${versionName}
//         `);
//         $('#confirmModal').modal('show');
//     });
//
//     $('#finalConfirmBtn').click(function () {
//         $('#createShowtimeForm').submit();
//     });
//
//     $('#confirmDeleteByDateBtn').click(function () {
//         const showDate = $('#deleteShowDate').val();
//         const roomId = $('#deleteRoomId').val();
//         if (!showDate || !roomId) {
//             alert('Vui lòng chọn ngày và phòng để xóa');
//             return;
//         }
//         if (confirm('Bạn có chắc muốn xóa tất cả suất chiếu của phòng ' + $('#deleteRoomId option:selected').text() + ' vào ngày ' + showDate + '?')) {
//             $.post('/admin/showtime/delete-by-date-and-room', {
//                 _csrf: window.appData._csrf,
//                 showDate: showDate,
//                 roomId: roomId,
//                 startDate: $('input[name="startDate"]').val(),
//                 endDate: $('input[name="endDate"]').val(),
//                 viewType: $('select[name="viewType"]').val(),
//                 selectedRoomId: selectedRoomId
//             }, function () {
//                 window.location.href = '/admin/showtime?startDate=' + $('input[name="startDate"]').val() +
//                     '&endDate=' + $('input[name="endDate"]').val() +
//                     (selectedRoomId ? '&roomId=' + selectedRoomId : '') +
//                     '&viewType=' + $('select[name="viewType"]').val();
//             }).fail(function (xhr) {
//                 alert('Lưu ý: Suất chiếu còn dưới 3 ngày nên không thể xóa!!');
//             });
//         }
//     });
//
//     // Auto-create showtime logic
//     function updateVersionOptions(movieSelect, versionSelect) {
//         var movieId = $(movieSelect).val();
//         versionSelect.empty();
//         versionSelect.append('<option value="">Chọn phiên bản</option>');
//         if (movieId) {
//             var selectedMovie = movies.find(function (movie) {
//                 return movie.movieId == movieId;
//             });
//             if (selectedMovie && selectedMovie.versions) {
//                 selectedMovie.versions.forEach(function (version) {
//                     versionSelect.append(
//                         '<option value="' + version.versionId + '">' + version.versionName + '</option>'
//                     );
//                 });
//             }
//         }
//     }
//
//     $('.autoMovieId').change(function () {
//         var movieEntry = $(this).closest('.movie-entry');
//         var versionSelect = movieEntry.find('.autoVersionId');
//         updateVersionOptions(this, versionSelect);
//     });
//
//     $('#addMovieBtn').click(function () {
//         var newEntry = $('.movie-entry:first').clone();
//         newEntry.find('.autoMovieId').val('');
//         newEntry.find('.autoVersionId').html('<option value="">Chọn phiên bản</option>');
//         newEntry.find('.autoShowtimeCount').val('1');
//         newEntry.find('.remove-movie-btn').show().click(function () {
//             if ($('.movie-entry').length > 1) {
//                 $(this).closest('.movie-entry').remove();
//             }
//         });
//         newEntry.find('.autoMovieId').change(function () {
//             var versionSelect = $(this).closest('.movie-entry').find('.autoVersionId');
//             updateVersionOptions(this, versionSelect);
//         });
//         $('.movie-entries-container').append(newEntry);
//     });
//
//     function displayConfirmModal(showtimes) {
//         for (const showtime of showtimes) {
//             const [startHour, startMin] = showtime.startTime.split(':').map(Number);
//             const [endHour, endMin] = showtime.endTime.split(':').map(Number);
//             if (startHour < 8 || startHour > 23 || startHour === 23) {
//                 alert('Giờ bắt đầu của phim ' + showtime.movieName + ' đã quá 23:00!');
//                 return;
//             }
//         }
//         var details = 'Vui lòng xác nhận các suất chiếu:<br/>';
//         showtimes.forEach(function (showtime, index) {
//             details += `
//                 Suất ${index + 1}:<br/>
//                 Ngày: ${showtime.showDate}<br/>
//                 Giờ: ${showtime.startTime} - ${showtime.endTime}<br/>
//                 Phim: ${showtime.movieName}<br/>
//                 Phòng: ${$('#autoRoomId option:selected').text()}<br/>
//                 Phiên bản: ${showtime.versionName}<br/><br/>
//             `;
//         });
//         $('#autoConfirmDetails').html(details);
//         $('#autoConfirmModal').modal('show');
//     }
//
//     $('#autoConfirmBtn').click(function () {
//         var showDate = $('#autoShowDate').val();
//         var roomId = $('#autoRoomId').val();
//         if (!showDate || !roomId) {
//             alert('Vui lòng chọn ngày và phòng chiếu');
//             return;
//         }
//         var today = new Date();
//         today.setDate(today.getDate() + 3);
//         var selectedDate = new Date(showDate);
//         if (selectedDate < today) {
//             alert('Chỉ có thể tạo lịch chiếu từ ngày ' + today.toISOString().split('T')[0]);
//             return;
//         }
//
//         var movieEntries = [];
//         $('.movie-entry').each(function () {
//             var movieId = $(this).find('.autoMovieId').val();
//             var versionId = $(this).find('.autoVersionId').val();
//             var count = parseInt($(this).find('.autoShowtimeCount').val());
//             if (movieId && versionId && count > 0) {
//                 movieEntries.push({
//                     movieId: movieId,
//                     versionId: versionId,
//                     count: count,
//                     movieName: $(this).find('.autoMovieId option:selected').text(),
//                     versionName: $(this).find('.autoVersionId option:selected').text()
//                 });
//             }
//         });
//
//         if (movieEntries.length === 0) {
//             alert('Vui lòng thêm ít nhất một phim hợp lệ');
//             return;
//         }
//
//         $.get('/admin/showtime/show-date-id', {showDate: showDate}, function (data) {
//             $(data).appendTo('body').hide();
//             var showDateId = $('#showDateId').val();
//             if (showDateId == -1) {
//                 alert('Ngày chiếu không hợp lệ');
//                 return;
//             }
//
//             var showtimes = [];
//             var currentTime = null;
//             var hasExistingShowtimes = false;
//
//             $.get('/admin/showtime/has-showtimes', {
//                 roomId: roomId,
//                 showDate: showDate
//             }, function (exists) {
//                 hasExistingShowtimes = exists;
//
//                 function fetchNextShowtime(index) {
//                     if (index >= movieEntries.length) {
//                         displayConfirmModal(showtimes);
//                         return;
//                     }
//
//                     var entry = movieEntries[index];
//                     var remainingCount = entry.count;
//
//                     function getNextShowtime() {
//                         if (remainingCount <= 0) {
//                             fetchNextShowtime(index + 1);
//                             return;
//                         }
//
//                         $.get('/admin/showtime/suggest', {
//                             roomId: roomId,
//                             showDateId: showDateId,
//                             movieId: entry.movieId
//                         }, function (suggestedTime) {
//                             if (!suggestedTime) {
//                                 alert('Không thể tạo suất chiếu cho ' + entry.movieName);
//                                 return;
//                             }
//
//                             var adjustedTime = adjustTime(suggestedTime, hasExistingShowtimes, currentTime);
//                             if (!adjustedTime) {
//                                 alert('Giờ chiếu cho ' + entry.movieName + ' ngoài khoảng 8:00-23:00');
//                                 return;
//                             }
//
//                             $.get('/admin/showtime/movie-duration', {movieId: entry.movieId}, function (duration) {
//                                 if (!duration) {
//                                     alert('Không thể lấy thời lượng phim cho ' + entry.movieName);
//                                     return;
//                                 }
//
//                                 var startDateTime = new Date(showDate + 'T' + adjustedTime);
//                                 var endDateTime = new Date(startDateTime.getTime() + duration * 60000);
//                                 var endTime = formatTime(endDateTime);
//
//                                 showtimes.push({
//                                     showDate: showDate,
//                                     startTime: adjustedTime,
//                                     endTime: endTime,
//                                     movieId: entry.movieId,
//                                     roomId: roomId,
//                                     versionId: entry.versionId,
//                                     movieName: entry.movieName,
//                                     versionName: entry.versionName
//                                 });
//
//                                 var nextDateTime = new Date(endDateTime.getTime() - 5); // 20 minutes gap
//                                 var remainder = nextDateTime.getMinutes() % 5;
//                                 if (remainder !== 0) {
//                                     nextDateTime.setMinutes(nextDateTime.getMinutes() + (5 - remainder));
//                                     if (nextDateTime.getMinutes() >= 60) {
//                                         nextDateTime.setMinutes(0);
//                                         nextDateTime.setHours(nextDateTime.getHours() + 1);
//                                     }
//                                 }
//                                 currentTime = formatTime(nextDateTime);
//                                 if (nextDateTime.getHours() > 23) {
//                                     currentTime = null; // Stop if exceeds 23:00
//                                 }
//
//                                 remainingCount--;
//                                 getNextShowtime();
//                             }).fail(function () {
//                                 alert('Không thể lấy thời lượng phim cho ' + entry.movieName);
//                             });
//                         }).fail(function () {
//                             alert('Không thể lấy gợi ý giờ cho ' + entry.movieName);
//                         });
//                     }
//
//                     getNextShowtime();
//                 }
//
//                 fetchNextShowtime(0);
//             }).fail(function () {
//                 alert('Không thể kiểm tra suất chiếu hiện có');
//             });
//         }).fail(function () {
//             alert('Không thể lấy showDateId');
//         });
//     });
//
//     $('#autoFinalConfirmBtn').click(function () {
//         var showtimes = [];
//         $('.movie-entry').each(function () {
//             var movieId = $(this).find('.autoMovieId').val();
//             var versionId = $(this).find('.autoVersionId').val();
//             var count = parseInt($(this).find('.autoShowtimeCount').val());
//             if (movieId && versionId && count > 0) {
//                 for (var i = 0; i < count; i++) {
//                     showtimes.push({
//                         showDate: $('#autoShowDate').val(),
//                         movieId: movieId,
//                         versionId: versionId,
//                         roomId: $('#autoRoomId').val()
//                     });
//                 }
//             }
//         });
//
//         function submitShowtime(index) {
//             if (index >= showtimes.length) {
//                 window.location.href = '/admin/showtime?showDate=' + $('#autoShowDate').val() +
//                     '&roomId=' + $('#autoRoomId').val();
//                 return;
//             }
//
//             var showtime = showtimes[index];
//             $.get('/admin/showtime/show-date-id', {showDate: showtime.showDate}, function (data) {
//                 $(data).appendTo('body').hide();
//                 var showDateId = $('#showDateId').val();
//                 $.get('/admin/showtime/suggest', {
//                     roomId: showtime.roomId,
//                     showDateId: showDateId,
//                     movieId: showtime.movieId
//                 }, function (suggestedTime) {
//                     if (suggestedTime) {
//                         $.post('/admin/showtime/create', {
//                             // _csrf: /*[[${_csrf.token}]]*/ '',
//                             _csrf: window.appData._csrf,
//                             showDate: showtime.showDate,
//                             scheduleTime: suggestedTime,
//                             movieId: showtime.movieId,
//                             roomId: showtime.roomId,
//                             versionId: showtime.versionId
//                         }, function () {
//                             submitShowtime(index + 1);
//                         }).fail(function (xhr) {
//                             alert('Không thể tạo suất chiếu: ' + (xhr.responseJSON?.message || 'Lỗi không xác định'));
//                         });
//                     }
//                 });
//             });
//         }
//
//         submitShowtime(0);
//     });
//
//     const today = new Date();
//     today.setDate(today.getDate() + 1);
//     const minDate = today.toISOString().split('T')[0];
//     document.getElementById('showDate').setAttribute('min', minDate);
//     document.getElementById('autoShowDate').setAttribute('min', minDate);
//     document.getElementById('deleteShowDate').setAttribute('min', minDate);
//
//     document.getElementById('scheduleTime').addEventListener('change', function () {
//         const selectedTime = this.value;
//         const [hours, minutes] = selectedTime.split(':').map(Number);
//         if (hours < 8 || hours > 23) {
//             alert('Vui lòng chọn giờ chiếu từ 8:00 sáng đến 23:00 tối!');
//             this.value = '';
//         }
//     });
// });



var movies = window.appData.movies || [];

function adjustTime(suggestedTimeStr, hasExisting, currentTimeStr) {
    var timeParts = suggestedTimeStr.split(':').map(Number);
    var hours = timeParts[0];
    var minutes = timeParts[1];

    if (!hasExisting && !currentTimeStr) {
        // For first showtime in empty room, enforce 08:00-09:00 and round minutes to nearest 5
        if (hours < 8 || hours > 9) hours = 8;
        if (minutes % 5 !== 0) {
            minutes = Math.ceil(minutes / 5) * 5;
            if (minutes >= 60) {
                minutes = 0;
                hours = hours < 9 ? 9 : hours;
            }
        }
    } else if (currentTimeStr) {
        // Use the previous end time as the base for the next start time
        timeParts = currentTimeStr.split(':').map(Number);
        hours = timeParts[0];
        minutes = timeParts[1];

        // Add 20 minutes as the base gap
        var totalMinutes = hours * 60 + minutes + 20;
        hours = Math.floor(totalMinutes / 60);
        minutes = totalMinutes % 60;

        // Round minutes to the nearest multiple of 5
        var remainder = minutes % 5;
        if (remainder !== 0) {
            minutes += (5 - remainder);
            if (minutes >= 60) {
                minutes -= 60;
                hours += 1;
            }
        }

        // Ensure within 08:00-23:00
        if (hours > 23 || (hours === 23 && minutes > 0)) {
            return null; // Exceeds 23:00, stop generation
        }
    }

    return padTime(hours) + ':' + padTime(minutes);
}

function formatTime(date) {
    return padTime(date.getHours()) + ':' + padTime(date.getMinutes());
}

function padTime(num) {
    return num < 10 ? '0' + num : num;
}

document.addEventListener('DOMContentLoaded', function () {
    var calendarEl = document.getElementById('calendar');
    var selectedRoomId = window.appData.selectedRoomId || null;
    var viewType = window.appData.viewType || 'week';

    // Xử lý initialDate an toàn
    var initialDate = window.appData.initialDate;
    if (!initialDate || initialDate === 'not set' || initialDate === '') {
        initialDate = new Date().toISOString().split('T')[0]; // Dùng ngày hiện tại
    }


    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: viewType === 'month' ? 'dayGridMonth' : 'timeGridWeek',
        headerToolbar: {
            left: 'prev today next',
            center: 'title',
            right: 'timeGridDay timeGridWeek dayGridMonth listWeek'
        },
        buttonText: {
            today: 'Hôm nay',
            timeGridWeek: 'Tuần',
            dayGridMonth: 'Tháng',
            timeGridDay: 'Ngày',
            listWeek: 'Danh sách'
        },
        locale: 'vi',
        slotLabelFormat: {hour: '2-digit', minute: '2-digit', hour12: false},
        allDaySlot: false,
        slotDuration: '00:30:00',
        slotLabelInterval: '01:00:00',
        slotMinTime: '08:00:00',
        slotMaxTime: '24:00:00',
        height: 'auto',
        // initialDate: /*[[${startDate}]]*/ '',
        initialDate:window.appData.initialDate,
        events: function (fetchInfo, successCallback, failureCallback) {
            $.get('/admin/showtime/events', {
                startDate: fetchInfo.startStr.split('T')[0],
                endDate: fetchInfo.endStr.split('T')[0],
                roomId: selectedRoomId
            }, function (newEvents) {
                successCallback(newEvents);
            }).fail(function () {
                failureCallback(new Error('Không thể tải lịch chiếu'));
            });
        },
        datesSet: function (info) {
            var startDate = info.startStr.split('T')[0];
            var endDate = info.endStr.split('T')[0];
            var newViewType = info.view.type === 'dayGridMonth' ? 'month' : 'week';
            $('input[name="startDate"]').val(startDate);
            $('input[name="endDate"]').val(endDate);
            $('select[name="viewType"]').val(newViewType);
        },
        dateClick: function (info) {
            $('#showDate').val(info.dateStr.split('T')[0]);
            $('#scheduleTime').val(info.dateStr.split('T')[1]?.substring(0, 5) || '08:00');
            if (selectedRoomId) {
                $('#roomId').val(selectedRoomId);
            }
            $('#createShowtimeModal').modal('show');
        },
        eventClick: function (info) {
            var startDate = window.appData.startDate || '';
            var endDate = window.appData.endDate || '';
            var today = new Date();
            var show = new Date(info.event.start);
            var timeDiff = show - today;
            var daysUntilShow = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));

            if (timeDiff <= 0) {
                Swal.fire('Thông báo', 'Suất chiếu đã qua hoặc đang diễn ra, không thể xóa.', 'warning');
                return;
            } else if (daysUntilShow === 1) {
                Swal.fire('Thông báo', 'Không thể xóa suất chiếu trong ngày hôm nay.', 'warning');
                return;
            } else if (daysUntilShow < 3) {
                Swal.fire('Thông báo', 'Không thể xóa suất chiếu vì chỉ còn ' + daysUntilShow + ' ngày đến ngày chiếu.', 'warning');
                return;
            }

            Swal.fire({
                title: 'Xác nhận',
                text: 'Xóa suất chiếu này?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#d33',
                confirmButtonText: 'Xóa',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    console.log('CSRF Token:', window.appData._csrf);
                    console.log('Event ID:', info.event.id);
                    console.log('Params:', { startDate, endDate, roomId: selectedRoomId, viewType });

                    $.ajax({
                        url: '/admin/showtime/delete/' + info.event.id,
                        type: 'POST',
                        contentType: 'application/x-www-form-urlencoded',
                        data: {
                            _csrf: window.appData._csrf,
                            startDate: startDate,
                            endDate: endDate,
                            roomId: selectedRoomId,
                            viewType: viewType
                        },
                        success: function () {
                            Swal.fire('Thành công', 'Xóa suất chiếu thành công!', 'success').then(() => {
                                window.location.href = '/admin/showtime?startDate=' + startDate +
                                    '&endDate=' + endDate +
                                    (selectedRoomId ? '&roomId=' + selectedRoomId : '') +
                                    '&viewType=' + viewType;
                            });
                        },
                        error: function (xhr) {
                            var errorMessage = xhr.responseJSON?.message || 'Lỗi không xác định';
                            console.log('Lỗi chi tiết:', xhr);
                            if (xhr.status === 403) {
                                Swal.fire('Lỗi', 'Bạn không có quyền xóa suất chiếu hoặc CSRF token không hợp lệ.', 'error');
                            } else if (xhr.status === 400 && errorMessage.includes('vé')) {
                                Swal.fire('Lỗi', 'Suất chiếu đã có người đặt vé, không thể xóa.', 'error');
                            } else {
                                Swal.fire('Lỗi', 'Không thể xóa suất chiếu: ' + errorMessage, 'error');
                            }
                        }
                    });
                }
            });
        }
    });
    calendar.render();

    $('#movieId').change(function () {
        var movieId = $(this).val();
        var versionSelect = $('#versionId');
        versionSelect.empty();
        versionSelect.append('<option value="">Chọn phiên bản</option>');
        if (movieId) {
            var selectedMovie = movies.find(function (movie) {
                return movie.movieId == movieId;
            });
            if (selectedMovie && selectedMovie.versions) {
                selectedMovie.versions.forEach(function (version) {
                    versionSelect.append(
                        '<option value="' + version.versionId + '">' + version.versionName + '</option>'
                    );
                });
            }
        }
    });

    $('#suggestTimeBtn').click(function () {
        var showDate = $('#showDate').val();
        var roomId = $('#roomId').val();
        var movieId = $('#movieId').val();
        if (showDate && roomId && movieId) {
            $.get('/admin/showtime/show-date-id', {showDate: showDate}, function (data) {
                $(data).appendTo('body').hide();
                var showDateId = $('#showDateId').val();
                if (showDateId == -1) {
                    Swal.fire('Lỗi', 'Ngày chiếu không hợp lệ', 'error');
                    return;
                }
                $.get('/admin/showtime/suggest', {
                    roomId: roomId,
                    showDateId: showDateId,
                    movieId: movieId
                }, function (suggestedTime) {
                    if (suggestedTime) {
                        $('#scheduleTime').val(suggestedTime);
                    } else {
                        Swal.fire('Lỗi', 'Không thể lấy gợi ý giờ', 'error');
                    }
                }).fail(function () {
                    Swal.fire('Lỗi', 'Không thể lấy gợi ý giờ', 'error');
                });
            }).fail(function () {
                Swal.fire('Lỗi', 'Không thể lấy showDateId', 'error');
            });
        } else {
            Swal.fire('Thông báo', 'Vui lòng chọn ngày, phòng và phim trước khi gợi ý giờ', 'info');
        }
    });

    $('#confirmSubmitBtn').click(function () {
        const showDate = $('#showDate').val();
        const scheduleTime = $('#scheduleTime').val();
        const movieId = $('#movieId').val();
        const roomId = $('#roomId').val();
        const versionId = $('#versionId').val();
        if (!showDate || !scheduleTime || !movieId || !roomId || !versionId) {
            Swal.fire('Thông báo', 'Vui lòng điền đầy đủ thông tin', 'info');
            return;
        }
        const movieName = $('#movieId option:selected').text();
        const roomName = $('#roomId option:selected').text();
        const versionName = $('#versionId option:selected').text();
        $('#confirmDetails').html(`
            Vui lòng xác nhận suất chiếu:<br/>
            Ngày: ${showDate}<br/>
            Giờ: ${scheduleTime}<br/>
            Phim: ${movieName}<br/>
            Phòng: ${roomName}<br/>
            Phiên bản: ${versionName}
        `);
        $('#confirmModal').modal('show');
    });

    $('#finalConfirmBtn').click(function () {
        $('#createShowtimeForm').submit();
    });

    $('#confirmDeleteByDateBtn').click(function () {
        const showDate = $('#deleteShowDate').val();
        const roomId = $('#deleteRoomId').val();
        if (!showDate || !roomId) {
            Swal.fire('Thông báo', 'Vui lòng chọn ngày và phòng để xóa', 'info');
            return;
        }
        var today = new Date();
        var selectedDate = new Date(showDate);
        var timeDiff = selectedDate - today;
        var daysUntil = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));
        if (timeDiff <= 0) {
            Swal.fire('Thông báo', 'Không thể xóa suất chiếu của ngày đã qua hoặc hôm nay.', 'warning');
            return;
        } else if (daysUntil < 3) {
            Swal.fire('Thông báo', 'Không thể xóa suất chiếu vì chỉ còn dưới 3 ngày.', 'warning');
            return;
        }
        Swal.fire({
            title: 'Xác nhận',
            text: 'Bạn có chắc muốn xóa tất cả suất chiếu của phòng ' + $('#deleteRoomId option:selected').text() + ' vào ngày ' + showDate + '?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.post('/admin/showtime/delete-by-date-and-room', {
                    _csrf: window.appData._csrf,
                    showDate: showDate,
                    roomId: roomId,
                    startDate: $('input[name="startDate"]').val(),
                    endDate: $('input[name="endDate"]').val(),
                    viewType: $('select[name="viewType"]').val(),
                    selectedRoomId: selectedRoomId
                }, function () {
                    Swal.fire('Thành công', 'Xóa tất cả suất chiếu thành công!', 'success').then(() => {
                        window.location.href = '/admin/showtime?startDate=' + $('input[name="startDate"]').val() +
                            '&endDate=' + $('input[name="endDate"]').val() +
                            (selectedRoomId ? '&roomId=' + selectedRoomId : '') +
                            '&viewType=' + $('select[name="viewType"]').val();
                    });
                }).fail(function (xhr) {
                    var errorMessage = xhr.responseJSON?.message || 'Lỗi không xác định';
                    if (errorMessage.includes('vé')) {  // Giả sử server trả về message chứa 'vé' nếu có vé
                        Swal.fire('Lỗi', 'Một số suất chiếu đã có người đặt vé, không thể xóa.', 'error');
                    } else {
                        Swal.fire('Lỗi', 'Lưu ý: Suất chiếu còn dưới 3 ngày nên không thể xóa!!', 'error');
                    }
                });
            }
        });
    });

    // Auto-create showtime logic
    function updateVersionOptions(movieSelect, versionSelect) {
        var movieId = $(movieSelect).val();
        versionSelect.empty();
        versionSelect.append('<option value="">Chọn phiên bản</option>');
        if (movieId) {
            var selectedMovie = movies.find(function (movie) {
                return movie.movieId == movieId;
            });
            if (selectedMovie && selectedMovie.versions) {
                selectedMovie.versions.forEach(function (version) {
                    versionSelect.append(
                        '<option value="' + version.versionId + '">' + version.versionName + '</option>'
                    );
                });
            }
        }
    }

    $('.autoMovieId').change(function () {
        var movieEntry = $(this).closest('.movie-entry');
        var versionSelect = movieEntry.find('.autoVersionId');
        updateVersionOptions(this, versionSelect);
    });

    $('#addMovieBtn').click(function () {
        var newEntry = $('.movie-entry:first').clone();
        newEntry.find('.autoMovieId').val('');
        newEntry.find('.autoVersionId').html('<option value="">Chọn phiên bản</option>');
        newEntry.find('.autoShowtimeCount').val('1');
        newEntry.find('.remove-movie-btn').show().click(function () {
            if ($('.movie-entry').length > 1) {
                $(this).closest('.movie-entry').remove();
            }
        });
        newEntry.find('.autoMovieId').change(function () {
            var versionSelect = $(this).closest('.movie-entry').find('.autoVersionId');
            updateVersionOptions(this, versionSelect);
        });
        $('.movie-entries-container').append(newEntry);
    });

    function displayConfirmModal(showtimes) {
        for (const showtime of showtimes) {
            const [startHour, startMin] = showtime.startTime.split(':').map(Number);
            const [endHour, endMin] = showtime.endTime.split(':').map(Number);
            if (startHour < 8 || startHour > 23 || startHour === 23) {
                Swal.fire('Lỗi', 'Giờ bắt đầu của phim ' + showtime.movieName + ' đã quá 23:00!', 'error');
                return;
            }
        }
        var details = 'Vui lòng xác nhận các suất chiếu:<br/>';
        showtimes.forEach(function (showtime, index) {
            details += `
                Suất ${index + 1}:<br/>
                Ngày: ${showtime.showDate}<br/>
                Giờ: ${showtime.startTime} - ${showtime.endTime}<br/>
                Phim: ${showtime.movieName}<br/>
                Phòng: ${$('#autoRoomId option:selected').text()}<br/>
                Phiên bản: ${showtime.versionName}<br/><br/>
            `;
        });
        $('#autoConfirmDetails').html(details);
        $('#autoConfirmModal').modal('show');
    }

    $('#autoConfirmBtn').click(function () {
        var showDate = $('#autoShowDate').val();
        var roomId = $('#autoRoomId').val();
        if (!showDate || !roomId) {
            Swal.fire('Thông báo', 'Vui lòng chọn ngày và phòng chiếu', 'info');
            return;
        }
        var today = new Date();
        today.setDate(today.getDate() + 3);
        var selectedDate = new Date(showDate);
        if (selectedDate < today) {
            Swal.fire('Thông báo', 'Chỉ có thể tạo lịch chiếu từ ngày ' + today.toISOString().split('T')[0], 'info');
            return;
        }

        var movieEntries = [];
        $('.movie-entry').each(function () {
            var movieId = $(this).find('.autoMovieId').val();
            var versionId = $(this).find('.autoVersionId').val();
            var count = parseInt($(this).find('.autoShowtimeCount').val());
            if (movieId && versionId && count > 0) {
                movieEntries.push({
                    movieId: movieId,
                    versionId: versionId,
                    count: count,
                    movieName: $(this).find('.autoMovieId option:selected').text(),
                    versionName: $(this).find('.autoVersionId option:selected').text()
                });
            }
        });

        if (movieEntries.length === 0) {
            Swal.fire('Thông báo', 'Vui lòng thêm ít nhất một phim hợp lệ', 'info');
            return;
        }

        $.get('/admin/showtime/show-date-id', {showDate: showDate}, function (data) {
            $(data).appendTo('body').hide();
            var showDateId = $('#showDateId').val();
            if (showDateId == -1) {
                Swal.fire('Lỗi', 'Ngày chiếu không hợp lệ', 'error');
                return;
            }

            var showtimes = [];
            var currentTime = null;
            var hasExistingShowtimes = false;

            $.get('/admin/showtime/has-showtimes', {
                roomId: roomId,
                showDate: showDate
            }, function (exists) {
                hasExistingShowtimes = exists;

                function fetchNextShowtime(index) {
                    if (index >= movieEntries.length) {
                        displayConfirmModal(showtimes);
                        return;
                    }

                    var entry = movieEntries[index];
                    var remainingCount = entry.count;

                    function getNextShowtime() {
                        if (remainingCount <= 0) {
                            fetchNextShowtime(index + 1);
                            return;
                        }

                        $.get('/admin/showtime/suggest', {
                            roomId: roomId,
                            showDateId: showDateId,
                            movieId: entry.movieId
                        }, function (suggestedTime) {
                            if (!suggestedTime) {
                                Swal.fire('Lỗi', 'Không thể tạo suất chiếu cho ' + entry.movieName, 'error');
                                return;
                            }

                            var adjustedTime = adjustTime(suggestedTime, hasExistingShowtimes, currentTime);
                            if (!adjustedTime) {
                                Swal.fire('Lỗi', 'Giờ chiếu cho ' + entry.movieName + ' ngoài khoảng 8:00-23:00', 'error');
                                return;
                            }

                            $.get('/admin/showtime/movie-duration', {movieId: entry.movieId}, function (duration) {
                                if (!duration) {
                                    Swal.fire('Lỗi', 'Không thể lấy thời lượng phim cho ' + entry.movieName, 'error');
                                    return;
                                }

                                var startDateTime = new Date(showDate + 'T' + adjustedTime);
                                var endDateTime = new Date(startDateTime.getTime() + duration * 60000);
                                var endTime = formatTime(endDateTime);

                                showtimes.push({
                                    showDate: showDate,
                                    startTime: adjustedTime,
                                    endTime: endTime,
                                    movieId: entry.movieId,
                                    roomId: roomId,
                                    versionId: entry.versionId,
                                    movieName: entry.movieName,
                                    versionName: entry.versionName
                                });

                                var nextDateTime = new Date(endDateTime.getTime() - 5); // 20 minutes gap
                                var remainder = nextDateTime.getMinutes() % 5;
                                if (remainder !== 0) {
                                    nextDateTime.setMinutes(nextDateTime.getMinutes() + (5 - remainder));
                                    if (nextDateTime.getMinutes() >= 60) {
                                        nextDateTime.setMinutes(0);
                                        nextDateTime.setHours(nextDateTime.getHours() + 1);
                                    }
                                }
                                currentTime = formatTime(nextDateTime);
                                if (nextDateTime.getHours() > 23) {
                                    currentTime = null; // Stop if exceeds 23:00
                                }

                                remainingCount--;
                                getNextShowtime();
                            }).fail(function () {
                                Swal.fire('Lỗi', 'Không thể lấy thời lượng phim cho ' + entry.movieName, 'error');
                            });
                        }).fail(function () {
                            Swal.fire('Lỗi', 'Không thể lấy gợi ý giờ cho ' + entry.movieName, 'error');
                        });
                    }

                    getNextShowtime();
                }

                fetchNextShowtime(0);
            }).fail(function () {
                Swal.fire('Lỗi', 'Không thể kiểm tra suất chiếu hiện có', 'error');
            });
        }).fail(function () {
            Swal.fire('Lỗi', 'Không thể lấy showDateId', 'error');
        });
    });

    $('#autoFinalConfirmBtn').click(function () {
        var showtimes = [];
        $('.movie-entry').each(function () {
            var movieId = $(this).find('.autoMovieId').val();
            var versionId = $(this).find('.autoVersionId').val();
            var count = parseInt($(this).find('.autoShowtimeCount').val());
            if (movieId && versionId && count > 0) {
                for (var i = 0; i < count; i++) {
                    showtimes.push({
                        showDate: $('#autoShowDate').val(),
                        movieId: movieId,
                        versionId: versionId,
                        roomId: $('#autoRoomId').val()
                    });
                }
            }
        });

        function submitShowtime(index) {
            if (index >= showtimes.length) {
                window.location.href = '/admin/showtime?showDate=' + $('#autoShowDate').val() +
                    '&roomId=' + $('#autoRoomId').val();
                return;
            }

            var showtime = showtimes[index];
            $.get('/admin/showtime/show-date-id', {showDate: showtime.showDate}, function (data) {
                $(data).appendTo('body').hide();
                var showDateId = $('#showDateId').val();
                $.get('/admin/showtime/suggest', {
                    roomId: showtime.roomId,
                    showDateId: showDateId,
                    movieId: showtime.movieId
                }, function (suggestedTime) {
                    if (suggestedTime) {
                        $.post('/admin/showtime/create', {
                            // _csrf: /*[[${_csrf.token}]]*/ '',
                            _csrf: window.appData._csrf,
                            showDate: showtime.showDate,
                            scheduleTime: suggestedTime,
                            movieId: showtime.movieId,
                            roomId: showtime.roomId,
                            versionId: showtime.versionId
                        }, function () {
                            submitShowtime(index + 1);
                        }).fail(function (xhr) {
                            Swal.fire('Lỗi', 'Không thể tạo suất chiếu: ' + (xhr.responseJSON?.message || 'Lỗi không xác định'), 'error');
                        });
                    }
                });
            });
        }

        submitShowtime(0);
    });

    const today = new Date();
    today.setDate(today.getDate() + 1);
    const minDate = today.toISOString().split('T')[0];
    document.getElementById('showDate').setAttribute('min', minDate);
    document.getElementById('autoShowDate').setAttribute('min', minDate);
    document.getElementById('deleteShowDate').setAttribute('min', minDate);

    document.getElementById('scheduleTime').addEventListener('change', function () {
        const selectedTime = this.value;
        const [hours, minutes] = selectedTime.split(':').map(Number);
        if (hours < 8 || hours > 23) {
            Swal.fire('Thông báo', 'Vui lòng chọn giờ chiếu từ 8:00 sáng đến 23:00 tối!', 'info');
            this.value = '';
        }
    });
});