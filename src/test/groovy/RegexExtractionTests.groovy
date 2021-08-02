import uk.ac.qub.eeecs.closer.parsers.GitParser

/**
 * @author : Jordan
 * @created : 14/04/2021, Wednesday
 * @description : 
 * */
class RegexExtractionTests extends CloserSpecification{

    private static GitParser GIT_PARSER = new GitParser()

    def "Test that commit id can be extracted from Git Log"(){
        when: "The commit line is reached in the revision and extracted using regex"
        String commitId = "commit dbb91c3548d2308b8330d5c491c9ed71326899a2"
        String id = GIT_PARSER.extractLineDetailsWithRegex(commitId, GIT_PARSER.COMMIT_REGEX)
        then: "The extracted value should be correct"
        id == "dbb91c3548d2308b8330d5c491c9ed71326899a2"
    }

    def "Test that author can be extracted from Git Log"(){
        when: "The commit line is reached in the revision and extracted using regex"
        String commitId = "Author:     UserIdentifier <UserIdentifier@email.com>"
        String author = GIT_PARSER.extractLineDetailsWithRegex(commitId, GIT_PARSER.AUTHOR_REGEX)
        then: "The extracted value should be correct"
        author == "UserIdentifier <UserIdentifier@email.com>"
    }

    def "Test that author date can be extracted from Git Log"(){
        when: "The commit line is reached in the revision and extracted using regex"
        String commitId = "AuthorDate: Wed Nov 11 09:57:28 2020 +0000"
        String authorDate = GIT_PARSER.extractLineDetailsWithRegex(commitId, GIT_PARSER.AUTHOR_DATE_REGEX)
        then: "The extracted value should be correct"
        authorDate  == "Wed Nov 11 09:57:28 2020 +0000"
    }

    def "Test that commit can be extracted from Git Log"(){
        when: "The commit line is reached in the revision and extracted using regex"
        String commitId = "Commit:     UserIdentifier <UserIdentifier@email.com>"
        String commit = GIT_PARSER.extractLineDetailsWithRegex(commitId, GIT_PARSER.COMMITTER_REGEX)
        then: "The extracted value should be correct"
        commit == "UserIdentifier <UserIdentifier@email.com>"
    }

    def "Test that commit date can be extracted from Git Log"(){
        when: "The commit line is reached in the revision and extracted using regex"
        String commitId = "CommitDate: Wed Nov 11 09:57:28 2020 +0000"
        String commitDate = GIT_PARSER.extractLineDetailsWithRegex(commitId, GIT_PARSER.COMMIT_DATE_REGEX)
        then: "The extracted value should be correct"
        commitDate == "Wed Nov 11 09:57:28 2020 +0000"
    }

    def "Test that merge details can be extracted from Git Log"(){
        when: "The commit line is reached in the revision and extracted using regex"
        String commitId = "Merge: 650a075 634254b"
        String merge = GIT_PARSER.extractLineDetailsWithRegex(commitId, GIT_PARSER.MERGE_REGEX)
        then: "The extracted value should be correct"
        merge == "650a075 634254b"
    }
}
