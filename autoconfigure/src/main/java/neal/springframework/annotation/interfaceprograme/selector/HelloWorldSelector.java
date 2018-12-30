package neal.springframework.annotation.interfaceprograme.selector;

import neal.springframework.annotation.interfaceprograme.configuration.HelloWorldConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 注解接口编程的接口
 */
public class HelloWorldSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{HelloWorldConfiguration.class.getName()};
    }
}
