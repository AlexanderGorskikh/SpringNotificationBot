package cc.gb.SpringNotificationBot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventInputState {
    private String description;
    private LocalDateTime timeOfNotification;
    private boolean isUpdate;
    private boolean isDelete;
}
