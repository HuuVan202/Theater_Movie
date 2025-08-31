document.addEventListener('DOMContentLoaded', () => {
    const chatButton = document.getElementById('chatButton');
    const chatPopup = document.getElementById('chatPopup');
    const closeChat = document.getElementById('closeChat');
    const chatForm = document.getElementById('chatForm');
    const messageInput = document.getElementById('messageInput');
    const chatMessages = document.getElementById('chatMessages');
    const typingIndicator = document.getElementById('typingIndicator');

    // Store chat state
    let isChatOpen = false;
    let ws = null;

    // Toggle chat popup
    function toggleChat() {
        isChatOpen = !isChatOpen;
        chatPopup.classList.toggle('active', isChatOpen);

        if (isChatOpen && !ws) {
            connectWebSocket();
        }
    }

    // Connect WebSocket
    function connectWebSocket() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/chat`;

        ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            console.log('WebSocket Connected');
        };

        ws.onmessage = (event) => {
            const response = JSON.parse(event.data);
            showTypingIndicator(false);
            addMessage(response.message, 'received');
        };

        ws.onclose = () => {
            console.log('WebSocket Disconnected');
            ws = null;
        };

        ws.onerror = (error) => {
            console.error('WebSocket Error:', error);
            addMessage('Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.', 'received');
        };
    }

    // Add message to chat
    function addMessage(text, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        messageDiv.textContent = text;

        chatMessages.insertBefore(messageDiv, typingIndicator);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Show/hide typing indicator
    function showTypingIndicator(show) {
        typingIndicator.style.display = show ? 'block' : 'none';
    }

    // Event Listeners
    chatButton.addEventListener('click', toggleChat);
    closeChat.addEventListener('click', toggleChat);

    chatForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const message = messageInput.value.trim();

        if (message) {
            addMessage(message, 'sent');
            showTypingIndicator(true);

            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({
                    type: 'message',
                    content: message
                }));
            } else {
                // Fallback to REST API if WebSocket is not available
                fetch('/api/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ message })
                })
                .then(response => response.json())
                .then(data => {
                    showTypingIndicator(false);
                    addMessage(data.message, 'received');
                })
                .catch(error => {
                    console.error('Error:', error);
                    showTypingIndicator(false);
                    addMessage('Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.', 'received');
                });
            }

            messageInput.value = '';
        }
    });

    // Close chat when clicking outside
    document.addEventListener('click', (e) => {
        if (isChatOpen &&
            !chatPopup.contains(e.target) &&
            !chatButton.contains(e.target)) {
            toggleChat();
        }
    });

    // Store chat state in localStorage
    const chatState = localStorage.getItem('chatState');
    if (chatState === 'open') {
        toggleChat();
    }

    window.addEventListener('beforeunload', () => {
        localStorage.setItem('chatState', isChatOpen ? 'open' : 'closed');
    });

    // Auto-resize input field
    messageInput.addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = (this.scrollHeight) + 'px';
    });
});
