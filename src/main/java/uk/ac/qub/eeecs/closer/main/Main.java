package uk.ac.qub.eeecs.closer.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import uk.ac.qub.eeecs.closer.exceptions.CloserErrorCode;
import uk.ac.qub.eeecs.closer.exceptions.CloserException;
import uk.ac.qub.eeecs.closer.models.dsl.Revision;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.qub.eeecs.closer.parsers.ParserSelector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.*;

public class Main {

    public static final String APPLICATION_VERSION = "0.0.0.1";
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
            return ZonedDateTime.parse(in.nextString());
        }
    }).enableComplexMapKeySerialization().create();
    public static final ParserSelector PARSER_SELECTOR = new ParserSelector();
    private static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args){
        log.info("Starting CLOSER command line tool execution");
        try{
            //Try to run the main method with catch for exception to print appropriate message
            mainWithoutExceptionHandling(args);
        } catch (CloserException ex){
            //Catch all closer exceptions and display appropriately
            log.error("A CLOSER exception has occurred during execution. Details ="+ex.toString());
        } catch (Exception ex){
            //Catch all finally to prevent tool crashing
            log.error(CloserErrorCode.INTERNAL_CLOSER_EXCEPTION.toString());
        }
        log.info("Finished CLOSER command line tool execution");
    }

    public static void mainWithoutExceptionHandling(String[] args){
        Options options = generateCommandLineOptions();

        log.info("Checking for help or version parameters first");
        checkForHelpOrVersionParameters(args, options);

        log.info("Parsing all command line arguments, validating that all required arguments are present");
        CommandLine commandLine = parseAndValidateCommandLineArguments(args, options ,false);

        ProgramArguments programArguments = extractProgramArguments(commandLine);
        File inputFile = getInputFileAndVerifyPermissions(programArguments.getInputFileLocation());
        File outputFile = getOutputFileAndVerifyPermissions(programArguments.getOutputFileLocation(), programArguments.isAppend(), programArguments.isOverwrite());
        List<String> lines = readFile(programArguments.getInputFileLocation(), inputFile);

        if (programArguments.isPrintInput()) {
            log.info("Printing Input to Standard Output");
            for (String s : lines) {
                System.out.println(s);
            }
            System.out.println("\n---END OF INPUT---\n");
        }

        //If the append parameter is sent then the required process depends on the format of the output
        //for the json closer format the output file must be read in full and then have the list of revisions appended to
        //for all other formats as the outputs are plain text files the output can simply be written without any additional file reading

        List<Revision> revs = new ArrayList<>();
        if (outputFile != null && programArguments.isAppend() && programArguments.getOutputVCSType().equals(VCSType.CLOSER)){
            log.info("Appending to JSON file when output type is CLOSER, Reading previous JSON file content");
            List<String> previousOutputLines = readFile(programArguments.getOutputFileLocation(), outputFile);
            log.info("Deleting previous JSON file, so that appended version can be written to a new file");
            outputFile = getOutputFileAndVerifyPermissions(programArguments.getOutputFileLocation(), programArguments.isAppend(), true);
            revs.addAll(PARSER_SELECTOR.selectParser(VCSType.CLOSER).parseInputToFormat(previousOutputLines));
        }

        log.info("Select VCS Parser based on inputType program argument and parse to CLOSER objects");
        revs.addAll(PARSER_SELECTOR.selectParser(programArguments.getInputVCSType()).parseInputToFormat(lines));
        log.info("Select VCS Parser based on outputType program argument and parse to output format");
        String jsonString = PARSER_SELECTOR.selectParser(programArguments.getOutputVCSType()).parseRevisionsToOutputFormat(revs);

        log.info("Write formatted output to file");
        writeOutput(programArguments, outputFile, jsonString);
    }

    private static void checkForHelpOrVersionParameters(String[] args, Options options) {
        Options helpOptions = new Options();
        helpOptions.addOption(options.getOption("h"));
        helpOptions.addOption(options.getOption("v"));
        CommandLine helpCmd = parseAndValidateCommandLineArguments(args, helpOptions ,true);

        if (helpCmd.hasOption("h")) {
            printCommandLineHelp(options);
            System.exit(0);
        }

        if (helpCmd.hasOption("v")) {
            System.out.println(APPLICATION_VERSION);
            System.exit(0);
        }
    }

    private static void writeOutput(ProgramArguments programArguments, File outputFile, String jsonString) {
        if (outputFile != null) {
            try {
                FileWriter fileWriter = new FileWriter(outputFile, programArguments.isAppend());
                //If in append mode add a line break to make the file formatting
                if (programArguments.isAppend()){
                    fileWriter.write("\n");
                }
                fileWriter.write(jsonString);
                fileWriter.close();
            } catch (IOException e) {
                Map<String, Object> map = new HashMap<>();
                map.put("fileLocation", programArguments.getOutputFileLocation());
                throw new CloserException(CloserErrorCode.CANNOT_WRITE_OUTPUT_FILE, map);
            }
        } else {
            System.out.println(jsonString);
        }
    }

    private static List<String> readFile(String fileLocation, File inputFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(inputFile.toPath());
        } catch (IOException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("fileLocation", fileLocation);
            throw new CloserException(CloserErrorCode.CLOSER_CANNOT_READ_FILE, map);
        }
        return lines;
    }

    private static File getInputFileAndVerifyPermissions(String vcsFileLocation) {
        //Check that the file exists and that the user has at least read permission on it
        File inputFile = new File(vcsFileLocation);
        //If the file does not exist or cannot be read then error
        if (!inputFile.isFile() || !inputFile.canRead()) {
            Map<String, Object> map = new HashMap<>();
            map.put("fileLocation", vcsFileLocation);
            throw new CloserException(CloserErrorCode.CLOSER_CANNOT_READ_FILE, map);
        }
        return inputFile;
    }

    private static File getOutputFileAndVerifyPermissions(String outputFileLocation, boolean append, boolean overwrite) {
        if (outputFileLocation == null) {
            //Return a null file that will cause the response to be sent to standard output
            return null;
        }
        File outputFile = new File(outputFileLocation);
        if (overwrite) {
            if (outputFile.exists()) {
                if (!outputFile.delete()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fileLocation", outputFileLocation);
                    throw new CloserException(CloserErrorCode.CANNOT_OVERWRITE_OUTPUT_FILE, map);
                }
                outputFile = new File(outputFileLocation);
            }
        } else if (!append){
            //Check that the file exists and if it does error
            if (outputFile.exists()) {
                Map<String, Object> map = new HashMap<>();
                map.put("fileLocation", outputFileLocation);
                throw new CloserException(CloserErrorCode.CANNOT_WRITE_OUTPUT_FILE, map);
            }
        }
        return outputFile;
    }

    public static CommandLine parseAndValidateCommandLineArguments(String[] args, Options options, boolean allowForMissingArguments) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args, allowForMissingArguments);
        } catch (ParseException exp) {
            Map<String, Object> map = new HashMap<>();
            if (exp instanceof MissingOptionException) {
                MissingOptionException m = ((MissingOptionException) exp);
                map.put("missingOptions", m.getMissingOptions());
                throw new CloserException(CloserErrorCode.MISSING_COMMAND_lINE_OPTION, map);
            }
            if (exp instanceof MissingArgumentException) {
                MissingArgumentException m = ((MissingArgumentException) exp);
                map.put("option", m.getOption().getOpt());
                map.put("missingArguments", m.getOption().getArgName());
                throw new CloserException(CloserErrorCode.MISSING_COMMAND_lINE_OPTION_ARGUMENT, map);
            }
            throw new CloserException(CloserErrorCode.MISSING_COMMAND_lINE_OPTION_ARGUMENT, map);
        }
        return line;
    }

    private static void printCommandLineHelp(Options options) {
        //Print help details explaining all options and usages
        String header = "Tool for conversion between commonly used Version Control Software outputs\n\n";
        String footer = "";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("closer", header, options, footer, true);
    }

    public static Options generateCommandLineOptions() {
        //First we create all the options required for the command line tool
        //These options will use a common cli library to assist with creating a common interface
        //that users of command line tools will be familiar with
        Options options = new Options();
        Option inputFile = Option.builder("i").required()
                .desc("Relevant file path to the target input text file. File must be in the format aligned with the inputType")
                .hasArg().argName("FILE")
                .longOpt("inputFile").build();
        Option inputType = Option.builder("t").required()
                .desc("Type of target input file. Must be one of " + Arrays.toString(VCSType.values()))
                .hasArg().argName("TYPE")
                .longOpt("inputType").build();
        Option outputFile = Option.builder("o")
                .desc("*Optional Parameter*, if provided the mapping to the output " +
                        "format from the input file will be written to this location, " +
                        "otherwise output will be written to standard output")
                .hasArg().argName("FILE").longOpt("outputFile").build();
        Option outputType = Option.builder("u").required()
                .desc("Type of the generated output. Must be one of " + Arrays.toString(VCSType.values()))
                .hasArg().argName("TYPE")
                .longOpt("outputType").build();
        Option help = Option.builder("h")
                .desc("Print details of all accepted parameters and their functionality")
                .longOpt("help").build();
        Option version = Option.builder("v")
                .desc("Print the version of CLOSER software currently being used")
                .longOpt("version").build();
        Option append = Option.builder("a")
                .desc("If the optional output file parameter is provide and the file exists the generated output will be appended to the txt file.")
                .longOpt("append").build();
        Option overwrite = Option.builder("w")
                .desc("If the optional output file parameter is provide and the file exists the generated output will overwrite the contents of the file" +
                        "Will error if target output file exists but cannot be deleted")
                .longOpt("overwrite").build();
        Option print = Option.builder("p")
                .desc("This parameter will print the contents of the input file to standard output")
                .longOpt("printInput").build();

        options.addOption(inputFile);
        options.addOption(inputType);
        options.addOption(outputFile);
        options.addOption(outputType);
        options.addOption(help);
        options.addOption(version);
        options.addOption(append);
        options.addOption(overwrite);
        options.addOption(print);
        return options;
    }

    public static ProgramArguments extractProgramArguments(CommandLine commandLine) {
        //Arguments with parameters
        String inputFile = commandLine.getOptionValue("i");
        String outputFile = commandLine.getOptionValue("o");
        VCSType inputFileType = parseVCSTypeFromCommandLineArgument(commandLine.getOptionValue("t"));
        VCSType outputFileType = parseVCSTypeFromCommandLineArgument(commandLine.getOptionValue("u"));
        ArgumentValidator.validateVCSTypes(inputFileType, commandLine.getOptionValue("t"), outputFileType, commandLine.getOptionValue("u"));

        //Flag parameters
        boolean help = commandLine.hasOption("h");
        boolean version = commandLine.hasOption("v");
        boolean append = commandLine.hasOption("a");
        boolean overwrite = commandLine.hasOption("w");
        boolean printInput = commandLine.hasOption("p");
        ArgumentValidator.validateOutputMode(append, overwrite);
        return new ProgramArguments(inputFile, inputFileType, outputFile, outputFileType,
                help, version, append, overwrite, printInput);
    }

    public static VCSType parseVCSTypeFromCommandLineArgument(String argument) {
        try {
            return VCSType.valueOf(argument);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
