package library.bookreviews.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import library.bookreviews.models.Book;

@Service
public class OpenLibraryApiClient {
    private static final String BASE_URL = "https://openlibrary.org";
    
    private RestTemplate restTemplate = new RestTemplate();

    public List<Book> searchBooks(String query) {
        String url = BASE_URL + "/search.json?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)+ "&limit=30";

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        JsonNode docs = response.getBody().get("docs");

        List<Book> books = new ArrayList<>();
        if (docs != null) {
            for (JsonNode doc : docs) {
                books.add(mapToBook(doc));
            }
        }
        return books;
    }

    private Book mapToBook(JsonNode node) {
        Book book = new Book();
        book.setId(node.has("key") ? node.get("key").asText().replace("/works/", "") : null);
        book.setTitle(node.has("title") ? node.get("title").asText() : null);
        book.setAuthor(node.has("author_name") ? node.get("author_name").get(0).asText() : "Unknown Author");
        book.setYearPublished(node.has("first_publish_year") ? node.get("first_publish_year").asInt() : 0);
        book.setImageUrl(node.has("cover_i") ? "https://covers.openlibrary.org/b/id/" + node.get("cover_i").asText() 
                        + "-L.jpg" : null); 
        if (node.has("id_goodreads") 
                && node.get("id_goodreads").isArray() 
                && node.get("id_goodreads").size() > 0) {

            String firstGoodreadsId = node.get("id_goodreads").get(0).asText();
            String goodreadsUrl = "https://www.goodreads.com/book/show/" + firstGoodreadsId;
            book.setGoodreadsUrl(goodreadsUrl);
        } 
        return book;
    }

    public Book getBookDetails(Book baseBook) {
        // baseBook already has id, title, author, year, imageUrl from the search doc
    
        // We'll retrieve the work details (description, subjects) from the Work endpoint
        String url = BASE_URL + "/works/" + baseBook.getId() + ".json";
        // e.g. if baseBook.getId() = "OL41495W", then => "/works/OL41495W.json"
    
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode workNode = response.getBody();
    
            // Enrich 'baseBook' with description + subjects
            mapToDetailedBook(baseBook, workNode);
        } catch (Exception ex) {
            // fallback or log errors
            System.out.println("Failed to retrieve /works data: " + ex.getMessage());
        }
    
        return baseBook;
    }

    /**
     * Fills the given book with extra details from the /works/{workId}.json endpoint:
     *   - description
     *   - subjects
     * Leaves the existing fields (title, author, cover, etc.) alone.
     */
    private Book mapToDetailedBook(Book book, JsonNode node) {
        // 1) Description
        String description = "No description available.";
        JsonNode descNode = node.get("description");
        if (descNode != null) {
            // If it's a raw string
            if (descNode.isTextual()) {
                description = descNode.asText();
            }
            // Or if it's an object: "description": {"value":"Some desc"}
            else if (descNode.has("value") && descNode.get("value").isTextual()) {
                description = descNode.get("value").asText();
            }
        }
        book.setDescription(description);

        return book;
    }   

    public Book fillDescription(Book book) {
        // 'book.getId()' is something like "OL41495W" (no "/works/")
        String workId = book.getId();
        String url = BASE_URL + "/works/" + workId + ".json"; 
        // e.g. "https://openlibrary.org/works/OL41495W.json"
    
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode node = response.getBody();
    
            // (1) description
            String description = "No description available.";
            JsonNode descNode = node.get("description");
            if (descNode != null) {
                if (descNode.isTextual()) {
                    description = descNode.asText();
                } else if (descNode.has("value") && descNode.get("value").isTextual()) {
                    description = descNode.get("value").asText();
                }
            }
            book.setDescription(description);

        } catch (Exception ex) {
            System.out.println("Failed to fetch extra info from /works/{id}: " + ex.getMessage());
            // Book remains partially filled
        }
    
        return book;
    }
    
    
}

