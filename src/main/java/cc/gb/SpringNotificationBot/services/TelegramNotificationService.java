package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

/**
 * Сервис, отвечающий за рассылку уведомлений,
 * использующий аннотацию Scheduled,
 * оповещающий пользователя о внесенным им уведомлении
 */
@Component
@EnableScheduling
public class TelegramNotificationService {

    private final TelegramBotCRUDHandler crudHandler;
    private TelegramBot telegramBot;


    public TelegramNotificationService(TelegramBotCRUDHandler crudHandler, TelegramBot telegramBot) {
        this.crudHandler = crudHandler;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkNotification() {

        List<Event> notifications = crudHandler.getListEventsByStatus(EventStatus.PLANNED);
        StringBuilder notification = new StringBuilder();

        for (Event event : notifications) {
            LocalDateTime eventTime = event.getTimeOfNotification();
            LocalDateTime now = LocalDateTime.now();
            if (eventTime.isAfter(now) && eventTime.minusMinutes(30).isBefore(now)) {
                notification.append("У вас запланировано: ");
                notification.append(event.getDescription());
                notification.append(" через полчаса!");
                telegramBot.sendMessage(event.getUser().getChatId(), notification.toString(), null);
                notification.setLength(0);
            } else if (eventTime.isAfter(now) && eventTime.minusMinutes(10).isBefore(now)) {
                notification.append("У вас запланировано: ");
                notification.append(event.getDescription());
                notification.append(" через десять минут!");
                telegramBot.sendMessage(event.getUser().getChatId(), notification.toString(), null);
                notification.setLength(0);
            }
            if (event.getTimeOfNotification().isAfter(now)) {
                notification.append("У вас запланировано!");
                notification.append(event.getDescription());
                telegramBot.sendMessage(event.getUser().getChatId(), notification.toString(), null);
            }
        }
    }
}
