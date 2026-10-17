# HIB Bank REST API

Java 17 and Spring Boot 4.1.1 backend for the Angular web client. Customer and request data are stored in a file-backed H2 database at `backend/data/hib-bank`; they persist when the API restarts. The backend keeps business rules in `BankService`, persistence in Spring Data repositories, and HTTP/session handling in `ApiController`.

## Run locally

From this folder, run `mvn spring-boot:run`. The API listens on `http://localhost:8080`. The Angular development origin `http://localhost:4200` is enabled with credentialed CORS; the client must send cookies (`withCredentials: true`). A customer session uses the standard HTTP-only `JSESSIONID` cookie.

The admin login is configurable with `BANK_ADMIN_USERNAME` and `BANK_ADMIN_PASSWORD`. For local demo use only, defaults are `admin` and `admin123`. Set strong values before exposing the service beyond a local development machine. H2 console is disabled.

## API

All JSON routes are under `/api`. Register and login return the customer view; registration also starts a session. Use `POST /auth/logout` to invalidate the session. Customer routes provide `/accounts/me`, `/accounts/deposit`, `/accounts/withdraw`, and `/requests`. Request creation expects the payer's username in `recipientUsername`; only that payer can decide a pending request. Requests expire 30 minutes after creation, and an expiry sweep runs once per minute as well as when the request inbox is read. Rejections remain visible to the payer and are included as a notification for the requester.

Admin routes require an admin session: `POST /admin/login`, `GET /admin/customers`, `PATCH /admin/customers/{username}/frozen`, `PATCH /admin/customers/{username}/active`, and `POST /admin/customers/{username}/charge`. Duplicate username or email registration responds with HTTP 409. Other validation and business-rule failures return a useful HTTP status and message.
