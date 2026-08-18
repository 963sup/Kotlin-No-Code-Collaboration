# Security Policy

## Supported branch

Security fixes are maintained on `main`.

## Reporting a vulnerability

Do not publish exploitable details, credentials, tokens, tenant data, or proof-of-concept payloads in a public Issue or Discussion.

Use GitHub private vulnerability reporting when it is available for this repository. If that channel is unavailable, open a public Issue containing only a short non-sensitive request for a private reporting channel; do not include exploit details.

High-value security boundaries for this Android collaboration client include:

- authentication and authorization;
- enterprise / repository tenant isolation;
- saved, notification, deep-link and canonical target access checks;
- local Room data, backup and device-transfer exposure;
- remote sync, Firebase ID-token verification and HTTPS transport;
- audit integrity and security-sensitive state changes.

Please include affected version or commit, reproducible impact, and the smallest safe reproduction information available. There is no guaranteed response SLA.
