==========================================================================
 jSymbolic 1.2.1
 by Cory McKay
 Copyright (C) 2010 (GNU GPL)
==========================================================================


-- OVERVIEW --

jSymbolic is an open source MIDI feature extraction system intended for use
with automatic classification systems. This is a prototype version of the
software only.

jSymbolic was developed as part of the jMIR music classification research
software suite, and may be used either as part of this suite or
independently. More information on jMIR is available at
http://jmir.sourceforge.net.

Please Cory McKay (cory.mckay@mail.mcgill.ca) with any bug reports or
questions relating to the software. 


-- COMPATIBILITY --

The jSymbolic software is written in Java, which means that it can
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
section. If not, you will need to install it in order to run jSymbolic.
The JRE can be downloaded for free from the java.sun.com web site. The
JDK includes the JRE.

When the download is complete, follow the installation instructions 
that come with it in order to install it. 


-- INSTALLING jSymbolic --

The jSymbolic software is delivered in a zipped file, from which 
jSymbolic can be extracted using any of a variety of dearchiving 
utilities (e.g. WinZip).

There are two versions of jSymbolic, namely the development version
and the user version. The user version contains everything needed to
run jSymbolic, but does not include any source code. The developer 
version does include source code.

The user version unzips into a single directory. Installation simply 
involves extracting this directory to any desired disk location.

The developer version presents jSymbolic in the form of a NetBeans 
project. Three directories are contained in the zipped distribution
file:

	- jSymbolic: Contains the jSymbolic source and bytecode 
	files, as well as other NetBeans project documents and 
	general documentation.
	- UtilityClasses: General jMIR classes, including source code,
	used by jSymbolic.
	- ACE: The jMIR ACE NetBeans project, including source code. 
	jSymbolic uses some classes from this software.


-- RUNNING THE SOFTWARE -- 

A file named "jSymbolic.jar" is produced upon installation. The
simplest way to start jSymbolic is to simply double click on this 
file.

The software may also be accessed via the command line (e.g. the DOS
prompt). To access DOS under Windows, for example, go to the Start 
Menu, select Run and type "cmd". Then use the "cd" command to move to
the directory that contains the jSymbolic.jar file. In that directory, 
type:

	java -jar jSymbolic.jar

It should be noted that the JRE does not always allocate sufficient
memory for jSymbolic to process large music collections. Running 
jSymbolic using either of the above two methods could therefore result
in an out of memory error (although this is relatively rare).

It is therefore sometimes preferable to manually allocate a greater
amount of memory to jSymbolic before running it. 250 MB should be more
than enough for most situations. This can be done by entering the 
following at the command prompt:

	java -ms16M -mx250M -jar jSymbolic.jar

If jSymbolic is run without any command line arguments, then the GUI
will be run. Alternatively, jSymbolic may be run directly from the
command line to extract features from a single MIDI file using the
following three command line arguments:

<SourceMIDIPath> <FeatureValuesOutputPath> <FeatureDescriptionsOutputPath>


-- ADDING FEATURES --

1) Implement a class for the new feature in the jsymbolic/features
   directory. It must extend the MIDIFeatureExtractor abstract class.
2) Add a reference to the new class to the getAllAvailableFeatureExtractors 
   method in the FeatureSelectorPanel class.


-- LICENSING AND LIABILITY -- 

jSymbolic 1.2.1
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

jSymbolic 1.2.1:
- Added basic command line functionality to complement the GUI. Note
that this involved updating the FeatureSelectionPanel class with a
new public getAllAvailableFeatureExtractors method to provide access to
the available features, as well as updating the private 
populateFeatureExtractors method to now use this new method to set up
features for the GUI internally. New features that were previously added
using the populateFeatureExtractors method should now be added with the
getAllAvailableFeatureExtractors method.
- Includes updated support libraries (ACE and UtilityClasses).

jSymbolic 1.2:
- Includes updated support libraries (ACE and UtilityClasses).

jSymbolic 1.1:
- Imported into the NetBeans framework to assist further development.