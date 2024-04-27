package cc.gb.SpringNotificationBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Конфигурационный файл, который отвечает за настройку взаимодействия с Telegram API
 * параметры botName и botToken получаем с application.yaml
 */

@Configuration
@Data
@PropertySource("application.yaml")
@EnableScheduling
public class BotConfiguration implements SchedulingConfigurer {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    /**
     * Метод который использует асинхронный доступ к уведомлениям.
     * Используется 5 потоков, но данный параметр можно настраивать.
     *
     * @param taskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
