package library.bookreviews.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class User {
    
    @NotNull @NotEmpty @Email(message="Please enter a valid email.")
    private String id; 
    @NotNull @NotEmpty @Size(min=8, max=20, message="Password must be between 8 and 20 characters.")
    private String password; 
    private List<Book> toReadList = new ArrayList<>(); // Books to read
    private List<ReadBook> readList = new ArrayList<>(); // Books already read

    public User (String id, String password){
        this.id = id;
        this.password = password;
    }

    public User(){}

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public List<Book> getToReadList() {return toReadList;}
    public void setToReadList(List<Book> toReadList) {this.toReadList = toReadList;}
    public List<ReadBook> getReadList() {return readList;}
    public void setReadList(List<ReadBook> readList) {this.readList = readList;}

    @Override
    public String toString() {
        return "User [id=" + id + ", password=" + password + ", toReadList=" + toReadList + ", readList=" + readList
                + "]";
    }
}
