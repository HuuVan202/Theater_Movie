package movie_theater_gr4.project_gr4.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Selenium tests for user login functionality
 */
public class UserTest extends BaseSeleniumTest {

    private static final String USER_USERNAME = "Test"; // Replace with actual user username
    private static final String USER_PASSWORD = "Test@123"; // Replace with actual user password

    @Test(description = "Test successful user login with valid credentials")
    public void testSuccessfulUserLogin() {
        driver.get(BASE_URL + "/auth");
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector(".login-btn"));

        usernameField.sendKeys(USER_USERNAME);
        passwordField.sendKeys(USER_PASSWORD);
        takeScreenshot("user-login-form-filled");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/"));
        takeScreenshot("user-login-success");
        Assert.assertTrue(driver.getCurrentUrl().contains("/"),
                "User should be redirected to home page after successful login");
    }

    @Test(description = "Test user login with invalid credentials")
    public void testUserLoginInvalidCredentials() {
        driver.get(BASE_URL + "/auth");
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector(".login-btn"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        takeScreenshot("user-login-invalid-credentials");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assert.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
    }


}

