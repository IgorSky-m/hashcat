package tt.haschat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tt.haschat.dto.api.State;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ДТО запроса
 */
@Data
@Entity(name = "request")
public class Request {

    //Идентификатор
    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    //Дата создания запроса
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dtCreate;

    //Дата последнего обновления запроса
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dtUpdate;


    //Статус запроса
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    private State state;

    //Email для ответа на запрос
    private String email;


    //Данные для обработки запроса
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> hashes;

    //Ответы
    @Transient
    private List<Response> responses;


}
