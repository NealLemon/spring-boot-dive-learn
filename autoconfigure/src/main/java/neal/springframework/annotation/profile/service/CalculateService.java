package neal.springframework.annotation.profile.service;

/**
 * 计算服务接口
 */
public interface CalculateService {

    /**
     * 求和
     * @param values  多个整数
     * @return sum 累加值
     */
    Integer sum(Integer... values);
}
