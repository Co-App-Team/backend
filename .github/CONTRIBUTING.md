# Contributing

Thank you for contributing! This guide will help you set up the project environment.

## Setup Instructions

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   ```

2. **Open the project**

    * Open the project in **IntelliJ IDEA**.

3. **Install Java JDK**

    * Make sure you have **Java JDK 21** installed. You can install it from [Oracle](https://www.oracle.com/ca-en/java/technologies/downloads/#java21) or via IntelliJ.
    * [Configure IntelliJ to use JDK 21](https://www.baeldung.com/intellij-change-java-version) for the project.

4. **Set up Gradle**

    * If Gradle is not set up in IntelliJ, download and configure it.
    * Set the Gradle version for this project:

      ```bash
      ./gradlew wrapper --gradle-version 9.2.1
      ```
    * Verify Gradle version:

      ```bash
      ./gradlew --version
      ```

5. **You're ready to go!**

    * You can now build the project and run tasks using Gradle.

## Development workflow

TBD 