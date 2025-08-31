const seatMap = document.getElementById("seatMap");
const reservedSeats = []; // bạn có thể truyền từ controller nếu cần

function showPopup(message) {
    const popup = document.getElementById("popup");
    popup.textContent = message;
    popup.style.display = "block";
    setTimeout(() => popup.style.display = "none", 2000);
}

// Tạo ghế từ seatData lấy từ server
seatData.forEach(seat => {
    const seatCode = seat.seatColumn + seat.seatRow;
    const seatDiv = document.createElement("div");
    seatDiv.classList.add("seat");
    seatDiv.textContent = seatCode;

    if (!seat.isActive || reservedSeats.includes(seatCode)) {
        seatDiv.classList.add("reserved");
    } else {
        seatDiv.addEventListener("click", function () {
            const quantity = parseInt(document.getElementById("quantity").value);
            const selectedSeats = document.querySelectorAll(".seat.selected").length;

            if (this.classList.contains("selected")) {
                this.classList.remove("selected");
            } else if (selectedSeats < quantity) {
                this.classList.add("selected");
            } else {
                showPopup("Vui lòng chọn đúng " + quantity + " ghế");
            }

            validateSeatSelection();
        });
    }

    seatMap.appendChild(seatDiv);
});

function validateSeatSelection() {
    const quantity = parseInt(document.getElementById("quantity").value);
    const selected = document.querySelectorAll('.seat.selected').length;
    const btn = document.getElementById("checkoutBtn");

    btn.disabled = quantity === 0 || selected !== quantity;

    if (selected < quantity) {
        showPopup("Chọn thêm " + (quantity - selected) + " ghế");
    }
}

document.getElementById("checkoutBtn").addEventListener("click", function () {
    const selectedSeats = Array.from(document.querySelectorAll(".seat.selected")).map(s => s.textContent);
    alert("Bạn đã chọn ghế: " + selectedSeats.join(", "));
});
