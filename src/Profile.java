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
