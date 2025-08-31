document.addEventListener('DOMContentLoaded', function () {
    // Toggle between display and edit modes
    window.toggleEditMode = function () {
        const displayMode = document.getElementById('displayMode');
        const editMode = document.getElementById('editMode');
        const editBtn = document.getElementById('editBtn');

        if (displayMode.style.display === 'none') {
            displayMode.style.display = 'block';
            editMode.style.display = 'none';
            editBtn.innerHTML = '<i class="fas fa-edit me-1"></i><span class="d-none d-sm-inline">Edit Profile</span><span class="d-sm-none">Edit</span>';
        } else {
            displayMode.style.display = 'none';
            editMode.style.display = 'block';
            editBtn.innerHTML = '<i class="fas fa-times me-1"></i><span class="d-none d-sm-inline">Cancel Edit</span><span class="d-sm-none">Cancel</span>';
        }
    };

    // Cancel edit mode
    window.cancelEdit = function () {
        toggleEditMode();
        document.getElementById('profileForm').reset();
    };

    // Show toast notification
    function showToast(message, type = 'success') {
        const toastId = 'toast-' + new Date().getTime();
        const toastHTML = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;
        document.getElementById('toastContainer').innerHTML += toastHTML;
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
        setTimeout(() => toastElement.remove(), 5000);
    }

    // Handle profile form submission
    document.getElementById('profileForm').addEventListener('submit', function (event) {
        event.preventDefault();

        const formData = new FormData(this);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });

        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch('/profile/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    // Update display fields
                    document.getElementById('displayFullName').textContent = data.fullName || 'Not provided';
                    document.getElementById('displayEmail').textContent = data.email || 'email@example.com';
                    // Format dateOfBirth to dd/MM/yyyy
                    document.getElementById('displayBirthDate').textContent = data.dateOfBirth ?
                        data.dateOfBirth.split('-').reverse().join('/') : 'Not provided';
                    document.getElementById('displayGender').innerHTML =
                        data.gender === 'M' ? '<span class="badge bg-info fs-6"><i class="fas fa-mars me-1 text-white fs-5"></i> Male</span>' :
                            data.gender === 'F' ? '<span class="badge bg-warning fs-6"><i class="fas fa-venus me-1 text-white fs-5"></i> Female</span>' :
                                'Not specified';
                    document.getElementById('displayPhone').textContent = data.phoneNumber || 'Not provided';
                    document.getElementById('displayCitizenId').textContent = data.identityCard || 'Not provided';
                    document.getElementById('displayAddress').textContent = data.address || 'Not provided';
                    document.getElementById('displayName').textContent = data.fullName || 'User Name';
                    document.getElementById('displayUsername').textContent = data.username || 'username';

                    // Switch back to display mode
                    toggleEditMode();
                    calculateProfileCompleteness();
                    showToast('Profile updated successfully!', 'success');
                } else {
                    showToast(result.message || 'Failed to update profile.', 'danger');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('An error occurred while updating the profile.', 'danger');
            });
    });

});

// Calculate Profile Completeness
function calculateProfileCompleteness() {
    const fields = [
        document.getElementById('displayFullName')?.textContent?.trim(),
        document.getElementById('displayEmail')?.textContent?.trim(),
        document.getElementById('displayPhone')?.textContent?.trim(),
        document.getElementById('displayBirthDate')?.textContent?.trim(),
        document.getElementById('displayGender')?.textContent?.trim(),
        document.getElementById('displayCitizenId')?.textContent?.trim(),
        document.getElementById('displayAddress')?.textContent?.trim()
    ];

    const filledFields = fields.filter(field =>
        field && field !== 'Not provided' && field !== 'Not specified'
    ).length;

    const totalFields = fields.length;
    const completeness = Math.round((filledFields / totalFields) * 100);

    // Update the display
    const completenessElement = document.getElementById('profileCompleteness');
    if (completenessElement) {
        completenessElement.textContent = completeness + '%';

        // Update color based on completeness
        if (completeness >= 80) {
            completenessElement.className = 'text-success fs-4';
        } else if (completeness >= 60) {
            completenessElement.className = 'text-warning fs-4';
        } else {
            completenessElement.className = 'text-danger fs-4';
        }
    }


    return completeness;
}

// User Profile JavaScript
let isEditMode = false;

// Toggle password visibility
function togglePassword(fieldId) {
    const field = document.getElementById(fieldId);
    const button = field.nextElementSibling;
    const icon = button.querySelector('i');

    if (field.type === 'password') {
        field.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        field.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

// Reset password form
function resetPasswordForm() {
    document.getElementById('passwordForm').reset();
    // Reset all password strength indicators
    const requirements = ['req-length', 'req-lowercase', 'req-uppercase', 'req-number', 'req-special'];
    requirements.forEach(req => {
        const element = document.getElementById(req);
        const icon = element.querySelector('i');
        icon.classList.remove('fa-check-circle', 'text-success');
        icon.classList.add('fa-times-circle', 'text-danger');
    });
}

// Make showTab function globally accessible
window.showTab = function(tabId) {
    // Hide all tabs
    const tabPanes = document.querySelectorAll('.tab-pane');
    tabPanes.forEach(pane => {
        pane.classList.remove('show', 'active');
        pane.style.display = 'none';
    });

    // Show selected tab
    const selectedTab = document.getElementById(tabId);
    if (selectedTab) {
        selectedTab.classList.add('show', 'active', 'fade-in');
        selectedTab.style.display = 'block';
    }

    // Update navigation buttons state
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('data-tab') === tabId) {
            link.classList.add('active');
        }
    });
}

// Initialize page
document.addEventListener('DOMContentLoaded', function () {
    // Show first tab by default
    showTab('personal-info');

    // Calculate profile completeness
    calculateProfileCompleteness();

    // Update current time
    updateCurrentTime();
    setInterval(updateCurrentTime, 6000);

    // Initialize tooltips
    initializeTooltips();

    // Initialize any other components
    console.log('User profile page initialized');
});

// Update current time display
function updateCurrentTime() {
    const timeElement = document.querySelector('.current-time');
    if (timeElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('en-US', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit'
        });
        timeElement.textContent = timeString;
    }
}

// Initialize tooltips if Bootstrap is available
function initializeTooltips() {
    if (typeof bootstrap !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
}

// // Show toast notification
// function showToast(title, message, type = 'info') {
//     // Clear all previous toasts
//     const toastContainer = document.getElementById('toastContainer');
//     toastContainer.innerHTML = '';
//     const toastId = 'toast-' + Date.now();
//
//     const bgColor = type === 'success' ? 'bg-success' : type === 'error' ? 'bg-danger' : 'bg-primary';
//
//     const toastHtml = `
//         <div id="${toastId}" class="toast align-items-center text-white ${bgColor} border-0 mb-2" role="alert" aria-live="assertive" aria-atomic="true">
//             <div class="d-flex">
//                 <div class="toast-body">
//                     <strong>${title}</strong><br>
//                     ${message}
//                 </div>
//                 <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
//             </div>
//         </div>
//     `;
//
//     toastContainer.insertAdjacentHTML('beforeend', toastHtml);
//
//     const toastElement = document.getElementById(toastId);
//     const toast = new bootstrap.Toast(toastElement);
//     toast.show();
//
//     // Remove toast after it's hidden
//     toastElement.addEventListener('hidden.bs.toast', function () {
//         toastElement.remove();
//     });
// }

// Show/hide loading overlay
function showLoading(show) {
    let loadingOverlay = document.getElementById('loadingOverlay');

    if (show) {
        if (!loadingOverlay) {
            const overlayHtml = `
                <div class="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center loading-overlay"
                     id="loadingOverlay">
                    <div class="text-center text-white">
                        <div class="spinner-border text-custom-yellow mb-3" role="status" style="width: 3rem; height: 3rem;">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                        <div class="fs-4">Loading...</div>
                    </div>
                </div>
            `;
            document.body.insertAdjacentHTML('beforeend', overlayHtml);
        } else {
            loadingOverlay.classList.add('show');
        }
    } else {
        if (loadingOverlay) {
            loadingOverlay.remove();
        }
    }
}

// Password strength checker
document.addEventListener('DOMContentLoaded', function () {
    const newPasswordField = document.getElementById('newPassword');
    if (newPasswordField) {
        newPasswordField.addEventListener('input', function () {
            const password = this.value;
            checkPasswordStrength(password);
        });
    }

    // Calculate initial profile completeness
    calculateProfileCompleteness();
});

function checkPasswordStrength(password) {
    const requirements = {
        'req-length': password.length >= 8,
        'req-lowercase': /[a-z]/.test(password),
        'req-uppercase': /[A-Z]/.test(password),
        'req-number': /\d/.test(password),
        'req-special': /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };

    Object.entries(requirements).forEach(([reqId, met]) => {
        const element = document.getElementById(reqId);
        if (element) {
            const icon = element.querySelector('i');
            if (icon) {
                if (met) {
                    icon.classList.remove('fa-times-circle', 'text-danger');
                    icon.classList.add('fa-check-circle', 'text-success');
                } else {
                    icon.classList.remove('fa-check-circle', 'text-success');
                    icon.classList.add('fa-times-circle', 'text-danger');
                }
            }
        }
    });
}    // Date formatting function for DD/MM/YYYY
function formatDateInput(input) {
    let value = input.value.replace(/\D/g, '');

    if (value.length >= 2) {
        value = value.substring(0, 2) + '/' + value.substring(2);
    }
    if (value.length >= 5) {
        value = value.substring(0, 5) + '/' + value.substring(5, 9);
    }

    input.value = value;
}

// Validate date format
function isValidDate(dateString) {
    const regex = /^(\d{2})\/(\d{2})\/(\d{4})$/;
    const match = dateString.match(regex);

    if (!match) return false;

    const day = parseInt(match[1], 10);
    const month = parseInt(match[2], 10);
    const year = parseInt(match[3], 10);

    const date = new Date(year, month - 1, day);
    return date.getFullYear() === year &&
           date.getMonth() === month - 1 &&
           date.getDate() === day;
}

// Date input event listeners
const fromDateInput = document.getElementById('fromDate');
const toDateInput = document.getElementById('toDate');

if (fromDateInput) {
    fromDateInput.addEventListener('input', function() {
        formatDateInput(this);
    });

    fromDateInput.addEventListener('blur', function() {
        if (this.value && !isValidDate(this.value)) {
            showToast('Please enter a valid date in DD/MM/YYYY format', 'danger');
            this.focus();
        }
    });
}

if (toDateInput) {
    toDateInput.addEventListener('input', function() {
        formatDateInput(this);
    });

    toDateInput.addEventListener('blur', function() {
        if (this.value && !isValidDate(this.value)) {
            showToast('Please enter a valid date in DD/MM/YYYY format', 'danger');
            this.focus();
        }
    });
}

    // Handle score history filtering with enhanced validation
    function handleScoreHistoryFilter() {
        const fromDate = document.getElementById('fromDate').value.trim();
        const toDate = document.getElementById('toDate').value.trim();
        const historyType = document.querySelector('input[name="historyType"]:checked').value;

        // Show loading state
        showLoadingState();

        // Simulate API call (replace with actual API endpoint)
        setTimeout(() => {
            fetchScoreHistory(fromDate, toDate, historyType);
        }, 1000);
    }

// Parse date from DD/MM/YYYY to Date object
function parseDate(dateString) {
    const parts = dateString.split('/');
    return new Date(parts[2], parts[1] - 1, parts[0]);
}

// Show loading state
function showLoadingState() {
    document.getElementById('initialState').style.display = 'none';
    document.getElementById('noResultsState').style.display = 'none';
    document.getElementById('resultsTable').style.display = 'none';
    document.getElementById('loadingState').style.display = 'block';
}

// Mock data for demonstration
// Parse date from DD/MM/YYYY to Date object
function parseDate(dateString) {
    const parts = dateString.split('/');
    return new Date(parts[2], parts[1] - 1, parts[0]);
}

// Format date from ISO (yyyy-MM-dd) to DD/MM/YYYY
function formatISODateToDDMMYYYY(isoDate) {
    const date = new Date(isoDate);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

// Fetch score history from bookingList
function fetchScoreHistory(fromDate, toDate, historyType) {
    let data = window.bookingList || [];

    // Filter by score type (addScore or useScore) and date range
    data = data.filter(record => {
        const score = historyType === 'adding' ? record.addScore : record.useScore;
        if (score <= 0) return false; // Only include records with positive addScore or useScore

        const recordDate = new Date(record.bookingDate); // Sử dụng bookingDate
        const fromDateObj = fromDate ? parseDate(fromDate) : new Date('1900-01-01');
        const toDateObj = toDate ? parseDate(toDate) : new Date(); // Sử dụng ngày hiện tại (21/07/2025) thay vì 2100-12-31
        toDateObj.setHours(23, 59, 59, 999); // Đặt thời gian cuối ngày
        return recordDate >= fromDateObj && recordDate <= toDateObj;
    });

    displayScoreHistory(data, historyType);
}

// Display score history results
function displayScoreHistory(data, historyType) {
    document.getElementById('loadingState').style.display = 'none';

    if (data.length === 0) {
        document.getElementById('noResultsState').style.display = 'block';
        document.getElementById('resultsTable').style.display = 'none';
        return;
    }

    // Update results header
    const resultsTitle = document.getElementById('resultsTitle');
    const scoreColumnHeader = document.getElementById('scoreColumnHeader');
    const resultsCount = document.getElementById('resultsCount');

    resultsTitle.textContent = historyType === 'adding' ? 'Lịch sử cộng điểm' : 'Lịch sử sử dụng điểm';
    scoreColumnHeader.textContent = historyType === 'adding' ? 'Điểm cộng' : 'Điểm sử dụng';
    resultsCount.textContent = `${data.length} bản ghi`;

    // Populate table
    const tableBody = document.getElementById('scoreHistoryTableBody');
    tableBody.innerHTML = '';

    data.forEach(record => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="text-white">
                <i class="fas fa-calendar-day me-2 text-custom-yellow"></i>
                ${formatISODateToDDMMYYYY(record.bookingDate)} <!-- Sử dụng bookingDate -->
            </td>
            <td class="text-white">
                <i class="fas fa-film me-2 text-custom-yellow"></i>
                ${record.movieName || 'Không xác định'}
            </td>
            <td class="text-center">
                <span class="score-badge ${historyType === 'adding' ? 'added' : 'used'}">
                    <i class="fas fa-${historyType === 'adding' ? 'plus' : 'minus'} me-1"></i>
                    ${historyType === 'adding' ? record.addScore : record.useScore} điểm <!-- Sử dụng addScore hoặc useScore -->
                </span>
            </td>
        `;
        tableBody.appendChild(row);
    });

    // Calculate and display total score
    const totalScore = data.reduce((sum, record) => sum + (historyType === 'adding' ? record.addScore : record.useScore), 0);
    const totalRow = document.createElement('tr');
    totalRow.innerHTML = `
        <td colspan="2" class="text-white fw-bold text-end">Tổng cộng:</td>
        <td class="text-center">
            <span class="score-badge ${historyType === 'adding' ? 'added' : 'used'} fw-bold">
                ${totalScore} điểm
            </span>
        </td>
    `;
    tableBody.appendChild(totalRow);

    document.getElementById('resultsTable').style.display = 'block';
    document.getElementById('resultsTable').classList.add('results-enter');

    showToast(`Tìm thấy ${data.length} bản ghi cho lịch sử ${historyType === 'adding' ? 'cộng điểm' : 'sử dụng điểm'}`, 'success');
}

// Enhanced form validation
function validateScoreHistoryForm() {
    const fromDate = document.getElementById('fromDate').value.trim();
    const toDate = document.getElementById('toDate').value.trim();

    let isValid = true;
    let errorMessage = '';

    // Clear previous error states
    document.getElementById('fromDate').classList.remove('is-invalid');
    document.getElementById('toDate').classList.remove('is-invalid');

    // Validate from date
    if (fromDate && !isValidDate(fromDate)) {
        document.getElementById('fromDate').classList.add('is-invalid');
        errorMessage = 'Please enter a valid From Date in DD/MM/YYYY format';
        isValid = false;
    }

    // Validate to date
    if (toDate && !isValidDate(toDate)) {
        document.getElementById('toDate').classList.add('is-invalid');
        errorMessage = 'Please enter a valid To Date in DD/MM/YYYY format';
        isValid = false;
    }

    // Validate date range
    if (isValid && fromDate && toDate) {
        const fromDateObj = parseDate(fromDate);
        const toDateObj = parseDate(toDate);

        if (fromDateObj > toDateObj) {
            document.getElementById('fromDate').classList.add('is-invalid');
            document.getElementById('toDate').classList.add('is-invalid');
            errorMessage = 'From Date cannot be later than To Date';
            isValid = false;
        }

        // Check if date range is too far in the future
        const today = new Date();
        if (fromDateObj > today || toDateObj > today) {
            errorMessage = 'Cannot select future dates';
            isValid = false;
        }
    }

    if (!isValid) {
        showToast(errorMessage, 'danger');
    }

    return isValid;
}

// Add input validation styling
function addInputValidationStyling() {
    const style = document.createElement('style');
    style.textContent = `
        .form-control.is-invalid {
            border-color: #dc3545 !important;
            box-shadow: 0 0 10px rgba(220, 53, 69, 0.3) !important;
        }
        
        .form-control.is-valid {
            border-color: #28a745 !important;
            box-shadow: 0 0 10px rgba(40, 167, 69, 0.3) !important;
        }
    `;
    document.head.appendChild(style);
}

// Clear form and reset to initial state
function clearScoreHistoryForm() {
    document.getElementById('scoreFilterForm').reset();
    document.getElementById('fromDate').classList.remove('is-invalid', 'is-valid');
    document.getElementById('toDate').classList.remove('is-invalid', 'is-valid');

    // Reset to initial state
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('noResultsState').style.display = 'none';
    document.getElementById('resultsTable').style.display = 'none';
    document.getElementById('initialState').style.display = 'block';

    showToast('Form cleared successfully', 'info');
}

// Add clear button functionality
function addClearButton() {
    const filterForm = document.querySelector('#scoreFilterForm .row');
    if (filterForm) {
        const clearButtonCol = document.createElement('div');
        clearButtonCol.className = 'col-md-2 d-flex align-items-end';
        clearButtonCol.innerHTML = `
            <button type="button" class="btn btn-outline-warning w-100" onclick="clearScoreHistoryForm()">
                <i class="fas fa-eraser me-1"></i>Clear
            </button>
        `;
        filterForm.appendChild(clearButtonCol);

        // Adjust the View Score button column
        const submitButtonCol = filterForm.querySelector('.col-md-2');
        if (submitButtonCol) {
            submitButtonCol.className = 'col-md-1 d-flex align-items-end';
        }
    }
}

// Keyboard shortcuts
function addKeyboardShortcuts() {
    document.addEventListener('keydown', function(event) {
        // Ctrl/Cmd + Enter to submit form
        if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
            const activeTab = document.querySelector('.tab-pane.show.active');
            if (activeTab && activeTab.id === 'score-history') {
                event.preventDefault();
                const form = document.getElementById('scoreFilterForm');
                if (form) {
                    form.dispatchEvent(new Event('submit'));
                }
            }
        }

        // Escape to clear form
        if (event.key === 'Escape') {
            const activeTab = document.querySelector('.tab-pane.show.active');
            if (activeTab && activeTab.id === 'score-history') {
                clearScoreHistoryForm();
            }
        }
    });
}

// Initialize all enhancements
function initializeScoreHistoryEnhancements() {
    addInputValidationStyling();
    addKeyboardShortcuts();

    // Update the form submission handler to use enhanced validation
    const scoreFilterForm = document.getElementById('scoreFilterForm');
    if (scoreFilterForm) {
        scoreFilterForm.addEventListener('submit', function(event) {
            event.preventDefault();
            if (validateScoreHistoryForm()) {
                handleScoreHistoryFilter();
            }
        });
    }
}

// Call initialization
initializeScoreHistoryEnhancements();

// Initialize date inputs with placeholders and current date as max
function initializeDateInputs() {
    const today = new Date();
    const todayString = String(today.getDate()).padStart(2, '0') + '/' +
                       String(today.getMonth() + 1).padStart(2, '0') + '/' +
                       today.getFullYear();

    // Set today as default "To Date" if user wants to see recent history
    // document.getElementById('toDate').value = todayString;
}

// Initialize on page load
initializeDateInputs();

// Function to handle password form submission
document.getElementById('passwordForm').addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent the default form submission (page reload)

    // Get form data
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Get CSRF token and header from meta tags
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Create FormData object to send form data
    const formData = new FormData();
    formData.append('currentPassword', currentPassword);
    formData.append('newPassword', newPassword);
    formData.append('confirmPassword', confirmPassword);

    // Make AJAX request to the server
    fetch('/profile/change-password', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken // Include CSRF token in the request header
        },
        body: formData
    })
        .then(response => response.json()) // Parse JSON response
        .then(data => {
            // Show toast notification based on response
            showToast(data.success, data.message);
            if (data.success) {
                // Reset the form on successful password change
                resetPasswordForm();
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast(false, 'An error occurred while updating the password.');
        });
});

// Function to show toast notification
function showToast(success, message) {
    const toastContainer = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white ${success ? 'bg-success' : 'bg-danger'} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toast);

    // Initialize and show the toast
    const bootstrapToast = new bootstrap.Toast(toast);
    bootstrapToast.show();

    // Auto-remove the toast element after it hides
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}


// Optional: Add real-time password requirements validation
document.getElementById('newPassword').addEventListener('input', function () {
    const password = this.value;

    // Check password requirements
    const lengthCheck = password.length >= 8;
    const lowercaseCheck = /[a-z]/.test(password);
    const uppercaseCheck = /[A-Z]/.test(password);
    const numberCheck = /[0-9]/.test(password);
    const specialCheck = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    // Update UI for each requirement
    updateRequirement('req-length', lengthCheck);
    updateRequirement('req-lowercase', lowercaseCheck);
    updateRequirement('req-uppercase', uppercaseCheck);
    updateRequirement('req-number', numberCheck);
    updateRequirement('req-special', specialCheck);
});

// Function to update password requirement indicators
function updateRequirement(elementId, isValid) {
    const element = document.getElementById(elementId);
    const icon = element.querySelector('i');
    icon.className = `fas ${isValid ? 'fa-check-circle text-success' : 'fa-times-circle text-danger'} me-2`;
}

// Preview image before upload
function previewImage(event, input) {
    const file = event.target.files[0];
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (input.files[0].size > maxSize) {
        alert("File is too large! Maximum size is 5MB.");
        input.value = "";
    }
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('profileAvatar').src = e.target.result;
        };
        reader.readAsDataURL(file);
    }
}

//
// document.getElementById('uploadForm').addEventListener('submit', function (e) {
//     e.preventDefault();
//     const fileInput = this.querySelector('input[type="file"]');
//     const file = fileInput.files[0];
//
//     // Check file size (5MB = 5 * 1024 * 1024 bytes)
//     if (file && file.size > 5 * 1024 * 1024) {
//         alert('File is too large. Maximum size is 5MB.');
//         const profileAvatar = document.getElementById('profileAvatar');
//         if (profileAvatar) {
//             profileAvatar.src =
//                 profileAvatar.getAttribute('th:src') ||
//                 'https://via.placeholder.com/150/f9a825/0F1729?text=U';
//         }
//         window.location.reload(); // Refresh page after user clicks OK
//         return;
//     }
//
//     const formData = new FormData(this);
//
//     fetch('/profile/upload-image', {
//         method: 'POST',
//         body: formData,
//         headers: {
//             'X-Requested-With': 'XMLHttpRequest'
//         }
//     })
//         .then(response => {
//             if (!response.ok) {
//                 throw new Error('Network response was not ok');
//             }
//             return response.json();
//         })
//         .then(data => {
//             if (data.success) {
//                 document.getElementById('profileAvatar').src = data.newImageUrl;
//                 alert(data.message);
//                 window.location.reload(); // Refresh page after user clicks OK
//             } else {
//                 alert('Upload failed: ' + data.message);
//                 document.getElementById('profileAvatar').src =
//                     document.getElementById('profileAvatar').getAttribute('th:src') ||
//                     'https://via.placeholder.com/150/f9a825/0F1729?text=U';
//                 window.location.reload(); // Refresh page after user clicks OK
//             }
//         })
//         .catch(error => {
//             console.error('Error:', error);
//             alert('Ảnh không hợp lệ');
//             document.getElementById('profileAvatar').src =
//                 document.getElementById('profileAvatar').getAttribute('th:src') ||
//                 'https://via.placeholder.com/150/f9a825/0F1729?text=U';
//             window.location.reload(); // Refresh page after user clicks OK
//         });
// });

// Handle form submission
document.getElementById('uploadForm').addEventListener('submit', function (event) {
    // Show loading toast (optional, can remove if not needed)
    const loadingToast = showToast(true, 'Đang tải lên...', true);
});

// Check for message in sessionStorage after page load
document.addEventListener('DOMContentLoaded', function () {
    const message = sessionStorage.getItem('uploadMessage');
    const success = sessionStorage.getItem('uploadSuccess') === 'true';

    if (message) {
        // Show SweetAlert2 popup instead of toast
        Swal.fire({
            icon: success ? 'success' : 'error',
            title: success ? 'Thành công' : 'Lỗi',
            text: message,
            confirmButtonText: 'OK',
            customClass: {
                confirmButton: 'btn btn-primary'
            }
        });
        // Clear sessionStorage
        sessionStorage.removeItem('uploadMessage');
        sessionStorage.removeItem('uploadSuccess');
    }
});

// Optional: Auto-hide popup after a few seconds
document.addEventListener("DOMContentLoaded", function () {
    const popup = document.querySelector('.popup');
    if (popup) {
        setTimeout(() => popup.style.display = 'none', 3000);
    }
});

function closePopup() {
    const popup = document.getElementById('popupNotification');
    if (popup) {
        popup.style.display = 'none';
    }
}

// Auto-hide popup after 3 seconds
document.addEventListener('DOMContentLoaded', () => {
    const popup = document.getElementById('popupNotification');
    if (popup) {
        setTimeout(() => {
            popup.style.display = 'none';
        }, 3000);
    }
});

// ===============================================
// TICKET DETAIL FUNCTIONS
// ===============================================

/**
 * Show ticket detail modal with information from data attributes
 * @param {HTMLElement} button - The button element clicked
 */
function showTicketDetail(button) {
    try {
        console.log('Showing ticket detail...');
        
        // Get data from button attributes
        const invoiceId = button.getAttribute('data-invoice-id');
        const movieName = button.getAttribute('data-movie-name');
        const bookingDate = button.getAttribute('data-booking-date');
        const seatNumber = button.getAttribute('data-seat-number');
        const paymentMethod = button.getAttribute('data-payment-method');
        const totalAmount = button.getAttribute('data-total-amount');
        const useScore = button.getAttribute('data-use-score') || '0';
        const addScore = button.getAttribute('data-add-score') || '0';
        const status = button.getAttribute('data-status');
        
        // Validate required data
        if (!invoiceId || !movieName) {
            console.error('Missing required ticket data');
            return;
        }
        
        // Format values
        const formattedAmount = formatCurrency(totalAmount);
        const formattedDate = formatDate(bookingDate);
        const statusText = getStatusText(status);
        
        // Populate modal fields
        const modalElements = {
            'modalInvoiceId': `#${invoiceId}`,
            'modalMovieName': movieName,
            'modalBookingDate': formattedDate,
            'modalSeatNumber': seatNumber || '-',
            'modalPaymentMethod': paymentMethod || '-',
            'modalTotalAmount': formattedAmount,
            'modalUsedScore': `${useScore} điểm`,
            'modalAddScore': `${addScore} điểm`,
            'modalStatus': statusText
        };
        
        // Update modal content
        Object.entries(modalElements).forEach(([elementId, value]) => {
            const element = document.getElementById(elementId);
            if (element) {
                element.textContent = value;
            } else {
                console.warn(`Element with ID '${elementId}' not found`);
            }
        });
        
        // Show modal
        const modal = document.getElementById('ticketDetailModal');
        if (modal) {
            const bootstrapModal = new bootstrap.Modal(modal);
            bootstrapModal.show();
            console.log('Modal displayed successfully');
        } else {
            console.error('Modal element not found');
        }
        
    } catch (error) {
        console.error('Error showing ticket detail:', error);
        alert('Không thể hiển thị chi tiết vé. Vui lòng thử lại.');
    }
}

/**
 * Format currency amount
 * @param {string|number} amount - The amount to format
 * @returns {string} Formatted currency string
 */
function formatCurrency(amount) {
    if (!amount) return '0 VND';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VND';
}

/**
 * Format date string
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
function formatDate(dateStr) {
    if (!dateStr) return '-';
    
    try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        console.error('Error formatting date:', error);
        return dateStr;
    }
}

/**
 * Get status text based on status code
 * @param {string|number} status - The status code
 * @returns {string} Status text in Vietnamese
 */
function getStatusText(status) {
    switch (parseInt(status)) {
        case 1:
            return 'Đã xác nhận';
        case 0:
            return 'Chờ thanh toán';
        case -1:
            return 'Đã hủy';
        default:
            return 'Không xác định';
    }
}

/**
 * Show toast notification
 * @param {string} message - The message to show
 * @param {string} type - The type of toast (success, error, warning, info)
 */
// function showToast(message, type = 'success') {
//     const toastId = 'toast-' + new Date().getTime();
//     const iconClass = {
//         'success': 'fas fa-check-circle',
//         'error': 'fas fa-exclamation-triangle',
//         'warning': 'fas fa-exclamation-circle',
//         'info': 'fas fa-info-circle'
//     }[type] || 'fas fa-check-circle';
//
//     const bgClass = {
//         'success': 'bg-success',
//         'error': 'bg-danger',
//         'warning': 'bg-warning',
//         'info': 'bg-info'
//     }[type] || 'bg-success';
//
//     const toastHTML = `
//         <div id="${toastId}" class="toast align-items-center text-white ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
//             <div class="d-flex">
//                 <div class="toast-body">
//                     <i class="${iconClass} me-2"></i>
//                     ${message}
//                 </div>
//                 <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
//             </div>
//         </div>
//     `;
//
//     // Create toast container if it doesn't exist
//     let toastContainer = document.getElementById('toastContainer');
//     if (!toastContainer) {
//         toastContainer = document.createElement('div');
//         toastContainer.id = 'toastContainer';
//         toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
//         toastContainer.style.zIndex = '1055';
//         document.body.appendChild(toastContainer);
//     }
//
//     toastContainer.innerHTML += toastHTML;
//     const toastElement = document.getElementById(toastId);
//     const toast = new bootstrap.Toast(toastElement);
//     toast.show();
//
//     // Auto remove after 5 seconds
//     setTimeout(() => {
//         if (toastElement && toastElement.parentNode) {
//             toastElement.remove();
//         }
//     }, 5000);
// }
