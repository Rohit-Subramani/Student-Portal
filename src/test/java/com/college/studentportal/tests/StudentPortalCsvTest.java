package com.college.studentportal.tests;

import com.college.studentportal.support.PortalOptions;
import com.college.studentportal.tests.support.TestDataSet;
import com.college.studentportal.tests.support.TestRecord;
import com.college.studentportal.tests.support.WebDriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Selenium WebDriver suite of the Student Portal.
 *
 * <p>The whole suite is one TestNG test method driven by {@link TestDataSet}: every row of
 * {@code student_portal_testdata.csv} becomes exactly one test case, and the rows always execute
 * in file order ({@code TC01} .. {@code TC20}) without parallelism, so the data dependent
 * scenarios (register, duplicate email, sign in, profile edit, password reset) see the data left
 * behind by the earlier rows.</p>
 *
 * <p>The Spring Boot application is started once in this JVM with a random port and an in-memory
 * H2 database, then driven through a real Chrome browser.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentPortalCsvTest extends AbstractTestNGSpringContextTests {

    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(25);

    /**
     * The twenty CSV rows were written before the phone number and the address were added to
     * the registration form, so the data set carries no columns for them. The server requires
     * both fields, therefore the registration helper supplies this one fixed valid pair for
     * every row that fills the form. The CSV rows, the data provider and every assertion in
     * this class stay untouched.
     */
    private static final String VALID_PHONE_NUMBER = "9876543210";
    private static final String VALID_ADDRESS = "24 College Road, Bengaluru 560001";

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @DataProvider(name = "csvTestCases", parallel = false)
    public Object[][] csvTestCases() {
        List<TestRecord> records = TestDataSet.all();
        Object[][] data = new Object[records.size()][1];
        for (int index = 0; index < records.size(); index++) {
            data[index][0] = records.get(index);
        }
        return data;
    }

    @Test(dataProvider = "csvTestCases",
            description = "Runs every CSV driven scenario against the running Student Portal application")
    public void executeCsvDrivenTestCase(TestRecord record) {
        switch (record.getScenario()) {
            case REGISTER_VALID,
                 REGISTER_DUPLICATE_EMAIL,
                 REGISTER_EMPTY_NAME,
                 REGISTER_EMPTY_EMAIL,
                 REGISTER_INVALID_EMAIL,
                 REGISTER_WEAK_PASSWORD,
                 REGISTER_MISMATCH_PASSWORD,
                 REGISTER_EMPTY_DEPARTMENT,
                 REGISTER_EMPTY_GENDER,
                 REGISTER_NO_INTERESTS,
                 REGISTER_EMPTY_DOB,
                 REGISTER_EMPTY_PASSWORD -> verifyRegistration(record);
            case LOGIN_VALID,
                 LOGIN_WRONG_PASSWORD,
                 LOGIN_UNKNOWN_EMAIL -> verifyLogin(record);
            case PROFILE_EDIT -> verifyProfileEdit(record);
            case PASSWORD_RESET_REQUEST_KNOWN,
                 PASSWORD_RESET_REQUEST_UNKNOWN -> verifyResetTokenRequest(record);
            case PASSWORD_RESET_MISMATCH,
                 PASSWORD_RESET_SUCCESS -> verifyResetConfirmation(record);
            default -> Assert.fail(msg(record, "No automation step is implemented for this scenario."));
        }
    }

    // ------------------------------------------------------------------ scenarios

    /** Registration page: fills every widget and checks the server side answer. */
    private void verifyRegistration(TestRecord record) {
        open("/register");
        type("name", record.getName());
        type("email", record.getEmail());
        type("password", record.getPassword());
        type("confirmPassword", record.getConfirmPassword());
        type("phoneNumber", VALID_PHONE_NUMBER);
        type("address", VALID_ADDRESS);
        selectByVisibleText("department", record.getDepartment());
        selectGender(record.getGender());
        selectInterests(record.getInterestList());
        setDateInput("dateOfBirth", record.getDateOfBirth());
        click("registerButton");

        if (record.isExpectSuccess()) {
            assertSuccessMessage(record);
        } else {
            assertErrorMessage(record);
            assertOnPage(record, "/register");
        }
    }

    /** Login page: a valid account lands on the profile page, the other rows stay on the form. */
    private void verifyLogin(TestRecord record) {
        open("/login");
        type("email", record.getEmail());
        type("password", record.getPassword());
        click("loginButton");

        if (record.isExpectSuccess()) {
            wait.until(ExpectedConditions.urlContains("/profile"));
            String title = visible(By.id("pageTitle")).getText().trim();
            Assert.assertEquals(title, record.getExpectedMessage(),
                    msg(record, "Sign in with valid credentials did not open the profile page."));
        } else {
            assertErrorMessage(record);
            assertOnPage(record, "/login");
        }
    }

    /** Profile page: edits name, department and preferences, then reloads to prove persistence. */
    private void verifyProfileEdit(TestRecord record) {
        signIn(record.getEmail(), record.getPassword());

        String emailBeforeEdit = visible(By.id("profileEmail")).getText().trim();

        type("fullName", record.getName());
        selectByVisibleText("department", record.getDepartment());
        selectInterests(record.getInterestList());
        click("saveProfileButton");

        assertSuccessMessage(record);
        assertOnPage(record, "/profile");

        // A reload re-reads the profile from the H2 database: the edit must survive it.
        driver.navigate().refresh();
        visible(By.id("profileForm"));

        Assert.assertEquals(valueOf("fullName"), record.getName(),
                msg(record, "The edited full name was not persisted in the database."));
        Assert.assertEquals(selectedDepartment("department"), record.getDepartment(),
                msg(record, "The edited department was not persisted in the database."));
        Assert.assertEquals(sorted(selectedInterests()), sorted(record.getInterestList()),
                msg(record, "The edited interests were not persisted in the database."));
        Assert.assertEquals(visible(By.id("profileEmail")).getText().trim(), emailBeforeEdit,
                msg(record, "The email address must not change when the profile is saved."));
    }

    /** Reset step 1: a known email shows a locally generated demo token, an unknown one an error. */
    private void verifyResetTokenRequest(TestRecord record) {
        open("/reset-password");
        type("email", record.getEmail());
        click("requestResetButton");

        if (record.isExpectSuccess()) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("demoResetPanel")));
            String token = visible(By.id("demoResetToken")).getText().trim();
            Assert.assertFalse(token.isEmpty(),
                    msg(record, "The demo reset panel was shown without a token."));
            assertSuccessMessage(record);
        } else {
            assertErrorMessage(record);
            Assert.assertTrue(driver.findElements(By.id("demoResetPanel")).isEmpty(),
                    msg(record, "No demo token panel may appear for an unknown email address."));
        }
    }

    /** Reset step 2: uses a real demo token and checks mismatch rejection and the happy path. */
    private void verifyResetConfirmation(TestRecord record) {
        String token = requestDemoToken(record);

        type("resetToken", token);
        type("newPassword", record.getPassword());
        type("confirmNewPassword", record.getConfirmPassword());
        click("resetPasswordButton");

        if (record.isExpectSuccess()) {
            assertSuccessMessage(record);
            // The stored hash changed: the new password must now open the profile page.
            signIn(record.getEmail(), record.getPassword());
            String title = visible(By.id("pageTitle")).getText().trim();
            Assert.assertEquals(title, "My profile",
                    msg(record, "Signing in with the new password did not open the profile page."));
        } else {
            assertErrorMessage(record);
        }
    }

    // ------------------------------------------------------------------ page helpers

    private void signIn(String email, String password) {
        open("/login");
        type("email", email);
        type("password", password);
        click("loginButton");
        wait.until(ExpectedConditions.urlContains("/profile"));
    }

    /** Runs step 1 of the demo reset workflow and returns the token shown on the page. */
    private String requestDemoToken(TestRecord record) {
        open("/reset-password");
        type("email", record.getEmail());
        click("requestResetButton");
        WebElement token = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("demoResetToken")));
        String value = token.getText().trim();
        Assert.assertFalse(value.isEmpty(),
                msg(record, "Could not read a demo reset token before confirming the reset."));
        return value;
    }

    // ------------------------------------------------------------------ browser helpers

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        baseUrl = "http://localhost:" + port;
        driver = WebDriverFactory.createChromeDriver();
        wait = new WebDriverWait(driver, DEFAULT_WAIT);
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private void open(String path) {
        driver.get(baseUrl + path);
    }

    private void type(String id, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        WebElement element = visible(By.id(id));
        element.clear();
        element.sendKeys(value);
    }

    private void click(String id) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id(id))).click();
    }

    private void selectByVisibleText(String id, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        new Select(visible(By.id(id))).selectByVisibleText(text);
    }

    private void selectGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return;
        }
        String id = switch (gender.toLowerCase(Locale.ROOT)) {
            case "male" -> "gender-male";
            case "female" -> "gender-female";
            case "other" -> "gender-other";
            default -> throw new IllegalArgumentException("The CSV uses an unsupported gender: " + gender);
        };
        WebElement radio = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
        if (!radio.isSelected()) {
            radio.click();
        }
    }

    private void selectInterests(List<String> wanted) {
        for (String interest : PortalOptions.INTERESTS) {
            setCheckbox("interest-" + interest.toLowerCase(Locale.ROOT), wanted.contains(interest));
        }
    }

    private void setCheckbox(String id, boolean checked) {
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
    }

    /** Date inputs are filled through the DOM because Chrome renders them as three segments. */
    private void setDateInput(String id, String isoDate) {
        WebElement element = visible(By.id(id));
        String value = isoDate == null ? "" : isoDate;
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", element, value);
    }

    private String selectedDepartment(String selectId) {
        return new Select(visible(By.id(selectId))).getFirstSelectedOption().getText().trim();
    }

    private List<String> selectedInterests() {
        List<String> selected = new ArrayList<>();
        for (String interest : PortalOptions.INTERESTS) {
            WebElement checkbox =
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.id("interest-" + interest.toLowerCase(Locale.ROOT))));
            if (checkbox.isSelected()) {
                selected.add(interest);
            }
        }
        return selected;
    }

    private String valueOf(String id) {
        return visible(By.id(id)).getAttribute("value");
    }

    private WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ------------------------------------------------------------------ assertions

    private void assertSuccessMessage(TestRecord record) {
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        String actual = banner.getText().trim();
        Assert.assertEquals(actual, record.getExpectedMessage(),
                msg(record, "The success message on the page did not match the CSV expectation."));
    }

    private void assertErrorMessage(TestRecord record) {
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        List<String> messages = new ArrayList<>();
        for (WebElement item : banner.findElements(By.className("error-item"))) {
            messages.add(item.getText().trim());
        }
        Assert.assertFalse(messages.isEmpty(),
                msg(record, "The error banner was shown but contained no messages."));
        Assert.assertTrue(messages.contains(record.getExpectedMessage()),
                msg(record, "Expected the error message '" + record.getExpectedMessage()
                        + "' but the page reported " + messages + "."));
    }

    private void assertOnPage(TestRecord record, String path) {
        Assert.assertTrue(driver.getCurrentUrl().contains(path),
                msg(record, "Expected to stay on " + path + " but the browser is on "
                        + driver.getCurrentUrl() + "."));
    }

    private String msg(TestRecord record, String detail) {
        return "[" + record.getTestCaseId() + " " + record.getScenario() + "] "
                + record.getDescription() + " -> " + detail;
    }

    /** The CSV may list interests in any order, so the comparison is order insensitive. */
    private static List<String> sorted(List<String> values) {
        List<String> copy = new ArrayList<>(values);
        copy.sort(String::compareTo);
        return copy;
    }
}
