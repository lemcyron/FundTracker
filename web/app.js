// State management
let expenses = [
  { id: '1', name: 'Apartment Rent', cost: 650.00, checked: false, category: 'Housing' },
  { id: '2', name: 'Groceries & Supplies', cost: 135.50, checked: true, category: 'Groceries' },
  { id: '3', name: 'Electric & Power Bill', cost: 82.25, checked: true, category: 'Utilities' },
  { id: '4', name: 'Water Utility Bill', cost: 24.50, checked: false, category: 'Utilities' },
  { id: '5', name: 'Internet & WiFi Plan', cost: 50.00, checked: true, category: 'Bills' },
  { id: '6', name: 'Fuel & Transit Pass', cost: 45.00, checked: false, category: 'Transport' }
];

let currencySymbol = '$';
let currentFilter = 'all';
let googleDriveUrl = '';

// DOM Elements
const expenseListEl = document.getElementById('expenseList');
const emptyStateEl = document.getElementById('emptyState');
const totalAllEl = document.getElementById('totalAllAmount');
const totalCheckedEl = document.getElementById('totalCheckedAmount');
const totalUncheckedEl = document.getElementById('totalUncheckedAmount');
const checkedCountBadgeEl = document.getElementById('checkedCountBadge');
const uncheckedCountBadgeEl = document.getElementById('uncheckedCountBadge');
const progressBadgeEl = document.getElementById('progressBadge');
const progressBarEl = document.getElementById('progressBar');

const countAllEl = document.getElementById('countAll');
const countCheckedEl = document.getElementById('countChecked');
const countUncheckedEl = document.getElementById('countUnchecked');

const addModal = document.getElementById('addModal');
const editModal = document.getElementById('editModal');
const settingsModal = document.getElementById('settingsModal');
const toastEl = document.getElementById('toast');

const inputDriveUrl = document.getElementById('inputDriveUrl');

// Init
document.addEventListener('DOMContentLoaded', () => {
  loadFromLocalStorage();
  setupEventListeners();
  render();
});

function loadFromLocalStorage() {
  const savedExpenses = localStorage.getItem('fund_tracker_items');
  const savedCurrency = localStorage.getItem('fund_tracker_currency');
  const savedDriveUrl = localStorage.getItem('fund_tracker_drive_url');

  if (savedExpenses) {
    try {
      expenses = JSON.parse(savedExpenses);
    } catch (e) {
      console.error(e);
    }
  }

  if (savedCurrency) {
    currencySymbol = savedCurrency;
    updateCurrencyUI();
  }

  if (savedDriveUrl) {
    googleDriveUrl = savedDriveUrl;
    if (inputDriveUrl) inputDriveUrl.value = savedDriveUrl;
  }
}

function saveToLocalStorage() {
  localStorage.setItem('fund_tracker_items', JSON.stringify(expenses));
  localStorage.setItem('fund_tracker_currency', currencySymbol);
  localStorage.setItem('fund_tracker_drive_url', googleDriveUrl);
}

function showToast(message) {
  toastEl.textContent = message;
  toastEl.classList.remove('hidden');
  setTimeout(() => {
    toastEl.classList.add('hidden');
  }, 2400);
}

function updateCurrencyUI() {
  document.querySelectorAll('.currency-symbol').forEach(el => el.textContent = currencySymbol);
  document.querySelectorAll('.chip').forEach(chip => {
    chip.classList.toggle('active', chip.dataset.cur === currencySymbol);
  });
}

function render() {
  // Compute totals
  let totalAll = 0;
  let totalChecked = 0;
  let totalUnchecked = 0;
  let checkedCount = 0;
  let uncheckedCount = 0;

  expenses.forEach(item => {
    const cost = Number(item.cost) || 0;
    totalAll += cost;
    if (item.checked) {
      totalChecked += cost;
      checkedCount++;
    } else {
      totalUnchecked += cost;
      uncheckedCount++;
    }
  });

  const totalCount = expenses.size || expenses.length;
  const percentDone = totalCount > 0 ? Math.round((checkedCount / totalCount) * 100) : 0;

  // Update Summary Bar
  totalAllEl.textContent = `${currencySymbol}${totalAll.toFixed(2)}`;
  totalCheckedEl.textContent = `${currencySymbol}${totalChecked.toFixed(2)}`;
  totalUncheckedEl.textContent = `${currencySymbol}${totalUnchecked.toFixed(2)}`;
  checkedCountBadgeEl.textContent = checkedCount;
  uncheckedCountBadgeEl.textContent = uncheckedCount;

  progressBadgeEl.textContent = `${percentDone}% Done`;
  progressBarEl.style.width = `${percentDone}%`;

  // Update filter counts
  countAllEl.textContent = totalCount;
  countCheckedEl.textContent = checkedCount;
  countUncheckedEl.textContent = uncheckedCount;

  // Filter items
  const filtered = expenses.filter(item => {
    if (currentFilter === 'checked') return item.checked;
    if (currentFilter === 'unchecked') return !item.checked;
    return true;
  });

  // Render items list
  expenseListEl.innerHTML = '';
  if (filtered.length === 0) {
    emptyStateEl.classList.remove('hidden');
  } else {
    emptyStateEl.classList.add('hidden');
    filtered.forEach(item => {
      const card = document.createElement('div');
      card.className = `expense-card ${item.checked ? 'checked' : ''}`;
      card.innerHTML = `
        <div class="checkbox-container" data-action="toggle" data-id="${item.id}">
          <div class="checkbox-custom">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="#FFFFFF"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
          </div>
        </div>
        <div class="expense-info" data-action="toggle" data-id="${item.id}">
          <div class="expense-name">${escapeHtml(item.name)}</div>
          ${item.category ? `<span class="expense-tag">${escapeHtml(item.category)}</span>` : ''}
        </div>
        <div class="expense-cost-actions">
          <div class="expense-cost">${currencySymbol}${Number(item.cost).toFixed(2)}</div>
          <div class="card-action-btns">
            <button class="mini-icon-btn edit-btn" title="Edit" data-id="${item.id}">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
            </button>
            <button class="mini-icon-btn delete-btn" title="Delete" data-id="${item.id}">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
          </div>
        </div>
      `;
      expenseListEl.appendChild(card);
    });
  }

  saveToLocalStorage();
}

function escapeHtml(text) {
  if (!text) return '';
  return String(text).replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[m]);
}

// Google Drive Link Parser
function normalizeGoogleDriveUrl(url) {
  const trimmed = url.trim();
  if (!trimmed) return '';

  const fileDMatch = trimmed.match(/https?:\/\/drive\.google\.com\/file\/d\/([a-zA-Z0-9_-]+)/);
  if (fileDMatch) {
    return `https://drive.google.com/uc?export=download&id=${fileDMatch[1]}`;
  }

  const openIdMatch = trimmed.match(/https?:\/\/drive\.google\.com\/.*[?&]id=([a-zA-Z0-9_-]+)/);
  if (openIdMatch) {
    return `https://drive.google.com/uc?export=download&id=${openIdMatch[1]}`;
  }

  if (!trimmed.startsWith('http') && /^[a-zA-Z0-9_-]{20,}$/.test(trimmed)) {
    return `https://drive.google.com/uc?export=download&id=${trimmed}`;
  }

  return trimmed;
}

async function syncGoogleDrive(url) {
  const downloadUrl = normalizeGoogleDriveUrl(url);
  if (!downloadUrl) {
    showToast('Please enter a Google Drive link or ID');
    return;
  }

  const syncBtnText = document.getElementById('syncBtnText');
  syncBtnText.textContent = 'Syncing...';

  try {
    const res = await fetch(downloadUrl);
    if (!res.ok) throw new Error(`HTTP Error: ${res.status}`);

    const text = await res.text();
    if (text.trim().startsWith('<!DOCTYPE') || text.trim().startsWith('<html')) {
      throw new Error('Google Drive returned a webpage. Set sharing to "Anyone with the link can view".');
    }

    const data = JSON.parse(text);
    if (Array.isArray(data)) {
      expenses = data;
    } else if (data && Array.isArray(data.items)) {
      expenses = data.items;
      if (data.currency) {
        currencySymbol = data.currency;
        updateCurrencyUI();
      }
    } else {
      throw new Error('Invalid JSON format');
    }

    googleDriveUrl = url;
    render();
    showToast(`Synced ${expenses.length} items from Google Drive!`);
    settingsModal.classList.add('hidden');
  } catch (err) {
    console.error(err);
    alert(`Google Drive Sync Failed:\n${err.message}`);
  } finally {
    syncBtnText.textContent = 'Sync From Google Drive Now';
  }
}

function setupEventListeners() {
  // Filter chips
  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentFilter = chip.dataset.filter;
      render();
    });
  });

  // List actions (delegate)
  expenseListEl.addEventListener('click', (e) => {
    const toggleTarget = e.target.closest('[data-action="toggle"]');
    if (toggleTarget) {
      const id = toggleTarget.dataset.id;
      const item = expenses.find(x => x.id === id);
      if (item) {
        item.checked = !item.checked;
        render();
      }
      return;
    }

    const editBtn = e.target.closest('.edit-btn');
    if (editBtn) {
      const id = editBtn.dataset.id;
      const item = expenses.find(x => x.id === id);
      if (item) {
        document.getElementById('editItemId').value = item.id;
        document.getElementById('editItemName').value = item.name;
        document.getElementById('editItemCost').value = item.cost;
        document.getElementById('editItemCategory').value = item.category || '';
        editModal.classList.remove('hidden');
      }
      return;
    }

    const deleteBtn = e.target.closest('.delete-btn');
    if (deleteBtn) {
      const id = deleteBtn.dataset.id;
      expenses = expenses.filter(x => x.id !== id);
      render();
      showToast('Item deleted');
      return;
    }
  });

  // Modal Open/Close
  document.getElementById('btnOpenAddModal').addEventListener('click', () => {
    document.getElementById('addExpenseForm').reset();
    addModal.classList.remove('hidden');
  });

  document.getElementById('btnCancelAdd').addEventListener('click', () => {
    addModal.classList.add('hidden');
  });

  document.getElementById('btnCancelEdit').addEventListener('click', () => {
    editModal.classList.add('hidden');
  });

  document.getElementById('btnOpenSettings').addEventListener('click', () => {
    settingsModal.classList.remove('hidden');
  });

  document.getElementById('btnCloseSettings').addEventListener('click', () => {
    settingsModal.classList.add('hidden');
  });

  // Quick sync from top bar
  document.getElementById('btnQuickSync').addEventListener('click', () => {
    if (googleDriveUrl) {
      syncGoogleDrive(googleDriveUrl);
    } else {
      settingsModal.classList.remove('hidden');
    }
  });

  // Add Item Submit
  document.getElementById('addExpenseForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const name = document.getElementById('inputItemName').value.trim();
    const cost = parseFloat(document.getElementById('inputItemCost').value);
    const category = document.getElementById('inputItemCategory').value.trim() || 'General';

    if (name && !isNaN(cost)) {
      expenses.unshift({
        id: 'item_' + Date.now(),
        name,
        cost,
        checked: false,
        category
      });
      addModal.classList.add('hidden');
      render();
      showToast(`Added '${name}'`);
    }
  });

  // Edit Item Submit
  document.getElementById('editExpenseForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const id = document.getElementById('editItemId').value;
    const name = document.getElementById('editItemName').value.trim();
    const cost = parseFloat(document.getElementById('editItemCost').value);
    const category = document.getElementById('editItemCategory').value.trim() || 'General';

    const item = expenses.find(x => x.id === id);
    if (item && name && !isNaN(cost)) {
      item.name = name;
      item.cost = cost;
      item.category = category;
      editModal.classList.add('hidden');
      render();
      showToast('Updated item');
    }
  });

  // Settings Actions: CHECK ALL / UNCHECK ALL
  document.getElementById('btnCheckAll').addEventListener('click', () => {
    expenses.forEach(item => item.checked = true);
    render();
    showToast('All items checked!');
  });

  document.getElementById('btnUncheckAll').addEventListener('click', () => {
    expenses.forEach(item => item.checked = false);
    render();
    showToast('All items unchecked!');
  });

  // Settings Actions: Sync Google Drive
  document.getElementById('btnSyncNow').addEventListener('click', () => {
    const url = inputDriveUrl.value.trim();
    syncGoogleDrive(url);
  });

  // Copy JSON Export
  document.getElementById('btnCopyJson').addEventListener('click', () => {
    const exportData = {
      title: 'FundTracker Expenses',
      currency: currencySymbol,
      lastUpdated: new Date().toISOString(),
      items: expenses
    };
    navigator.clipboard.writeText(JSON.stringify(exportData, null, 2))
      .then(() => showToast('Checklist JSON copied to clipboard!'))
      .catch(() => alert('Failed to copy to clipboard'));
  });

  // Currency chips selection
  document.querySelectorAll('#currencyChips .chip').forEach(chip => {
    chip.addEventListener('click', () => {
      currencySymbol = chip.dataset.cur;
      updateCurrencyUI();
      render();
      showToast(`Currency changed to ${currencySymbol}`);
    });
  });

  // Clear all
  document.getElementById('btnClearAll').addEventListener('click', () => {
    if (confirm('Are you sure you want to clear all items?')) {
      expenses = [];
      render();
      settingsModal.classList.add('hidden');
      showToast('All items cleared');
    }
  });
}
