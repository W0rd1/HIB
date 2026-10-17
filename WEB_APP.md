# HIB Bank web app

The web app has two local services: the Spring Boot API and the Angular 21 site. The site sends credentialed HTTP requests to the API at `http://localhost:8080` and runs at `http://localhost:4200`.

## Start the API

In a terminal, open `backend/` and run:

```text
mvn spring-boot:run
```

The API stores customers and payment requests in the file-backed H2 database under `backend/data/`. Set `BANK_ADMIN_USERNAME` and `BANK_ADMIN_PASSWORD` before starting it to configure the administrator login. Local demo defaults are documented in [backend/README.md](backend/README.md).

## Start the website

In a second terminal, open `frontend/` and run:

```text
npm install
npm start
```

Then open `http://localhost:4200`. The frontend defaults to the API at `http://localhost:8080`; its API address can be overridden with `window.HIB_API_BASE_URL` in `frontend/src/index.html`.

See [backend/README.md](backend/README.md) and [frontend/README.md](frontend/README.md) for the API and feature details.
