package cc.gb.SpringNotificationBot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventInputState {
    private String description;
    private LocalDateTime timeOfNotification;
    private int day;
    private int hour;
    private boolean isUpdate;
    private boolean isDelete;
    private boolean choiceDay;
    private boolean choiceHour;
}
