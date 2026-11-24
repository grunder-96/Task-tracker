const $ = id => document.getElementById(id);
let token = null, user = null, tasks = [], authMode = 'login';

function toast(msg) {
  const el = document.createElement('div');
  el.className = 'toast'; el.textContent = msg;
  $('toasts').append(el);
  setTimeout(() => el.remove(), 2600);
}

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
  $('loading').hidden = false;
  try { tasks = await api('/tasks'); show($('page-error')); } catch (e) { show($('page-error'), e.message); }
  $('loading').hidden = true;
  render();
}

function render() {
  for (const [id, done] of [['todo', false], ['done', true]]) {
    const ul = $(id), list = tasks.filter(t => t.done === done);
    ul.replaceChildren();
    $(id + '-count').textContent = list.length;
    $(id + '-empty').hidden = list.length > 0;
    for (const t of list) {
      const li = document.createElement('li');
      const box = document.createElement('input');
      box.type = 'checkbox'; box.checked = t.done; box.setAttribute('aria-label', 'Сделано');
      box.onclick = e => e.stopPropagation();
      box.onchange = () => toggle(t.id, box.checked);
      const text = document.createElement('div'); text.className = 'text';
      const title = document.createElement('span'); title.className = 'title'; title.textContent = t.title;
      text.append(title);
      const first = (t.description || '').split('\n')[0].trim();
      if (first) { const d = document.createElement('span'); d.className = 'desc'; d.textContent = first; text.append(d); }
      const del = document.createElement('button');
      del.className = 'del'; del.textContent = '×'; del.title = 'Удалить'; del.setAttribute('aria-label', 'Удалить задачу');
      del.onclick = e => {
        e.stopPropagation();
        if (del.classList.contains('armed')) return removeTask(t.id);
        del.classList.add('armed'); del.textContent = 'Удалить?';
        setTimeout(() => { del.classList.remove('armed'); del.textContent = '×'; }, 3000);
      };
      li.append(box, text, del);
      li.onclick = () => openTask(t);
      ul.append(li);
    }
  }
}

async function removeTask(id) {
  try {
    await api('/tasks/' + id, { method: 'DELETE' });
    tasks = tasks.filter(t => t.id !== id); show($('page-error')); toast('Задача удалена');
  } catch (err) { show($('page-error'), err.message); }
  render();
}

async function toggle(id, done) {
  try {
    const updated = await api('/tasks/' + id, { method: 'PATCH', body: JSON.stringify({ done }) });
    tasks = tasks.map(t => t.id === id ? updated : t); show($('page-error'));
  } catch (err) { show($('page-error'), err.message); }
  render();
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
    $('new-title').value = ''; $('new-desc').value = ''; show($('page-error')); render(); toast('Задача добавлена');
  } catch (err) { show($('page-error'), err.message); }
};

let currentId = null, original = null;
function openTask(t) {
  currentId = t.id; original = t;
  $('t-title').value = t.title; $('t-desc').value = t.description; $('t-done').checked = t.done;
  updateSaveState(); $('task-dialog').showModal();
}

function changes() {
  const patch = {};
  const title = $('t-title').value.trim();
  if (title !== original.title) patch.title = title;
  if ($('t-desc').value !== original.description) patch.description = $('t-desc').value;
  if ($('t-done').checked !== original.done) patch.done = $('t-done').checked;
  return patch;
}

function updateSaveState() {
  const blank = !$('t-title').value.trim();
  show($('task-error'), blank ? 'Заголовок не может быть пустым' : '');
  $('t-save').disabled = blank || Object.keys(changes()).length === 0;
}
['t-title', 't-desc'].forEach(id => $(id).oninput = updateSaveState);
$('t-done').onchange = updateSaveState;

$('t-save').onclick = async () => {
  try {
    const updated = await api('/tasks/' + currentId, { method: 'PATCH', body: JSON.stringify(changes()) });
    tasks = tasks.map(t => t.id === currentId ? updated : t);
    $('task-dialog').close(); render(); toast('Изменения сохранены');
  } catch (err) { show($('task-error'), err.message); }
};
$('t-delete').onclick = async () => {
  try {
    await api('/tasks/' + currentId, { method: 'DELETE' });
    tasks = tasks.filter(t => t.id !== currentId);
    $('task-dialog').close(); render(); toast('Задача удалена');
  } catch (err) { show($('task-error'), err.message); }
};

const isDirty = () => Object.keys(changes()).length > 0;
function requestCloseTask() { if (isDirty()) $('confirm-dialog').showModal(); else $('task-dialog').close(); }
$('task-dialog').addEventListener('cancel', e => { if (isDirty()) { e.preventDefault(); $('confirm-dialog').showModal(); } });
$('confirm-stay').onclick = () => $('confirm-dialog').close();
$('confirm-discard').onclick = () => { $('confirm-dialog').close(); $('task-dialog').close(); };

document.querySelectorAll('[data-close]').forEach(b => b.onclick = () => {
  const d = b.closest('dialog');
  if (d.id === 'task-dialog') requestCloseTask(); else d.close();
});

// --- start: restore the session from the refresh cookie
refresh().then(() => api('/user')).then(setUser).catch(() => setUser(null));
