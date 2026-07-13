# CampusLink Career Modernization Plan

Generated from the assessment report at [.github/modernize/assessment/reports/report-20260711225851/report.json](../../assessment/reports/report-20260711225851/report.json).

## Scope

Selected assessment categories:
- Java runtime upgrade
- Containerization
- Database migration
- File-system management
- Hardcoded credentials
- Default encoding

Additional security tasks:
- Resolve CWE-567, CWE-662, CWE-820, and CWE-821 by hardening shared-state synchronization.
- Resolve CWE-259, CWE-778, and CWE-798 by removing hard-coded secrets and adding authentication audit logging.
- Resolve CWE-22, CWE-23, and CWE-36 by validating and restricting file paths before opening resources.
- Resolve the JasperReports CVE findings by upgrading vulnerable dependencies.

## Planned work

1. Upgrade the Java runtime and Maven build configuration to a current supported LTS release and validate the desktop application build.
2. Add container support with a Dockerfile and make the app compatible with Azure Container Apps or App Service.
3. Externalize database settings and plan the migration from local MySQL to Azure Database for MySQL.
4. Replace local file-system usage with cloud-safe storage patterns and move configuration out of local files.
5. Remove hard-coded credentials and default passwords from schema, documentation, and runtime defaults.
6. Add authentication audit events and security logging for login success/failure paths.
7. Harden resource-opening flows so paths are normalized, restricted, and validated before use.
8. Make shared singleton utilities thread-safe and remove unsafe concurrent access patterns.
9. Upgrade JasperReports to a patched version that addresses the reported CVE advisories.

## Acceptance criteria

- The application builds successfully with the upgraded Java toolchain.
- The app can run in a containerized environment with environment-based configuration.
- Secrets are no longer embedded in source, schema, or documentation.
- Resource access is limited to safe locations and path traversal is blocked.
- Shared utilities are thread-safe under concurrent use.
- Vulnerable JasperReports dependencies are upgraded and verified.
