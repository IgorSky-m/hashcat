package tt.haschat.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("md5.service")
public class Md5Properties {
    private String url;
    private String email;
    private String code;
    private String type;
    private String validationPattern;
}
