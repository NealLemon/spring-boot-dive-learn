# Springboot--外部化配置(一)

  笔记是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。

## 基本介绍

SpringBoot允许将配置进行外部化（externalize），这样你就能够在不同的环境下使用相同的代码。你可以使用properties文件，yaml文件，环境变量和命令行参数来外部化配置。使用@Value注解，可以直接将属性值注入到beans中，然后通过Spring的Environment抽象或通过@ConfigurationProperties绑定到结构化对象来访问。

SpringBoot设计了一个非常特别的加载指定属性文件的顺序(@PropertySource)，以允许对属性值进行合理的覆盖，属性会以如下的顺序进行设值：
​    

- home目录下的devtools全局设置属性（~/.spring-boot-devtools.properties，如      果devtools激活）。

- 测试用例上的@TestPropertySource注解。

- 测试用例上的@SpringBootTest#properties注解。

- 命令行参数.

- 来自SPRING_APPLICATION_JSON的属性（环境变量或系统属性中内嵌的内联JSON）。

- ServletConfig初始化参数。

- ServletContext初始化参数。

- 来自于java:comp/env的JNDI属性。

- Java系统属性（System.getProperties()）。

- 操作系统环境变量。

- RandomValuePropertySource，只包含random.*中的属性。

- 没有打进jar包的Profile-specific应用属性（application-{profile}.properties和YAML变量）。

- 打进jar包中的Profile-specific应用属性（application-{profile}.properties和YAML变量）。

- 没有打进jar包的应用配置（application.properties和YAML变量）。

- 打进jar包中的应用配置（application.properties和YAML变量）。

- @Configuration类上的@PropertySource注解。

- 默认属性（使用SpringApplication.setDefaultProperties指定）。

 <u>以上需要重点关注的就是读取外部化配置的优先级顺序。</u>

## 主要应用方式

- Spring/Springboot
  -  XML Bean 定义的属性占位符
  -  `@Value` 注入
     -  @Value 字段注入（Field Injection）
     -  @Value 构造器注入（Constructor Injection）
     -  @Value 方法注入（Method Injection）
  -  `Environment` 读取
- Springboot
  - `@ConfigurationProperties` Bean 绑定
  - `@ConditionalOnProperty` 判断



这次我们先介绍 **XML Bean 定义的属性占位符** 和 **@Value 注入**。



### 基础环境

#### pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### XML Bean 定义的属性占位符

相信使用Spring XML配置的小伙伴对这个不配置方式不陌生。

#### 相关代码

1. 外部化要配置的bean

    ```java
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
    ```

2. Spring相关配置XML 

   1. resources/META-INF/spring/spring-context.xml  上下文配置 使用`PropertyPlaceholderConfigurer` 读取 properties文件

      ```xml
      <?xml version="1.0" encoding="UTF-8"?>
      <beans xmlns="http://www.springframework.org/schema/beans"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
      
          <!-- 属性占位符配置-->
          <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
              <!-- Properties 文件 classpath 路径 -->
              <property name="location" value="classpath:/META-INF/default.properties"/>
              <!-- 文件字符编码 -->
              <property name="fileEncoding" value="UTF-8"/>
          </bean>
      
      </beans>
      ```

   2. resources/META-INF/spring/car-context.xml   针对`SpringXmlCar` 使用占位符赋值

      ```xml
      <?xml version="1.0" encoding="UTF-8"?>
      <beans xmlns="http://www.springframework.org/schema/beans"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
      
          <!-- User Bean -->
          <bean id="springXmlCar" class="neal.externalized.domain.SpringXmlCar">
              <property name="name" value="${car.name}"/>
              <property name="color" value="${car.color}"/>
              <property name="producer" value="${car.producer}"/>
          </bean>
      
      </beans>
      ```

   3. resources/-INF/default.properties  外部化配置

      ```properties
      car.name=Lexus
      car.color=sliver
      car.producer=Japan
      ```

3. 启动类

    1. 完全Spring方式 SpringXmlConfigPlaceholderBootstrap

        ```java
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
        ```

    2. springboot自动装配方式

        ```java
        @ImportResource("META-INF/spring/car-context.xml") // 加载 Spring 上下文 XML 文件
        @PropertySource("META-INF/default.properties")   //加载配置文件
        @EnableAutoConfiguration
        public class XmlPlaceholderExternalizedConfigurationBootstrap {
        
            public static void main(String[] args) {
        
                ConfigurableApplicationContext context =
                        new SpringApplicationBuilder(XmlPlaceholderExternalizedConfigurationBootstrap.class)
                                .web(WebApplicationType.NONE) // 非 Web 应用
                                .run(args);
        
                SpringXmlCar springXmlCar = context.getBean("springXmlCar", SpringXmlCar.class);
                System.out.println("获取汽车信息 : " + springXmlCar);
                // 关闭上下文
                context.close();
            }
        }
        ```

#### 测试结果

- Spring XML启动结果

  p1.png

- Spingboot自动装配

​      p2.png

### @Value 注入

在这里我们使用Springboot来实现Demo。因为我们使用的是自动装配，所以Springboot 会去默认读取`resources\application.properties` 的配置信息。

#### @Value 字段注入（Field Injection）

#### 相关代码

1. 外部化配置 `application.properties`

   ```properties
   car.name=Lexus
   car.color=sliver
   car.producer=Japan
   ```

2. 要外部化配置的Bean

   ```java
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
   ```

3. 配置类，也就是使用Java Config声明需要的Bean 

   ```java
   /**
    * @ClassName ValueAnnotationBootstrap
    * @Description config声明bean
    * @Author Neal
    * @Date 2019/1/18 11:11
    * @Version 1.0
    */
   
   @Configuration
   public class ExternalizedConfig {
   
       @Bean
       public Car car() {
           return new Car();
       }
   }
   ```

4. 启动类,在这个类中，我们需要扫描到所需要的类

   ```java
   /**
    * @ClassName ValueAnnotationBootstrap
    * @Description
    * @Author Neal
    * @Date 2019/1/18 11:11
    * @Version 1.0
    */
   @EnableAutoConfiguration
   @ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
   public class ValueAnnotationBootstrap {
   
       public static void main(String[] args) {
           ConfigurableApplicationContext context =
                   new SpringApplicationBuilder(ValueAnnotationBootstrap.class)
                           .web(WebApplicationType.NONE) // 非 Web 应用
                           .run(args);
   
           Car  car = context.getBean("car", Car.class);
   
           System.out.println(car.toString());
   
           // 关闭上下文
           context.close();
       }
   }
   ```



#### 测试结果

我们可以看到结果跟之前的XML Bean的配置方式结果一致。

p3.png



### @Value 构造器注入（Constructor Injection）

#### 相关代码

1.外部化配置`application.properties`不变。

2.为了防止和之前的外部化配置类 `Car.java` ，我们新建一个外部化配置类 MethodCar。

```java
/**
 * @ClassName MethodCar
 * @Description  方法注入
 * @Author Neal
 * @Date 2019/1/19 14:50
 * @Version 1.0
 */
public class MethodCar {

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
        return "MethodCar{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", producer='" + producer + '\'' +
                '}';
    }

}
```

3.修改之前的 `ExternalizedConfig.java`

```java
@Configuration
public class ExternalizedConfig {

    /**
     * 字段注入
     * @return
     */
    @Bean
    public Car car() {
        return new Car();
    }


    /**
     *
     * 方法注入
     * @param name
     * @param color
     * @param producer
     * @return
     */
    @Bean
    public MethodCar methodCar(@Value("${car.name}") String name,
                               @Value("${car.color}") String color,
                               @Value("${car.producer}") String producer) {
        MethodCar methodCar = new MethodCar();
        methodCar.setName(name);
        methodCar.setColor(color);
        methodCar.setProducer(producer);
        return methodCar;
    }
}
```



结果测试会等之后其他的所有注入方式配置完后统一执行。

### @Value 构造器注入（Constructor Injection）

#### 相关代码

1.外部化配置`application.properties`不变。

2.我们新建一个外部化配置类 ConstructorCar。

```java
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
```

3.修改之前的 `ExternalizedConfig.java`

```java
/**
 * @ClassName ValueAnnotationBootstrap
 * @Description config声明bean
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */

@Configuration
public class ExternalizedConfig {

    
    private final String name;

    private final String color;

    private final String producer;

    /**
     * 构造器注入
     * @param name
     * @param color
     * @param producer
     */
    public ExternalizedConfig(@Value("${car.name}") String name,
                              @Value("${car.color}") String color,
                              @Value("${car.producer}") String producer) {
        this.name = name;
        this.color = color;
        this.producer = producer;
    }

    @Bean
    public ConstructorCar constructorCar() {
        ConstructorCar constructorCar = new ConstructorCar();
        constructorCar.setName(name);
        constructorCar.setColor(color);
        constructorCar.setProducer(producer);
        return constructorCar;
    }

    /**
     * 字段注入
     * @return
     */
    @Bean
    public Car car() {
        return new Car();
    }


    /**
     *
     * 方法注入
     * @param name
     * @param color
     * @param producer
     * @return
     */
    @Bean
    public MethodCar methodCar(@Value("${car.name}") String name,
                               @Value("${car.color}") String color,
                               @Value("${car.producer}") String producer) {
        MethodCar methodCar = new MethodCar();
        methodCar.setName(name);
        methodCar.setColor(color);
        methodCar.setProducer(producer);
        return methodCar;
    }
}
```



### @Value三个注入方式整体的测试对比结果

我们通过结果可以看到这三种注入方式全部注入成功。

p4.png



### @Value 默认值设置

1.我这里就在application.properties修改了值。

`application.properties`

p5.png

2.默认值设置其实很简单就是在注入方法的EL表达式中再加些东西就可以实现，具体方式就是 

`{value : defaultValue}`

3.修改字段注入，构造注入，方法注入相关类。

- 字段注入

  p6.png

- 构造注入

  p7.png

- 方法注入

  p8.png



##### 测试结果

 我们可以看到 即使我们去掉了car.producer=Japan 配置值，但是我们也可以通过默认赋值来完成注入。

p9.png





