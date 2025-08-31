// Booking Management JavaScript
class BookingManager {
    constructor() {
        this.currentPage = 1;
        this.itemsPerPage = 10;
        this.totalPages = 1;
        this.bookings = [];
        this.filteredBookings = [];
        this.selectedBooking = null;
        this.csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        this.csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.setInitialStats();
        this.showTableImmediately();
        this.loadMockData();
        this.loadMovieOptions();
        this.updateStats();
        this.checkElements();
    }

    // Set initial stats for immediate display
    setInitialStats() {
        document.getElementById('totalBookings').textContent = '125';
        document.getElementById('pendingBookings').textContent = '18';
        document.getElementById('confirmedBookings').textContent = '102';
        document.getElementById('todayBookings').textContent = '24';
    }

    // Show table immediately without loading state
    showTableImmediately() {
        const spinner = document.getElementById('loadingSpinner');
        const tableContainer = document.getElementById('bookingTableContainer');
        
        if (spinner) spinner.style.display = 'none';
        if (tableContainer) tableContainer.style.display = 'block';
    }

    // Check if all required elements exist
    checkElements() {
        const requiredElements = [
            'searchInput', 'statusFilter', 'movieFilter', 'bookingTableBody',
            'loadingSpinner', 'bookingTableContainer', 'emptyState',
            'totalBookings', 'pendingBookings', 'confirmedBookings', 'todayBookings'
        ];

        const missingElements = requiredElements.filter(id => !document.getElementById(id));
        
        if (missingElements.length > 0) {
            console.warn('Missing elements:', missingElements);
            this.showError('Some page elements are missing. Please refresh the page.');
        }
    }

    bindEvents() {
        // Search input event with debounce
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', 
                this.debounce(this.handleSearch.bind(this), 300));
        }

        // Filter events
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) {
            statusFilter.addEventListener('change', this.applyFilters.bind(this));
        }

        const movieFilter = document.getElementById('movieFilter');
        if (movieFilter) {
            movieFilter.addEventListener('change', this.applyFilters.bind(this));
        }

        // Modal events
        const modal = document.getElementById('bookingDetailModal');
        if (modal) {
            modal.addEventListener('hidden.bs.modal', this.clearSelectedBooking.bind(this));
        }

        // Add error handling for window events
        window.addEventListener('error', (e) => {
            console.error('JavaScript error:', e.error);
        });
    }

    // Debounce function for search input
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Load mock data - Replace with actual API calls
    loadMockData() {
        // Simulate loading
        this.showLoading(true);
        
        setTimeout(() => {
            this.bookings = [
                {
                    id: 'BK2025001',
                    identityCard: '123456789012',
                    phoneNumber: '+84 901 234 567',
                    movieTitle: 'Avatar: The Way of Water',
                    showtime: '2025-07-21 19:30',
                    status: 'pending',
                    customerName: 'Nguyễn Văn A',
                    email: 'nguyenvana@example.com',
                    seats: ['A1', 'A2', 'A3'],
                    totalAmount: 450000,
                    bookingDate: '2025-07-20 14:30',
                    cinema: 'Hall A - Screen 1',
                    paymentMethod: 'Credit Card',
                    paymentStatus: 'Paid',
                    ticketType: 'Standard'
                },
                {
                    id: 'BK2025002',
                    identityCard: '987654321098',
                    phoneNumber: '+84 902 345 678',
                    movieTitle: 'Spider-Man: No Way Home',
                    showtime: '2025-07-21 21:00',
                    status: 'confirmed',
                    customerName: 'Trần Thị B',
                    email: 'tranthib@example.com',
                    seats: ['B5', 'B6'],
                    totalAmount: 300000,
                    bookingDate: '2025-07-20 16:45',
                    cinema: 'Hall B - Screen 2',
                    paymentMethod: 'VNPay',
                    paymentStatus: 'Paid',
                    ticketType: 'VIP'
                },
                {
                    id: 'BK2025003',
                    identityCard: '123456789014',
                    phoneNumber: '+84 901 234 569',
                    movieTitle: 'Black Widow',
                    showtime: '2025-07-22 15:30',
                    status: 'pending',
                    customerName: 'Lê Văn Cường',
                    email: 'levanC@example.com',
                    seats: ['C1'],
                    totalAmount: 100000,
                    bookingDate: '2025-07-20 10:15',
                    cinema: 'MoonCinema - Bình Thạnh, TP.HCM',
                    paymentMethod: 'Cash',
                    paymentStatus: 'Pending',
                    ticketType: 'Standard'
                },
                {
                    id: 'BK2025004',
                    identityCard: '123456789015',
                    phoneNumber: '+84 901 234 570',
                    movieTitle: 'Doctor Strange',
                    showtime: '2025-07-22 18:00',
                    status: 'cancelled',
                    customerName: 'Phạm Thị Dung',
                    email: 'phamthidung@example.com',
                    seats: ['D5', 'D6'],
                    totalAmount: 200000,
                    bookingDate: '2025-07-19 20:30',
                    cinema: 'MoonCinema - Quận 3, TP.HCM',
                    paymentMethod: 'Credit Card',
                    paymentStatus: 'Refunded',
                    ticketType: 'Standard'
                },
                {
                    id: 'BK2025005',
                    identityCard: '123456789016',
                    phoneNumber: '+84 901 234 571',
                    movieTitle: 'Top Gun: Maverick',
                    showtime: '2025-07-21 20:15',
                    status: 'confirmed',
                    customerName: 'Hoàng Minh Tuấn',
                    email: 'hoangminhtuan@example.com',
                    seats: ['E7', 'E8'],
                    totalAmount: 250000,
                    bookingDate: '2025-07-21 09:20',
                    cinema: 'MoonCinema - Quận 1, TP.HCM',
                    paymentMethod: 'VNPay',
                    paymentStatus: 'Paid',
                    ticketType: 'Premium'
                },
                {
                    id: 'BK2025006',
                    identityCard: '123456789017',
                    phoneNumber: '+84 901 234 572',
                    movieTitle: 'Fast & Furious 10',
                    showtime: '2025-07-21 22:30',
                    status: 'pending',
                    customerName: 'Vũ Thị Hằng',
                    email: 'vuthihang@example.com',
                    seats: ['F1', 'F2', 'F3', 'F4'],
                    totalAmount: 400000,
                    bookingDate: '2025-07-21 11:45',
                    cinema: 'MoonCinema - Quận 5, TP.HCM',
                    paymentMethod: 'Credit Card',
                    paymentStatus: 'Paid',
                    ticketType: 'VIP'
                }
            ];
            
            this.filteredBookings = [...this.bookings];
            this.updateTotalCount();
            this.renderBookings();
            this.renderPagination();
            this.showLoading(false);
        }, 1000);
    }

    // Load movie options for filter
    loadMovieOptions() {
        setTimeout(() => {
            const movieSelect = document.getElementById('movieFilter');
            const uniqueMovies = [...new Set(this.bookings.map(booking => booking.movieTitle))];
            
            // Clear existing options except "All Movies"
            movieSelect.innerHTML = '<option value="">All Movies</option>';
            
            uniqueMovies.forEach(movie => {
                const option = document.createElement('option');
                option.value = movie;
                option.textContent = movie;
                movieSelect.appendChild(option);
            });
        }, 1200);
    }

    // Update statistics
    updateStats() {
        const total = this.bookings.length;
        const pending = this.bookings.filter(b => b.status === 'pending').length;
        const confirmed = this.bookings.filter(b => b.status === 'confirmed').length;
        const today = this.bookings.filter(b => {
            const bookingDate = new Date(b.bookingDate);
            const today = new Date();
            return bookingDate.toDateString() === today.toDateString();
        }).length;

        // Animate count up
        this.animateCounter('totalBookings', total);
        this.animateCounter('pendingBookings', pending);
        this.animateCounter('confirmedBookings', confirmed);
        this.animateCounter('todayBookings', today);
    }

    // Animate counter
    animateCounter(elementId, target) {
        const element = document.getElementById(elementId);
        let current = 0;
        const increment = target / 20;
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            element.textContent = Math.floor(current);
        }, 50);
    }

    // Handle search functionality
    handleSearch() {
        this.currentPage = 1;
        this.applyFilters();
    }

    // Apply filters and search
    applyFilters() {
        try {
            const searchInput = document.getElementById('searchInput');
            const statusFilter = document.getElementById('statusFilter');
            const movieFilter = document.getElementById('movieFilter');

            if (!searchInput || !statusFilter || !movieFilter) {
                console.warn('Filter elements not found');
                return;
            }

            const searchTerm = searchInput.value.toLowerCase().trim();
            const statusValue = statusFilter.value;
            const movieValue = movieFilter.value;

            this.filteredBookings = this.bookings.filter(booking => {
                const matchesSearch = !searchTerm || 
                    booking.id.toLowerCase().includes(searchTerm) ||
                    booking.identityCard.includes(searchTerm) ||
                    booking.phoneNumber.includes(searchTerm) ||
                    booking.movieTitle.toLowerCase().includes(searchTerm) ||
                    booking.customerName.toLowerCase().includes(searchTerm);

                const matchesStatus = !statusValue || booking.status === statusValue;
                const matchesMovie = !movieValue || booking.movieTitle === movieValue;

                return matchesSearch && matchesStatus && matchesMovie;
            });

            this.currentPage = 1;
            this.updateTotalCount();
            this.renderBookings();
            this.renderPagination();
        } catch (error) {
            console.error('Error applying filters:', error);
            this.showError('Error applying filters. Please try again.');
        }
    }

    // Show/hide loading spinner
    showLoading(show) {
        const spinner = document.getElementById('loadingSpinner');
        const tableContainer = document.getElementById('bookingTableContainer');
        const emptyState = document.getElementById('emptyState');

        if (show) {
            spinner.style.display = 'block';
            tableContainer.style.display = 'none';
            emptyState.style.display = 'none';
        } else {
            spinner.style.display = 'none';
        }
    }

    // Update total booking count
    updateTotalCount() {
        const totalElement = document.getElementById('bookingCount');
        const count = this.filteredBookings.length;
        totalElement.textContent = `${count} booking${count !== 1 ? 's' : ''}`;
    }

    // Render booking table
    renderBookings() {
        const tbody = document.getElementById('bookingTableBody');
        const tableContainer = document.getElementById('bookingTableContainer');
        const emptyState = document.getElementById('emptyState');

        tbody.innerHTML = '';

        if (this.filteredBookings.length === 0) {
            tableContainer.style.display = 'none';
            emptyState.style.display = 'block';
            return;
        }

        tableContainer.style.display = 'block';
        emptyState.style.display = 'none';

        const startIndex = (this.currentPage - 1) * this.itemsPerPage;
        const endIndex = startIndex + this.itemsPerPage;
        const pageBookings = this.filteredBookings.slice(startIndex, endIndex);

        pageBookings.forEach(booking => {
            const row = this.createBookingRow(booking);
            tbody.appendChild(row);
        });
    }

    // Create booking table row
    createBookingRow(booking) {
        const row = document.createElement('tr');
        
        const statusClass = `status-${booking.status}`;
        const isConfirmable = booking.status === 'pending';

        row.innerHTML = `
            <td>
                <span class="booking-id">${booking.id}</span>
            </td>
            <td>${booking.identityCard}</td>
            <td>${booking.phoneNumber}</td>
            <td>
                <span class="movie-title">${booking.movieTitle}</span>
            </td>
            <td>
                <span class="showtime">${this.formatDateTime(booking.showtime)}</span>
            </td>
            <td>
                <span class="status-badge ${statusClass}">${this.getStatusText(booking.status)}</span>
            </td>
            <td>
                <button class="btn btn-view-details" onclick="bookingManager.viewBookingDetails('${booking.id}')">
                    <i class="fas fa-eye me-1"></i>
                    View Details
                </button>
                <button class="btn btn-confirm-booking ${isConfirmable ? '' : 'disabled'}" 
                        onclick="bookingManager.showConfirmDialog('${booking.id}')"
                        ${isConfirmable ? '' : 'disabled'}>
                    <i class="fas fa-check me-1"></i>
                    ${isConfirmable ? 'Confirm Booking' : 'Confirmed'}
                </button>
            </td>
        `;

        return row;
    }

    // Format date time
    formatDateTime(dateTimeStr) {
        const date = new Date(dateTimeStr);
        return date.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    // Get status text
    getStatusText(status) {
        const statusTexts = {
            'confirmed': 'Confirmed',
            'pending': 'Pending',
            'cancelled': 'Cancelled'
        };
        return statusTexts[status] || status;
    }

    // View booking details
    viewBookingDetails(bookingId) {
        const booking = this.bookings.find(b => b.id === bookingId);
        if (!booking) {
            this.showError('Booking not found');
            return;
        }

        this.selectedBooking = booking;
        this.renderBookingDetails(booking);
        
        const modal = new bootstrap.Modal(document.getElementById('bookingDetailModal'));
        modal.show();
    }

    // Render booking details in modal
    renderBookingDetails(booking) {
        const content = document.getElementById('bookingDetailContent');
        const confirmBtn = document.getElementById('confirmBookingBtn');

        // Update confirm button state
        if (booking.status === 'pending') {
            confirmBtn.style.display = 'inline-block';
            confirmBtn.disabled = false;
        } else {
            confirmBtn.style.display = 'none';
        }

        content.innerHTML = `
            <div class="booking-detail-container">
                <div class="detail-section">
                    <h6><i class="fas fa-user me-2"></i>Customer Information</h6>
                    <div class="detail-item">
                        <span class="detail-label">Full Name:</span>
                        <span class="detail-value">${booking.customerName}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Identity Card:</span>
                        <span class="detail-value">${booking.identityCard}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Phone Number:</span>
                        <span class="detail-value">${booking.phoneNumber}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Email:</span>
                        <span class="detail-value">${booking.email}</span>
                    </div>
                </div>

                <div class="detail-section">
                    <h6><i class="fas fa-ticket-alt me-2"></i>Booking Information</h6>
                    <div class="detail-item">
                        <span class="detail-label">Booking ID:</span>
                        <span class="detail-value">${booking.id}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Movie Title:</span>
                        <span class="detail-value">${booking.movieTitle}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Showtime:</span>
                        <span class="detail-value">${this.formatDateTime(booking.showtime)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Cinema:</span>
                        <span class="detail-value">${booking.cinema}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Booking Date:</span>
                        <span class="detail-value">${this.formatDateTime(booking.bookingDate)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Status:</span>
                        <span class="detail-value">
                            <span class="status-badge status-${booking.status}">${this.getStatusText(booking.status)}</span>
                        </span>
                    </div>
                </div>

                <div class="detail-section">
                    <h6><i class="fas fa-chair me-2"></i>Seat Information</h6>
                    <div class="detail-item">
                        <span class="detail-label">Selected Seats:</span>
                        <span class="detail-value">
                            <div class="seat-list">
                                ${booking.seats.map(seat => `<span class="seat-item">${seat}</span>`).join('')}
                            </div>
                        </span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ticket Type:</span>
                        <span class="detail-value">${booking.ticketType}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Number of Tickets:</span>
                        <span class="detail-value">${booking.seats.length} ticket(s)</span>
                    </div>
                </div>

                <div class="detail-section">
                    <h6><i class="fas fa-credit-card me-2"></i>Payment Information</h6>
                    <div class="detail-item">
                        <span class="detail-label">Total Amount:</span>
                        <span class="detail-value">${this.formatCurrency(booking.totalAmount)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Payment Method:</span>
                        <span class="detail-value">${booking.paymentMethod}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Payment Status:</span>
                        <span class="detail-value">${booking.paymentStatus}</span>
                    </div>
                </div>
            </div>
        `;
    }

    // Format currency
    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    // Show confirm dialog
    showConfirmDialog(bookingId) {
        const booking = this.bookings.find(b => b.id === bookingId);
        if (!booking || booking.status !== 'pending') {
            this.showError('Cannot confirm this booking. It may already be confirmed or cancelled.');
            return;
        }

        // Set up confirmation modal
        const confirmationModal = new bootstrap.Modal(document.getElementById('confirmationModal'));
        const confirmationMessage = document.getElementById('confirmationMessage');
        const confirmActionBtn = document.getElementById('confirmActionBtn');

        confirmationMessage.innerHTML = `
            <div class="text-center">
                <i class="fas fa-question-circle text-warning" style="font-size: 3rem; margin-bottom: 1rem;"></i>
                <h5>Confirm Booking</h5>
                <p>Are you sure you want to confirm booking <strong>${bookingId}</strong>?</p>
                <p class="text-muted">Customer: <strong>${booking.customerName}</strong></p>
                <p class="text-muted">Movie: <strong>${booking.movieTitle}</strong></p>
            </div>
        `;

        confirmActionBtn.onclick = () => {
            this.confirmBooking(bookingId);
            confirmationModal.hide();
        };

        confirmationModal.show();
    }

    // Confirm booking
    async confirmBooking(bookingId = null) {
        const id = bookingId || this.selectedBooking?.id;
        if (!id) {
            this.showError('No booking selected');
            return;
        }

        try {
            // Simulate API call
            await this.simulateApiCall(500);
            
            // Update local data
            const booking = this.bookings.find(b => b.id === id);
            if (booking) {
                booking.status = 'confirmed';
            }

            // Update filtered data
            const filteredBooking = this.filteredBookings.find(b => b.id === id);
            if (filteredBooking) {
                filteredBooking.status = 'confirmed';
            }

            // Refresh display
            this.renderBookings();
            this.updateStats();
            
            // Close modal if open
            const modal = bootstrap.Modal.getInstance(document.getElementById('bookingDetailModal'));
            if (modal) {
                modal.hide();
            }

            this.showSuccess(`Booking ${id} has been confirmed successfully!`);

        } catch (error) {
            console.error('Error confirming booking:', error);
            this.showError('Failed to confirm booking. Please try again.');
        }
    }

    // Simulate API call
    async simulateApiCall(delay = 1000) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ success: true });
            }, delay);
        });
    }

    // Clear selected booking
    clearSelectedBooking() {
        this.selectedBooking = null;
    }

    // Render pagination
    renderPagination() {
        const nav = document.getElementById('paginationNav');
        nav.innerHTML = '';

        this.totalPages = Math.ceil(this.filteredBookings.length / this.itemsPerPage);

        if (this.totalPages <= 1) {
            return;
        }

        // Previous button
        const prevItem = document.createElement('li');
        prevItem.className = `page-item ${this.currentPage === 1 ? 'disabled' : ''}`;
        prevItem.innerHTML = `
            <a class="page-link" href="#" onclick="bookingManager.goToPage(${this.currentPage - 1})" aria-label="Previous">
                <i class="fas fa-chevron-left"></i>
            </a>
        `;
        nav.appendChild(prevItem);

        // Page numbers
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.totalPages, this.currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = `page-item ${i === this.currentPage ? 'active' : ''}`;
            pageItem.innerHTML = `
                <a class="page-link" href="#" onclick="bookingManager.goToPage(${i})">${i}</a>
            `;
            nav.appendChild(pageItem);
        }

        // Next button
        const nextItem = document.createElement('li');
        nextItem.className = `page-item ${this.currentPage === this.totalPages ? 'disabled' : ''}`;
        nextItem.innerHTML = `
            <a class="page-link" href="#" onclick="bookingManager.goToPage(${this.currentPage + 1})" aria-label="Next">
                <i class="fas fa-chevron-right"></i>
            </a>
        `;
        nav.appendChild(nextItem);
    }

    // Go to specific page
    goToPage(page) {
        if (page < 1 || page > this.totalPages || page === this.currentPage) {
            return;
        }
        
        this.currentPage = page;
        this.renderBookings();
        this.renderPagination();
        
        // Scroll to top of table
        document.getElementById('bookingTableContainer').scrollIntoView({ behavior: 'smooth' });
    }

    // Refresh bookings
    refreshBookings() {
        this.showSuccess('Refreshing booking data...');
        this.loadMockData();
        this.updateStats();
    }

    // Show success message
    showSuccess(message) {
        try {
            const toast = document.getElementById('successToast');
            const messageElement = document.getElementById('successMessage');
            
            if (!toast || !messageElement) {
                console.warn('Toast elements not found, using alert instead');
                alert('Success: ' + message);
                return;
            }
            
            messageElement.textContent = message;
            const bsToast = new bootstrap.Toast(toast);
            bsToast.show();
        } catch (error) {
            console.error('Error showing success message:', error);
            alert('Success: ' + message);
        }
    }

    // Show error message
    showError(message) {
        try {
            const toast = document.getElementById('errorToast');
            const messageElement = document.getElementById('errorMessage');
            
            if (!toast || !messageElement) {
                console.warn('Toast elements not found, using alert instead');
                alert('Error: ' + message);
                return;
            }
            
            messageElement.textContent = message;
            const bsToast = new bootstrap.Toast(toast);
            bsToast.show();
        } catch (error) {
            console.error('Error showing error message:', error);
            alert('Error: ' + message);
        }
    }
}

// Global variables and functions
let bookingManager;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check if user has proper role (Admin/Manager/Employee)
    // This would normally be handled by server-side security
    bookingManager = new BookingManager();
});

// Global functions for onclick handlers
function refreshBookings() {
    if (bookingManager) {
        bookingManager.refreshBookings();
        showSuccessToast('📊 Data refreshed successfully!');
    }
}

function applyFilters() {
    if (bookingManager) {
        bookingManager.applyFilters();
        const searchValue = document.getElementById('searchInput')?.value;
        const statusValue = document.getElementById('statusFilter')?.value;
        const movieValue = document.getElementById('movieFilter')?.value;
        showSuccessToast(`🔍 Filters applied: ${searchValue || 'All'} | Status: ${statusValue || 'All'} | Movie: ${movieValue || 'All'}`);
    }
}

function confirmSelectedBooking() {
    if (bookingManager) {
        bookingManager.confirmBooking();
    }
}

// Enhanced Toast Functions
function showSuccessToast(message) {
    const toastElement = document.getElementById('successToast');
    const toastMessageElement = document.getElementById('toastMessage');
    
    if (toastElement && toastMessageElement) {
        toastMessageElement.textContent = message;
        
        const toast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: 4000
        });
        
        toast.show();
    }
}

function showInfoToast(message) {
    const toastElement = document.getElementById('infoToast');
    const toastMessageElement = document.getElementById('infoToastMessage');
    
    if (toastElement && toastMessageElement) {
        toastMessageElement.textContent = message;
        
        const toast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: 4000
        });
        
        toast.show();
    }
}

function showErrorToast(message) {
    const toastElement = document.getElementById('errorToast');
    const toastMessageElement = document.getElementById('errorToastMessage');
    
    if (toastElement && toastMessageElement) {
        toastMessageElement.textContent = message;
        
        const toast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: 4000
        });
        
        toast.show();
    }
}

// Enhanced Booking Detail Functions
function showBookingDetail(bookingId) {
    const bookingData = {
        'BK2025001': {
            customerName: 'Nguyễn Văn A',
            identityCard: '123456789012',
            phoneNumber: '+84 901 234 567',
            email: 'nguyenvana@email.com',
            movieTitle: 'Avatar: The Way of Water',
            showtime: '21/07/2025 19:30',
            cinemaHall: 'Hall A - Screen 1',
            seats: 'A1, A2, A3',
            totalPrice: '450,000 VND',
            status: 'Pending'
        },
        'BK2025002': {
            customerName: 'Trần Thị B',
            identityCard: '987654321098',
            phoneNumber: '+84 902 345 678',
            email: 'tranthib@email.com',
            movieTitle: 'Spider-Man: No Way Home',
            showtime: '21/07/2025 21:00',
            cinemaHall: 'Hall B - Screen 2',
            seats: 'B5, B6',
            totalPrice: '300,000 VND',
            status: 'Confirmed'
        }
    };
    
    const booking = bookingData[bookingId];
    if (booking) {
        // Populate modal with booking data
        document.getElementById('modalBookingId').textContent = bookingId;
        document.getElementById('modalCustomerName').textContent = booking.customerName;
        document.getElementById('modalIdentityCard').textContent = booking.identityCard;
        document.getElementById('modalPhoneNumber').textContent = booking.phoneNumber;
        document.getElementById('modalEmail').textContent = booking.email;
        document.getElementById('modalMovieTitle').textContent = booking.movieTitle;
        document.getElementById('modalShowtime').textContent = booking.showtime;
        document.getElementById('modalCinemaHall').textContent = booking.cinemaHall;
        document.getElementById('modalSeats').textContent = booking.seats;
        document.getElementById('modalTotalPrice').textContent = booking.totalPrice;
        
        // Update status badge
        const statusElement = document.getElementById('modalStatus');
        const badge = statusElement.querySelector('.badge');
        badge.textContent = booking.status;
        
        // Set status badge color
        if (booking.status === 'Confirmed') {
            badge.style.background = '#d1fae5';
            badge.style.color = '#059669';
        } else if (booking.status === 'Pending') {
            badge.style.background = '#fef3c7';
            badge.style.color = '#d97706';
        } else if (booking.status === 'Cancelled') {
            badge.style.background = '#fee2e2';
            badge.style.color = '#dc2626';
        }
        
        // Show/hide confirm button based on status
        const confirmBtn = document.getElementById('modalConfirmBtn');
        if (booking.status === 'Pending') {
            confirmBtn.style.display = 'inline-block';
            confirmBtn.setAttribute('data-booking-id', bookingId);
        } else {
            confirmBtn.style.display = 'none';
        }
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('bookingDetailModal'));
        modal.show();
    } else {
        showInfoToast(`Booking data not found for: ${bookingId}`);
    }
}

function confirmBookingFromModal() {
    const confirmBtn = document.getElementById('modalConfirmBtn');
    const bookingId = confirmBtn.getAttribute('data-booking-id');
    
    if (confirm(`Confirm booking ${bookingId}?`)) {
        // Show loading state
        confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
        confirmBtn.disabled = true;
        
        // Simulate API call
        setTimeout(() => {
            // Close modal first
            const modal = bootstrap.Modal.getInstance(document.getElementById('bookingDetailModal'));
            modal.hide();
            
            // Update table row status
            updateBookingStatusInTable(bookingId, 'Confirmed');
            
            // Show success toast
            showSuccessToast(`Booking ${bookingId} confirmed successfully!`);
            
            // Reset button
            confirmBtn.innerHTML = '<i class="fas fa-check me-1"></i>Confirm Booking';
            confirmBtn.disabled = false;
        }, 1500);
    }
}

function confirmBooking(bookingId) {
    if (confirm(`Confirm booking ${bookingId}?`)) {
        // Find the confirm button for this booking
        const tableRow = findTableRowByBookingId(bookingId);
        if (tableRow) {
            const confirmBtn = tableRow.querySelector('.btn-confirm-booking');
            
            // Show loading state
            confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
            confirmBtn.disabled = true;
            
            // Simulate API call
            setTimeout(() => {
                // Update status in table
                updateBookingStatusInTable(bookingId, 'Confirmed');
                
                // Show success toast
                showSuccessToast(`Booking ${bookingId} confirmed successfully!`);
                
                // Update button to confirmed state
                confirmBtn.innerHTML = '<i class="fas fa-check me-1"></i>Confirmed';
                confirmBtn.disabled = true;
                confirmBtn.classList.remove('btn-confirm-booking');
            }, 1500);
        }
    }
}

function updateBookingStatusInTable(bookingId, newStatus) {
    const tableRow = findTableRowByBookingId(bookingId);
    if (tableRow) {
        const statusBadge = tableRow.querySelector('.status-badge');
        statusBadge.textContent = newStatus;
        statusBadge.className = 'status-badge status-confirmed';
        
        // Update badge styling
        statusBadge.style.background = 'rgba(34, 197, 94, 0.15)';
        statusBadge.style.color = '#22c55e';
        statusBadge.style.borderColor = 'rgba(34, 197, 94, 0.4)';
        statusBadge.style.boxShadow = '0 0 15px rgba(34, 197, 94, 0.2)';
    }
}

function findTableRowByBookingId(bookingId) {
    const bookingIdElements = document.querySelectorAll('.booking-id');
    for (let element of bookingIdElements) {
        if (element.textContent === bookingId) {
            return element.closest('tr');
        }
    }
    return null;
}

// Prevent form submission on Enter key in search
document.addEventListener('keypress', function(e) {
    if (e.target.id === 'searchInput' && e.key === 'Enter') {
        e.preventDefault();
        applyFilters();
    }
});

// Auto-refresh data every 5 minutes
setInterval(() => {
    if (bookingManager) {
        bookingManager.loadMockData();
        bookingManager.updateStats();
    }
}, 300000); // 5 minutes
