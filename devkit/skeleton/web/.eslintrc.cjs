/**
 * ESLint 配置 — 遵循阿里巴巴 f2e-spec 规范
 * @see https://github.com/alibaba/f2e-spec
 */
module.exports = {
  root: true,
  env: {
    browser: true,
    es2022: true,
    node: true,
  },
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint'],
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:vue/recommended',
    'eslint-config-prettier',
  ],
  rules: {
    // ── TypeScript 强制项 ──────────────────────────────────
    // 禁止 any（f2e-spec 强制）
    '@typescript-eslint/no-explicit-any': 'error',
    // 禁止未使用变量（允许下划线前缀忽略）
    '@typescript-eslint/no-unused-vars': ['error', {
      argsIgnorePattern: '^_',
      varsIgnorePattern: '^_',
    }],
    // 优先 interface 定义对象结构
    '@typescript-eslint/consistent-type-definitions': ['warn', 'interface'],
    // 禁止 non-null 断言（用可选链或类型守卫替代）
    '@typescript-eslint/no-non-null-assertion': 'warn',

    // ── 通用强制项 ────────────────────────────────────────
    // 禁止 var
    'no-var': 'error',
    // 优先 const
    'prefer-const': 'error',
    // 禁止 console.log（允许 warn/error）
    'no-console': ['warn', { allow: ['warn', 'error'] }],
    // 严格相等
    eqeqeq: ['error', 'always'],
    // 禁止 eval
    'no-eval': 'error',

    // ── Vue 强制项 ────────────────────────────────────────
    // SFC 顺序：script → template → style
    'vue/block-order': ['error', {
      order: ['script', 'template', 'style'],
    }],
    // 组件名 PascalCase
    'vue/component-name-in-template-casing': ['error', 'PascalCase'],
    // Props 必须声明类型
    'vue/require-prop-types': 'error',
    // 禁止 v-html（XSS 风险）
    'vue/no-v-html': 'warn',
    // 多属性换行
    'vue/max-attributes-per-line': 'off',
    // 允许单行 HTML 内容
    'vue/singleline-html-element-content-newline': 'off',
  },
  overrides: [
    {
      // 测试文件放宽规则
      files: ['**/*.test.ts', '**/*.spec.ts'],
      rules: {
        '@typescript-eslint/no-explicit-any': 'off',
        'no-console': 'off',
      },
    },
    {
      // 类型声明文件
      files: ['**/*.d.ts'],
      rules: {
        '@typescript-eslint/no-explicit-any': 'off',
        '@typescript-eslint/no-unused-vars': 'off',
      },
    },
  ],
}
