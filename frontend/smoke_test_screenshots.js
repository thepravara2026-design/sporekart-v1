import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';

const BASE_URL = process.env.BASE_URL || 'http://localhost:5173';
const OUTPUT_DIR = process.env.OUTPUT_DIR || path.join(process.cwd(), 'screenshots');

if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

const PRESET_USERS = {
  admin: {
    user: {
      id: 'usr-admin-01',
      name: 'System Admin',
      email: 'admin@sporekart.com',
      role: 'ROLE_ADMIN',
      roles: ['ROLE_ADMIN'],
    },
    token: 'mock-jwt-admin-token',
  },
  grower: {
    user: {
      id: 'usr-grower-01',
      name: 'Preetham Bio Farms',
      email: 'grower@sporekart.com',
      role: 'ROLE_GROWER',
      roles: ['ROLE_GROWER', 'ROLE_SELLER'],
    },
    token: 'mock-jwt-grower-token',
  },
  trainee: {
    user: {
      id: 'usr-trainee-01',
      name: 'Ramesh Trainee',
      email: 'trainee@sporekart.com',
      role: 'ROLE_TRAINEE',
      roles: ['ROLE_TRAINEE'],
    },
    token: 'mock-jwt-trainee-token',
  },
  customer: {
    user: {
      id: 'usr-customer-01',
      name: 'Mushroom Cultivator',
      email: 'customer@sporekart.com',
      role: 'ROLE_CUSTOMER',
      roles: ['ROLE_CUSTOMER'],
    },
    token: 'mock-jwt-customer-token',
  },
  dual: {
    user: {
      id: 'usr-dual-01',
      name: 'Grower & Trainee Combined',
      email: 'grower.trainee@sporekart.com',
      role: 'ROLE_GROWER',
      roles: ['ROLE_GROWER', 'ROLE_TRAINEE', 'ROLE_SELLER'],
    },
    token: 'mock-jwt-dual-token',
  },
};

const TEST_SCENARIOS = [
  // 1. Anonymous / Public Storefront
  {
    roleName: 'Anonymous Guest',
    presetKey: null,
    routes: [
      { name: '01_guest_homepage', url: '/' },
      { name: '02_guest_product_catalog', url: '/products' },
      { name: '03_guest_product_detail', url: '/products/prd-spore-01' },
      { name: '04_guest_categories', url: '/categories' },
      { name: '05_guest_login_page', url: '/login' },
      { name: '06_guest_health_check', url: '/health' },
      { name: '07_guest_design_system_showcase', url: '/design-system-showcase' },
    ],
  },
  // 2. Customer Role
  {
    roleName: 'Customer',
    presetKey: 'customer',
    routes: [
      { name: '08_customer_catalog', url: '/products' },
      { name: '09_customer_cart', url: '/cart' },
      { name: '10_customer_checkout', url: '/checkout' },
      { name: '11_customer_orders', url: '/orders' },
    ],
  },
  // 3. Grower Role
  {
    roleName: 'Grower',
    presetKey: 'grower',
    routes: [
      { name: '12_grower_dashboard', url: '/grower' },
      { name: '13_grower_profile', url: '/grower/profile' },
      { name: '14_grower_products', url: '/grower/products' },
      { name: '15_grower_inventory', url: '/grower/inventory' },
      { name: '16_grower_orders', url: '/grower/orders' },
      { name: '17_grower_shipments', url: '/grower/shipments' },
      { name: '18_grower_reports', url: '/grower/reports' },
      { name: '19_grower_settings', url: '/grower/settings' },
    ],
  },
  // 4. Trainee Role
  {
    roleName: 'Trainee',
    presetKey: 'trainee',
    routes: [
      { name: '20_trainee_portal_console', url: '/training' },
    ],
  },
  // 5. Seller Role
  {
    roleName: 'Seller',
    presetKey: 'grower',
    routes: [
      { name: '21_seller_dashboard', url: '/seller' },
      { name: '22_seller_products', url: '/seller/products' },
      { name: '23_seller_inventory', url: '/seller/inventory' },
      { name: '24_seller_orders', url: '/seller/orders' },
    ],
  },
  // 6. Master Admin Role
  {
    roleName: 'Master Admin',
    presetKey: 'admin',
    routes: [
      { name: '25_admin_dashboard', url: '/admin' },
      { name: '26_admin_returns', url: '/admin/returns' },
      { name: '27_admin_training_program', url: '/admin/training' },
      { name: '28_admin_training_batches', url: '/admin/training/batches' },
      { name: '29_admin_training_operations', url: '/admin/training/operations' },
      { name: '30_admin_training_reports', url: '/admin/training/reports' },
      { name: '31_admin_notifications', url: '/admin/notifications' },
    ],
  },
  // 7. Dual Role (Grower + Trainee)
  {
    roleName: 'Dual Role (Grower + Trainee)',
    presetKey: 'dual',
    routes: [
      { name: '32_dual_grower_view', url: '/grower' },
      { name: '33_dual_training_view', url: '/training' },
    ],
  },
];

async function runSmokeTest() {
  console.log('🚀 Starting SPOREKART v3.0 Comprehensive Smoke Test...');
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });
  const page = await context.newPage();

  const results = [];

  for (const scenario of TEST_SCENARIOS) {
    console.log(`\n--- Testing Role: ${scenario.roleName} ---`);

    // Clear context storage first
    await context.clearCookies();

    // Set auth state via page navigation and evaluate localStorage
    if (scenario.presetKey && PRESET_USERS[scenario.presetKey]) {
      const presetData = PRESET_USERS[scenario.presetKey];
      await page.goto(`${BASE_URL}/login`);
      await page.evaluate(({ token, user }) => {
        localStorage.setItem('accessToken', token);
        localStorage.setItem('sporekart_user', JSON.stringify(user));
      }, presetData);
    } else {
      await page.goto(`${BASE_URL}/login`);
      await page.evaluate(() => {
        localStorage.clear();
      });
    }

    for (const route of scenario.routes) {
      const fullUrl = `${BASE_URL}${route.url}`;
      console.log(`Navigating to ${route.name} (${route.url})...`);

      const consoleErrors = [];
      const pageErrorListener = err => consoleErrors.push(err.message);
      page.on('pageerror', pageErrorListener);

      try {
        const response = await page.goto(fullUrl, { waitUntil: 'networkidle', timeout: 15000 });
        await page.waitForTimeout(1000);

        const status = response ? response.status() : 'N/A';
        const screenshotPath = path.join(OUTPUT_DIR, `${route.name}.png`);

        await page.screenshot({ path: screenshotPath, fullPage: false });
        console.log(`  ✅ Screenshot saved: ${route.name}.png (HTTP ${status})`);

        results.push({
          role: scenario.roleName,
          name: route.name,
          url: route.url,
          status,
          errors: consoleErrors.length > 0 ? consoleErrors : null,
          screenshot: `${route.name}.png`,
          passed: status === 200 && consoleErrors.length === 0,
        });
      } catch (err) {
        console.error(`  ❌ Failed loading ${route.url}: ${err.message}`);
        results.push({
          role: scenario.roleName,
          name: route.name,
          url: route.url,
          status: 'ERROR',
          errors: [err.message],
          screenshot: null,
          passed: false,
        });
      } finally {
        page.removeListener('pageerror', pageErrorListener);
      }
    }
  }

  await browser.close();

  const reportPath = path.join(OUTPUT_DIR, 'smoke_test_report.json');
  fs.writeFileSync(reportPath, JSON.stringify(results, null, 2));
  console.log(`\n🎉 Smoke Test Complete! Report saved to ${reportPath}`);
}

runSmokeTest().catch(err => {
  console.error('Fatal Smoke Test Error:', err);
  process.exit(1);
});
