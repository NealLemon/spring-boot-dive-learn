## Spring 模式注解装配 笔记

                      *学习笔记是学习了 慕课网小马哥的 《Spring Boot 2.0深度实践之核心技术篇》*

------



### 模式注解(Stereotype Annotations)

#### 模式注解概念描述

模式注解是一种用于声明在应用中扮演“组件”角色的注解。如 Spring Framework 中的 @Repository 标注在任何类上 ，用 于扮演仓储角色的模式注解。

@Component 作为一种由 Spring 容器托管的通用模式组件，任何被 @Component 标准的组件均为组件扫描的候选对象。类 似地，凡是被 @Component 元标注（meta-annotated）的注解，如 @Service ，当任何组件标注它时，也被视作组件扫 描的候选对象。



#### 复习Java注解基础知识   [原文链接](http://www.importnew.com/14227.html)

只简单介绍文中出现的注解以及相关解释，具体注解的详解，可以点解原文链接进行阅读，在此感谢各位前辈的总结。

##### 内建注解

- @Retention：这个注解注在其他注解上，并用来说明如何存储已被标记的注解。这是一种元注解，用来标记注解并提供注解的信息。

  - SOURCE：表明这个注解会被编译器忽略，并只会保留在源代码中。（.java文件）
  - CLASS:表明这个注解会通过编译驻留在CLASS文件，但会被JVM在运行时忽略,正因为如此,其在运行时不可见。(.class文件)
  - RUNTIME：表示这个注解会被JVM获取，并在运行时通过反射获取。(内存字节码)
- @Target:这个注解用于限制某个元素可以被注解的类型。

  - ANNOTATION_TYPE 表示该注解可以应用到其他注解上。

  - CONSTRUCTOR 表示可以使用到构造器上。

  - FIELD 表示可以使用到域或属性上。

  - LOCAL_VARIABLE表示可以使用到局部变量上。

  - METHOD可以使用到方法级别的注解上。

  - PACKAGE可以使用到包声明上。

  - PARAMETER可以使用到方法的参数上。

  - TYPE可以使用到一个类的任何元素上。
- @Documented：被注解的元素将会作为Javadoc产生的文档中的内容。注解都默认不会成为成为文档中的内容。这个注解可以对其它注解使用。
- @Inherited：在默认情况下，注解不会被子类继承。被此注解标记的注解会被所有子类继承。这个注解可以对类使用。
- @Deprecated：说明被标记的元素不应该再度使用。这个注解会让编译器产生警告消息。可以使用到方法，类和域上。相应的解释和原因，包括另一个可取代的方法应该同时和这个注解使用。
- @SuppressWarnings：说明编译器不会针对指定的一个或多个原因产生警告。
- @Override：向编译器说明被注解元素是重写的父类的一个元素。在重写父类元素的时候此注解并非强制性的，不过可以在重写错误时帮助编译器产生错误以提醒我们。比如子类方法的参数和父类不匹配，或返回值类型不同。



#### Spring自定义模式注解

##### @Compent  “派生性”/"层次性"  

  严格上讲 注解是没有派生性和层次性的，之所以这样讲，是因为在spring中的很多注解都是有着派生性和层次性的结构。下面举例说明:

我们拿@Repository注解为例

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Repository {

   /**
    * The value may indicate a suggestion for a logical component name,
    * to be turned into a Spring bean in case of an autodetected component.
    * @return the suggested component name, if any (or empty String otherwise)
    */
   @AliasFor(annotation = Component.class)
   String value() default "";

}
```



由此注解可以看到 Repository 注解的签名方法 `String value() default "";`  然而@Repository 注解之上还有一个 @Component 注解 ,我们再来看一下这个注解。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface Component {

   /**
    * The value may indicate a suggestion for a logical component name,
    * to be turned into a Spring bean in case of an autodetected component.
    * @return the suggested component name, if any (or empty String otherwise)
    */
   String value() default "";

}
```

可以看到 @Componet也有 `String value() default "";`  签名方法。 我们来做一个测试，我们分别使用 @Repository 和 @Component 注解在spring boot 中声明 bean，看看是否都可以获取到该bean。



首先我们先来创建一个 MyRepository的类，并使用 @Repository注解来注释该类。代码如下：

```java
/**
 * @author Neal
 */
@Repository("myRepository")  //设置bean的名称
public class MyRepository {
}
```

然后创建启动类来启动并获取该类，如果能获取到，则证明该类已经加载到了spring 容器中。

启动类代码如下:

```java
/**
 * @author Neal
 * 注解测试启动类 {@link MyRepository}
 * 使用ComponentScan 扫描指定的包
 */
@ComponentScan(basePackages = "org.neal.cn.chaptertwo.stereotype.repository")
public class RepositoryBootStrap {

    public static void main(String[] args) {
        //使用SpringApplicationBuilder 来启动容器
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(RepositoryBootStrap.class).
                        web(WebApplicationType.NONE).run(args);
        //来获取 容器上下文中的 MyRepository Bean 
        MyRepository myRepository = context.getBean("myRepository", MyRepository.class);
        System.out.println("myRepository " + myRepository);
        //关闭上下文
        context.close();
    }
}
```

运行后查看工作台，可以看到 容器内已经加载了声明的bean 。



图1 





接着让我们改变MyRepository 类中的注解，改为 @Componet,其他的不变 

```java
/**
 * @author Neal
 */
@Component("myRepository")  //设置bean的名称
public class MyRepository {
}
```

接着再来执行启动类 ,同样可以查找到声明的bean。 



**之所以先整理这些，是因为springboot 有着大量的模式注解，需要我们层层理解，之后会继续总结**



