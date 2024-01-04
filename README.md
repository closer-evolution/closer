Original Author: Jordan Garrity 

### Project Details
This Project covers a framework for conversion between VCS outputs 
to assist with repository mining across version control system 
(VCS) variants.

It defines a domain specific language (DSL) for a 
consistent method to display the key data from each VCS output variant.

It the outlines parsers for conversion of the output for the main VCS 
variants to the DSL model which can be used to analyse of the output 
for repository mining. It is independent of the VCS it originated from
but retains all the key data so it can be used for comparisons between them.

It also outlines parsers for conversion from the DSL model back to any of the 
original VCS variants. This provides the ability to convert from one VCS output
such as Git to SVN. This allows for existing tools for mining and analysing a specific 
repository type to now be used on any repository type.

### Releases and Binary Downloads
From the CLOSER repository: [github.com/closer-evolution/closer/releases](https://github.com/closer-evolution/closer/releases) 
- Here is a **[direct link to the latest version](https://github.com/closer-evolution/closer/releases/latest)**.

### Documentation
From the CLOSER site: [closer-evolution.github.io/closer](https://closer-evolution.github.io/closer/)

### VCS variants supported
git, svn, hg (Mercurial)

### Project structure explained
This project is an IntelliJ project.

src/main/java contains all the source code for this project.

src/main/java/models contains the class files that build up the DSL
model and class files for the various VCS variants. 

For the DSL model these classes can all be jsonified as the output of the parsing 
to the model will be a json file with the key data.
The class files for the VCS variants can be used 
to generate their respective outputs from the DSL model.

src/main/java/parsers contains the files with the functions for parsing 
to and from the DSL, it is organised by VCS variant with the the functions
for converting to and from each variant contained in the same class.


