# Dataspace Datalife Connector Extensions

## Directory Structure

### `extensions/`

Contains the source code of the extensions. It also contains implementations for certain SPIs in the **common** directory.

### `spi/`

Contains the source code of interfaces thar are meant to be implemented/extended in order to be used on extensions.

### `launchers/`

Contains launcher modules that generate Docker images and provide comprehensive API documentation for each component. Each launcher includes:

**Available Launchers:**
- **`controlplane/`** - Control Plane with Management API, DSP API, Participant API, Control API, Catalog API, and observability endpoints
- **`dataplane/`** - Data Plane with Public API, Control API, and observability endpoints
- **`identity-hub/`** - Identity Hub with Identity API, DID API, Credentials API (DCP), STS API, and observability endpoints

**Launcher Contents:**
- `src/main/docker/Dockerfile` - Docker image configuration with base image, dependencies, health checks, and startup commands
- `api-docs/` - OpenAPI specifications organized by API context
- `README.md` - Comprehensive documentation covering API endpoints, default ports, base paths, and Docker configuration


### `gradle/`

Contains Gradle build configuration and dependency management:

- **`libs.versions.toml`** - Version catalog defining all project dependencies and their versions. Provides centralized dependency version management across all modules, ensuring consistency and simplifying updates. For example:

    ```sh
    [versions]
    edc = "0.12.0"
    gradiant_dataspace = "0.3.0" # change it to the version in use recent version

    [libraries]
    # EDC Libraries used by extensions
    edc-dataplane-spi = { module = "org.eclipse.edc:data-plane-spi", version.ref = "edc"}
    edc-util-lib = { module = "org.eclipse.edc:util-lib", version.ref = "edc"}
    edc-dataplane-util = { module = "org.eclipse.edc:data-plane-util", version.ref = "edc"}

    # Dataspace Components Bundles
    controlplane = { module = "org.gradiant.edc:bundle-controlplane", version.ref = "gradiant_dataspace" }
    dataplane = { module = "org.gradiant.edc:bundle-dataplane", version.ref = "gradiant_dataspace" }
    identityhub = { module = "org.gradiant.edc:bundle-identityhub", version.ref = "gradiant_dataspace" }
    ```

- **`wrapper/`** - Gradle wrapper files for reproducible builds

## Component Building Dependecies

In the file **settings.gradle.kts** this dependencies should be present:

```sh
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri(System.getenv("MAVEN_PKG_URL"))
            credentials {
                username = System.getenv("MAVEN_PKG_USERNAME")
                password = System.getenv("MAVEN_PKG_PASSWORD")
            }
        }
        mavenCentral()

    }
}
``` 

The second repository, *maven {...}* represents the github package repository from where the artifacts will are obtained with the github access token credential.


## Publishing Strategy Configuration

In **build.gradle.kts**, the publishing strategy is to upload the maven package created in a dedicated Github Package Repository to

```bash
val publishStrategies = mapOf(
    LOCAL_REPO to { repoHandler: RepositoryHandler ->
        repoHandler.mavenLocal()
    },
    "github" to { repoHandler: RepositoryHandler ->
        repoHandler.maven {
            name = "GitHubPackages"
            url = uri(System.getenv("PUBLISHING_URL"))
            credentials {
                username = System.getenv("PUBLISHING_USER")
                password = System.getenv("PUBLISHING_PASSWORD")
            }
        }
    }
)
```

## Build


First, export the token using the next commands template.

```shell
export MAVEN_PKG_URL=maven_package_url # ex.: https://maven.pkg.github.com/dataspacedatalife/connector
export MAVEN_PKG_USERNAME=your_username
export MAVEN_PKG_PASSWORD=your_token
```

**The token must have Package READ permission**


Once the variables are set, run the command to compile and build the components.

```bash
./gradlew build
```

## Publish

If you want to publish the components packages in the lib repository then configure the github token values with the next commands.

```shell
export PUBLISHING_URL=maven_package_url # ex.: https://maven.pkg.github.com/dataspacedatalife/connector-libs
export PUBLISHING_USER=your_github_username
export PUBLISHING_PASSWORD=your_token
```

**The token must have Package WRITE permission**

Then, the next command publishes the packages.

```bash
./gradlew publish
```


## Dockerize
To generate the Docker images, execute:

 ```shell
 ./gradlew dockerize`
 ```

After the process completes, you can verify the images by running:

```shell
docker images
```

You should see output similar to the following:

```text
>> docker images
REPOSITORY                                                                                  TAG                   IMAGE ID       CREATED              SIZE
identity-hub                                                                                0.3.0                 4ec0b1176656   About a minute ago   245MB
identity-hub                                                                                latest                4ec0b1176656   About a minute ago   245MB
dataplane                                                                                   0.3.0                 61a63d86e88e   About a minute ago   244MB
dataplane                                                                                   latest                61a63d86e88e   About a minute ago   244MB
controlplane                                                                                0.3.0                 d9fc72b88524   About a minute ago   246MB
controlplane                                                                                latest                d9fc72b88524   About a minute ago   246MB
```




## Extensions

If extensions are developed, in **extensions/<-extension-name->** folder, then they should be added in **settings.gradle.kts**:

```sh
include(":extensions:<extension-name>")

``` 

In **launchers/<-component->/build.gradle.kts**, the developed extension is added to the dependecies of the component.

```sh
dependencies {
    runtimeOnly(project(":extensions:<extension-name>"))
}
```

And should they use other libraries, they should be defined in **gradle/libs.versions.toml**

```sh
[versions]
edc = "0.12.0"

[libraries]
# EDC Libraries used by extension
edc-dataplane-spi = { module = "org.eclipse.edc:data-plane-spi", version.ref = "edc"}
edc-util-lib = { module = "org.eclipse.edc:util-lib", version.ref = "edc"}
edc-dataplane-util = { module = "org.eclipse.edc:data-plane-util", version.ref = "edc"}
```

Later, when it's being built, the jar artifacts will possess the extended features.

### Examples

This repository includes several extension examples demonstrating different EDC capabilities:

#### 1. Printer Extensions (Basic SPI Implementation)

Two basic extensions demonstrating SPI implementation for printing messages:

**Terminal Printer Extension** (`terminal-printer-provider-extension`)
- Implements a `Printer` SPI that outputs messages to the terminal console
- Located in `extensions/terminal-printer-provider-extension/`
- Launcher: `launchers/terminal-printer/`

**File Printer Extension** (`file-printer-provider-extension`)
- Implements a `Printer` SPI that outputs messages to a file
- Configurable file path via environment variable: `EDC_PRINTER_FILE_PATH`
- Located in `extensions/file-printer-provider-extension/`
- Launcher: `launchers/file-printer/`

**Testing the Printer Extensions:**

After building with `./gradlew build`:

For terminal printer:
```shell
cd launchers/terminal-printer
java -jar build/libs/terminal-printer.jar
```
Verify **"Hello, World!"** is printed in the terminal.

For file printer:
```shell
cd launchers/file-printer
export EDC_PRINTER_FILE_PATH=file.txt
java -jar build/libs/file-printer.jar
```
Verify **"Hello, World!"** is written to `file.txt`.

---

#### 2. Data Plane Extension: File to Mail Transfer (`dataplane-demo-extension`)

A comprehensive Data Plane extension demonstrating custom data source and sink implementations for transferring files via email.

**Location:** `extensions/dataplane-demo-extension/`

**Purpose:** Enable data transfers from file sources to email destinations using SMTP.

**Key Components:**

1. **FileDataSource** - Custom data source implementation
   - Reads data from local files
   - Implements `DataSource` interface
   - Provides file streaming capabilities with proper media type detection
   - Supports part-based data streaming

2. **FileDataSourceFactory** - Factory for file sources
   - Supports type: `"File"`
   - Validates the `sourceFile` property in `DataFlowStartMessage`
   - Ensures file exists and is readable before transfer

3. **MailDataSink** - Custom data sink implementation
   - Sends data via email using SMTP protocol
   - Extends `ParallelSink` for efficient parallel processing
   - Supports attachments with proper media types
   - Creates MIME multipart messages with file attachments

4. **MailDataSinkFactory** - Factory for mail sinks
   - Supports type: `"Mail"`
   - Validates the `recipient` property in destination address
   - Configures SMTP session with authentication

5. **MyDataPlaneExtension** - Main extension class
   - Registers both factories with the `PipelineService`
   - Configures SMTP session (currently using ethereal.email for testing)
   - Manages email authentication

**Configuration:**

Required environment variables or EDC settings:

```properties
edc.dataplane.mail.sender=your-email@example.com
edc.dataplane.mail.password=your-smtp-password
```

**Dependencies** (from `build.gradle.kts`):
```kotlin
dependencies {
    implementation(libs.edc.dataplane.spi)
    implementation(libs.edc.util.lib)
    implementation(libs.edc.dataplane.util)
    implementation("com.sun.mail:jakarta.mail:2.0.2")
}
```

**Usage Example:**

When creating a data transfer request:

- **Source Data Address:**
  ```json
  {
    "type": "File",
    "sourceFile": "/path/to/file.pdf"
  }
  ```

- **Destination Data Address:**
  ```json
  {
    "type": "Mail",
    "recipient": "recipient@example.com"
  }
  ```

The extension will read the file and send it as an email attachment to the specified recipient. The email subject will be automatically generated as `"File transfer {processId}"`.

**Service Registration:**

Create `src/main/resources/META-INF/services/org.eclipse.edc.spi.system.ServiceExtension`:
```plaintext
org.datalife.edc.connector.dataplane.demo.MyDataPlaneExtension
```

---

#### 3. Policy Extension: Credential Validation (`policy-demo-extension`)

A Control Plane extension demonstrating policy enforcement based on Verifiable Credentials (VCs), specifically checking for MembershipCredential types.

**Location:** `extensions/policy-demo-extension/`

**Purpose:** Validate participant credentials during catalog access, contract negotiation, and data transfer processes.

**Key Components:**

1. **CredentialCheckFunction** - Policy evaluation function
   - Implements `AtomicConstraintRuleFunction<Permission, ParticipantAgentPolicyContext>`
   - Validates Verifiable Credentials in participant claims
   - Checks for specific credential types (e.g., `MembershipCredential`)
   - Evaluates membership types against policy constraints
   - Supports equality (`EQ`) operator for credential matching

2. **CredentialPolicyExtension** - Main extension class
   - Registers the credential check function with the `PolicyEngine`
   - Binds the `MembershipCredential.type` constraint to multiple scopes:
     - `catalog` - Controls catalog visibility
     - `contract.negotiation` - Enforces constraints during contract negotiation
     - `transfer.process` - Validates credentials during data transfer

**Policy Evaluation Logic:**

The function performs the following checks:
1. Verifies the participant has Verifiable Credentials in their claims (`vc` claim)
2. Ensures the VC list is not empty
3. Filters credentials by type (looking for credentials ending with `MembershipCredential`)
4. Extracts the `membership` claim from credential subjects
5. Compares the `membershipType` value against the policy's right operand

**Configuration:**

No specific configuration required. The extension automatically registers during EDC initialization.

**Dependencies** (from `build.gradle.kts`):
```kotlin
dependencies {
    implementation(libs.edc.dcp.core)
    implementation(libs.edc.spi.controlplane)
}
```

**Policy Definition Example:**

To enforce that only "FullMember" participants can access a resource:

```json
{
  "policy": {
    "permissions": [
      {
        "action": "use",
        "constraint": {
          "leftOperand": "MembershipCredential.type",
          "operator": "eq",
          "rightOperand": "FullMember"
        }
      }
    ]
  }
}
```

**Usage Contexts:**

1. **Catalog Policy** - Controls which assets are visible to participants based on their credentials
2. **Contract Negotiation Policy** - Enforces credential requirements when negotiating contracts
3. **Transfer Process Policy** - Validates credentials before initiating data transfers

**Service Registration:**

Create `src/main/resources/META-INF/services/org.eclipse.edc.spi.system.ServiceExtension`:
```plaintext
org.datalife.edc.connector.policy.demo.CredentialPolicyExtension
```

**Credential Structure Expected:**

The extension expects Verifiable Credentials with the following structure:
```json
{
  "type": ["VerifiableCredential", "MembershipCredential"],
  "credentialSubject": {
    "https://w3id.org/mvd/credentials/": {
      "membership": {
        "membershipType": "FullMember"
      }
    }
  }
}
```

---

### Extension Development Best Practices

1. **Always create the ServiceExtension registration file** in `META-INF/services/`
2. **Use dependency injection** with `@Inject` annotation for EDC services
3. **Implement proper validation** in factory classes before creating sources/sinks
4. **Handle errors gracefully** and return meaningful failure messages
5. **Document configuration settings** using `@Setting` annotation
6. **Test extensions independently** before integrating into launchers
7. **Follow EDC naming conventions** for supported types and properties