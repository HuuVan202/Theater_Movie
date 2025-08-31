const toggleBtn = document.getElementById("theme-toggle");
const body = document.body;
const icon = toggleBtn.querySelector("i");

function updateIcon(theme) {
    if (theme === "dark") {
        icon.classList.remove("bi-sun");
        icon.classList.add("bi-moon-stars");
    } else {
        icon.classList.remove("bi-moon-stars");
        icon.classList.add("bi-sun");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const savedTheme = localStorage.getItem("theme") || "dark";
    body.classList.add(savedTheme + "-mode");
    updateIcon(savedTheme);

    toggleBtn.addEventListener("click", () => {
        body.classList.toggle("dark-mode");
        body.classList.toggle("light-mode");

        const newTheme = body.classList.contains("dark-mode") ? "dark" : "light";
        localStorage.setItem("theme", newTheme);
        updateIcon(newTheme);
    });
});
