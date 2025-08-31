console.log("JavaScript file loaded successfully at", new Date().toLocaleString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" }));

// Sử dụng seatData đã được định nghĩa trong HTML
console.log("Seat data in JS at", new Date().toLocaleString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" }), ":", seatData);

const MAX_SEATS = 8;
let desiredQuantity = 0;
let selectedSeats = [];
let selectedGroups = new Map();
const seatInfoMap = {};

seatData.forEach(seat => {
    const code = seat.seatColumn + seat.seatRow;
    seatInfoMap[code] = seat;
});

const seatMap = document.getElementById("seatMap");
const seatQuantitySelect = document.getElementById("seatQuantity");
const selectedSeatsText = document.getElementById("selectedSeats");
const selectedCountText = document.getElementById("seatCount");
const confirmButton = document.getElementById("confirmBookingBtn");
const selectionMessage = document.getElementById("seatSelectionMessage");
const totalPriceText = document.getElementById("totalPrice");
const memberRadio = document.getElementById('memberRadio');
const guestRadio = document.getElementById('guestRadio');
const memberInputGroup = document.getElementById("memberInput").closest(".mb-3");
const memberInfo = document.getElementById("memberInfo");
const notFound = document.getElementById("memberNotFound");
const guestForm = document.getElementById("guestForm");

function showMessage(message) {
    if (selectionMessage) {
        selectionMessage.textContent = message;
        selectionMessage.classList.remove("d-none");
        setTimeout(() => selectionMessage.classList.add("d-none"), 3000);
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price) + ' VNĐ';
}

function updateSummary() {
    if (selectedSeats.length > 0) {
        selectedSeatsText.textContent = selectedSeats.join(", ");
        selectedCountText.textContent = selectedSeats.length;

        let total = 0;
        selectedSeats.forEach(code => {
            const price = seatInfoMap[code]?.seatPrice || 0;
            total += price;
        });
        if (totalPriceText) totalPriceText.textContent = formatPrice(total);
        confirmButton.disabled = false;
    } else {
        selectedSeatsText.textContent = "Chưa chọn ghế";
        selectedCountText.textContent = "0";
        if (totalPriceText) totalPriceText.textContent = "0 VNĐ";
        confirmButton.disabled = true;
    }
}

document.querySelectorAll('.seat-qty-btn').forEach(btn => {
    btn.addEventListener('click', function () {
        document.querySelectorAll('.seat-qty-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        const qty = parseInt(this.dataset.value);
        document.getElementById("seatQuantity").value = qty;
        setQuantity(qty);
    });
});

function setQuantity(qty) {
    const remaining = MAX_SEATS - selectedSeats.length;
    if (qty > remaining) {
        showMessage(`Chỉ còn được chọn tối đa ${remaining} ghế nữa!`);
        seatQuantitySelect.value = "0";
        return;
    }
    desiredQuantity = qty;
    if (qty > 0) {
        showMessage(`Chọn ${qty} ghế. Click vào 1 ghế để tự động chọn liền kề.`);
    }
    lockInvalidRows(qty);
}

function lockInvalidRows(qty) {
    const rows = [...new Set(seatData.map(s => s.seatRow))];
    rows.forEach(row => {
        const seatsInRow = seatData
            .filter(s => s.seatRow === row)
            .sort((a, b) => a.seatColumn.charCodeAt(0) - b.seatColumn.charCodeAt(0));

        let segments = [], currentSegment = [];
        for (const seat of seatsInRow) {
            if (seat.seatTypeId === 4 || seat.seatTypeId === 5 || seat.isBooked || selectedSeats.includes(seat.seatColumn + seat.seatRow)) {
                if (currentSegment.length) segments.push(currentSegment);
                currentSegment = [];
            } else {
                currentSegment.push(seat);
            }
        }
        if (currentSegment.length) segments.push(currentSegment);

        let validCodes = new Set();
        segments.forEach(segment => {
            if (segment.length < qty) return;
            for (let i = 0; i <= segment.length - qty; i++) {
                const group = segment.slice(i, i + qty);
                let isValid = true;
                for (let j = 0; j < group.length; j++) {
                    const seat = group[j];
                    const code = seat.seatColumn + seat.seatRow;
                    if (seat.isBooked || seat.seatTypeId === 4 || seat.seatTypeId === 5 || selectedSeats.includes(code)) {
                        isValid = false;
                        break;
                    }
                    if (j > 0) {
                        const prevChar = group[j - 1].seatColumn.charCodeAt(0);
                        const currChar = seat.seatColumn.charCodeAt(0);
                        if (currChar !== prevChar + 1) {
                            isValid = false;
                            break;
                        }
                        const inBetweenCode = String.fromCharCode(prevChar + 1) + seat.seatRow;
                        if (seatInfoMap[inBetweenCode]?.seatTypeId === 5) {
                            isValid = false;
                            break;
                        }
                    }
                }
                if (isValid) group.forEach(s => validCodes.add(s.seatColumn + s.seatRow));
            }
        });

        selectedSeats.forEach(code => validCodes.add(code));

        seatsInRow.forEach(seat => {
            const code = seat.seatColumn + seat.seatRow;
            const seatDiv = document.querySelector(`.seat[data-seat="${code}"]`);
            if (!seatDiv) return;
            if (qty === 0 || validCodes.has(code)) {
                seatDiv.classList.remove("disabled");
                seatDiv.style.pointerEvents = "auto";
            } else {
                seatDiv.classList.add("disabled");
                seatDiv.style.pointerEvents = "none";
            }
        });
    });
}

function selectSeat(clickedSeatElement, clickedCode) {
    if (desiredQuantity <= 0) {
        showMessage("Vui lòng chọn số lượng ghế trước!");
        return;
    }
    const clickedSeat = seatInfoMap[clickedCode];
    if (!clickedSeat || clickedSeat.isBooked || clickedSeat.seatTypeId === 4 || clickedSeat.seatTypeId === 5 || selectedSeats.includes(clickedCode)) return;

    const row = clickedSeat.seatRow;
    const seatsInRow = seatData.filter(s => s.seatRow === row && !s.isBooked && s.seatTypeId !== 4 && s.seatTypeId !== 5)
        .sort((a, b) => a.seatColumn.charCodeAt(0) - b.seatColumn.charCodeAt(0));
    const colList = seatsInRow.map(s => s.seatColumn);
    const clickedIndex = colList.indexOf(clickedSeat.seatColumn);

    function isValidGroup(group) {
        if (group.length !== desiredQuantity) return false;
        for (let i = 0; i < group.length; i++) {
            const code = group[i] + row;
            const seat = seatInfoMap[code];
            if (!seat || seat.isBooked || seat.seatTypeId === 4 || seat.seatTypeId === 5 || selectedSeats.includes(code)) return false;
            if (i > 0) {
                const prevChar = group[i - 1].charCodeAt(0);
                const currChar = group[i].charCodeAt(0);
                if (currChar !== prevChar + 1) return false;
                const inBetweenCode = String.fromCharCode(prevChar + 1) + row;
                if (seatInfoMap[inBetweenCode]?.seatTypeId === 5) return false;
            }
        }
        return true;
    }

    let selectedGroup = [];
    for (let i = clickedIndex - desiredQuantity + 1; i <= clickedIndex; i++) {
        if (i < 0) continue;
        const group = colList.slice(i, i + desiredQuantity);
        if (isValidGroup(group)) {
            selectedGroup = group;
            break;
        }
    }

    if (selectedGroup.length === 0) {
        for (let i = clickedIndex + 1; i <= colList.length - desiredQuantity; i++) {
            const group = colList.slice(i, i + desiredQuantity);
            if (isValidGroup(group)) {
                selectedGroup = group;
                break;
            }
        }
    }

    if (selectedGroup.length === 0) {
        showMessage(`Không tìm thấy ${desiredQuantity} ghế liền kề khả dụng.`);
        return;
    }

    const fullCodes = selectedGroup.map(col => col + row);
    if (selectedSeats.length + fullCodes.length > MAX_SEATS) {
        showMessage(`Bạn chỉ được chọn tối đa ${MAX_SEATS} ghế!`);
        return;
    }

    fullCodes.forEach(code => {
        selectedSeats.push(code);
        const seatElement = document.querySelector(`.seat[data-seat="${code}"]`);
        if (seatElement) {
            seatElement.classList.add("selected");
            const seatTypeId = seatInfoMap[code]?.seatTypeId;
            let selectedClass = "";
            if (seatTypeId === 1) selectedClass = "standard-selected";
            else if (seatTypeId === 2) selectedClass = "vip-selected";
            else if (seatTypeId === 3) selectedClass = "couple-selected";
            if (selectedClass) seatElement.classList.add(selectedClass);
        }
    });

    selectedGroups.set(fullCodes[0], fullCodes);
    updateSummary();
    lockInvalidRows(selectedSeats.length === MAX_SEATS ? 0 : desiredQuantity);
}

function generateSeatMap() {
    seatMap.innerHTML = "";
    if (!seatData.length) return;
    const rows = [...new Set(seatData.map(s => s.seatRow))].sort((a, b) => a - b);
    const columns = [...new Set(seatData.map(s => s.seatColumn))].sort();

    const headerRow = document.createElement("div");
    headerRow.classList.add("seat-grid");
    headerRow.style.marginBottom = "6px";
    const emptyCell = document.createElement("div");
    emptyCell.style.width = "40px";
    emptyCell.style.marginRight = "6px";
    headerRow.appendChild(emptyCell);
    columns.forEach(col => {
        const colLabel = document.createElement("div");
        colLabel.textContent = col;
        colLabel.style.width = "40px";
        colLabel.style.textAlign = "center";
        colLabel.style.color = "#ffd700";
        colLabel.style.fontWeight = "bold";
        headerRow.appendChild(colLabel);
    });
    seatMap.appendChild(headerRow);

    rows.forEach(row => {
        const rowDiv = document.createElement("div");
        rowDiv.classList.add("seat-grid");
        rowDiv.style.marginBottom = "6px";
        const rowLabel = document.createElement("div");
        rowLabel.textContent = row;
        rowLabel.style.width = "40px";
        rowLabel.style.textAlign = "center";
        rowLabel.style.color = "#ffd700";
        rowLabel.style.fontWeight = "bold";
        rowLabel.style.marginRight = "6px";
        rowDiv.appendChild(rowLabel);

        columns.forEach(col => {
            const code = col + row;
            const seat = seatInfoMap[code];
            const seatDiv = document.createElement("div");
            seatDiv.classList.add("seat");
            seatDiv.setAttribute("data-seat", code);
            if (!seat || seat.seatTypeId === 4) seatDiv.classList.add("empty");
            else if (seat.seatTypeId === 5) {
                seatDiv.classList.add("staircase");
                seatDiv.textContent = "⇅";
            } else {
                const type = seat.seatTypeName?.toLowerCase();
                if (type) seatDiv.classList.add(type);
                seatDiv.textContent = code;
                seatDiv.setAttribute("data-seat", code);
                seatDiv.setAttribute("data-seat-id", seat.scheduleSeatId);

                if (seat.isBooked) seatDiv.classList.add("seat-occupied");

                seatDiv.addEventListener("click", () => selectSeat(seatDiv, code));
                seatDiv.addEventListener("contextmenu", (e) => {
                    e.preventDefault();
                    for (const [key, group] of selectedGroups.entries()) {
                        if (group.includes(code)) {
                            group.forEach(c => {
                                const seatElement = document.querySelector(`.seat[data-seat="${c}"]`);
                                if (seatElement) seatElement.classList.remove("selected", "standard-selected", "vip-selected", "couple-selected");
                                selectedSeats = selectedSeats.filter(s => s !== c);
                            });
                            selectedGroups.delete(key);
                            updateSummary();
                            lockInvalidRows(desiredQuantity);
                            break;
                        }
                    }
                });
            }
            rowDiv.appendChild(seatDiv);
        });
        seatMap.appendChild(rowDiv);
    });
}

document.getElementById("resetSeatQtyBtn").addEventListener("click", () => {
    desiredQuantity = 0;
    seatQuantitySelect.value = "0";
    document.querySelectorAll('.seat-qty-btn').forEach(b => b.classList.remove("active"));

    // Reset ghế đã chọn
    selectedSeats.forEach(code => {
        const seatElement = document.querySelector(`.seat[data-seat="${code}"]`);
        if (seatElement) {
            seatElement.classList.remove("selected", "standard-selected", "vip-selected", "couple-selected");
        }
    });
    selectedSeats = [];
    selectedGroups.clear();

    updateSummary();
    lockInvalidRows(0);

    showMessage("Đã reset số lượng và ghế đã chọn!");
});

// Khi DOM đã load
document.addEventListener("DOMContentLoaded", () => {
    generateSeatMap();
    updateSummary();

    if (seatQuantitySelect) {
        seatQuantitySelect.addEventListener("change", () => {
            const qty = parseInt(seatQuantitySelect.value);
            setQuantity(qty || 0);
        });
    }

    const memberInputGroup = document.getElementById("memberInputGroup");

    if (memberRadio && guestRadio) {
        memberRadio.addEventListener("change", () => {
            if (memberRadio.checked && memberInputGroup && memberInfo && guestForm && notFound) {
                memberInputGroup.classList.remove("d-none");
                memberInfo.classList.add("d-none");
                guestForm.classList.add("d-none");
                notFound.classList.add("d-none");
            }
        });

        guestRadio.addEventListener("change", () => {
            if (guestRadio.checked && memberInputGroup && memberInfo && guestForm && notFound) {
                memberInputGroup.classList.add("d-none");
                memberInfo.classList.add("d-none");
                guestForm.classList.remove("d-none");
                notFound.classList.add("d-none");
            }
        });
    }

    const checkBtn = document.getElementById("checkMemberBtn");
    if (checkBtn) {
        checkBtn.addEventListener("click", () => {
            const input = document.getElementById("memberInput");
            if (!input) return;
            const keyword = input.value.trim();
            if (!keyword) return alert("Vui lòng nhập SĐT, CCCD hoặc Email!");

            fetch(`/employee/check?keyword=${encodeURIComponent(keyword)}`)
                .then(res => {
                    if (!res.ok) throw new Error("Không tìm thấy hội viên");
                    return res.json();
                })
                .then(data => {
                    if (memberInputGroup) memberInputGroup.classList.remove("d-none");
                    if (guestForm) guestForm.classList.add("d-none");
                    if (notFound) notFound.classList.add("d-none");
                    if (memberInfo) memberInfo.classList.remove("d-none");

                    const memberIdEl = document.getElementById("memberId");
                    const memberNameEl = document.getElementById("memberName");
                    const memberCCCD = document.getElementById("memberCCCD");
                    const memberPhone = document.getElementById("memberPhone");
                    const memberScore = document.getElementById("memberScore");

                    if (memberIdEl) memberIdEl.textContent = data.memberId;
                    if (memberNameEl) memberNameEl.textContent = data.fullName;
                    if (memberCCCD) memberCCCD.textContent = data.identityCard;
                    if (memberPhone) memberPhone.textContent = data.phoneNumber;
                    if (memberScore) memberScore.textContent = data.score;
                })
                .catch(err => {
                    console.error(err);
                    if (memberInputGroup) memberInputGroup.classList.remove("d-none");
                    if (guestForm) guestForm.classList.add("d-none");
                    if (memberInfo) memberInfo.classList.add("d-none");
                    if (notFound) notFound.classList.remove("d-none");
                });
        });
    }

    const resetButton = document.getElementById("resetButton");
    if (resetButton) {
        resetButton.addEventListener("click", () => {
            selectedSeats.forEach(code => {
                const seatElement = document.querySelector(`.seat[data-seat="${code}"]`);
                if (seatElement) seatElement.classList.remove("selected", "standard-selected", "vip-selected", "couple-selected");
            });
            selectedSeats = [];
            selectedGroups.clear();
            updateSummary();
            lockInvalidRows(desiredQuantity);
        });
    }

    document.getElementById("confirmBookingBtn").addEventListener("click", () => {
        if (selectedSeats.length === 0) {
            return alert("Vui lòng chọn ghế trước!");
        }

        let bookerInfo = "";
        if (memberRadio.checked) {
            const memberId = document.getElementById("memberId").textContent;
            const memberName = document.getElementById("memberName").textContent;
            if (!memberId) return alert("Vui lòng kiểm tra hội viên trước!");
            bookerInfo = `${memberName} (Hội viên #${memberId})`;
        } else {
            const name = document.getElementById("guestName").value.trim();
            const age = document.getElementById("guestAge").value.trim();
            const email = document.getElementById("guestEmail").value.trim();
            if (!name || !age || !email) return alert("Vui lòng nhập đầy đủ thông tin khách!");
            bookerInfo = `${name} (${email})`;
        }

        // Đổ dữ liệu vào popup
        document.getElementById("summarySeats").textContent = selectedSeats.join(", ");
        document.getElementById("summaryQuantity").textContent = selectedSeats.length;
        document.getElementById("summaryPrice").textContent = totalPriceText.textContent;
        document.getElementById("summaryBooker").textContent = bookerInfo;

        // Hiện modal
        const bookingModal = new bootstrap.Modal(document.getElementById("bookingSummaryModal"));
        bookingModal.show();
    });
});

document.getElementById("checkMemberBtn").addEventListener("click", () => {
    const keyword = document.getElementById("memberInput").value.trim();
    if (!keyword) return alert("Vui lòng nhập SĐT, CCCD hoặc Email!");
    fetch(`/employee/check?keyword=${encodeURIComponent(keyword)}`)
        .then(res => {
            if (!res.ok) throw new Error("Không tìm thấy hội viên");
            return res.json();
        })
        .then(data => {
            if (memberRadio.checked) {
                memberInputGroup.classList.remove("d-none");
                guestForm.classList.add("d-none");
                notFound.classList.add("d-none");
            }

            notFound.classList.add("d-none");
            memberInfo.classList.remove("d-none");
            document.getElementById("memberId").textContent = data.memberId;
            document.getElementById("memberName").textContent = data.fullName;
            document.getElementById("memberCCCD").textContent = data.identityCard;
            document.getElementById("memberPhone").textContent = data.phoneNumber;
            document.getElementById("memberScore").textContent = data.score;
        })
        .catch(err => {
            console.error(err);

            if (memberRadio.checked) {
                memberInputGroup.classList.remove("d-none"); // Hiện ô nhập
                guestForm.classList.add("d-none");           // Ẩn form khách
            }

            memberInfo.classList.add("d-none");
            notFound.classList.remove("d-none"); // Hiện dòng "Không tìm thấy"
        });

});

document.getElementById("resetButton").addEventListener("click", () => {
    selectedSeats.forEach(code => {
        const seatElement = document.querySelector(`.seat[data-seat="${code}"]`);
        if (seatElement) seatElement.classList.remove("selected", "standard-selected", "vip-selected", "couple-selected");
    });
    selectedSeats = [];
    selectedGroups.clear();
    updateSummary();
    lockInvalidRows(desiredQuantity);
});
document.getElementById("onlinePaymentForm").addEventListener("submit", function (e) {
    // === Lấy dữ liệu từ modal-body ===
    const seatsText = document.getElementById("summarySeats")?.textContent.trim() || "";
    const totalPriceText = document.getElementById("summaryPrice")?.textContent.trim() || "";
    const finalTotal = parseInt(totalPriceText.replace(/[^\d]/g, ''), 10) || 0;

    const scheduleId = document.getElementById("selectedScheduleId")?.value || "";
    const movieName = document.getElementById("selectedMovieName")?.value || "";
    const promotionId = document.getElementById("promotionSelect")?.value || "";
    const usedScore = parseInt(document.getElementById("usedScoreInput")?.value || "0", 10);
    const currentDate = new Date().toISOString().slice(0, 10);

    document.getElementById("amount").value = finalTotal;
    document.getElementById("orderInfo").value = `Đặt vé phim: ${movieName} (${seatsText})`;
    document.getElementById("movieName").value = movieName;
    document.getElementById("scheduleId").value = scheduleId;
    document.getElementById("selectedPromotionId").value = promotionId;
    document.getElementById("finalTotalHidden").value = finalTotal;
    document.getElementById("usedScoreHidden").value = usedScore;
    document.getElementById("date").value = currentDate;

    const seatCodes = seatsText.split(',').map(s => s.trim());
    console.log("Seat codes:", seatCodes);

    const seatContainer = document.getElementById("seatIdContainer");
    seatContainer.innerHTML = "";

    seatCodes.forEach(code => {
        const seatId = seatInfoMap?.[code]?.scheduleSeatId;
        console.log(`Seat code: ${code}, Mapped seatId: ${seatId}`);

        if (seatId) {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = "seatIds";
            input.value = seatId;
            seatContainer.appendChild(input);
        } else {
            console.warn(`Không tìm thấy seatId cho code: ${code}`);
        }
    });
    
    console.log("✅ Submit với:", {
        seatsText, finalTotal, movieName, scheduleId, promotionId, usedScore, currentDate
    });

    if (!finalTotal || !movieName || !scheduleId) {
        e.preventDefault();
        alert("Thiếu thông tin cần thiết để thanh toán.");
    }
    const finalInput = document.querySelector("input[name='finalTotal']");
    console.log("finalTotal:", finalInput?.value);

});



