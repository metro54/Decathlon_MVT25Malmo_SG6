const el = (id) => document.getElementById(id);
const err = el('error');
const msg = el('msg');
const disciplineSelect = el('discipline');
const eventSelect = el('event');
const standingsHeader = el('standingsHeader');

let currentEvents = [];

function setError(text) { err.textContent = text; msg.textContent = ''; }
function setMsg(text) { msg.textContent = text; err.textContent = ''; }

async function loadEvents() {
  const discipline = disciplineSelect.value;
  try {
    const res = await fetch(`/api/events?discipline=${encodeURIComponent(discipline)}`);
    if (!res.ok) {
      setError(`Could not load events (status ${res.status})`);
      currentEvents = [];
    } else {
      currentEvents = await res.json();
    }
  } catch (e) {
    setError('Could not load events');
    currentEvents = [];
  }

  eventSelect.innerHTML = currentEvents
    .map(e => `<option value="${e.id}">${e.label} (${e.unit})</option>`)
    .join('');

  standingsHeader.innerHTML = '<th>Name</th>'
    + currentEvents.map(e => `<th>${e.label}</th>`).join('')
    + '<th>Total</th>';
}

disciplineSelect.addEventListener('change', async () => {
  await loadEvents();
  await renderStandings();
});

el('add').addEventListener('click', async () => {
  const name = el('name').value;
  try {
    const res = await fetch('/api/competitors', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name })
    });
    if (!res.ok) {
      const t = await res.text();
      setError(t || 'Failed to add competitor');
    } else {
      setMsg('Added');
      el('name').value = '';
    }
    await renderStandings();
  } catch (e) {
    setError('Network error');
  }
});

el('save').addEventListener('click', async () => {
  const body = {
    name: el('name2').value,
    event: eventSelect.value,
    raw: parseFloat(el('raw').value)
  };
  try {
    const res = await fetch('/api/score', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) {
      const t = await res.text();
      setError(t || 'Failed to save score');
    } else {
      const json = await res.json();
      setMsg(`Saved: ${json.points} pts`);
    }
    await renderStandings();
  } catch (e) {
    setError('Score failed');
  }
});

el('export').addEventListener('click', async () => {
  try {
    const res = await fetch('/api/export.csv');
    const text = await res.text();
    const blob = new Blob([text], { type: 'text/csv;charset=utf-8' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'results.csv';
    a.click();
  } catch (e) {
    setError('Export failed');
  }
});

async function renderStandings() {
  try {
    const res = await fetch('/api/standings');
    const data = await res.json();

    const rows = data
      .sort((a,b)=> (b.total||0)-(a.total||0))
      .map(r => `<tr>
        <td>${escapeHtml(r.name)}</td>
        ${currentEvents.map(e => `<td>${r.scores?.[e.id] ?? ''}</td>`).join('')}
        <td>${r.total ?? 0}</td>
      </tr>`).join('');

    el('standings').innerHTML = rows;
  } catch (e) {
    setError('Could not load standings');
  }
}

function escapeHtml(s){
  return String(s).replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
}

async function init() {
  await loadEvents();
  await renderStandings();
}

init();
