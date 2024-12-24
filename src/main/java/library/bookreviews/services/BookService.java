package library.bookreviews.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import library.bookreviews.models.Book;
import library.bookreviews.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OpenLibraryApiClient openLibraryApiClient;

    /**
     * Searches for books using the Open Library API. 
     * Results are cached in Redis for subsequent searches.
     * 
     * @param query The search query (title or author).
     * @return A list of books matching the query.
     */
    public List<Book> searchBooks(String query) {
        // Check the cache for previous search results
        List<Book> cachedBooks = bookRepository.findSearchResults(query);
        if (cachedBooks != null && !cachedBooks.isEmpty()) {
            return cachedBooks;
        }

        // If no cache exists, fetch results from the Open Library API
        List<Book> books = openLibraryApiClient.searchBooks(query);

        // Cache the results in Redis
        bookRepository.saveSearchResults(query, books);

        // Return the results
        return books.stream().limit(30).toList();
    }

    /**
     * Fetches detailed metadata about a specific book using its ID.
     * Data is cached in Redis for faster access in future requests.
     * 
     * @param bookId The unique ID of the book.
     * @return The detailed metadata of the book.
     */
    public Book getBookDetails(String bookId) {
        // Check the cache for the book details
        Book cachedBook = bookRepository.findBookById(bookId);
        if (cachedBook != null) {
            return cachedBook;
        }

        // If no cache exists, fetch details from the Open Library API
        Book book = openLibraryApiClient.getBookDetails(bookId);

        // Cache the book details in Redis
        bookRepository.saveBook(book);

        // Return the book details
        return book;
    }

}
