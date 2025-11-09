package com.medcloud.app.domain.service;

import com.medcloud.app.domain.dto.CaptchaResponseDTO;
import com.medcloud.app.domain.dto.EpsValidationResponseDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;
import org.openqa.selenium.support.ui.Select;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class AdresValidationService {

    private static final String ADRES_URL = "https://aplicaciones.adres.gov.co/bdua_internet/Pages/ConsultarAfiliadoWeb.aspx";
    private WebDriver driver;
    private WebDriverWait wait;

    @PostConstruct
    public void init() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    /**
     * Creates an SSL context that trusts all certificates.
     * This is used to bypass SSL certificate validation for the ADRES domain.
     *
     * @return SSLContext configured to trust all certificates
     * @throws NoSuchAlgorithmException if TLS is not available
     * @throws KeyManagementException if key management fails
     */
    private SSLContext createTrustAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }

    @PreDestroy
    public void destroy() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Initiates a validation request by filling document type and number, then fetching the captcha image and returning it with a session ID.
     *
     * @param tipoDocumento the document type
     * @param numeroDocumento the document number
     * @return CaptchaResponseDTO containing the captcha image in base64 and session ID
     */
    public CaptchaResponseDTO initiateValidation(String tipoDocumento, String numeroDocumento) {
        String sessionId = UUID.randomUUID().toString();
        try {
            driver.get(ADRES_URL);
            log.info("Navigated to ADRES URL, page title: {}", driver.getTitle());

            // Fill document type and number before fetching captcha
            // Busca el elemento por su ID, que parece ser 'tipoDoc'
            WebElement tipoDocumentoSelect = driver.findElement(By.id("tipoDoc"));
            Select tipoDocSelect = new Select(tipoDocumentoSelect);
            tipoDocSelect.selectByVisibleText(tipoDocumento); // O selectByValue() si tipoDocumento es el valor interno
            WebElement numeroDocumentoInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtNumDoc")));

            // Wait a bit for stability and re-locate elements
            try {
                Thread.sleep(500); // Small delay for page stability
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            //tipoDocumentoSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("tipoDoc")));
            numeroDocumentoInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtNumDoc")));

            //tipoDocumentoSelect.clear();
            //tipoDocumentoSelect.sendKeys(tipoDocumento);
            //numeroDocumentoInput.clear();
            numeroDocumentoInput.sendKeys(numeroDocumento);
            log.info("Form fields filled in initiate: tipoDocumento='{}', numeroDocumento='{}'", tipoDocumento, numeroDocumento);

            // Comprehensive debugging logging
            log.info("=== DEBUGGING PAGE CONTENT ===");
            log.info("Current URL: {}", driver.getCurrentUrl());

            // List all img elements
            var imgElements = driver.findElements(By.tagName("img"));
            log.info("Found {} img elements on the page:", imgElements.size());
            for (int i = 0; i < imgElements.size(); i++) {
                WebElement img = imgElements.get(i);
                log.info("Img[{}]: src='{}', id='{}', class='{}', alt='{}', width='{}', height='{}'",
                    i, img.getAttribute("src"), img.getAttribute("id"), img.getAttribute("class"),
                    img.getAttribute("alt"), img.getAttribute("width"), img.getAttribute("height"));
            }

            // Detect iframes
            var iframeElements = driver.findElements(By.tagName("iframe"));
            log.info("Found {} iframe elements on the page:", iframeElements.size());
            for (int i = 0; i < iframeElements.size(); i++) {
                WebElement iframe = iframeElements.get(i);
                log.info("Iframe[{}]: src='{}', id='{}', name='{}', width='{}', height='{}'",
                    i, iframe.getAttribute("src"), iframe.getAttribute("id"), iframe.getAttribute("name"),
                    iframe.getAttribute("width"), iframe.getAttribute("height"));
            }

            // Inspect page source for captcha-related content
            String pageSource = driver.getPageSource();
            log.info("Page source length: {} characters", pageSource.length());

            // Search for captcha-related patterns in page source
            if (pageSource.contains("captcha") || pageSource.contains("Captcha") || pageSource.contains("CAPTCHA")) {
                log.info("Page source contains 'captcha' (case-insensitive)");
            } else {
                log.warn("Page source does NOT contain 'captcha' (case-insensitive)");
            }

            // Look for specific selectors in page source
            String[] captchaSelectors = {"Capcha_CaptchaImageUP", "captcha", "Captcha", "CAPTCHA"};
            for (String selector : captchaSelectors) {
                if (pageSource.contains(selector)) {
                    log.info("Page source contains selector pattern: '{}'", selector);
                }
            }

            // Log a snippet of the page source around potential captcha areas
            int captchaIndex = pageSource.toLowerCase().indexOf("captcha");
            if (captchaIndex != -1) {
                int start = Math.max(0, captchaIndex - 200);
                int end = Math.min(pageSource.length(), captchaIndex + 200);
                String snippet = pageSource.substring(start, end);
                log.info("Page source snippet around 'captcha': ...{}...", snippet.replaceAll("\\s+", " "));
            }

            log.info("=== END DEBUGGING PAGE CONTENT ===");

            // Wait for captcha image to be present
            WebElement captchaImg = null;
            String captchaSrc = null;
            try {
                // Try different selectors for captcha image
                String[] selectors = {"#Capcha_CaptchaImageUP", "img[src*='captcha']", "img[alt*='captcha']", "img[id*='captcha']", "img[class*='captcha']"};
                log.info("Attempting to find captcha image with {} selectors: {}", selectors.length, String.join(", ", selectors));
                for (String selector : selectors) {
                    try {
                        log.debug("Trying selector: {}", selector);
                        captchaImg = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                        log.info("Captcha image located successfully with selector: {}", selector);
                        // Immediately get attributes to prevent stale reference
                        captchaSrc = captchaImg.getAttribute("src");
                        String captchaId = captchaImg.getAttribute("id");
                        String captchaClass = captchaImg.getAttribute("class");
                        String captchaAlt = captchaImg.getAttribute("alt");
                        log.info("Captcha image details: src='{}', id='{}', class='{}', alt='{}'",
                            captchaSrc, captchaId, captchaClass, captchaAlt);
                        break;
                    } catch (Exception e) {
                        log.debug("Captcha image not found with selector: {}, trying next", selector);
                    }
                }
                if (captchaImg == null) {
                    log.error("No captcha image found with any of the {} selectors", selectors.length);
                    throw new Exception("No captcha image found with any selector");
                }
            } catch (Exception e) {
                log.error("Captcha image not found after waiting: {}", e.getMessage());
                return new CaptchaResponseDTO(null, sessionId, "Captcha image not found");
            }

            log.info("Captcha image src: {}", captchaSrc);
            log.info("Captcha src starts with 'data:': {}", captchaSrc.startsWith("data:"));

            byte[] captchaImageBytes;
            if (captchaSrc.startsWith("data:")) {
                // Handle data URL (base64 encoded image)
                log.info("Processing data URL captcha image");
                String base64Data = captchaSrc.split(",")[1];
                captchaImageBytes = Base64.getDecoder().decode(base64Data);
                log.info("Decoded data URL captcha image, size: {} bytes", captchaImageBytes.length);
            } else {
                // Handle regular URL
                log.info("Processing regular URL captcha image");
                captchaImageBytes = fetchCaptchaImage(captchaSrc);
                if (captchaImageBytes != null) {
                    log.info("Fetched captcha image from URL, size: {} bytes", captchaImageBytes.length);
                } else {
                    log.warn("Failed to fetch captcha image from URL");
                }
            }

            if (captchaImageBytes == null) {
                log.error("Captcha image bytes are null, cannot proceed");
                return new CaptchaResponseDTO(null, sessionId, "Failed to fetch captcha image");
            }

            String captchaImageBase64 = Base64.getEncoder().encodeToString(captchaImageBytes);
            log.info("Encoded captcha image to base64, length: {} characters", captchaImageBase64.length());
            return new CaptchaResponseDTO(captchaImageBase64, sessionId, null);
        } catch (Exception e) {
            log.error("Error initiating validation", e);
            return new CaptchaResponseDTO(null, sessionId, "Error initiating validation: " + e.getMessage());
        }
    }

    /**
     * Validates the EPS by submitting the form with the provided captcha solution.
     *
     * @param sessionId the session ID from the initiation
     * @param captchaSolution the user's captcha solution
     * @param tipoDocumento the document type
     * @param numeroDocumento the document number
     * @return EpsValidationResponseDTO containing validation result
     */
    public EpsValidationResponseDTO validateEps(String sessionId, String captchaSolution, String tipoDocumento, String numeroDocumento) {
        try {
            log.info("Starting EPS validation for session: {}, documento: {}", sessionId, numeroDocumento);

            // Additional debugging for form submission
            log.info("=== DEBUGGING FORM SUBMISSION ===");
            log.info("Current URL before form fill: {}", driver.getCurrentUrl());
            log.info("Page title before form fill: {}", driver.getTitle());

            // List all form elements
            var formElements = driver.findElements(By.cssSelector("input, select, textarea"));
            log.info("Found {} form elements on the page:", formElements.size());
            for (int i = 0; i < formElements.size(); i++) {
                WebElement elem = formElements.get(i);
                log.info("FormElement[{}]: tag='{}', name='{}', id='{}', type='{}', value='{}'",
                    i, elem.getTagName(), elem.getAttribute("name"), elem.getAttribute("id"),
                    elem.getAttribute("type"), elem.getAttribute("value"));
            }

            // Wait for and fill the form fields with multiple selector fallbacks
            WebElement captchaInput = null;
            String[] captchaSelectors = {"input[name='captcha']", "input[id*='captcha']", "input[name*='captcha']", "#Capcha_CaptchaTextBox", "input[type='text'][placeholder*='captcha']"};
            for (String selector : captchaSelectors) {
                try {
                    captchaInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                    log.info("Captcha input found with selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Captcha input not found with selector: {}", selector);
                }
            }
            if (captchaInput == null) {
                throw new Exception("Captcha input field not found with any selector");
            }

            // Re-locate elements after potential page changes
            WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConsultar")));
            log.info("Form elements located and ready for input");

            try {
                // Immediately get attributes to prevent stale reference
                String captchaName = captchaInput.getAttribute("name");
                String captchaId = captchaInput.getAttribute("id");
                String captchaType = captchaInput.getAttribute("type");
                String submitTag = submitButton.getTagName();
                String submitType = submitButton.getAttribute("type");
                String submitValue = submitButton.getAttribute("value");
                log.info("Captcha input element: name='{}', id='{}', type='{}'", captchaName, captchaId, captchaType);
                log.info("Submit button: tag='{}', type='{}', value='{}'", submitTag, submitType, submitValue);

                captchaInput.clear();
                captchaInput.sendKeys(captchaSolution);
                log.info("Form fields filled: captcha='{}'", captchaSolution.replaceAll(".", "*"));
            } catch (StaleElementReferenceException e) {
                log.warn("Stale element reference detected during form filling, re-locating elements");
                // Re-locate elements
                captchaInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#Capcha_CaptchaTextBox")));
                submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConsultar")));

                // Retry form filling
                captchaInput.clear();
                captchaInput.sendKeys(captchaSolution);
                log.info("Form fields re-filled after stale element recovery: captcha='{}'", captchaSolution.replaceAll(".", "*"));
            }

            // Submit the form
            String originalWindow = driver.getWindowHandle();
            String originalUrl = driver.getCurrentUrl();
            submitButton.click();
            log.info("Form submitted, waiting for response or page change");
            // Wait for URL change or specific elements to appear after form submission
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.not(ExpectedConditions.urlToBe(originalUrl)),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success, .valid, .error, .invalid, .alert-danger, .alert-success"))
                ));
                log.info("Page change or response elements detected after form submission");
            } catch (Exception e) {
                log.warn("Timeout waiting for page change or response elements, proceeding anyway: {}", e.getMessage());
            }
            log.info("Current URL after submit: {}", driver.getCurrentUrl());
            log.info("Page title after submit: {}", driver.getTitle());

            // Handle popup windows or new tabs
            try {
                wait.until(driver -> driver.getWindowHandles().size() > 1 || driver.getCurrentUrl().contains("result") || driver.findElements(By.cssSelector(".success, .valid, .error")).size() > 0);
                var allWindows = driver.getWindowHandles();
                if (allWindows.size() > 1) {
                    log.info("Detected {} windows after submission, switching to new window", allWindows.size());
                    for (String windowHandle : allWindows) {
                        if (!windowHandle.equals(originalWindow)) {
                            driver.switchTo().window(windowHandle);
                            log.info("Switched to new window: URL={}, Title={}", driver.getCurrentUrl(), driver.getTitle());
                            break;
                        }
                    }
                } else {
                    log.info("No new windows detected, staying on current window");
                }
            } catch (Exception e) {
                log.warn("Error handling windows after submit: {}", e.getMessage());
            }

            // Wait for response elements to appear, re-finding elements after page change
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".valid"))
                ));
                log.info("Response elements detected after form submission");
            } catch (Exception e) {
                log.warn("No success or valid elements found after submit, proceeding with parsing");
            }

            // Additional debugging after form submission
            log.info("=== DEBUGGING POST-SUBMISSION PAGE ===");
            log.info("Current URL after submit: {}", driver.getCurrentUrl());
            log.info("Page title after submit: {}", driver.getTitle());

            // Check for error messages or validation results (re-finding elements after page change)
            var errorElements = driver.findElements(By.cssSelector(".error, .invalid, .alert-danger"));
            log.info("Found {} error elements:", errorElements.size());
            for (int i = 0; i < errorElements.size(); i++) {
                WebElement error = errorElements.get(i);
                // Immediately get attributes to prevent stale reference
                String errorText = error.getText();
                String errorClass = error.getAttribute("class");
                log.info("Error[{}]: text='{}', class='{}'", i, errorText, errorClass);
            }

            var successElements = driver.findElements(By.cssSelector(".success, .valid, .alert-success"));
            log.info("Found {} success elements:", successElements.size());
            for (int i = 0; i < successElements.size(); i++) {
                WebElement success = successElements.get(i);
                // Immediately get attributes to prevent stale reference
                String successText = success.getText();
                String successClass = success.getAttribute("class");
                log.info("Success[{}]: text='{}', class='{}'", i, successText, successClass);
            }

            // Parse the response to determine if EPS is valid
            boolean isValid = parseValidationResult();
            String epsName = isValid ? extractEpsName() : null;
            String message = isValid ? "EPS validated successfully" : "EPS validation failed";
            log.info("Validation result: isValid={}, epsName='{}', message='{}'", isValid, epsName, message);

            log.info("=== END DEBUGGING FORM SUBMISSION ===");
            return new EpsValidationResponseDTO(isValid, epsName, numeroDocumento, message);
        } catch (Exception e) {
            log.error("Error validating EPS", e);
            return new EpsValidationResponseDTO(false, null, null, "Error validating EPS: " + e.getMessage());
        }
    }

    private byte[] fetchCaptchaImage(String captchaUrl) {
        log.info("Starting fetchCaptchaImage with original URL: {}", captchaUrl);
        try {
            // For data URLs, decode directly
            if (captchaUrl.startsWith("data:")) {
                log.info("Processing data URL captcha image");
                String base64Data = captchaUrl.split(",")[1];
                byte[] decoded = Base64.getDecoder().decode(base64Data);
                log.info("Successfully decoded data URL, size: {} bytes", decoded.length);
                return decoded;
            }

            // For regular URLs, resolve relative URLs against the base ADRES URL
            log.info("Processing regular URL captcha image, base URI: {}", ADRES_URL);
            URI baseUri = URI.create(ADRES_URL);
            URI resolvedUri = baseUri.resolve(captchaUrl);
            log.info("Resolved URI: {}", resolvedUri.toString());
            URL imageUrl = resolvedUri.toURL();
            log.info("Final image URL: {}", imageUrl.toString());

            // Create SSL context that trusts all certificates
            SSLContext sslContext = createTrustAllSSLContext();
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // Fetch the image bytes from the resolved URL using HttpURLConnection
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
            }
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                int totalBytes = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                byte[] result = outputStream.toByteArray();
                log.info("Successfully fetched {} bytes from URL: {}", totalBytes, imageUrl.toString());
                return result;
            } catch (Exception e) {
                log.error("Error during image stream read from resolved URL {}: {}", resolvedUri.toString(), e.getMessage(), e);
                return null;
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            log.error("Error fetching captcha image from original URL {}: {}", captchaUrl, e.getMessage(), e);
            return null;
        }
    }

    private boolean parseValidationResult() {
        // Check for success indicators in the current page (re-finding elements after page change)
        log.info("Parsing validation result, checking for success elements");
        try {
            WebElement successElement = driver.findElement(By.cssSelector(".success, .valid"));
            // Immediately get attributes to prevent stale reference
            boolean isDisplayed = successElement.isDisplayed();
            String text = successElement.getText();
            String classAttr = successElement.getAttribute("class");
            log.info("Success element found: displayed={}, text='{}', class='{}'", isDisplayed, text, classAttr);
            return isDisplayed;
        } catch (Exception e) {
            log.info("No success element found on the page with selectors '.success, .valid'");
            // Additional checks for other possible success indicators
            try {
                var allDivs = driver.findElements(By.cssSelector("div"));
                log.info("Checking {} div elements for success indicators", allDivs.size());
                for (WebElement div : allDivs) {
                    // Immediately get text to prevent stale reference
                    String text = div.getText().toLowerCase();
                    if (text.contains("válido") || text.contains("valido") || text.contains("success") || text.contains("éxito")) {
                        log.info("Found potential success text in div: '{}'", div.getText());
                        return true;
                    }
                }
            } catch (Exception ex) {
                log.debug("Error checking div elements: {}", ex.getMessage());
            }
            return false;
        }
    }

    private String extractEpsName() {
        // Extract EPS name from the current page (re-finding elements after page change)
        log.info("Extracting EPS name from page");
        try {
            WebElement epsNameElement = driver.findElement(By.cssSelector(".eps-name"));
            // Immediately get text to prevent stale reference
            String epsName = epsNameElement.getText();
            log.info("EPS name extracted from '.eps-name': {}", epsName);
            return epsName;
        } catch (Exception e) {
            log.info("EPS name element not found with '.eps-name', trying alternative selectors");
            // Try alternative selectors for EPS name
            String[] epsSelectors = {".eps", "[class*='eps']", ".entidad", ".health-entity", "h2", "h3", "h4"};
            for (String selector : epsSelectors) {
                try {
                    var elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement elem : elements) {
                        // Immediately get text to prevent stale reference
                        String text = elem.getText().trim();
                        if (!text.isEmpty() && (text.toLowerCase().contains("eps") || text.toLowerCase().contains("salud"))) {
                            log.info("EPS name extracted from '{}': {}", selector, text);
                            return text;
                        }
                    }
                } catch (Exception ex) {
                    log.debug("Error checking selector '{}': {}", selector, ex.getMessage());
                }
            }

            // Check all text content for EPS-related information
            try {
                WebElement bodyElement = driver.findElement(By.tagName("body"));
                // Immediately get text to prevent stale reference
                String bodyText = bodyElement.getText();
                log.info("Full body text length: {} characters", bodyText.length());
                // Look for EPS names in the text
                String[] epsKeywords = {"EPS", "Entidad Promotora de Salud", "Salud Total", "Nueva EPS", "Famisanar", "Sura", "Coomeva"};
                for (String keyword : epsKeywords) {
                    if (bodyText.contains(keyword)) {
                        log.info("Found EPS keyword in body text: {}", keyword);
                        return keyword;
                    }
                }
            } catch (Exception ex) {
                log.debug("Error extracting body text: {}", ex.getMessage());
            }

            log.info("EPS name element not found with any method, returning 'Unknown EPS'");
            return "Unknown EPS";
        }
    }
}