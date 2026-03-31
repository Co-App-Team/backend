# Co-App Backend REST API Server

[![Docker Build](https://github.com/Co-App-Team/backend/actions/workflows/docker-build.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/docker-build.yml)
[![Unit tests](https://github.com/Co-App-Team/backend/actions/workflows/unit-testing.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/unit-testing.yml)
[![Code Format](https://github.com/Co-App-Team/backend/actions/workflows/format.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/format.yml)
[![Gradle Build](https://github.com/Co-App-Team/backend/actions/workflows/gradle.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/gradle.yml)

## About us

CoApp is a co-op application management platform, a comprehensive web application designed to streamline the student experience of the co-op job search. This platform uses React, Spring Boot, and MongoDB to address the most common challenges faced by students throughout their co-op job searches and provides an all-in-one space to manage job applications, interview preparations, and researching potential employers.

This application takes all the essential co-op organizational tools and puts them in one user-friendly interface. Students can track their job applications from the start to their outcome, maintain interview schedules for these applications with a calendar view, and access a communal “rate my co-op” review board to see what others think about their work terms. This app gets rid of the need for multiple scattered spreadsheets, tracking apps, or unorganized notes.

For further information, please check out our [Project Proposal](https://github.com/Co-App-Team/.github/blob/main/docs/ProjectProjectProposal.md).

## Onboarding Instructions

Please see instructions from [CONTRIBUTING.md](.github/CONTRIBUTING.md)

## Project package structure

We follow a **layered architecture**, where an application is organized into horizontal layers, 
and each layer has a clear responsibility. For further breakdown, please check out [ARCHITECTURE.md](docs/ARCHITECTURE.md)

## Running the App Locally

### Docker

To run application with Docker image, please follow steps:
1. Build docker image
```bash
docker image build -t coapp-backend .
```

2. Run docker image
```bash
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/local.properties:/app/local.properties \
  -e SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/local.properties \
  coapp-backend
  ```
