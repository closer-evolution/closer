import uk.ac.qub.eeecs.closer.models.dsl.FileChange
import uk.ac.qub.eeecs.closer.models.dsl.FileChangeType
import uk.ac.qub.eeecs.closer.parsers.GitParser
import uk.ac.qub.eeecs.closer.parsers.HgParser
import uk.ac.qub.eeecs.closer.parsers.SvnParser
import spock.lang.Unroll

import static uk.ac.qub.eeecs.closer.models.dsl.FileChangeType.*

/**
 * @author : Jordan
 * @created : 04/04/2021, Sunday
 * @description : Test for converting VCS output file changes to CLOSER
 * */
class FileChangeTests extends CloserSpecification{

    private static GitParser GIT_PARSER = new GitParser()
    private static SvnParser SVN_PARSER = new SvnParser()
    private static HgParser HG_PARSER = new HgParser()

    @Unroll
    def "Test that all possible Git file changes can be converted to the correct CLOSER type #type"(
            String rawFileChange, FileChangeType type, String loc, String newLoc, Integer changePercentage){
        when: "A file change exists in a Git log and the data is parsed"
            FileChange f = GIT_PARSER.extractFileChange(rawFileChange)
        then: "Verify that the formatted data is correctly extracted"
            f.collect{[it.type, it.file, it.newLocation, it.changePercentage]} == [[type, loc, newLoc, changePercentage]]
        where: "File changes are"
            rawFileChange                   | type     | loc        | newLoc         | changePercentage
            "A\tfile.txt"                   | ADDED    | "file.txt" | null           | null
            "M\tfile.txt"                   | MODIFIED | "file.txt" | null           | null
            "D\tfile.txt"                   | DELETED  | "file.txt" | null           | null
            "T\tfile.txt"                   | CHANGED  | "file.txt" | null           | null
            "C\tfile.txt"                   | COPIED   | "file.txt" | null           | null
            "U\tfile.txt"                   | UNMERGED | "file.txt" | null           | null
            "X\tfile.txt"                   | UNKNOWN  | "file.txt" | null           | null
            "R050\tfile.txt\tnew/file.txt"  | RENAMED  | "file.txt" | "new/file.txt" | 50
    }

    @Unroll
    def "Test that all possible Svn file changes can be converted to the correct CLOSER type #type"(
            String rawFileChange, FileChangeType type, String loc, String newLoc, Integer changePercentage){
        when: "A file change exists in a SVN log and the data is parsed"
            FileChange f = SVN_PARSER.extractFileChange(rawFileChange)
        then: "Verify that the formatted data is correctly extracted"
            f.collect{[it.type, it.file, it.newLocation, it.changePercentage]} == [[type, loc, newLoc, changePercentage]]
        where: "File changes are"
            rawFileChange  | type      | loc        | newLoc         | changePercentage
            "  A file.txt" | ADDED     | "file.txt" | null           | null
            "M file.txt"   | MODIFIED  | "file.txt" | null           | null
            "D /file.txt"  | DELETED   | "file.txt" | null           | null
            "R /file.txt"  | REPLACED  | "file.txt" | null           | null
    }

    def "Test that all possible Mercurial file changes can be converted to the correct CLOSER type"(){
        when: "File changes exists in a HG log and the data is parsed"
            List<FileChange> changes = HG_PARSER.extractFileChanges(
                    "AddFile1.txt AddFile2.txt AddFile3.txt DelFile1.txt ModFile.txt",
                    "AddFile1.txt AddFile2.txt AddFile3.txt",
                    "DelFile1.txt")
            List<FileChange> addChanges = changes.findAll{ it.type == ADDED}
            List<FileChange> modChanges = changes.findAll{ it.type == MODIFIED}
            List<FileChange> delChanges = changes.findAll{ it.type == DELETED}
        then: "Verify that the formatted data is correctly extracted"
            addChanges.collect{[it.type, it.file, it.newLocation, it.changePercentage]}.sort() ==
                    [[ADDED, "AddFile1.txt", null, null],
                     [ADDED, "AddFile2.txt", null, null],
                     [ADDED, "AddFile3.txt", null, null]].sort()
            modChanges.collect{[it.type, it.file, it.newLocation, it.changePercentage]} ==
                    [[MODIFIED, "ModFile.txt", null, null]]
            delChanges.collect{[it.type, it.file, it.newLocation, it.changePercentage]} ==
                    [[DELETED, "DelFile1.txt", null, null]]
    }

    @Unroll
    def "Test that all possible CLOSER file changes can be converted to the correct Git type #type"(
            FileChangeType type, String loc, String newLoc, Integer changePercentage, String rawFileChange){
        when: "All possible CLOSER types exists"
            FileChange f = new FileChange(type, loc, changePercentage, newLoc)
        then: "parsing the file change to GIT is the correct format as if the change occurred in Git"
            GIT_PARSER.parseFileChangesToOutputFormat([f]) == [rawFileChange]
        where: "All change types are considered"
            type     | loc        | newLoc         | changePercentage | rawFileChange
            ADDED    | "file.txt" | null           | null             | "A\tfile.txt"
            MODIFIED | "file.txt" | null           | null             | "M\tfile.txt"
            DELETED  | "file.txt" | null           | null             | "D\tfile.txt"
            CHANGED  | "file.txt" | null           | null             | "T\tfile.txt"
            COPIED   | "file.txt" | null           | null             | "C\tfile.txt"
            UNMERGED | "file.txt" | null           | null             | "U\tfile.txt"
            UNKNOWN  | "file.txt" | null           | null             | "X\tfile.txt"
            RENAMED  | "file.txt" | "new/file.txt" | 50               | "R050\tfile.txt\tnew/file.txt"
            REPLACED | "file.txt" | null           | null             | "M\tfile.txt"
    }

    @Unroll
    def "Test that all possible CLOSER file changes can be converted to the correct SVN type #type"(
            FileChangeType type, String loc, String newLoc, Integer changePercentage, List<String> rawFileChanges){
        when: "All possible CLOSER types exists"
            FileChange f = new FileChange(type, loc, changePercentage, newLoc)
        then: "parsing the file change to SVN is the correct format as if the change occurred in Git"
            SVN_PARSER.parseFileChangesToOutputFormat([f]) == rawFileChanges.sort()
        where: "All change types are considered"
            type     | loc        | newLoc         | changePercentage | rawFileChanges
            ADDED    | "file.txt" | null           | null             | ["   A /file.txt"]
            MODIFIED | "file.txt" | null           | null             | ["   M /file.txt"]
            DELETED  | "file.txt" | null           | null             | ["   D /file.txt"]
            CHANGED  | "file.txt" | null           | null             | ["   M /file.txt"]
            COPIED   | "file.txt" | null           | null             | ["   M /file.txt"]
            UNMERGED | "file.txt" | null           | null             | ["   M /file.txt"]
            UNKNOWN  | "file.txt" | null           | null             | ["   M /file.txt"]
            RENAMED  | "file.txt" | "new/file.txt" | 50               | ["   D /file.txt", "   A /new/file.txt"].sort()
            REPLACED | "file.txt" | null           | null             | ["   R /file.txt"]
    }

    @Unroll
    def "Test that all possible CLOSER file changes can be converted to the correct Mercurial type #type"(
            FileChangeType type, String loc, String newLoc, Integer changePercentage, List<String> rawFileChanges){
        when: "All possible CLOSER types exists"
            FileChange f = new FileChange(type, loc, changePercentage, newLoc)
        then: "parsing the file change to Mercurial is the correct format as if the change occurred in Mercurial"
            HG_PARSER.parseFileChangesToOutputFormat([f]) == rawFileChanges
        where: "All change types are considered"
        type     | loc        | newLoc         | changePercentage | rawFileChanges
        ADDED    | "file.txt" | null           | null             | ["file.txt", "file.txt", ""]
        MODIFIED | "file.txt" | null           | null             | ["file.txt", "", ""]
        DELETED  | "file.txt" | null           | null             | ["file.txt", "", "file.txt"]
        CHANGED  | "file.txt" | null           | null             | ["file.txt", "", ""]
        COPIED   | "file.txt" | null           | null             | ["file.txt", "", ""]
        UNMERGED | "file.txt" | null           | null             | ["file.txt", "", ""]
        UNKNOWN  | "file.txt" | null           | null             | ["file.txt", "", ""]
        RENAMED  | "file.txt" | "new/file.txt" | 50               | ["file.txt new/file.txt", "new/file.txt", "file.txt"]
        REPLACED | "file.txt" | null           | null             | ["file.txt", "", ""]
    }
}
