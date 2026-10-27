/*
 * Password visibility toggle.
 *
 * Every button that carries the "data-password-toggle" attribute switches the
 * password input named by the attribute value between masked and visible text.
 * The button never submits a form and never changes the typed value.
 */
(function () {
    "use strict";

    function handleToggle(button) {
        var input = document.getElementById(button.getAttribute("data-password-toggle"));
        if (!input) {
            return;
        }

        var reveal = input.type === "password";
        input.type = reveal ? "text" : "password";

        var wrapper = button.closest(".password-field");
        if (wrapper) {
            wrapper.classList.toggle("is-visible", reveal);
        }

        button.setAttribute("aria-pressed", reveal ? "true" : "false");
        button.setAttribute("aria-label", reveal ? "Hide password" : "Show password");
        button.setAttribute("title", reveal ? "Hide password" : "Show password");

        // Keep the caret at the end so toggling does not disturb the typed value.
        if (typeof input.setSelectionRange === "function" && input.type === "text") {
            var end = input.value.length;
            input.setSelectionRange(end, end);
        }
        input.focus();
    }

    document.addEventListener("click", function (event) {
        var target = event.target;
        if (!target || typeof target.closest !== "function") {
            return;
        }
        var button = target.closest("[data-password-toggle]");
        if (!button) {
            return;
        }
        event.preventDefault();
        handleToggle(button);
    });
})();
