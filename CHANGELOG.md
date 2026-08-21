# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.1.0] - 2026-08-21

### Dependencies
- **eslint**: 10.8.1 → 10.9.0 (minor)
- **deepmerge-ts**: 8.0.1 → 8.0.2 (patch)

## [7.0.0] - 2026-08-21

### Dependencies
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 9.1.0 → 10.0.0 (major)
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.15.0 → 6.0.0 (major)

## [6.0.0] - 2026-08-20

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.5.0 → 40.0.0 (major)
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.14.0 → 5.15.0 (minor)

## [5.1.0] - 2026-08-19

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.3.0 → 39.5.0 (minor)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 9.0.0 → 9.1.0 (minor)
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.13.0 → 5.14.0 (minor)
- **@jeap/jeap-frontend-license-checker**: 1.0.1 → 1.0.2 (patch)

## [5.0.1] - 2026-08-19

### Changed
- Replaced the frontend license checker with `@jeap/jeap-frontend-license-checker`. The generated third-party notices now carry the full license texts of the redistributed dependencies, and the license steps run as part of the npm frontend build.
## [5.0.0] - 2026-08-18

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.0.1 → 39.3.0 (minor)
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.12.0 → 5.13.0 (minor)
- **uuid**: 11.1.1 → 14.0.2 (major)

## [4.0.0] - 2026-08-18

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.0.0 → 39.0.1 (patch)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 8.6.0 → 9.0.0 (major)
- **@quadrel-enterprise-ui/framework**: 20.34.0 → 20.35.0 (minor)

## [3.0.1] - 2026-08-18

### Security
- **deepmerge-ts**: 7.1.5 → 8.0.1 (CVE-2026-40345, npm override)
- **uuid**: 9.0.1 → 11.1.1 (CVE-2026-41907, npm override)

## [3.0.0] - 2026-08-13

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 38.1.0 → 39.0.0 (major)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 8.2.0 → 8.6.0 (minor)
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.9.0 → 5.12.0 (minor)
- **@typescript-eslint/parser**: 8.66.0 → 8.67.0 (minor)
- **@typescript-eslint/eslint-plugin**: 8.66.0 → 8.67.0 (minor)
- **@quadrel-enterprise-ui/framework**: 20.32.2 → 20.34.0 (minor)

## [2.1.0] - 2026-08-09

### Dependencies
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.8.1 → 5.9.0 (minor)

## [2.0.1] - 2026-08-07

### Dependencies
- **eslint**: 10.8.0 → 10.8.1 (patch)

## [2.0.0] - 2026-08-06

### Dependencies
- **com.microsoft.playwright:playwright**: 1.61.0 → 1.62.0 (minor)
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 37.6.0 → 38.1.0 (major)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 7.3.0 → 8.2.0 (major)
- **ch.admin.bit.jeap.jme:jme-spring-boot-integration-test**: 5.7.4 → 5.8.1 (minor)
- **org.zalando:logbook-spring-boot-starter**: 4.0.4 → 4.1.0 (minor)
- **zone.js**: 0.15.1 → 0.16.2 (minor)
- **prettier**: 3.8.1 → 3.9.6 (minor)
- **fast-uri**: 3.1.5 → 4.1.2 (major)
- **eslint-config-prettier**: 9.1.2 → 10.1.8 (major)
- **eslint**: 9.39.5 → 10.8.0 (major)
- **@typescript-eslint/parser**: 8.57.2 → 8.66.0 (minor)
- **@typescript-eslint/eslint-plugin**: 8.57.2 → 8.66.0 (minor)
- **@types/node**: 25.5.0 → 25.9.5 (minor)
- **@quadrel-enterprise-ui/framework**: 20.28.1 → 20.32.2 (minor)
- **@ngrx/store**: 20.1.0 → 21.1.1 (major)

## [1.0.1] - 2026-08-04

- Update Angular to 20.3.27 and fast-uri to 3.1.5 to fix security vulnerabilities.

## [1.0.0] - 2026-07-28

- Add a self-contained OAuth mock-server integration test.
- Add OSS metadata, license checks, credential scanning and public CI.
