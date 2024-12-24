package library.bookreviews.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import library.bookreviews.models.Book;

@Repository
public class BookRepository {

    @Autowired
    @Qualifier("redis-object")
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BOOK_KEY_PREFIX = "book:";
    private static final String QUERY_KEY_PREFIX = "query:";

    public void saveBook(Book book) {
        redisTemplate.opsForValue().set(BOOK_KEY_PREFIX + book.getId(), book);
    }

    public Book findBookById(String bookId) {
        return (Book) redisTemplate.opsForValue().get(BOOK_KEY_PREFIX + bookId);
    }

    public void saveSearchResults(String query, List<Book> books) {
        redisTemplate.opsForValue().set(QUERY_KEY_PREFIX + query, books);
    }

    public List<Book> findSearchResults(String query) {
        return (List<Book>) redisTemplate.opsForValue().get(QUERY_KEY_PREFIX + query);
    }
}
