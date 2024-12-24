package library.bookreviews.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private static final long serialVersionUID = 1L;
    private String id; 
    private String title;
    private String author;
    private String publisher;
    private int yearPublished;
    private String imageUrl;
    private String description;

    public Book(String author, String id, String publisher, String title, int yearPublished, String imageUrl) {
        this.author = author;
        this.id = id;
        this.publisher = publisher;
        this.title = title;
        this.yearPublished = yearPublished;
        this.imageUrl = imageUrl;
    }

    public Book(){}

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}
    public String getPublisher() {return publisher;}
    public void setPublisher(String publisher) {this.publisher = publisher;}
    public int getYearPublished() {return yearPublished;}
    public void setYearPublished(int yearPublished) {this.yearPublished = yearPublished;}
    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Book{");
        sb.append("id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", author=").append(author);
        sb.append(", publisher=").append(publisher);
        sb.append(", yearPublished=").append(yearPublished);
        sb.append(", imageUrl").append(imageUrl);
        sb.append(", description").append(description);
        sb.append('}');
        return sb.toString();
    }

  
   
}