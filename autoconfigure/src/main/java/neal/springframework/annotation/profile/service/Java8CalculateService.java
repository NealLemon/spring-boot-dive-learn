package neal.springframework.annotation.profile.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * java8 lambda
 */
@Profile("java8")
@Service
public class Java8CalculateService implements CalculateService {
    @Override
    public Integer sum(Integer... values) {
        System.out.println("使用 Java7CalculateService 开始计算");
        int sum = Stream.of(values).reduce(0,Integer::sum);
        return sum;
    }
}
