package cc.gb.SpringNotificationBot.repository;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * JPA репозиторий запланированных мероприятий
 */
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusIs(EventStatus eventStatus);
    List<Event> findByUserAndStatusIs(User user, EventStatus status);
    @Query("SELECT e FROM Event e WHERE e.user = ?1 AND e.status = ?2 OR e.status = ?3")
    List<Event> findAllActiveEvents(User user, EventStatus eventStatus1, EventStatus eventStatus2);
}
