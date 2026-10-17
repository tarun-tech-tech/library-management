(function () {
    "use strict";

    var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    function readCookie(name) {
        var prefix = name + "=";
        var cookies = document.cookie.split(";");
        for (var index = 0; index < cookies.length; index += 1) {
            var cookie = cookies[index].trim();
            if (cookie.indexOf(prefix) === 0) {
                return decodeURIComponent(cookie.substring(prefix.length));
            }
        }
        return "";
    }

    function getCsrfToken() {
        return fetch("/api/v1/auth/csrf", { credentials: "same-origin" })
            .then(function (response) {
                if (!response.ok) throw new Error("Could not prepare secure session");
                return response.json();
            })
            .then(function (body) { return body.token || readCookie("XSRF-TOKEN"); });
    }

    function request(url, options) {
        var config = options || {};
        config.credentials = "same-origin";
        config.headers = Object.assign({ "Content-Type": "application/json" }, config.headers || {});
        return getCsrfToken().then(function (token) {
            if (token) config.headers["X-XSRF-TOKEN"] = token;
            return fetch(url, config);
        }).then(function (response) {
            return response.text().then(function (text) {
                var payload = {};
                try { payload = text ? JSON.parse(text) : {}; } catch (ignore) { payload = { message: text }; }
                if (!response.ok) {
                    var message = payload.detail || payload.message || payload.error || "The request could not be completed.";
                    throw new Error(message);
                }
                return payload;
            });
        });
    }

    function showMessage(element, message) {
        if (!element) return;
        element.textContent = message || "";
    }

    function showToast(message) {
        var toast = document.querySelector(".toast");
        if (!toast) return;
        toast.textContent = message;
        toast.classList.add("is-visible");
        window.clearTimeout(showToast.timeout);
        showToast.timeout = window.setTimeout(function () { toast.classList.remove("is-visible"); }, 3600);
    }

    function initLogin() {
        var form = document.getElementById("login-form");
        if (!form) return;
        form.addEventListener("submit", function (event) {
            event.preventDefault();
            var button = form.querySelector("button[type='submit']");
            var message = document.getElementById("login-message");
            var username = form.elements.username.value.trim();
            var password = form.elements.password.value;
            button.disabled = true;
            showMessage(message, "");
            request("/api/v1/auth/login", {
                method: "POST",
                body: JSON.stringify({ username: username, password: password })
            }).then(function () {
                window.location.assign("/dashboard");
            }).catch(function (error) {
                showMessage(message, error.message || "Sign in failed.");
                button.disabled = false;
            });
        });
    }

    function initSidebar() {
        var toggle = document.querySelector("[data-sidebar-toggle]");
        var sidebar = document.querySelector(".sidebar");
        if (!toggle || !sidebar) return;
        toggle.addEventListener("click", function () { sidebar.classList.toggle("is-open"); });
        sidebar.querySelectorAll("a").forEach(function (link) {
            link.addEventListener("click", function () { sidebar.classList.remove("is-open"); });
        });
    }

    function initAccordions() {
        document.querySelectorAll("[data-accordion]").forEach(function (accordion) {
            accordion.querySelectorAll(".accordion-item").forEach(function (item) {
                item.addEventListener("click", function () {
                    accordion.querySelectorAll(".accordion-item").forEach(function (other) { other.classList.remove("is-open"); });
                    item.classList.add("is-open");
                });
            });
        });
    }

    function initFeedbackCarousel() {
        document.querySelectorAll("[data-feedback]").forEach(function (carousel) {
            var slides = Array.from(carousel.querySelectorAll("[data-feedback-slide]"));
            var next = carousel.querySelector("[data-feedback-next]");
            var previous = carousel.querySelector("[data-feedback-previous]");
            var index = 0;
            if (!slides.length) return;
            function render() {
                slides.forEach(function (slide, slideIndex) { slide.hidden = slideIndex !== index; });
                var counter = carousel.querySelector("[data-feedback-counter]");
                if (counter) counter.textContent = String(index + 1).padStart(2, "0") + " / " + String(slides.length).padStart(2, "0");
            }
            if (next) next.addEventListener("click", function () { index = (index + 1) % slides.length; render(); });
            if (previous) previous.addEventListener("click", function () { index = (index - 1 + slides.length) % slides.length; render(); });
            render();
        });
    }

    function initCirculation() {
        var form = document.getElementById("issue-form");
        if (form) {
            form.addEventListener("submit", function (event) {
                event.preventDefault();
                var payload = {
                    memberId: Number(form.elements.memberId.value),
                    bookCopyId: Number(form.elements.bookCopyId.value),
                    durationDays: form.elements.durationDays.value ? Number(form.elements.durationDays.value) : null
                };
                var message = document.getElementById("issue-message");
                var button = form.querySelector("button[type='submit']");
                button.disabled = true;
                showMessage(message, "Checking member, copy, and due date…");
                request("/api/v1/loans", { method: "POST", body: JSON.stringify(payload) })
                    .then(function (loan) {
                        showMessage(message, "Issued until " + loan.dueDate + ". Reloading the queue…");
                        window.setTimeout(function () { window.location.reload(); }, 650);
                    })
                    .catch(function (error) { showMessage(message, error.message); button.disabled = false; });
            });
        }
        document.querySelectorAll("[data-loan-return]").forEach(function (button) {
            button.addEventListener("click", function () {
                button.disabled = true;
                request("/api/v1/loans/" + button.getAttribute("data-loan-return") + "/return", { method: "POST" })
                    .then(function () { showToast("Book returned and inventory reconciled."); window.setTimeout(function () { window.location.reload(); }, 550); })
                    .catch(function (error) { showToast(error.message); button.disabled = false; });
            });
        });
        document.querySelectorAll("[data-loan-renew]").forEach(function (button) {
            button.addEventListener("click", function () {
                button.disabled = true;
                request("/api/v1/loans/" + button.getAttribute("data-loan-renew") + "/renew", { method: "POST", body: JSON.stringify({}) })
                    .then(function () { showToast("Loan renewed within library rules."); window.setTimeout(function () { window.location.reload(); }, 550); })
                    .catch(function (error) { showToast(error.message); button.disabled = false; });
            });
        });
    }

    function initFinePayments() {
        document.querySelectorAll("[data-fine-pay]").forEach(function (form) {
            form.addEventListener("submit", function (event) {
                event.preventDefault();
                var button = form.querySelector("button[type='submit']");
                var message = form.querySelector(".form-message");
                var payload = {
                    amount: Number(form.elements.amount.value),
                    paymentMethod: form.elements.paymentMethod.value,
                    referenceNumber: form.elements.referenceNumber.value.trim() || null
                };
                button.disabled = true;
                showMessage(message, "Recording payment…");
                request("/api/v1/fines/" + form.getAttribute("data-fine-pay") + "/payments", {
                    method: "POST",
                    body: JSON.stringify(payload)
                }).then(function () {
                    showToast("Payment recorded and balance reconciled.");
                    window.setTimeout(function () { window.location.reload(); }, 650);
                }).catch(function (error) {
                    showMessage(message, error.message);
                    button.disabled = false;
                });
            });
        });
    }

    function initToasts() {
        document.querySelectorAll("[data-toast]").forEach(function (element) {
            element.addEventListener("click", function () { showToast(element.getAttribute("data-toast")); });
        });
    }

    function initMotion() {
        var images = document.querySelectorAll(".image-reveal");
        if (window.gsap && window.ScrollTrigger && !reduceMotion) {
            gsap.registerPlugin(ScrollTrigger);
            var media = gsap.matchMedia();
            media.add("(prefers-reduced-motion: no-preference)", function () {
                gsap.utils.toArray(".reveal-section").forEach(function (section, index) {
                    gsap.fromTo(section, { y: 34, opacity: 0 }, {
                        y: 0, opacity: 1, duration: .85, delay: Math.min(index * .035, .25), ease: "power3.out",
                        scrollTrigger: { trigger: section, start: "top 88%", once: true }
                    });
                });
                gsap.utils.toArray(".image-reveal").forEach(function (image) {
                    gsap.fromTo(image.querySelector("img"), { scale: .8, opacity: .22 }, {
                        scale: 1, opacity: 1, ease: "power2.out",
                        scrollTrigger: { trigger: image, start: "top 90%", end: "bottom 10%", scrub: 1 }
                    });
                    ScrollTrigger.create({ trigger: image, start: "top 90%", end: "bottom 10%", onEnter: function () { image.classList.add("is-inview"); }, onLeaveBack: function () { image.classList.remove("is-inview"); } });
                });
                var pin = document.querySelector(".pin-title");
                if (pin && window.innerWidth > 820) {
                    ScrollTrigger.create({ trigger: pin, start: "top 18%", endTrigger: ".pinned-story", end: "bottom 78%", pin: true, pinSpacing: false });
                }
            });
        } else {
            images.forEach(function (image) { image.classList.add("is-inview"); });
            document.querySelectorAll(".reveal-section").forEach(function (section) { section.style.opacity = "1"; });
        }
    }

    initLogin();
    initSidebar();
    initAccordions();
    initFeedbackCarousel();
    initCirculation();
    initFinePayments();
    initToasts();
    initMotion();
})();
