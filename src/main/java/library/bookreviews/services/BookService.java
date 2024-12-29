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
     * Searches for books using the Open Library API
     * Results are cached in Redis for subsequent searches
     * 
     * query = search query (title or author)
     * returns a list of books matching the query
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
     * Fetches or enriches metadata about a specific book using its ID
     * Data is cached in Redis for faster access in future requests
     *
     * bookId = unique ID of the book (e.g. "OL41495W")
     * returns the Book object with all known fields, now including description
     */
    public Book getBookDetails(String bookId) {

        // 1) Check the cache
        Book cachedBook = bookRepository.findBookById(bookId);
        if (cachedBook != null) {
            // If we already have a description, assume it's fully enriched
            if (cachedBook.getDescription() != null && !cachedBook.getDescription().equals("No description available.")) {
                return cachedBook;
            }
            // Otherwise, it's partial => fill in missing description
            openLibraryApiClient.fillDescription(cachedBook);
            bookRepository.saveBook(cachedBook);
            return cachedBook;
        }

        // 2) If not in cache, fetch partial data from the search API
        Book partialBook = fetchPartialBook(bookId);
        if (partialBook == null) {
            throw new RuntimeException("Could not fetch partial book details for ID: " + bookId);
        }

        // 3) Enrich the book with description and subjects
        openLibraryApiClient.fillDescription(partialBook);

        // 4) Save enriched Book to Redis
        bookRepository.saveBook(partialBook);

        // 5) Return the enriched Book
        return partialBook;
    }

    private Book fetchPartialBook(String bookId) {
        // Use the search endpoint to find a partial book
        List<Book> books = openLibraryApiClient.searchBooks(bookId);
    
        // Match by ID
        for (Book book : books) {
            if (bookId.equals(book.getId())) {
                return book;
            }
        }
    
        // If no match is found, return null
        return null;
    }

}
