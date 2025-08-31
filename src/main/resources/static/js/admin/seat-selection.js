// Seat Selection JavaScript
// Global variables
let urlParams;
let selectedSeatsArray = [];
let requiredSeatCount = 0;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize seat selection functionality only if we're on the seat selection tab
    initializeSeatSelection();
});

function initializeSeatSelection() {
    // Parse URL parameters
    urlParams = new URLSearchParams(window.location.search);
    const movieId = urlParams.get('movieId');
    const date = urlParams.get('date');
    const showtimeId = urlParams.get('showtimeId');
    
    // DOM elements for seat selection
    const movieTitle = document.getElementById('movieTitle');
    const moviePoster = document.getElementById('moviePoster');
    const movieDate = document.getElementById('movieDate');
    const movieTime = document.getElementById('movieTime');
    const movieHall = document.getElementById('movieHall');
    const movieDuration = document.getElementById('movieDuration');
    const movieGenre = document.getElementById('movieGenre');
    const movieRating = document.getElementById('movieRating');
    const seatContainer = document.getElementById('seatContainer');
    const selectedSeats = document.getElementById('selectedSeats');
    const seatCount = document.getElementById('seatCount');
    const confirmButton = document.getElementById('confirmButton');
    const summaryMovieName = document.getElementById('summaryMovieName');
    const summaryDate = document.getElementById('summaryDate');
    const summaryTime = document.getElementById('summaryTime');
    const summaryHall = document.getElementById('summaryHall');
    const seatQuantity = document.getElementById('seatQuantity');
    const seatSelectionMessage = document.getElementById('seatSelectionMessage');
    
    // Initialize movie and showtime details
    fetchMovieAndShowtimeDetails(movieId, date, showtimeId);
    
    // Seat quantity selection event handler
    if (seatQuantity) {
        seatQuantity.addEventListener('change', function() {
            requiredSeatCount = parseInt(this.value);
            updateSeatSelectionStatus();
            
            // Reset seat selection if quantity changes
            if (selectedSeatsArray.length > 0) {
                if (confirm("Thay đổi số lượng ghế sẽ xóa các ghế đã chọn. Bạn có muốn tiếp tục?")) {
                    resetSeatSelection();
                } else {
                    this.value = selectedSeatsArray.length > 0 ? selectedSeatsArray.length : 0;
                    requiredSeatCount = parseInt(this.value);
                }
            }
        });
    }
    
    // Function to fetch movie and showtime details
    function fetchMovieAndShowtimeDetails(movieId, date, showtimeId) {
        const movieData = {
            id: 1,
            title: "Dune: Hành Tinh Cát - Phần Hai",
            duration: 166,
            genre: "Khoa Học Viễn Tưởng",
            rating: "PG-13",
            posterUrl: "https://m.media-amazon.com/images/M/MV5BN2QyY2I5OTMtMjA5OC00MDZiLWI1NzQtZmU1MTjlkMGM1YzhmXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg"
        };
        
        const showtimeData = {
            id: showtimeId,
            time: "17:45",
            hall: "HALL A",
            date: formatDate(date)
        };
        
        // Populate movie details
        if (movieTitle) movieTitle.textContent = movieData.title;
        if (moviePoster) {
            moviePoster.src = movieData.posterUrl;
            moviePoster.alt = movieData.title + " Poster";
        }
        if (movieDate) movieDate.textContent = showtimeData.date;
        if (movieTime) movieTime.textContent = showtimeData.time;
        if (movieHall) movieHall.textContent = showtimeData.hall;
        if (movieDuration) movieDuration.textContent = movieData.duration;
        if (movieGenre) movieGenre.textContent = movieData.genre;
        if (movieRating) movieRating.textContent = movieData.rating;
        
        // Populate summary details
        if (summaryMovieName) summaryMovieName.textContent = movieData.title;
        if (summaryDate) summaryDate.textContent = showtimeData.date;
        if (summaryTime) summaryTime.textContent = showtimeData.time;
        if (summaryHall) summaryHall.textContent = showtimeData.hall;
        
        // Generate seat layout
        generateSeatLayout();
    }
    
    // Function to generate seat layout
    function generateSeatLayout() {
        if (!seatContainer) return;
        
        const rows = 8; // A to H
        const seatsPerRow = 10; // 1 to 10
        
        const occupiedSeats = generateRandomOccupiedSeats(rows, seatsPerRow, 15);
        
        const seatGrid = document.createElement('div');
        seatGrid.className = 'seat-grid';
        
        for (let i = 0; i < rows; i++) {
            const rowLetter = String.fromCharCode(65 + i);
            
            const rowLabel = document.createElement('div');
            rowLabel.className = 'seat-row-label';
            rowLabel.textContent = rowLetter;
            seatGrid.appendChild(rowLabel);
            
            for (let j = 1; j <= seatsPerRow; j++) {
                const seatId = rowLetter + j;
                const seat = document.createElement('div');
                seat.className = 'seat';
                seat.dataset.seatId = seatId;
                seat.textContent = j;
                
                if (occupiedSeats.includes(seatId)) {
                    seat.classList.add('seat-occupied');
                } else {
                    seat.classList.add('seat-available');
                    seat.addEventListener('click', function() {
                        if (requiredSeatCount === 0) {
                            showToast("Vui lòng chọn số lượng ghế trước khi chọn vị trí ghế.", 'warning');
                            return;
                        }
                        
                        if (!this.classList.contains('seat-selected') && selectedSeatsArray.length >= requiredSeatCount) {
                            showToast(`Bạn chỉ có thể chọn ${requiredSeatCount} ghế.`, 'warning');
                            return;
                        }
                        
                        toggleSeatSelection(this);
                    });
                }
                
                seatGrid.appendChild(seat);
            }
        }
        
        seatContainer.appendChild(seatGrid);
    }
    
    // Function to generate random occupied seats
    function generateRandomOccupiedSeats(rows, seatsPerRow, occupiedCount) {
        const occupiedSeats = [];
        while (occupiedSeats.length < occupiedCount) {
            const rowIndex = Math.floor(Math.random() * rows);
            const seatNumber = Math.floor(Math.random() * seatsPerRow) + 1;
            const seatId = String.fromCharCode(65 + rowIndex) + seatNumber;
            
            if (!occupiedSeats.includes(seatId)) {
                occupiedSeats.push(seatId);
            }
        }
        return occupiedSeats;
    }
    
    // Function to handle seat selection
    function toggleSeatSelection(seatElement) {
        const seatId = seatElement.dataset.seatId;
        
        if (seatElement.classList.contains('seat-selected')) {
            // Deselect seat
            seatElement.classList.remove('seat-selected');
            seatElement.classList.add('seat-available');
            
            const index = selectedSeatsArray.indexOf(seatId);
            if (index > -1) {
                selectedSeatsArray.splice(index, 1);
            }
        } else {
            // Check if we've reached the required seat count
            if (requiredSeatCount > 0 && selectedSeatsArray.length >= requiredSeatCount) {
                showToast('Bạn đã chọn đủ số lượng ghế yêu cầu!', 'warning');
                return;
            }
            
            // Select seat
            seatElement.classList.remove('seat-available');
            seatElement.classList.add('seat-selected');
            selectedSeatsArray.push(seatId);
        }
        
        updateSelectionSummary();
    }
    
    // Reset all seat selections
    function resetSeatSelection() {
        document.querySelectorAll('.seat-selected').forEach(seat => {
            seat.classList.remove('seat-selected');
            seat.classList.add('seat-available');
        });
        
        selectedSeatsArray.length = 0;
        updateSelectionSummary();
    }
    
    // Function to update seat selection summary
    function updateSelectionSummary() {
        if (selectedSeats) {
            if (selectedSeatsArray.length === 0) {
                selectedSeats.textContent = 'Chưa chọn ghế';
            } else {
                // Sort seats properly (A1, A2, B1, B2, etc.)
                const sortedSeats = [...selectedSeatsArray].sort((a, b) => {
                    if (a[0] === b[0]) {
                        return parseInt(a.substring(1)) - parseInt(b.substring(1));
                    }
                    return a[0].localeCompare(b[0]);
                });
                selectedSeats.textContent = sortedSeats.join(', ');
            }
        }
        
        if (seatCount) {
            seatCount.textContent = selectedSeatsArray.length;
        }
        
        updateSeatSelectionStatus();
    }
    
    // Update status messages and button state
    function updateSeatSelectionStatus() {
        if (!seatSelectionMessage || !confirmButton) return;
        
        if (requiredSeatCount === 0) {
            seatSelectionMessage.textContent = 'Vui lòng chọn số lượng ghế trước khi chọn ghế.';
            seatSelectionMessage.className = 'alert alert-warning mt-3';
            seatSelectionMessage.classList.remove('d-none');
            confirmButton.disabled = true;
        } else if (selectedSeatsArray.length === 0) {
            seatSelectionMessage.textContent = `Vui lòng chọn ${requiredSeatCount} ghế.`;
            seatSelectionMessage.className = 'alert alert-info mt-3';
            seatSelectionMessage.classList.remove('d-none');
            confirmButton.disabled = true;
        } else if (selectedSeatsArray.length < requiredSeatCount) {
            const remaining = requiredSeatCount - selectedSeatsArray.length;
            seatSelectionMessage.textContent = `Bạn cần chọn thêm ${remaining} ghế nữa.`;
            seatSelectionMessage.className = 'alert alert-info mt-3';
            seatSelectionMessage.classList.remove('d-none');
            confirmButton.disabled = true;
        } else if (selectedSeatsArray.length === requiredSeatCount) {
            seatSelectionMessage.textContent = 'Hoàn thành! Bạn có thể xác nhận lựa chọn.';
            seatSelectionMessage.className = 'alert alert-success mt-3';
            seatSelectionMessage.classList.remove('d-none');
            confirmButton.disabled = false;
        } else {
            seatSelectionMessage.textContent = 'Bạn đã chọn quá số lượng ghế yêu cầu.';
            seatSelectionMessage.className = 'alert alert-danger mt-3';
            seatSelectionMessage.classList.remove('d-none');
            confirmButton.disabled = true;
        }
    }
    
    // Add event listener for confirm button
    if (confirmButton) {
        confirmButton.addEventListener('click', function() {
            if (selectedSeatsArray.length === requiredSeatCount && requiredSeatCount > 0) {
                const confirmMessage = `Bạn có chắc chắn muốn đặt ${selectedSeatsArray.length} ghế: ${selectedSeatsArray.join(', ')}?`;
                if (confirm(confirmMessage)) {
                    // Process booking confirmation
                    processBookingConfirmation();
                }
            }
        });
    }
    
    // Function to process booking confirmation
    function processBookingConfirmation() {
        // Show loading state
        if (confirmButton) {
            confirmButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
            confirmButton.disabled = true;
        }
        
        // Create booking data
        const bookingData = {
            movieId: urlParams.get('movieId'),
            date: urlParams.get('date'),
            showtimeId: urlParams.get('showtimeId'),
            seats: selectedSeatsArray,
            quantity: selectedSeatsArray.length,
            movieTitle: movieTitle ? movieTitle.textContent : '',
            showtime: movieTime ? movieTime.textContent : '',
            hall: movieHall ? movieHall.textContent : ''
        };
        
        // Simulate API call
        setTimeout(() => {
            showToast('Đặt ghế thành công!', 'success');
            console.log('Booking confirmed:', bookingData);
            
            // Reset form
            if (seatQuantity) seatQuantity.value = '0';
            requiredSeatCount = 0;
            resetSeatSelection();
            
            // Reset button
            if (confirmButton) {
                confirmButton.innerHTML = 'Xác Nhận Lựa Chọn';
                confirmButton.disabled = true;
            }
        }, 2000);
    }
    
    // Format date function
    function formatDate(dateString) {
        if (!dateString) return new Date().toLocaleDateString('vi-VN');
        
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('vi-VN');
        } catch (e) {
            return dateString;
        }
    }
    
    // Show toast notification function
    function showToast(message, type = 'info') {
        let toastElement;
        let messageElement;
        
        switch (type) {
            case 'success':
                toastElement = document.getElementById('successToast');
                messageElement = document.getElementById('toastMessage');
                break;
            case 'error':
                toastElement = document.getElementById('errorToast');
                messageElement = document.getElementById('errorToastMessage');
                break;
            case 'warning':
            case 'info':
            default:
                toastElement = document.getElementById('infoToast');
                messageElement = document.getElementById('infoToastMessage');
                break;
        }
        
        if (toastElement && messageElement) {
            messageElement.textContent = message;
            const toast = new bootstrap.Toast(toastElement);
            toast.show();
        } else {
            // Fallback to alert if toast elements not found
            alert(message);
        }
    }
}

// Global functions for booking management compatibility
window.showBookingDetail = function(bookingId) {
    const modal = new bootstrap.Modal(document.getElementById('bookingDetailModal'));
    modal.show();
};

window.confirmBooking = function(bookingId) {
    if (confirm(`Confirm booking ${bookingId}?`)) {
        showToast(`Booking ${bookingId} has been confirmed.`, 'success');
    }
};

window.confirmBookingFromModal = function() {
    const bookingId = document.getElementById('modalBookingId').textContent;
    if (confirm(`Confirm booking ${bookingId}?`)) {
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('bookingDetailModal'));
        modal.hide();
        
        // Show success toast
        showToast(`Booking ${bookingId} has been confirmed successfully!`, 'success');
    }
};

window.applyFilters = function() {
    const search = document.getElementById('searchInput').value;
    const status = document.getElementById('statusFilter').value;
    const movie = document.getElementById('movieFilter').value;
    
    console.log('Applying filters:', { search, status, movie });
    showToast('Filters applied successfully!', 'info');
};

window.refreshBookings = function() {
    console.log('Refreshing bookings...');
    showToast('Booking data refreshed!', 'info');
};

// Global showToast function
window.showToast = function(message, type = 'info') {
    let toastElement;
    let messageElement;
    
    switch (type) {
        case 'success':
            toastElement = document.getElementById('successToast');
            messageElement = document.getElementById('toastMessage');
            break;
        case 'error':
            toastElement = document.getElementById('errorToast');
            messageElement = document.getElementById('errorToastMessage');
            break;
        case 'warning':
        case 'info':
        default:
            toastElement = document.getElementById('infoToast');
            messageElement = document.getElementById('infoToastMessage');
            break;
    }
    
    if (toastElement && messageElement) {
        messageElement.textContent = message;
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
    } else {
        alert(message);
    }
};
