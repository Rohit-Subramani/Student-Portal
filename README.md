# StudentPortal

A small college project that contains **both** a Spring Boot web application and its
**Selenium WebDriver + TestNG** automation suite in the same Maven project.

| Item | Value |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 (Spring MVC + Thymeleaf) |
| Build | Maven |
| Database | H2 (in-memory, `jdbc:h2:mem:studentportal` — see [section 8](#8-where-student-data-is-stored)) |
| Views | Thymeleaf + HTML + CSS |
| Automation | Selenium WebDriver 4.27.0 + TestNG 7.10.2 |
| Test data | `src/test/resources/testdata/student_portal_testdata.csv` (20 rows) |

Passwords are stored as **PBKDF2-HMAC-SHA256 hashes** (salted, 120000 iterations), never as
plain text.

## 1. Features

| Area | Behaviour |
| --- | --- |
| Registration | Name, email, phone number, address, password, confirm password, department dropdown, gender radio buttons, interests checkboxes, date of birth |
| Server side validation | Empty fields, invalid email, invalid phone number (not exactly 10 digits), missing or over long address, weak password, mismatched passwords, missing department / gender / interests / date of birth |
| Duplicate email | A second registration with an existing email is rejected |
| Login / logout | Session based sign in and sign out |
| Profile | Editable name, department, phone number, address and preferences; changes are persisted and verified after a page reload |
| Password reset | **Local demo workflow only.** A one time token is generated and displayed on the page. **This application does not send real emails.** |
| Messages | Explicit success and error banners |
| Selectors | Stable HTML `id` attributes on every element the tests touch |

## 2. Project structure

```
StudentPortal/
├── pom.xml
├── testng.xml                                  # TestNG suite: single thread, controlled order
├── README.md
├── .gitignore
└── src
    ├── main
    │   ├── java/com/college/studentportal
    │   │   ├── StudentPortalApplication.java    # Spring Boot entry point
    │   │   ├── controller                       # AuthController, ProfileController, PasswordResetController, ...
    │   │   ├── model/User.java                  # JPA entity ("users" table)
    │   │   ├── repository/UserRepository.java
    │   │   ├── service                          # UserService, PasswordHasher, DuplicateEmailException
    │   │   ├── support                          # PortalOptions, SessionAttributes, FormErrors
    │   │   └── validation                       # RegistrationForm, LoginForm, ProfileForm, reset forms
    │   └── resources
    │       ├── application.properties
    │       ├── static/css/style.css
    │       └── templates                        # index, register, login, profile, reset-password, fragments
    └── test
        ├── java/com/college/studentportal/tests
        │   ├── StudentPortalCsvTest.java        # the CSV driven Selenium suite
        │   └── support                          # TestDataSet, CsvDataReader, TestRecord, Scenario, WebDriverFactory, ResultReporter
        └── resources/testdata/student_portal_testdata.csv
```

## 3. Prerequisites (Windows)

* **JDK 17** (`java -version` must report 17)
* **Apache Maven 3.8+** on `PATH`
* **Google Chrome** installed — Selenium Manager downloads the matching ChromeDriver
  automatically on the first run, so no driver binary has to be committed
* Internet access on the first build (Maven downloads the dependencies)

> If Maven is not on `PATH`, replace `mvn` in the commands below with the full path, for example
> `& "$env:USERPROFILE\Tools\apache-maven-3.9.9\bin\mvn.cmd"`.

## 4. Run the application

```powershell
# from the project root
mvn spring-boot:run
```

Then open <http://localhost:8080>.

To build a runnable jar instead:

```powershell
mvn clean package
java -jar target\studentportal-1.0.0.jar
```

Useful pages:

| Page | URL |
| --- | --- |
| Home | <http://localhost:8080/> |
| Register | <http://localhost:8080/register> |
| Login | <http://localhost:8080/login> |
| Profile | <http://localhost:8080/profile> |
| Reset password | <http://localhost:8080/reset-password> |
| H2 console | <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:studentportal`, user `sa`, empty password) |

## 5. Run the automation suite

The suite starts the Spring Boot application itself on a **random port** with a fresh in-memory
H2 database, then drives a real Chrome browser through Selenium.

```powershell
# headless (default)
mvn test

# watch the browser window while the tests run
mvn test -Dbrowser.headless=false

# run only one scenario type, e.g. everything that touches the reset page
mvn test -Dtest=StudentPortalCsvTest
```

### Results

| Output | Location |
| --- | --- |
| Individual result per CSV test case | `target/automation-results/individual-results.csv` |
| Summary (totals, duration, verdict) | `target/automation-results/summary.txt` |
| TestNG / Surefire HTML + text reports | `target/surefire-reports/` |

## 6. Test data and execution order

The suite executes **exactly 20 CSV driven test cases** read from
`src/test/resources/testdata/student_portal_testdata.csv` with the JDK file APIs
(`Files.newBufferedReader`), through a TestNG `@DataProvider`.

The rows are data dependent, so they run **sequentially in file order** (`TC01` … `TC20`):

* `TestDataSet` refuses to start if the file does not hold exactly 20 rows or if the ids are out
  of order.
* `testng.xml` sets `parallel="none" thread-count="1"`, the `@DataProvider` is `parallel = false`
  and the Surefire plugin is configured with `<parallel>none</parallel>`.

The CSV file was written before the registration form gained the **phone number** and the
**address**. It therefore has no columns for them, so the Selenium helper supplies one fixed valid
pair (`9876543210` / `24 College Road, Bengaluru 560001`) for every registration row that fills the
form. The 20 CSV rows, the data provider, the assertions and the expected messages are unchanged,
and the rows that intentionally leave fields empty still submit the same empty fields as before.

| Id | Scenario | What it proves |
| --- | --- | --- |
| TC01 | `REGISTER_VALID` | Full registration with every widget (dropdown, radios, checkboxes, date picker) |
| TC02 | `REGISTER_DUPLICATE_EMAIL` | Duplicate email addresses are rejected |
| TC03 | `REGISTER_EMPTY_NAME` | Empty field validation |
| TC04 | `REGISTER_EMPTY_EMAIL` | Empty field validation |
| TC05 | `REGISTER_INVALID_EMAIL` | Invalid email validation |
| TC06 | `REGISTER_WEAK_PASSWORD` | Weak password validation |
| TC07 | `REGISTER_MISMATCH_PASSWORD` | Password confirmation mismatch |
| TC08 | `REGISTER_EMPTY_DEPARTMENT` | Dropdown validation |
| TC09 | `REGISTER_EMPTY_GENDER` | Radio button validation |
| TC10 | `REGISTER_NO_INTERESTS` | Checkbox validation |
| TC11 | `REGISTER_EMPTY_DOB` | Date picker validation |
| TC12 | `REGISTER_EMPTY_PASSWORD` | Empty password validation |
| TC13 | `LOGIN_VALID` | Successful login |
| TC14 | `LOGIN_WRONG_PASSWORD` | Wrong password is refused |
| TC15 | `LOGIN_UNKNOWN_EMAIL` | Unknown email is refused |
| TC16 | `PROFILE_EDIT` | Name, department and preferences persist after a reload; email is unchanged |
| TC17 | `PASSWORD_RESET_REQUEST_KNOWN` | Demo token is generated and displayed (no email is sent) |
| TC18 | `PASSWORD_RESET_REQUEST_UNKNOWN` | Unknown email gets no token |
| TC19 | `PASSWORD_RESET_MISMATCH` | Mismatched new passwords are rejected |
| TC20 | `PASSWORD_RESET_SUCCESS` | Reset succeeds and the new password can sign in |

## 7. Notes on the demo password reset

1. `POST /reset-password/request` looks the account up and generates an 8 character token
   (valid 30 minutes) which the application **prints on the page**.
2. `POST /reset-password/confirm` consumes that token and stores a new PBKDF2 hash.

No SMTP server, mail queue or third party mail API is used anywhere in this project.

## 8. Where student data is stored

This section documents what the code actually configures in
`src/main/resources/application.properties`; nothing below is assumed.

| Setting | Value in this project | What it means |
| --- | --- | --- |
| Database technology | H2, driver `org.h2.Driver` | An embedded Java database, nothing to install separately |
| JDBC URL | `jdbc:h2:mem:studentportal;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` | `mem:` means **in memory** |
| User name / password | `sa` / empty | Local development defaults, not real secrets |
| Schema handling | `spring.jpa.hibernate.ddl-auto=create-drop` | Tables are created at start up and dropped at shut down |
| H2 console | `spring.h2.console.enabled=true`, `spring.h2.console.path=/h2-console` | Built in, local inspection page |

### The database is in-memory, not file-based

The JDBC URL starts with `jdbc:h2:mem:`, so the database exists **only inside the memory of the
running application process**. There is **no database file on disk**, so there is no folder to
open and no `.mv.db` file to look for. The `DB_CLOSE_DELAY=-1` part only keeps the in-memory
database alive while the JVM is running even if the last connection closes; it does **not** make
the data persist. The database mode was **not** changed for this documentation.

### What happens to registered data when the application stops

The data is **lost**. When the application stops (Ctrl+C, closing the IDE, killing the process) the
in-memory database disappears with the process, and `create-drop` also drops the tables on a clean
shutdown. The next start begins again with an empty `users` table. For a local college demo this is
intentional: every run starts from a clean state.

Note that the automated test run is a **separate** Spring Boot instance on a random port with its
own fresh in-memory database, so running `mvn test` neither depends on nor changes the data you see
while the application is running normally.

### How a registration is mapped to the entity and saved

1. `templates/register.html` posts the form to `POST /register`.
2. Spring binds the request to `validation/RegistrationForm.java` and runs Bean Validation there
   (`@NotBlank`, `@Email`, `@Pattern` for the phone number, `@Size` for the address, ...). If
   anything is invalid the page is re-rendered with the message under the matching field and
   **nothing is written to the database**.
3. `controller/AuthController.java` calls `service/UserService.java#register(...)`, which trims the
   values, hashes the password with PBKDF2-HMAC-SHA256 (`service/PasswordHasher.java`) and builds a
   `User` entity. The phone number is kept as text so a leading zero survives; the address is stored
   without leading or trailing whitespace.
4. `model/User.java` is the JPA entity: `@Entity @Table(name = "users")`, with one `@Column` per
   field — `full_name`, `email`, `password_hash`, `department`, `gender`, `interests`,
   `date_of_birth`, `phone_number`, `address`, `reset_token`, `reset_token_created_at`,
   `created_at`.
5. `repository/UserRepository.java` extends `JpaRepository<User, Long>`. `userRepository.save(user)`
   is the call that makes Hibernate issue the `INSERT INTO users (...)` that H2 stores.
6. The profile page reads the same row back with `userRepository.findById(...)`, which is why an
   edit is still visible after a reload.

`phone_number` and `address` are declared nullable in the entity **on purpose**: rows created before
those fields existed stay valid. "Required" is enforced by the registration and profile forms, not
by the column.

### Showing the database to a teacher

1. Start the application: `mvn spring-boot:run`
2. Register one account at <http://localhost:8080/register>, for example with the email
   `demo.student@college.edu`.
3. Open <http://localhost:8080/h2-console> (the console is part of the running application itself,
   so it is only reachable on your own machine). Fill in the login form with:
   * **Driver Class**: `org.h2.Driver`
   * **JDBC URL**: `jdbc:h2:mem:studentportal`
   * **User Name**: `sa`
   * **Password**: leave it empty
4. Run this query:

   ```sql
   SELECT id, full_name, email, phone_number, address, department, created_at FROM users;
   ```

   The row you just registered appears, which shows exactly where the data is kept. The query
   deliberately does **not** select `password_hash` or `reset_token`, and that output is never
   pasted anywhere: those are authentication secrets, not demonstration material.
5. Refresh <http://localhost:8080/profile> to show that the same values are read back from that row,
   and restart the application to show that the table is empty again (the in-memory database does
   not survive a restart).

This project does not expose the console on the network and does not weaken any security setting to
make the demonstration easier. One consequence of an in-memory database is that an **external**
database tool (H2 Shell, DBeaver, a second JDBC connection) cannot attach to it, because the
database belongs to the private memory of the application process: the console served by that same
process is the supported way to look inside.
