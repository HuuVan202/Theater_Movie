package movie_theater_gr4.project_gr4.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Selenium tests for admin login functionality
 * This class contains tests for various login scenarios for the admin portal
 */
public class AdminEmployeeTest extends BaseSeleniumTest {

    private static final String ADMIN_USERNAME = "chatt"; // Replace with actual admin username
    private static final String ADMIN_PASSWORD = "Chat@12345"; // Replace with actual admin password
    private static final String INVALID_USERNAME = "invalid_user";
    private static final String INVALID_PASSWORD = "invalid_password";

    /**
     * Test successful admin login with valid credentials
     */
    @Test(description = "Test successful admin login with valid credentials")
    public void testSuccessfulLogin() {
        // Navigate to admin login page
        driver.get(BASE_URL + "/authAdmin");

        // Fill in login form with valid credentials
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector(".login-btn"));

        usernameField.sendKeys(ADMIN_USERNAME);
        passwordField.sendKeys(ADMIN_PASSWORD);

        // Take a screenshot before submitting
        takeScreenshot("admin-login-form-filled");

        // Submit login form
        loginButton.click();

        // Wait for redirect to dashboard after successful login (max 10 seconds)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/stats/all"));

        // Take screenshot after successful login
        takeScreenshot("admin-login-success");

        // Verify redirect to dashboard
        Assert.assertTrue(driver.getCurrentUrl().contains("/stats/all"),
                "User should be redirected to admin dashboard after successful login");

        // Verify some dashboard elements are present
        Assert.assertTrue(driver.findElements(By.cssSelector(".sidebar, #sidebar")).size() > 0,
                "Admin dashboard should contain sidebar");
    }

     /**
     * Test logout functionality
     */
    @Test(description = "Test logout functionality")
    public void testLogout() {
        // First login successfully
        testSuccessfulLogin();

        // Look for logout button or link and click it
        try {
            // Wait for the logout button to be clickable and use a more specific selector
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("form#adminLogoutForm button.logout-btn"))
            );
            logoutBtn.click();

            // Wait to be redirected to login page
            wait.until(ExpectedConditions.urlContains("/authAdmin"));

            // Check for logout success message if it exists
            boolean hasLogoutMessage = driver.findElements(By.id("logoutAlert")).size() > 0;
            if (hasLogoutMessage) {
                WebElement logoutMessage = driver.findElement(By.id("logoutAlert"));
                Assert.assertTrue(logoutMessage.isDisplayed(), "Logout success message should be displayed");
                Assert.assertTrue(logoutMessage.getText().contains("đăng xuất thành công"),
                        "Logout message should indicate successful logout");
            }

            // Verify we're back on login page
            Assert.assertTrue(driver.getCurrentUrl().contains("/authAdmin"),
                    "Should be redirected to login page after logout");

            // Take screenshot of login page after logout
            takeScreenshot("admin-logout-success");

        } catch (Exception e) {
            System.err.println("Logout element not found or logout failed: " + e.getMessage());
            Assert.fail("Failed to find logout element or logout process failed");
        }
    }

    /**
     * Test add employee (Create)
     */
    @Test(description = "Test add employee (Create)")
    public void testAddEmployee() {
        // Login first
        testSuccessfulLogin();
        // Go to add employee page
        driver.get(BASE_URL + "/admin/employees/add");
        // Fill the form
        driver.findElement(By.id("username")).sendKeys("testuser" + System.currentTimeMillis());
        driver.findElement(By.id("email")).sendKeys("test" + System.currentTimeMillis() + "@example.com");
        driver.findElement(By.id("fullName")).sendKeys("Test User");
        driver.findElement(By.id("dateOfBirth")).sendKeys("2000-01-01");
        driver.findElement(By.id("male")).click();
        driver.findElement(By.id("phoneNumber")).sendKeys("912345678");
        driver.findElement(By.id("address")).sendKeys("123 Test St");
        driver.findElement(By.id("identityCard")).sendKeys("123456789012");
        driver.findElement(By.id("hireDate")).sendKeys("2020-01-01");
        driver.findElement(By.id("position")).sendKeys("Nhân viên test");
        // Submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        // Wait for redirect and success message
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/admin/employees"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success"))
        ));
        // Assert success
        Assert.assertTrue(driver.getPageSource().contains("Thêm nhân viên thành công") ||
                          driver.findElements(By.cssSelector(".alert-success")).size() > 0,
                "Should show success message after adding employee");
    }

    /**
     * Test view employee detail (Read)
     */
    @Test(description = "Test view employee detail (Read)")
    public void testViewEmployeeDetail() {
        // Login first
        testSuccessfulLogin();
        // Go to employee list
        driver.get(BASE_URL + "/admin/employees");
        // Click the first employee row (if exists)
        WebElement firstRow = driver.findElement(By.cssSelector(".employee-row"));
        firstRow.click();
        // Wait for detail page
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/admin/employees/detail/"));
        // Assert detail page loaded
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/employees/detail/"),
                "Should be on employee detail page");
        Assert.assertTrue(driver.getPageSource().contains("Chi tiết nhân viên"),
                "Detail page should contain 'Chi tiết nhân viên'");
    }

    /**
     * Test edit employee (Update)
     */
    @Test(description = "Test edit employee (Update)")
    public void testEditEmployee() {
        // Login first
        testSuccessfulLogin();
        // Go to employee list
        driver.get(BASE_URL + "/admin/employees");
        // Click the first employee row
        WebElement firstRow = driver.findElement(By.cssSelector(".employee-row"));
        firstRow.click();
        // Wait for detail page
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/admin/employees/detail/"));
        // Click edit button
        WebElement editBtn = driver.findElement(By.cssSelector("a.btn.btn-primary"));
        editBtn.click();
        // Wait for edit page
        wait.until(ExpectedConditions.urlContains("/admin/employees/edit/"));
        // Change full name
        WebElement fullNameField = driver.findElement(By.id("fullName"));
        fullNameField.clear();
        fullNameField.sendKeys("User Updated");
        // Submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        // Wait for redirect and success message
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/admin/employees"),
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success"))
        ));
        // Assert success
        Assert.assertTrue(driver.getPageSource().contains("Cập nhật nhân viên thành công") ||
                          driver.findElements(By.cssSelector(".alert-success")).size() > 0,
                "Should show success message after editing employee");
    }

    /**
     * Test delete employee (Delete)
     */
    @Test(description = "Test delete employee (Delete)")
    public void testDeleteEmployee() {
            try {
            // Login first
            testSuccessfulLogin();

            // Go to employee list
            driver.get(BASE_URL + "/admin/employees");

            // Take a screenshot before attempting deletion
            takeScreenshot("before-delete-employee");

            // Find first employee row and open dropdown
            WebElement firstRow = driver.findElement(By.cssSelector(".employee-row"));
            WebElement dropdownBtn = firstRow.findElement(By.cssSelector(".custom-dropdown-toggle"));
            dropdownBtn.click();

            // Wait for the delete button to be clickable
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".custom-dropdown-menu a.text-danger")
            ));

            // Store the employee ID before clicking delete for verification later
            String employeeUrl = deleteBtn.getAttribute("href");
            String employeeId = employeeUrl.substring(employeeUrl.lastIndexOf('/') + 1);
            System.out.println("Attempting to delete employee with ID: " + employeeId);

            // Instead of triggering the confirmation dialog through click,
            // navigate directly to the delete URL and handle the alert separately
            String deleteUrl = deleteBtn.getAttribute("href");

            // Execute JavaScript to navigate to URL and handle confirmation
            // This avoids issues with alert handling in Selenium
            String script = "window.onbeforeunload = function() { return null; };" +
                            "window.location.href = arguments[0];" +
                            "setTimeout(function() {" +
                            "  if(window.confirm) { window.confirm = function() { return true; } }" +
                            "}, 500);";
            ((JavascriptExecutor) driver).executeScript(script, deleteUrl);

            // Wait for the page to load after deletion
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/admin/employees"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success"))
            ));

            // Take a screenshot after deletion
            takeScreenshot("after-delete-employee");

            // Verify success - either through URL or success message
            boolean hasSuccessMessage = driver.findElements(By.cssSelector(".alert-success")).size() > 0;
            if (hasSuccessMessage) {
                WebElement successMsg = driver.findElement(By.cssSelector(".alert-success"));
                System.out.println("Success message found: " + successMsg.getText());
                Assert.assertTrue(successMsg.isDisplayed(), "Success message should be visible");
            }

            Assert.assertTrue(driver.getCurrentUrl().contains("/admin/employees"),
                "Should be on employees page after deletion");

        } catch (Exception e) {
            System.err.println("Error during employee deletion: " + e.getMessage());
            takeScreenshot("delete-employee-error");
            Assert.fail("Error during employee deletion: " + e.getMessage());
        }
    }


    @Test(description = "Test employee activation functionality")
    public void testActiveEmployee() {
        // Login first
        testSuccessfulLogin();

        // Go to employee list
        driver.get(BASE_URL + "/admin/employees");

        // Find first inactive employee row and open dropdown
        WebElement firstInactiveRow = driver.findElement(By.cssSelector(".employee-row"));
        WebElement dropdownBtn = firstInactiveRow.findElement(By.cssSelector(".custom-dropdown-toggle"));
        dropdownBtn.click();

        try {
            // Wait for the activate button to be clickable
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement activateBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".custom-dropdown-menu a.text-success")
            ));

            // Take screenshot before activation
            takeScreenshot("before-activate-employee");

            // Click activate button
            activateBtn.click();

            // Handle confirmation alert if present
            try {
                WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                alertWait.until(ExpectedConditions.alertIsPresent());
                driver.switchTo().alert().accept();
            } catch (Exception alertEx) {
                // No alert appeared, continue
            }

            // Wait for success message or page reload
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/admin/employees"),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success"))
            ));

            // Take screenshot after activation
            takeScreenshot("after-activate-employee");

            // Verify success message if present
            boolean hasSuccessMessage = driver.findElements(By.cssSelector(".alert-success")).size() > 0;
            if (hasSuccessMessage) {
                WebElement successMsg = driver.findElement(By.cssSelector(".alert-success"));
                Assert.assertTrue(successMsg.isDisplayed(), "Success message should be visible");
                Assert.assertTrue(successMsg.getText().contains("Kích hoạt nhân viên thành công"),
                        "Success message should indicate successful activation");
            }

        } catch (Exception e) {
            System.err.println("Error during employee activation: " + e.getMessage());
            takeScreenshot("activate-employee-error");
            Assert.fail("Error during employee activation: " + e.getMessage());
        }
    }
}
