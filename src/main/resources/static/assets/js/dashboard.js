document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login';
        return;
    }

    // Fetch user profile
    fetch('/api/v1/users/me', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Not authenticated');
        }
        return response.json();
    })
    .then(data => {
        if (data.status === 200) {
            const user = data.data;
            updateUI(user);
        } else {
            handleLogout();
        }
    })
    .catch(error => {
        console.error('Error fetching profile:', error);
        handleLogout();
    });

    function updateUI(user) {
        // Update name and username in various places
        const nameElements = document.querySelectorAll('.user-full-name');
        nameElements.forEach(el => el.textContent = user.fullName || user.username);

        const usernameElements = document.querySelectorAll('.user-username');
        usernameElements.forEach(el => el.textContent = '@' + user.username);

        // Update school info if available
        const schoolNameElements = document.querySelectorAll('.school-name');
        if (user.school && user.school.name) {
            schoolNameElements.forEach(el => el.textContent = user.school.name);
        } else {
            schoolNameElements.forEach(el => el.textContent = 'No School Assigned');
        }
        
        // Update welcome message
        const welcomeHeader = document.querySelector('.welcome-header');
        if (welcomeHeader) {
            welcomeHeader.textContent = '👋 Hello ' + (user.fullName || user.username).split(' ')[0] + ',';
        }

        // Update avatar if available
        if (user.avatarUrl) {
            const avatars = document.querySelectorAll('.user-avatar');
            avatars.forEach(img => img.src = user.avatarUrl);
        }
    }

    // Logout functionality
    const logoutButtons = document.querySelectorAll('.logout-btn');
    logoutButtons.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            handleLogout();
        });
    });

    function handleLogout() {
        const token = localStorage.getItem('token');
        const cleanup = () => {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('userRole');
            window.location.href = '/login';
        };
        if (token) {
            fetch('/api/v1/users/logout', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            }).finally(cleanup);
        } else {
            cleanup();
        }
    }
});
