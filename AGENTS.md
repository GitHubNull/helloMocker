# AGENTS.md

Coding agent instructions for the helloMocker BurpSuite HTTP Mock plugin project.

## Project Structure

This is a **multi-module Maven project** with the following structure:

```
helloMocker/
├── pom.xml                              # Parent POM (manages all modules)
├── helloMocker-api/                     # API module (IMockHandler interface)
│   ├── pom.xml
│   └── src/main/java/org/oxff/hellomocker/api/
│       └── IMockHandler.java
├── helloMocker-plugin/                  # Plugin module (Burp extension)
│   ├── pom.xml
│   └── src/                             # All plugin source code
└── examples/
    ├── helloMocker-api-example/         # Spring Boot example
    └── jar-extension/                   # JAR extension example
```

### Module Descriptions

- **helloMocker-api**: Contains the `IMockHandler` interface for JAR extensions. This is a separate module so users can depend on it without pulling in all plugin dependencies.
- **helloMocker-plugin**: The main BurpSuite extension module. Contains all the plugin code.
- **examples/**: Contains example projects demonstrating how to use the API and create JAR extensions.

## Build Commands

```bash
# Compile the project
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Package the JAR (includes running tests)
mvn package

# Package without tests
mvn package -DskipTests

# Clean build artifacts
mvn clean

# Full clean build
mvn clean package

# Install to local Maven repository
mvn install
```

## Code Style Guidelines

### Java Conventions

- **Java Version**: Java 17 (LTS)
- **Encoding**: UTF-8
- **Indentation**: 4 spaces (no tabs)
- **Line endings**: LF (Unix-style)
- **Max line length**: 120 characters

### Naming Conventions

- **Classes**: PascalCase (e.g., `MockRuleManager`)
- **Interfaces**: PascalCase with descriptive names (e.g., `IMockHandler`)
- **Methods**: camelCase, verbs first (e.g., `handleRequest()`)
- **Variables**: camelCase (e.g., `requestBody`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- **Packages**: lowercase, reverse domain (e.g., `org.oxff.hellomocker`)

### Imports

- Organize imports: static first, then java.*, then javax.*, then third-party, then project
- No wildcard imports (e.g., `import java.util.*;` is forbidden)
- Remove unused imports before committing

### Formatting

- Opening braces on same line: `if (x) {`
- One blank line between methods
- Two blank lines between class definitions
- Always use braces for control structures

### Types & Null Safety

- Use `Optional<T>` for nullable returns
- Avoid raw types, use generics
- Prefer `final` for variables that don't change
- Use `var` only when type is obvious

### Error Handling

- Use checked exceptions for recoverable errors
- Use unchecked exceptions for programming errors
- Never swallow exceptions with empty catch blocks
- Always include context in exception messages
- Log exceptions before re-throwing

### Documentation

- All public APIs must have Javadoc
- Use `@param`, `@return`, `@throws` tags
- Document thread-safety considerations
- Example:
```java
/**
 * Processes the HTTP request and returns a mock response.
 *
 * @param request the incoming HTTP request
 * @return the mock response bytes
 * @throws MockProcessingException if processing fails
 */
public byte[] handleRequest(HttpRequest request) throws MockProcessingException;
```

### Testing

- Use JUnit 5 (Jupiter)
- Test class names: `*Test` suffix
- Test methods: descriptive names using snake_case with backticks (JUnit 5)
- Aim for >80% code coverage
- Mock external dependencies
- One assertion per test (when practical)

### BurpSuite Extension Specific

- Implement `burp.api.montoya.BurpExtension` interface
- Register all handlers in `initialize()` method
- Use Montoya API (v2023.x+)
- Never block the EDT (Event Dispatch Thread)
- Log to Burp's logging API, not System.out

### Security

- Never hardcode credentials or secrets
- Validate all user inputs
- Use parameterized logging to prevent injection
- Review all reflection usage carefully
- Sanitize Python script inputs

### Git

- Commit message format: `<type>: <subject>`
- Types: feat, fix, docs, style, refactor, test, chore
- Keep commits focused and atomic
- Reference issue numbers when applicable
