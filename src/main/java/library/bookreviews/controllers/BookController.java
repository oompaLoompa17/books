package library.bookreviews.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import library.bookreviews.models.Book;
import library.bookreviews.models.ReadBook;
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
    public String landingPage() {
        return "landing"; // Points to a new landing page
    }

    // Registration Page
    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("user", new User()); // Provide a blank user object for the form
        return "register"; // Points to the registration page
    }

    // Handle User Registration
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult br, Model model) {
        if (br.hasErrors()) {
            return "register"; // Reload registration page with validation errors
        }
        if (!userService.register(user)) {
            model.addAttribute("error", "Username already exists or invalid details provided.");
            return "register"; // Reload registration page with an error message
        }
        model.addAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }

    // Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Points to the login page
    }

    // Handle Login
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("user", user); // Store the entire user in session
            session.setAttribute("userId", user.getId()); // Explicitly set userId
            System.out.println("Session User: " + user); // Debugging log
            System.out.println("Session UserId: " + session.getAttribute("userId"));
            return "redirect:/dashboard"; // Redirect to dashboard after login
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login"; // Reload login page with error message
        }
    }


    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/"; // Redirect to landing page
    }

    // Dashboard, fetch user from HttpSession
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session is empty
        }
        List<Book> toReadList = userService.getToReadList(user.getId());
        List<ReadBook> ReadList = userService.getReadList(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("toReadList", toReadList);
        model.addAttribute("readList", ReadList);
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
        model.addAttribute("query", query);
        return "search"; 
    }

    // Book details
    @GetMapping("/books/{id}")
    public String getBookDetails(@PathVariable String id, @RequestParam(required = false, defaultValue = "") String query, 
                                    HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/"; // Redirect to login if session is empty
        }
        Book book = bookService.getBookDetails(id);
        model.addAttribute("user", user); // Add user for use in the view
        System.out.println("Fetched Book: " + book); // Debugging log
        model.addAttribute("book", book);
        model.addAttribute("query", query); 
        return "book-details"; 
    }

    @GetMapping("/books/mark-as-read")
    public String showMarkAsReadPage(@RequestParam String bookId, HttpSession session, Model model) {
        // Fetch the user directly from the session
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Redirect to login if user is not in session
        }

        // Log the current To Read List
        System.out.println("Session User: " + user);
        System.out.println("Reloaded User's To Read List: " + user.getToReadList());

        // Check if To Read List is empty
        if (user.getToReadList() == null || user.getToReadList().isEmpty()) {
            System.out.println("To Read List is empty.");
            return "redirect:/dashboard"; // Redirect to dashboard
        }

        // Find the book in the To Read List
        Book bookToRead = user.getToReadList().stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (bookToRead == null) {
            System.out.println("Book ID entered: " + bookId);
            System.out.println("Book not found in To Read list, redirecting to dashboard.");
            return "redirect:/dashboard"; // Redirect if book is not found
        }

        // Add the book to the model
        model.addAttribute("book", bookToRead);
        System.out.println("Book found, rendering markasread view.");
        return "markasread";
    }


    @PostMapping("/mark-as-read")
    public String markAsRead(
            @RequestParam String bookId,
            @RequestParam int rating,
            @RequestParam String review,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRead,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        boolean marked = userService.markAsRead(user.getId(), bookId, rating, review, dateRead, session);
        if (marked) {
            redirectAttributes.addFlashAttribute("successMessage", "Book marked as read successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found in your 'To Read' list.");
        }

        return "redirect:/dashboard";
    }

}
