
package star.xingxing.mall;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xingxing
 * @email 2064989403@qq.com
 */
@MapperScan(basePackages = "star.xingxing.mall.dao")
@SpringBootApplication
@Slf4j
public class StarMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarMallApplication.class, args);
        log.error("localtion:http://localhost:8001/");
    }
}
