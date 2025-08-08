// assets/js/auth.js
// Simple JWT helpers for pages that require authentication.

const TOKEN_KEY = "jwtToken";

function saveToken(token) {
  try { localStorage.setItem(TOKEN_KEY, token); } catch (_) {}
}

function getToken() {
  try { return localStorage.getItem(TOKEN_KEY); } catch (_) { return null; }
}

function clearToken() {
  try { localStorage.removeItem(TOKEN_KEY); } catch (_) {}
}

function isAuthenticated() {
  const t = getToken();
  return Boolean(t && t.length > 10);
}

function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = "login.html";
  }
}

function redirectIfAuthenticated() {
  if (isAuthenticated()) {
    window.location.href = "home.html";
  }
}

function authHeaders(extra = {}) {
  const t = getToken();
  return {
    "Content-Type": "application/json",
    ...(t ? { "Authorization": "Bearer " + t } : {}),
    ...extra
  };
}
