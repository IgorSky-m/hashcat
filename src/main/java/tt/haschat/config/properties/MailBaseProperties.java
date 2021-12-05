package tt.haschat.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties("hashcat.tt.email.base")
public class MailBaseProperties {

    private Map<String, String> subjects;


}
