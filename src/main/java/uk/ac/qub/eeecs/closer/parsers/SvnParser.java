package uk.ac.qub.eeecs.closer.parsers;

import uk.ac.qub.eeecs.closer.exceptions.CloserException;
import uk.ac.qub.eeecs.closer.main.VCSType;
import uk.ac.qub.eeecs.closer.models.dsl.Author;
import uk.ac.qub.eeecs.closer.models.dsl.FileChange;
import uk.ac.qub.eeecs.closer.models.dsl.FileChangeType;
import uk.ac.qub.eeecs.closer.models.dsl.Revision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SvnParser implements Parser {

    public static final String REVISION_SEPARATOR = "------------------------------------------------------------------------";

    private static Logger log = LogManager.getLogger(SvnParser.class);

    @Override
    public List<Revision> parseInputToFormat(List<String> linesOfInput) {
        List<Integer> commitStarts = extractRevisionStartingPoints(linesOfInput,"r[0-9]+.*");
        return parseAllRevisionsToFormat(linesOfInput, commitStarts);
    }

    @Override
    public Revision extractDetailsAtRevision(List<String> linesOfRevision, int revisionStart) {

        //Split and trim the first line for svn to get the revision, author, date and commit message lines
        String[] lineParts = linesOfRevision.get(0).split("\\|");

        log.trace("Extract commit id from SVN revision starting at line "+revisionStart);
        String id = lineParts[0].trim();

        log.trace("Extract user identifier for SVN revision starting at line "+revisionStart);
        Author author = null;
        try{
            author = extractAuthorDetails(lineParts[1].trim());
        } catch (ArrayIndexOutOfBoundsException ex){
            log.warn("Cannot extract user identifier for SVN revision starting at line "+revisionStart);
        }

        log.trace("Extract date for SVN revision starting at line "+revisionStart);
        ZonedDateTime authorDate = null;
        try {
            String dateString = lineParts[2].replaceAll("\\((.*?)\\)", "").trim();
            authorDate = extractDateTimeZoned(dateString, "yyyy-MM-dd HH:mm:ss Z");
        } catch (ArrayIndexOutOfBoundsException | CloserException ex) {
            log.warn("Cannot extract date for SVN revision starting at line "+revisionStart);
        }

        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(lineParts[3].trim());
        m.find();
        int commitMessageLines = 0;
        try {
            commitMessageLines = Integer.parseInt(m.group());
        } catch (NumberFormatException ex){
            //Let the commit lines remain 0
        }

        int endOfRevisionContent = linesOfRevision.size() - 1;
        List<String> commitLines = linesOfRevision.subList(endOfRevisionContent - commitMessageLines, endOfRevisionContent);
        String commitMessage = String.join("\n", commitLines);
        List<FileChange> extractFileChanges = extractFileChanges(linesOfRevision.subList(2, endOfRevisionContent - 1 - commitMessageLines));
        return new Revision(id, author, author, authorDate, authorDate, commitMessage, extractFileChanges);
    }

    public List<FileChange> extractFileChanges(List<String> linesOfFileChanges) {
        return linesOfFileChanges.stream().map(this::extractFileChange).collect(Collectors.toList());
    }

    @Override
    public FileChange extractFileChange(String fileChange) {
        String[] fileChangeParts = fileChange.trim().split(" ");
        FileChangeType fileChangeType = FileChangeType.getFileChangedTypeFromSvnIdentifier(fileChangeParts[0].charAt(0));
        Integer changePercentage = null;

        //Remove leading / from svn files so that it is consistent
        String file = fileChangeParts[1].replaceFirst("/", "");
        String newLocation = null;
        if (fileChangeParts.length > 2) {
            newLocation = fileChangeParts[2];
        }
        return new FileChange(fileChangeType, file, changePercentage, newLocation);
    }

    @Override
    public Author extractAuthorDetails(String authorString) {
        return new Author(authorString, authorString);
    }

    @Override
    public String parseRevisionsToOutputFormat(List<Revision> revisions) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(REVISION_SEPARATOR);
        revisions.forEach(r -> stringBuilder.append(parseRevisionToOutputFormat(r)));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    @Override
    public List<String> parseFileChangesToOutputFormat(List<FileChange> fileChanges) {
        List<String> fileChangeStrings = new ArrayList<>();
        for (FileChange fileChange:fileChanges) {
            List<FileChange> supportedFileChanges = fileChange.convertToSupportedFileChangeTypes(VCSType.SVN);
            for (FileChange supportedFileChange:supportedFileChanges) {
                fileChangeStrings.add(supportedFileChange.convertToSpecificOutputFormat(VCSType.SVN));
            }
        }
        return fileChangeStrings;
    }

    public String parseRevisionToOutputFormat(Revision revision) {
        String authorDate = convertZonedDateTimeToString(revision.getAuthorDate(), "yyyy-MM-dd HH:mm:ss Z (E, d MMM yyyy)");

        String[] commitMessageLines = revision.getMessage().split("\n");
        int lineCount = commitMessageLines.length;

        String lineString = lineCount<2? lineCount +" line": lineCount +" lines";
        String commitIdLine = String.join(" | ", Arrays.asList(revision.getId(), revision.getAuthor().getIdentifier(), authorDate, lineString));

        StringBuilder fileChanges = new StringBuilder();
        for (String fileChange: parseFileChangesToOutputFormat(revision.getFileChanges())) {
            fileChanges.append(fileChange).append("\n");
        }

        for (String commitMessageLine : commitMessageLines) {
            fileChanges.append("\n").append(commitMessageLine);
        }

        return String.join("\n", Arrays.asList( "",commitIdLine, "Changed paths:", fileChanges.toString(), REVISION_SEPARATOR));
    }
}
