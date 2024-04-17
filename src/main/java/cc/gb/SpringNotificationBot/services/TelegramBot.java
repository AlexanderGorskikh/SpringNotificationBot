package cc.gb.SpringNotificationBot.services;

import  cc.gb.SpringNotificationBot.config.BotConfiguration;
import cc.gb.SpringNotificationBot.model.*;
import cc.gb.SpringNotificationBot.repository.EventRepository;
import cc.gb.SpringNotificationBot.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public TelegramBot(BotConfiguration botConfiguration, TelegramBotCRUDHandler crudHandler) {
        this.botConfiguration = botConfiguration;
        this.CRUDHandler = crudHandler;
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
                case "Get events" -> {

                }
                case "Update event" -> {

                }
                case "Help" -> {
                    sendMessage(chatId, StaticMessages.HELP_MESSAGE, regularKeyboard);
                }
                case "Delete event" -> {
                }
                default -> {
                    handleMessage(chatId, message);
                }
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + " nice to meet you :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer, regularKeyboard);
    }

    private void sendMessage(long chatId, String msg, ReplyKeyboardMarkup keyboard) {
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

    private ReplyKeyboardMarkup createRegularKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Add event");
        row.add("Get events");
        row.add("Update event");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("Help");
        row.add("Delete event");
        keyboardRows.add(row);
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    private void createEvent(Long chatId) {
        sendMessage(chatId, "Введите описание мероприятия:", regularKeyboard);
        eventInputStates.put(chatId, new EventInputState());
    }

    private void processEventDescriptionInput(Long chatId, String message) {
        EventInputState state = eventInputStates.get(chatId);
        state.setDescription(message);
        sendMessage(chatId, "Введите дату события в формате \"dd MM YY hh mm\":", regularKeyboard);
    }

    private void processEventDateInput(Long chatId, String message) {
        EventInputState state = eventInputStates.get(chatId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yy HH mm");
        LocalDateTime dateTime = LocalDateTime.parse(message, formatter);
        state.setTimeOfNotification(dateTime);
        CRUDHandler.addEvent(chatId, state);
        sendMessage(chatId, "Событие успешно добавлено!", regularKeyboard);
        eventInputStates.remove(chatId);
    }


    private void handleMessage(Long chatId, String message) {
        if (!eventInputStates.containsKey(chatId)) {
            sendMessage(chatId, "Неизвестная команда. Используйте Help для получения справки.", regularKeyboard);
            return;
        }
        EventInputState state = eventInputStates.get(chatId);
        if (state.getDescription() == null) {
            processEventDescriptionInput(chatId, message);
        } else if (state.getTimeOfNotification() == null) {
            processEventDateInput(chatId, message);
        }
    }


}
