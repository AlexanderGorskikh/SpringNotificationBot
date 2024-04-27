package cc.gb.SpringNotificationBot.repository;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA репозиторий запланированных мероприятий
 */
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusIs(EventStatus eventStatus);
    List<Event> findByUserAndStatusIs(User user,EventStatus status);
    List<Event> findByUserIs(User user);
}
