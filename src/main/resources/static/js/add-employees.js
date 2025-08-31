// Sử dụng các biến toàn cục đã được khởi tạo từ HTML
const existingUsernames = window.existingUsernames || new Set();
const existingEmails = window.existingEmails || new Set();
const existingIdentityCards = window.existingIdentityCards || new Set();
const existingPhoneNumbers = window.existingPhoneNumbers || new Set();

// DEBUG: Log để kiểm tra dữ liệu có được load không
console.log('Existing usernames:', existingUsernames);
console.log('Existing emails:', existingEmails);

document.addEventListener('DOMContentLoaded', function() {
    setupDateLimits();
    setupInputAnimations();
    setupFormValidation();
    checkServerErrors();
});

function setupDateLimits() {
    const today = new Date();
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    const hireDateInput = document.getElementById('hireDate');

    const minBirthDate = new Date(today.getFullYear() - 65, today.getMonth(), today.getDate());
    const maxBirthDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());

    dateOfBirthInput.min = minBirthDate.toISOString().split('T')[0];
    dateOfBirthInput.max = maxBirthDate.toISOString().split('T')[0];
    hireDateInput.max = today.toISOString().split('T')[0];
}

function setupInputAnimations() {
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('focus', () => input.parentElement.classList.add('focused'));
        input.addEventListener('blur', () => input.parentElement.classList.remove('focused'));
    });
}

function setupFormValidation() {
    const form = document.querySelector('.employee-form');
    form.addEventListener('submit', function(e) {
        if (!validateForm()) {
            e.preventDefault();
            showFormError('Vui lòng sửa các lỗi trước khi gửi.');
        }
    });
}

function checkServerErrors() {
    const feedbackElements = document.querySelectorAll('.feedback.error');
    feedbackElements.forEach(feedback => {
        const fieldId = feedback.id.replace('Feedback', '');
        const fieldInput = document.getElementById(fieldId);
        if (fieldInput) {
            fieldInput.value = '';
            fieldInput.parentElement.classList.remove('valid');
        }
    });
}

function togglePassword(fieldId) {
    const passwordInput = document.getElementById(fieldId);
    const toggleIcon = document.getElementById(fieldId + 'ToggleIcon');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.remove('fa-eye');
        toggleIcon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.remove('fa-eye-slash');
        toggleIcon.classList.add('fa-eye');
    }
}

function isValidPassword(password) {
    const passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    return passwordRegex.test(password);
}

function isValidPhoneNumber(phoneNumber) {
    const phoneRegex = /^\d{10}$/;
    return phoneRegex.test(phoneNumber);
}

function isValidIdentityCard(identityCard) {
    const idRegex = /^\d{12}$/;
    return idRegex.test(identityCard);
}

function handleDateChange(fieldId) {
    const fieldInput = document.getElementById(fieldId);
    const container = fieldInput.parentElement;
    const feedback = document.getElementById(`${fieldId}Feedback`);

    if (fieldInput.value) {
        if (fieldId === 'dateOfBirth') {
            if (isValidDateOfBirth(fieldInput.value)) {
                container.classList.add('valid');
                feedback.className = 'feedback';
                feedback.textContent = '';
                updateHireDateLimits();
            } else {
                showFieldError(fieldInput, 'Nhân viên phải từ 18 đến 65 tuổi');
            }
        } else if (fieldId === 'hireDate') {
            const dateOfBirth = document.getElementById('dateOfBirth').value;
            if (dateOfBirth && isValidHireDate(fieldInput.value, dateOfBirth)) {
                container.classList.add('valid');
                feedback.className = 'feedback';
                feedback.textContent = '';
            } else {
                showFieldError(fieldInput, 'Ngày tuyển dụng phải sau ngày sinh và không sau ngày hiện tại');
            }
        }
    } else {
        container.classList.remove('valid');
    }
}

function updateHireDateLimits() {
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    const hireDateInput = document.getElementById('hireDate');

    if (dateOfBirthInput.value) {
        const birthDate = new Date(dateOfBirthInput.value);
        const minHireDate = new Date(birthDate.getFullYear() + 18, birthDate.getMonth(), birthDate.getDate());

        hireDateInput.min = minHireDate.toISOString().split('T')[0];

        if (hireDateInput.value && new Date(hireDateInput.value) < minHireDate) {
            hireDateInput.value = '';
            hireDateInput.parentElement.classList.remove('valid');
            showFieldError(hireDateInput, 'Ngày tuyển dụng phải ít nhất 18 năm sau ngày sinh');
        }
    }
}

function clearValidation(fieldId) {
    const container = document.getElementById(fieldId).parentElement;
    const feedback = document.getElementById(`${fieldId}Feedback`);

    container.classList.remove('valid');
    feedback.className = 'feedback';
    feedback.textContent = '';
    document.getElementById(fieldId).setCustomValidity('');
}

function clearDateValidation(fieldId) {
    const fieldInput = document.getElementById(fieldId);
    const container = fieldInput.parentElement;
    const feedback = document.getElementById(`${fieldId}Feedback`);

    if (!fieldInput.value.trim()) {
        container.classList.remove('valid');
        feedback.className = 'feedback';
        feedback.textContent = '';
        fieldInput.setCustomValidity('');
    }
}

function checkField(fieldId) {
    const fieldInput = document.getElementById(fieldId);
    const container = fieldInput.parentElement;
    const feedback = document.getElementById(`${fieldId}Feedback`);
    const value = fieldInput.value.trim();

    container.classList.remove('valid');

    switch (fieldId) {
        case 'username':
            validateUsername(value, container, feedback, fieldInput);
            break;
        case 'password':
            validatePassword(value, container, feedback, fieldInput);
            break;
        case 'confirmPassword':
            validateConfirmPassword(value, container, feedback, fieldInput);
            break;
        case 'email':
            validateEmail(value, container, feedback, fieldInput);
            break;
        case 'identityCard':
            validateIdentityCard(value, container, feedback, fieldInput);
            break;
        case 'phoneNumber':
            validatePhoneNumber(value, container, feedback, fieldInput);
            break;
        case 'fullName':
            validateFullName(value, container, feedback, fieldInput);
            break;
        case 'address':
            validateAddress(value, container, feedback, fieldInput);
            break;
        case 'position':
            validatePosition(value, container, feedback, fieldInput);
            break;
        case 'salary':
            validateSalary(value, container, feedback, fieldInput);
            break;
    }
}

function validateUsername(value, container, feedback, fieldInput) {
    const usernameRegex = /^[a-zA-Z0-9_]+$/;

    // DEBUG: Kiểm tra giá trị
    console.log('Validating username:', value);
    console.log('existingUsernames has value:', existingUsernames.has(value));
    console.log('existingUsernames size:', existingUsernames.size);

    if (!value) {
        showFieldError(fieldInput, 'Tên đăng nhập không được để trống');
    } else if (value.length < 3 || value.length > 50) {
        showFieldError(fieldInput, 'Tên đăng nhập phải từ 3 đến 50 ký tự');
    } else if (!usernameRegex.test(value)) {
        showFieldError(fieldInput, 'Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới');
    } else if (existingUsernames.has(value)) {
        showFieldError(fieldInput, 'Tên đăng nhập đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validatePassword(value, container, feedback, fieldInput) {
    if (!value) {
        showFieldError(fieldInput, 'Mật khẩu không được để trống');
    } else if (!isValidPassword(value)) {
        showFieldError(fieldInput, 'Mật khẩu phải có ít nhất 8 ký tự, gồm 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
        const confirmPasswordInput = document.getElementById('confirmPassword');
        if (confirmPasswordInput.value.trim()) {
            checkField('confirmPassword');
        }
    }
}

function validateConfirmPassword(value, container, feedback, fieldInput) {
    const passwordValue = document.getElementById('password').value.trim();
    if (!value) {
        showFieldError(fieldInput, 'Vui lòng xác nhận mật khẩu');
    } else if (value !== passwordValue) {
        showFieldError(fieldInput, 'Xác nhận mật khẩu không khớp');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateEmail(value, container, feedback, fieldInput) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    console.log('Validating email:', value);
    console.log('existingEmails has value:', existingEmails.has(value));

    if (!value) {
        showFieldError(fieldInput, 'Email không được để trống');
    } else if (!emailRegex.test(value)) {
        showFieldError(fieldInput, 'Email không hợp lệ');
    } else if (existingEmails.has(value)) {
        showFieldError(fieldInput, 'Email đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateIdentityCard(value, container, feedback, fieldInput) {
    console.log('Validating identity card:', value);
    console.log('existingIdentityCards has value:', existingIdentityCards.has(value));

    if (!value) {
        showFieldError(fieldInput, 'CMND/CCCD không được để trống');
    } else if (!isValidIdentityCard(value)) {
        showFieldError(fieldInput, 'CMND/CCCD phải đúng 12 chữ số');
    } else if (existingIdentityCards.has(value)) {
        showFieldError(fieldInput, 'CMND/CCCD đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validatePhoneNumber(value, container, feedback, fieldInput) {
    console.log('Validating phone number:', value);
    console.log('existingPhoneNumbers has value:', existingPhoneNumbers.has(value));

    if (!value) {
        showFieldError(fieldInput, 'Số điện thoại không được để trống');
    } else if (!isValidPhoneNumber(value)) {
        showFieldError(fieldInput, 'Số điện thoại phải đúng 10 chữ số');
    } else if (existingPhoneNumbers.has(value)) {
        showFieldError(fieldInput, 'Số điện thoại đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateFullName(value, container, feedback, fieldInput) {
    if (!value) {
        showFieldError(fieldInput, 'Họ và tên không được để trống');
    } else if (value.length < 2 || value.length > 150) {
        showFieldError(fieldInput, 'Họ và tên phải từ 2 đến 150 ký tự');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateAddress(value, container, feedback, fieldInput) {
    if (!value) {
        showFieldError(fieldInput, 'Địa chỉ không được để trống');
    } else if (value.length < 5 || value.length > 200) {
        showFieldError(fieldInput, 'Địa chỉ phải từ 5 đến 200 ký tự');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validatePosition(value, container, feedback, fieldInput) {
    if (!value) {
        showFieldError(fieldInput, 'Chức vụ không được để trống');
    } else if (value.length < 2 || value.length > 50) {
        showFieldError(fieldInput, 'Chức vụ phải từ 2 đến 50 ký tự');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateSalary(value, container, feedback, fieldInput) {
    const salary = parseFloat(value);
    if (!value) {
        showFieldError(fieldInput, 'Lương không được để trống');
    } else if (isNaN(salary) || salary <= 0) {
        showFieldError(fieldInput, 'Lương phải lớn hơn 0');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function isValidDateOfBirth(dateOfBirth) {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    const minAgeDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    const maxAgeDate = new Date(today.getFullYear() - 65, today.getMonth(), today.getDate());
    return birthDate <= minAgeDate && birthDate >= maxAgeDate;
}

function isValidHireDate(hireDate, dateOfBirth) {
    const today = new Date();
    const hire = new Date(hireDate);
    const birth = new Date(dateOfBirth);
    const minHireDate = new Date(birth.getFullYear() + 18, birth.getMonth(), birth.getDate());
    return hire <= today && hire >= minHireDate;
}

function showFieldError(fieldInput, message) {
    const feedback = document.getElementById(`${fieldInput.id}Feedback`);
    feedback.className = 'feedback error';
    feedback.textContent = message;
    fieldInput.setCustomValidity(message);
    fieldInput.parentElement.classList.remove('valid');
}

function showFieldSuccess(container, feedback, fieldInput) {
    feedback.className = 'feedback';
    feedback.textContent = '';
    fieldInput.setCustomValidity('');
    container.classList.add('valid');
}

function showFormError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    const form = document.querySelector('.employee-form');
    form.insertBefore(errorDiv, form.firstChild);
    setTimeout(() => errorDiv.remove(), 5000);
}

function validateForm() {
    const requiredFields = [
        'username', 'password', 'confirmPassword', 'email', 'identityCard', 'phoneNumber',
        'fullName', 'dateOfBirth', 'gender', 'address', 'hireDate', 'position', 'salary'
    ];
    let isValid = true;

    requiredFields.forEach(fieldId => {
        const input = document.getElementById(fieldId);
        if (input && !input.checkValidity()) {
            isValid = false;
            checkField(fieldId);
        }
    });

    const genderInputs = document.querySelectorAll('input[name="gender"]');
    let genderSelected = false;
    genderInputs.forEach(input => {
        if (input.checked) genderSelected = true;
    });
    if (!genderSelected) {
        isValid = false;
        showFormError('Vui lòng chọn giới tính');
    }

    return isValid;
}

function autoCreateUsername() {
    let newUsername;
    let attempts = 0;
    const maxAttempts = 100;

    do {
        if (attempts >= maxAttempts) {
            showFormError('Không thể tạo tên đăng nhập duy nhất. Vui lòng nhập thủ công.');
            return;
        }
        const randomNum = String(Math.floor(Math.random() * 999999) + 1).padStart(6, '0');
        newUsername = `EMP${randomNum}`;
        attempts++;
    } while (existingUsernames.has(newUsername));

    const usernameInput = document.getElementById('username');
    usernameInput.value = newUsername;
    checkField('username');
}