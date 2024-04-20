package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис, отвечающий за рассылку уведомлений,
 * использующий аннотацию Scheduled,
 * оповещающий пользователя о внесенным им уведомлени
 */
@Component
public class TelegramNotificationService {

    private final TelegramBotCRUDHandler crudHandler;
    private TelegramBot telegramBot;


    public TelegramNotificationService(TelegramBotCRUDHandler crudHandler, TelegramBot telegramBot) {
        this.crudHandler = crudHandler;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkNotification() {
        List<Event> notifications = crudHandler.getAllEventsByStatus(EventStatus.PLANNED);
        for (Event event : notifications) {
            LocalDateTime now = LocalDateTime.now();
            if (event.getTimeOfNotification().isBefore(now)) {
                String message = "У вас запланировано: " + event.getDescription() + " на " + event.getTimeOfNotification();
                telegramBot.sendMessage(event.getUser().getChatId(), message, null);
                System.out.println("Notification");
            }
        }
    }
}
