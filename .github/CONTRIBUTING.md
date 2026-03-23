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

#### Project structure
We follow a **layered architecture**, where an application is organized into horizontal layers,
and each layer has a clear responsibility. 
Ensure to follow the structure as described in [ARCHITECTURE.md](../docs/ARCHITECTURE.md).

#### Coding standard

We follow [Google Java Style Standard](https://google.github.io/styleguide/javaguide.html)
for all Java code to ensure consistency and readability across the project.

> \[!TIP\]
> Make sure to format your code using `./gradlew spotlessApply` before commit.

#### Commit message

Each commit should follow the structure below:
```bash
Header  # A concise summary, no more than 50 characters

Body    # Optional. Wrap lines at 72 characters
```
### Development

1. Create a branch by following the convention: `task/<issue_number>-<IndicatorOfTheTask>`.
2. Merge to `dev` when the task development is completed.
3. `dev` is only merged to `main` on the release day.

> \[!IMPORTANT\]
> All tasks branch need to be merged to `dev`. Please avoid merging task branch directly to `main`.

 ### Authentication for development

For every API call, except from `api/auth/*`, the application will expect `JWT` to be included in the header of the request.
To obtain the `JWT` token, you will need to log in to the application (if you have not yet created an account, please do so).
After log in successfully, you can find `JWT` in cookie:

 ![loginFromPostman.png](docs/img/loginFromPostman.png)
 
Postman will also cache your cookie here: \
![tokenExample.png](docs/img/tokenExample.png)

If you need to clean cookie and login against, please select `Cookies` and remove `Authorization` cookie