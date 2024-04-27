package cc.gb.SpringNotificationBot.repository;

import cc.gb.SpringNotificationBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA репозиторий пользователей бота
 */
public interface UserRepository extends JpaRepository<User,Long> {
}
