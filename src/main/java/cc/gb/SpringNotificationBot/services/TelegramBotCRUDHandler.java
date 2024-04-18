package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventInputState;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.User;
import cc.gb.SpringNotificationBot.repository.EventRepository;
import cc.gb.SpringNotificationBot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
@Slf4j
public class TelegramBotCRUDHandler {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public TelegramBotCRUDHandler(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public void addEvent(EventInputState state) {
        Event event = new Event();
        event.setDescription(state.getDescription());
        event.setTimeOfNotification(state.getTimeOfNotification());
        event.setStatus(EventStatus.PLANNED);
        eventRepository.save(event);
    }

    public void updateEvent(Long eventId, String newDescription) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.setDescription(newDescription);
        eventRepository.save(event);
    }
    public void deleteEvent(Long eventId){
        eventRepository.delete(eventRepository.getReferenceById(eventId));
    }

    public void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User(chatId, chat.getUserName(), new TimeStamp());
            userRepository.save(user);
            log.info("User added");
        }
    }

    public List<Event> getListEventsByStatus(EventStatus eventStatus) {
        return eventRepository.findByStatusIs(eventStatus);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}