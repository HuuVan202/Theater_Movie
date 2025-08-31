(function () {
    document.addEventListener('DOMContentLoaded', function () {
        // Date input handling
        const startTimeInput = document.getElementById('startTimeInput');
        const endTimeInput = document.getElementById('endTimeInput');
        const startTimeFormatted = document.getElementById('startTimeFormatted');
        const endTimeFormatted = document.getElementById('endTimeFormatted');

        // Set minimum date for startTime to current date/time
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset()); // Adjust for local timezone
        startTimeInput.min = now.toISOString().slice(0, 16);

        // Format datetime-local to dd/MM/yyyy HH:mm for backend
        function formatDateForBackend(dateStr) {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = date.getFullYear();
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${day}/${month}/${year} ${hours}:${minutes}`;
        }

        // Update hidden inputs and endTime min on startTime change
        startTimeInput.addEventListener('change', function () {
            startTimeFormatted.value = formatDateForBackend(startTimeInput.value);
            endTimeInput.min = startTimeInput.value || now.toISOString().slice(0, 16);
        });

        // Update hidden input for endTime
        endTimeInput.addEventListener('change', function () {
            endTimeFormatted.value = formatDateForBackend(endTimeInput.value);
        });

        // Initialize hidden inputs
        if (startTimeInput.value) {
            startTimeFormatted.value = formatDateForBackend(startTimeInput.value);
            endTimeInput.min = startTimeInput.value;
        }
        if (endTimeInput.value) {
            endTimeFormatted.value = formatDateForBackend(endTimeInput.value);
        }

        // Discount type toggling
        const discountLevelRadio = document.getElementById('discountLevelRadio');
        const discountAmountRadio = document.getElementById('discountAmountRadio');
        const discountLevelField = document.getElementById('discountLevelField');
        const discountAmountField = document.getElementById('discountAmountField');
        const discountLevelInput = document.getElementById('discountLevelInput');
        const discountAmountInput = document.getElementById('discountAmountInput');

        function toggleDiscountFields() {
            if (discountLevelRadio.checked) {
                discountLevelField.style.display = 'block';
                discountAmountField.style.display = 'none';
                discountAmountInput.value = ''; // Clear the other field
            } else if (discountAmountRadio.checked) {
                discountAmountField.style.display = 'block';
                discountLevelField.style.display = 'none';
                discountLevelInput.value = ''; // Clear the other field
            } else {
                discountLevelField.style.display = 'none';
                discountAmountField.style.display = 'none';
                discountLevelInput.value = '';
                discountAmountInput.value = '';
            }
        }

        // Initialize discount fields visibility
        toggleDiscountFields();

        // Add event listeners for radio buttons
        discountLevelRadio.addEventListener('change', toggleDiscountFields);
        discountAmountRadio.addEventListener('change', toggleDiscountFields);

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
        const form = document.getElementById('createPromotionForm');
        const submitButton = document.getElementById('submitButton');
        form.addEventListener('submit', function () {
            // Ensure formatted values are set before submission
            startTimeFormatted.value = formatDateForBackend(startTimeInput.value);
            endTimeFormatted.value = formatDateForBackend(endTimeInput.value);
            submitButton.disabled = true;
            submitButton.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang xử lý...';
        });
    });
})();