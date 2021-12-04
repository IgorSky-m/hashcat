package tt.haschat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

//TODO сделать джобу, которая будет удалять записи, у котолых истек срок. Срок установить через проперти
@Entity(name = "response")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    //хеш
    @Id
    private String hash;

    //результат расшифровки
    private String result;

    //дата последнего получения
    @JsonIgnore
    private Date dtLastRetrieval;



}
