package uk.ac.qub.eeecs.closer.parsers;

import uk.ac.qub.eeecs.closer.main.VCSType;

public class ParserSelector {

    private final GitParser gitParser = new GitParser();
    private final SvnParser svnParser = new SvnParser();
    private final HgParser hgParser = new HgParser();
    private final CloserParser closerParser = new CloserParser();

    public Parser selectParser(VCSType vcsInputType){
        switch (vcsInputType){
            case GIT: return gitParser;
            case SVN: return svnParser;
            case HG: return hgParser;
            case CLOSER: return closerParser;
            default:
                throw new IllegalStateException("Unexpected value: " + vcsInputType);
        }
    }
}
