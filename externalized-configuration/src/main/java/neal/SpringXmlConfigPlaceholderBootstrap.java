package neal;

import neal.externalized.domain.SpringXmlCar;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringXmlConfigPlaceholderBootstrap {

    public static void main(String[] args) {

        String[] locations = {"META-INF/spring/spring-context.xml", "META-INF/spring/car-context.xml"};
        ClassPathXmlApplicationContext applicationContext = new
                ClassPathXmlApplicationContext(locations);
        SpringXmlCar springXmlCar = applicationContext.getBean("springXmlCar", SpringXmlCar.class);
        System.out.println("获取汽车信息 : " + springXmlCar);
        // 关闭上下文
        applicationContext.close();
    }
}
