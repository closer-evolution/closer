# CLOSER Documentation
A Common Language of Software Evolution in Repositories (CLOSER), developed and maintained by the [Software Engineering Research Theme](https://www.qub.ac.uk/schools/eeecs/Research/softwareengineering/) in the [School of Electronics, Electrical Engineering and Computer Science](https://www.qub.ac.uk/schools/eeecs/) at [Queen's University Belfast](https://www.qub.ac.uk/). Original development by Jordan Garrity. Project lead [David Cutting](https://davecutting.uk).

CLOSER is free software licenced under the [GNU GPL v3](https://www.gnu.org/licences/).

## Project Links

- Main Project Site: [closer-evolution.github.io/closer](https://closer-evolution.github.io/closer/)
- Repository: [github.com/closer-evolution/closer](https://github.com/closer-evolution/closer/)
- Documentation: [closer-evolution.github.io/closer](https://closer-evolution.github.io/closer/)
- Release Downloads: [github.com/closer-evolution/closer/releases](https://github.com/closer-evolution/closer/releases)
  - Latest Release: [Latest CLOSER Release](https://github.com/closer-evolution/closer/releases/latest)
- Support and Bugs: [Please open Github issue](https://github.com/closer-evolution/closer/issues) (alternatively you can email [d.cutting@qub.ac.uk](mailto://d.cutting@qub.ac.uk) but opening an issue will be quicker and better).

## Obtaining CLOSER

CLOSER is available as a compiled (binary) JAR from Github (see links above) or the Java source as an IntelliJ project from [the repository](https://github.com/closer-evolution/closer).

# Documentation

## Overview

CLOSER is a method of mapping repository metadata (log files) from specific formats (GIT, SVN and HG) to/from a domain-specific generic representation (the CLOSER format; a generic JSON representation). You can also convert from one specific format to another (within certain limitations for data representation).

The most common use of CLOSER is to allow the easy machine reading (for analysis and data processing) of historic repository information in a consistent format. It also supports common analysis over multiple concrete repository types.

CLOSER can be run from a compiled JAR on the command line: ```java -jar closer.jar [options]``` where the ```[options]``` determine the operation.

## Command Line Options

```
Usage: closer [-a] [-h] -i <FILE> [-o <FILE>] [-p] -t <TYPE> -u <TYPE> [-v] [-w]
Tool for conversion between commonly used Version Control Software outputs

 -a,--append              If the optional output file parameter is provide
                          and the file exists the generated output will be
                          appended to the txt file.
 -h,--help                Print details of all accepted parameters and
                          their functionality
 -i,--inputFile <FILE>    Relevant file path to the target input text
                          file. File must be in the format aligned with
                          the inputType
 -o,--outputFile <FILE>   *Optional Parameter*, if provided the mapping to
                          the output format from the input file will be
                          written to this location, otherwise output will
                          be written to standard output
 -p,--printInput          This parameter will print the contents of the
                          input file to standard output
 -t,--inputType <TYPE>    Type of target input file. Must be one of
                          [CLOSER, GIT, SVN, HG]
 -u,--outputType <TYPE>   Type of the generated output. Must be one of
                          [CLOSER, GIT, SVN, HG]
 -v,--version             Print the version of CLOSER software currently
                          being used
 -w,--overwrite           If the optional output file parameter is provide
                          and the file exists the generated output will
                          overwrite the contents of the fileWill error if
                          target output file exists but cannot be deleted
```

## CLOSER Data Format

A JSON array of changes containing the metadata, for example the following for a single commit:

```json
[
  {
    "id": "abcdef0123456789",
    "author": {
      "identifier": "Bob Smith",
      "emailAddress": "bob@example.com"
    },
    "authorDate": "2023-01-14T14:19:11Z",
    "committer": {
      "identifier": "Bob Smith",
      "emailAddress": "bob@example.com"
    },
    "committerDate": "2023-01-14T14:19:11Z",
    "message": "Some change details here",
    "fileChanges": [
      {
        "type": "MODIFIED",
        "file": "file.txt"
      },
      {
        "type": "ADDED",
        "file": "somedir/newfile.txt"
      }
    ]
  }
]
```

## Repository Data Extraction

To generate log data from the repositories in the correct format the following options must be used with the repository tool commands.

### Git

```git log --pretty=fuller --name-status``` will generate the required log format for CLOSER to read, this can be piped into a file for ingress for example ```git log --pretty=fuller --name-status > output.log```.

### SVN

```svn log . --verbose``` will generate the required log format for CLOSER to read, this can be piped into a file for ingress for example ```svn log . --verbose > output.log```.

### Mercurial (HG)

```hg log --template '{rev}:{node}\n{author}\n{date|isodate}\n{files}\n{file_adds}\n{file_dels}\n{desc}\n\n'``` will generate the required log format for CLOSER to read, this can be piped into a file for ingress for example ```hg log --template '{rev}:{node}\n{author}\n{date|isodate}\n{files}\n{file_adds}\n{file_dels}\n{desc}\n\n' > output.log```