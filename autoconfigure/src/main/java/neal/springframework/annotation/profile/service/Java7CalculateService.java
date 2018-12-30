package neal.springframework.annotation.profile.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *  Java7 for 循环实现
 *
 */
@Profile("java7")
@Service
public class Java7CalculateService implements CalculateService {

    @Override
    public Integer sum(Integer... values) {
        System.out.println("使用 Java7CalculateService 开始计算");
        int sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum+= values[i];
        }
        return sum;
    }
}
