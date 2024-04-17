package cc.gb.SpringNotificationBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.glassfish.grizzly.http.util.TimeStamp;

@Data
@ToString
@Entity(name = "userDataTable")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private Long chatId;

    private String userName;
    private TimeStamp registeredAt;
}
