# ADC Individual Project

---
## Before you begin
### Dependencies
- Java 21
- Maven
- Git

### Google Cloud CLI
[Docs](https://docs.cloud.google.com/sdk/docs/install-sdk)

### Google Datastore Emulator
```
gcloud components install cloud-datastore-emulator
```
[Docs](https://docs.cloud.google.com/datastore/docs/tools/datastore-emulator)

---
## Usage
### Local
Authenticate to your Google Account:
```
gcloud auth login
```
Config to your project:
```
gcloud config set project <proj-id>
```
```
mvn clean package -DskipTests
mvn appengine:run
```

### Emulator
Authenticate to your Google Account:
```
gcloud auth application-default login
```
Config to your project:
```
gcloud config set project <proj-id>
```
In a new terminal run the emulator:
```
gcloud beta emulators datastore start --host-port=localhost:8081 --no-store-on-disk
```
In a new terminal run your application (it will be attached to the emulator):
```
export DATASTORE_EMULATOR_HOST=localhost:8081
mvn clean package -DskipTests
mvn appengine:run
```

---
## Test Application
Make sure the emulator is running and set the environment variable:
```
export DATASTORE_EMULATOR_HOST=localhost:8081
```
Then run:
```
mvn test
```
In IntelliJ IDEA add `DATASTORE_EMULATOR_HOST=localhost:8081` to the environment variables.

---
## Deploy Application
```
gcloud auth login
gcloud config set project <proj-id>
mvn appengine:deploy -Dapp.deploy.projectId=<your-proj-id> -Dapp.deploy.version=<version-number>
```
Your application will be running on: `https://<your-project-id>.appspot.com/`

---
## Implementation
The entities that can be found in the Datastore are 'User' and 'Token', which are independent of one another.

A user can log in multiple times, creating a new login token each time. This enables the user to log in on different devices.

Each login token represents a user, so when a user's role changes, all of their login tokens are updated with the new role.

When a user changes their password, they are logged out.

When a user is deleted, their login tokens are also deleted.

When a user logs out, the token used to log out is deleted. This keeps the user logged in on different devices.

When an admin logs out a user, all of their tokens are deleted.