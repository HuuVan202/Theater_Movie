// // Lấy ngôn ngữ đã lưu từ localStorage hoặc mặc định là 'vi'
// let currentLang = localStorage.getItem('selectedLanguage') || 'vi';
//
// // Hàm cập nhật nội dung theo ngôn ngữ
// function updateContent(lang) {
//     // Cập nhật tất cả các phần tử có thuộc tính data-i18n
//     document.querySelectorAll('[data-i18n]').forEach(element => {
//         const key = element.getAttribute('data-i18n');
//         if (translations[lang] && translations[lang][key]) {
//             if (element.getAttribute('data-i18n-html')) {
//                 element.innerHTML = translations[lang][key];
//             } else {
//                 element.textContent = translations[lang][key];
//             }
//         }
//     });
//
//     // Cập nhật placeholder cho input
//     document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
//         const key = element.getAttribute('data-i18n-placeholder');
//         if (translations[lang] && translations[lang][key]) {
//             element.placeholder = translations[lang][key];
//         }
//     });
//
//     // Cập nhật trạng thái active của nút ngôn ngữ
//     document.querySelectorAll('.lang-btn').forEach(btn => {
//         if (btn.getAttribute('data-lang') === lang) {
//             btn.classList.add('active');
//         } else {
//             btn.classList.remove('active');
//         }
//     });
//
//     // Lưu ngôn ngữ đã chọn vào localStorage
//     localStorage.setItem('selectedLanguage', lang);
//     currentLang = lang;
// }
//
// // Hàm chuyển đổi ngôn ngữ
// function changeLanguage(lang) {
//     if (lang !== currentLang) {
//         updateContent(lang);
//     }
// }
//
// // Create a MutationObserver to handle dynamically added elements
// const observer = new MutationObserver(mutations => {
//     mutations.forEach(mutation => {
//         if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
//             // When new nodes are added, apply translations to them
//             mutation.addedNodes.forEach(node => {
//                 if (node.nodeType === 1) { // Element node
//                     // Apply translations to the new element and its children
//                     const elements = [node, ...node.querySelectorAll('[data-i18n]')];
//                     elements.forEach(element => {
//                         if (element.hasAttribute && element.hasAttribute('data-i18n')) {
//                             const key = element.getAttribute('data-i18n');
//                             if (translations[currentLang] && translations[currentLang][key]) {
//                                 if (element.getAttribute('data-i18n-html')) {
//                                     element.innerHTML = translations[currentLang][key];
//                                 } else {
//                                     element.textContent = translations[currentLang][key];
//                                 }
//                             }
//                         }
//                     });
//                 }
//             });
//         }
//     });
// });
//
// // Apply translations when page loads
// document.addEventListener('DOMContentLoaded', () => {
//     updateContent(currentLang);
//
//     // Start observing the entire document for changes
//     observer.observe(document.body, {
//         childList: true,
//         subtree: true
//     });
// });
//
// // Make updateContent available globally
// window.updateTranslations = function() {
//     updateContent(currentLang);
// };
