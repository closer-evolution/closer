package uk.ac.qub.eeecs.closer.parsers;

import com.google.gson.reflect.TypeToken;
import uk.ac.qub.eeecs.closer.models.dsl.Author;
import uk.ac.qub.eeecs.closer.models.dsl.FileChange;
import uk.ac.qub.eeecs.closer.models.dsl.Revision;

import java.util.List;

import static uk.ac.qub.eeecs.closer.main.Main.GSON;

/**
 * @author : Jordan
 * @created : 12/01/2021, Tuesday
 * @description : Parser to convert CLOSER json back to in memory objects
 **/
public class CloserParser implements Parser {

    @Override
    public List<Revision> parseInputToFormat(List<String> linesOfInput) {
        String input = String.join("", linesOfInput);
        return GSON.fromJson(input, new TypeToken<List<Revision>>(){}.getType());
    }

    @Override
    public Revision extractDetailsAtRevision(List<String> linesOfRevision, int revisionStart) {
        return null;
    }

    @Override
    public FileChange extractFileChange(String fileChange) {
        return null;
    }

    @Override
    public Author extractAuthorDetails(String authorString) {
        return null;
    }

    @Override
    public String parseRevisionsToOutputFormat(List<Revision> revisions) {
        return GSON.toJson(revisions);
    }

    @Override
    public List<String> parseFileChangesToOutputFormat(List<FileChange> fileChanges) {
        return null;
    }
}
