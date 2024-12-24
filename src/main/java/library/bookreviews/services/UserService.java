package library.bookreviews.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import library.bookreviews.models.Book;
import library.bookreviews.models.ReadBook;
import library.bookreviews.models.User;
import library.bookreviews.repository.BookRepository;
import library.bookreviews.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    public User login(User user) {
        return userRepository.findOrCreateUser(user);
    }

    public boolean addToReadList(String userId, String bookId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if the book is already in the "To Read" or "Read" list
        boolean alreadyInToRead = user.getToReadList().stream().anyMatch(book -> book.getId().equals(bookId));
        boolean alreadyInRead = user.getReadList().stream().anyMatch(book -> book.getId().equals(bookId));

        if (alreadyInToRead || alreadyInRead) {
            return false; // Book is already in one of the lists
        }

        // Fetch book details and add to "To Read" list
        Book book = bookService.getBookDetails(bookId);
        user.getToReadList().add(book);

        // Save updated user data
        userRepository.save(user);
        return true;
    }

    public boolean markAsRead(String userId, String bookId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if the book is in the "To Read" list
        Book bookToRead = user.getToReadList().stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (bookToRead == null) {
            return false; // Book not found in "To Read" list
        }

        // Remove from "To Read" list
        user.getToReadList().remove(bookToRead);

        // Add to "Read" list with additional fields
        ReadBook readBook = new ReadBook();
        readBook.setId(bookToRead.getId());
        readBook.setTitle(bookToRead.getTitle());
        readBook.setAuthor(bookToRead.getAuthor());
        readBook.setImageUrl(bookToRead.getImageUrl());
        readBook.setRating(0); // Placeholder for rating
        readBook.setReview(""); // Placeholder for review
        readBook.setDateRead(LocalDate.now());
        user.getReadList().add(readBook);

        // Save updated user data
        userRepository.save(user);
        return true;
    }

    public List<Book> getToReadList(String userId) {
        return userRepository.findById(userId).getToReadList();
    }

    public List<ReadBook> getReadList(String userId) {
        return userRepository.findById(userId).getReadList();
    }

    public User getUserDetails(String userId) {
        // Fetch user details from Redis repository
        User user = userRepository.findById(userId);

        if (user == null) {
            // Handle case where user does not exist in the database
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        return user;
    }
}