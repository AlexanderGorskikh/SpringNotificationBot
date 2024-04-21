package cc.gb.SpringNotificationBot.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InlineCalendarService {

    public SendMessage createMessageWithDayButtons(Long chatId) {
        SendMessage message = new SendMessage();
        message.setText("Выберите день:");
        message.setChatId(chatId);

        LocalDate currentDate = LocalDate.now();
        int daysInMonth = currentDate.lengthOfMonth();

        message.setReplyMarkup(createInlineNumberButtons(daysInMonth));
        return message;
    }

    public SendMessage createMessageWithHourButtons(Long chatId) {
        SendMessage message = new SendMessage();
        message.setText("Выберите час:");
        message.setChatId(chatId);
        message.setReplyMarkup(createInlineNumberButtons(24));
        return message;
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
