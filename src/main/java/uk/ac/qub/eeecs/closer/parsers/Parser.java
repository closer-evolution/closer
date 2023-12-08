package uk.ac.qub.eeecs.closer.parsers;

import uk.ac.qub.eeecs.closer.exceptions.CloserErrorCode;
import uk.ac.qub.eeecs.closer.exceptions.CloserException;
import uk.ac.qub.eeecs.closer.models.dsl.Author;
import uk.ac.qub.eeecs.closer.models.dsl.FileChange;
import uk.ac.qub.eeecs.closer.models.dsl.Revision;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public interface Parser {

    List<Revision> parseInputToFormat(List<String> linesOfInput);

    default List<Integer> extractRevisionStartingPoints(List<String> linesOfInput, String commitStartIdentifier) {
        List<Integer> commitStarts = new ArrayList<>();
        //Iterate the lines of input to determine the points where commits start
        for (int i = 0; i < linesOfInput.size(); i++) {
            String line = linesOfInput.get(i);
            if (line.matches(commitStartIdentifier)){
                commitStarts.add(i);
            }
        }
        commitStarts.add(linesOfInput.size());
        return commitStarts;
    }

    default List<Revision> parseAllRevisionsToFormat(List<String> linesOfInput, List<Integer> commitStarts) {
        List<Revision> revisions = new ArrayList<>();
        //For each commit then extract the details from it
        for (int i = 0; i < commitStarts.size() - 1; i++) {
            List<String> linesOfRevision = linesOfInput.subList(commitStarts.get(i), commitStarts.get(i + 1));
            revisions.add(extractDetailsAtRevision(linesOfRevision, commitStarts.get(i)));
        }
        return revisions;
    }

    Revision extractDetailsAtRevision(List<String> linesOfRevision, int revisionStart);

    FileChange extractFileChange(String fileChange);

    default ZonedDateTime extractDateTimeZoned(String dateString, String dateFormat) {
        try {
            // note Locale.US required to allow for "Sep" in log strings, not "Sept" in later Java
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.US);
            return ZonedDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("inputDate", dateString);
            map.put("dateFormat", dateFormat);
            throw new CloserException(CloserErrorCode.DATE_PARSING_ERROR, map);
        }
    }

    default String convertZonedDateTimeToString(ZonedDateTime dateTime, String dateFormat){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
            return dateTime.format(formatter);
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("inputDate", dateTime.toString());
            map.put("dateFormat", dateFormat);
            throw new CloserException(CloserErrorCode.DATE_PARSING_ERROR, map);
        }
    }

    Author extractAuthorDetails(String authorString);

    String parseRevisionsToOutputFormat(List<Revision> revisions);

    List<String> parseFileChangesToOutputFormat(List<FileChange> fileChanges);

}
