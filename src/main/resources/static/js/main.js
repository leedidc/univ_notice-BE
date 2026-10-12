let currentPage = 0;
const pageSize = 10;
let totalPages = 1; //전체 페이지 수

window.onload = () => {
  const token = localStorage.getItem("token");
  if (token) {
    const logoutBtn = document.createElement("button");
    logoutBtn.textContent = "로그아웃";
    logoutBtn.onclick = () => {
      localStorage.removeItem("token");
      alert("로그아웃 되었습니다.");
      location.href = "/";
    };

    const settingBar = document.querySelector(".setting-bar");
    if (settingBar) {
      // 기존 로그인/회원가입 링크 숨기기
      const loginLink = settingBar.querySelector('a[href="/login"]');
      const signupLink = settingBar.querySelector('a[href="/signup"]');
      if (loginLink) loginLink.style.display = "none";
      if (signupLink) signupLink.style.display = "none";

      // 로그아웃 버튼 추가
      settingBar.appendChild(logoutBtn);
    }
  }

  loadDepartments(); // 학과 목록 로드
  setupEventListeners();
  loadNotices();
};

function loadDepartments() {
  const typeSelect = document.getElementById("typeSelect");
  if (!typeSelect) return;

  fetch("/notice/departments")
    .then((res) => res.json())
    .then((departments) => {
      departments.forEach((dep) => {
        const option = document.createElement("option");
        option.value = dep;
        option.textContent = dep;
        typeSelect.appendChild(option);
      });
    })
    .catch((err) => console.error("학과 불러오기 실패:", err));
}

function setupEventListeners() {
  document.getElementById("searchBtn").addEventListener("click", () => {
    currentPage = 0;
    loadNotices();
  });
  document.getElementById("prevPage").addEventListener("click", () => {
    if (currentPage > 0) {
      currentPage--;
      loadNotices();
    }
  });
  document.getElementById("nextPage").addEventListener("click", () => {
    if (currentPage + 1 < totalPages) {
      currentPage++;
      loadNotices();
    }
  });
}

function renderPagination() {
  const container = document.getElementById("pageNumbers");
  container.innerHTML = "";

  const startPage = Math.floor(currentPage / 10) * 10;
  const endPage = Math.min(startPage + 10, totalPages);

  for (let i = startPage; i < endPage; i++) {
    const btn = document.createElement("button");
    btn.innerText = i + 1;
    if (i === currentPage) btn.classList.add("active");

    btn.addEventListener("click", () => {
      currentPage = i;
      loadNotices();
    });

    container.appendChild(btn);
  }
}

function loadNotices() {
  const keyword = document.getElementById("searchInput")?.value.trim() || "";
  const department = document.getElementById("typeSelect")?.value || "";
  const noticeContainer = document.getElementById("notice-list");
  noticeContainer.innerHTML = "";

  let url = `/notice/search?page=${currentPage}&size=${pageSize}`;
  if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
  if (department) url += `&department=${encodeURIComponent(department)}`;

  fetch(url)
    .then((res) => res.json())
    .then((data) => {
      noticeContainer.innerHTML = ""; // 초기화
      totalPages = data.totalPages;
      renderPagination();
      document.getElementById("prevPage").disabled = currentPage === 0;
      document.getElementById("nextPage").disabled =
        currentPage + 1 >= totalPages;
      if (data.content.length === 0) {
        noticeContainer.innerHTML = "<p>검색 결과가 없습니다.</p>";
        return;
      }
      data.content.forEach((notice) => {
        const card = document.createElement("div");
        card.className = "notice-card";
        const heading = document.createElement("h3");
        const title = document.createElement("a");
        title.textContent = notice.title;
        try {
          const link = new URL(notice.link);
          if (["http:", "https:"].includes(link.protocol)) {
            title.href = link.href;
            title.target = "_blank";
            title.rel = "noopener noreferrer";
          }
        } catch {
          /* Show the title without an invalid link. */
        }
        heading.appendChild(title);
        const source = document.createElement("p");
        source.textContent = `출처: ${notice.department ?? ""}`;
        card.append(heading, source);
        noticeContainer.appendChild(card);
      });

      totalPages = data.totalPages;
      renderPagination(); // 페이지네이션 버튼 다시 그리기
    })
    .catch((err) => {
      console.error("공지 불러오기 실패:", err);
    });
}
