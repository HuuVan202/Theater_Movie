document.addEventListener('DOMContentLoaded', function () {
    const hamburgerBtn = document.querySelector('.hamburger-btn');
    const mainNav = document.querySelector('.main-nav');
    const notificationBell = document.querySelector('.notification-bell');

    hamburgerBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        mainNav.classList.toggle('active');
    });

    document.addEventListener('click', function (e) {
        if (!mainNav.contains(e.target) && !hamburgerBtn.contains(e.target)) {
            mainNav.classList.remove('active');
        }
    });

    document.addEventListener('click', function (e) {
        if (mainNav && hamburgerBtn && !mainNav.contains(e.target) && !hamburgerBtn.contains(e.target)) {
            mainNav.classList.remove('active');
        }
        if (notificationDropdown && notificationBell && !notificationDropdown.contains(e.target) && !notificationBell.contains(e.target)) {
            notificationDropdown.classList.remove('show');
        }
        if (userDropdown && !e.target.closest('.user-avatar') && !e.target.closest('.dropdown-menu')) {
            userDropdown.classList.remove('show');
        }
    });

    function toggleDropdown() {
        const dropdown = document.getElementById('dropdownMenu');
        if (dropdown) {
            dropdown.classList.toggle('show');
            const notificationDropdown = document.getElementById('notificationDropdown');
            if (notificationDropdown && notificationDropdown.classList.contains('show')) {
                notificationDropdown.classList.remove('show');
            }
        }
    }

    document.addEventListener('click', function (event) {
        if (!event.target.closest('.user-avatar') && !event.target.closest('.dropdown-menu')) {
            const dropdown = document.getElementById('dropdownMenu');
            if (dropdown) {
                dropdown.classList.remove('show');
            }
        }
    });

    let stompClient = null;
    const accountId = document.querySelector('meta[name="account-id"]')?.getAttribute('content') || null;
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    let currentNotificationId = null;

    function connectWebSocket() {
        if (!accountId) {
            console.log('No account ID found, WebSocket connection not established');
            return;
        }

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('WebSocket Connected: ' + frame);
            // Subscribe to notifications
            stompClient.subscribe('/topic/notifications/' + accountId, function (notification) {
                const notificationData = JSON.parse(notification.body);
                console.log('Received notification:', notificationData);
                if (notificationData.notificationId) {
                    // Handle deletion event
                    handleNotificationDeletion(notificationData.notificationId);
                } else {
                    // Handle new notification
                    addNotificationToDropdown(notificationData);
                }
            });
            // Subscribe to unread count updates
            stompClient.subscribe('/topic/unread-count/' + accountId, function (unreadCountMessage) {
                const unreadCount = parseInt(unreadCountMessage.body) || 0;
                console.log('Received unread count update:', unreadCount);
                updateNotificationBadge(unreadCount);
            });
            fetchInitialUnreadCount();
        }, function (error) {
            console.error('WebSocket connection error:', error);
            setTimeout(connectWebSocket, 5000);
        });
    }

    function fetchInitialUnreadCount() {
        fetch('/notifications', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
            .then(response => response.json())
            .then(notifications => {
                const unreadCount = notifications.filter(n => !n.status).length;
                console.log('Fetched initial unread count:', unreadCount);
                updateNotificationBadge(unreadCount);
            })
            .catch(error => {
                console.error('Error fetching initial unread count:', error);
            });
    }

    function addNotificationToDropdown(notification) {
        const notificationList = document.querySelector('.notification-list');
        if (!notificationList) {
            console.error('Notification list element not found');
            return;
        }

        const notificationItem = document.createElement('div');
        notificationItem.className = 'notification-item' + (notification.status ? '' : ' unread');
        notificationItem.setAttribute('data-notification-id', notification.notification_Id);
        notificationItem.setAttribute('data-notification-title', notification.title);
        notificationItem.setAttribute('data-notification-message', notification.message);
        notificationItem.setAttribute('data-notification-date', new Date(notification.date).toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        }).replace(/,/, ''));
        notificationItem.setAttribute('data-notification-status', notification.status);

        notificationItem.innerHTML = `
            <div class="notification-icon success">
                <i class="fas fa-check"></i>
            </div>
            <div class="notification-content">
                <div class="notification-title">${notification.title}</div>
                <div class="notification-message">${notification.message}</div>
                <div class="notification-time-container">
                    <span class="delete-notification" onclick="deleteNotification(event, this)">Delete</span>
                    <div class="notification-time">${notificationItem.getAttribute('data-notification-date')}</div>
                </div>
            </div>
        `;

        notificationItem.onclick = () => showNotificationPopup(notificationItem);
        notificationList.prepend(notificationItem);
        console.log('Added notification to dropdown, waiting for unread count update');
    }

    function handleNotificationDeletion(notificationId) {
        const notificationList = document.querySelector('.notification-list');

        if (notificationList.children.length === 0) {
            const notificationItemEmpty = document.createElement('div');
            notificationItemEmpty.className = 'notification-empty';
            notificationItemEmpty.style.display = 'flex';
            notificationItemEmpty.innerHTML = '<p>Hiện tại không có thông báo</p>';
            notificationList.prepend(notificationItemEmpty);

            const notificationFooter = document.querySelector('.view-all-notifications');
            if (notificationFooter) {
                notificationFooter.style.display = 'none';
            }
        }

        const notificationItem = document.querySelector(`.notification-item[data-notification-id="${notificationId}"]`);
        if (notificationItem) {
            notificationItem.remove();
            console.log('Removed notification from UI:', notificationId);
            updateNotificationBadge();
        } else {
            console.warn('Notification item not found for deletion:', notificationId);
        }
    }

    function updateNotificationBadge(serverUnreadCount = null) {
        const badge = document.querySelector('.notification-badge');
        if (!badge) {
            console.error('Notification badge element not found');
            return;
        }

        let count = serverUnreadCount !== null ? serverUnreadCount : document.querySelectorAll('.notification-item.unread').length;
        console.log('Updating badge with count:', count);
        badge.textContent = count;
        badge.style.display = count > 0 ? 'flex' : 'none';

        const notificationList = document.querySelector('.notification-list');
        const notificationEmpty = document.querySelector('.notification-empty');
        if (notificationList && notificationEmpty) {
            const hasNotifications = notificationList.querySelectorAll('.notification-item').length > 0;
            notificationEmpty.style.display = hasNotifications ? 'none' : 'block';
        }
    }

    function toggleNotifications() {
        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown) {
            dropdown.classList.toggle('show');
            const userDropdown = document.getElementById('dropdownMenu');
            if (userDropdown && userDropdown.classList.contains('show')) {
                userDropdown.classList.remove('show');
            }
        }
    }

    function markAllAsRead(event) {
        event.stopPropagation();
        const unreadItems = document.querySelectorAll('.notification-item.unread');
        unreadItems.forEach(item => item.classList.remove('unread'));
        updateNotificationBadge(0);

        fetch('/notifications/mark-all-read', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        }).then(response => {
            if (!response.ok) {
                console.error('Failed to mark notifications as read:', response.statusText);
                unreadItems.forEach(item => item.classList.add('unread'));
                updateNotificationBadge();
                alert('Failed to mark notifications as read. Please try again.');
            }
        }).catch(error => {
            console.error('Error marking notifications as read:', error);
            unreadItems.forEach(item => item.classList.add('unread'));
            updateNotificationBadge();
            alert('Error occurred while marking notifications as read.');
        });
    }

    function showNotificationPopup(element) {
        const popup = document.getElementById('notificationPopup');
        const overlay = document.getElementById('notificationPopupOverlay');
        const title = document.getElementById('popupTitle');
        const message = document.getElementById('popupMessage');
        const time = document.getElementById('popupTime');
        const markReadButton = document.getElementById('markReadButton');

        currentNotificationId = element.getAttribute('data-notification-id');
        title.textContent = element.getAttribute('data-notification-title');
        message.textContent = element.getAttribute('data-notification-message');
        time.textContent = element.getAttribute('data-notification-date');
        const isRead = element.getAttribute('data-notification-status') === 'true';
        markReadButton.disabled = isRead;
        markReadButton.style.display = isRead ? 'none' : 'inline-block';

        popup.classList.add('show');
        overlay.classList.add('show');
    }

    function closeNotificationPopup() {
        const popup = document.getElementById('notificationPopup');
        const overlay = document.getElementById('notificationPopupOverlay');
        popup.classList.remove('show');
        overlay.classList.remove('show');
        currentNotificationId = null;
    }

    function markNotificationAsRead() {
        if (!currentNotificationId) {
            console.error('No notification ID selected');
            return;
        }

        const notificationItem = document.querySelector(`.notification-item[data-notification-id="${currentNotificationId}"]`);
        if (!notificationItem) {
            console.error('Notification item not found for ID:', currentNotificationId);
            return;
        }

        notificationItem.classList.remove('unread');
        document.getElementById('markReadButton').disabled = true;
        document.getElementById('markReadButton').style.display = 'none';
        updateNotificationBadge();

        fetch('/notifications/mark-read', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: 'notificationId=' + encodeURIComponent(currentNotificationId)
        }).then(response => {
            if (!response.ok) {
                console.error('Failed to mark notification as read:', response.statusText);
                notificationItem.classList.add('unread');
                document.getElementById('markReadButton').disabled = false;
                document.getElementById('markReadButton').style.display = 'inline-block';
                updateNotificationBadge();
                alert('Failed to mark notification as read. Please try again.');
            } else {
                console.log('Notification marked as read:', currentNotificationId);
                closeNotificationPopup();
            }
        }).catch(error => {
            console.error('Error marking notification as read:', error);
            notificationItem.classList.add('unread');
            document.getElementById('markReadButton').disabled = false;
            document.getElementById('markReadButton').style.display = 'inline-block';
            updateNotificationBadge();
            alert('Error occurred while marking notification as read.');
        });
    }

    function deleteNotification(event, element) {
        event.stopPropagation();
        const notificationId = element.closest('.notification-item').getAttribute('data-notification-id');
        if (!notificationId) {
            console.error('No notification ID found');
            return;
        }

        if (confirm('Are you sure you want to delete this notification?')) {
            const notificationItem = element.closest('.notification-item');
            notificationItem.remove();
            updateNotificationBadge();

            fetch('/notifications/delete-notification', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                body: 'notificationId=' + encodeURIComponent(notificationId)
            }).then(response => {
                if (!response.ok) {
                    console.error('Failed to delete notification:', response.statusText);
                    document.querySelector('.notification-list').prepend(notificationItem);
                    updateNotificationBadge();
                    alert('Failed to delete notification. Please try again.');
                } else {
                    console.log('Notification deleted:', notificationId);
                    // WebSocket will handle updating the unread count and other clients
                }
            }).catch(error => {
                console.error('Error deleting notification:', error);
                document.querySelector('.notification-list').prepend(notificationItem);
                updateNotificationBadge();
                alert('Error occurred while deleting notification.');
            });
        }
    }

    if (accountId) {
        connectWebSocket();
    }

    window.toggleNotifications = toggleNotifications;
    window.markAllAsRead = markAllAsRead;
    window.showNotificationPopup = showNotificationPopup;
    window.closeNotificationPopup = closeNotificationPopup;
    window.markNotificationAsRead = markNotificationAsRead;
    window.toggleDropdown = toggleDropdown;
    window.deleteNotification = deleteNotification;

    document.getElementById('notificationPopupOverlay').addEventListener('click', closeNotificationPopup);
});