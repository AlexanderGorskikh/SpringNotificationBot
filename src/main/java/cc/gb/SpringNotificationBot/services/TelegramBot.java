package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.config.BotConfiguration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private BotConfiguration botConfiguration;

    public TelegramBot(BotConfiguration botConfiguration) {
        this.botConfiguration = botConfiguration;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (message) {
                case "/start" -> {
                    startCommandReceived(chatId, message);
                }
                default -> {
                    sendMessage(chatId,"Пока функция не поддерживается, ");
                }
            }
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

    private void startCommandReceived(long chatId, String msg) {
        String answer = "Hi " + msg;
        sendMessage(chatId, msg);
    }

    private void sendMessage(long chatId, String msg) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
