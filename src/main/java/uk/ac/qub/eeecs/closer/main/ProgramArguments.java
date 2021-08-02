package uk.ac.qub.eeecs.closer.main;

/**
 * Class to store the arguments passed in to the command line application so that they can be passed as a single object
 */
public class ProgramArguments {

    private String inputFileLocation;
    private VCSType inputVCSType;
    private VCSType outputVCSType;
    private String outputFileLocation;
    private boolean help;
    private boolean version;
    private boolean append;
    private boolean overwrite;
    private boolean printInput;

    public ProgramArguments(String inputFileLocation, VCSType inputVCSType,
                            String outputFileLocation, VCSType outputVCSType,
                            boolean help, boolean version,
                            boolean append, boolean overwrite,
                            boolean printInput) {
        this.inputFileLocation = inputFileLocation;
        this.inputVCSType = inputVCSType;
        this.outputVCSType = outputVCSType;
        this.outputFileLocation = outputFileLocation;
        this.help = help;
        this.version = version;
        this.append = append;
        this.overwrite = overwrite;
        this.printInput = printInput;
    }

    public String getOutputFileLocation() {
        return outputFileLocation;
    }

    public void setOutputFileLocation(String outputFileLocation) {
        this.outputFileLocation = outputFileLocation;
    }

    public String getInputFileLocation() {
        return inputFileLocation;
    }

    public void setInputFileLocation(String inputFileLocation) {
        this.inputFileLocation = inputFileLocation;
    }

    public VCSType getInputVCSType() {
        return inputVCSType;
    }

    public void setInputVCSType(VCSType inputVCSType) {
        this.inputVCSType = inputVCSType;
    }

    public VCSType getOutputVCSType() {
        return outputVCSType;
    }

    public void setOutputVCSType(VCSType outputVCSType) {
        this.outputVCSType = outputVCSType;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public boolean isVersion() {
        return version;
    }

    public void setVersion(boolean version) {
        this.version = version;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isPrintInput() {
        return printInput;
    }

    public void setPrintInput(boolean printInput) {
        this.printInput = printInput;
    }
}
