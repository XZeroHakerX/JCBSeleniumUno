
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class Principal {
    // Contador global para las capturas de pantalla
    static int screenshotCounter = 1;

    public static void main(String[] args) {
        // Configurar el path del ChromeDriver (ajusta la ruta según tu entorno)
        System.setProperty("webdriver.chrome.driver", "C:\\Desarrollo\\Selenium\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        Scanner inputScanner = new Scanner(System.in);
        System.out.println("=== Parámetros de Filtro ===");
        System.out.print("Edad mínima: ");
        int ageMin = inputScanner.nextInt();
        System.out.print("Edad máxima: ");
        int ageMax = inputScanner.nextInt();
        System.out.print("Precio máximo: ");
        double priceMax = inputScanner.nextDouble();

        WebDriver driver = new ChromeDriver();
        // Configuración de espera explícita
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Acceder a la página principal
            driver.get("https://amazondating.co/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-grid")));

            // Extraer los elementos de producto
            List<WebElement> productElements = driver.findElements(By.className("product-tile"));
            List<Profile> profiles = new ArrayList<>();

            // Recorremos cada producto para extraer la información
            for (WebElement product : productElements) {
                try {
                    // Se obtiene el enlace directo del producto (usando el primer <a>)
                    WebElement linkElement = product.findElement(By.tagName("a"));
                    String productUrl = linkElement.getAttribute("href");

                    // Extraemos el texto del nombre (formato: "Nombre, edad")
                    WebElement nameElement = product.findElement(By.className("product-name"));
                    String fullText = nameElement.getText().trim();
                    String profileName = fullText.replaceAll("[0-9]", " ").replaceAll("[,]", " ").trim();
                    // Se asume que la edad es la parte tras la última coma
                    String ageToken = fullText.substring(fullText.lastIndexOf(",") + 1).trim();
                    int profileAge = Integer.parseInt(ageToken);

                    // Extraer rating a partir de la clase del div de estrellas
                    WebElement starElement = product.findElement(By.xpath(".//div[contains(@class, 'stars')]"));
                    String starClasses = starElement.getAttribute("class");
                    double rating = parseRating(starClasses);

                    // Extraer el precio del producto
                    WebElement priceContainer = product.findElement(By.xpath(".//div[contains(@class, 'product-price')]"));
                    WebElement priceParagraph = priceContainer.findElement(By.tagName("p"));
                    String priceText = priceParagraph.getText().trim();
                    // Si el producto está marcado como no disponible, lo saltamos
                    if (priceText.contains("Currently unavailable")) continue;
                    double price = parsePrice(priceText);

                    // Agregamos el perfil a la lista
                    profiles.add(new Profile(productUrl, profileName, profileAge, rating, price));
                } catch (Exception ex) {
                    System.out.println("No se pudo procesar un producto: " + ex.getMessage());
                }
            }

            // Filtrar según criterios: rating >= 3, edad en el rango y precio <= precioMax
            List<Profile> filteredProfiles = new ArrayList<>();
            for (Profile p : profiles) {
                if (p.getRating() >= 3.0 && p.getAge() >= ageMin && p.getAge() <= ageMax && p.getPrice() <= priceMax) {
                    filteredProfiles.add(p);
                }
            }

            // Ordenar de mayor a menor rating
            filteredProfiles.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));

            // Simular el proceso de compra en hasta 3 productos
            int productsToBuy = Math.min(3, filteredProfiles.size());
            for (int idx = 0; idx < productsToBuy; idx++) {
                Profile current = filteredProfiles.get(idx);
                System.out.println("Comprando: " + current);
                // Abrir la página del producto
                driver.get(current.getUrl());
                takeScreenshot(driver);
                Thread.sleep(3000);

                // Simulación de personalización: seleccionar un interés
                // Se usa un XPath que busque un botón con data-interest que contenga "acts of service"
                WebElement interestButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(@data-interest, 'acts of service')]")));
                interestButton.click();
                takeScreenshot(driver);
                Thread.sleep(3000);

                // Agregar al carrito (se busca un botón con la clase "add-to-cart")
                WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("add-to-cart")));
                addToCartButton.click();
                Thread.sleep(1000);
                takeScreenshot(driver);

                // Confirmar la compra (se busca un botón con la clase "amazon-button")
                WebElement purchaseButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("amazon-button")));
                purchaseButton.click();
                Thread.sleep(1000);
                takeScreenshot(driver);
                Thread.sleep(3000);
            }

            // Espera final para revisión antes de cerrar
            Thread.sleep(50000);
            driver.quit();
            inputScanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            inputScanner.close();
        }
    }

    // Método que extrae el rating a partir de la cadena de clases
    private static double parseRating(String classString) throws Exception {
        // Se espera que la clase tenga formato similar a "stars star-4-5" o "stars star-3"
        for (String token : classString.split("\\s+")) {
            if (token.startsWith("star-")) {
                String numberStr = token.substring("star-".length()).replace("-", ".");
                return Double.parseDouble(numberStr);
            }
        }
        throw new Exception("No se pudo extraer el rating de: " + classString);
    }

    // Método que parsea el precio a partir del texto (por ejemplo, "$59.99 ...")
    private static double parsePrice(String text) throws Exception {
        // Se elimina el símbolo de dólar y se extrae el primer token numérico
        String cleaned = text.replace("$", "").trim();
        String[] parts = cleaned.split("\\s+");
        if (parts.length > 0) {
            return Double.parseDouble(parts[0]);
        }
        throw new Exception("Precio no válido: " + text);
    }

    // Método para tomar capturas de pantalla
    private static void takeScreenshot(WebDriver driver) throws IOException, InterruptedException {
        File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destination = new File(Paths.get("./capturas/screenshot" + screenshotCounter + ".png").toString());
        FileUtils.copyFile(sourceFile, destination);
        System.out.println("Captura guardada: " + destination.getAbsolutePath());
        screenshotCounter++;
        Thread.sleep(3000);
    }
}

// Clase alternativa para representar un perfil de producto
class Profile {
    private String url;
    private String name;
    private int age;
    private double rating;
    private double price;

    public Profile(String url, String name, int age, double rating, double price) {
        this.url = url;
        this.name = name;
        this.age = age;
        this.rating = rating;
        this.price = price;
    }

    public String getUrl() {
        return url;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public double getRating() {
        return rating;
    }
    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", rating=" + rating +
                ", price=" + price +
                ", url='" + url + '\'' +
                '}';
    }
}
