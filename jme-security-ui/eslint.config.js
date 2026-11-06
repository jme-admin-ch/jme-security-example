const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = tseslint.config(
  {ignores: ['projects/**', 'node_modules/**', 'target/**', 'coverage/**']},
  {
    files: ['**/*.ts'],
    extends: [...angular.configs.tsRecommended],
    processor: angular.processInlineTemplates,
    rules: {
      '@angular-eslint/prefer-standalone': 'off',
      // Preserve the existing constructor injection while upgrading the framework.
      '@angular-eslint/prefer-inject': 'off'
    }
  },
  {
    files: ['**/*.html'],
    extends: [...angular.configs.templateRecommended]
  }
);
