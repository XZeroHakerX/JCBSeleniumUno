
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

    // Contador global para las capturas de pantalla:
    static int contadorScreen = 1;

    public static void main(String[] XZeroHakerX) {

        // Direccion del driver:
        System.setProperty("webdriver.chrome.driver", "C:\\Desarrollo\\Selenium\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        Scanner inputScanner = new Scanner(System.in);


        // Peticion de datos para el filtro:
        System.out.print("Edad minima: ");
        int edadMin = inputScanner.nextInt();
        System.out.print("Edad maxima: ");
        int edadMax = inputScanner.nextInt();
        System.out.print("Precio maximo: ");
        double precioMax = inputScanner.nextDouble();


        // Inicio del driver y configuracion del driverwait
        WebDriver driver = new ChromeDriver();
        WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {

            // Accder a la pagina principal
            driver.get("https://amazondating.co/");
            // Llamada a la espera con driverwait
            espera.until(ExpectedConditions.presenceOfElementLocated(By.className("product-grid")));

            // Extraer los perfiles como elementos:
            List<WebElement> perfilesElementos = driver.findElements(By.className("product-tile"));
            // Lista para los objetos perfiles que vamos extrayendo:
            List<Profile> perfiles = new ArrayList<>();

            // Recorremos cada elemento-perfil para extraer la informacion:
            for (WebElement perfil : perfilesElementos) {

                try {
                    // Se busca y se obtiene el enlace del perfil para poder acceder a el:
                    WebElement linkPerfil = perfil.findElement(By.tagName("a"));
                    String perfilUrl = linkPerfil.getAttribute("href");

                    // Extraemos el texto del nombre
                    WebElement nombrePerfil = perfil.findElement(By.className("product-name"));
                    String nombreCompleto = nombrePerfil.getText().trim();
                    String nombrePerfilTexto = nombreCompleto.replaceAll("[0-9]", " ").replaceAll("[,]", " ").trim();
                    // Extraemos la edad del perfil:
                    String edadToken = nombreCompleto.substring(nombreCompleto.lastIndexOf(",") + 1).trim();
                    int edadPerfil = Integer.parseInt(edadToken);

                    // Extraer puntuacion a partir de la clase del div de estrellas
                    WebElement estrellasPerfil = perfil.findElement(By.xpath(".//div[contains(@class, 'stars')]"));
                    String estrellas = estrellasPerfil.getAttribute("class");
                    // Utilizamos el metodo para convertir la puntuacion:
                    double puntuacion = convertirPuntuacion(estrellas);

                    // Extraer el precio del perfil:
                    WebElement precioPerfil = perfil.findElement(By.xpath(".//div[contains(@class, 'product-price')]"));
                    WebElement precioPerfilTexto = precioPerfil.findElement(By.tagName("p"));
                    String precioTexto = precioPerfilTexto.getText().trim();

                    // Si el producto esta marcado como no disponible, lo saltamos
                    if (precioTexto.contains("Currently unavailable")) continue;
                    double precio = convertirPrecio(precioTexto);

                    // Agregamos el perfil a la lista
                    perfiles.add(new Profile(perfilUrl, nombrePerfilTexto, edadPerfil, puntuacion, precio));

                } catch (Exception ex) {

                    System.out.println("No se pudo procesar un producto: " + ex.getMessage());

                }
            }


            // Aqui filtramos la lista con los criterios que queremos y que le hemos proporciando al principio:
            List<Profile> perfilesFiltrados = new ArrayList<>();
            for (Profile p : perfiles) {

                if (p.getPuntuacion() >= 3.0 && p.getEdad() >= edadMin && p.getEdad() <= edadMax && p.getPrecio() <= precioMax) {
                    perfilesFiltrados.add(p);
                }

            }

            // Ordenamos los perfiles para coger los mejores puntuados:
            perfilesFiltrados.sort((a, b) -> Double.compare(b.getPuntuacion(), a.getPuntuacion()));

            // Ahora realizamos las 3 compras correspondientes con los 3 primeros perfiles:
            int perfilesCompra = Math.min(3, perfilesFiltrados.size());
            for (int i = 0; i < perfilesCompra; i++) {

                Profile actual = perfilesFiltrados.get(i);
                System.out.println("Comprando: " + actual);

                // Abrimos la pagina del perfil
                driver.get(actual.getUrl());
                tomarCapturas(driver);
                Thread.sleep(1000);

                // Cambiamos las opciones de personalizacion del perfil:
                WebElement botonOpciones = espera.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@data-interest, 'acts of service')]")));
                botonOpciones.click();
                tomarCapturas(driver);
                Thread.sleep(1000);

                // Agregamos el boton para añadir al carrito y damos click:
                WebElement botonAniadir = espera.until(ExpectedConditions.elementToBeClickable(By.className("add-to-cart")));
                botonAniadir.click();
                Thread.sleep(1000);
                tomarCapturas(driver);

                // Agregamos el boton para la compra y damos click:
                WebElement botonComprar = espera.until(ExpectedConditions.elementToBeClickable(By.className("amazon-button")));
                botonComprar.click();
                Thread.sleep(1000);
                tomarCapturas(driver);
                Thread.sleep(1000);
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            driver.quit();
            inputScanner.close();
        }
    }

    // Metodo que extrae el rating a partir de la cadena de clases
    private static double convertirPuntuacion(String classString) throws Exception {

        // Cogemos la cadena de estrellas y modificamos la cadena para que se ajuste a un double:
        for (String token : classString.split("\\s+")) {
            if (token.startsWith("star-")) {
                String numeroTexto = token.substring("star-".length()).replace("-", ".");
                return Double.parseDouble(numeroTexto);
            }
        }
        throw new Exception("No se pudo extraer el rating de: " + classString);
    }

    // Metodo para la conversion del precio a un tipo double que sea compatible para la comparacion:
    private static double convertirPrecio(String text) throws Exception {

        // Eliminamos el signo del dolar:
        String cleaned = text.replace("$", "").trim();
        String[] parts = cleaned.split("\\s+");
        if (parts.length > 0) {
            return Double.parseDouble(parts[0]);
        }
        throw new Exception("Precio no válido: " + text);
    }

    // Metodo para tomar capturas de pantalla:
    private static void tomarCapturas(WebDriver driver) throws IOException, InterruptedException {
        File archivo = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destino = new File(Paths.get("./capturas/screenshot" + contadorScreen + ".png").toString());
        FileUtils.copyFile(archivo, destino);
        System.out.println("Captura guardada: " + destino.getAbsolutePath());
        contadorScreen++;
        Thread.sleep(1000);
    }
}

