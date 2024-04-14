package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.config.BotConfiguration;
import cc.gb.SpringNotificationBot.model.CallbackDataType;
import cc.gb.SpringNotificationBot.model.User;
import cc.gb.SpringNotificationBot.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfiguration botConfiguration;
    @Autowired
    private UserRepository userRepository;
    private final static String HELP_MESSAGE = "Welcome to our demonstration bot";
    private final ReplyKeyboardMarkup regularKeyboard;

    public TelegramBot(BotConfiguration botConfiguration) {
        this.botConfiguration = botConfiguration;
        regularKeyboard = createRegularKeyboard();
        addListOfCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (message) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                }
                case "my_data" -> {

                }
                case "/delete_data" -> {
                }
                case "/settings" -> {
                }
                case "/help" -> {
                    sendMessage(chatId, HELP_MESSAGE, regularKeyboard);
                }
                case "/register" ->{
                    register(chatId);
                }
                default -> {
                    sendMessage(chatId, "Пока функция не поддерживается, ", regularKeyboard);
                }
            }
        } else if (update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            switch (callbackData){
                case CallbackDataType.YES_BUTTON -> {
                    String text = "You pressed YES button";
                    EditMessageText editedMessage = new EditMessageText();
                    editedMessage.setChatId(chatId);
                    editedMessage.setText(text);
                    editedMessage.setMessageId((int) messageId);
                    try {
                        execute(editedMessage);
                    } catch (TelegramApiException e) {
                        System.out.println("ошибка");
                        log.error("Error occurred: " + e.getMessage());
                    }
                }
                case CallbackDataType.NO_BUTTON -> {
                    String text = "You pressed NO button";
                    EditMessageText editedMessage = new EditMessageText();
                    editedMessage.setChatId(chatId);
                    editedMessage.setText(text);
                    editedMessage.setMessageId((int) messageId);
                    try {
                        execute(editedMessage);
                    } catch (TelegramApiException e) {
                        System.out.println("ошибка");
                        log.error("Error occurred: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you want to registered?");
        var inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();

        yesButton.setText("yes");
        yesButton.setCallbackData(CallbackDataType.YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("no");
        noButton.setCallbackData(CallbackDataType.NO_BUTTON);

        rowInline.add(yesButton);
        rowInline.add(noButton);
        rowsInLine.add(rowInline);
        inlineKeyboard.setKeyboard(rowsInLine);
        message.setReplyMarkup(inlineKeyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("ошибка");
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new TimeStamp());
            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfiguration.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfiguration.getBotToken();
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + " nice to meet you" + " :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer, regularKeyboard);
    }

    private void sendMessage(long chatId, String msg, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(msg);
        message.setReplyMarkup(keyboard);

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
        row.add("button1");
        row.add("button2");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("button3");
        row.add("button4");
        row.add("button5");
        keyboardRows.add(row);
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    private void addListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start program"));
        listOfCommands.add(new BotCommand("/register", "start register"));
        listOfCommands.add(new BotCommand("/my_data", "get data stored"));
        listOfCommands.add(new BotCommand("/delete_data", "delete my data"));
        listOfCommands.add(new BotCommand("/settings", "list of preferences"));
        listOfCommands.add(new BotCommand("/help", "get help message"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {
            log.error("Error settings bot command list" + e.getMessage());
        }
    }
}
