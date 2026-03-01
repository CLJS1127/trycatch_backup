(() => {
    const state = {
        programId: null,
        program: null,
        canApply: false,
        hasApplied: false,
        isLoggedIn: false,
        memberPhone: "",
        memberEmail: "",
    };

    const selectors = {
        modalOverlay: "#applyModalOverlay",
        modalCloseButton: "#modalCloseBtn",
        modalSubmitButton: "#applyModal .dev-btn-apply",
        topCompanyName: ".styles_mb_space8__dk46ts1p h2",
        topTitle: "main h1",
        modalTitle: ".apply-job-info .job-title",
        modalDescription: ".apply-job-info .job-description",
        modalPhoneInput: "#modal-phone",
        modalEmailInput: "#modal-email",
        detailContent: "#detail-content",
    };

    function getProgramId() {
        const pathMatch = window.location.pathname.match(
            /\/experience\/(?:training-program|program)\/(\d+)/,
        );
        if (pathMatch) {
            return Number(pathMatch[1]);
        }

        const queryId = new URLSearchParams(window.location.search).get("id");
        return queryId ? Number(queryId) : null;
    }

    function goToLogin() {
        const reUrl = `${window.location.pathname}${window.location.search}`;
        window.location.href = `/main/log-in?re_url=${encodeURIComponent(reUrl)}`;
    }

    function normalizeText(value) {
        return (value || "").replace(/\s+/g, " ").trim();
    }

    function stripHtml(html) {
        if (!html) {
            return "";
        }
        return html.replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim();
    }

    function formatDeadline(deadline) {
        if (!deadline) {
            return "상시채용";
        }

        const date = new Date(deadline);
        if (Number.isNaN(date.getTime())) {
            return deadline;
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `~${year}.${month}.${day} 마감`;
    }

    function setText(selector, text) {
        const element = document.querySelector(selector);
        if (!element) {
            return;
        }
        element.textContent = text;
    }

    function setInputValue(selector, value) {
        const element = document.querySelector(selector);
        if (!element) {
            return;
        }
        element.value = value || "";
    }

    function setInfoItemValue(labelText, value) {
        if (!value) {
            return;
        }

        const targetItem = Array.from(
            document.querySelectorAll('[data-sentry-component="JobInfoItem"]'),
        ).find((item) => {
            const label = item.querySelector('span[data-accent-color="gray700"]');
            return label && normalizeText(label.textContent) === labelText;
        });

        if (!targetItem) {
            return;
        }

        const valueNode = Array.from(targetItem.querySelectorAll("span")).find(
            (node) => {
                const accent = node.getAttribute("data-accent-color");
                return accent && accent !== "gray700";
            },
        );

        if (valueNode) {
            valueNode.textContent = value;
        }
    }

    function getPrimaryApplyButtons() {
        const applyButtons = Array.from(
            document.querySelectorAll('[data-sentry-component="ApplyButton"]'),
        );
        return applyButtons;
    }

    function setButtonLabel(button, label) {
        if (!button) {
            return;
        }

        const spans = button.querySelectorAll("span");
        const target = spans.length > 0 ? spans[spans.length - 1] : button;
        target.textContent = label;
    }

    function setButtonDisabled(button, disabled) {
        if (!button) {
            return;
        }

        button.disabled = disabled;
        button.setAttribute("data-disabled", String(disabled));
        button.style.cursor = disabled ? "not-allowed" : "pointer";
        button.style.opacity = disabled ? "0.65" : "1";
    }

    function openApplyModal() {
        const overlay = document.querySelector(selectors.modalOverlay);
        if (!overlay) {
            return;
        }
        overlay.classList.add("active");
        document.body.style.overflow = "hidden";
    }

    function closeApplyModal() {
        const overlay = document.querySelector(selectors.modalOverlay);
        if (!overlay) {
            return;
        }
        overlay.classList.remove("active");
        document.body.style.overflow = "";
    }

    function handleApplyEntryClick(event) {
        event.preventDefault();

        if (!state.isLoggedIn) {
            goToLogin();
            return;
        }

        if (!state.canApply) {
            alert("개인회원만 체험 프로그램에 지원할 수 있습니다.");
            return;
        }

        if (state.hasApplied) {
            alert("이미 지원한 프로그램입니다.");
            return;
        }

        openApplyModal();
    }

    function bindApplyButtons() {
        const applyButtons = getPrimaryApplyButtons();
        applyButtons.forEach((button) => {
            button.addEventListener("click", handleApplyEntryClick);
        });
    }

    function updateApplyButtons() {
        const primaryButtons = getPrimaryApplyButtons();
        const modalSubmitButton = document.querySelector(selectors.modalSubmitButton);

        const allButtons = [...primaryButtons];
        if (modalSubmitButton) {
            allButtons.push(modalSubmitButton);
        }

        if (state.hasApplied) {
            allButtons.forEach((button) => {
                setButtonLabel(button, "지원 완료");
                setButtonDisabled(button, true);
            });
            return;
        }

        if (!state.isLoggedIn) {
            primaryButtons.forEach((button) => {
                setButtonLabel(button, "로그인 후 지원");
                setButtonDisabled(button, false);
            });
            if (modalSubmitButton) {
                setButtonLabel(modalSubmitButton, "로그인");
                setButtonDisabled(modalSubmitButton, false);
            }
            return;
        }

        if (!state.canApply) {
            allButtons.forEach((button) => {
                setButtonLabel(button, "개인회원 전용");
                setButtonDisabled(button, true);
            });
            return;
        }

        primaryButtons.forEach((button) => {
            setButtonLabel(button, "즉시 지원");
            setButtonDisabled(button, false);
        });

        if (modalSubmitButton) {
            setButtonLabel(modalSubmitButton, "지원하기");
            setButtonDisabled(modalSubmitButton, false);
        }
    }

    async function submitApply() {
        if (!state.programId) {
            return;
        }

        if (!state.isLoggedIn) {
            goToLogin();
            return;
        }

        if (!state.canApply) {
            alert("개인회원만 체험 프로그램에 지원할 수 있습니다.");
            return;
        }

        if (state.hasApplied) {
            alert("이미 지원한 프로그램입니다.");
            return;
        }

        const response = await fetch(
            `/api/apply?experienceProgramId=${state.programId}`,
            {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                },
            },
        );

        const result = await response.json();

        if (!result.success) {
            if (result.message && result.message.includes("로그인")) {
                goToLogin();
                return;
            }
            alert(result.message || "지원 처리 중 오류가 발생했습니다.");
            return;
        }

        state.hasApplied = true;
        updateApplyButtons();
        closeApplyModal();
        alert("지원이 완료되었습니다.");
    }

    function bindModalEvents() {
        const modalSubmitButton = document.querySelector(selectors.modalSubmitButton);
        const modalCloseButton = document.querySelector(selectors.modalCloseButton);
        const modalOverlay = document.querySelector(selectors.modalOverlay);

        if (modalSubmitButton) {
            modalSubmitButton.addEventListener("click", async (event) => {
                event.preventDefault();
                await submitApply();
            });
        }

        if (modalCloseButton) {
            modalCloseButton.addEventListener("click", (event) => {
                event.preventDefault();
                closeApplyModal();
            });
        }

        if (modalOverlay) {
            modalOverlay.addEventListener("click", (event) => {
                if (event.target === modalOverlay) {
                    closeApplyModal();
                }
            });
        }

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeApplyModal();
            }
        });
    }

    function updateProgramData(program) {
        if (!program) {
            return;
        }

        const title = normalizeText(program.experienceProgramTitle) || "체험 공고";
        const company = normalizeText(program.corpCompanyName) || "기업명 미입력";
        const job = normalizeText(program.experienceProgramJob) || "직무 미입력";
        const deadline = formatDeadline(program.experienceProgramDeadline);
        const descriptionHtml = program.experienceProgramDescription || "";

        setText(selectors.topTitle, title);
        setText(selectors.topCompanyName, company);
        setText(selectors.modalTitle, title);
        setText(selectors.modalDescription, job);

        const detailContent = document.querySelector(selectors.detailContent);
        if (detailContent && descriptionHtml) {
            detailContent.innerHTML = descriptionHtml;
        }

        setInfoItemValue("직무", job);
        setInfoItemValue("마감일", deadline);
        if (program.experienceProgramWorkHours) {
            setInfoItemValue("근무시간", normalizeText(program.experienceProgramWorkHours));
        }

        document.title = `${title} | TRY-CATCH`;

        const fallbackText = stripHtml(descriptionHtml);
        if (!fallbackText && detailContent) {
            detailContent.textContent = "상세 설명이 등록되지 않았습니다.";
        }
    }

    async function fetchDetailData(programId) {
        const response = await fetch(`/experience/program/${programId}/detail-data`, {
            headers: {
                "X-Requested-With": "XMLHttpRequest",
            },
        });

        if (!response.ok) {
            throw new Error("상세 정보를 불러오지 못했습니다.");
        }

        return response.json();
    }

    async function init() {
        state.programId = getProgramId();
        if (!state.programId) {
            return;
        }

        bindApplyButtons();
        bindModalEvents();

        try {
            const data = await fetchDetailData(state.programId);
            state.program = data.program || null;
            state.isLoggedIn = Boolean(data.isLoggedIn);
            state.canApply = Boolean(data.canApply);
            state.hasApplied = Boolean(data.hasApplied);
            state.memberPhone = data.memberPhone || "";
            state.memberEmail = data.memberEmail || "";

            updateProgramData(state.program);
            setInputValue(selectors.modalPhoneInput, state.memberPhone);
            setInputValue(selectors.modalEmailInput, state.memberEmail);
            updateApplyButtons();
        } catch (error) {
            console.error(error);
            alert("체험 공고 상세 정보를 불러오지 못했습니다.");
        }
    }

    document.addEventListener("DOMContentLoaded", init);
})();
