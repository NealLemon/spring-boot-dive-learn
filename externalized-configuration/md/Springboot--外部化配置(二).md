# Springboot--外部化配置(二)

  笔记是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。

## 主要应用方式
- Spring/Springboot
  -  XML Bean 定义的属性占位符
  -  `@Value` 注入
     -  @Value 字段注入（Field Injection）
     -  @Value 构造器注入（Constructor Injection）
     -  @Value 方法注入（Method Injection）
  -  `Environment` 读取
     -  Environment 方法/构造器依赖注入
     -  Environment @Autowired 依赖注入
     -  EnvironmentAware 接口回调
     -  BeanFactory 依赖查找 Environment
- Springboot
  - `@ConfigurationProperties` Bean 绑定
  - `@ConditionalOnProperty` 判断

之前的笔记，介绍了XML Bean定义的属性占位符注入以及 @Value注入。那么这篇就会讲解到使用 `Environment` 读取以及使用Springboot 独有的注解读取。



## `Environment` 读取

  在最初的笔记中其实已经介绍过`Environment`  这个Bean,我们再来复习一下。

### 基本概念

`Environment`是对配置文件(*profiles* )和属性文件(*properties*)两个关键应用环境方面的建模。提供激活和默认的配置文件和 操作底层配置资源的功能。 

通过上面的描述我们可以了解到，通过`Environment` 我们是可以读取到外部properties文件的。那么下面就让我们通过几种获取 `Environment`的方式，来实现外部化的配置。

我们可以先来看一下`Environment`的API

p10.png



我们这里主要用的就是`getRequiredProperty`  （必须获取到值不能为NULL）和`getProperty` （可以为NULL）两个方法。

在开始之前，我们需要更新一下 `application.properties`文件，为了区分之前的注入操作,我们新起一个配置。新增内容如下

```properties
car.name=Lexus
car.color=sliver
#car.producer=Japan

#新增  支持Environment 获取配置
evn.name=Neal
evn.age=18
```

接着要新增一个对应外部化的实体类

```java
/**
 * 通过 environment 实现外部化配置的bean
 */
public class User {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```

接下来让我们来分别通过上面描述的集中方式来获取到配置文件中的值。

### Environment 方法/构造器依赖注入以及@Autowired 依赖注入

我们需要创建一个配置类

```java
@Configuration
public class EnvExternalizedConfig {

    //构造方式注入 environment
    private final Environment constructorEnvironment;

    //依赖注入
    @Autowired
    private Environment environment;


    @Autowired
    public EnvExternalizedConfig(Environment environment) {
        this.constructorEnvironment = environment;
    }

    /**
     * 方法注入
     * @param methodEnvironment
     * @return
     */
    @Bean
    public User methodUser(Environment methodEnvironment) {
        User user = new User();
        String name = methodEnvironment.getRequiredProperty("evn.name");
        int age = methodEnvironment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    /**
     * 构造方式注入
     * @return
     */
    @Bean
    public User constructorUser() {
        User user = new User();
        String name = constructorEnvironment.getRequiredProperty("evn.name");
        int age = constructorEnvironment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    /**
     * 依赖注入
     * @return
     */
    @Bean
    public User autowiredUser() {
        User user = new User();
        String name = environment.getRequiredProperty("evn.name");
        int age = environment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

}
```

在这里根据注释 我们分别使用了三种方法获取 Environment 进而读取到了properties文件中的值。

## EnvironmentAware 接口回调以及BeanFactory 依赖查找 

为了和上面的几种方式区分开，我们在新建一个配置类

```java
/**
 *  EnvironmentAware,BeanFactoryAware 两种方式获取
 */
@Configuration
public class EnvironmentConfig implements EnvironmentAware,BeanFactoryAware {

    //BeanFactoryAware
    private Environment beanFactoryEnvironment;

    //EnvironmentAware
    private Environment environment;

    /**
     *  BeanFactoryAware
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactoryEnvironment = beanFactory.getBean(Environment.class);

    }

    /**
     * EnvironmentAware
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     *  BeanFactoryAware 获取的 environment对象初始化
     * @return
     */
    @Bean
    public User beanFactoryAwareUser() {
        User user = new User();
        String name = beanFactoryEnvironment.getRequiredProperty("evn.name");
        int age = beanFactoryEnvironment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    /**
     * EnvironmentAware 获取的environment对象初始化
     * @return
     */
    @Bean
    public User environmentAwareUser() {
        User user = new User();
        String name = environment.getRequiredProperty("evn.name");
        int age = environment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

}
```

在这里我们通过两个接口方法来实现`Environment`对象获取。然后在进行User类的配置声明。

### 结果测试

#### 引导类

```java
@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
public class EnvironmentBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(EnvironmentBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);
        //方法注入
        User methodUser = context.getBean("methodUser", User.class);
        //构造注入
        User constructorUser = context.getBean("constructorUser",User.class);
        //autowired注入
        User autowiredUser = context.getBean("autowiredUser",User.class);

        //BeanFactoryAware接口获取
        User beanFactoryAwareUser = context.getBean("beanFactoryAwareUser",User.class);
        //EnvironmentAware接口获取
        User environmentAwareUser = context.getBean("environmentAwareUser",User.class);

        System.out.println("Environment 方法注入 " + methodUser.toString());

        System.out.println("Environment 构造注入 " + constructorUser.toString());

        System.out.println("Environment 依赖注入 " + autowiredUser.toString());


        System.out.println("Environment BeanFactoryAware接口获取并注入 " + beanFactoryAwareUser.toString());

        System.out.println("Environment EnvironmentAware接口获取并注入 " + environmentAwareUser.toString());

        // 关闭上下文
        context.close();
    }
}
```



我们启动引导类 来查看控制台的输出结果是否一致。

p11.png

我们看到了 结果是一致的，证明这几个方法都可以获取到外部化的值。



## @ConfigurationProperties绑定以@ConditionalOnProperty 判断

### 简单介绍

由于 `@ConditionalOnProperty`是配合外部化配置使用的 所以我们在这里顺带演示了。这里需要先介绍一下 

`@ConfigurationProperties`

```java
@Target({ ElementType.TYPE, ElementType.METHOD })   //可以是类级别 也可以是方法级别
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

   /**
    * The name prefix of the properties that are valid to bind to this object. Synonym
    * for {@link #prefix()}.
    * @return the name prefix of the properties to bind
    */
    //绑定 下面的 prefix 
   @AliasFor("prefix")
   String value() default "";

   /**
    * The name prefix of the properties that are valid to bind to this object. Synonym
    * for {@link #value()}.
    * @return the name prefix of the properties to bind
    */
    //绑定上面的 value
   @AliasFor("value")
   String prefix() default "";

   /**
    * Flag to indicate that when binding to this object invalid fields should be ignored.
    * Invalid means invalid according to the binder that is used, and usually this means
    * fields of the wrong type (or that cannot be coerced into the correct type).
    * @return the flag value (default false)
    */
    //是否忽略校验字段
   boolean ignoreInvalidFields() default false;

   /**
    * Flag to indicate that when binding to this object unknown fields should be ignored.
    * An unknown field could be a sign of a mistake in the Properties.
    * @return the flag value (default true)
    */
    //是否忽略未知字段
   boolean ignoreUnknownFields() default true;

}
```

每个注解签名在注释中已经给出解释。

接下来 我们来看一下 `ConditionalOnProperty`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPropertyCondition.class)
public @interface ConditionalOnProperty {

   /**
    * Alias for {@link #name()}.
    * @return the names
    */
    //properties的 完整的key值 绑定 name
   String[] value() default {};

   /**
    * A prefix that should be applied to each property. The prefix automatically ends
    * with a dot if not specified.
    * @return the prefix
    */
    //前缀
   String prefix() default "";

   /**
    * The name of the properties to test. If a prefix has been defined, it is applied to
    * compute the full key of each property. For instance if the prefix is
    * {@code app.config} and one value is {@code my-value}, the full key would be
    * {@code app.config.my-value}
    * <p>
    * Use the dashed notation to specify each property, that is all lower case with a "-"
    * to separate words (e.g. {@code my-long-property}).
    * @return the names
    */
    //可以是properties的完整的key值 也可以是跟prefix() 一起进行判断，但是必须是剩下完整的KEY值 
   String[] name() default {};

   /**
    * The string representation of the expected value for the properties. If not
    * specified, the property must <strong>not</strong> be equal to {@code false}.
    * @return the expected value
    */
    //
   String havingValue() default "";

   /**
    * Specify if the condition should match if the property is not set. Defaults to
    * {@code false}.
    * @return if should match if the property is missing
    */
    //匹配不存在的时候是否往下进行
   boolean matchIfMissing() default false;

}
```

### 具体实践

1.在`application.properties`中添加相应的内容

```properties
car.name=Lexus
car.color=sliver
#car.producer=Japan

#新增  支持Environment 获取配置
evn.name=Neal
evn.age=18

#新增 支持 @ConfigurationProperties 类级别
configuser.name=Lemon
configuser.age=20

#新增 支持 @ConfigurationProperties 方法级别以及类的嵌套绑定
province.name=LiaoNing
province.city.name=DaLian
```

2.添加 `@ConfigurationProperties`类级别相应的实体类

```java
/**
 * 用于类注解配置
 */
@ConfigurationProperties(prefix = "configuser")
public class ConfigUser {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "ConfigUser{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```

3.添加 `@ConfigurationProperties` 方法级别以及嵌套类型绑定的实体类。

```java
/**
 * 表示城市类
 */
public class City {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                '}';
    }
}
```

```java
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
```

4.引导类以及`ConditionalOnProperty` 的条件判断。

```java
/**
 * @ClassName ConfigurationBootstrap
 * @Description
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
@EnableConfigurationProperties(ConfigUser.class)
public class ConfigurationBootstrap {

    @Bean
    @ConfigurationProperties(prefix = "province")  //方法级别的注入
    //当application.properties 中的province.city.name 必须存在并且其值必须是 Dalian
    @ConditionalOnProperty(name="province.city.name",matchIfMissing = false,havingValue = "Dalian")
    public Province province() {
        return new Province();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ConfigurationBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);

        ConfigUser configUser  = context.getBean(ConfigUser.class);
        Province province = context.getBean("province",Province.class);
        System.out.println("@ConfigurationProperties --类注入" + configUser.toString());
        System.out.println("@ConfigurationProperties --方法注入" + province.toString());

        // 关闭上下文
        context.close();
    }
}
```

这段代码就需要注意 在引导类中 我们添加了 `province()`方法 并且进行了 方法级别的外部化依赖注入同时还进行了外部化配置的条件判断。具体判断的逻辑在注释中已经给出。



### 测试结果

p12.png



可以看到类级别和方法级别注入以及条件判断都是没有问题的。如果想测试条件判断，可以将    `@ConditionalOnProperty(name="province.city.name",matchIfMissing = false,havingValue = "Dalian") ` 改为其他值，自己再重新跑一下，检验一下是否条件判断成功。