package tt.haschat.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tt.haschat.config.AsyncConfig;
import tt.haschat.config.properties.MailBaseProperties;
import tt.haschat.services.api.IMsgSenderService;

@Service
public class EmailSenderService implements IMsgSenderService {

    private final JavaMailSender mailSender;
    private final MailBaseProperties mailBaseProperties;

    public EmailSenderService(JavaMailSender mailSender, MailBaseProperties mailBaseProperties) {
        this.mailSender = mailSender;
        this.mailBaseProperties = mailBaseProperties;
    }

    @Async(AsyncConfig.ASYNC_EXECUTOR_BEAN_NAME)
    @Override
    public void sendText(String to, String subj, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subj);
        msg.setText(text);
        this.mailSender.send(msg);
    }

    public String getBaseSubjectText(String subjectType){
        return this.mailBaseProperties.getSubjects().get(subjectType);
    }

    public enum DefaultSubjectTypes{
        CONFIRM("confirm");

        public final String type;

        DefaultSubjectTypes(String type){
            this.type = type;
        }
    }
}
