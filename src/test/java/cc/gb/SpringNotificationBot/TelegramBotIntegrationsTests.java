package cc.gb.SpringNotificationBot;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventInputState;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.User;
import cc.gb.SpringNotificationBot.repository.EventRepository;
import cc.gb.SpringNotificationBot.repository.UserRepository;
import cc.gb.SpringNotificationBot.services.TelegramBotCRUDHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@SpringBootTest
public class TelegramBotIntegrationsTests {

    @MockBean
    private EventRepository eventRepository;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private TelegramBotCRUDHandler crudHandler;
    @Mock
    private Message message;

    @Test
    void addEventSuccessFlow() {
        Long id = 1L;
        given(userRepository.findById(id)).willReturn(Optional.of(User.builder().build()));
        EventInputState eventInputState = EventInputState.builder().description("TestDescription").
                timeOfNotification(LocalDateTime.now()).build();
        Event event = Event.builder()
                .description(eventInputState.getDescription())
                .user(userRepository.findById(id).orElse(null))
                .timeOfNotification(eventInputState.getTimeOfNotification())
                .timeOfCreation(LocalDateTime.now()).status(EventStatus.PLANNED).build();
        crudHandler.addEvent(eventInputState, id);
    }

    @Test
    void updateEventSuccessFlow() {
        Long eventId = 1L;
        Event event = Event.builder()
                .id(eventId).description("newDescription").build();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        crudHandler.updateEvent(eventId, "newDescription");
        assertEquals(event.getDescription(), eventRepository.findById(eventId).orElse(null).getDescription());

    }

    @Test
    void deleteEventSuccessFlow() {
        Long eventId = 1L;
        Event event = Event.builder()
                .id(1L)
                .status(EventStatus.CANCELED)
                .build();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        crudHandler.deleteEvent(eventId);
        assertEquals(event.getStatus(), eventRepository.findById(eventId).orElse(null).getStatus());
        ;
    }

    @Test
    void alreadyRegisterUserSuccessFlow() {
        var chatId = 1L;
        given(message.getChatId()).willReturn(chatId);
        given(userRepository.findById(chatId)).willReturn(Optional.of(User.builder().chatId(chatId).build()));
        boolean result = crudHandler.registerUser(message);
        assertFalse(result);
    }


    @Test
    void getAllUserEvents() {
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
