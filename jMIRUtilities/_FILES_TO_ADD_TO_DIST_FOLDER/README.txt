==========================================================================
 jMIRUtilities 1.4.1
 by Cory McKay
 Copyright (C) 2010 (GNU GPL)
==========================================================================


-- OVERVIEW -- 

jMIRUtilities is an open source set of tools for performing miscellaneous
tasks relating to the jMIR music classification research software suite.
More information on jMIR is available at http://jmir.sourceforge.net. The
current release version can perform the following tasks:

- Manual instance labelling: A simple GUI is provided for quickly batch
labelling sets of audio, MIDI or other files and generating a corresponding
ACE XML Instance Label file.
- Automatic instance labelling: An ACE XML Instance Label file may be 
automatically generated based on a tab-delimited text file containing
labelled instances. This is convenient for model classifications that
are stored in a spreadsheet table, for example, which may easily be
exported to such a tab-delimited text files.
- Extract labels from iTunes files: An iTunes XML file may be parsed
and a delimited text file generated that lists the File Path, Recording
Title, Artist Name, Composer Name, Album Title and/or Genre Names for
each recording in the file, as requested by the user. This can be a useful
pre-processing step for generating ACE XML Instance Label files from
iTunes XML files via jMIRUtilities’ automatic instance labelling
functionality.
- Modify instance identifiers: Text files containing mappings between old
instance identifiers and new instance identifiers may be used to 
automatically update the instance identifiers found in an ACE XML Feature 
Values file.
- Feature merging: ACE XML Feature Value files that contain different
features for the same instances may be merged into a single ACE XML Feature
Value file. ACE XML Feature Description files that correspond to these
Feature Value files may also be merged. This might be used, for example, to
combine feature values extracted by jAudio with feature values extracted by
jWebMiner for the same pieces of music.
- Split instances by feature types: An ACE XML Feature Values file can be
automatically broken into one new ACE XML Features Values file for each
feature that is contained in the original file. Each new file only contains
feature values for its associated feature, and only contains instances that
have the given feature value available.

Please contact Cory McKay (cory.mckay@mail.mcgill.ca) with any bug reports
or questions relating to jMIRUtilities. 


-- COMPATIBILITY --

The jMIRUtilities software is written in Java, which means that it can
theoretically be run on any system that has the Java Runtime 
Environment (JRE) installed on it. Although the software should
theoretically run under earlier versions of Windows, OS X, Linux, 
Solaris or any other operating system with the JRE installed on it,
users should be advised that the software has not yet been tested on 
other platforms, so difficulties may be encountered.

This software was developed with version 1.6.0_16 of the JDK (Java 
Development Kit), so it is suggested that the corresponding version 
or higher of the JRE be installed on the user's computer.


-- INSTALLING THE JAVA RUNTIME ENVIRONMENT --

If your system already has the JRE installed, you may skip this 
section. If not, you will need to install it in order to run 
jMIRUtilities. The JRE can be downloaded for free from the java.sun.com
web site. The JDK includes the JRE.

When the download is complete, follow the installation instructions 
that come with it in order to install it. 


-- INSTALLING jMIRUtilities --

The jMIRUtilities software is delivered in a zipped file, from which 
jMIRUtilities can be extracted using any of a variety of dearchiving 
utilities (e.g. WinZip).

There are two versions of jMIRUtilities, namely the development version
and the user version. The user version contains everything needed to
run jMIRUtilities, but does not include any source code. The developer 
version does include source code.

The user version unzips into a single directory. Installation simply 
involves extracting this directory to any desired disk location.

The developer version presents jMIRUtilities in the form of a NetBeans 
project. Five directories are contained in the zipped distribution
file:

	- jMIRUtilities: Contains the jMIRUtilities source and bytecode 
	files, as well as other NetBeans project documents and 
	general documentation.
	- Third_Party_Jars: Contains the third party software used by
	jMIRUtilities. Further information is available in the licenses
	section of the manual.
	- UtilityClasses: General jMIR classes, including source code,
	used by jMIRUtilities.
	- ACE: The jMIR ACE NetBeans project, including source code. 
	jMIRUtilities uses some classes from this software.
	- jMusicMetaManager: The jMIR jMusicMetaManager NetBeans 
	project, including source code. jMIRUtilities uses some classes 
	from this software.


-- RUNNING THE SOFTWARE -- 

A file named "jMIRUtilities.jar" is produced upon installation.
The software is accessed via the command line (e.g. the DOS
prompt). To access DOS under Windows, for example, go to the Start 
Menu, select Run and type "cmd". Then use the "cd" command to move to
the directory that contains the jMIRUtilities.jar file. In that directory, 
type:

	java -jar jMIRUtilities.jar -help

This will provide the user with the valid command line flags that can
be used with jMIRUtilities.

It should be noted that the JRE does not always allocate sufficient
memory for jMIRUtilities to process large music collections. Running 
jMIRUtilities using the above method could therefore result in an out
of memory error (although this is relatively rare).

It is therefore sometimes preferable to manually allocate a greater
amount of memory to jMIRUtilities before running it. 250 MB should be more
than enough for most situations. This can be done by entering the 
following at the command prompt:

	java -ms16M -mx250M -jar jMIRUtilities.jar


-- LICENSING AND LIABILITY -- 

jMIRUtilities 1.4.1
Copyright (C) 2010 Cory McKay

This program is free software; you can redistribute it
and/or modify it under the terms of the GNU General 
Public License as published by the Free Software Foundation.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public 
License along with this program; if not, write to the Free 
Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
USA.


-- UPDATES SINCE VERSION 1.0 -- 

jMIRUtilities 1.4.1:
- Fixed a bug related to parsing ACE XML and iTunes XML files.
- Includes updated support libraries (ACE, jMusicMetaManager
 and UtilityClasses).

jMIRUtilities 1.4:
- New processing functionality
- Includes updated support libraries (ACE and UtilityClasses)
