package cc.gb.SpringNotificationBot;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventInputState;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.User;
import cc.gb.SpringNotificationBot.repository.EventRepository;
import cc.gb.SpringNotificationBot.repository.UserRepository;
import cc.gb.SpringNotificationBot.services.TelegramBotCRUDHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TelegramBotCRUDHandlerTests {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    Message message;
    @InjectMocks
    private TelegramBotCRUDHandler crudHandler;

    @Test
    public void successDeleteEvent() {
        Event event = new Event();
        event.setId(1L);
        given(eventRepository.findById(event.getId())).willReturn(Optional.of(event));
        given(eventRepository.save(event)).willReturn(event);
        crudHandler.deleteEvent(event.getId());
        assertEquals(eventRepository.findById(event.getId()).get().getStatus(), EventStatus.CANCELED);
    }

    @Test
    public void successAddEvent() {
        var chatId = 1L;
        given(userRepository.findById(chatId)).willReturn(
                Optional.of(User.builder().chatId(chatId).build()));
        var state = EventInputState.builder()
                .description("test")
                .timeOfNotification(LocalDateTime.now())
                .build();
        crudHandler.addEvent(state, chatId);
    }

    @Test
    public void successUpdateEvent() {
        var chatId = 1L;
        var lastEvent = Event.builder()
                .description("last event")
                .build();
        var newDescription = "new event";
        given(eventRepository.findById(chatId)).willReturn(Optional.of(lastEvent));
        crudHandler.updateEvent(chatId, newDescription);
        assertEquals(eventRepository.findById(chatId).get().getDescription(), newDescription);
    }

    @Test
    public void successRegisterUser() {
        var chatId = 1L;
        given(message.getChatId()).willReturn(chatId);
        given(userRepository.findById(chatId)).willReturn(Optional.of(User.builder().chatId(chatId).build()));
        crudHandler.registerUser(message);
        assertEquals(userRepository.findById(chatId).get().getChatId(), chatId);
    }

    @Test
    public void getAllUserEvents() {
        var chatId = 1L;
        given(userRepository.findById(chatId))
                .willReturn(
                        Optional.of(
                                User.builder()
                                        .chatId(chatId)
                                        .build()));
        given(eventRepository.findAllActiveEvents(
                userRepository.findById(chatId).get(),
                EventStatus.PLANNED,
                EventStatus.FINISHED))
                .willReturn(List.of(
                        Event.builder().status(EventStatus.PLANNED).build(),
                        Event.builder().status(EventStatus.FINISHED).build()));
        var events = crudHandler.getAllUserEvents(chatId);
        assertFalse(events.stream().anyMatch(
                event -> event.getStatus().equals(EventStatus.CANCELED)));
    }
}
