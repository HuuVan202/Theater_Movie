document.addEventListener('DOMContentLoaded', function () {
    const chatMessages = document.getElementById('chatMessages');
    const chatForm = document.getElementById('chatForm');
    const messageContent = document.getElementById('messageContent');
    const dialogueId = document.getElementById('dialogueId') ? Number(document.getElementById('dialogueId').value) : null;
    const accountId = Number(document.getElementById('accountId').value);

    if (!dialogueId || !chatForm || !chatMessages) {
        console.log('Missing required elements.');
        return;
    }

    chatMessages.scrollTop = chatMessages.scrollHeight;

    let stompClient = null;
    if (dialogueId) {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function () {
            stompClient.subscribe('/topic/chat/' + dialogueId, function (message) {
                try {
                    const chat = JSON.parse(message.body);
                    if (chat && chat.tempId) {
                        replaceTempMessage(chat.tempId, chat);
                    } else if (chat && chat.messageId) {
                        updateChatMessage(chat);
                    }
                } catch (e) {
                    console.error('Error parsing message:', e);
                }
            });
        }, function (error) {
            console.error('WebSocket connection error:', error);
        });
    }

    function isImageMessage(content) {
        return content && content.startsWith('IMAGE:');
    }

    function getImageUrl(content) {
        return content.substring(6);
    }

    function createImageElement(imageUrl) {
        const img = document.createElement('img');
        img.src = imageUrl;
        img.className = 'chat-image';
        img.alt = 'Chat image';
        img.onclick = () => openImageModal(imageUrl);
        return img;
    }

    function updateChatMessage(chat, isTemp = false) {
        if (!chat || !chatMessages) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${chat.senderId == accountId ? 'user-message' : 'support-message'}`;
        if (chat.messageId) {
            messageDiv.setAttribute('data-message-id', chat.messageId);
        }
        if (isTemp && chat.tempId) {
            messageDiv.setAttribute('data-temp-id', chat.tempId);
        }

        if (isImageMessage(chat.messageContent)) {
            const imageContainer = document.createElement('div');
            imageContainer.className = 'image-container';
            imageContainer.appendChild(createImageElement(getImageUrl(chat.messageContent)));
            messageDiv.appendChild(imageContainer);
        } else {
            const p = document.createElement('p');
            p.textContent = chat.messageContent || 'Nội dung trống';
            messageDiv.appendChild(p);
        }

        const sentAt = chat.sentAt ? new Date(chat.sentAt) : new Date();
        const timeString = !isNaN(sentAt) ? sentAt.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Thời gian không xác định';
        const timeElement = document.createElement('div');
        timeElement.className = 'message-time';
        timeElement.textContent = timeString;

        messageDiv.appendChild(timeElement);

        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function replaceTempMessage(tempId, chat) {
        if (!chat || !chatMessages) return;

        const tempMessage = document.querySelector(`[data-temp-id="${tempId}"]`);
        if (tempMessage && chat.messageId) {
            const sentAt = chat.sentAt ? new Date(chat.sentAt) : new Date();
            const timeString = !isNaN(sentAt) ? sentAt.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Thời gian không xác định';

            const messageDiv = document.createElement('div');
            messageDiv.className = `message ${chat.senderId == accountId ? 'user-message' : 'support-message'}`;
            messageDiv.setAttribute('data-message-id', chat.messageId);

            let contentElement = document.createElement('div');
            if (isImageMessage(chat.messageContent)) {
                contentElement.appendChild(createImageElement(getImageUrl(chat.messageContent)));
            } else {
                contentElement.textContent = chat.messageContent || 'Nội dung trống';
            }

            const timeElement = document.createElement('div');
            timeElement.className = 'message-time';
            timeElement.textContent = timeString;

            messageDiv.appendChild(contentElement);
            messageDiv.appendChild(timeElement);

            tempMessage.replaceWith(messageDiv);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        } else {
            updateChatMessage(chat);
        }
    }

    if (chatForm) {
        chatForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const message = messageContent.value.trim();
            const fileInput = document.getElementById('fileInput');
            const file = fileInput.files[0];

            if (!message && !file) {
                alert('Vui lòng nhập nội dung tin nhắn hoặc chọn ảnh.');
                return;
            }

            const tempId = Date.now();
            let tempMessageContent = message || '';

            if (file) {
                tempMessageContent = 'IMAGE:' + URL.createObjectURL(file);
            }

            const tempChat = {
                dialogueId: dialogueId,
                senderId: accountId,
                messageContent: tempMessageContent,
                tempId: tempId,
                sentAt: new Date().toISOString()
            };
            updateChatMessage(tempChat, true);

            if (stompClient && stompClient.connected && !file) {
                const textChat = {
                    dialogueId: dialogueId,
                    senderId: accountId,
                    messageContent: message,
                    tempId: tempId,
                    sentAt: new Date().toISOString()
                };
                stompClient.send('/app/chat/' + dialogueId, {}, JSON.stringify(textChat));
                messageContent.value = '';
                fileInput.value = '';
            } else {
                const formData = new FormData();
                formData.append('dialogueId', dialogueId);
                formData.append('senderId', accountId);
                formData.append('messageContent', message || '');
                if (file) formData.append('file', file);

                fetch('/chat/send', {
                    method: 'POST',
                    body: formData
                })
                    .then(response => response.json())
                    .then(chat => {
                        if (chat && chat.messageId) {
                            replaceTempMessage(tempId, chat);
                        }
                        messageContent.value = '';
                        fileInput.value = '';
                    })
                    .catch(error => {
                        console.error('Error sending message:', error);
                        alert('Không thể gửi tin nhắn hoặc ảnh. Vui lòng thử lại.');
                        const tempMessage = document.querySelector(`[data-temp-id="${tempId}"]`);
                        if (tempMessage) tempMessage.remove();
                    });
            }
        });
    }

    // Emoji picker handling
    if (emojiBtn && emojiPickerContainer && emojiPicker && messageContent) {
        // Toggle emoji picker on button click
        emojiBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            const isVisible = emojiPickerContainer.style.display === 'block';
            emojiPickerContainer.style.display = isVisible ? 'none' : 'block';

            if (!isVisible) {
                // Position the picker below the button
                const rect = emojiBtn.getBoundingClientRect();
                emojiPickerContainer.style.position = 'absolute';
                emojiPickerContainer.style.left = `${rect.left}px`;
                emojiPickerContainer.style.top = `${rect.bottom + window.scrollY + 5}px`;
                emojiPickerContainer.style.zIndex = '1000';
            }
        });

        // Insert emoji into textarea
        emojiPicker.addEventListener('emoji-click', function (event) {
            messageContent.value += event.detail.unicode;
            messageContent.focus();
            emojiPickerContainer.style.display = 'none';
        });

        // Hide picker when clicking outside
        document.addEventListener('click', function (e) {
            if (!emojiPickerContainer.contains(e.target) && !emojiBtn.contains(e.target)) {
                emojiPickerContainer.style.display = 'none';
            }
        });
    }

    window.openImageModal = function(imageSrc) {
        const modal = document.getElementById('imageModal');
        const modalImg = document.getElementById('modalImage');
        if (modal && modalImg) {
            modal.style.display = 'block';
            modalImg.src = imageSrc;
            document.body.style.overflow = 'hidden';
        }
    };
});