// Doughnut Chart
const doughnutCtx = document.getElementById('doughnutChart').getContext('2d');
new Chart(doughnutCtx, {
    type: 'doughnut',
    data: {
        labels: ['Chrome', 'IE', 'FireFox', 'Safari', 'Opera', 'Navigator'],
        datasets: [{
            data: [30, 10, 20, 15, 15, 10],
            backgroundColor: [
                '#3b82f6', '#6366f1', '#f59e42', '#22c55e', '#ef4444', '#fbbf24'
            ],
            borderWidth: 2
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'right',
                labels: { boxWidth: 18, padding: 18 }
            }
        },
        animation: {
            animateScale: true
        }
    }
});

// Bar Chart
const barCtx = document.getElementById('barChart').getContext('2d');
new Chart(barCtx, {
    type: 'bar',
    data: {
        labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
        datasets: [
            {
                label: 'Electronics',
                backgroundColor: '#3b82f6',
                data: [65, 59, 80, 81, 56, 55, 40]
            },
            {
                label: 'Fashion',
                backgroundColor: '#ef4444',
                data: [28, 48, 40, 19, 86, 27, 90]
            },
            {
                label: 'Foods',
                backgroundColor: '#22c55e',
                data: [35, 40, 60, 47, 88, 27, 100]
            }
        ]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            }
        },
        animation: {
            duration: 1200,
            easing: 'easeOutBounce'
        }
    }
});
