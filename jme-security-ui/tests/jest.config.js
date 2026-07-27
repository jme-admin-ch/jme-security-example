'use strict';

module.exports = {
  roots: ['<rootDir>src/app'],
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/tests/setupJest.ts'],
  moduleNameMapper: {
    qdQuadrelSandbox: '<rootDir>src/app/app.module.ts'
  },
  globals: {
    'ts-jest': {
      diagnostics: false,
      tsconfig: '<rootDir>/tsconfig.spec.json'
    }
  },
  collectCoverage: true,
  coverageDirectory: '<rootDir>/tests/coverage/sonarQube',
  forceCoverageMatch: ['**/src/**/*.ts', '**/src/**/*.html'],
  testResultsProcessor: 'jest-sonar-reporter'
};
