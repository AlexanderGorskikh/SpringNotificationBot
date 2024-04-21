package cc.gb.SpringNotificationBot.repository;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusIs(EventStatus eventStatus);
    List<Event> findByUserIs(User user);
}
