/**
 * Forgot Password Page JavaScript
 * Handles form validation, submission, and user interactions
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize the forgot password functionality
    initializeForgotPassword();
});

function initializeForgotPassword() {
    const form = document.getElementById('forgotPasswordForm');
    const emailInput = document.getElementById('email');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');

    if (!form || !emailInput || !submitBtn) {
        console.error('Forgot password form elements not found');
        return;
    }

    // Email validation
    emailInput.addEventListener('input', function() {
        validateEmail(this.value);
    });

    emailInput.addEventListener('blur', function() {
        validateEmail(this.value);
    });

    // Form submission
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const email = emailInput.value.trim();
        
        if (!validateEmail(email)) {
            showFieldError(emailInput, 'Please enter a valid email address');
            return;
        }

        submitForm(email);
    });

    // Auto-focus email input
    emailInput.focus();
}

/**
 * Validate email address
 * @param {string} email - Email address to validate
 * @returns {boolean} - True if valid, false otherwise
 */
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const emailInput = document.getElementById('email');
    const isValid = email && emailRegex.test(email);

    if (email === '') {
        clearFieldValidation(emailInput);
        return false;
    }

    if (isValid) {
        showFieldSuccess(emailInput);
        return true;
    } else {
        showFieldError(emailInput, 'Please enter a valid email address');
        return false;
    }
}

/**
 * Submit the forgot password form
 * @param {string} email - Email address
 */
function submitForm(email) {
    const submitBtn = document.getElementById('submitBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');

    // Show loading state
    setLoadingState(true);

    // Create form data
    const formData = new FormData();
    formData.append('email', email);

    // Submit form via fetch
    fetch('/forgot-password', {
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
            showSuccess(data.message || 'If an account with that email exists, we\'ve sent you a password reset link.');
            // Clear form
            document.getElementById('forgotPasswordForm').reset();
            clearFieldValidation(document.getElementById('email'));
        } else {
            showError(data.message || 'An error occurred. Please try again.');
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
        btnText.style.display = 'none';
        btnLoading.style.display = 'inline-flex';
    } else {
        submitBtn.disabled = false;
        submitBtn.classList.remove('loading');
        btnText.style.display = 'inline-flex';
        btnLoading.style.display = 'none';
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
    
    // Also update the page with success state
    setTimeout(() => {
        window.location.href = '/forgot-password?success=true';
    }, 2000);
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

/**
 * Utility function to escape HTML
 * @param {string} text - Text to escape
 * @returns {string} - Escaped text
 */
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function(m) {
        return map[m];
    });
}

/**
 * Handle network errors gracefully
 * @param {Error} error - Network error
 */
function handleNetworkError(error) {
    console.error('Network error:', error);
    
    let message = 'Network error occurred. Please check your connection and try again.';
    
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
        message = 'Unable to connect to the server. Please check your internet connection.';
    } else if (error.message.includes('timeout')) {
        message = 'Request timed out. Please try again.';
    }
    
    showError(message);
}

// Export functions for potential use in other scripts
window.ForgotPassword = {
    validateEmail,
    showToast,
    setLoadingState
};
