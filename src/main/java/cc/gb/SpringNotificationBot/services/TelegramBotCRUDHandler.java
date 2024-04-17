package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventInputState;
import cc.gb.SpringNotificationBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.stereotype.Component;
import cc.gb.SpringNotificationBot.repository.UserRepository;
import cc.gb.SpringNotificationBot.repository.EventRepository;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Slf4j
public class TelegramBotCRUDHandler {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public TelegramBotCRUDHandler(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public void addEvent(Long chatId, EventInputState state) {
        Event event = new Event();
        event.setDescription(state.getDescription());
        event.setTimeOfNotification(state.getTimeOfNotification());
        eventRepository.save(event);
    }

    public void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User(chatId, chat.getUserName(),new TimeStamp());
            userRepository.save(user);
            log.info("User added");
        }
    }
}
