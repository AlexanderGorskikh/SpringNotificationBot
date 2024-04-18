package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Класс отвечающий за получение списка
 * ползователелей и рассылающий им уведомление
 *
 */
@Component
public class TelegramNotificationService {

    private final TelegramBotCRUDHandler crudHandler;
    private TelegramBot telegramBot;

    public TelegramNotificationService(TelegramBotCRUDHandler crudHandler, TelegramBot telegramBot) {
        this.crudHandler = crudHandler;
        this.telegramBot = telegramBot;
        checkNotification();
    }

    @Scheduled(fixedDelay = 60000)
    public void checkNotification() {
        List<Event> notifications = crudHandler.getListEventsByStatus(EventStatus.PLANNED);
        for (Event event : notifications) {
            LocalDateTime now = LocalDateTime.now();
            if (event.getTimeOfNotification().isBefore(now)) {
                String message = "IT'S UR EVENT TIME";
                telegramBot.sendMessage(event.getUser().getChatId(), message, null);
                System.out.println("Notification");
            }
        }
    }
}
