'use strict';

const rootConfig = require('./jest.config');

module.exports = {
  ...rootConfig,
  rootDir: '../',
  setupFilesAfterEnv: ['<rootDir>/tests/setupJest.idea.ts']
};
