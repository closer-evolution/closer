package uk.ac.qub.eeecs.closer.models.dsl;

public enum FileChangeType {
    ADDED(      "A", "A", "A"),
    COPIED(     "C", "C", "M"),
    DELETED(    "D", "D", "D"),
    MODIFIED(   "M", "M", "M"),
    RENAMED(    "R", "R", ""),
    REPLACED(    "Y", "M", "R"),
    CHANGED(    "T", "T", "M"),
    UNMERGED(   "U", "U", "M"),
    UNKNOWN(    "X", "X", "M");

    private final String identifier;
    private final String gitIdentifier;
    private final String svnIdentifier;

    FileChangeType(String identifier, String gitIdentifier, String svnIdentifier) {
        this.identifier = identifier;
        this.gitIdentifier = gitIdentifier;
        this.svnIdentifier = svnIdentifier;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public final String getGitIdentifier() {
        return gitIdentifier;
    }

    public final String getSvnIdentifier() { return svnIdentifier; }

    public static FileChangeType getFileChangedTypeFromGitIdentifier(char identifier){
        switch (identifier){
            case 'A': return ADDED;
            case 'C': return COPIED;
            case 'D': return DELETED;
            case 'M': return MODIFIED;
            case 'R': return RENAMED;
            case 'T': return CHANGED;
            case 'U': return UNMERGED;
            default:
                return UNKNOWN;
        }
    }

    public static FileChangeType getFileChangedTypeFromSvnIdentifier(char identifier){
        switch (identifier){
            case 'A': return ADDED;
            case 'D': return DELETED;
            case 'M': return MODIFIED;
            case 'R': return REPLACED;
            default:
                return UNKNOWN;
        }
    }
}
