# HIB — Banking System Simulation

HIB is a Java-based banking system simulation that runs in the command-line interface (CLI). It is designed as an educational project for practicing Java development and understanding basic banking system concepts.

## Features

- Command-line interface for interacting with the application.
- Java-based application logic.
- Maven-based project configuration.
- Banking operations implemented in the source code.
- Educational simulation of banking workflows.

> **Disclaimer:** This project is intended for educational purposes only. It is not a real banking system and should not be used for real financial transactions or sensitive financial data.

## Requirements

- Java Development Kit (JDK), compatible with the version specified in `pom.xml`.
- Apache Maven.
- Git.

Verify your installations:

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

Compile the source code and package the application using Maven:

```bash
mvn clean package
```

Maven generates build artifacts in the `target/` directory.

## Run the Application

Open the project in your preferred Java IDE, such as IntelliJ IDEA, locate the class containing the `main()` method, and run it.

If the project is configured to generate an executable JAR, run:

```bash
java -jar target/HIB-1.0-SNAPSHOT.jar
```

If the JAR is not executable, run the main class through your IDE or configure the Maven build to specify the application's entry point.

## Usage

1. Start the application.
2. Follow the command-line prompts.
3. Select from the operations provided by the application.
4. Enter the requested information.
5. Follow the displayed instructions to complete each operation.
6. set up environment variables in your system to login as admin (see .env file)

The available commands and banking operations depend on the current implementation.

## Project Structure

```text
HIB/
├── src/          # Java source code
├── data/         # Project data, if applicable
├── pom.xml       # Maven configuration
└── README.md     # Project documentation
```

## Troubleshooting

- **Java is not recognized:** Install a compatible JDK and configure your system PATH.
- **Maven is not recognized:** Install Apache Maven and configure your system PATH.
- **Build fails:** Verify the Java version and dependencies specified in `pom.xml`.
- **JAR cannot be executed:** Run the main class from your IDE or configure the Maven JAR manifest.

## Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Submit a pull request describing your updates.

## License

Check the repository for a `LICENSE` file to determine the applicable license and usage permissions.

## Repository

[HIB on GitHub](https://github.com/W0rd1/HIB)
