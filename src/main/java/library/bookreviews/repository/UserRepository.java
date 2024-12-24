package library.bookreviews.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import library.bookreviews.models.User;

@Repository
public class UserRepository {
    
    @Autowired
    @Qualifier("redis-object")
    private RedisTemplate<String, Object> redisTemplate;

    private final String USER_PREFIX = "user:";

    public User findById(String userId) {
        return (User) redisTemplate.opsForValue().get(USER_PREFIX + userId);
    }

    public User findOrCreateUser(User user) {
        User existingUser = findById(user.getId());
        if (existingUser == null) {
            save(user);
            return user;
        }
        return existingUser;
    }

    public void save(User user) {
        redisTemplate.opsForValue().set(USER_PREFIX + user.getId(), user);
    }
}
