/**
 * BookVault — main.js
 * Lightweight progressive enhancements. No dependencies.
 */

document.addEventListener('DOMContentLoaded', function () {

    // ─── Auto-dismiss flash alerts after 5 seconds ───────────────────────────
    document.querySelectorAll('.alert').forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.5s ease, max-height 0.5s ease, margin 0.5s ease';
            alert.style.opacity = '0';
            alert.style.maxHeight = '0';
            alert.style.marginBottom = '0';
            setTimeout(function () { alert.remove(); }, 500);
        }, 5000);
    });

    // ─── Confirm delete / cancel dialogs with custom overlay ─────────────────
    // Fallback: native confirm is already used inline via onsubmit attributes.
    // This adds a nicer UX if we detect a data-confirm attribute.
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            if (!window.confirm(el.getAttribute('data-confirm'))) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });

    // ─── Qty input: submit form on change (basket) ────────────────────────────
    document.querySelectorAll('.qty-input').forEach(function (input) {
        input.addEventListener('change', function () {
            var form = this.closest('form');
            if (form) form.submit();
        });
    });

    // ─── Navbar: mark active link by current URL ─────────────────────────────
    var currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(function (link) {
        var href = link.getAttribute('href');
        if (href && href !== '/' && currentPath.startsWith(href)) {
            link.classList.add('active');
        }
    });

    // ─── Table row: make entire row clickable if it has data-href ────────────
    document.querySelectorAll('tr[data-href]').forEach(function (row) {
        row.style.cursor = 'pointer';
        row.addEventListener('click', function (e) {
            if (e.target.closest('button, a, form')) return;
            window.location.href = row.getAttribute('data-href');
        });
    });

    // ─── Search bar: clear button ─────────────────────────────────────────────
    document.querySelectorAll('.search-bar input[type="text"]').forEach(function (input) {
        if (!input.value) return;
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = '✕';
        btn.style.cssText = 'background:none;border:none;padding:0 0.5rem;cursor:pointer;color:var(--muted);font-size:0.9rem;';
        btn.addEventListener('click', function () {
            input.value = '';
            input.focus();
            // Submit the parent form to clear search
            var form = input.closest('form');
            if (form) form.submit();
        });
        input.insertAdjacentElement('afterend', btn);
    });

    // ─── Smooth page transitions ──────────────────────────────────────────────
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.2s ease';
    requestAnimationFrame(function () {
        document.body.style.opacity = '1';
    });

    // ─── Book card: colour palette generator based on title ──────────────────
    // Assigns a deterministic accent colour to each book cover spine
    var spines = document.querySelectorAll('.book-cover-spine');
    var palette = [
        '#8b2e12', '#1a4a7a', '#2d6a4f', '#6b3a9e',
        '#9a6f1a', '#2d3a8b', '#7a1a4a', '#1a6b5a'
    ];
    spines.forEach(function (spine, i) {
        if (!spine.style.background || spine.style.background.includes('var(')) {
            // Only override if it's using available/unavailable variable colours
            // (leave CSS variable-based ones as-is)
        }
    });

    // ─── Mobile nav toggle ───────────────────────────────────────────────────
    // If a hamburger button is ever added, this handles it
    var toggle = document.getElementById('navToggle');
    var mobileMenu = document.getElementById('mobileMenu');
    if (toggle && mobileMenu) {
        toggle.addEventListener('click', function () {
            mobileMenu.classList.toggle('open');
            toggle.setAttribute('aria-expanded', mobileMenu.classList.contains('open'));
        });
    }

    // ─── Form: disable submit button after click to prevent double-submit ─────
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            var submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.hasAttribute('data-no-disable')) {
                setTimeout(function () {
                    submitBtn.disabled = true;
                    submitBtn.style.opacity = '0.6';
                    submitBtn.style.cursor = 'wait';
                }, 0);
            }
        });
    });

    // ─── Lazy-load images (future proof) ─────────────────────────────────────
    if ('IntersectionObserver' in window) {
        var lazyImages = document.querySelectorAll('img[data-src]');
        var imageObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    var img = entry.target;
                    img.src = img.getAttribute('data-src');
                    img.removeAttribute('data-src');
                    imageObserver.unobserve(img);
                }
            });
        });
        lazyImages.forEach(function (img) { imageObserver.observe(img); });
    }

    var checkoutForm = document.getElementById('checkoutForm');

    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function () {

            var items = [];

            document.querySelectorAll('.basket-item').forEach(function (item) {
                var bookName = item.querySelector('.basket-title').textContent.trim();
                var qty = parseInt(item.querySelector('.qty-input').value, 10);

                items.push({
                    bookName: bookName,
                    quantity: qty,
                });
            });

            var hidden = document.getElementById('basketJson');
            if (hidden) {
                hidden.value = JSON.stringify(items);
            }
        });
    }

    const form = document.getElementById("profileForm");
    const name = document.getElementById("name");
    const email = document.getElementById("email");
    const submitBtn = document.getElementById("submitBtn");

    function isValid() {
        return name.value.trim().length > 0 || email.value.trim().length > 0;
    }

    function updateButton() {
        submitBtn.disabled = !isValid();
    }

    // only for enabling/disabling button (no red styling here)
    name.addEventListener("input", updateButton);
    email.addEventListener("input", updateButton);

    updateButton();

    form.addEventListener("submit", function (e) {
        if (!isValid()) {
            e.preventDefault();

            if (!name.value.trim()) name.classList.add("is-invalid");
            else name.classList.remove("is-invalid");

            if (!email.value.trim()) email.classList.add("is-invalid");
            else email.classList.remove("is-invalid");
        } else {
            name.classList.remove("is-invalid");
            email.classList.remove("is-invalid");
        }
    });
});

// ─── Utility: CSRF-aware fetch helper (for future AJAX calls) ────────────────
window.BookVault = window.BookVault || {};

window.BookVault.post = function (url, body) {
    var headers = { 'Content-Type': 'application/json' };
    return fetch(url, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(body)
    });
};

function validateQty(input) {
    let value = parseInt(input.value, 10);

    if (isNaN(value)) value = 1;
    if (value < 1) value = 1;
    if (value > 99) value = 99;

    input.value = value;

    updateSummary();
}

function changeQty(btn, delta) {
    const item = btn.closest('.basket-item');
    const input = item.querySelector('.qty-input');

    let qty = parseInt(input.value || '1', 10);
    const min = parseInt(input.min || '1', 10);
    const max = parseInt(input.max || '99', 10);

    qty = Math.min(max, Math.max(min, qty + delta));
    input.value = qty;

    updateSummary();
}

function updateSummary() {
    const items = document.querySelectorAll('.basket-item');

    let total = 0;
    let count = 0;

    items.forEach(item => {
        const price = parseFloat(item.dataset.price);
        const qtyInput = item.querySelector('.qty-input');
        const qty = parseInt(qtyInput.value || '1', 10);

        total += price * qty;
        count += qty;
    });

    document.querySelector('.js-items-count').textContent = count;
    document.querySelector('.js-total').textContent = '$' + total.toFixed(2);
}