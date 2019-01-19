package neal.externalized.domain;


/**
 * @ClassName ConstructorCar
 * @Description 构造函数注入方式配置bean
 * @Author Neal
 * @Date 2019/1/19 14:40
 * @Version 1.0
 */
public class ConstructorCar {

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
        return "ConstructorCar{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", producer='" + producer + '\'' +
                '}';
    }
}
