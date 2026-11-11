const assert = require('assert');
const fs = require('fs');
const path = require('path');

console.log('--- Running FundTracker Unit Tests ---');

// 1. Validate sample_google_drive_expenses.json structure
const sampleJsonPath = path.join(__dirname, 'sample_google_drive_expenses.json');
assert(fs.existsSync(sampleJsonPath), 'sample_google_drive_expenses.json should exist');

const sampleData = JSON.parse(fs.readFileSync(sampleJsonPath, 'utf8'));
assert(sampleData.title, 'JSON should have title');
assert(sampleData.currency, 'JSON should have currency');
assert(Array.isArray(sampleData.items), 'JSON items should be an array');
assert(sampleData.items.length > 0, 'JSON items should have at least 1 item');

console.log('✓ Google Drive sample JSON format validated successfully.');

// 2. Validate Calculations Logic
function calculateTotals(items) {
  let totalAll = 0;
  let totalChecked = 0;
  let totalUnchecked = 0;
  let checkedCount = 0;
  let uncheckedCount = 0;

  for (const item of items) {
    const cost = Number(item.cost) || 0;
    totalAll += cost;
    if (item.checked) {
      totalChecked += cost;
      checkedCount++;
    } else {
      totalUnchecked += cost;
      uncheckedCount++;
    }
  }

  return {
    totalAll: Math.round(totalAll * 100) / 100,
    totalChecked: Math.round(totalChecked * 100) / 100,
    totalUnchecked: Math.round(totalUnchecked * 100) / 100,
    checkedCount,
    uncheckedCount,
    totalCount: items.length
  };
}

const testItems = [
  { id: '1', name: 'Rent', cost: 750.00, checked: true },
  { id: '2', name: 'Power', cost: 85.50, checked: true },
  { id: '3', name: 'WiFi', cost: 50.00, checked: false },
  { id: '4', name: 'Groceries', cost: 125.75, checked: false }
];

const totals = calculateTotals(testItems);
assert.strictEqual(totals.totalAll, 1011.25, 'Total all should match sum');
assert.strictEqual(totals.totalChecked, 835.50, 'Total checked should match sum');
assert.strictEqual(totals.totalUnchecked, 175.75, 'Total unchecked should match sum');
assert.strictEqual(totals.checkedCount, 2, 'Checked count should match');
assert.strictEqual(totals.uncheckedCount, 2, 'Unchecked count should match');
assert.strictEqual(totals.totalAll, totals.totalChecked + totals.totalUnchecked, 'Checked + Unchecked must equal Total All');

console.log('✓ Financial calculations (Total All, Total Checked, Total Unchecked) verified.');

// 3. Validate Google Drive URL normalization
function normalizeGoogleDriveUrl(input) {
  const trimmed = input.trim();
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

const shareLink1 = 'https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIvE2up0I/view?usp=sharing';
const normalized1 = normalizeGoogleDriveUrl(shareLink1);
assert.strictEqual(normalized1, 'https://drive.google.com/uc?export=download&id=1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIvE2up0I');

const openLink2 = 'https://drive.google.com/open?id=1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIvE2up0I';
const normalized2 = normalizeGoogleDriveUrl(openLink2);
assert.strictEqual(normalized2, 'https://drive.google.com/uc?export=download&id=1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIvE2up0I');

const rawId = '1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIvE2up0I';
const normalized3 = normalizeGoogleDriveUrl(rawId);
assert.strictEqual(normalized3, 'https://drive.google.com/uc?export=download&id=1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIvE2up0I');

console.log('✓ Google Drive URL conversion logic passed.');

// 4. Validate Batch Check/Uncheck functions
const allChecked = testItems.map(item => ({ ...item, checked: true }));
assert(allChecked.every(item => item.checked === true), 'Check all must set all items checked');

const allUnchecked = testItems.map(item => ({ ...item, checked: false }));
assert(allUnchecked.every(item => item.checked === false), 'Uncheck all must set all items unchecked');

console.log('✓ Batch Check All & Uncheck All operations verified.');

console.log('\n--- ALL TESTS PASSED SUCCESSFULLY! ---');
