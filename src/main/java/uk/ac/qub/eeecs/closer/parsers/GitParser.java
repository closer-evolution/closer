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

public class GitParser implements Parser {

    private static final String COMMIT = "commit ";
    private static final Pattern COMMIT_REGEX = Pattern.compile("\\bcommit\\s+(.*)");
    private static final String AUTHOR = "Author:     ";
    private static final Pattern AUTHOR_REGEX = Pattern.compile("\\bAuthor:\\s+(.*)");
    private static final String AUTHOR_DATE = "AuthorDate: ";
    private static final Pattern AUTHOR_DATE_REGEX = Pattern.compile("\\bAuthorDate:\\s+(.*)");
    private static final String COMMITTER = "Commit:     ";
    private static final Pattern COMMITTER_REGEX = Pattern.compile("\\bCommit:\\s+(.*)");
    private static final String COMMIT_DATE = "CommitDate: ";
    private static final Pattern COMMIT_DATE_REGEX = Pattern.compile("\\bCommitDate:\\s+(.*)");
    private static final String DATE_FORMAT = "E MMM d HH:mm:ss yyyy Z";
    private static final String MERGE = "Merge: ";
    private static final Pattern MERGE_REGEX = Pattern.compile("\\bMerge:\\s+(.*)");

    private static Logger log = LogManager.getLogger(GitParser.class);

    @Override
    public List<Revision> parseInputToFormat(List<String> linesOfInput) {
        //Add additional blank line to make sure there is a uniform format
        linesOfInput.add("");
        log.info("Calculate commit starting points for the Git log");
        List<Integer> commitStarts = extractRevisionStartingPoints(linesOfInput, "commit [a-f0-9]{40}");
        log.info("Input file contains "+commitStarts.size()+" revisions");
        return parseAllRevisionsToFormat(linesOfInput, commitStarts);
    }

    @Override
    public Revision extractDetailsAtRevision(List<String> linesOfRevision, int revisionStart) {
        //First 5 lines of the git output will contain the id, author and committer and time of both events
        //Possibly merge details if it is a merge commit

        log.trace("Extract commit id from Git revision starting at line "+revisionStart);
        String id = extractLineDetailsWithRegex(linesOfRevision.get(0), COMMIT_REGEX);
        String mergeDetails = extractLineDetailsWithRegex(linesOfRevision.get(1), MERGE_REGEX);

        //If the merge details are not present then no extension required to line indices
        int mergeCommitIndexOffset = (mergeDetails == null)?0:1;

        log.trace("Extract author from revision starting at line "+revisionStart);
        String authorString = extractLineDetailsWithRegex(linesOfRevision.get(1+mergeCommitIndexOffset), AUTHOR_REGEX);
        Author author = extractAuthorDetails(authorString);
        if (author == null){
            log.warn("Cannot extract author for Git revision starting at line "+revisionStart+", checking line "+(revisionStart+1+mergeCommitIndexOffset));
        }

        log.trace("Extract author date from revision starting at line "+revisionStart);
        String authorDateString = extractLineDetailsWithRegex(linesOfRevision.get(2+mergeCommitIndexOffset), AUTHOR_DATE_REGEX);
        ZonedDateTime authorDate = null;
        try {
            authorDate = extractDateTimeZoned(authorDateString, DATE_FORMAT);
        } catch (CloserException ex){
            log.warn("Cannot extract author date for Git revision starting at line "+revisionStart+", checking line "+(revisionStart+2+mergeCommitIndexOffset)+". Details="+ex.toString());
        }

        log.trace("Extract committer from revision starting at line "+revisionStart);
        String committerString = extractLineDetailsWithRegex(linesOfRevision.get(3+mergeCommitIndexOffset), COMMITTER_REGEX);
        Author committer = extractAuthorDetails(committerString);
        if (committer == null){
            log.warn("Cannot extract committer for Git revision starting at line "+revisionStart+", checking line "+(revisionStart+3+mergeCommitIndexOffset));
        }

        log.trace("Extract commit id from revision starting at line "+revisionStart);
        String committerDateString = extractLineDetailsWithRegex(linesOfRevision.get(4+mergeCommitIndexOffset), COMMIT_DATE_REGEX);
        ZonedDateTime committerDate = null;
        try {
            committerDate = extractDateTimeZoned(committerDateString, DATE_FORMAT);
        } catch (CloserException ex){
            log.warn("Cannot extract committer date for Git revision starting at line "+revisionStart+", checking line "+(revisionStart+4+mergeCommitIndexOffset)+". Details="+ex.toString());
        }

        int commitOffset = 6+mergeCommitIndexOffset;

        List<String> commitMessageAndFileChanges = linesOfRevision.subList(commitOffset, linesOfRevision.size()-1);
        List<String> commitMessageLines = new ArrayList<>();
        List<String> fileChanges = new ArrayList<>();
        boolean endOfCommitMessage = false;
        for (String commitLine: commitMessageAndFileChanges) {
            if (endOfCommitMessage){
                fileChanges.add(commitLine);
            } else {
                if (commitLine.isEmpty()) {
                    endOfCommitMessage = true;
                }else {
                    commitMessageLines.add(commitLine);
                }
            }
        }

        String commitMessage = commitMessageLines.stream().map(String::trim).collect(Collectors.joining("\n"));
        List<FileChange> extractFileChanges = extractFileChanges(fileChanges);
        Revision revision = new Revision(id, author, committer, authorDate, committerDate, commitMessage, extractFileChanges);
        revision.setMergeDetails(mergeDetails);
        return revision;
    }

    public String extractLineDetailsWithRegex(String line, Pattern pattern){
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()){
            return matcher.group(1);
        }
        return null;
    }

    public List<FileChange> extractFileChanges(List<String> linesOfFileChanges) {
        return linesOfFileChanges.stream().map(this::extractFileChange).collect(Collectors.toList());
    }

    @Override
    public FileChange extractFileChange(String fileChange) {
        String[] fileChangeParts = fileChange.trim().split("\t");

        FileChangeType fileChangeType = FileChangeType.getFileChangedTypeFromGitIdentifier(fileChangeParts[0].charAt(0));
        Integer changePercentage = null;
        try {
            changePercentage = Integer.parseInt(fileChangeParts[0].substring(1));
        }catch (NumberFormatException e){
            //Leave it as null
        }

        String file = fileChangeParts[1];
        String newLocation = null;
        if (fileChangeParts.length > 2){
            newLocation = fileChangeParts[2];
        }
        return new FileChange(fileChangeType, file, changePercentage, newLocation);
    }

    @Override
    public Author extractAuthorDetails(String authorString) {
        int splittingLocation = authorString.lastIndexOf('<');
        String authorName = authorString.substring(0, splittingLocation-1);
        String authorEmail = authorString.substring(splittingLocation+1, authorString.length()-1);

        return new Author(authorName, authorEmail);
    }

    @Override
    public String parseRevisionsToOutputFormat(List<Revision> revisions) {
        StringBuilder revisionsString = new StringBuilder();
        boolean last = false;
        for (int i = 0; i < revisions.size(); i++) {
            if (i+1 == revisions.size()){
                last = true;
            }
            revisionsString.append(parseRevisionToOutputFormat(revisions.get(i), last));
        }
        return revisionsString.toString();
    }

    @Override
    public List<String> parseFileChangesToOutputFormat(List<FileChange> fileChanges) {
        List<String> fileChangeStrings = new ArrayList<>();
        for (FileChange fileChange:fileChanges) {
            List<FileChange> supportedFileChanges = fileChange.convertToSupportedFileChangeTypes(VCSType.GIT);
            for (FileChange supportedFileChange:supportedFileChanges) {
                fileChangeStrings.add(supportedFileChange.convertToSpecificOutputFormat(VCSType.GIT));
            }
        }
        return fileChangeStrings;
    }

    public String parseRevisionToOutputFormat(Revision revision, boolean lastRevision) {

        String commitIdLine = COMMIT+revision.getId();
        if (revision.getMergeDetails() != null){
            commitIdLine=commitIdLine+"\n"+MERGE+revision.getMergeDetails();
        }
        String author = AUTHOR+revision.getAuthor().convertToSpecificOutputType(VCSType.GIT);
        String authorDate = AUTHOR_DATE+convertZonedDateTimeToString(revision.getAuthorDate(), DATE_FORMAT);
        String committer = COMMITTER+revision.getCommitter().convertToSpecificOutputType(VCSType.GIT);
        String committerDate = COMMIT_DATE+convertZonedDateTimeToString(revision.getCommitterDate(), DATE_FORMAT);
        String commitMessage = "\n    "+revision.getMessage().replaceAll("\\n", "\n    ")+"\n";

        StringBuilder fileChanges = new StringBuilder();
        for (String fileChange: parseFileChangesToOutputFormat(revision.getFileChanges())) {
                fileChanges.append(fileChange).append("\n");
        }

        if (!lastRevision){
            fileChanges.append("\n");
        }

        return String.join("\n", Arrays.asList(
                commitIdLine,
                author,
                authorDate,
                committer,
                committerDate,
                commitMessage,
                revision.getFileChanges().isEmpty()?"":fileChanges.toString()));
    }
}
