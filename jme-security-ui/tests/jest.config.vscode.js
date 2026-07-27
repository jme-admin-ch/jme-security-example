'use strict';

const rootConfig = require('./jest.config');

module.exports = {
  ...rootConfig,
  rootDir: '../',

  // not useful for single file debuggung. And: sonar has path issues under vscode
  collectCoverage: false,
  testResultsProcessor: undefined
};
