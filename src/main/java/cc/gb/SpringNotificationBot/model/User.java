package cc.gb.SpringNotificationBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.glassfish.grizzly.http.util.TimeStamp;

import java.util.List;

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
    @OneToMany(mappedBy = "user")
    private List<Event> eventList;
}
