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
[Docs](https://docs.cloud.google.com/datastore/docs/tools/datastore-emulator)
```
gcloud components install cloud-datastore-emulator
```

---
## Usage
Authenticate to your Google Account:
```
gcloud auth application-default login
```
Config to your project:
```
gcloud config set project <proj-id>
```

### Local
```
mvn clean package -DskipTests
mvn appengine:run
```

### Emulator
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
The entities you can find in the Datastore are User and Token, and they are independent of one another.

The same user can log in multiple times, creating always a new login token.

The login token has the role of the user, so when changing the role of a user, every login token of that user is updated with the new role.

When a user is deleted or logs out, every login token of that user is also deleted.