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

    // returns a list of books based on query
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

    // pulls out the relevant fields and populates individual book objects
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

    // adds the description to a book that could not be accessed through the initial 'general' search
    public Book fillDescription(Book book) {
        // 'book.getId()' is something like "OL41495W" (no "/works/")
        String workId = book.getId();
        String url = BASE_URL + "/works/" + workId + ".json"; 
        // e.g. "https://openlibrary.org/works/OL41495W.json"
    
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode node = response.getBody();

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

