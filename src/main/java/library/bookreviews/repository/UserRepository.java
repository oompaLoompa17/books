package library.bookreviews.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import library.bookreviews.models.Book;
import library.bookreviews.models.ReadBook;
import library.bookreviews.models.User;

@Repository
public class UserRepository {

    @Autowired
    @Qualifier("redis-object")
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_PREFIX = "user:";

    // Fetch a user by ID
    public User findById(String userId) {
        String userKey = USER_PREFIX + userId;

        User user = new User();
        user.setId(userId);
        user.setPassword((String) redisTemplate.opsForHash().get(userKey, "password"));
        
        try {
            List<Book> toReadList = (List<Book>) redisTemplate.opsForHash().get(userKey, "toReadList");
            user.setToReadList(toReadList != null ? toReadList : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error deserializing toReadList for user " + userId + ": " + e.getMessage());
            user.setToReadList(new ArrayList<>());
        }
    
        try {
            List<ReadBook> readList = (List<ReadBook>) redisTemplate.opsForHash().get(userKey, "readList");
            user.setReadList(readList != null ? readList : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error deserializing readList for user " + userId + ": " + e.getMessage());
            user.setReadList(new ArrayList<>());
        }

        return user;
    }

    // Save or update a user
    public void save(User user) {
        String userKey = USER_PREFIX + user.getId();
        redisTemplate.opsForHash().put(userKey, "id", user.getId());
        redisTemplate.opsForHash().put(userKey, "password", user.getPassword());
        redisTemplate.opsForHash().put(userKey, "toReadList", user.getToReadList());
        redisTemplate.opsForHash().put(userKey, "readList", user.getReadList());

        // Log the current Redis state for the user
        System.out.println("Redis state for user " + userKey + ": " + redisTemplate.opsForHash().entries(userKey) + "\n\n");
    }

    // Find or create a user
    public User findOrCreateUser(User user) {
        User existingUser = findById(user.getId());
        if (existingUser.getPassword() == null) { // User not found
            save(user);
            return user;
        }
        return existingUser;
    }

    // Add a book to the user's "to read" list
    public void addToToReadList(String userId, Book book) {
        String userKey = USER_PREFIX + userId;
    
        // Fetch the current toReadList
        List<Book> toReadList = (List<Book>) redisTemplate.opsForHash().get(userKey, "toReadList");
        System.out.println("Current toReadList for user " + userId + ": " + toReadList);
    
        // Initialize if null
        if (toReadList == null) {
            toReadList = new ArrayList<>();
        }
    
        // Add the book if it doesn't already exist
        boolean alreadyExists = toReadList.stream().anyMatch(b -> b.getId().equals(book.getId()));
        if (!alreadyExists) {
            toReadList.add(book);
            redisTemplate.opsForHash().put(userKey, "toReadList", toReadList);
            System.out.println("Updated toReadList for user " + userId + ": " + toReadList);
        } else {
            System.out.println("Book already exists in toReadList: " + book.getId());
        }
    }
    
    public void saveToReadList(String userId, List<Book> toReadList) {
        String userKey = USER_PREFIX + userId;
        redisTemplate.opsForHash().put(userKey, "toReadList", toReadList);
    }

    // Add a book to the user's "read" list
    public void addToReadList(String userId, ReadBook readBook) {
        String userKey = USER_PREFIX + userId;
        List<ReadBook> readList = (List<ReadBook>) redisTemplate.opsForHash().get(userKey, "readList");
        if (readList == null) {
            readList = new ArrayList<>();
        }
        readList.add(readBook);
        redisTemplate.opsForHash().put(userKey, "readList", readList);
    }

}

