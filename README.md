# Phenylalanine Calculator API

## How to Run the Project

### Required Software
- **Java 21** (preferably GraalVM)
- **Maven** (https://maven.apache.org/download.cgi)
- **Docker** (https://www.docker.com/)
- **Docker Compose** (https://docs.docker.com/compose/)

### Recommended IDE
**IntelliJ IDEA Ultimate** (https://www.jetbrains.com/idea/)  

## Configuring the Project

When running the project locally, you must provide configuration via a `.env` file and a `local-config` directory.  
These are ignored by Git to protect your credentials.

### 1. Docker Environment (`.env`)

Create a file named `.env` in the root directory:

```properties
# Database Credentials
MYSQL_DB=phenylalanine
MYSQL_USER=phenylalanine
MYSQL_PASS=your_super_secret_password
MYSQL_ROOT_PASS=your_super_secret_root_password

# Port mapping (Defaults to 3306 if omitted)
# ONLY add this if you are already running a local MySQL server on port 3306.
# Using 3307 (or another port) prevents port conflicts with your local installation.
EXTERNAL_MYSQL_PORT=3307
```

### 2. Local Config Directory (`local-config/`)

Create a directory named `local-config` in the project root.

Inside this folder, create the following two files:

#### `application-db.properties`

```properties
# Match the port to EXTERNAL_MYSQL_PORT in your .env
spring.datasource.url=jdbc:mysql://localhost:3306/phenylalanine
spring.datasource.username=phenylalanine
spring.datasource.password=your_super_secret_password
```

#### `application-secrets.properties`

```properties
# Secret key for JWT signing
jwt.secret.string=your_super_long_random_secret_key_here
```