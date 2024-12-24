package library.bookreviews.models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable{
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes
    private String id; // Unique user identifier (e.g., email or username)
    private String password; // Hashed password
    private List<Book> toReadList; // Books to read
    private List<ReadBook> readList; // Books already read

    public User (String id, String password){
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Book> getToReadList() {
        return toReadList;
    }

    public void setToReadList(List<Book> toReadList) {
        this.toReadList = toReadList;
    }

    public List<ReadBook> getReadList() {
        return readList;
    }

    public void setReadList(List<ReadBook> readList) {
        this.readList = readList;
    }
}
