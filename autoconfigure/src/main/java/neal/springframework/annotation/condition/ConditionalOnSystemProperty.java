package neal.springframework.annotation.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * java系统属性 条件判断
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnSystemCondition.class)
public @interface ConditionalOnSystemProperty {

    //系统属性名
    String name();

    //系统属性值
    String value();
}
