package uk.ac.qub.eeecs.closer.models.dsl;

import uk.ac.qub.eeecs.closer.main.VCSType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileChange {
    private FileChangeType type;
    private String file;

    private Integer changePercentage;
    private String newLocation;

    public FileChange(FileChangeType type, String file, Integer changePercentage, String newLocation) {
        this.type = type;
        this.file = file;
        this.changePercentage = changePercentage;
        this.newLocation = newLocation;
    }

    public FileChange(FileChangeType type, String file) {
        this.type = type;
        this.file = file;
    }

    public Integer getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(Integer changePercentage) {
        this.changePercentage = changePercentage;
    }

    public String getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(String newLocation) {
        this.newLocation = newLocation;
    }

    public FileChangeType getType() {
        return type;
    }

    public void setType(FileChangeType type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String convertToSpecificOutputFormat(VCSType vcsType){
        switch (vcsType) {
            case GIT:
                if (changePercentage != null && newLocation != null){
                    return type.getGitIdentifier()+String.format("%03d",changePercentage)+"\t"+file+"\t"+newLocation;
                }else if (changePercentage != null){
                    return type.getGitIdentifier()+String.format("%03d",changePercentage)+"\t"+file;
                }else if (newLocation != null){
                    return type.getGitIdentifier()+"\t"+file+"\t"+newLocation;
                } else {
                    return type.getGitIdentifier()+"\t"+file;
                }
                //Leading / must be added for svn type
            case SVN: return "   "+type.getSvnIdentifier()+ " /"+file;
            case HG: return "hg";
            default:
                throw new UnsupportedOperationException();
        }
    }

    public List<FileChange> convertToSupportedFileChangeTypes(VCSType vcsType){
        //Git renamed event
        if (this.type.equals(FileChangeType.RENAMED) && (vcsType.equals(VCSType.HG) || vcsType.equals(VCSType.SVN))){
            return Arrays.asList(new FileChange(FileChangeType.ADDED, this.newLocation), new FileChange(FileChangeType.DELETED, this.file));
        }
        return Collections.singletonList(this);
    }

}
