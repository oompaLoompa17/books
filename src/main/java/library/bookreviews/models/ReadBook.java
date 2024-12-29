package library.bookreviews.models;

import java.time.LocalDate;

public class ReadBook extends Book {
    private int rating; // User's rating of the book
    private String review; // User's review
    private LocalDate dateRead; // Date the book was finished

    public int getRating() {return rating;}
    public void setRating(int rating) {this.rating = rating;}
    public String getReview() {return review;}
    public void setReview(String review) {this.review = review;}
    public LocalDate getDateRead() {return dateRead;}
    public void setDateRead(LocalDate dateRead) {this.dateRead = dateRead;}
}
