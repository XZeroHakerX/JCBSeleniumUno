// Clase alternativa para representar un perfil de producto
class Profile {
    private String url;
    private String nombre;
    private int edad;
    private double puntuacion;
    private double precio;

    public Profile(String url, String nombre, int edad, double puntuacion, double precio) {
        this.url = url;
        this.nombre = nombre;
        this.edad = edad;
        this.puntuacion = puntuacion;
        this.precio = precio;
    }

    public String getUrl() {
        return url;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    public double getPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "url='" + url + '\'' +
                ", nombre='" + nombre + '\'' +
                ", edad=" + edad +
                ", puntuacion=" + puntuacion +
                ", precio=" + precio +
                '}';
    }
}
