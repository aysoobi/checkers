async function apiAuth(path, options = {}) {
  const res = await fetch(path, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });
  let body = null;
  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    body = await res.json();
  } else {
    const text = await res.text();
    body = text ? { message: text } : null;
  }
  if (!res.ok) {
    const err = new Error(body?.message || "Ошибка запроса");
    err.code = body?.code || "ERROR";
    throw err;
  }
  return body;
}

function showAuthError(el, err) {
  el.hidden = false;
  el.className = "auth-error";
  el.textContent = "⚠️ " + (err.message || "Ошибка");
}
