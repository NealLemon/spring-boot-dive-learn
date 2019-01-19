package neal.externalized.domain;

/**
 * 描述 省级的类 用于 springboot 的配置
 */
public class Province {

    private String name;

    private City city = new City();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Province{" +
                "name='" + name + '\'' +
                ", city=" + city +
                '}';
    }
}
