/** @type {import('@playwright/test').PlaywrightTestConfig} */
module.exports = {
  testDir: './tests-e2e',
  timeout: 120000,
  use: {
    baseURL: 'http://127.0.0.1:8080',
    headless: true
  }
};