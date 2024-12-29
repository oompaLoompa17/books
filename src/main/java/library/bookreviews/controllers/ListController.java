package library.bookreviews.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import library.bookreviews.models.User;
import library.bookreviews.services.UserService;

@RestController
public class ListController {
    
    @Autowired
    private UserService userService;

    // Add to "To Read" list
    @PostMapping("/books/to-read/{id}")
    public ResponseEntity<String> addToReadList(@PathVariable String id, HttpSession session) {
        System.out.println("Book ID received by addToReadList: " + id);
        User user = (User) session.getAttribute("user");
        System.out.println("Session User: " + user);
        System.out.println("Book ID: " + id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to add books.");
        }

        boolean added = userService.addToReadList(user.getId(), id);
        if (added) {
            userService.updateSession(session, user.getId());
            return ResponseEntity.ok("Book has been added to your 'To Read' list.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Book is already in your 'To Read' or 'Read' list.");
        }
    }

    @PostMapping("/books/remove-to-read/{id}")
    public ResponseEntity<String> removeFromToReadList(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to remove books.");
        }

        boolean removed = userService.removeFromToReadList(user.getId(), id);
        if (removed) {
            userService.updateSession(session, user.getId()); // Update session
            return ResponseEntity.ok("Book has been removed from your 'To Read' list.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found in your 'To Read' list.");
        }
    }
}
