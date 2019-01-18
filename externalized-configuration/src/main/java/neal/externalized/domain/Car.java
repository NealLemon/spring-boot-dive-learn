package neal.externalized.domain;

import org.springframework.beans.factory.annotation.Value;

/**
 * @ClassName Car
 * @Description 外部化配置相关bean
 * @Author Neal
 * @Date 2019/1/18 10:57
 * @Version 1.0
 */
public class Car {

    @Value("${car.name}")
    private String name;

    @Value("${car.color}")
    private String color;

    @Value("${car.producer}")
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
