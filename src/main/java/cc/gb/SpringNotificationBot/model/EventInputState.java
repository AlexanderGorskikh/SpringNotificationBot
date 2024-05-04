package cc.gb.SpringNotificationBot.model;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * Вспомогательный класс для ввода мероприятия.
 */
@Data
public class EventInputState {
    private String description;
    private LocalDateTime timeOfNotification;
    private int day;
    private int hour;
    private int minute;
    private boolean isUpdate;
    private boolean isDelete;
    private boolean choiceDay;
    private boolean choiceHour;

}
