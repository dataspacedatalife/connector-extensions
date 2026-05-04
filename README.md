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
    gradiant_dataspace = "0.4.0" # change it to the version in use recent version

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

In the file **settings.gradle.kts** this should be present:

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

The second repository, *maven {...}* represents the github package repository from where the artifacts are obtained with the github access token credential.


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
export MAVEN_PKG_URL=maven_package_url # ex.: https://maven.pkg.github.com/dataspacedatalife/connector-libs
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
identity-hub                                                                                0.4.0                 4ec0b1176656   About a minute ago   245MB
identity-hub                                                                                latest                4ec0b1176656   About a minute ago   245MB
dataplane                                                                                   0.4.0                 61a63d86e88e   About a minute ago   244MB
dataplane                                                                                   latest                61a63d86e88e   About a minute ago   244MB
controlplane                                                                                0.4.0                 d9fc72b88524   About a minute ago   246MB
controlplane                                                                                latest                d9fc72b88524   About a minute ago   246MB
```

## Dataspace Datalife Connector

After creating the docker images, load them in the kind cluser

```sh
kind load docker-image identity-hub:latest dataplane:latest controlplane:latest -n mvd
```

Before the deployment of the participant make sure to change the values to reflect the changes in the components:
* connector/charts/participant/charts/controlplane/values.yaml
* connector/charts/participant/charts/identityhub/values.yaml
* connector/charts/participant/charts/dataplane/values.yaml
* connector/charts/participant/charts/participant-portal/values.yaml

```yml
deployment:
  replicaCount: 1
  imagePullSecret: harbor-regcred-usuarios # only necessary if repository is datalife harbor (harbor.gradiant.org/si-xdatashare-usuarios-pr-01616/controlplane)
  image:
    repository: docker.io/library/controlplane # local component with custom extension 
    tag: latest # component version
    pullPolicy: IfNotPresent
```

**Don't forget to add the configuration values required by the custom extensions.**

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
    "recipient": "recipient@ethereal.com"
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
   - Checks for specific credential types (e.g., `XdataShareMembershipCredential`)
   - Evaluates membership types against policy constraints
   - Supports equality (`EQ`) operator for credential matching

2. **CredentialPolicyExtension** - Main extension class
   - Registers the credential check function with the `PolicyEngine`
   - Binds the `XdataShareMembershipCredential.partner` constraint to multiple scopes:
     - `catalog` - Controls catalog visibility
     - `contract.negotiation` - Enforces constraints during contract negotiation
     - `transfer.process` - Validates credentials during data transfer

**Policy Evaluation Logic:**

The function performs the following checks:
1. Verifies the participant has Verifiable Credentials in their claims (`vc` claim)
2. Ensures the VC list is not empty
3. Filters credentials by type (looking for credentials ending with `XdataShareMembershipCredential`)
4. Extracts the `membership` claim from credential subjects
5. Compares the `id` value against the policy's right operand method `BusinessPartners` (from local json business partners DID list)

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
          "leftOperand": "XdataShareMembershipCredential.partner",
          "operator": "eq",
          "rightOperand": "BusinessPartners"
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


## Testing (Postman)

This repository includes a comprehensive Postman collection for end-to-end validation of EDC connector functionality, located at `postman/EDC End-to-End Validation Runbook.postman_collection.json`.

### Prerequisites

1. **Import the Postman Collection:**
   - Open Postman
   - Import `postman/EDC End-to-End Validation Runbook.postman_collection.json`
   - The collection includes pre-configured requests for the complete data sharing flow

2. **Configure Environment Variables:**

   Edit the collection variables (or create a Postman environment) with the following values:

   | Variable | Description | Example Value |
   |----------|-------------|---------------|
   | `PROVIDER_URL` | Provider connector base URL | `https://connector1.xdatashare.com` |
   | `CONSUMER_URL` | Consumer connector base URL | `https://dl-pharmacy1.srv.cesga.es` |
   | `PROVIDER_MANAGEMENT_URL` | Provider Management API endpoint | `https://connector1.xdatashare.com/controlplane/management/api/management/v3` |
   | `CONSUMER_MANAGEMENT_URL` | Consumer Management API endpoint | `https://dl-pharmacy1.srv.cesga.es/controlplane/management/api/management/v3` |
   | `PROVIDER_DSP_URL` | Provider Dataspace Protocol endpoint | `https://connector1.xdatashare.com/controlplane/dsp/api/dsp` |
   | `PROVIDER_API_KEY` | Provider Management API authentication key | `daksmdsadasd=` (base64 encoded) |
   | `CONSUMER_API_KEY` | Consumer Management API authentication key | `sasdasxsaefae=` (base64 encoded) |
   | `PROVIDER_DID` | Provider's Decentralized Identifier | `did:web:connector1.xdatashare.com:identityhub:did` |
   | `CONSUMER_DID` | Consumer's Decentralized Identifier | `did:web:dl-pharmacy1.srv.cesga.es:identityhub:did` |
   | `ASSET_ID` | Unique identifier for the asset | `asset-policy-test-active` |
   | `RECIPIENT_EMAIL` | Ethereal email address for file transfer | `recipient@ethereal.com` |
   | `POLICY_ID_ASSET_1` | Policy offer ID from catalog (manually fetched from catalog dataset during testing)| - |
   | `CONTRACT_AGREEMENT_ID` | Negotiated contract ID (manually fetched from contract list during testing) | - |

---

### End-to-End Testing Flow

#### **Phase 1: Provider Setup - Create Policies**

**Step 1: Create Access Policy (SUCCESS)**

The provider creates an access policy that validates participant credentials during catalog access.

```http
POST {{PROVIDER_MANAGEMENT_URL}}/policydefinitions
X-API-KEY: {{PROVIDER_API_KEY}}
```

**What's happening:**
- Creates a policy with ID `credential-policy-success`
- Requires participants to have an active `XDataShareMembershipCredential`
- This policy controls **who can see the asset** in the catalog
- Uses the custom `CredentialCheckFunction` extension

**Step 2: Create Contract Policy (Business Partner)**

The provider creates a contract policy that enforces business partner restrictions.

```http
POST {{PROVIDER_MANAGEMENT_URL}}/policydefinitions
X-API-KEY: {{PROVIDER_API_KEY}}
```

**What's happening:**
- Creates a policy with ID `credential-policy-business-partner`
- Checks if the participant's credential contains a business partner ID that matches the allowed list
- This policy controls **who can negotiate and use the asset**
- Uses the custom `BusinessPartnerCredentialCheckFunction` extension
- The extension reads business partner IDs from a file configured via `EDC_BUSINESS_PARTNERS_FILE` in controlplane

**Business Partners File Setup:**

The `BusinessPartnerCredentialCheckFunction` expects a JSON file with allowed DIDs:

```json
[
  "did:web:dl-pharmacy1.srv.cesga.es:identityhub:did"
]
```

This file should be accessible to the provider's controlplane container.

---

#### **Phase 2: Provider Setup - Create Asset**

**Step 3: Create Asset File in Data Plane**

Before creating the asset definition, you need to create the actual data file in the provider's data plane container.

**Option A: Using kubectl (for Kubernetes deployments)**

```bash
# Create the file directly in the data plane pod
kubectl exec -n <namespace> <provider-dataplane-pod-name> -- sh -c 'echo "Sample data content for testing" > /tmp/asset.txt'

# Verify the file was created
kubectl exec -n <namespace> <provider-dataplane-pod-name> -- ls -la /tmp/asset.txt
```

**Option B: Using docker exec (for Docker deployments)**

```bash
# Create the file in the data plane container
docker exec <provider-dataplane-container> sh -c 'echo "Sample data content for testing" > /tmp/asset.txt'

# Verify the file was created
docker exec <provider-dataplane-container> ls -la /tmp/asset.txt
```

**Option C: Copy existing file**

```bash
# Kubernetes
kubectl cp /path/to/local/file.txt <namespace>/<provider-dataplane-pod-name>:/tmp/asset.txt

# Docker
docker cp /path/to/local/file.txt <provider-dataplane-container>:/tmp/asset.txt
```

**Step 3.1: Create Asset Definition**

After the file exists in the data plane, create the asset definition in the control plane.

```http
POST {{PROVIDER_MANAGEMENT_URL}}/assets
X-API-KEY: {{PROVIDER_API_KEY}}
```

**What's happening:**
- Registers an asset with ID `asset-policy-test-active` (or your custom `{{ASSET_ID}}`)
- Points to the file created in the data plane at `/tmp/asset.txt`
- Uses the custom `File` data source type from the `dataplane-demo-extension`
- The asset metadata includes a description visible in the catalog

**Asset Configuration:**
```json
{
  "@id": "asset-policy-test-active",
  "properties": {
    "description": "Policy validation asset"
  },
  "dataAddress": {
    "type": "File",
    "sourceFile": "/tmp/asset.txt"
  }
}
```

---

#### **Phase 3: Provider Setup - Create Contract Definition**

**Step 4: Create Contract Definition**

Link the asset with the policies to create an offered contract.

```http
POST {{PROVIDER_MANAGEMENT_URL}}/contractdefinitions
X-API-KEY: {{PROVIDER_API_KEY}}
```

**What's happening:**
- Creates a contract definition with ID `contractdef-policy-test-active2`
- Links the access policy (`credential-policy-success`) for catalog visibility
- Links the contract policy (`credential-policy-business-partner`) for negotiation/usage
- Selects the specific asset using the `assetsSelector` criteria
- Makes the asset available for discovery and negotiation

**Policy Enforcement Flow:**
1. **Access Policy** → Evaluated when consumer requests catalog
2. **Contract Policy** → Evaluated during contract negotiation and transfer

---

#### **Phase 4: Consumer Actions - Discovery**

**Step 5: Consumer 1 - Catalog Query (Pharmacy1)**

The authorized consumer queries the provider's catalog.

```http
POST {{CONSUMER_MANAGEMENT_URL}}/catalog/request
X-API-KEY: {{CONSUMER_API_KEY}}
```

**What's happening:**
- Consumer (pharmacy1) requests the provider's catalog
- Provider evaluates the access policy against pharmacy1's credentials
- **Expected Result:** Asset appears in catalog because pharmacy1 has valid `XDataShareMembershipCredential`
- Response includes available datasets with their policies
- Copy the policy `@id` from the response to `POLICY_ID_ASSET_1` variable

**Step 5.1: Consumer 2 - Catalog Query (Pharmacy3)**

An unauthorized consumer attempts to query the catalog.

```http
POST https://dl-pharmacy3.srv.cesga.es/controlplane/management/api/management/v3/catalog/request
X-API-KEY: qwertyujnb456kjbv
```

**What's happening:**
- Consumer (pharmacy3) requests the provider's catalog
- Provider evaluates the access policy against pharmacy3's credentials
- **Expected Result (if not in business partners list):** Asset may appear in catalog (access policy passes) BUT negotiation will fail (contract policy fails)
- This demonstrates the difference between access policies and contract policies

---

**Note**: If user wants to test with a different `consumer 2` then it must be changed manually in the request.

#### **Phase 5: Consumer Actions - Contract Negotiation**

**Step 6: Consumer 1 - Contract Negotiation (Pharmacy1)**

Pharmacy1 initiates contract negotiation for the discovered asset.

```http
POST {{CONSUMER_MANAGEMENT_URL}}/contractnegotiations
X-API-KEY: {{CONSUMER_API_KEY}}
```

**What's happening:**
- Consumer sends a contract request to the provider
- Includes the policy from the catalog offer (`POLICY_ID_ASSET_1`)
- Provider evaluates `credential-policy-business-partner`
- Checks if pharmacy1's DID (`did:web:dl-pharmacy1.srv.cesga.es:identityhub:did`) is in the business partners file
- **Expected Result:** Negotiation succeeds if pharmacy1 is in the list, and contractAgreementId is present in request 7.

**Step 6.1: Consumer 2 - Contract Negotiation (Pharmacy3)**

Pharmacy3 attempts to negotiate a contract.

```http
POST https://dl-pharmacy3.srv.cesga.es/controlplane/management/api/management/v3/contractnegotiations
X-API-KEY: qwertyujnb456kjbv
```

**What's happening:**
- Consumer sends a contract request to the provider
- Provider evaluates `credential-policy-business-partner`
- Checks if pharmacy3's DID is in the business partners file
- **Expected Result:** Negotiation fails if pharmacy3 is NOT in the list, and contractAgreementId is not present in request 7.

---

**Note**: If user wants to test with a different `consumer 2` then it must be changed manually in the request.

#### **Phase 6: Consumer Actions - Retrieve Agreement**

**Step 7: Get Contract Agreement ID (Pharmacy1)**

After successful negotiation, retrieve the contract agreement ID.

```http
POST {{CONSUMER_MANAGEMENT_URL}}/contractnegotiations/request
X-API-KEY: {{CONSUMER_API_KEY}}
```

**What's happening:**
- Queries all contract negotiations for the consumer
- Finds completed negotiations in `FINALIZED` state
- Extracts the `contractAgreementId` from the response

User should manually search this result with the the IdResponse of the previous request, and if it exists, copy the contractAgreementId which will be used in the next query.

#### **Phase 7: Consumer Actions - Data Transfer**

**Step 8: Initiate Transfer (Mail-PUSH)**

Use the contract agreement to initiate a data transfer.

```http
POST {{CONSUMER_MANAGEMENT_URL}}/transferprocesses
X-API-KEY: {{CONSUMER_API_KEY}}
```

**What's happening:**
- Consumer initiates a transfer using the `CONTRACT_AGREEMENT_ID`
- Specifies `Mail-PUSH` as the transfer type (custom extension)
- Provides destination address with recipient email
- Provider's data plane:
  1. Reads the file from `/tmp/asset.txt` using `FileDataSource`
  2. Sends it via email using `MailDataSink`
  3. Uses the configured SMTP settings from the extension

**Transfer Request Configuration:**
```json
{
  "assetId": "asset-policy-test-active",
  "transferType": "Mail-PUSH",
  "dataDestination": {
    "type": "Mail",
    "recipient": "recipient@ethereal.com"
  }
}
```

---

#### **Phase 8: Verification**

**Step 9: List Transfer Processes**

Monitor the transfer process status.

```http
POST {{PROVIDER_MANAGEMENT_URL}}/transferprocesses/request
X-API-KEY: {{PROVIDER_API_KEY}}
```

**What's happening:**
- Provider lists all transfer processes
- Shows current state of each transfer
- Verify the transfer reached `DEPROVISIONED` state
- Check provider data plane logs for email sending confirmation

**Verify Email Delivery:**
- Check the recipient's message inbox for the email with attachment
- Email subject: `File transfer {processId}`
- Email contains the file from `/tmp/asset.txt` as an attachment

---

### Testing Different Scenarios

**Scenario 1: Authorized Consumer (Pharmacy1)**
- Has valid membership credential ✓
- DID is in business partners file ✓
- Expected: Full access (catalog → negotiate → transfer) ✓

**Scenario 2: Unauthorized Consumer (Pharmacy3)**
- Has valid membership credential ✓
- DID is NOT in business partners file ✗
- Expected: Can see catalog, but negotiation fails ✗

**Scenario 3: Multiple Business Partners**
- Add multiple DIDs to business partners JSON file:
  ```json
  [
    "did:web:dl-pharmacy1.srv.cesga.es:identityhub:did",
    "did:web:dl-pharmacy2.srv.cesga.es:identityhub:did"
  ]
  ```
- Both pharmacy1 and pharmacy2 can negotiate contracts




