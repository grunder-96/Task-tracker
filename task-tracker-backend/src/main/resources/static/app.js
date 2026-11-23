const $ = id => document.getElementById(id);
let token = null, user = null, tasks = [], authMode = 'login';
const timers = {};

const bearer = h => (h || '').replace(/^Bearer\s+/i, '');
const show = (el, msg) => { el.textContent = msg || ''; el.hidden = !msg; };

async function errorText(res) {
  try { const j = await res.json(); if (j.message) return j.message; } catch {}
  return { 400: 'Проверьте введённые данные', 401: 'Неверный email или пароль', 409: 'Такой email уже занят' }[res.status] || 'Ошибка сервера, попробуйте позже';
}

let refreshing = null;
function refresh() {
  refreshing ??= fetch('/auth/refresh', { method: 'POST' })
    .then(res => {
      const t = bearer(res.headers.get('Authorization'));
      if (!res.ok || !t) throw new Error('refresh failed');
      token = t;
    })
    .finally(() => { refreshing = null; });
  return refreshing;
}

async function api(path, opts = {}, retry = true) {
  const headers = { ...(token && { Authorization: 'Bearer ' + token }), ...(opts.body && { 'Content-Type': 'application/json' }) };
  const res = await fetch(path, { ...opts, headers });
  if (res.status === 401 && retry) {
    try { await refresh(); return api(path, opts, false); } catch { setUser(null); }
  }
  if (!res.ok) throw new Error(await errorText(res));
  return res.status === 204 ? null : res.json();
}

async function authenticate(path, body) {
  const res = await fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
  if (!res.ok) throw new Error(await errorText(res));
  token = bearer(res.headers.get('Authorization'));
  if (!token) throw new Error('Сервер не вернул токен доступа');
  setUser(await api('/user'));
}

function setUser(u) {
  user = u;
  $('guest-bar').hidden = !!u; $('user-bar').hidden = !u;
  $('welcome').hidden = !!u; $('content').hidden = !u;
  $('user-email').textContent = u ? u.email : '';
  if (u) loadTasks(); else { token = null; tasks = []; render(); }
}

async function loadTasks() {
  try { tasks = await api('/tasks'); show($('page-error')); } catch (e) { show($('page-error'), e.message); }
  render();
}

function render() {
  for (const [id, done] of [['todo', false], ['done', true]]) {
    const ul = $(id); ul.replaceChildren();
    for (const t of tasks.filter(t => t.done === done)) {
      const li = document.createElement('li');
      li.textContent = t.title;
      li.onclick = () => openTask(t);
      ul.append(li);
    }
  }
}

// --- auth dialog
document.querySelectorAll('[data-auth]').forEach(b => b.onclick = () => {
  authMode = b.dataset.auth;
  const reg = authMode === 'register';
  $('auth-title').textContent = reg ? 'Регистрация' : 'Вход';
  $('password').placeholder = reg ? 'Пароль (от 8 символов)' : 'Пароль';
  $('password').autocomplete = reg ? 'new-password' : 'current-password';
  $('password2').hidden = !reg; $('password2').required = reg;
  $('auth-form').reset(); show($('auth-error'));
  $('auth-dialog').showModal();
});
$('auth-form').onsubmit = async e => {
  e.preventDefault();
  const reg = authMode === 'register';
  if (reg && $('password').value !== $('password2').value) return show($('auth-error'), 'Пароли не совпадают');
  try {
    await authenticate(reg ? '/user' : '/auth/login', { email: $('email').value, password: $('password').value });
    $('auth-dialog').close();
  } catch (err) { show($('auth-error'), err.message); }
};
$('logout').onclick = async () => { await fetch('/auth/logout', { method: 'POST' }).catch(() => {}); setUser(null); };

// --- tasks
$('add-form').onsubmit = async e => {
  e.preventDefault();
  try {
    tasks.push(await api('/tasks', { method: 'POST', body: JSON.stringify({ title: $('new-title').value, description: $('new-desc').value }) }));
    $('new-title').value = ''; $('new-desc').value = ''; show($('page-error')); render();
  } catch (err) { show($('page-error'), err.message); }
};

let currentId = null;
function openTask(t) {
  currentId = t.id;
  $('t-title').value = t.title; $('t-desc').value = t.description; $('t-done').checked = t.done;
  show($('task-error')); $('task-dialog').showModal();
}

async function save(id, patch) {
  try {
    const updated = await api('/tasks/' + id, { method: 'PATCH', body: JSON.stringify(patch) });
    tasks = tasks.map(t => t.id === id ? updated : t);
    show($('task-error')); render();
  } catch (err) { show($('task-error'), err.message); }
}

function autosave(input, field) {
  input.oninput = () => {
    const id = currentId;
    clearTimeout(timers[field]);
    if (field === 'title' && !input.value.trim()) return show($('task-error'), 'Заголовок не может быть пустым');
    show($('task-error'));
    timers[field] = setTimeout(() => save(id, { [field]: input.value }), 600);
  };
}
autosave($('t-title'), 'title');
autosave($('t-desc'), 'description');
$('t-done').onchange = () => save(currentId, { done: $('t-done').checked });
$('t-delete').onclick = async () => {
  try {
    await api('/tasks/' + currentId, { method: 'DELETE' });
    tasks = tasks.filter(t => t.id !== currentId);
    $('task-dialog').close(); render();
  } catch (err) { show($('task-error'), err.message); }
};

document.querySelectorAll('[data-close]').forEach(b => b.onclick = () => b.closest('dialog').close());

// --- start: restore the session from the refresh cookie
refresh().then(() => api('/user')).then(setUser).catch(() => setUser(null));
