import { test, expect, type Page } from '@playwright/test';

/**
 * E2E 主流程：注册 → 登录 → 创建文章 → 列表可见 → 详情 → 删除。
 * 数据通过随机后缀隔离，不依赖种子数据。
 */

function uniqueName(prefix: string): string {
  return `${prefix}_${Date.now().toString(36)}${Math.floor(Math.random() * 1000)}`;
}

async function registerAndLogin(page: Page, username: string, password: string) {
  // 注册
  await page.goto('/register');
  await page.getByPlaceholder(/用户名/).fill(username);
  await page.getByPlaceholder('邮箱').fill(`${username}@example.com`);
  await page.getByPlaceholder(/密码（至少 8 位）/).fill(password);
  await page.getByPlaceholder('确认密码').fill(password);
  // 注意：AntD 会在双字按钮文本中插入空格，用正则匹配
  await page.getByRole('button', { name: /注\s*册/ }).click();
  // 注册成功后跳转登录页
  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByText('注册成功')).toBeVisible();

  // 登录
  await page.getByPlaceholder('用户名').fill(username);
  await page.getByPlaceholder('密码').fill(password);
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByText(/欢迎回来/)).toBeVisible();
}

test.describe('完整用户旅程', () => {
  test('注册→登录→创建文章→查看→删除', async ({ page }) => {
    const username = uniqueName('e2e');
    const password = 'e2e-passw0rd';
    const title = `E2E 测试文章 ${Date.now()}`;

    // 1. 注册并登录（用户名出现在导航栏）
    await registerAndLogin(page, username, password);
    await expect(page.getByRole('banner').getByText(username)).toBeVisible();

    // 2. 创建文章（直接导航，避免 antd Menu 拦截点击）
    await page.goto('/articles');
    await page.getByRole('link', { name: '+ 写文章' }).click();
    await page.getByLabel('标题').fill(title);
    await page.getByLabel('正文').fill('这是一篇由 Playwright 端到端测试创建的文章内容。');
    await page.getByRole('switch').click(); // 发布
    await page.getByRole('button', { name: /创\s*建\s*文\s*章/ }).click();
    await expect(page.getByText('文章已创建')).toBeVisible();

    // 3. 列表可见（在包含标题的列表项内断言"已发布"标签）
    await expect(page.getByText(title)).toBeVisible();
    const listItem = page.locator('li.ant-list-item').filter({ hasText: title });
    await expect(listItem.getByText('已发布')).toBeVisible();

    // 4. 进入详情（点击列表项内的"查看详情"链接）
    await listItem.getByRole('link', { name: '查看详情' }).click();
    await expect(page.getByText('这是一篇由 Playwright 端到端测试创建的文章内容。')).toBeVisible();

    // 5. 删除（作者可见删除按钮；Popconfirm 确认按钮同样用正则）
    await page.getByRole('button', { name: /删\s*除/ }).click();
    await page
      .getByRole('button', { name: /删\s*除/ })
      .nth(1)
      .click();
    await expect(page.getByText('文章已删除')).toBeVisible();
    await expect(page).toHaveURL(/\/articles$/);
  });

  test('未登录访问写文章页面被重定向到登录', async ({ page }) => {
    await page.goto('/articles/new');
    await expect(page).toHaveURL(/\/login/);
  });
});
