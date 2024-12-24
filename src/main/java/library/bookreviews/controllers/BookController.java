package library.bookreviews.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import library.bookreviews.models.Book;
import library.bookreviews.models.User;
import library.bookreviews.services.BookService;
import library.bookreviews.services.UserService;

@Controller
@RequestMapping
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    // Landing Page
    @GetMapping("/")
    public String loginPage() {
        return "login"; 
    }

    // Login and store user in HttpSession
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        User user = userService.login(new User(username, password));
        session.setAttribute("user", user); // Store the logged-in user in the session
        return "redirect:/dashboard"; // Redirect to dashboard after login
    }

    // Dashboard, fetch user from HttpSession
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session is empty
        }
        model.addAttribute("user", user);
        return "dashboard"; 
    }

    // Search books, user from HttpSession
    @GetMapping("/search")
    public String searchBooks(@RequestParam String query, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session is empty
        }
        List<Book> books = bookService.searchBooks(query);
        model.addAttribute("user", user); // Add user for use in the view
        model.addAttribute("books", books);
        return "search"; 
    }

    // Book details
    @GetMapping("/books/{id}")
    public String getBookDetails(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session is empty
        }
        Book book = bookService.getBookDetails(id);
        model.addAttribute("user", user); // Add user for use in the view
        System.out.println("Fetched Book: " + book); // Debugging log
        model.addAttribute("book", book);
        return "book-details"; 
    }

    // Add to "To Read" list
    @PostMapping("/books/to-read/{id}")
    @ResponseBody
    public ResponseEntity<String> addToReadList(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to add books.");
        }
        boolean added = userService.addToReadList(user.getId(), id);
        if (added) {
            return ResponseEntity.ok("Book has been added to your 'To Read' list.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Book is already in your 'To Read' or 'Read' list.");
        }
    }

    @PostMapping("/books/mark-as-read/{id}")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to mark books as read.");
        }
        boolean marked = userService.markAsRead(user.getId(), id);
        if (marked) {
            return ResponseEntity.ok("Book has been marked as read.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Book is not in your 'To Read' list.");
        }
    }

}
