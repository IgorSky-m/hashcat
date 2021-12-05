package tt.haschat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

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

}
