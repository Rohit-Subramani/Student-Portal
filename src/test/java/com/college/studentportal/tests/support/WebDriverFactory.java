package com.college.studentportal.tests.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Creates the browser used by the suite.
 *
 * <p>Selenium Manager (built into Selenium 4) resolves the matching ChromeDriver, so no driver
 * binary has to be committed to the repository. Run with {@code -Dbrowser.headless=false} to watch
 * the tests in a visible browser window.</p>
 */
public final class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("browser.headless", "true"));
    }

    public static WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        if (isHeadless()) {
            options.addArguments("--headless=new");
        }
        WebDriver driver = new ChromeDriver(options);
        // Only explicit waits are used by the suite, so the implicit wait stays at zero.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(20));
        return driver;
    }
}
