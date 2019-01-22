package neal.externalized.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class ExternalizePropertyPostProcessor implements EnvironmentPostProcessor,Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

    }

    @Override
    public int getOrder() {
        return ConfigFileApplicationListener.DEFAULT_ORDER  - 1;
    }
}
