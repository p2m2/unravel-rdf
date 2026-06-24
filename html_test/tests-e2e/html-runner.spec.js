const { test, expect } = require('@playwright/test');

test('manual HTML tests should all pass', async ({ page }) => {
  page.on('console', msg => {
    console.log(`[browser:${msg.type()}] ${msg.text()}`);
  });

  page.on('pageerror', err => {
    console.error('[pageerror]', err);
  });

  await page.goto('/html_test/run.html', { waitUntil: 'load' });

  await page.waitForFunction(() => {
    return document.body?.getAttribute('data-tests-finished') === 'true';
  }, { timeout: 120000 });

  const failed = await page.locator('body').getAttribute('data-tests-failed');
  const summary = await page.locator('#summary').textContent();

  console.log('Summary:', summary);
  expect(failed).toBe('0');
});