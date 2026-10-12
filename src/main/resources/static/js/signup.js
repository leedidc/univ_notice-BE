const form = document.getElementById("signupForm");
const errorMsg = document.getElementById("errorMsg");

form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const id = document.getElementById("id").value.trim();
  const email = document.getElementById("email").value.trim();
  const pw = document.getElementById("password").value;
  const pw2 = document.getElementById("confirmPassword").value;
  const school = document.getElementById("school").value.trim();
  const department = document.getElementById("department").value.trim();

  if (pw !== pw2) {
    errorMsg.textContent = "비밀번호가 일치하지 않습니다.";
    return;
  }

  try {
    const res = await fetch("/auth/signup", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, email, password: pw, school, department }),
    });

    if (res.ok) {
      alert("회원가입 성공! 로그인 페이지로 이동합니다.");
      location.href = "login";
    } else {
      const errorText = (await res.json().catch(() => ({}))).message;
      errorMsg.textContent = errorText || "회원가입 실패";
    }
  } catch (err) {
    console.error(err);
    errorMsg.textContent = "요청 중 오류가 발생했습니다.";
  }
});
