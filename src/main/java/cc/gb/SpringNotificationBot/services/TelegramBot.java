package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.config.BotConfiguration;
import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventInputState;
import cc.gb.SpringNotificationBot.model.EventStatus;
import cc.gb.SpringNotificationBot.model.StaticMessages;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * todo
 * 1. Добавить кнопку и реализовать логику вывода списка заметок +
 * 2. Добавить возможность удаления заметки +
 * 3. Добавить возможность обновления статуса, редактирования заметки+
 * 4. Добавить сервис CRUD для данного сервиса ---
 * 5. Добавить документацию к методам -
 * 6. Добавить логику уведомлений -
 */
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfiguration botConfiguration;
    private final Map<Long, EventInputState> eventInputStates = new HashMap<>();
    private final ReplyKeyboardMarkup regularKeyboard;
    private final TelegramBotCRUDHandler CRUDHandler;
    private final InlineCalendarService inlineCalendarService;


    public TelegramBot(BotConfiguration botConfiguration,
                       TelegramBotCRUDHandler crudHandler
            , InlineCalendarService inlineCalendarService) {
        this.botConfiguration = botConfiguration;
        this.CRUDHandler = crudHandler;
        this.inlineCalendarService = inlineCalendarService;
        regularKeyboard = createRegularKeyboard();
    }


    @Override
    public String getBotUsername() {
        return botConfiguration.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfiguration.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (message) {
                case "/start" -> {
                    CRUDHandler.registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                }
                case "Add event" -> {
                    createEvent(chatId);
                }
                case "Planned events" -> {
                    sendEvents(chatId,
                            CRUDHandler.getUserEventsByStatus(chatId, EventStatus.PLANNED),
                            "Список запланированных мероприятий");
                }
                case "All events" -> {
                    sendEvents(chatId,
                            CRUDHandler.getAllUserEvents(chatId),
                            "Список всех мероприятий: \n");
                }
                case "Update event" -> {
                    sendEvents(chatId,
                            CRUDHandler.getUserEventsByStatus(chatId, EventStatus.PLANNED),
                            "Введите новое описание мероприятия в формате: \"номер_новое описание\" \n");
                    var inputState = new EventInputState();
                    inputState.setUpdate(true);
                    eventInputStates.put(chatId, inputState);
                }
                case "Help" -> {
                    sendMessage(chatId, StaticMessages.HELP_MESSAGE, regularKeyboard);
                }
                case "Delete event" -> {
                    sendEvents(chatId,
                            CRUDHandler.getAllUserEvents(chatId),
                            "Введите индекс мероприятия, который вы хотите удалить: \n");
                    var inputState = new EventInputState();
                    inputState.setDelete(true);
                    eventInputStates.put(chatId, inputState);
                }
                default -> {
                    handleMessage(chatId, message);
                }
            }
        } else if (update.hasCallbackQuery()) {

            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (eventInputStates.get(chatId) != null) {
                String data = update.getCallbackQuery().getData();
                Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                processEventDateInput(chatId, data, messageId);
            }
        }
    }

    /**
     * Метод который позволяет переслать в сообщении пользователю все Event в списке listEvent
     * определённым образом
     *
     * @param chatId    идентификатор чата
     * @param listEvent список Event для передачи пользователю
     */
    private void sendEvents(long chatId, Iterable<Event> listEvent, String description) {
        StringBuilder sb = new StringBuilder();
        if (description != null) sb.append(description);
        listEvent.forEach(event ->
                sb.append(event.getId()).append(" ")
                        .append(event.getStatus()).append(" : ")
                        .append(event.getDescription()).append("\n"));
        sendMessage(chatId, sb.toString(), regularKeyboard);
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + " nice to meet you :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer, regularKeyboard);
    }

    public void sendMessage(long chatId, String msg, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(msg);
        message.setReplyMarkup(keyboard);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup createRegularKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Add event");
        row.add("Planned events");
        row.add("Update event");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("Help");
        row.add("All events");
        row.add("Delete event");
        keyboardRows.add(row);
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    private void createEvent(Long chatId) {
        sendMessage(chatId, "Введите описание мероприятия:", regularKeyboard);
        eventInputStates.put(chatId, new EventInputState());
    }

    private void updateEvent(Long chatId, Long eventId, String newDescription) {
        CRUDHandler.updateEvent(eventId, newDescription);
        eventInputStates.remove(chatId);
        sendEvents(chatId, CRUDHandler.getAllEventsByStatus(EventStatus.PLANNED), null);
    }

    private void deleteEvent(Long chatId, Long eventId) {
        CRUDHandler.deleteEvent(eventId);
        sendEvents(chatId, CRUDHandler.getAllEventsByStatus(EventStatus.PLANNED), null);
    }

    private void processEventDescriptionInput(Long chatId, String message) {
        EventInputState state = eventInputStates.get(chatId);
        state.setDescription(message);
        SendMessage sendMessage = inlineCalendarService.createMessageWithDayButtons(chatId);

        executeMessage(sendMessage);
    }

    private void processEventDateInput(Long chatId, String dayOfMonth, Integer messageId) {
        EventInputState state = eventInputStates.get(chatId);
        LocalDate localdate = LocalDate.now();
        LocalDateTime dateTime = LocalDateTime.of(
                localdate.getYear(),
                localdate.getMonth(),
                Integer.parseInt(dayOfMonth),
                0, 0, 0);
        state.setTimeOfNotification(dateTime);
        CRUDHandler.addEvent(state, chatId);
        EditMessageText editedMessage = new EditMessageText();
        editedMessage.setChatId(chatId);
        editedMessage.setMessageId(messageId);
        editedMessage.setText("Событие успешно добавлено!");
        executeMessage(editedMessage);
        eventInputStates.remove(chatId);
    }


    private void handleMessage(Long chatId, String message) {
        if (!eventInputStates.containsKey(chatId)) {
            sendMessage(chatId, "Неизвестная команда. Используйте Help для получения справки.", regularKeyboard);
            return;
        }
        EventInputState state = eventInputStates.get(chatId);
        if (state.isUpdate()) {
            try {
                String[] tmp = message.split(" ");
                Long eventId = Long.parseLong(tmp[0]);
                StringBuilder sb = new StringBuilder();
                Arrays.stream(tmp).skip(1).forEach(s -> sb.append(s).append(" "));
                updateEvent(chatId, eventId, sb.toString());
            } catch (Exception e) {
                sendMessage(chatId, "Вы ввели не число, попробуйте снова", regularKeyboard);
            }
        } else if (state.isDelete()) {
            try {
                deleteEvent(chatId, Long.parseLong(message));
            } catch (Exception e) {
                sendMessage(chatId, "Неверный формат или мероприятие не найдено", regularKeyboard);
            }
        } else {
            if (state.getDescription() == null) {
                processEventDescriptionInput(chatId, message);
            }
        }
    }
}
