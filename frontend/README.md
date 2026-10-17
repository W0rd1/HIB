# HIB Bank web frontend

Angular 21 customer and admin interface for the Spring Boot API.

## Run locally

1. Start the Spring Boot backend on `http://localhost:8080`.
2. Install a current Node.js release with npm.
3. From this folder run `npm install` and then `npm start`.
4. Open `http://localhost:4200`.

The API base URL defaults to `http://localhost:8080`. To use another API address, set `window.HIB_API_BASE_URL` before Angular starts (for example in `src/index.html` with a small script before the application bundle). All API requests use `withCredentials: true`; the backend must allow credentialed CORS requests from the frontend origin and set its session cookie accordingly.

## API routes used

Customer auth and account: `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/accounts/me`, `/api/accounts/deposit`, `/api/accounts/withdraw`.

Payment requests: `/api/requests`, `/api/requests/{uuid}/decision`.

Admin: `/api/admin/login`, `/api/admin/customers`, `/api/admin/customers/{username}/frozen`, `/api/admin/customers/{username}/active`, `/api/admin/customers/{username}/charge`.

The frontend expects JSON responses. Collection endpoints may return either a JSON array or an object containing `requests`/`inbox` and `customers` respectively. The account endpoint may return the account directly or under `account` or `customer`.
