# Contributing

Thank you for contributing! This guide will help you set up the project environment.

## Setup Instructions

1. **Clone the repository**

   ```bash
   git clone https://github.com/Co-App-Team/backend.git
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

### Preliminary
We follow a **layered architecture**, where an application is organized into horizontal layers,
and each layer has a clear responsibility. 
Ensure to follow the structure as described in [ARCHITECTURE.md](../docs/ARCHITECTURE.md).

### Development

1. Create a branch by following the convention: `feat/<issue_number>-<IndicatorOfTheFeature>`.
2. Merge to `dev` when the feature development is completed.
3. `dev` is only merged to `main` on the release day.

> \[!IMPORTANT\]
> All features branch need to be merged to `dev`. Please avoid merging feature branch directly to `main`.

 
 