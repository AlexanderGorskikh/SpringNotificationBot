package cc.gb.SpringNotificationBot.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 *  Сервис, отвечающий за графческое представление календаря
 *   при выборе времени уведомления о мероприятии
 */

@Service
public class InlineCalendarService {

    public SendMessage createMessageWithDayButtons(Long chatId, LocalDate date) {
        SendMessage message = new SendMessage();
        message.setText(String.format("Выберите день (%s): ",date.getMonth().name()));
        message.setChatId(chatId);
        message.setReplyMarkup(createChoiceKeyboard(date));
        return message;
    }
    public InlineKeyboardMarkup createChoiceKeyboard(LocalDate date) {
        var keyboard = createInlineNumberButtons(date.lengthOfMonth());
        var rows = keyboard.getKeyboard();
        var nextMonth = new InlineKeyboardButton("Next month");
        nextMonth.setCallbackData("next");
        var prevMonth = new InlineKeyboardButton("Prev month");
        prevMonth.setCallbackData("prev");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(prevMonth);
        row.add(nextMonth);
        rows.add(row);
        return keyboard;
    }

    public InlineKeyboardMarkup createInlineNumberButtons(int numbers) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 1; i <= numbers; i++) {
            var button = new InlineKeyboardButton(String.valueOf(i));
            button.setCallbackData(String.valueOf(i));
            row.add(button);
            if (i % 7 == 0) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }
}
