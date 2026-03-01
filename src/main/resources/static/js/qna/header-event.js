// 헤더 네비게이션에 마우스 올렸을 때 색 변경
const serviceNav = document.querySelectorAll(".dev-serviceNav a span");

serviceNav.forEach((link) => {
    link.addEventListener("mouseenter", () => {
        link.style.color = "hsl(154.36deg 100% 21.57%)";
    });

    link.addEventListener("mouseleave", () => {
        link.style.color = "#222222";
    });
});

const userMy = document.querySelector(".userNav-item.my.member");
if (userMy) {
    userMy.addEventListener("mouseenter", () => {
        userMy.classList.add("open");
    });
    userMy.addEventListener("mouseleave", () => {
        userMy.classList.remove("open");
    });
}
const headerFooterActive = document.querySelector(".notification-item");
const headerFooterDetailButton = document.querySelector(
    ".notificaiton-footer .buttons a",
);

// ✅ 수정: mouseenter, mouseleave
if (headerFooterDetailButton) {
    headerFooterDetailButton.addEventListener("mouseenter", (e) => {
        if (headerFooterActive) headerFooterActive.classList.add("active");
    });

    headerFooterDetailButton.addEventListener("mouseleave", (e) => {
        if (headerFooterActive) headerFooterActive.classList.remove("active");
    });
}

// 기업 서비스에 마우스 올렸을 때 class open 추가 삭제
const userCorp = document.querySelector(".userNav .corp");
if (userCorp) {
    userCorp.addEventListener("mouseenter", (e) => {
        userCorp.classList.add("open");
    });
    userCorp.addEventListener("mouseleave", (e) => {
        userCorp.classList.remove("open");
    });
}

// 기업 서비스 안에 기업 로그인에 마우스 올렸을 때 색 변경
const userCorpLogin = document.querySelector(".btnRowWrap a");
if (userCorpLogin) {
    userCorpLogin.addEventListener("mouseenter", (e) => {
        e.target.style.backgroundColor = "#F8F8F8";
    });
    userCorpLogin.addEventListener("mouseleave", (e) => {
        e.target.style.backgroundColor = "#fff";
    });
}

// 헤더 네비게이션 마우스 올렸을 때 open-active 클래스 추가 삭제
const headNav = document.getElementById("headNavBar");
const navMenu = document.querySelector(".lyNavArea");
const serviceNavDrop = document.querySelector(".dev-serviceNav"); // ul 선택
const backgroundColorChange = document.querySelector(".jkNavDimm");

serviceNavDrop.addEventListener("mouseenter", (e) => {
    headNav.classList.add("open-active");
    backgroundColorChange.classList.add("on");
});

navMenu.addEventListener("mouseleave", () => {
    headNav.classList.remove("open-active");
    backgroundColorChange.classList.remove("on");
});
navMenu.addEventListener("mouseenter", () => {
    headNav.classList.add("open-active");
    backgroundColorChange.classList.add("on");
});
serviceNavDrop.addEventListener("mouseleave", () => {
    headNav.classList.remove("open-active");
    backgroundColorChange.classList.remove("on");
});

// 로그아웃 버튼 배경색(로그인)
const logoutButton = document.querySelector(".btnRowWrap");
if (logoutButton) {
    logoutButton.addEventListener("mouseenter", (e) => {
        e.target.style.backgroundColor = "#F8F8F8";
    });
    logoutButton.addEventListener("mouseleave", (e) => {
        e.target.style.backgroundColor = "#fff";
    });
}

// 알림 자세히보기 버튼 색변화(로그인)
const notifyButton = document.getElementById("js-bell");
const notifyToggleAttached = document.querySelector(".popup-notification");
const notifyToggleOpen = document.querySelector(
    ".userNav-item.notification.devLiNotification",
);

// 페이지 로드 시 미읽음 알림이 있으면 빨간 점 표시
const alarmDot = document.getElementById("js-alarmDot");
if (alarmDot) {
    const hasUnread = document.querySelector('.notification-item[data-is-read="false"]');
    if (hasUnread) {
        alarmDot.style.display = "";
    }
}

// 헤더 알림 목록을 시간순(최신순)으로 정렬
const alarmList = document.querySelector(".list-notification");
if (alarmList) {
    const items = Array.from(alarmList.querySelectorAll(".notification-item[data-date]"));
    items.sort((a, b) => new Date(b.dataset.date) - new Date(a.dataset.date));
    items.forEach((item) => alarmList.appendChild(item));
}

if (notifyButton) {
    notifyButton.addEventListener("click", (e) => {
        if (notifyToggleAttached) notifyToggleAttached.classList.toggle("attached");
        if (alarmDot) {
            alarmDot.style.display = "none";
            fetch("/api/alarm/read", { method: "PUT" });
        }
    });
    notifyButton.addEventListener("mouseenter", (e) => {
        if (notifyToggleOpen) notifyToggleOpen.classList.add("open");
    });
    notifyButton.addEventListener("mouseleave", (e) => {
        if (notifyToggleOpen) notifyToggleOpen.classList.remove("open");
    });
}

// 기업회원 알림 벨 클릭 이벤트
const corpNotifyButton = document.getElementById("js-corpBell");
const corpNotifyLayer = document.getElementById("js-corpNotiLayer");
if (corpNotifyButton) {
    corpNotifyButton.addEventListener("click", (e) => {
        if (corpNotifyLayer) corpNotifyLayer.classList.toggle("attached");
        const corpAlarmDot = document.getElementById("js-corpAlarmDotQna");
        if (corpAlarmDot) {
            corpAlarmDot.style.display = "none";
            fetch("/api/corp-alarm/read", { method: "PUT" });
        }
    });
}

// 검색창 자동완성 관련 (로그인)
const searchToolDiv = document.querySelector(".autoSearch");
const searchInput = document.getElementById("stext");
const searchBox = document.querySelector(
    "#headerWrap #header .headInner .search",
);

if (searchInput) {
    // 검색창 클릭시 클래스 추가(로그인)
    searchInput.addEventListener("click", (e) => {
        if (searchToolDiv) searchToolDiv.classList.add("autoSearchDisabledClose");
    });

    // keyup 이벤트 - 자동완성 꺼진 상태면 무시(로그인)
    searchInput.addEventListener("keyup", (e) => {
        if (!searchToolDiv) return;
        if (searchToolDiv.classList.contains("autoSearchDisabled")) {
            return;
        }
        if (searchInput.value.length > 0) {
            searchToolDiv.classList.add("autoSearchShow");
            if (searchBox) searchBox.classList.add("searchOpen");
        } else {
            searchToolDiv.classList.remove("autoSearchShow");
            if (searchBox) searchBox.classList.remove("searchOpen");
        }
    });
}

// 자동완성 끄기 버튼
const autoCompleteOffBtn = document.getElementById("devAcOff");
if (autoCompleteOffBtn) {
    autoCompleteOffBtn.addEventListener("click", (e) => {
        const isConfirmed = confirm(
            "검색어 자동완성 기능을 중지합니다.\n사용을 원하실 경우 검색창 내 ▼버튼을 클릭하세요.",
        );
        if (isConfirmed && searchToolDiv) {
            searchToolDiv.classList.remove("autoSearchShow");
            searchToolDiv.classList.add("autoSearchDisabled");
            searchToolDiv.classList.add("autoSearchDisabledClose");
            if (searchBox) searchBox.classList.remove("searchOpen");
        }
    });
}

// 화살표 버튼 (창 열기)
const arrowOpenBtn = document.querySelector("#btnArrow_C button");
if (arrowOpenBtn) {
    arrowOpenBtn.addEventListener("click", (e) => {
        if (searchToolDiv) searchToolDiv.classList.remove("autoSearchDisabledClose");
        if (searchBox) searchBox.classList.add("searchOpen");
    });
}

// 화살표 버튼 (창 닫기)
const arrowCloseBtn = document.querySelector("#btnArrow_O button");
if (arrowCloseBtn) {
    arrowCloseBtn.addEventListener("click", (e) => {
        if (searchToolDiv) searchToolDiv.classList.add("autoSearchDisabledClose");
        if (searchBox) searchBox.classList.remove("searchOpen");
    });
}

// 자동완성 켜기 버튼
const autoCompleteOnBtn = document.getElementById("devAcOn");
if (autoCompleteOnBtn) {
    autoCompleteOnBtn.addEventListener("click", (e) => {
        if (searchToolDiv) {
            searchToolDiv.classList.remove("autoSearchDisabled", "autoSearchDisabledClose");
        }
        if (searchInput && searchInput.value.length > 0) {
            if (searchToolDiv) searchToolDiv.classList.add("autoSearchShow");
            if (searchBox) searchBox.classList.add("searchOpen");
        } else {
            if (searchBox) searchBox.classList.remove("searchOpen");
        }
    });
}

// 닫기 버튼 (로그인, 자동완성 켜진 상태에서)
const closeBtn = document.getElementById("devAcInfoClose2");
if (closeBtn) {
    closeBtn.addEventListener("click", (e) => {
        if (searchToolDiv) searchToolDiv.classList.remove("autoSearchShow");
        if (searchBox) searchBox.classList.remove("searchOpen");
    });
}

// 닫기 버튼 (자동완성 꺼진 상태에서)
const closeBtn2 = document.getElementById("devAcInfoClose");
if (closeBtn2) {
    closeBtn2.addEventListener("click", (e) => {
        if (searchToolDiv) searchToolDiv.classList.add("autoSearchDisabledClose");
        if (searchBox) searchBox.classList.remove("searchOpen");
    });
}

// 체험 정보/체험 공고 이동 경로 고정
(() => {
    const experienceListUrl = "/experience/list";
    const linkCandidates = new Set();

    const topExperience = document.querySelector("#onepick > a.linkItem");
    if (topExperience) {
        linkCandidates.add(topExperience);
    }

    document.querySelectorAll("a").forEach((anchor) => {
        const text = (anchor.textContent || "").trim();
        if (text === "체험 정보" || text === "체험 공고") {
            linkCandidates.add(anchor);
        }
    });

    linkCandidates.forEach((anchor) => {
        anchor.setAttribute("href", experienceListUrl);
    });
})();
