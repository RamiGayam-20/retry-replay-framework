****Retry & Replay Framework****
The Retry & Replay Framework is a Spring Boot application designed to manage failed transactions by allowing users to retry or replay them with configurable strategies. It provides a web interface for administrators to monitor and control transactions, schedules retries using Quartz Scheduler, sends email notifications on transaction outcomes, and secures access with Spring Security. The application uses an in-memory H2 database for persistence and is ideal for scenarios requiring robust transaction retry mechanisms.

**Features**
**Transaction Management:** Store and track transactions with statuses (FAILED, SUCCESS).
**Retry Mechanism:** Schedule retries with strategies like fixed delay or exponential backoff.
**Replay Functionality:** Manually replay transactions to reset and reprocess them.
**Email Notifications**: Send emails to administrators on transaction success or failure using Gmail SMTP.
**Security:** Restrict actions to ADMIN users, with read-only access for USER roles via HTTP Basic Authentication.
**Web Interface:** View transactions and perform actions using a Thymeleaf-based UI.
**Persistence:** Store transactions and retry metadata in an H2 in-memory database.
**Scheduling:** Use Quartz Scheduler for reliable retry job execution.

**Tech Stack**
Spring Boot: 3.2.4 (or latest compatible version)
Spring Security: 6.0+ for role-based access control
Spring Data JPA: For database operations
H2 Database: In-memory database for development and testing
Quartz Scheduler: For scheduling retry jobs
Spring Mail: For email notifications
Thymeleaf: For rendering the web UI
Maven: Build tool

**Prerequisites**
Java: 17 or higher
Maven: 3.6.0 or higher
Docker: Optional, for running MailHog (local SMTP server for testing)
Gmail Account: For email notifications (requires App Password)

**Setup Instructions**
1. Clone the Repository
_bash__
git clone https://github.com/your-repo/retry-replay-framework.git
cd retry-replay-framework_

2. Configure Application Properties
Edit _src/main/resources/application.properties_ with your Gmail credentials and other settings:

**properties**
# H2 Database
spring.datasource.url=jdbc:h2:mem:retrydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

# Email Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your16characterapppassword
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.notification.recipient=your-email@gmail.com

# Quartz Scheduler
spring.quartz.job-store-type=memory
spring.quartz.properties.org.quartz.threadPool.threadCount=5
spring.quartz.startup-delay=5s

# Logging
logging.level.com.example.retry=DEBUG
logging.level.org.springframework.security=DEBUG
Note: Replace your-email@gmail.com and your16characterapppassword with your Gmail address and App Password (generated without spaces). See  for instructions.

3. Generate an App Password
Enable 2-Factor Authentication (2FA) in your Google Account:
Go to myaccount.google.com > Security > 2-Step Verification.
Follow prompts to enable 2FA.
Generate an App Password:
Go to Security > App passwords.
Select App: Mail, Device: Other (e.g., "Spring Boot").
Copy the 16-character password (e.g., wxyzabcdefghijkl, no spaces).
Update spring.mail.password in application.properties with the App Password.
4. Build the Project
mvn clean install

6. Run the Application
mvn spring-boot:run

The application will start at http://localhost:8080.

Usage
Accessing the Application
Web UI: Open http://localhost:8080/transactions in a browser.
Credentials:
Admin: Username: admin, Password: admin123
Full access to view, retry, and replay transactions.
Non-Admin: Username: user, Password: userpass
Read-only access to /transactions/view.
H2 Console: Access http://localhost:8080/h2-console (no login required).
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (empty)
Key Endpoints
GET /transactions: View all transactions (admin-only).
GET /transactions/view: View transactions (admin and user).
POST /transactions/retry/{id}: Retry a transaction with a strategy (admin-only, e.g., strategy=FIXED).
POST /transactions/replay/{id}: Replay a transaction (admin-only).
Example Workflow
Insert Test Data (via H2 Console):
sql

Copy
INSERT INTO TRANSACTION (TRANSACTION_ID, SYSTEM_ID, TRANSACTION_TYPE, STATUS, PAYLOAD, RETRY_COUNT, LAST_ERROR, IS_STATEFUL)
VALUES ('TXN123', 'SYS1', 'PAYMENT', 'FAILED', '{}', 0, 'Network error', false);

INSERT INTO RETRY_METADATA (TRANSACTION_ID, STRATEGY, MAX_ATTEMPTS, INTERVAL_MS, IS_CIRCUIT_OPEN, LAST_ATTEMPT_TIME, RETRY_COUNT)
VALUES (1, 'FIXED', 3, 1000, false, 0, 0);
