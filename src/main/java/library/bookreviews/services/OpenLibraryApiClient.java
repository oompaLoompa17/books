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

    public Book getBookDetails(String workId) {
        String url = BASE_URL + "/works/" + workId + ".json";

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        JsonNode bookNode = response.getBody();
        System.out.println("API Response: " + bookNode); // Debugging log

        return mapToDetailedBook(bookNode);
    }

    private Book mapToBook(JsonNode node) {
        Book book = new Book();
        book.setId(node.has("key") ? node.get("key").asText().replace("/works/", "") : null);
        book.setTitle(node.has("title") ? node.get("title").asText() : null);
        book.setAuthor(node.has("author_name") ? node.get("author_name").get(0).asText() : "Unknown Author");
        book.setPublisher(node.has("publisher") ? node.get("publisher").get(0).asText() : "Unknown Publisher");
        book.setYearPublished(node.has("first_publish_year") ? node.get("first_publish_year").asInt() : 0);
        book.setImageUrl(node.has("cover_i") ? "https://covers.openlibrary.org/b/id/" + node.get("cover_i").asText() + "-L.jpg" : null); // Example URL construction
        return book;
    }

    private Book mapToDetailedBook(JsonNode node) {
        Book book = new Book();
    
        book.setId(node.has("key") ? node.get("key").asText().replace("/works/", "") : null);
        book.setTitle(node.has("title") ? node.get("title").asText() : "Unknown Title");
        book.setAuthor(node.has("authors") && node.get("authors").isArray() && node.get("authors").size() > 0
                ? node.get("authors").get(0).get("name").asText() : "Unknown Author");
        book.setPublisher(node.has("publishers") && node.get("publishers").isArray() && node.get("publishers").size() > 0
                ? node.get("publishers").get(0).asText() : "Unknown Publisher");
        book.setYearPublished(node.has("first_publish_year") ? node.get("first_publish_year").asInt() : 0);
        book.setImageUrl(node.has("covers") && node.get("covers").isArray() && node.get("covers").size() > 0
                ? "https://covers.openlibrary.org/b/id/" + node.get("covers").get(0).asText() + "-L.jpg"
                : "/img/default-cover.png");
        book.setDescription(node.has("description")
                ? (node.get("description").isTextual()
                    ? node.get("description").asText()
                    : node.get("description").has("value") ? node.get("description").get("value").asText() : "No description available.")
                : "No description available.");
    
        return book;
    }
}

