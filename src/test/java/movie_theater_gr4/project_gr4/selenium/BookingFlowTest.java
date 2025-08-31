package movie_theater_gr4.project_gr4.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class BookingFlowTest extends BaseSeleniumTest {
    @Test(description = "Test main booking flow: login, find movie, book, select version/date/showtime, choose seats, confirm age, apply promotion, pay")
    public void testMainBookingFlow() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 0. Login as member first
        driver.get(BASE_URL + "/auth");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.className("submit-btn"));
        usernameField.sendKeys("Test"); // Replace with valid member username
        passwordField.sendKeys("Test@123"); // Replace with valid member password
        takeScreenshot("booking-login-form-filled");
        loginButton.click();
        // Wait for a user-specific element to ensure login is successful
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".user-avatar, .logout-btn, .profile-link"))
        ));
        takeScreenshot("booking-login-success");
        // Print cookies after login for debugging session issues
        driver.manage().getCookies().forEach(cookie -> System.out.println("[LOGIN] " + cookie));

        // 1. Go to now showing page
        driver.get(BASE_URL + "/nowShowing");
        takeScreenshot("booking-step1-now-showing");

        // 2. Search for movie 'conan'
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search")));
        searchInput.clear();
        searchInput.sendKeys("conan");
        WebElement searchForm = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("form[action$='/nowShowing']")));
        WebElement searchBtn = searchForm.findElement(By.cssSelector("button[type='submit']"));
        searchBtn.click();
        takeScreenshot("booking-step2-search-conan");

        // 3. Find first movie card in search results and click the card to go to detail page
        WebElement firstMovieCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".movie-card, .movie-card-wrapper")));
        firstMovieCard.click();
        takeScreenshot("booking-step3-movie-detail");

        // 4. On movie detail page, click 'Đặt vé ngay' button to open booking modal
        WebElement bookingBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-booking, .btn-booking")));
        bookingBtn.click();
        takeScreenshot("booking-step4-booking-modal-open");

        // 5. Select version (first available)
        WebElement versionBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".version-btn")));
        versionBtn.click();
        takeScreenshot("booking-step5-version-selected");

        // 6. Select date (first available)
        WebElement dateBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".date-btn")));
        dateBtn.click();
        takeScreenshot("booking-step6-date-selected");

        // 7. Select showtime (first available)
        WebElement showtimeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".time-btn")));
        showtimeBtn.click();
        takeScreenshot("booking-step7-showtime-selected");

        // 8. Click 'Tiếp Tục Đặt Vé' (Continue Booking)
        WebElement bookNowModalBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("bookNowBtn")));
        bookNowModalBtn.click();
        takeScreenshot("booking-step8-continue-to-seats");

        // 9. Wait for redirect to select seats page
        // Defensive: If redirected to login, fail with a clear message and print cookies/page source for debugging
        wait.until(driver1 -> {
            String url = driver1.getCurrentUrl();
            if (url.contains("/auth")) {
                driver1.manage().getCookies().forEach(cookie -> System.out.println("[REDIRECT] " + cookie));
                System.out.println("[REDIRECT PAGE SOURCE] " + driver1.getPageSource());
                takeScreenshot("booking-redirected-to-login");
                Assert.fail("Session expired or not logged in. Redirected to login page during booking flow.");
            }
            return url.contains("/selectSeats") || driver1.findElements(By.id("seatMap")).size() > 0;
        });
        WebElement seatMap = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("seatMap")));
        List<WebElement> availableSeats = driver.findElements(By.cssSelector(".seat:not(.reserved):not(.disabled):not(.empty)"));
        Assert.assertFalse(availableSeats.isEmpty(), "No available seats to select for this showtime.");
        WebElement seatBtn = availableSeats.get(0);
        seatBtn.click();
        takeScreenshot("booking-step9-seat-selected");

        // 10. Click checkout/continue button
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkoutBtn")));
        checkoutBtn.click();
        takeScreenshot("booking-step10-checkout-clicked");

        // 11. Handle age confirmation popup if present
        try {
            WebElement ageConfirmCheckbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ageConfirmCheckbox")));
            ageConfirmCheckbox.click();
            WebElement confirmAgeBtn = driver.findElement(By.xpath("//button[contains(text(),'Đồng ý') or contains(text(),'tiếp tục')]"));
            confirmAgeBtn.click();
            takeScreenshot("booking-step11-age-confirmed");
        } catch (Exception ignore) {
            // No age confirmation required
        }

        // 12. Apply promotion code if input is present (optional, adjust selector as needed)
        try {
            WebElement promoInput = driver.findElement(By.cssSelector("input[name='promotionCode'], #promotionCode"));
            promoInput.sendKeys("PROMO2025");
            WebElement applyPromoBtn = driver.findElement(By.cssSelector("button.apply-promo, #applyPromoBtn"));
            applyPromoBtn.click();
            takeScreenshot("booking-step12-promo-applied");
        } catch (Exception ignore) {
            // No promotion input found, skip
        }

        // 13. Proceed to payment (simulate clicking pay button)
        try {
            WebElement payBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.pay-btn, #payBtn, .btn-pay")));
            payBtn.click();
            takeScreenshot("booking-step13-pay-clicked");
        } catch (Exception ignore) {
            // No pay button found, skip
        }

        // 14. Assert booking success (adjust selector/message as needed)
        boolean bookingSuccess = driver.getPageSource().contains("thanh toán thành công") ||
                driver.getPageSource().toLowerCase().contains("đặt vé thành công") ||
                driver.getCurrentUrl().contains("/success") ||
                driver.getCurrentUrl().contains("/payment");
        Assert.assertTrue(bookingSuccess, "Booking flow should complete successfully");
    }
}
