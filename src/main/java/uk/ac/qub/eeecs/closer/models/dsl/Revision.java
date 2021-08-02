package uk.ac.qub.eeecs.closer.models.dsl;

import java.time.ZonedDateTime;
import java.util.List;

public class Revision {

    private String id;
    private Author author;
    private ZonedDateTime authorDate;
    private Author committer;
    private ZonedDateTime committerDate;
    private String message;
    private List<FileChange> fileChanges;
    private String mergeDetails;

    public Revision(String id, Author author, Author committer,
                    ZonedDateTime authorDate, ZonedDateTime committerDate,
                    String message, List<FileChange> fileChanges) {
        this.id = id;
        this.author = author;
        this.authorDate = authorDate;
        this.committer = committer;
        this.committerDate = committerDate;
        this.message = message;
        this.fileChanges = fileChanges;
    }

    public Revision(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Author getCommitter() {
        return committer;
    }

    public void setCommitter(Author committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FileChange> getFileChanges() {
        return fileChanges;
    }

    public void setFileChanges(List<FileChange> fileChanges) {
        this.fileChanges = fileChanges;
    }

    public ZonedDateTime getAuthorDate() {
        return authorDate;
    }

    public void setAuthorDate(ZonedDateTime authorDate) {
        this.authorDate = authorDate;
    }

    public ZonedDateTime getCommitterDate() {
        return committerDate;
    }

    public void setCommitterDate(ZonedDateTime committerDate) {
        this.committerDate = committerDate;
    }

    public String getMergeDetails() {
        return mergeDetails;
    }

    public void setMergeDetails(String mergeDetails) {
        this.mergeDetails = mergeDetails;
    }
}
