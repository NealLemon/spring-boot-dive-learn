package neal.externalized.domain;


/**
 * @ClassName SpringXmlCar
 * @Description XML配置的外部化配置
 * @Author Neal
 * @Date 2019/1/19 9:59
 * @Version 1.0
 */
public class SpringXmlCar {

    private String name;

    private String color;

    private String producer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", producer='" + producer + '\'' +
                '}';
    }
}
