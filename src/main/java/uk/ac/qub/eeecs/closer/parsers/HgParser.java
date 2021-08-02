package uk.ac.qub.eeecs.closer.parsers;

import uk.ac.qub.eeecs.closer.main.VCSType;
import uk.ac.qub.eeecs.closer.models.dsl.Author;
import uk.ac.qub.eeecs.closer.models.dsl.FileChange;
import uk.ac.qub.eeecs.closer.models.dsl.FileChangeType;
import uk.ac.qub.eeecs.closer.models.dsl.Revision;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : Jordan
 * @created : 06/01/2021, Wednesday
 * @description : Parser for mercurial scm source control output generated using
 * hg log --template '{rev}:{node}\n{author}\n{date|isodate}\n{files}\n{file_adds}\n{file_dels}\n{desc}\n\n'
 **/
public class HgParser implements Parser {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm Z";
    private static final int REVISION_ID_INDEX = 0;
    private static final int REVISION_AUTHOR_INDEX = 1;
    private static final int REVISION_DATE_INDEX = 2;
    private static final int REVISION_FILES_INDEX = 3;
    private static final int REVISION_ADD_FILES_INDEX = 4;
    private static final int REVISION_DEL_FILES_INDEX = 5;
    private static final int REVISION_DESC_START_INDEX = 6;

    @Override
    public List<Revision> parseInputToFormat(List<String> linesOfInput) {
        List<Integer> commitStarts = extractRevisionStartingPoints(linesOfInput, "[0-9]+:[a-f0-9]{40}");
        return parseAllRevisionsToFormat(linesOfInput, commitStarts);
    }

    @Override
    public Revision extractDetailsAtRevision(List<String> linesOfRevision, int revisionStart) {
        //Extract id, userDetails and the commit date
        String id = linesOfRevision.get(REVISION_ID_INDEX);
        String userIdentifier = linesOfRevision.get(REVISION_AUTHOR_INDEX);
        Author author = extractAuthorDetails(userIdentifier);
        String dateString = linesOfRevision.get(REVISION_DATE_INDEX);
        ZonedDateTime authorDate = extractDateTimeZoned(dateString, DATE_FORMAT);

        List<String> commitMessageLines = linesOfRevision.subList(REVISION_DESC_START_INDEX, linesOfRevision.size() - 1);
        String commitMessage = String.join("\n", commitMessageLines);

        //Now extract all changes, new files and old files so that file change types can be inferred
        List<FileChange> extractFileChanges = extractFileChanges(linesOfRevision.get(REVISION_FILES_INDEX), linesOfRevision.get(REVISION_ADD_FILES_INDEX), linesOfRevision.get(REVISION_DEL_FILES_INDEX));
        return new Revision(id, author, author, authorDate, authorDate, commitMessage, extractFileChanges);
    }

    public List<FileChange> extractFileChanges(String allChangesLine, String addChangesLine, String delChangesLine) {
        /**
         * Due to mercurial not explicitly storing the type of file change
         * we are limited to only being able to infer the changes from the log data
         *
         * The FileChangeTypes that are acceptable for this output type are
         *  - ADDED
         *  - DELETED
         *  - MODIFIED
         */
        Set<String> allChanges = new HashSet<>(Arrays.asList(allChangesLine.split(" ")));
        Set<String> addChanges = new HashSet<>(Arrays.asList(addChangesLine.split(" ")));
        Set<String> delChanges = new HashSet<>(Arrays.asList(delChangesLine.split(" ")));

        //Infer that the modified files are any that have not been added or deleted
        Set<String> modChanges = allChanges.stream().filter(s -> !addChanges.contains(s) && !delChanges.contains(s)).collect(Collectors.toSet());

        List<FileChange> fileChanges = new ArrayList<>();
        fileChanges.addAll(addChanges.stream().filter(c -> !c.isEmpty()).map(c -> new FileChange(FileChangeType.ADDED, c)).collect(Collectors.toSet()));
        fileChanges.addAll(delChanges.stream().filter(c -> !c.isEmpty()).map(c -> new FileChange(FileChangeType.DELETED, c)).collect(Collectors.toSet()));
        fileChanges.addAll(modChanges.stream().filter(c -> !c.isEmpty()).map(c -> new FileChange(FileChangeType.MODIFIED, c)).collect(Collectors.toSet()));

        return fileChanges;
    }

    @Override
    public FileChange extractFileChange(String fileChange) {
        return null;
    }

    @Override
    public Author extractAuthorDetails(String authorString) {
        int splittingLocation = authorString.lastIndexOf('<');
        if (splittingLocation == -1) {
            return new Author(authorString, null);
        }
        String authorName = authorString.substring(0, splittingLocation - 1);
        String authorEmail = authorString.substring(splittingLocation + 1, authorString.length() - 1);
        return new Author(authorName, authorEmail);
    }

    @Override
    public String parseRevisionsToOutputFormat(List<Revision> revisions) {
        StringBuilder revisionsString = new StringBuilder();
        for (int i = 0; i < revisions.size(); i++) {
            revisionsString.append(parseRevisionToOutputFormat(revisions.get(i)));
        }
        return revisionsString.toString();
    }

    @Override
    public List<String> parseFileChangesToOutputFormat(List<FileChange> fileChanges) {

        List<FileChange> convertedFileChanges = fileChanges.stream()
                .map(f -> f.convertToSupportedFileChangeTypes(VCSType.HG))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        ArrayList<String> allFiles = new ArrayList<>();
        ArrayList<String> addFiles = new ArrayList<>();
        ArrayList<String> delFiles = new ArrayList<>();

        for (FileChange fileChange : convertedFileChanges) {
            if (fileChange.getType() == FileChangeType.ADDED) {
                addFiles.add(fileChange.getFile());
            }
            if (fileChange.getType() == FileChangeType.DELETED) {
                delFiles.add(fileChange.getFile());
            }
            allFiles.add(fileChange.getFile());
        }
        String allFilesString = allFiles.stream().sorted().collect(Collectors.joining(" "));
        String addFilesString = addFiles.stream().sorted().collect(Collectors.joining(" "));
        String delFilesString = delFiles.stream().sorted().collect(Collectors.joining(" "));
        return Arrays.asList(allFilesString, addFilesString, delFilesString);
    }

    public String parseRevisionToOutputFormat(Revision revision) {
        String revisionId = revision.getId();
        String authorString = revision.getAuthor().convertToSpecificOutputType(VCSType.HG);
        String authorDate = convertZonedDateTimeToString(revision.getAuthorDate(), DATE_FORMAT);
        String fileChanges = String.join("\n", parseFileChangesToOutputFormat(revision.getFileChanges()));
        String commitMessage = revision.getMessage();

        return String.join("\n", Arrays.asList(
                revisionId,
                authorString,
                authorDate,
                fileChanges,
                commitMessage,
                "\n"
        ));
    }
}
