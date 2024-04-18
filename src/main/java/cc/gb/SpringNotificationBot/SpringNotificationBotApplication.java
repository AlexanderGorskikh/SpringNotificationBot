package cc.gb.SpringNotificationBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringNotificationBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringNotificationBotApplication.class, args);
	}
}
