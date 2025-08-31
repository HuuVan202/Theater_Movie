package movie_theater_gr4.project_gr4.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Base class for Selenium tests that provides common functionality
 */
public class BaseSeleniumTest {

    protected WebDriver driver;
    protected final String BASE_URL = "http://localhost:8080"; // Change this to your application's base URL


    @BeforeMethod
    public void setUp() {
        // Initialize WebDriver using WebDriverSetup helper
        driver = WebDriverSetup.setupChromeDriver(false);
        System.out.println("Starting test with Chrome browser");
    }


    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }


    /**
     * Wait for page to completely load
     */
    protected void waitForPageToLoad() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("return document.readyState").equals("complete");

        try {
            // Give a small pause for any animations or delayed rendering
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Take screenshot and save to specified path
     *
     * @param fileName file name for the screenshot
     */
    protected void takeScreenshot(String fileName) {
        try {
            org.openqa.selenium.OutputType<byte[]> source = org.openqa.selenium.OutputType.BYTES;
            byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(source);

            Path path = Paths.get("test-screenshots", fileName + ".png");
            Files.createDirectories(path.getParent());
            Files.write(path, screenshot);

            System.out.println("Screenshot saved to: " + path.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }
}
