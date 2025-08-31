/**
 * OTP Verification JavaScript
 * Handles OTP input, validation, timer, and form submission
 */

class OTPVerification {
    constructor() {
        this.otpInputs = document.querySelectorAll('.otp-input');
        this.verifyBtn = document.getElementById('verifyBtn');
        this.otpCodeInput = document.getElementById('otpCode');
        this.resendBtn = document.getElementById('resendBtn');
        this.timerElement = document.getElementById('countdown');
        this.timerSection = document.querySelector('.otp-timer');

        this.init();
    }

    init() {
        this.setupOTPInputs();
        this.setupFormSubmission();
        this.setupResendButton();
        this.startTimer(300); // Initialize timer with 5 minutes
        this.focusFirstInput();
    }

    setupOTPInputs() {
        this.otpInputs.forEach((input, index) => {
            // Input event handler
            input.addEventListener('input', (e) => this.handleInput(e, index));

            // Keydown event handler
            input.addEventListener('keydown', (e) => this.handleKeydown(e, index));

            // Paste event handler
            input.addEventListener('paste', (e) => this.handlePaste(e));
        });
    }

    handleInput(e, index) {
        const value = e.target.value;

        // Only allow numbers
        if (!/^\d$/.test(value) && value !== '') {
            e.target.value = '';
            return;
        }

        // Add filled class and move to next input
        if (value) {
            e.target.classList.add('filled');
            e.target.classList.remove('error');

            // Move to next input
            if (index < this.otpInputs.length - 1) {
                this.otpInputs[index + 1].focus();
            }
        } else {
            e.target.classList.remove('filled');
        }

        // Update complete OTP and verify button
        this.updateOTPCode();
    }

    handleKeydown(e, index) {
        // Handle backspace
        if (e.key === 'Backspace' && !e.target.value && index > 0) {
            this.otpInputs[index - 1].focus();
            this.otpInputs[index - 1].value = '';
            this.otpInputs[index - 1].classList.remove('filled');
            this.updateOTPCode();
        }

        // Handle arrow keys
        if (e.key === 'ArrowLeft' && index > 0) {
            e.preventDefault();
            this.otpInputs[index - 1].focus();
        }

        if (e.key === 'ArrowRight' && index < this.otpInputs.length - 1) {
            e.preventDefault();
            this.otpInputs[index + 1].focus();
        }

        // Handle Enter key
        if (e.key === 'Enter') {
            e.preventDefault();
            if (this.getOTPValue().length === 6) {
                this.submitForm();
            }
        }
    }

    handlePaste(e) {
        e.preventDefault();

        // Get pasted data
        const paste = (e.clipboardData || window.clipboardData).getData('text');
        const numbers = paste.replace(/\D/g, '').slice(0, 6);

        if (numbers.length === 6) {
            this.otpInputs.forEach((input, i) => {
                input.value = numbers[i] || '';
                if (numbers[i]) {
                    input.classList.add('filled');
                    input.classList.remove('error');
                }
            });
            this.updateOTPCode();

            // Focus the last input
            this.otpInputs[5].focus();
        }
    }

    updateOTPCode() {
        const otp = this.getOTPValue();
        this.otpCodeInput.value = otp;

        // Enable/disable verify button
        if (otp.length === 6) {
            this.enableVerifyButton();
        } else {
            this.disableVerifyButton();
        }
    }

    getOTPValue() {
        return Array.from(this.otpInputs).map(input => input.value).join('');
    }

    enableVerifyButton() {
        this.verifyBtn.disabled = false;
        this.verifyBtn.querySelector('.btn-text').textContent = 'Xác thực';
        this.verifyBtn.classList.remove('btn-disabled');
    }

    disableVerifyButton() {
        this.verifyBtn.disabled = true;
        const otp = this.getOTPValue();
        if (this.timerElement.textContent === 'Hết hạn') {
            this.verifyBtn.querySelector('.btn-text').textContent = 'Mã đã hết hạn';
        } else if (otp.length === 0) {
            this.verifyBtn.querySelector('.btn-text').textContent = 'Nhập mã OTP';
        } else {
            this.verifyBtn.querySelector('.btn-text').textContent = `Nhập đủ 6 số (${otp.length}/6)`;
        }
    }

    setupFormSubmission() {
        const form = document.getElementById('otpForm');
        form.addEventListener('submit', (e) => {
            const otp = this.getOTPValue();
            if (otp.length !== 6) {
                e.preventDefault();
                this.showInputError();
            } else if (this.timerElement.textContent === 'Hết hạn') {
                e.preventDefault();
                this.showMessage('Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới!', 'danger');
            } else {
                this.showSubmittingState();
            }
        });
    }

    showInputError() {
        this.otpInputs.forEach(input => {
            if (!input.value) {
                input.classList.add('error');
            }
        });

        setTimeout(() => {
            this.otpInputs.forEach(input => input.classList.remove('error'));
        }, 1000);

        this.showMessage('Vui lòng nhập đủ 6 chữ số!', 'danger');
    }

    showSubmittingState() {
        this.verifyBtn.disabled = true;
        this.verifyBtn.innerHTML = '<span class="otp-loading"></span>Đang xác thực...';
    }

    startTimer(duration) {
        let timer = duration, minutes, seconds;

        const interval = setInterval(() => {
            minutes = parseInt(timer / 60, 10);
            seconds = parseInt(timer % 60, 10);

            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;

            this.timerElement.textContent = minutes + ":" + seconds;

            if (--timer < 0) {
                clearInterval(interval);
                this.timerElement.textContent = "Hết hạn";
                this.disableVerifyButton();
                this.timerSection.classList.add('expired');
                this.resendBtn.classList.remove('disabled');
                this.showMessage('Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới!', 'danger');
            }
        }, 1000);
    }

    focusFirstInput() {
        this.otpInputs[0].focus();
    }

    clearInputs() {
        this.otpInputs.forEach(input => {
            input.value = '';
            input.classList.remove('filled', 'error');
        });
        this.updateOTPCode();
        this.focusFirstInput();
    }

    resetTimer(newTime = 300) {
        this.timerElement.textContent = '05:00';
        this.timerSection.classList.remove('expired');
        this.timerSection.style.color = '#d97706';
        this.startTimer(newTime);
    }

    showMessage(message, type) {
        // Remove existing alerts
        document.querySelectorAll('.alert').forEach(alert => alert.remove());

        // Create new alert
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} otp-alert`;
        alertDiv.style.marginTop = '20px';

        const icon = type === 'danger' ? 'exclamation-triangle' :
            type === 'success' ? 'check-circle' : 'info-circle';

        alertDiv.innerHTML = `<i class="fas fa-${icon}"></i> ${message}`;

        // Insert after instruction
        const instruction = document.querySelector('.otp-instruction');
        instruction.parentNode.insertBefore(alertDiv, instruction.nextSibling);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }

    setupResendButton() {
        this.resendBtn.addEventListener('click', (event) => {
            event.preventDefault(); // Prevent default link behavior

            const emailInput = document.querySelector('input[name="email"]');
            const email = emailInput ? emailInput.value : '';

            if (!email) {
                this.showMessage('Email không được tìm thấy. Vui lòng thử lại.', 'danger');
                return;
            } else {
                this.showMessage('Mã OTP mới đang được gửi!', 'success');
            }

            // Disable resend button to prevent multiple clicks
            this.resendBtn.setAttribute('disabled', 'true');
            this.resendBtn.innerHTML = '<span class="otp-loading"></span>Đang gửi...';

            // Create a temporary form for submission
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/forgot';

            // Add email input
            const emailField = document.createElement('input');
            emailField.type = 'hidden';
            emailField.name = 'email';
            emailField.value = email;
            form.appendChild(emailField);

            // Add CSRF token if present
            const csrfToken = document.querySelector('meta[name="_csrf"]');
            if (csrfToken) {
                const csrfField = document.createElement('input');
                csrfField.type = 'hidden';
                csrfField.name = '_csrf';
                csrfField.value = csrfToken.content;
                form.appendChild(csrfField);
            }

            // Append form to body and submit
            document.body.appendChild(form);
            form.submit();
        });
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function () {
    // Initialize OTP verification
    window.otpVerification = new OTPVerification();

    // Add keyboard shortcuts
    document.addEventListener('keydown', function (e) {
        // Ctrl/Cmd + R to resend
        if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
            e.preventDefault();
            const resendBtn = document.getElementById('resendBtn');
            if (!resendBtn.hasAttribute('disabled')) {
                resendBtn.click();
            }
        }

        // Escape to clear inputs
        if (e.key === 'Escape') {
            window.otpVerification.clearInputs();
        }
    });

    // Add auto-submit after successful paste
    document.addEventListener('paste', function (e) {
        setTimeout(() => {
            const otp = window.otpVerification.getOTPValue();
            if (otp.length === 6) {
                // Auto-submit after 1 second if all fields are filled
                setTimeout(() => {
                    if (window.otpVerification.getOTPValue().length === 6) {
                        document.getElementById('otpForm').submit();
                    }
                }, 1000);
            }
        }, 100);
    });
});