module.exports = {
  // 继承 Conventional Commits 规范
  // 格式：type(scope): subject
  // type 可选值：feat|fix|docs|style|refactor|perf|test|chore|ci|build|revert
  // 示例：feat: 添加用户登录
  //      fix(auth): 修复 token 过期逻辑
  extends: ['@commitlint/config-conventional'],
};
