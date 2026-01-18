# Co-App Backend REST API Server

[![Docker Build](https://github.com/Co-App-Team/backend/actions/workflows/docker-build.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/docker-build.yml)
[![Unit tests](https://github.com/Co-App-Team/backend/actions/workflows/unit-testing.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/unit-testing.yml)
[![Code Format](https://github.com/Co-App-Team/backend/actions/workflows/format.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/format.yml)
[![Gradle Build](https://github.com/Co-App-Team/backend/actions/workflows/gradle.yml/badge.svg?branch=main&event=push)](https://github.com/Co-App-Team/backend/actions/workflows/gradle.yml)

*Description: TODO*

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
docker run -d -p 8080:8080 coapp-backend
```
