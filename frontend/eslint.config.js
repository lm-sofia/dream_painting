import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import prettier from 'eslint-config-prettier';

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'coverage'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2022,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      // 代码质量：禁止 any、未使用变量
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],
      // 实用规则（全栈经验踩坑最多的）
      'no-debugger': 'error', // 禁止 debugger 语句提交到仓库
      'no-console': ['warn', { allow: ['warn', 'error'] }], // 禁止 console.log，保留 warn/error
      'prefer-const': 'warn', // 从不重新赋值的变量必须用 const（而不是 let）
      '@typescript-eslint/no-empty-function': ['warn', { allow: ['arrowFunctions'] }], // 禁止空函数体
      // 与 Prettier 冲突的规则全部关闭（eslint-config-prettier）
      ...prettier.rules,
    },
  },
);
