import uk.ac.qub.eeecs.closer.models.dsl.Revision
import uk.ac.qub.eeecs.closer.parsers.CloserParser
import uk.ac.qub.eeecs.closer.parsers.GitParser
import uk.ac.qub.eeecs.closer.parsers.HgParser
import uk.ac.qub.eeecs.closer.parsers.SvnParser

/**
 * @author : Jordan
 * @created : 05/04/2021, Monday
 * @description : 
 * */
class RevisionTests extends CloserSpecification{

    private static GitParser GIT_PARSER = new GitParser()
    private static SvnParser SVN_PARSER = new SvnParser()
    private static HgParser HG_PARSER = new HgParser()
    private static CloserParser CLOSER_PARSER = new CloserParser()

    def "Test that a standard Git revision can be correctly parsed to CLOSER and back to the original format"(){
        when: "A complete revision exists in Git format"
            String revisionString =
                    """commit dbb91c3548d2308b8330d5c491c9ed71326899a2
Author:     AuthorName <AuthorName@email.com>
AuthorDate: Thu Nov 12 10:57:28 2020 +0000
Commit:     CommitterName <CommitterName@email.com>
CommitDate: Wed Nov 11 09:57:28 2020 +0000

    Commit
    
    of
    
    files

A\tTextFile1.txt
D\tTextFile2.txt
M\tTextFile3.txt
"""
        and: "The revision gets parsed to CLOSER format"
            List<String> inputLines = revisionString.split("\n")
            List<Revision> revisions = GIT_PARSER.parseInputToFormat(inputLines)
        then: "Verify All data is correct on the revision"
            revisions.collect{[it.id, it.message, it.mergeDetails]} ==
                    [["dbb91c3548d2308b8330d5c491c9ed71326899a2", "Commit\n\nof\n\nfiles", null]]
            revisions.collect{[it.author.identifier, it.author.emailAddress, it.authorDate]} ==
                    [["AuthorName",
                      "AuthorName@email.com",
                      GIT_PARSER.extractDateTimeZoned("Thu Nov 12 10:57:28 2020 +0000", "E MMM d HH:mm:ss yyyy Z")]]
            revisions.collect{[it.committer.identifier, it.committer.emailAddress, it.committerDate]} ==
                    [["CommitterName",
                      "CommitterName@email.com",
                      GIT_PARSER.extractDateTimeZoned("Wed Nov 11 09:57:28 2020 +0000", "E MMM d HH:mm:ss yyyy Z")]]
        and: "The CLOSER revisions are converted back to the original format"
            String exportedCloser = CLOSER_PARSER.parseRevisionsToOutputFormat(revisions)
            List<Revision> inputCloser = CLOSER_PARSER.parseInputToFormat([exportedCloser])
            String reconstructedOutput = GIT_PARSER.parseRevisionsToOutputFormat(inputCloser)
        then: "The input and output should be identical"
            revisionString == reconstructedOutput
    }

    def "Test that a merge commit in Git can be correctly parsed to CLOSER and back to the original format"(){
        when: "A complete revision exists in Git format"
            String revisionString =
                """commit dbb91c3548d2308b8330d5c491c9ed71326899a2
Merge: 650a075 634254b
Author:     AuthorName <AuthorName@email.com>
AuthorDate: Thu Nov 12 10:57:28 2020 +0000
Commit:     CommitterName <CommitterName@email.com>
CommitDate: Wed Nov 11 09:57:28 2020 +0000

    Commit of files

A\tTextFile1.txt
D\tTextFile2.txt
M\tTextFile3.txt
"""
        and: "The revision gets parsed to CLOSER format"
            List<String> inputLines = revisionString.split("\n")
            List<Revision> revisions = GIT_PARSER.parseInputToFormat(inputLines)
        then: "Verify All data is correct on the revision"
            revisions.collect{[it.id, it.message, it.mergeDetails]} ==
                [["dbb91c3548d2308b8330d5c491c9ed71326899a2", "Commit of files", "650a075 634254b"]]
            revisions.collect{[it.author.identifier, it.author.emailAddress, it.authorDate]} ==
                [["AuthorName",
                  "AuthorName@email.com",
                  GIT_PARSER.extractDateTimeZoned("Thu Nov 12 10:57:28 2020 +0000", "E MMM d HH:mm:ss yyyy Z")]]
            revisions.collect{[it.committer.identifier, it.committer.emailAddress, it.committerDate]} ==
                [["CommitterName",
                  "CommitterName@email.com",
                  GIT_PARSER.extractDateTimeZoned("Wed Nov 11 09:57:28 2020 +0000", "E MMM d HH:mm:ss yyyy Z")]]
        and: "The CLOSER revisions are converted back to the original format"
            String exportedCloser = CLOSER_PARSER.parseRevisionsToOutputFormat(revisions)
            List<Revision> inputCloser = CLOSER_PARSER.parseInputToFormat([exportedCloser])
            String reconstructedOutput = GIT_PARSER.parseRevisionsToOutputFormat(inputCloser)
        then: "The input and output should be identical"
            revisionString == reconstructedOutput
    }

    def "Test that a standard SVN revision can be correctly parsed to CLOSER and back to the original format"(){
        when: "A complete revision exists in SVN format"
            String revisionString = """------------------------------------------------------------------------
r56 | UserIdentifier | 2020-11-10 21:20:54 +0000 (Tue, 10 Nov 2020) | 1 line
Changed paths:
   M /TextFile1.txt
   A /TextFile2.txt
   D /TextFile2.txt

Commit of files
------------------------------------------------------------------------
"""
        and: "The revision gets parsed to CLOSER format"
            List<String> inputLines = revisionString.split("\n")
            List<Revision> revisions = SVN_PARSER.parseInputToFormat(inputLines)
        then: "Verify All data is correct on the revision"
            revisions.collect{[it.id, it.message, it.mergeDetails]} ==
                [["r56", "Commit of files", null]]
            revisions.collect{[it.author.identifier, it.author.emailAddress, it.authorDate]} ==
                [["UserIdentifier",
                  "UserIdentifier",
                  SVN_PARSER.extractDateTimeZoned("2020-11-10 21:20:54 +0000", "yyyy-MM-dd HH:mm:ss Z")]]
            revisions.collect{[it.committer.identifier, it.committer.emailAddress, it.committerDate]} ==
                [["UserIdentifier",
                  "UserIdentifier",
                  SVN_PARSER.extractDateTimeZoned("2020-11-10 21:20:54 +0000", "yyyy-MM-dd HH:mm:ss Z")]]
        and: "The CLOSER revisions are converted back to the original format"
            String exportedCloser = CLOSER_PARSER.parseRevisionsToOutputFormat(revisions)
            List<Revision> inputCloser = CLOSER_PARSER.parseInputToFormat([exportedCloser])
            String reconstructedOutput = SVN_PARSER.parseRevisionsToOutputFormat(inputCloser)
        then: "The input and output should be identical"
            revisionString == reconstructedOutput
    }

    def "Test that a standard Mercurial revision can be correctly parsed to CLOSER and back to the original format"(){
        when: "A complete revision exists in Mercurial format"
            String revisionString = """4:f96d42481268ef29163ed5d2ec63472b0605832e
UserIdentifier <UserIdentifier@email.com>
2021-01-05 20:01 +0000
TextFile1.txt TextFile2.txt TextFile3.txt
TextFile1.txt TextFile2.txt
TextFile3.txt
Commit of files

"""
        and: "The revision gets parsed to CLOSER format"
            List<String> inputLines = revisionString.split("\n") + [""]
            List<Revision> revisions = HG_PARSER.parseInputToFormat(inputLines)
        then: "Verify All data is correct on the revision"
            revisions.collect{[it.id, it.message, it.mergeDetails]} ==
                [["4:f96d42481268ef29163ed5d2ec63472b0605832e", "Commit of files", null]]
            revisions.collect{[it.author.identifier, it.author.emailAddress, it.authorDate]} ==
                [["UserIdentifier",
                  "UserIdentifier@email.com",
                  HG_PARSER.extractDateTimeZoned("2021-01-05 20:01 +0000", "yyyy-MM-dd HH:mm Z")]]
            revisions.collect{[it.committer.identifier, it.committer.emailAddress, it.committerDate]} ==
                [["UserIdentifier",
                  "UserIdentifier@email.com",
                  HG_PARSER.extractDateTimeZoned("2021-01-05 20:01 +0000", "yyyy-MM-dd HH:mm Z")]]
        and: "The CLOSER revisions are converted back to the original format"
            String exportedCloser = CLOSER_PARSER.parseRevisionsToOutputFormat(revisions)
            List<Revision> inputCloser = CLOSER_PARSER.parseInputToFormat([exportedCloser])
            String reconstructedOutput = HG_PARSER.parseRevisionsToOutputFormat(inputCloser)
        then: "The input and output should be identical"
            revisionString == reconstructedOutput
    }

    def "Test that a standard Mercurial revision, with a multiline message can be correctly parsed to CLOSER and back to the original format"(){
        when: "A complete revision exists in Mercurial format"
        String revisionString = """4:f96d42481268ef29163ed5d2ec63472b0605832e
UserIdentifier <UserIdentifier@email.com>
2021-01-05 20:01 +0000
TextFile1.txt TextFile2.txt TextFile3.txt
TextFile1.txt TextFile2.txt
TextFile3.txt
Commit 
of 
files

"""
        and: "The revision gets parsed to CLOSER format"
        List<String> inputLines = revisionString.split("\n") + [""]
        List<Revision> revisions = HG_PARSER.parseInputToFormat(inputLines)
        then: "Verify All data is correct on the revision"
        revisions.collect{[it.id, it.message, it.mergeDetails]} ==
                [["4:f96d42481268ef29163ed5d2ec63472b0605832e", "Commit \nof \nfiles", null]]
        revisions.collect{[it.author.identifier, it.author.emailAddress, it.authorDate]} ==
                [["UserIdentifier",
                  "UserIdentifier@email.com",
                  HG_PARSER.extractDateTimeZoned("2021-01-05 20:01 +0000", "yyyy-MM-dd HH:mm Z")]]
        revisions.collect{[it.committer.identifier, it.committer.emailAddress, it.committerDate]} ==
                [["UserIdentifier",
                  "UserIdentifier@email.com",
                  HG_PARSER.extractDateTimeZoned("2021-01-05 20:01 +0000", "yyyy-MM-dd HH:mm Z")]]
        and: "The CLOSER revisions are converted back to the original format"
        String exportedCloser = CLOSER_PARSER.parseRevisionsToOutputFormat(revisions)
        List<Revision> inputCloser = CLOSER_PARSER.parseInputToFormat([exportedCloser])
        String reconstructedOutput = HG_PARSER.parseRevisionsToOutputFormat(inputCloser)
        then: "The input and output should be identical"
        revisionString == reconstructedOutput
    }
}
