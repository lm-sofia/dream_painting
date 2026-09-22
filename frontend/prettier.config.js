/**
 * Prettier 配置
 * 运行：npm run format（prettier --write .）
 * 说明：改用 .js 文件而非 .json，是为了能写注释而不被编辑器标红
 */
export default {
  // 语句末尾加分号（与 TS 默认风格一致）
  semi: true,
  // 字符串用单引号（JS/TS 惯例，比双引号少按一个 shift）
  singleQuote: true,
  // 每行最大 100 字符，超长自动换行（兼顾宽屏与可读性）
  printWidth: 100,
  // 缩进 2 个空格（前端社区主流，不混用 tab）
  tabWidth: 2,
  // 多行最后一项也加逗号（git diff 友好：增删项只动一行）
  trailingComma: 'all',
  // 箭头函数参数总是加括号：(x) => x 而非 x => x（一致性 + 便于加类型）
  arrowParens: 'always',
  // 行结束符统一 LF（跨平台一致，避免 Windows 误产生 CRLF 导致 git 报错）
  endOfLine: 'lf',
  // 对象花括号内加空格：{ foo } 而非 {foo}
  bracketSpacing: true,
  // JSX 属性用双引号：<div className="x">（与 HTML 默认一致，区别于 JS 单引号）
  jsxSingleQuote: false,
  // 多行 JSX 元素的 > 单独换行（长组件更易读，属性多时不挤在一行）
  bracketSameLine: false,
  // inline 元素空白按 CSS display 判断（antd Space/Tag 等 inline 组件不受多余空格影响）
  htmlWhitespaceSensitivity: 'css',
  // 对象属性引号：仅在属性名非合法标识符时加（name 不加，'data-id' 加）
  quoteProps: 'as-needed',
  // 模板字符串内嵌的 CSS/HTML 自动格式化（内联 style、styled-components 受益）
  embeddedLanguageFormatting: 'auto',
  // 不要求文件顶部有 /** @format */ 标记才格式化（确保 --write 覆盖所有文件）
  requirePragma: false,
  // 格式化后不在文件顶部插入 /** @format */（避免污染源码）
  insertPragma: false,
  plugins: [],
};
