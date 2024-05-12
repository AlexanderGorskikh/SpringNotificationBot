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

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * @param state  вспомогательный класс для хранения состояния ввода мероприятия
     * @param chatId идентификатор чата
     */
    public void addEvent(EventInputState state, Long chatId) {
        eventRepository.save(
                Event.builder()
                        .description(state.getDescription())
                        .user(userRepository.findById(chatId).orElse(null))
                        .timeOfNotification(state.getTimeOfNotification())
                        .timeOfCreation(LocalDateTime.now())
                        .status(EventStatus.PLANNED)
                        .build());
    }


    /**
     * @param eventId        идентификатор мероприятия
     * @param newDescription новое описание мероприятия
     */
    public void updateEvent(Long eventId, String newDescription) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.setDescription(newDescription);
        eventRepository.save(event);
    }

    /**
     * @param eventId идентификатор мероприятия
     */
    public void deleteEvent(Long eventId) {
        var event = eventRepository.findById(eventId).orElseThrow();
        event.setStatus(EventStatus.CANCELED);
        eventRepository.save(event);
    }

    /**
     * @param msg сообщение из телеграм бота
     * @return успешность добавления пользователя
     */
    public boolean registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            userRepository.save(
                    User.builder()
                            .chatId(chatId)
                            .userName(chat.getUserName())
                            .registeredAt(new TimeStamp())
                            .eventList(new ArrayList<>())
                            .build());
            log.info("User added");
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param eventStatus статус уведомления
     * @return список всех мероприятий по статусу
     */

    public List<Event> getAllEventsByStatus(EventStatus eventStatus) {
        return eventRepository.findByStatusIs(eventStatus);
    }

    /**
     * @param chatId      идентификатор чата
     * @param eventStatus статус уведомления
     * @return список всех мероприятий пользователя по статусу
     */
    public List<Event> getUserEventsByStatus(Long chatId, EventStatus eventStatus) {
        return eventRepository.findByUserAndStatusIs(
                userRepository.findById(chatId).orElseThrow(), eventStatus);
    }

    /**
     * @param chatId идентификатор чата
     * @return список всех мероприятий пользователя кроме отмененных
     */
    public List<Event> getAllUserEvents(Long chatId) {
        return eventRepository.findAllActiveEvents(
                userRepository.findById(chatId).orElseThrow(),
                EventStatus.PLANNED, EventStatus.FINISHED);
    }

    public void updateEventStatus(Long eventId, EventStatus eventStatus) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.setStatus(eventStatus);
        eventRepository.save(event);
        log.info("Event with id {} status updated",eventId);
    }
}
