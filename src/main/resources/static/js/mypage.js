const keywordInput = document.getElementById("keywordInput");
const addKeywordBtn = document.getElementById("addKeywordBtn");
const keywordList = document.getElementById("keywordList");

const getToken = () => localStorage.getItem("token");

const logout = () => {
  localStorage.removeItem("token");
  alert("로그아웃 되었습니다.");
  window.location.href = "/";
};

const fetchKeywords = async () => {
  try {
    const token = getToken();
    const res = await fetch("/api/keywords", {
      headers: { Authorization: `Bearer ${token}` },
    });

    if (res.status === 401) {
      logout();
      return;
    }

    const keywords = await res.json();
    renderKeywords(keywords);
  } catch (err) {
    console.error(err);
    alert("서버에 연결할 수 없습니다.");
  }
};

const renderKeywords = (keywords) => {
  keywordList.innerHTML = "";
  keywords.forEach((kw) => {
    const li = document.createElement("li");
    const label = document.createElement("span");
    label.textContent = kw;
    const remove = document.createElement("button");
    remove.textContent = "삭제";
    remove.addEventListener("click", () => deleteKeyword(kw));
    li.append(label, remove);
    keywordList.appendChild(li);
  });
};

const addKeyword = async () => {
  const keyword = keywordInput.value.trim();
  if (!keyword) {
    alert("키워드를 입력해주세요.");
    return;
  }

  try {
    const token = getToken();
    const res = await fetch("/api/keywords", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ keyword }),
    });

    if (res.status === 401) {
      logout();
      return;
    }

    if (!res.ok) {
      alert((await res.json()).message || "키워드를 저장하지 못했습니다.");
      return;
    }
    keywordInput.value = "";
    fetchKeywords();
  } catch (err) {
    console.error(err);
    alert("서버에 연결할 수 없습니다.");
  }
};

const deleteKeyword = async (kw) => {
  try {
    const token = getToken();
    const res = await fetch(`/api/keywords/${encodeURIComponent(kw)}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${token}` },
    });

    if (res.status === 401) {
      logout();
      return;
    }

    fetchKeywords();
  } catch (err) {
    console.error(err);
    alert("키워드 삭제 실패");
  }
};

document.getElementById("updatePwBtn").addEventListener("click", async () => {
  const currentPw = document.getElementById("currentPw").value;
  const newPw = document.getElementById("newPw").value;
  const confirmNewPw = document.getElementById("confirmNewPw").value;
  const msg = document.getElementById("accountMsg");

  if (!currentPw || !newPw || !confirmNewPw) {
    msg.textContent = "모든 비밀번호 입력란을 채워주세요.";
    return;
  }

  if (newPw !== confirmNewPw) {
    msg.textContent = "새 비밀번호가 일치하지 않습니다.";
    return;
  }

  try {
    const token = getToken();
    const res = await fetch("/auth/password", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ currentPassword: currentPw, newPassword: newPw }),
    });

    if (res.ok) {
      alert("비밀번호가 성공적으로 변경되었습니다.");
      document.getElementById("currentPw").value = "";
      document.getElementById("newPw").value = "";
      document.getElementById("confirmNewPw").value = "";
      msg.textContent = "";
    } else {
      const errorText = (await res.json().catch(() => ({}))).message;
      msg.textContent = errorText || "비밀번호 변경 실패";
    }
  } catch (err) {
    console.error(err);
    msg.textContent = "요청 중 오류가 발생했습니다.";
  }
});

document
  .getElementById("deleteAccountBtn")
  .addEventListener("click", async () => {
    if (!confirm("정말로 회원 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다."))
      return;

    try {
      const token = getToken();
      const res = await fetch("/auth/delete", {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        alert("회원 탈퇴가 완료되었습니다.");
        logout();
      } else {
        const errorText = (await res.json().catch(() => ({}))).message;
        document.getElementById("accountMsg").textContent =
          errorText || "회원 탈퇴 실패";
      }
    } catch (err) {
      console.error(err);
      document.getElementById("accountMsg").textContent =
        "요청 중 오류가 발생했습니다.";
    }
  });

window.onload = () => {
  const token = getToken();
  if (!token) {
    alert("로그인이 필요합니다.");
    window.location.href = "login";
    return;
  }

  document.body.style.display = "block";

  //엔터 눌렀을 때 등록되게 변경
  keywordInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      addKeyword();
    }
  });

  fetchKeywords();
  document.getElementById("logoutBtn").addEventListener("click", logout);
  document
    .getElementById("addKeywordBtn")
    .addEventListener("click", addKeyword);
};
