package tt.haschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableAsync(proxyTargetClass = true)
public class HashcatApplication {
    public static void main(String[] args) {
        SpringApplication.run(HashcatApplication.class, args);
    }
}
