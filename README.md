
# HIB — Banking System Simulation

HIB is a Java-based banking system simulation that runs through a Command-Line Interface (CLI). It provides a practical way to explore banking workflows and Java application development.

## Screenshots

The following screenshots illustrate the application interface and its command-line workflow.

### Main Menu
![HIB Main Menu](docs/images/main-menu.png)

### Banking Operations
![HIB Banking Operations](docs/images/banking-operations.png)

### Administrator Access
![HIB Administrator Access](docs/images/admin-access.png)

> **Note:** Replace these placeholder image paths with actual screenshots from the application.

## Features

- Command-line interface for interacting with the banking system.
- Java-based application logic.
- Maven project configuration.
- Banking operations implemented in the application.
- Administrator access configured through environment variables.

## Requirements

- Java Development Kit (JDK) compatible with the project configuration.
- Apache Maven.
- Git.

Check your installed versions:

```bash
java -version
mvn -version
git --version
```

## Installation

Clone the repository:

```bash
git clone https://github.com/W0rd1/HIB.git
cd HIB
```

## Build the Project

Compile and package the application:

```bash
mvn clean package
```

The build artifacts are generated in the `target/` directory.

## Configuration

### Administrator Account

To log in using the administrator account, you must configure the environment variables defined in the project's `.env` file.

1. Locate the `.env` file in the project.
2. Identify the environment variables used for administrator authentication.
3. Set those variables in your environment before starting the application.
4. Run the application and use the configured administrator credentials.

**Important:** Use the exact variable names and values expected by the project. Do not share your `.env` file or commit credentials and secrets to the repository.

If the application does not load `.env` automatically, make sure the variables are exported into the process environment or use the project's existing environment-loading mechanism.

## Run the Application

Run the main class from your Java IDE, or use the executable JAR if the Maven configuration supports it:

```bash
java -jar target/HIB-1.0-SNAPSHOT.jar
```

The JAR filename and executable configuration may vary depending on `pom.xml`.

## Usage

1. Start the application.
2. Follow the CLI prompts.
3. Select an available operation.
4. Enter the requested information.
5. To access administrator functionality, configure the required environment variables from `.env` before launching the application.

Available operations depend on the current implementation.

## Project Structure

```text
HIB/
├── src/                # Java source code
├── data/               # Application data, if applicable
├── docs/
│   └── images/         # README screenshots
├── .env                # Local environment configuration
├── pom.xml             # Maven configuration
└── README.md           # Project documentation
```

## Troubleshooting

- **Java is not recognized:** Install a compatible JDK and configure your system PATH.
- **Maven is not recognized:** Install Maven and configure your system PATH.
- **Build fails:** Check the Java version and dependencies in `pom.xml`.
- **Administrator login fails:** Verify that all required environment variables from `.env` are configured correctly before starting the application.
- **Screenshots are not displayed:** Confirm that the image files exist at the paths specified in this README.

## Security Notice

HIB is an educational banking simulation, not a production banking system.

- Never commit passwords, API keys, or other secrets.
- Keep `.env` out of version control.
- Do not use real financial credentials or sensitive customer data.

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Submit a pull request with a description of the changes.

## License

Check the repository for a `LICENSE` file to determine the applicable usage and redistribution terms.

## Repository

[GitHub — HIB](https://github.com/W0rd1/HIB)
