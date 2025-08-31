(function () {
    document.addEventListener('DOMContentLoaded', function () {
        // Date input handling
        const startTimeInput = document.getElementById('startTimeInput');
        const endTimeInput = document.getElementById('endTimeInput');
        const startTimeFormatted = document.getElementById('startTimeFormatted');
        const endTimeFormatted = document.getElementById('endTimeFormatted');
        const startTimeError = document.getElementById('startTimeError');
        const endTimeError = document.getElementById('endTimeError');

        // Set minimum date for startTime to current date/time (if not disabled)
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        if (!startTimeInput.disabled) {
            startTimeInput.min = now.toISOString().slice(0, 16);
        }

        // Format datetime-local to dd/MM/yyyy HH:mm for backend
        function formatDateForBackend(dateStr) {
            if (!dateStr) {
                console.warn('Empty date string provided');
                return '';
            }
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) {
                console.warn('Invalid date string:', dateStr);
                return '';
            }
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = date.getFullYear();
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${day}/${month}/${year} ${hours}:${minutes}`;
        }

        // Validate dates
        function validateDates() {
            const startTimeValue = startTimeInput.value;
            const endTimeValue = endTimeInput.value;
            let isValid = true;

            // Validate startTime
            if (startTimeValue) {
                const startDate = new Date(startTimeValue);
                if (isNaN(startDate.getTime())) {
                    startTimeError.textContent = 'Thời gian bắt đầu không hợp lệ.';
                    startTimeError.style.display = 'block';
                    isValid = false;
                } else {
                    startTimeError.textContent = '';
                    startTimeError.style.display = 'none';
                }
            }

            // Validate endTime
            if (endTimeValue) {
                const endDate = new Date(endTimeValue);
                if (isNaN(endDate.getTime())) {
                    endTimeError.textContent = 'Thời gian kết thúc không hợp lệ.';
                    endTimeError.style.display = 'block';
                    isValid = false;
                } else if (startTimeValue) {
                    const startDate = new Date(startTimeValue);
                    if (endDate <= startDate) {
                        endTimeError.textContent = 'Thời gian kết thúc phải sau thời gian bắt đầu.';
                        endTimeError.style.display = 'block';
                        isValid = false;
                    } else {
                        endTimeError.textContent = '';
                        endTimeError.style.display = 'none';
                    }
                } else {
                    endTimeError.textContent = '';
                    endTimeError.style.display = 'none';
                }
            }

            return isValid;
        }

        // Update hidden inputs and endTime min on startTime change
        startTimeInput.addEventListener('change', function () {
            if (!startTimeInput.disabled) {
                startTimeFormatted.value = formatDateForBackend(startTimeInput.value);
                endTimeInput.min = startTimeInput.value || now.toISOString().slice(0, 16);
                validateDates();
            }
        });

        // Update hidden input for endTime and validate
        endTimeInput.addEventListener('change', function () {
            endTimeFormatted.value = formatDateForBackend(endTimeInput.value);
            validateDates();
        });

        // Initialize hidden inputs
        if (startTimeInput.value && !startTimeInput.disabled) {
            startTimeFormatted.value = formatDateForBackend(startTimeInput.value);
            endTimeInput.min = startTimeInput.value;
        }
        if (endTimeInput.value) {
            endTimeFormatted.value = formatDateForBackend(endTimeInput.value);
        }
        validateDates();

        // Discount field initialization
        const discountLevelField = document.getElementById('discountLevelField');
        const discountAmountField = document.getElementById('discountAmountField');
        if (discountLevelField) {
            discountLevelField.style.display = 'block';
        } else if (discountAmountField) {
            discountAmountField.style.display = 'block';
        }

        // Image preview and input handling
        const imageFileInput = document.getElementById('imageFileInput');
        const imagePreview = document.getElementById('imagePreview');
        const imageUrlInput = document.getElementById('imageUrlInput');

        function showImagePreview(src) {
            if (src) {
                imagePreview.src = src;
                imagePreview.classList.add('show');
                imagePreview.style.display = 'block';
            } else {
                imagePreview.src = '';
                imagePreview.classList.remove('show');
                imagePreview.style.display = 'none';
            }
        }

        function handleImageInputChange() {
            if (imageUrlInput.value.trim() !== '') {
                imageFileInput.disabled = true;
                showImagePreview(imageUrlInput.value);
            } else {
                imageFileInput.disabled = false;
                showImagePreview('');
            }
        }

        function handleImageFileChange() {
            const file = imageFileInput.files[0];
            if (file) {
                imageUrlInput.disabled = true;
                const reader = new FileReader();
                reader.onload = function (ev) {
                    showImagePreview(ev.target.result);
                };
                reader.readAsDataURL(file);
            } else {
                imageUrlInput.disabled = false;
                showImagePreview(imageUrlInput.value);
            }
        }

        imageFileInput.addEventListener('change', handleImageFileChange);
        imageUrlInput.addEventListener('input', handleImageInputChange);

        // Initialize image preview
        if (imageUrlInput.value) {
            showImagePreview(imageUrlInput.value);
        }

        // Form submission handling
        const form = document.getElementById('editPromotionForm');
        const submitButton = document.getElementById('submitButton');
        form.addEventListener('submit', function (event) {
            if (!validateDates()) {
                event.preventDefault();
                submitButton.disabled = false;
                submitButton.innerHTML = '<i class="bi bi-save"></i> Lưu thay đổi';
                return;
            }
            if (!startTimeInput.disabled) {
                startTimeFormatted.value = formatDateForBackend(startTimeInput.value);
            }
            endTimeFormatted.value = formatDateForBackend(endTimeInput.value);
            console.log('Submitting startTimeFormatted:', startTimeFormatted.value);
            console.log('Submitting endTimeFormatted:', endTimeFormatted.value);
            submitButton.disabled = true;
            submitButton.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang xử lý...';
        });
    });
})();