//Written by Jordan Garrity

package uk.ac.qub.eeecs.closer.models.dsl;

import uk.ac.qub.eeecs.closer.main.VCSType;

public class Author {

    /**
     * Each VCS solution will have some form of identifier that will map tp this string
     */
    private String identifier;

    /**
     * Git also stores email addresses so should be retained here if the VCS used is git
     */
    private String emailAddress;

    public Author(String identifier, String emailAddress) {
        this.identifier = identifier;
        this.emailAddress = emailAddress;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String convertToSpecificOutputType(VCSType type){
        switch (type) {
            case GIT: return identifier+" <"+emailAddress+">";
            case SVN: return "svn";
            case HG: return emailAddress != null ? identifier+" <"+emailAddress+">" : identifier;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
