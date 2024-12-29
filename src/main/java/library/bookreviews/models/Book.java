package library.bookreviews.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book{
    private String id; 
    private String title;
    private String author;
    private int yearPublished;
    private String imageUrl;
    private String goodreadsUrl;
    private String description;

    public Book(String author, String id, String title, int yearPublished, String imageUrl, String goodreadsUrl,
                 String description) {
        this.author = author;
        this.id = id;
        this.title = title;
        this.yearPublished = yearPublished;
        this.imageUrl = imageUrl;
        this.goodreadsUrl = goodreadsUrl;
        this.description = description;
    }

    public Book(){}

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}
    public int getYearPublished() {return yearPublished;}
    public void setYearPublished(int yearPublished) {this.yearPublished = yearPublished;}
    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
    public String getGoodreadsUrl() {return goodreadsUrl;}
    public void setGoodreadsUrl(String goodreadsUrl) {this.goodreadsUrl = goodreadsUrl;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", author=" + author + ", yearPublished=" + yearPublished
                + ", imageUrl=" + imageUrl + ", goodreadsUrl=" + goodreadsUrl + ", description=" + description + "]";
    }
}