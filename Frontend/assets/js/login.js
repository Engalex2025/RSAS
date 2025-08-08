// assets/js/login.js
// Handles login form submission and stores JWT.

const LOGIN_ENDPOINT = "/api/auth/login"; 

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");
  const resultBox = document.getElementById("loginResult");

  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    resultBox.textContent = "Logging in...";

    const username = document.getElementById("username")?.value?.trim();
    const password = document.getElementById("password")?.value?.trim();

    if (!username || !password) {
      resultBox.textContent = "Please fill in username and password.";
      return;
    }

    try {
      const resp = await fetch(LOGIN_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
      });

      if (!resp.ok) {
        const msg = await resp.text();
        resultBox.textContent = msg || "Invalid credentials.";
        return;
      }

      // Ex.: { token: "..." } ou { jwt: "..." }
      const data = await resp.json();
      const token = data.token || data.jwt || null;

      if (!token) {
        resultBox.textContent = "Login succeeded, but token is missing.";
        return;
      }

      saveToken(token);
      resultBox.textContent = "Login successful. Redirecting...";
      setTimeout(() => { window.location.href = "home.html"; }, 700);
    } catch (err) {
      console.error(err);
      resultBox.textContent = "Network error. Try again.";
    }
  });
});
