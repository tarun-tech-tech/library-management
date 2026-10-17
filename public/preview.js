(function () {
    var toast = document.querySelector(".toast");
    document.querySelectorAll("[data-toast]").forEach(function (button) {
        button.addEventListener("click", function () {
            toast.textContent = button.getAttribute("data-toast");
            toast.classList.add("show");
            window.setTimeout(function () { toast.classList.remove("show"); }, 2600);
        });
    });
    document.querySelectorAll("[data-carousel]").forEach(function (carousel) {
        var slides = Array.from(carousel.querySelectorAll("[data-slide]"));
        var counter = carousel.querySelector("[data-count]");
        var index = 0;
        function render() {
            slides.forEach(function (slide, slideIndex) { slide.hidden = slideIndex !== index; });
            counter.textContent = String(index + 1).padStart(2, "0") + " / " + String(slides.length).padStart(2, "0");
        }
        carousel.querySelector("[data-prev]").addEventListener("click", function () { index = (index - 1 + slides.length) % slides.length; render(); });
        carousel.querySelector("[data-next]").addEventListener("click", function () { index = (index + 1) % slides.length; render(); });
        render();
    });
})();
