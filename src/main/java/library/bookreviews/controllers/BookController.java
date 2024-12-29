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

    @GetMapping("/")
    public String landingPage() {
        return "landing";
    }

    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // handles user registration
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult br, Model model) {
        if (br.hasErrors()) {
            return "register"; // if validation has errors
        }
        if (!userService.register(user)) {
            model.addAttribute("error", "This email is already registered.");
            return "register"; // if user already exists
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; 
    }

    // handles login
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("user", user); // Store the entire user in session
            System.out.println("Session User: " + user); // DEBUG LINE
            return "redirect:/dashboard"; // redirect to dashboard after login
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login"; // Reload login page with error message
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/"; // Redirect to landing page
    }

    // dashboard view
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Redirect to login if session is empty
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
            return "redirect:/login"; // Redirect to login if session is empty
        }
        List<Book> books = bookService.searchBooks(query);
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
            return "redirect:/login"; // Redirect to login if session is empty
        }
        Book book = bookService.getBookDetails(id);
        model.addAttribute("book", book);
        model.addAttribute("query", query); 
        return "book-details"; 
    }

    @GetMapping("/books/mark-as-read")
    public String showMarkAsReadPage(@RequestParam String bookId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Redirect to login if session is empty
        }

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

        model.addAttribute("book", bookToRead);
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
