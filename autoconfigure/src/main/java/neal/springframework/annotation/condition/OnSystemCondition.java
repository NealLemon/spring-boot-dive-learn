package neal.springframework.annotation.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 系统属性条件判断
 */
public class OnSystemCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        /**
         * 获取 {@link ConditionalOnSystemProperty} 的属性集合
         */
        Map<String,Object> attributes = metadata.getAnnotationAttributes(ConditionalOnSystemProperty.class.getName());

        //系统属性名称
        String property = String.valueOf(attributes.get("name"));

        //期望的对应的系统属性值
        String propertyValue = String.valueOf(attributes.get("value"));

        //获取系统属性值
        String javaPropertyValue = System.getProperty(property);

        System.out.println("当前java系统属性" + property + "的值为 ：" + javaPropertyValue);

        boolean flag = javaPropertyValue.equals(propertyValue);
        if(flag) {
            System.out.println("当前java系统属性与@ConditionalOnSystemProperty 中value的值相等，加载bean");
        }else{
            System.out.println("当前java系统属性与@ConditionalOnSystemProperty 中value的值不等，拒绝加载bean");
        }
        //判断期望值和对应的系统属性值 是否一致
        return flag;
    }
}
