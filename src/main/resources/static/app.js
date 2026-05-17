const PIECE = { EMPTY: 0, WHITE_MAN: 1, WHITE_KING: 2, BLACK_MAN: 3, BLACK_KING: 4 };

const ERROR_MESSAGES = {
  MANDATORY_CAPTURE:
    "Обязательное взятие! Нужно взять фигуру соперника. Другие шашки сейчас ходить не могут.",
  WRONG_TURN: "Сейчас ход другого цвета.",
  NOT_YOUR_PIECE: "Это не ваша шашка.",
  INVALID_MOVE: "Такой ход невозможен. Шашки ходят только по диагонали.",
  PIECE_CANNOT_MOVE:
    "Эта шашка не может так ходить. Выберите подсвеченную фигуру или нажмите «Подсказки».",
  GAME_FINISHED: "Партия уже завершена.",
  UNAUTHORIZED: "Войдите в аккаунт",
};

const state = {
  user: null,
  gameId: null,
  board: null,
  currentTurn: "WHITE",
  winner: null,
  mode: "vsAI",
  selected: null,
  hints: [],
  movableFrom: new Set(),
};

const $ = (id) => document.getElementById(id);

function hideToast() {
  $("toast").hidden = true;
  clearTimeout(showToast._t);
}

function showToast(message, type = "error") {
  const toast = $("toast");
  const icon = $("toastIcon");
  icon.textContent = type === "error" ? "⚠️" : type === "info" ? "ℹ️" : "✅";
  $("toastMsg").textContent = message;
  toast.className = `toast toast-${type}`;
  toast.hidden = false;
  clearTimeout(showToast._t);
  showToast._t = setTimeout(hideToast, type === "error" ? 6000 : 4000);
}

function parseError(err, fallback) {
  if (err.code && ERROR_MESSAGES[err.code]) return ERROR_MESSAGES[err.code];
  if (err.message && !err.message.startsWith("{")) return err.message;
  return fallback || "Произошла ошибка";
}

async function api(path, options = {}) {
  const res = await fetch(path, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });
  let body = null;
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) {
    body = await res.json();
  } else if (!res.ok) {
    body = { message: await res.text() };
  }
  if (res.status === 401) {
    location.href = "/login.html";
    throw Object.assign(new Error("UNAUTHORIZED"), { code: "UNAUTHORIZED" });
  }
  if (!res.ok) {
    const e = new Error(body?.message || res.statusText);
    e.code = body?.code;
    throw e;
  }
  return body;
}

function isDark(x, y) {
  return (x + y) % 2 === 1;
}

function key(x, y) {
  return `${x},${y}`;
}

function rebuildMovableFrom() {
  state.movableFrom.clear();
  state.hints.forEach((h) => state.movableFrom.add(key(h.fromX, h.fromY)));
}

function renderBoard() {
  const boardEl = $("board");
  boardEl.innerHTML = "";
  if (!state.board) return;

  for (let y = 0; y < 8; y++) {
    for (let x = 0; x < 8; x++) {
      const cell = document.createElement("div");
      cell.className = `cell ${isDark(x, y) ? "dark" : "light"}`;
      cell.dataset.x = x;
      cell.dataset.y = y;

      if (isDark(x, y)) {
        const v = state.board[x][y];
        if (v !== PIECE.EMPTY) {
          const piece = document.createElement("div");
          const white = v === PIECE.WHITE_MAN || v === PIECE.WHITE_KING;
          piece.className = `piece ${white ? "white" : "black"} ${
            v === PIECE.WHITE_KING || v === PIECE.BLACK_KING ? "king" : ""
          }`;
          cell.appendChild(piece);
        }
        if (state.selected?.x === x && state.selected?.y === y) {
          cell.classList.add("selected");
        }
        if (state.movableFrom.has(key(x, y))) {
          cell.classList.add("can-move");
        }
        if (
          state.hints.some((h) => (h.toX === x && h.toY === y) || (h.fromX === x && h.fromY === y))
        ) {
          cell.classList.add("hint");
        }
        if (!state.winner) {
          cell.addEventListener("click", () => onCellClick(x, y));
        }
      }
      boardEl.appendChild(cell);
    }
  }
}

function setStatus(text) {
  $("status").textContent = text;
}

function updateStats() {
  if (!state.user) return;
  $("playerName").textContent = state.user.username;
  $("statWins").textContent = state.user.wins;
  $("statLosses").textContent = state.user.losses;
  $("statDraws").textContent = state.user.draws;
}

async function loadUser() {
  state.user = await api("/api/auth/me");
  updateStats();
}

async function loadHistory() {
  try {
    const list = await api("/api/history");
    const ul = $("historyList");
    ul.innerHTML = "";
    if (!list.length) {
      ul.innerHTML = "<li>Пока нет завершённых партий</li>";
      return;
    }
    list.forEach((h) => {
      const li = document.createElement("li");
      const date = h.playedAt ? new Date(h.playedAt).toLocaleString("ru") : "";
      const resultRu =
        h.result === "win" ? "Победа" : h.result === "lose" ? "Поражение" : "Ничья";
      li.textContent = `${date} · ${h.opponentType === "ai" ? "ИИ" : "2 игрока"} · ${resultRu}`;
      ul.appendChild(li);
    });
  } catch (e) {
    $("historyList").innerHTML = `<li>${parseError(e)}</li>`;
  }
}

async function refreshHints() {
  if (!state.gameId || state.winner) {
    state.hints = [];
    rebuildMovableFrom();
    return;
  }
  try {
    state.hints = await api(`/api/game/${state.gameId}/hints`);
    rebuildMovableFrom();
  } catch {
    state.hints = [];
    rebuildMovableFrom();
  }
}

async function newGame() {
  state.mode = $("mode").value;
  const data = await api("/api/game/new", {
    method: "POST",
    body: JSON.stringify({ mode: state.mode, aiDifficulty: $("difficulty").value }),
  });
  state.gameId = data.gameId;
  state.board = data.board;
  state.currentTurn = data.currentTurn;
  state.winner = null;
  state.selected = null;
  await refreshHints();
  setStatus(`Новая игра · ход: ${state.currentTurn === "WHITE" ? "белых" : "чёрных"}`);
  renderBoard();
  showToast("Игра началась!", "success");
}

async function refreshState() {
  const data = await api(`/api/game/${state.gameId}/state`);
  state.board = data.board;
  state.currentTurn = data.currentTurn;
  state.winner = data.winner;
  await refreshHints();
  if (state.winner) {
    const humanWon =
      state.mode === "vsAI"
        ? state.winner === "WHITE"
        : state.winner === "WHITE";
    let msg;
    let toastType;
    if (state.mode === "vsAI") {
      if (humanWon) {
        msg = "Победа! Счёт побед обновлён.";
        toastType = "success";
      } else {
        msg = "Поражение. Счёт поражений обновлён.";
        toastType = "error";
      }
    } else {
      msg = state.winner === "WHITE" ? "Победили белые" : "Победили чёрные";
      toastType = "info";
    }
    setStatus(msg);
    showToast(msg, toastType);
    await loadUser();
    await loadHistory();
  } else {
    setStatus(`Ход: ${state.currentTurn === "WHITE" ? "белых" : "чёрных"}`);
  }
  renderBoard();
}

async function applyMove(fromX, fromY, toX, toY) {
  await api(`/api/game/${state.gameId}/move`, {
    method: "POST",
    body: JSON.stringify({ fromX, fromY, toX, toY }),
  });
  state.selected = null;
  await refreshState();

  if (state.mode === "vsAI" && !state.winner && state.currentTurn === "BLACK") {
    setStatus("Ход ИИ…");
    await api(`/api/game/${state.gameId}/ai-move`, { method: "POST" });
    await refreshState();
  }
}

function canSelectPiece(x, y) {
  const piece = state.board[x][y];
  if (piece === PIECE.EMPTY) return false;
  const isWhite = piece === PIECE.WHITE_MAN || piece === PIECE.WHITE_KING;
  const isBlack = piece === PIECE.BLACK_MAN || piece === PIECE.BLACK_KING;
  const myTurn =
    (state.currentTurn === "WHITE" && isWhite) || (state.currentTurn === "BLACK" && isBlack);
  if (!myTurn) return false;
  if (state.movableFrom.size > 0) {
    return state.movableFrom.has(key(x, y));
  }
  return true;
}

async function onCellClick(x, y) {
  if (!state.gameId || state.winner) return;

  if (state.selected) {
    const { x: fx, y: fy } = state.selected;
    if (fx === x && fy === y) {
      state.selected = null;
      renderBoard();
      return;
    }
    try {
      await applyMove(fx, fy, x, y);
    } catch (e) {
      showToast(parseError(e, "Недопустимый ход"), "error");
    }
    return;
  }

  if (canSelectPiece(x, y)) {
    state.selected = { x, y };
    renderBoard();
    return;
  }

  const piece = state.board[x][y];
  const isWhite = piece === PIECE.WHITE_MAN || piece === PIECE.WHITE_KING;
  const isBlack = piece === PIECE.BLACK_MAN || piece === PIECE.BLACK_KING;
  if (
    (state.currentTurn === "WHITE" && isBlack) ||
    (state.currentTurn === "BLACK" && isWhite)
  ) {
    showToast(
      `Сейчас ход ${state.currentTurn === "WHITE" ? "белых" : "чёрных"}. Эта шашка не может ходить.`,
      "error"
    );
  } else if (state.movableFrom.size > 0) {
    showToast(ERROR_MESSAGES.MANDATORY_CAPTURE, "error");
  }
}

async function showHints() {
  if (!state.gameId) {
    showToast("Сначала начните новую игру", "info");
    return;
  }
  await refreshHints();
  if (!state.hints.length) {
    showToast("Нет доступных ходов", "info");
  } else {
    const captures = state.hints.filter((h) => h.capture).length;
    showToast(
      captures
        ? `Подсказка: ${state.hints.length} ход(ов), есть обязательное взятие (${captures})`
        : `Подсказка: ${state.hints.length} возможных ход(ов)`,
      "info"
    );
  }
  renderBoard();
}

function initTheme() {
  const saved = localStorage.getItem("checkers-theme") || "light";
  document.documentElement.setAttribute("data-theme", saved === "dark" ? "dark" : "light");
}

$("themeBtn").addEventListener("click", () => {
  const isDark = document.documentElement.getAttribute("data-theme") === "dark";
  const next = isDark ? "light" : "dark";
  document.documentElement.setAttribute("data-theme", next);
  localStorage.setItem("checkers-theme", next);
});

$("toastClose").addEventListener("click", hideToast);

$("logoutBtn").addEventListener("click", async () => {
  await api("/api/auth/logout", { method: "POST" });
  location.href = "/login.html";
});

$("newGameBtn").addEventListener("click", () =>
  newGame().catch((e) => showToast(parseError(e), "error"))
);
$("hintsBtn").addEventListener("click", () => showHints().catch((e) => showToast(parseError(e), "error")));

$("mode").addEventListener("change", () => {
  $("difficultyWrap").style.display = $("mode").value === "vsAI" ? "flex" : "none";
});

initTheme();
$("difficultyWrap").style.display = "flex";

(async function init() {
  try {
    await loadUser();
    await loadHistory();
    setStatus('Нажмите «Новая игра»');
  } catch {
    location.href = "/login.html";
  }
})();
