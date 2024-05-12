package cc.gb.SpringNotificationBot.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.Month;


/**
 * Вспомогательный класс для ввода мероприятия.
 */
@Data
@Builder
public class EventInputState {
    private String description;
    private LocalDateTime timeOfNotification;
    private int day;
    private int hour;
    private int minute;
    private int choiceMonth;
    private Month month;
    private boolean isUpdate;
    private boolean isDelete;
    private boolean choiceDay;
    private boolean choiceHour;

}
