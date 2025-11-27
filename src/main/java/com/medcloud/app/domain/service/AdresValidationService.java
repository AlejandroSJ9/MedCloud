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

/**
 * Service for validating EPS (health insurance) information against the Colombian ADRES database.
 * This service uses Selenium WebDriver to scrape the ADRES website for validation.
 *
 * Performance optimizations implemented:
 * - Optimized Chrome options for headless execution (--disable-gpu, --disable-extensions, etc.)
 * - Reduced WebDriverWait timeout from 10s to 5s
 * - Reduced HTTP connection timeouts from 10s to 5s
 * - Session reuse by keeping WebDriver instance alive
 *
 * Expected execution time: Optimized from ~2 minutes to ~20-40 seconds depending on network.
 */
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
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        options.addArguments("--window-size=1280,720");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    /**
     * Creates an SSL context that trusts all certificates.
     * This is used to bypass SSL certificate validation for the ADRES domain.
     *
     * @return SSLContext configured to trust all certificates
     * @throws NoSuchAlgorithmException if TLS is not available
     * @throws KeyManagementException   if key management fails
     */
    private SSLContext createTrustAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
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
     * Maps abbreviated document types from frontend to full names expected by ADRES website.
     *
     * @param tipoDocumento abbreviated document type (CC, TI, CE)
     * @return full document type name for ADRES website
     */
    private String mapTipoDocumento(String tipoDocumento) {
        switch (tipoDocumento) {
            case "CC":
                return "Cédula de Ciudadanía";
            case "TI":
                return "Tarjeta de Identidad";
            case "CE":
                return "Cédula de Extranjería";
            default:
                return tipoDocumento; // Return as-is if not recognized
        }
    }

    /**
     * Initiates a validation request by filling document type and number, then
     * fetching the captcha image and returning it with a session ID.
     *
     * @param tipoDocumento   the document type
     * @param numeroDocumento the document number
     * @return CaptchaResponseDTO containing the captcha image in base64 and session
     *         ID
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

            // Try to select by value first (should be the abbreviated form)
            try {
                tipoDocSelect.selectByValue(tipoDocumento);
                log.info("Selected document type by value: {}", tipoDocumento);
            } catch (Exception e) {
                log.warn("Select by value failed, trying by visible text");
                // Fallback to visible text with proper encoding
                String fullTipoDocumento = mapTipoDocumento(tipoDocumento);
                tipoDocSelect.selectByVisibleText(fullTipoDocumento);
                log.info("Selected document type by visible text: {}", fullTipoDocumento);
            }
            WebElement numeroDocumentoInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtNumDoc")));

            // Wait a bit for stability and re-locate elements
            try {
                Thread.sleep(500); // Small delay for page stability
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // tipoDocumentoSelect =
            // wait.until(ExpectedConditions.elementToBeClickable(By.id("tipoDoc")));
            numeroDocumentoInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtNumDoc")));

            // tipoDocumentoSelect.clear();
            // tipoDocumentoSelect.sendKeys(tipoDocumento);
            // numeroDocumentoInput.clear();
            numeroDocumentoInput.sendKeys(numeroDocumento);
            log.info("Form fields filled in initiate: tipoDocumento='{}', numeroDocumento='{}'", tipoDocumento,
                    numeroDocumento);

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
            String[] captchaSelectors = { "Capcha_CaptchaImageUP", "captcha", "Captcha", "CAPTCHA" };
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
                String[] selectors = { "#Capcha_CaptchaImageUP", "img[src*='captcha']", "img[alt*='captcha']",
                        "img[id*='captcha']", "img[class*='captcha']" };
                log.info("Attempting to find captcha image with {} selectors: {}", selectors.length,
                        String.join(", ", selectors));
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
     * @param sessionId       the session ID from the initiation
     * @param captchaSolution the user's captcha solution
     * @param tipoDocumento   the document type
     * @param numeroDocumento the document number
     * @return EpsValidationResponseDTO containing validation result
     */
    public EpsValidationResponseDTO validateEps(String sessionId, String captchaSolution, String tipoDocumento,
            String numeroDocumento) {
        try {
            // Check if we're already on the ADRES page, if not navigate there
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("adres.gov.co")) {
                log.info("Not on ADRES page, navigating to: {}", ADRES_URL);
                driver.get(ADRES_URL);
                Thread.sleep(3000);
            } else {
                log.info("Already on ADRES page: {}", currentUrl);
            }

            // Map abbreviated document types to full names
            String fullTipoDocumento = mapTipoDocumento(tipoDocumento);
            log.info("Starting EPS validation for session: {}, documento: {}, tipo: {}", sessionId, numeroDocumento, fullTipoDocumento);

            // Try to fill document type if the element exists
            try {
                WebElement tipoDocumentoSelect = driver.findElement(By.id("tipoDoc"));
                Select tipoDocSelect = new Select(tipoDocumentoSelect);
                try {
                    tipoDocSelect.selectByValue(tipoDocumento);
                    log.info("Selected document type by value: {}", tipoDocumento);
                } catch (Exception e) {
                    log.warn("Select by value failed, trying by visible text");
                    tipoDocSelect.selectByVisibleText(fullTipoDocumento);
                    log.info("Selected document type by visible text: {}", fullTipoDocumento);
                }
            } catch (Exception e) {
                log.info("Document type select not found, might already be filled");
            }

            // Try to fill document number if the element exists
            try {
                WebElement numeroDocumentoInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtNumDoc")));
                numeroDocumentoInput.clear();
                numeroDocumentoInput.sendKeys(numeroDocumento);
                log.info("Form fields filled: tipoDocumento='{}', numeroDocumento='{}'", fullTipoDocumento, numeroDocumento);
            } catch (Exception e) {
                log.info("Document number input not found or not clickable, might already be filled");
            }

            // Wait for captcha image to be loaded if not already present
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#Capcha_CaptchaImageUP")));
                log.info("Captcha image loaded");
            } catch (Exception e) {
                log.info("Captcha image not found or already loaded");
            }

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
            String[] captchaSelectors = { "input[name='captcha']", "input[id*='captcha']", "input[name*='captcha']",
                    "#Capcha_CaptchaTextBox", "input[type='text'][placeholder*='captcha']" };
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
                captchaInput = wait
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#Capcha_CaptchaTextBox")));
                submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConsultar")));

                // Retry form filling
                captchaInput.clear();
                captchaInput.sendKeys(captchaSolution);
                log.info("Form fields re-filled after stale element recovery: captcha='{}'",
                        captchaSolution.replaceAll(".", "*"));
            }

            // Submit the form
            String originalWindow = driver.getWindowHandle();
            String originalUrl = driver.getCurrentUrl();
            submitButton.click();
            log.info("Form submitted, waiting for response or page change");

            log.info("Current URL after submit: {}", driver.getCurrentUrl());
            log.info("Page title after submit: {}", driver.getTitle());

            // Handle popup windows or new tabs
            try {
                wait.until(driver -> driver.getWindowHandles().size() > 1 || driver.getCurrentUrl().contains("result")
                        || driver.findElements(By.cssSelector(".success, .valid, .error")).size() > 0);
                var allWindows = driver.getWindowHandles();
                if (allWindows.size() > 1) {
                    log.info("Detected {} windows after submission, switching to new window", allWindows.size());
                    for (String windowHandle : allWindows) {
                        if (!windowHandle.equals(originalWindow)) {
                            driver.switchTo().window(windowHandle);
                            log.info("Switched to new window: URL={}, Title={}", driver.getCurrentUrl(),
                                    driver.getTitle());
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
                // Esperar a que la tabla de resultados esté presente
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("GridViewAfiliacion")));
                log.info("Tabla de resultados (GridViewAfiliacion) detectada después del submit");
            } catch (Exception e) {
                log.error("No se encontró la tabla de resultados 'GridViewAfiliacion' a tiempo.", e);
                // Si la tabla no aparece, asume fallo y sal del proceso.
                // Esto es más robusto que simplemente "proceder con parsing".
                return new EpsValidationResponseDTO(false, null, null, numeroDocumento,
                        "Timeout: No se encontró la tabla de resultados en la ventana emergente.");
            }

            // Additional debugging after form submission
            log.info("=== DEBUGGING POST-SUBMISSION PAGE ===");
            log.info("Current URL after submit: {}", driver.getCurrentUrl());
            log.info("Page title after submit: {}", driver.getTitle());

            // Check for error messages or validation results (re-finding elements after
            // page change)
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
            // 1. Localizar la tabla de resultados
            WebElement gridTable = driver.findElement(By.id("GridViewAfiliacion"));

            // 2. Localizar la fila de datos
            WebElement dataRow = gridTable.findElement(By.cssSelector("tr.DataGrid_Item"));

            // 3. Locali
            WebElement statusElement = dataRow.findElements(By.tagName("td")).get(0);
            String status = statusElement.getText().trim().toUpperCase();
            String message = isValid ? "EPS validated successfully" : "EPS validation failed";
            log.info("Validation result: isValid={}, epsName='{}', message='{}'", isValid, epsName, message);

            log.info("=== END DEBUGGING FORM SUBMISSION ===");
            return new EpsValidationResponseDTO(isValid, epsName, numeroDocumento, status, message);
        } catch (Exception e) {
            log.error("Error validating EPS", e);
            return new EpsValidationResponseDTO(false, null, null, null, "Error validating EPS: " + e.getMessage());
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
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

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
                log.error("Error during image stream read from resolved URL {}: {}", resolvedUri.toString(),
                        e.getMessage(), e);
                return null;
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            log.error("Error fetching captcha image from original URL {}: {}", captchaUrl, e.getMessage(), e);
            return null;
        }
    }

    public boolean parseValidationResult() {
        try {
            // 1. Localizar la tabla de resultados
            WebElement gridTable = driver.findElement(By.id("GridViewAfiliacion"));

            // 2. Localizar la fila de datos
            WebElement dataRow = gridTable.findElement(By.cssSelector("tr.DataGrid_Item"));

            // 3. Localizar la celda de ESTADO (el primer <td>)
            WebElement statusElement = dataRow.findElements(By.tagName("td")).get(0);

            String status = statusElement.getText().trim().toUpperCase();

            if ("ACTIVO".equals(status) || "ACTIVA".equals(status)) {
                log.info("Validation successful. Status found: {}", status);
                return true;
            } else {
                log.warn("Validation failed. Status found: {}", status);
                return false;
            }

        } catch (Exception e) {
            log.error("Error during validation/parsing of GridViewAfiliacion.", e);
            return false;
        }
    }

    private String extractEpsName() {
        log.info("Extracting EPS name from the specific results table (GridViewAfiliacion)");
        try {
            // 1. Localizar la tabla por su ID
            WebElement gridTable = driver.findElement(By.id("GridViewAfiliacion"));

            // 2. Localizar la primera fila de datos (<tbody>/<tr>[2])
            // La tabla tiene un <tr> de encabezado y un <tr> de datos.
            // Si usamos XPath, By.cssSelector("tr.DataGrid_Item") es más robusto.
            WebElement dataRow = gridTable.findElement(By.cssSelector("tr.DataGrid_Item"));

            // 3. Localizar la celda con el nombre de la ENTIDAD (el segundo <td>)
            // Usamos findElements para obtener todas las celdas <td> de esa fila.
            // El índice es 1, ya que los índices de la lista comienzan en 0 (1er <td> es
            // ESTADO, 2do <td> es ENTIDAD).
            WebElement epsNameElement = dataRow.findElements(By.tagName("td")).get(1);

            // 4. Obtener el texto de la celda
            String epsName = epsNameElement.getText().trim();

            if (!epsName.isEmpty()) {
                log.info("EPS name successfully extracted from GridViewAfiliacion: {}", epsName);
                return epsName;
            }

            log.warn("EPS name element found, but text was empty. Returning 'Unknown EPS'.");
            return "Unknown EPS";

        } catch (org.openqa.selenium.NoSuchElementException nsee) {
            log.error("Table 'GridViewAfiliacion' or data elements not found. Returning 'Unknown EPS'.", nsee);
            return "Unknown EPS";
        } catch (Exception e) {
            log.error("An unexpected error occurred during EPS name extraction.", e);
            return "Unknown EPS";
        }
    }
}