package cc.gb.SpringNotificationBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;


/**
 * Сущность - мероприятие
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Events")
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Basic
    private LocalDateTime timeOfCreation;
    @Basic
    private LocalDateTime timeOfNotification;
    private EventStatus status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
