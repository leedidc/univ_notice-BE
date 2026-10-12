document.querySelector("#loginBtn").addEventListener("click", async () => {
  const id = document.querySelector("#id").value.trim();
  const password = document.querySelector("#pw").value;

  try {
    const res = await fetch("/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, password }),
    });

    if (res.ok) {
      const token = await res.text();
      localStorage.setItem("token", token);
      location.href = "mypage";
    } else {
      const data = await res.json();
      document.querySelector("#msg").textContent =
        data.message || "로그인 실패";
    }
  } catch (err) {
    console.error(err);
    alert("서버에 연결할 수 없습니다. 관리자에게 문의하세요.");
  }
});
