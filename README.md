# CLOSER - A Common Language for Software Evolution in Repositories

### Project Details
This Project (original developer Jordan Garrity) covers a framework for conversion between VCS outputs 
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

### Licence and Copyright

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see [gnu.org/licences](https://www.gnu.org/licenses/).

Copyright (C) the authors, all rights reserved.

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


