# JME Security Example

This project contains example applications that demonstrate how to implement OAuth2-protected resources and clients
with [jEAP](https://github.com/jeap-admin-ch/jeap). It covers service-to-service access, browser login, token forwarding,
semantic authorization and role introspection. The project is self-contained and uses the included OAuth mock server for
local development and integration tests.

## Modules

| Module | Purpose |
| --- | --- |
| `jme-security-resource-service` | OAuth2 resource server with partner- and operation-based semantic authorization |
| `jme-security-client-service` | OAuth2 client that calls the resource and client/resource services |
| `jme-security-clientresource-service` | Service acting as both an OAuth2 client and resource server |
| `jme-security-auth-scs` | Configured instance of the jEAP OAuth mock server |
| `jme-security-ui` | Angular OAuth2 client packaged into the SCS |
| `jme-security-scs` | Self-contained Spring Boot backend and Angular UI |
| `jme-security-test` | End-to-end tests using the included OAuth mock server |

## How the Examples Relate

The browser-login example and the service-to-service example are independent. The UI packaged in
`jme-security-scs` authenticates a user with the OAuth mock server and calls only SCS endpoints. The SCS does not call
`jme-security-resource-service` or `jme-security-client-service`.

```text
Browser -> jme-security-scs
        -> jme-security-auth-scs (authorization-code flow)
```

The service-to-service example starts at `jme-security-client-service`. Its public example endpoints obtain a system
token from the OAuth mock server using the client-credentials flow and use that token to call the protected
`jme-security-resource-service` endpoints.

```text
Caller -> jme-security-client-service -> jme-security-resource-service
                    |
                    +-> jme-security-auth-scs (client-credentials flow)
```

## Prerequisites

- JDK 25 or later
- Node.js 22 and npm
- Google Chrome for the browser integration tests

## Build and Test

Build all modules and run their tests with:

```shell
./mvnw install
```

The end-to-end test starts the OAuth mock and resource service as Maven subprocesses, obtains a client-credentials token
and exercises a protected business endpoint. To run only that test:

```shell
./mvnw install -pl '!:jme-security-test'
./mvnw verify -pl jme-security-test
```

## Running Locally

Build the project first, then start `jme-security-auth-scs` with the `local` profile. Start only the resource, client and
SCS applications needed for the access chain you want to test.

```shell
./mvnw -pl jme-security-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-resource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-client-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-clientresource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-scs spring-boot:run -Dspring-boot.run.profiles=local
```

The local client secrets are intentionally public test fixtures and must not be reused outside this example.

### Trying the Client and Resource Services

For the basic service-to-service example, start these three applications in separate terminals:

```shell
./mvnw -pl jme-security-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-resource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-client-service spring-boot:run -Dspring-boot.run.profiles=local
```

Call the client service without providing credentials:

```shell
curl http://localhost:8090/jme-security-client-service/api/partners
```

The client service obtains a token for `jme-security-client-service`, adds it as a bearer token to its outgoing request
and returns the response from the resource service. The response starts with `Partner list:` followed by the accessible
partners.

Calling the protected resource endpoint without a token returns `401 Unauthorized`:

```shell
curl -i http://localhost:8070/jme-security-resource-service/api/partners
```

To call the resource directly, first request a token from the OAuth mock server:

```shell
curl --user jme-security-client-service:secret \
  --data grant_type=client_credentials \
  http://localhost:8081/jme-security-auth-scs/oauth2/token
```

Copy the `access_token` from the response and use it in the protected request:

```shell
curl --header "Authorization: Bearer <access-token>" \
  http://localhost:8070/jme-security-resource-service/api/partners
```

### Multiple Authorization Servers

The OAuth mock server also provides a `local-2` profile. It exposes the same users and roles on another port and signs
tokens with another key. This can be used to demonstrate a resource server that trusts multiple issuers.

```shell
./mvnw -pl jme-security-auth-scs spring-boot:run -Dspring-boot.run.profiles=local-2
./mvnw -pl jme-security-resource-service spring-boot:run -Dspring-boot.run.profiles=local,local-auth-servers
./mvnw -pl jme-security-client-service spring-boot:run -Dspring-boot.run.profiles=local,local-auth-servers
```

The client configuration supports the providers `mock-server-1` and `mock-server-2`; `mock-server-2` is selected by
default in `application-local-auth-servers.yml`.

## Access Chains

The examples support these access chains:

- `client-service -> resource-service`
- `client-service -> clientresource-service -> resource-service`
- `UI -> SCS`

The partner endpoints on `jme-security-client-service` call `jme-security-resource-service` by default. Add
`target=clientresource` to route the call through `jme-security-clientresource-service` instead:

- `/api/partners`
- `/api/partners?target=clientresource`

## Resource Endpoints

The resource service demonstrates annotation-based and programmatic authorization, roles scoped to a business partner,
roles valid for all partners and authorization by operation only. The client service exposes corresponding endpoints and
adds the required access token when forwarding requests.

### Partners

- `/api/partners` lists all partners the caller may access.
- `/api/partners/11111` returns a partner by numeric ID.
- `/api/partners/eins` returns a partner by external reference.
- `/api/partners/eins/name` returns only the partner name.

### Things

- `/api/things` lists all accessible things.
- `/api/partners/11111/things` lists things belonging to a partner.
- `/api/things/1` returns one thing belonging to a partner.

### Operation-only Authorization

These endpoints demonstrate authorization based on an operation without specifying a semantic resource:

- `/api/operation-things`
- `/api/operation-things/partners/11111`
- `/api/operation-things/1`

### Additional Endpoints

- `/api/info` forwards a request protected with basic authentication instead of OAuth2.
- `/api/introspected-roles` returns roles added during token introspection.
- `/api/introspected-roles?pruned=true` uses a client whose token roles are pruned and recovered by introspection.
- `/api/current-user` calls the SCS current-user endpoint with semantic roles in the standard syntax.
- `/api/current-user?alternateRoles=true` uses the alternate semantic-role syntax.
- `/api/bproles` returns the business-partner roles from the access token.
- `/api/bproles?scoped=true` requests a token whose roles are restricted with the `bproles:11111` scope.

## Local Test URLs

### UI and SCS

- http://localhost:8080/jme-security-scs/

### Client Service

- http://localhost:8090/jme-security-client-service/api/partners
- http://localhost:8090/jme-security-client-service/api/partners?target=clientresource
- http://localhost:8090/jme-security-client-service/api/partners/11111
- http://localhost:8090/jme-security-client-service/api/partners/eins/name
- http://localhost:8090/jme-security-client-service/api/things
- http://localhost:8090/jme-security-client-service/api/partners/11111/things
- http://localhost:8090/jme-security-client-service/api/things/1
- http://localhost:8090/jme-security-client-service/api/operation-things
- http://localhost:8090/jme-security-client-service/api/operation-things/partners/11111
- http://localhost:8090/jme-security-client-service/api/operation-things/1
- http://localhost:8090/jme-security-client-service/api/info
- http://localhost:8090/jme-security-client-service/api/introspected-roles
- http://localhost:8090/jme-security-client-service/api/introspected-roles?pruned=true
- http://localhost:8090/jme-security-client-service/api/current-user
- http://localhost:8090/jme-security-client-service/api/current-user?alternateRoles=true
- http://localhost:8090/jme-security-client-service/api/bproles?scoped=true

## Local Tests with a Simulated B2B Gateway

The `jme-security-auth-scs` and `jme-security-client-service` modules provide a `local-b2b` profile. This profile starts
the applications on separate ports and configures the client service to obtain simulated B2B gateway tokens from the
OAuth mock server. The client service still calls `jme-security-resource-service`, so start the resource service with
the `local` profile.

```shell
./mvnw -pl jme-security-auth-scs spring-boot:run -Dspring-boot.run.profiles=local-b2b
./mvnw -pl jme-security-resource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-client-service spring-boot:run -Dspring-boot.run.profiles=local-b2b
```

Use this endpoint to test a request with a simulated B2B gateway token:

- http://localhost:8190/jme-security-client-service/api/partners

## Preparing and Starting the Local SCS

To use the UI packaged in the SCS, start the OAuth mock server and then the SCS with the `local` profile:

```shell
./mvnw -pl jme-security-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-scs spring-boot:run -Dspring-boot.run.profiles=local
```

Open http://localhost:8080/jme-security-scs/ and use the mock login form to select a user and roles.

To run the Angular development server separately, start the SCS with `local-ui`, then run `npm start` in
`jme-security-ui`. The UI is available on port `4200` and calls the SCS on port `8080`.

## Restricting Business-partner Roles

The OAuth mock configuration contains the clients `jme-security-ui-bpscoped` and
`jme-security-client-service-bpscoped`. They support the dynamic scope `bproles:*`, which restricts business-partner
roles in a token to one partner.

For the system context, call:

- http://localhost:8090/jme-security-client-service/api/bproles?scoped=true

This uses the scope `bproles:11111`. For the user context, start `jme-security-scs` with the profiles `local,bpscoped`.
The UI then requests tokens for `jme-security-ui-bpscoped` with the same scope.

## Changes

This project follows Semantic Versioning. Changes are documented in [CHANGELOG.md](CHANGELOG.md).

## JME

This repository is part of the [JME open source distribution](https://github.com/jme-admin-ch/jme).

## License

This repository is Open Source Software licensed under the [Apache License 2.0](LICENSE).
