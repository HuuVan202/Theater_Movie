// Sử dụng các biến toàn cục đã được khởi tạo từ HTML
const existingUsernames = window.existingUsernames || new Set();
const existingEmails = window.existingEmails || new Set();
const existingIdentityCards = window.existingIdentityCards || new Set();
const existingPhoneNumbers = window.existingPhoneNumbers || new Set();

// Lưu trữ giá trị ban đầu của employee hiện tại để so sánh
let originalEmployeeData = {};

// DEBUG: Log để kiểm tra dữ liệu có được load không
console.log('Existing usernames:', existingUsernames);
console.log('Existing emails:', existingEmails);

document.addEventListener('DOMContentLoaded', function() {
    setupDateLimits();
    setupInputAnimations();
    setupFormValidation();
    checkServerErrors();
    saveOriginalData(); // Lưu dữ liệu ban đầu
});

function saveOriginalData() {
    const fields = [
        'username', 'email', 'identityCard', 'phoneNumber',
        'fullName', 'address', 'position', 'salary'
    ];

    fields.forEach(fieldId => {
        const element = document.getElementById(fieldId);
        if (element) {
            originalEmployeeData[fieldId] = element.value.trim();
            console.log(`Saved ${fieldId}:`, originalEmployeeData[fieldId]); // Debug
            if (originalEmployeeData[fieldId]) {
                element.parentElement.classList.add('valid');
            }
        }
    });

    // Log existingEmails để kiểm tra
    console.log('Existing emails from server:', Array.from(existingEmails));
}

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
        input.addEventListener('blur', () => {
            input.parentElement.classList.remove('focused');
            // Tự động validate khi blur nếu có giá trị hoặc đã từng có interaction
            if (input.value.trim() || input.dataset.hasInteraction) {
                checkField(input.id);
            }
        });

        // Đánh dấu đã có interaction khi người dùng gõ
        input.addEventListener('input', () => {
            input.dataset.hasInteraction = 'true';
        });
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
            // Hiển thị thông báo lỗi từ server
            feedback.textContent = feedback.getAttribute('data-error') || 'Dữ liệu không hợp lệ';
            feedback.className = 'feedback error';
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
    // Kiểm tra lại giá trị sau khi xóa
    if (document.getElementById(fieldId).value.trim()) {
        checkField(fieldId);
    }
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

    // Nếu người dùng chưa tương tác với field này và giá trị vẫn như ban đầu
    if (!fieldInput.dataset.hasInteraction && isOriginalValue(fieldId, value)) {
        // Giữ trạng thái valid ban đầu, không hiển thị lỗi
        container.classList.add('valid');
        // feedback.className = 'feedback';
        feedback.textContent = '';
        fieldInput.setCustomValidity('');
        return;
    }

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

// Hàm kiểm tra xem giá trị hiện tại có phải là giá trị ban đầu không
function isOriginalValue(fieldId, currentValue) {
    return originalEmployeeData[fieldId] === currentValue;
}

function validateUsername(value, container, feedback, fieldInput) {
    const usernameRegex = /^[a-zA-Z0-9_]+$/;

    // DEBUG: Kiểm tra giá trị
    console.log('Validating username:', value);
    console.log('existingUsernames has value:', existingUsernames.has(value));
    console.log('existingUsernames size:', existingUsernames.size);
    console.log('Is original value:', isOriginalValue('username', value));

    if (!value) {
        showFieldError(fieldInput, 'Tên đăng nhập không được để trống');
    } else if (value.length < 3 || value.length > 50) {
        showFieldError(fieldInput, 'Tên đăng nhập phải từ 3 đến 50 ký tự');
    } else if (!usernameRegex.test(value)) {
        showFieldError(fieldInput, 'Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới');
    } else if (existingUsernames.has(value) && !isOriginalValue('username', value)) {
        // Chỉ báo lỗi nếu username đã tồn tại VÀ không phải là giá trị ban đầu
        showFieldError(fieldInput, 'Tên đăng nhập đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validatePassword(value, container, feedback, fieldInput) {
    // Nếu chưa có interaction và password trống (giá trị ban đầu)
    if (!fieldInput.dataset.hasInteraction && !value) {
        showFieldSuccess(container, feedback, fieldInput);
        return;
    }

    // Đối với edit, password có thể để trống (không thay đổi)
    if (!value) {
        // Nếu password trống trong edit mode, coi như hợp lệ (không thay đổi password)
        showFieldSuccess(container, feedback, fieldInput);
        // Clear confirm password validation
        const confirmPasswordInput = document.getElementById('confirmPassword');
        if (confirmPasswordInput.value.trim() === '') {
            const confirmContainer = confirmPasswordInput.parentElement;
            const confirmFeedback = document.getElementById('confirmPasswordFeedback');
            showFieldSuccess(confirmContainer, confirmFeedback, confirmPasswordInput);
        }
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
    const passwordInput = document.getElementById('password');

    // Nếu chưa có interaction với cả password và confirm password
    if (!fieldInput.dataset.hasInteraction && !passwordInput.dataset.hasInteraction &&
        !passwordValue && !value) {
        showFieldSuccess(container, feedback, fieldInput);
        return;
    }

    // Nếu cả password và confirm password đều trống, coi như hợp lệ (không thay đổi password)
    if (!passwordValue && !value) {
        showFieldSuccess(container, feedback, fieldInput);
    } else if (!passwordValue && value) {
        showFieldError(fieldInput, 'Vui lòng nhập mật khẩu mới trước');
    } else if (passwordValue && !value) {
        showFieldError(fieldInput, 'Vui lòng xác nhận mật khẩu');
    } else if (value !== passwordValue) {
        showFieldError(fieldInput, 'Xác nhận mật khẩu không khớp');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateEmail(value, container, feedback, fieldInput) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    console.log('Validating email:', value);
    console.log('existingEmails has value:', existingEmails.has(value));
    console.log('Is original value:', isOriginalValue('email', value));

    if (!value) {
        showFieldError(fieldInput, 'Email không được để trống');
    } else if (!emailRegex.test(value)) {
        showFieldError(fieldInput, 'Email không hợp lệ');
    } else if (existingEmails.has(value) && !isOriginalValue('email', value)) {
        showFieldError(fieldInput, 'Email đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validateIdentityCard(value, container, feedback, fieldInput) {
    console.log('Validating identity card:', value);
    console.log('existingIdentityCards has value:', existingIdentityCards.has(value));
    console.log('Is original value:', isOriginalValue('identityCard', value));

    if (!value) {
        showFieldError(fieldInput, 'CMND/CCCD không được để trống');
    } else if (!isValidIdentityCard(value)) {
        showFieldError(fieldInput, 'CMND/CCCD phải đúng 12 chữ số');
    } else if (existingIdentityCards.has(value) && !isOriginalValue('identityCard', value)) {
        // Chỉ báo lỗi nếu CCCD đã tồn tại VÀ không phải là giá trị ban đầu
        showFieldError(fieldInput, 'CMND/CCCD đã tồn tại. Vui lòng thử lại');
    } else {
        showFieldSuccess(container, feedback, fieldInput);
    }
}

function validatePhoneNumber(value, container, feedback, fieldInput) {
    console.log('Validating phone number:', value);
    console.log('existingPhoneNumbers has value:', existingPhoneNumbers.has(value));
    console.log('Is original value:', isOriginalValue('phoneNumber', value));

    if (!value) {
        showFieldError(fieldInput, 'Số điện thoại không được để trống');
    } else if (!isValidPhoneNumber(value)) {
        showFieldError(fieldInput, 'Số điện thoại phải đúng 10 chữ số');
    } else if (existingPhoneNumbers.has(value) && !isOriginalValue('phoneNumber', value)) {
        // Chỉ báo lỗi nếu số điện thoại đã tồn tại VÀ không phải là giá trị ban đầu
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
    if (!hireDate || !dateOfBirth) {
        console.error("Ngày thuê hoặc ngày sinh không hợp lệ");
        return false;
    }
    const hire = new Date(hireDate);
    const birth = new Date(dateOfBirth);
    if (isNaN(hire) || isNaN(birth)) {
        console.error("Không thể phân tích ngày tháng");
        return false;
    }
    const today = new Date();
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

function validateForm() {
    const requiredFields = [
        'username', 'email', 'identityCard', 'phoneNumber',
        'fullName', 'dateOfBirth', 'gender', 'address', 'hireDate', 'position', 'salary'
    ];
    let isValid = true;

    requiredFields.forEach(fieldId => {
        const input = document.getElementById(fieldId);
        if (input) {
            checkField(fieldId); // Luôn kiểm tra, bất kể interaction
            if (!input.checkValidity()) {
                isValid = false;
            }
        }
    });

    // Kiểm tra password chỉ khi có nhập
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    if (passwordInput.value.trim() || confirmPasswordInput.value.trim()) {
        checkField('password');
        checkField('confirmPassword');
        if (!passwordInput.checkValidity() || !confirmPasswordInput.checkValidity()) {
            isValid = false;
        }
    }

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