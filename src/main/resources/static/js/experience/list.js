(function () {
    const form = document.getElementById('filterForm');
    if (!form) {
        return;
    }

    const pageInput = document.getElementById('pageInput');
    const statusSelect = document.getElementById('statusSelect');
    const jobSelect = document.getElementById('jobSelect');
    const sortSelect = document.getElementById('sortSelect');
    const keywordInput = document.getElementById('keywordInput');
    const searchButton = document.getElementById('searchButton');
    const resetButton = document.getElementById('resetFilterButton');
    const pageLinks = document.querySelectorAll('.js-page-link');

    const submitWithPage = (page) => {
        if (!pageInput) {
            return;
        }
        pageInput.value = String(page);
        form.submit();
    };

    [statusSelect, jobSelect, sortSelect].forEach((element) => {
        if (!element) {
            return;
        }
        element.addEventListener('change', function () {
            submitWithPage(1);
        });
    });

    if (searchButton) {
        searchButton.addEventListener('click', function () {
            submitWithPage(1);
        });
    }

    if (keywordInput) {
        keywordInput.addEventListener('keydown', function (event) {
            if (event.key !== 'Enter') {
                return;
            }
            event.preventDefault();
            submitWithPage(1);
        });
    }

    if (resetButton) {
        resetButton.addEventListener('click', function () {
            if (statusSelect) {
                statusSelect.value = 'all';
            }
            if (jobSelect) {
                jobSelect.value = '';
            }
            if (sortSelect) {
                sortSelect.value = 'latest';
            }
            if (keywordInput) {
                keywordInput.value = '';
            }
            submitWithPage(1);
        });
    }

    pageLinks.forEach((link) => {
        link.addEventListener('click', function (event) {
            if (link.classList.contains('disabled')) {
                event.preventDefault();
                return;
            }

            const targetPage = Number(link.getAttribute('data-page'));
            if (!Number.isInteger(targetPage) || targetPage < 1) {
                return;
            }

            event.preventDefault();
            submitWithPage(targetPage);
        });
    });
})();
