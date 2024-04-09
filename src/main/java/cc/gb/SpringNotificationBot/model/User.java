package cc.gb.SpringNotificationBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.glassfish.grizzly.http.util.TimeStamp;

@Getter
@Setter
@ToString
@Entity(name = "userDataTable")
public class User {
    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private TimeStamp registeredAt;
}
