package cc.gb.SpringNotificationBot.services;

import cc.gb.SpringNotificationBot.model.Event;
import cc.gb.SpringNotificationBot.model.EventStatus;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
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

    /**
     * Метод, отвечающий за рассылку уведомлений о внесенном
     * пользователе событии. Используя аннотацию Schedule
     * и потоки, каждую минуту мы проверяем подошло ли время.
     * Уведомления приходят за 30, 10 минут и во время события.
     * После отправки уведомления, событие переходит в статус завершенного.
     */

    @Scheduled(fixedDelay = 60000)
    public void checkNotification() {

        List<Event> notifications = crudHandler.getAllEventsByStatus(EventStatus.PLANNED);
        StringBuilder notification = new StringBuilder();

        for (Event event : notifications) {
            LocalDateTime eventTime = event.getTimeOfNotification();
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

            if (eventTime.isAfter(now)) {
                LocalDateTime halfAnHourBefore = eventTime.truncatedTo(ChronoUnit.MINUTES).minusMinutes(30);
                LocalDateTime tenMinutesBefore = eventTime.truncatedTo(ChronoUnit.MINUTES).minusMinutes(10);

                if (now.isEqual(halfAnHourBefore)) {
                    sendEventNotification(notification, event, " через полчаса!");
                } else if (now.isEqual(tenMinutesBefore)) {
                    sendEventNotification(notification, event, " через десять минут!");
                }
            }
            if (now.equals(eventTime.truncatedTo(ChronoUnit.MINUTES))) {
                sendEventNotification(notification, event, " уже сейчас!");
                event.setStatus(EventStatus.FINISHED);
                crudHandler.updateEventStatus(event.getId(), EventStatus.FINISHED);
            }
        }
    }

    private void sendEventNotification(StringBuilder notification, Event event, String str) {
        notification.append("У вас запланировано: ");
        notification.append(event.getDescription());
        notification.append(str);
        telegramBot.sendMessage(event.getUser().getChatId(), notification.toString(), null);
        notification.setLength(0);
    }


    private boolean isNotificationTime(LocalDateTime eventTime, LocalDateTime now) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(eventTime).equals(sdf.format(now));
    }
}
