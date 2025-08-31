//validate Username
document.addEventListener('DOMContentLoaded', function () {
    const usernameInput = document.querySelector('input[name="username"]');
    const sizeError = document.querySelector('#size-error');
    const patternError = document.querySelector('#pattern-error');
    const serverError = document.querySelector('#server-error-username');

    function updateValidations(username) {
        const sizeRegex = /^.{3,50}$/;
        const patternRegex = /^[a-zA-Z0-9_]+$/;
        let hasErrors = false;

        // Validate size (3-50 characters)
        if (!sizeRegex.test(username)) {
            sizeError.textContent = sizeError.textContent.replace('✓', '✗');
            sizeError.classList.remove('valid');
            sizeError.classList.add('error');
            hasErrors = true;
        } else {
            sizeError.textContent = sizeError.textContent.replace('✗', '✓');
            sizeError.classList.remove('error');
            sizeError.classList.add('valid');
        }

        // Validate pattern (letters, numbers, underscores only)
        if (!patternRegex.test(username)) {
            patternError.textContent = patternError.textContent.replace('✓', '✗');
            patternError.classList.remove('valid');
            patternError.classList.add('error');
            hasErrors = true;
        } else {
            patternError.textContent = patternError.textContent.replace('✗', '✓');
            patternError.classList.remove('error');
            patternError.classList.add('valid');
        }

        sizeError.style.display = 'block';
        patternError.style.display = 'block';
        // Hide server-side error when client-side validation is triggered
        if (serverError) {
            serverError.style.display = 'none';
        }

        return hasErrors;
    }

    usernameInput.addEventListener('focus', function () {
        const username = usernameInput.value;
        updateValidations(username);
    });

    usernameInput.addEventListener('blur', function () {
        const username = usernameInput.value;
        const hasErrors = updateValidations(username);
        if (!hasErrors) {
            sizeError.style.display = 'none';
            patternError.style.display = 'none';
        }
    });

    usernameInput.addEventListener('input', function () {
        const username = usernameInput.value;
        const hasErrors = updateValidations(username);
        if (document.activeElement !== usernameInput && !hasErrors) {
            sizeError.style.display = 'none';
            patternError.style.display = 'none';
        }
    });
});

//Validate Password
document.addEventListener('DOMContentLoaded', function () {
    const passwordInput = document.querySelector('input[name="password"]');
    const errorDiv = document.querySelector('#validation-messages');
    const serverError = document.querySelector('#server-error-password');
    const validations = [
        {text: 'Password must be at least 8 characters', check: val => val.length >= 8},
        {text: 'Include a digit', check: val => /[0-9]/.test(val)},
        {text: 'Include a special character', check: val => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(val)},
        {text: 'Include an uppercase letter', check: val => /[A-Z]/.test(val)},
        {text: 'No spaces', check: val => !/\s/.test(val)}
    ];

    function updateMessages(password) {
        let hasErrors = false;
        const messages = validations.map(val => {
            const isValid = val.check(password);
            if (!isValid) hasErrors = true;
            const symbol = isValid ? '✓' : '✗';
            const className = isValid ? 'valid' : 'error';
            return `<span class="${className}">${symbol} ${val.text}</span><br>`;
        });

        errorDiv.innerHTML = messages.join('');
        errorDiv.style.display = 'block';
        // Hide server-side error when client-side validation is triggered
        if (serverError) {
            serverError.style.display = 'none';
        }

        return hasErrors;
    }

    // Check for server-side error on page load
    function checkServerError() {
        if (serverError && window.getComputedStyle(serverError).display !== 'none') {
            errorDiv.style.display = 'none';
        }
    }

    checkServerError();

    passwordInput.addEventListener('focus', function () {
        const password = passwordInput.value;
        updateMessages(password);
    });

    passwordInput.addEventListener('blur', function () {
        const password = passwordInput.value;
        const hasErrors = updateMessages(password);
        if (!hasErrors) {
            errorDiv.style.display = 'none';
        }
    });

    passwordInput.addEventListener('input', function () {
        const password = passwordInput.value;
        const hasErrors = updateMessages(password);
        if (document.activeElement !== passwordInput && !hasErrors) {
            errorDiv.style.display = 'none';
        }
    });
});

//Check Match Password
document.addEventListener('DOMContentLoaded', function () {
    const passwordInput = document.querySelector('input[name="password"]');
    const confirmInput = document.querySelector('input[name="passwordConfirm"]');
    const checkMatchDiv = document.getElementById('checkMatch');

    function checkMatch() {
        const password = passwordInput.value;
        const confirmPassword = confirmInput.value;
        const hasMismatch = confirmPassword && password !== confirmPassword;

        if (hasMismatch) {
            checkMatchDiv.textContent = '✗ Passwords do not match';
            checkMatchDiv.style.color = 'red';
            checkMatchDiv.style.display = 'block';
        } else if (confirmPassword) {
            checkMatchDiv.textContent = '✓ Passwords match';
            checkMatchDiv.style.color = 'green';
            checkMatchDiv.style.display = 'block';
        } else {
            checkMatchDiv.textContent = '';
            checkMatchDiv.style.display = 'none';
        }

        return hasMismatch;
    }

    confirmInput.addEventListener('focus', function () {
        checkMatch();
    });

    confirmInput.addEventListener('blur', function () {
        const hasMismatch = checkMatch();
        if (!hasMismatch) {
            checkMatchDiv.style.display = 'none';
        }
    });

    confirmInput.addEventListener('input', function () {
        const hasMismatch = checkMatch();
        if (document.activeElement !== confirmInput && !hasMismatch) {
            checkMatchDiv.style.display = 'none';
        }
    });

    passwordInput.addEventListener('input', checkMatch);
});

document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('#register form');
    const termsCheckbox = document.querySelector('#terms-checkbox');

    form.addEventListener('submit', function (event) {
        if (!termsCheckbox.checked) {
            event.preventDefault();
            alert('Vui lòng đồng ý với các điều khoản trước khi đăng ký.');
        }
    });
});