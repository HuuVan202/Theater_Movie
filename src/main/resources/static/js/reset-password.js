/**
 * Reset Password Page JavaScript
 * Handles password validation, strength checking, and form submission
 */

document.addEventListener('DOMContentLoaded', function () {
    // Initialize the reset password functionality
    initializeResetPassword();
});

function initializeResetPassword() {
    const form = document.getElementById('resetPasswordForm');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const submitBtn = document.getElementById('submitBtn');

    if (!form || !newPasswordInput || !confirmPasswordInput || !submitBtn) {
        console.error('Reset password form elements not found');
        return;
    }

    // Initialize password toggle functionality
    initializePasswordToggle();

    // Password validation events
    newPasswordInput.addEventListener('input', function () {
        checkPasswordStrength(this.value);
        checkPasswordsMatch();
        updateSubmitButton();
    });

    confirmPasswordInput.addEventListener('input', function () {
        checkPasswordsMatch();
        updateSubmitButton();
    });

    // Form submission
    form.addEventListener('submit', function (e) {
        // e.preventDefault();

        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        if (!validatePasswords(newPassword, confirmPassword)) {
            return;
        }

        submitForm(newPassword);
    });

    // Initial password strength check
    checkPasswordStrength(newPasswordInput.value);
    updateSubmitButton();

    // Auto-focus new password input
    newPasswordInput.focus();
}

/**
 * Initialize password toggle functionality
 */
function initializePasswordToggle() {
    const toggleButtons = document.querySelectorAll('.password-toggle');

    toggleButtons.forEach(button => {
        button.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const targetInput = document.getElementById(targetId);
            const icon = this.querySelector('i');

            if (targetInput.type === 'password') {
                targetInput.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                targetInput.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
}

/**
 * Check password strength and update requirements
 * @param {string} password - Password to check
 */
function checkPasswordStrength(password) {
    const requirements = [
        {id: 'req-length', test: (pwd) => pwd.length >= 8, text: 'At least 8 characters'},
        {id: 'req-lowercase', test: (pwd) => /[a-z]/.test(pwd), text: 'Contains lowercase letter'},
        {id: 'req-uppercase', test: (pwd) => /[A-Z]/.test(pwd), text: 'Contains uppercase letter'},
        {id: 'req-number', test: (pwd) => /\d/.test(pwd), text: 'Contains number'},
        {
            id: 'req-special',
            test: (pwd) => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(pwd),
            text: 'Contains special character'
        }
    ];

    let validCount = 0;

    requirements.forEach(req => {
        const element = document.getElementById(req.id);
        if (!element) return;

        const icon = element.querySelector('i');
        const isValid = req.test(password);

        if (isValid) {
            validCount++;
            icon.classList.remove('fa-times-circle', 'text-danger');
            icon.classList.add('fa-check-circle', 'text-success');
            element.classList.add('valid');
            element.classList.remove('invalid');
        } else {
            icon.classList.remove('fa-check-circle', 'text-success');
            icon.classList.add('fa-times-circle', 'text-danger');
            element.classList.add('invalid');
            element.classList.remove('valid');
        }
    });

    // Update password strength meter if exists
    updatePasswordStrengthMeter(validCount, requirements.length);

    return validCount === requirements.length;
}

/**
 * Update password strength meter
 * @param {number} validCount - Number of valid requirements
 * @param {number} totalCount - Total number of requirements
 */
function updatePasswordStrengthMeter(validCount, totalCount) {
    const strengthMeter = document.querySelector('.strength-fill');
    const strengthText = document.querySelector('.strength-text');

    if (!strengthMeter || !strengthText) return;

    const percentage = (validCount / totalCount) * 100;
    strengthMeter.style.width = percentage + '%';

    // Remove all strength classes
    strengthMeter.classList.remove('strength-weak', 'strength-fair', 'strength-good', 'strength-strong');

    if (percentage <= 25) {
        strengthMeter.classList.add('strength-weak');
        strengthText.textContent = 'Weak';
        strengthText.style.color = '#f56565';
    } else if (percentage <= 50) {
        strengthMeter.classList.add('strength-fair');
        strengthText.textContent = 'Fair';
        strengthText.style.color = '#ed8936';
    } else if (percentage <= 75) {
        strengthMeter.classList.add('strength-good');
        strengthText.textContent = 'Good';
        strengthText.style.color = '#38b2ac';
    } else {
        strengthMeter.classList.add('strength-strong');
        strengthText.textContent = 'Strong';
        strengthText.style.color = '#48bb78';
    }
}

/**
 * Check if passwords match
 * @returns {boolean} - True if passwords match
 */
function checkPasswordsMatch() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const matchReq = document.getElementById('req-match');

    if (!matchReq) return true;

    const icon = matchReq.querySelector('i');

    if (confirmPassword === '') {
        // No confirm password entered yet
        icon.classList.remove('fa-check-circle', 'text-success', 'fa-times-circle', 'text-danger');
        icon.classList.add('fa-times-circle', 'text-danger');
        matchReq.classList.remove('valid');
        matchReq.classList.add('invalid');
        return false;
    }

    const passwordsMatch = newPassword === confirmPassword;

    if (passwordsMatch) {
        icon.classList.remove('fa-times-circle', 'text-danger');
        icon.classList.add('fa-check-circle', 'text-success');
        matchReq.classList.add('valid');
        matchReq.classList.remove('invalid');
    } else {
        icon.classList.remove('fa-check-circle', 'text-success');
        icon.classList.add('fa-times-circle', 'text-danger');
        matchReq.classList.add('invalid');
        matchReq.classList.remove('valid');
    }

    return passwordsMatch;
}

/**
 * Update submit button state based on form validity
 */
function updateSubmitButton() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const submitBtn = document.getElementById('submitBtn');

    const isPasswordStrong = checkPasswordStrength(newPassword);
    const passwordsMatch = checkPasswordsMatch();
    const isFormValid = isPasswordStrong && passwordsMatch && newPassword.length > 0;

    submitBtn.disabled = !isFormValid;
}

/**
 * Validate both passwords
 * @param {string} newPassword - New password
 * @param {string} confirmPassword - Confirm password
 * @returns {boolean} - True if valid
 */
function validatePasswords(newPassword, confirmPassword) {
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // Clear previous validations
    clearFieldValidation(newPasswordInput);
    clearFieldValidation(confirmPasswordInput);

    if (!newPassword) {
        showFieldError(newPasswordInput, 'Password is required');
        return false;
    }

    if (!confirmPassword) {
        showFieldError(confirmPasswordInput, 'Please confirm your password');
        return false;
    }

    if (!checkPasswordStrength(newPassword)) {
        showFieldError(newPasswordInput, 'Password does not meet requirements');
        return false;
    }

    if (newPassword !== confirmPassword) {
        showFieldError(confirmPasswordInput, 'Passwords do not match');
        return false;
    }

    return true;
}

/**
 * Submit the reset password form
 * @param {string} newPassword - New password
 */
function submitForm(newPassword) {
    const submitBtn = document.getElementById('submitBtn');
    const token = document.querySelector('input[name="token"]').value;

    // Show loading state
    setLoadingState(true);

    // Create form data
    const formData = new FormData();
    formData.append('token', token);
    formData.append('newPassword', newPassword);
    formData.append('confirmPassword', newPassword);

    // Submit form via fetch
    fetch('/reset-password', {
        method: 'POST',
        body: formData,
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Network response was not ok');
        })
        .then(data => {
            setLoadingState(false);

            if (data.success) {
                showSuccess(data.message || 'Password has been reset successfully!');
                // Redirect to login page after success
                setTimeout(() => {
                    window.location.href = '/auth?message=password-reset-success';
                }, 2000);
            } else {
                showError(data.message || 'Failed to reset password. Please try again.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            setLoadingState(false);
            showError('An error occurred. Please check your connection and try again.');
        });
}

/**
 * Set loading state for the submit button
 * @param {boolean} isLoading - Loading state
 */
function setLoadingState(isLoading) {
    const submitBtn = document.getElementById('submitBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');

    if (isLoading) {
        submitBtn.disabled = true;
        submitBtn.classList.add('loading');
        if (btnText) btnText.style.display = 'none';
        if (btnLoading) btnLoading.style.display = 'inline-flex';
    } else {
        submitBtn.classList.remove('loading');
        if (btnText) btnText.style.display = 'inline-flex';
        if (btnLoading) btnLoading.style.display = 'none';
        // Don't enable button here - let updateSubmitButton handle it
        updateSubmitButton();
    }
}

/**
 * Show field error state
 * @param {HTMLElement} field - Input field element
 * @param {string} message - Error message
 */
function showFieldError(field, message) {
    field.classList.remove('is-valid');
    field.classList.add('is-invalid');

    const errorElement = field.parentElement.nextElementSibling;
    if (errorElement && errorElement.classList.contains('invalid-feedback')) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

/**
 * Show field success state
 * @param {HTMLElement} field - Input field element
 */
function showFieldSuccess(field) {
    field.classList.remove('is-invalid');
    field.classList.add('is-valid');

    const errorElement = field.parentElement.nextElementSibling;
    if (errorElement && errorElement.classList.contains('invalid-feedback')) {
        errorElement.style.display = 'none';
    }
}

/**
 * Clear field validation state
 * @param {HTMLElement} field - Input field element
 */
function clearFieldValidation(field) {
    field.classList.remove('is-valid', 'is-invalid');

    const errorElement = field.parentElement.nextElementSibling;
    if (errorElement && errorElement.classList.contains('invalid-feedback')) {
        errorElement.style.display = 'none';
    }
}

/**
 * Show success message
 * @param {string} message - Success message
 */
function showSuccess(message) {
    showToast('Success', message, 'success');
}

/**
 * Show error message
 * @param {string} message - Error message
 */
function showError(message) {
    showToast('Error', message, 'error');
}

/**
 * Show toast notification
 * @param {string} title - Toast title
 * @param {string} message - Toast message
 * @param {string} type - Toast type (success, error, info, warning)
 */
function showToast(title, message, type = 'info') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        console.warn('Toast container not found');
        return;
    }

    const toastId = 'toast_' + Date.now();
    const iconClass = getToastIcon(type);
    const bgClass = getToastBgClass(type);

    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body d-flex align-items-center">
                    <i class="${iconClass} me-2"></i>
                    <div>
                        <strong>${title}</strong><br>
                        ${message}
                    </div>
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    toastContainer.insertAdjacentHTML('beforeend', toastHTML);

    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 5000
    });

    toast.show();

    // Remove toast element after it's hidden
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

/**
 * Get toast icon class based on type
 * @param {string} type - Toast type
 * @returns {string} - Icon class
 */
function getToastIcon(type) {
    switch (type) {
        case 'success':
            return 'fas fa-check-circle';
        case 'error':
            return 'fas fa-exclamation-triangle';
        case 'warning':
            return 'fas fa-exclamation-circle';
        default:
            return 'fas fa-info-circle';
    }
}

/**
 * Get toast background class based on type
 * @param {string} type - Toast type
 * @returns {string} - Background class
 */
function getToastBgClass(type) {
    switch (type) {
        case 'success':
            return 'text-bg-success';
        case 'error':
            return 'text-bg-danger';
        case 'warning':
            return 'text-bg-warning';
        default:
            return 'text-bg-info';
    }
}

// Export functions for potential use in other scripts
window.ResetPassword = {
    checkPasswordStrength,
    checkPasswordsMatch,
    showToast,
    setLoadingState
};
