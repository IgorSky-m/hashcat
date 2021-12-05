package tt.haschat.services.api;

public interface IMsgSenderService {
    void sendText(String to, String subj, String text);

    String getBaseSubjectText(String subjectType);
}
