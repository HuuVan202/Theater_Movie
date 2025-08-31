document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra xem jQuery và jQuery UI có sẵn không
    if (typeof jQuery === 'undefined' || typeof jQuery.ui === 'undefined') {
        console.error('jQuery hoặc jQuery UI không được tải.');
        return;
    }

    // Kiểm tra xem Select2 có sẵn không
    if (typeof jQuery.fn.select2 === 'undefined') {
        console.error('Select2 không được tải.');
        return;
    }

    // Lấy ngày hiện tại dạng YYYY-MM-DD
    const today = new Date().toISOString().split('T')[0];

    // 1. Autocomplete tìm kiếm
    $('#search').autocomplete({
        source: function(request, response) {
            console.log('Fetching suggestions for query: ' + request.term);
            $.ajax({
                url: '/admin/suggestMovies',
                data: {
                    keyword: request.term,
                    fromDate: today,
                    toDate: today
                },
                dataType: 'json',
                success: function(data) {
                    console.log('Suggestions received: ', data);
                    // Kiểm tra dữ liệu trả về
                    if (Array.isArray(data)) {
                        response(data);
                    } else {
                        console.warn('Dữ liệu không đúng định dạng:', data);
                        response([]);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('Error fetching suggestions:', error, xhr.status, xhr.responseText);
                    response([]);
                }
            });
        },
        minLength: 2,
        delay: 300
    });

    // 2. Xử lý filter phim khi submit form
    $('form').on('submit', function(event) {
        event.preventDefault();

        const searchQuery = $('#search').val().trim();
        const selectedGenres = $('#genresSelect').val() || [];

        $.ajax({
            url: '/nowShowing',
            method: 'GET',
            data: {
                searchQuery: searchQuery,
                genres: selectedGenres,
                fromDate: today,
                toDate: today
            },
            success: function(data) {
                console.log('Filtered movies received:', data);

                // Kiểm tra dữ liệu trả về
                const $data = $(data);
                const $grid = $data.find('.grid');
                const $pagination = $data.find('.mt-6');

                if ($grid.length) {
                    $('.grid').html($grid.html());
                } else {
                    $('.grid').html('<div class="text-center text-[#D9D6C3] text-lg py-10">Không tìm thấy phim.</div>');
                }

                // Cập nhật pagination nếu có
                if ($pagination.length) {
                    $('.mt-6').html($pagination.html());
                } else {
                    $('.mt-6').empty();
                }
            },
            error: function(xhr, status, error) {
                console.error('Error fetching filtered movies:', error, xhr.status, xhr.responseText);
                $('.grid').html('<div class="text-center text-[#D9D6C3] text-lg py-10">Lỗi khi tải danh sách phim. Vui lòng thử lại.</div>');
            }
        });
    });

    // 3. Select2 cho dropdown thể loại
    $('#genresSelect').select2({
        placeholder: "Chọn thể loại",
        allowClear: true,
        width: '200px'
    });
});