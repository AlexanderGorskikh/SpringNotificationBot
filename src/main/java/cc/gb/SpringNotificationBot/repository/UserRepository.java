package cc.gb.SpringNotificationBot.repository;

import cc.gb.SpringNotificationBot.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {
}
