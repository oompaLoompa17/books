package library.bookreviews.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import library.bookreviews.models.Book;
import library.bookreviews.models.ReadBook;
import library.bookreviews.models.User;
import library.bookreviews.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookService bookService;

    // handles registration
    public boolean register(User user) {
        // Check if user already exists
        User existingUser = userRepository.findById(user.getId());
        if (existingUser != null && existingUser.getPassword() != null) {
            return false; // User already exists
        }

        userRepository.save(user); // saves new user
        return true;
    }

    // handles login
    public User login(String username, String password) {
        User user = userRepository.findById(username);

        if (user == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Username does not exist."); // user not found
        }

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Incorrect password."); // password mismatch
        }

        return user; // returns an existing user
    }

    // adds book to "to read" list
    public boolean addToReadList(String userId, String bookId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
    
        // Check if the book is already in the "To Read" or "Read" list
        boolean alreadyInToRead = user.getToReadList().stream().anyMatch(book -> book.getId().equals(bookId));
        boolean alreadyInRead = user.getReadList().stream().anyMatch(book -> book.getId().equals(bookId));
    
        if (alreadyInToRead || alreadyInRead) {
            System.out.println("Book already exists in one of the lists: " + bookId + "\n");
            return false;
        }
    
        // Fetch book details via bookID from cached search result
        Book book = bookService.getBookDetails(bookId);
    
        user.getToReadList().add(book); // Add to "to read" list
       
        userRepository.save(user); // Save updated user back to Redis
   
        return true;
    }

    public boolean removeFromToReadList(String userId, String bookId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
    
        // Find the book in the "To Read" list
        Book bookToRemove = user.getToReadList().stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);
    
        if (bookToRemove == null) {
            return false; // Book not found
        }
    
        user.getToReadList().remove(bookToRemove); // Remove the book
    
        userRepository.save(user); // Save updated user back to Redis
        return true;
    }
    

    public boolean markAsRead(String userId, String bookId, int rating, String review, LocalDate dateRead, HttpSession session) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
    
        // Find the book in the "To Read" list
        Book bookToRead = user.getToReadList().stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);
    
        if (bookToRead == null) {
            return false; // Book not found
        }
    
        // Remove from "To Read" list
        user.getToReadList().remove(bookToRead);
    
        // Add to "Read" list with additional fields
        ReadBook readBook = new ReadBook();
        readBook.setId(bookToRead.getId());
        readBook.setTitle(bookToRead.getTitle());
        readBook.setAuthor(bookToRead.getAuthor());
        readBook.setImageUrl(bookToRead.getImageUrl());
        readBook.setRating(rating); // Set rating
        readBook.setReview(review); // Set review
        readBook.setDateRead(dateRead); // Set date read
        user.getReadList().add(readBook);
    

        userRepository.save(user); // Save updated user back to Redis
        updateSession(session, userId);
        return true;
    }
    

    public List<Book> getToReadList(String userId) {
        return userRepository.findById(userId).getToReadList();
    }

    public List<ReadBook> getReadList(String userId) {
        return userRepository.findById(userId).getReadList();
    }

    public void updateSession(HttpSession session, String userId) {
        User updatedUser = userRepository.findById(userId);
        session.setAttribute("user", updatedUser);
    }
    
}