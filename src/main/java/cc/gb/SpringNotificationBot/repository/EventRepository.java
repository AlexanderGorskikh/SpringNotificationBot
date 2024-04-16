package cc.gb.SpringNotificationBot.repository;

import cc.gb.SpringNotificationBot.model.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Long> {
}
