package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.config.BotConfiguration;
import cc.gb.SpringNotificationBot.model.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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


    /**
     * Метод захватывающий и обрабатывающий сообщение от пользователя.
     *
     * @param update Класс update представляющий объект с сообщением, текстом и chatId
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            commandMenu(update, EmojiParser.removeAllEmojis(message), chatId);
        } else if (update.hasCallbackQuery()) {
            eventInputHandle(update);
        }
    }

    /**
     * Метод который позволяет "захватывать" процесс ввода даты мероприятия.
     * Он проверяет, начал ли user ввод мероприятия. Если да, его необходимо завершить.
     *
     * @param update класс Update представляющий объект с сообщением, текстом и chatId
     */
    private void eventInputHandle(Update update) {
        var chatId = update.getCallbackQuery().getMessage().getChatId();
        if (eventInputStates.get(chatId) != null) {
            var data = update.getCallbackQuery().getData();
            var messageId = update.getCallbackQuery().getMessage().getMessageId();
            var currentState = eventInputStates.get(chatId);

            if (currentState.isChoiceDay()) {
                int currentMonth = currentState.getChoiceMonth();
                if (data.equals("next")) {
                    currentState.setChoiceMonth(++currentMonth);
                    var date = LocalDate.now().plusMonths(currentMonth);
                    editMessage(chatId, String.format("Выберите день (%s): ", date.getMonth().name()),
                            messageId,
                            inlineCalendarService
                                    .createChoiceKeyboard(
                                            LocalDate.now()
                                                    .plusMonths(currentMonth)));
                    return;
                }
                if (data.equals("prev")) {
                    currentState.setChoiceMonth(--currentMonth);
                    var date = LocalDate.now().plusMonths(currentMonth);
                    editMessage(chatId, String.format("Выберите день (%s): ", date.getMonth().name()),
                            messageId,
                            inlineCalendarService
                                    .createChoiceKeyboard(
                                            LocalDate.now()
                                                    .plusMonths(currentMonth)));
                    return;
                }
                currentState.setMonth(LocalDate.now().plusMonths(currentState.getChoiceMonth()).getMonth());
                currentState.setDay(Integer.parseInt(data));
                editMessage(chatId,
                        "Выберите час: ",
                        messageId,
                        inlineCalendarService.createInlineNumberButtons(24));
                currentState.setChoiceDay(false);
                currentState.setChoiceHour(true);
            } else if (currentState.isChoiceHour()) {
                currentState.setHour(Integer.parseInt(data));
                editMessage(chatId,
                        "Выберите минуту: ",
                        messageId,
                        inlineCalendarService.createInlineNumberButtons(60));
                currentState.setChoiceHour(false);
            } else {
                currentState.setMinute(Integer.parseInt(data));
                processEventDateInput(chatId, messageId);
            }
        }
    }

    /**
     * Меню для зарегистрированных пользователей
     *
     * @param update  класс Update представляющий объект с сообщением, текстом и chatId
     * @param message сообщение пользователя отправленное в чат
     * @param chatId  chatId данного пользователя
     */

    private void commandMenu(Update update, String message, long chatId) {
        switch (message) {
            case "/start" -> {
                if (CRUDHandler.registerUser(update.getMessage()))
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                else sendMessage(chatId, "Вы уже зарегистрированы. Для подробностей нажмите Help", null);
            }
            case "Add event" -> {
                createEvent(chatId);
                log.info("User: {} started creating event", chatId);
            }
            case "Planned events" -> {
                sendEvents(chatId,
                        CRUDHandler.getUserEventsByStatus(chatId, EventStatus.PLANNED),
                        "Список запланированных мероприятий: \n");
                log.info("Sending planned events for user: {}", chatId);
            }
            case "All events" -> {
                sendEvents(chatId,
                        CRUDHandler.getAllUserEvents(chatId),
                        "Список всех мероприятий: \n");
                log.info("Sending all events for user: {}", chatId);
            }
            case "Update event" -> {
                sendEvents(chatId,
                        CRUDHandler.getUserEventsByStatus(chatId, EventStatus.PLANNED),
                        "Введите новое описание мероприятия в формате: \"номер_новое описание\" \n");
                eventInputStates.put(chatId, EventInputState.builder().isUpdate(true).build());
                log.info("user: {} started update event", chatId);
            }
            case "Help" -> {
                sendMessage(chatId, StaticMessages.HELP_MESSAGE, regularKeyboard);
                log.info("Sending help-massage events for user: {}", chatId);
            }
            case "Delete event" -> {
                sendEvents(chatId,
                        CRUDHandler.getAllUserEvents(chatId),
                        "Введите индекс мероприятия, который вы хотите удалить: \n");
                eventInputStates.put(chatId, EventInputState.builder().isDelete(true).build());
            }
            default -> handleMessage(chatId, message);
        }
    }

    /**
     * Метод, который позволяет переслать в сообщении пользователю все Event в списке listEvent
     * определённым образом
     *
     * @param chatId      идентификатор чата
     * @param listEvent   список Event для передачи пользователю
     * @param description текстовое описание каждого мероприятия
     */
    private void sendEvents(long chatId, Iterable<Event> listEvent, String description) {
        var sb = new StringBuilder();
        if (description != null) sb.append(description);
        listEvent.forEach(event ->
                sb.append(event.getId()).append(" ")
                        .append(event.getStatus()).append(" : ")
                        .append(event.getDescription()).append("\n")
                        .append("Запланирован на ")
                        .append(event.getTimeOfNotification().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                        .append("\n"));
        sendMessage(chatId, sb.toString(), regularKeyboard);
    }

    /**
     * Метод который вызывается при команде /start и выводит приветственное сообщение
     *
     * @param chatId идентификатор чата
     * @param name   имя пользователя
     */
    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + " nice to meet you &#128075;:blush: ");
        sendSticker(chatId, new InputFile(Stickers.Hi.getKey()));
        log.info("Replied to user {}", name);
        sendMessage(chatId, answer, regularKeyboard);
    }

    /**
     * Базовый метод для отправки сообщений
     *
     * @param chatId   Идентификатор чата
     * @param msg      Сообщение, которое будет переслано в чат
     * @param keyboard Постоянная клавиатура
     */
    public void sendMessage(long chatId, String msg, ReplyKeyboardMarkup keyboard) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(msg);
        message.setReplyMarkup(keyboard);
        executeMessage(message);
    }

    /**
     * Метод который позволяет редактировать сообщение
     *
     * @param chatId    Идентификатор чата
     * @param msg       Новое сообщение которое будет переслано в чат
     * @param messageId Идентификатор конкретного сообщения, которое будет изменено
     * @param markup    Клавиатура, которая присоединяется к сообщению
     */
    public void editMessage(
            long chatId,
            String msg,
            Integer messageId,
            InlineKeyboardMarkup markup) {
        var editedMessage = new EditMessageText();
        editedMessage.setChatId(chatId);
        editedMessage.setMessageId(messageId);
        if (markup != null) editedMessage.setReplyMarkup(markup);
        editedMessage.setText(msg);
        executeMessage(editedMessage);
    }

    /**
     * Отправка приветственного стикера при регистрации
     *
     * @param chatId    - chatId user-a
     * @param inputFile - стикер
     */
    public void sendSticker(Long chatId, InputFile inputFile) {
        var sendSticker = new SendSticker();
        sendSticker.setChatId(chatId);
        sendSticker.setSticker(inputFile);
        try {
            execute(sendSticker);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вспомогательный метод для отправки обычных сообщений
     *
     * @param message Объект инкапсулирующий сообщение
     */
    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для редактирования сообщений (перегруженный)
     *
     * @param message Объект инкапсулирующий сообщение
     */
    private void executeMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

    /**
     * Метод который создаёт постоянную клавиатуру
     *
     * @return Объект постоянной клавиатуры
     */
    private ReplyKeyboardMarkup createRegularKeyboard() {
        var keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        var row = new KeyboardRow();
        row.add(EmojiParser.parseToUnicode("&#128161;Add event"));
        row.add(EmojiParser.parseToUnicode("&#128269;Planned events"));
        row.add(EmojiParser.parseToUnicode("&#128203;Update event"));
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("Help");
        row.add(EmojiParser.parseToUnicode("&#128197;All events"));
        row.add(EmojiParser.parseToUnicode("&#10060;Delete event"));
        keyboardRows.add(row);
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    /**
     * Стартовый метод цепочки сохранения мероприятия,
     * последующие действия перехватываются в handleMessage()
     *
     * @param chatId Идентификатор чата
     */
    private void createEvent(Long chatId) {
        sendMessage(chatId, "Введите описание мероприятия:", regularKeyboard);
        eventInputStates.put(chatId, EventInputState.builder().build());
    }

    /**
     * Метод обновления мероприятия
     *
     * @param chatId         Идентификатор чата
     * @param eventId        Идентификатор мероприятия
     * @param newDescription Новый текст мероприятия
     */
    private void updateEvent(Long chatId, Long eventId, String newDescription) {
        CRUDHandler.updateEvent(eventId, newDescription);
        eventInputStates.remove(chatId);
        sendEvents(chatId, CRUDHandler.getAllEventsByStatus(EventStatus.PLANNED), null);
    }

    /**
     * Метод удаления мероприятия
     *
     * @param chatId  Идентификатор чата
     * @param eventId Идентификатор мероприятия
     */
    private void deleteEvent(Long chatId, Long eventId) {
        CRUDHandler.deleteEvent(eventId);
        sendEvents(chatId, CRUDHandler.getAllEventsByStatus(EventStatus.PLANNED), null);
    }

    /**
     * Вспомогательный метод для выбора дня при создании новой записи о мероприятии
     *
     * @param chatId  Идентификатор чата
     * @param message Текст сообщения
     */
    private void processEventDescriptionInput(Long chatId, String message) {
        var state = eventInputStates.get(chatId);
        state.setDescription(message);
        var sendMessage = inlineCalendarService
                .createMessageWithDayButtons(
                        chatId,
                        LocalDate.now().plusMonths(state.getChoiceMonth()));
        state.setChoiceDay(true);
        executeMessage(sendMessage);
    }

    /**
     * Вспомогательный метод для выбора часа при создании новой записи о мероприятии, а также
     * добавления в базу данных
     *
     * @param chatId    Идентификатор чата
     * @param messageId Идентификатор конкретного сообщения в чате
     */
    private void processEventDateInput(Long chatId, Integer messageId) {
        var state = eventInputStates.get(chatId);
        var localdate = LocalDate.now();
        var dateTime = LocalDateTime.of(
                localdate.getYear(),
                state.getMonth(),
                state.getDay(),
                state.getHour(),
                state.getMinute(), 0);
        state.setTimeOfNotification(dateTime);
        CRUDHandler.addEvent(state, chatId);
        editMessage(chatId, "Событие успешно добавлено!", messageId, null);
        eventInputStates.remove(chatId);
    }

    /**
     * Метод, служащий для обработки сообщений от пользователя
     *
     * @param chatId  Идентификатор чата
     * @param message Сообщение от пользователя
     */
    private void handleMessage(Long chatId, String message) {
        if (!eventInputStates.containsKey(chatId)) {
            sendMessage(chatId, "Неизвестная команда. Используйте Help для получения справки.", regularKeyboard);
            return;
        }
        var state = eventInputStates.get(chatId);
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
