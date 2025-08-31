document.addEventListener('DOMContentLoaded', function () {
    const supportForm = document.getElementById('supportForm');
    const submitButton = document.getElementById('submitButton');

    if (supportForm) {
        supportForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            // Clear previous messages
            document.getElementById('generalError').classList.add('d-none');
            document.getElementById('successMessage').classList.add('d-none');
            ['fullName', 'phoneNumber', 'email', 'supportTopic', 'subject', 'description'].forEach(field => {
                document.getElementById(`${field}Error`).textContent = '';
            });

            // Collect form data
            const formData = {
                fullName: document.getElementById('fullName').value.trim(),
                phoneNumber: document.getElementById('phoneNumber').value.trim(),
                email: document.getElementById('email').value.trim(),
                supportTopic: document.getElementById('supportTopic').value,
                subject: document.getElementById('subject').value.trim(),
                description: document.getElementById('description').value.trim()
            };

            // Get CSRF token
            const csrfToken = document.querySelector('input[name="_csrf"]').value;

            // Disable submit button
            submitButton.disabled = true;
            submitButton.innerHTML = 'Đang gửi...';

            try {
                const response = await fetch('/support/submit', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken // Include CSRF token
                    },
                    body: JSON.stringify(formData)
                });

                const result = await response.json();
                console.log('Server response:', result);

                if (response.ok && result.success) {
                    document.getElementById('successMessage').textContent = result.message || 'Yêu cầu hỗ trợ đã được gửi thành công!';
                    document.getElementById('successMessage').classList.remove('d-none');
                    supportForm.reset();
                } else {
                    if (result.errors) {
                        if (result.errors.general) {
                            document.getElementById('generalError').textContent = result.errors.general;
                            document.getElementById('generalError').classList.remove('d-none');
                        } else {
                            for (const [field, message] of Object.entries(result.errors)) {
                                const errorElement = document.getElementById(`${field}Error`);
                                if (errorElement) errorElement.textContent = message;
                            }
                        }
                    } else {
                        document.getElementById('generalError').textContent = `Lỗi server: ${response.status} - ${result.message || 'Vui lòng thử lại.'}`;
                        document.getElementById('generalError').classList.remove('d-none');
                    }
                }
            } catch (error) {
                console.error('Error:', error);
                document.getElementById('generalError').textContent = 'Lỗi kết nối. Vui lòng thử lại sau.';
                document.getElementById('generalError').classList.remove('d-none');
            } finally {
                submitButton.disabled = false;
                submitButton.innerHTML = 'Gửi Yêu Cầu';
            }
        });
    } else {
        console.error('supportForm not found');
    }
});