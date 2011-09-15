==========================================================================
 jMusicMetaManager 1.2.1
 by Cory McKay
 Copyright (C) 2010 (GNU GPL)
==========================================================================


-- OVERVIEW -- 

jMusicMetaManager is an open source software project for
automatically detecting metadata errors in MP3 music
collections, as well as profiling and organizing
collections. In particular, jMusicMetaManager places an
emphasis on detecting differing field values that should
in fact be the same (e.g. different spellings of “Lynyrd
Skynyrd”) and on detecting redundant duplicates of the
same recording. jMusicManager can produce a total of 39
different reports providing various kinds of information
about music collections.

The jMusicMetaManager software can extract metadata (e.g.
title, artist, genre, etc.) from Apple iTunes XML files
or directly from the ID3 tags of MP3 files. This data can
then be processed and organized in user specifiable ways,
with the results presented in HTML formatted reports. A
special emphasis is placed on the Title, Artist, Composer,
Album and Genre fields.

jMusicMetaManager has been developed as part of the jMIR
music classification research software suite,
and may be used either as part of this suite or
independently. The quality, consistency and reliability of
ground truth plays an essential role in the ultimate
efficacy of any learned model, and jMusicMetaManager was
developed to help improve the metadata that is used to
train and test learning algorithms for music information
retrieval (MIR) projects. As such, jMusicMetaManager is
capable of exporting parsed metadata to both Weka ARFF and
ACE XML files.

The jMusicMetaManager software is in no way limited to MIR
projects, and can be a useful tool for users ranging from
individuals wishing to organize their personal digital music
collections to employees of libraries or other large
institutions wishing to profile their music databases.

More information on jMIR is available at 
http://jmir.sourceforge.net. Please contact Cory McKay 
at cory.mckay@mail.mcgill.ca with any bug reports or
questions relating to either jMusicMetaManager or jMIR
in general. 


-- MANUAL -- 

A file entitled "Manual.html" is extracted upon 
installation. This file provides many more details on the
jMusicMetaManager software than this Readme document.


-- COMPATIBILITY --

Compatibility Issues The jMusicMetaManager software is
written in Java, which means that it can theoretically be run
on any system that has the Java Runtime Environment (JRE)
installed on it. It is recommended that this software be used
with Windows XP, however, as it was developed and tested under
the Windows XP operating system, and the interface’s appearance
is optimized for Windows. Although the software should
theoretically run under earlier versions of Windows, OS X,
Linux, Solaris or any other operating system with the JRE
installed on it, users should be advised that the software has
not yet been tested on other platforms, so difficulties may be
encountered.

This software was developed with version 1.6.0_16 of the JDK (Java
Development Kit), so it is suggested that the corresponding 
version or higher of the JRE be installed on the user's computer.


-- INSTALLING THE JAVA RUNTIME ENVIRONMENT --

If your system already has the JRE installed, you may skip this 
section. If not, you will need to install it in order to run
jMusicMetaManager. The JRE can be downloaded for free from the
java.sun.com web site. The JDK includes the JRE.

When the download is complete, follow the installation
instructions that come with it. 


-- INSTALLING jMusicMetaManager --

The jMusicMetaManager software is delivered in a zipped file,
from which jMusicMetaManager can be extracted using any of a
variety of dearchiving utilities (e.g. WinZip).

There are two versions of jMusicMetaManager, namely the
development version and the user version. The user version 
contains everything needed to run jMusicMetaManager, but does
not include any source code. The developer version does 
include source code.

The user version unzips into a single directory. Installation
simply involves copying this directory into any desired
location.

The developer version presents jMusicMetaManager in the form 
of a NetBeans project. Three directories are contained in the 
zipped distribution file:

	- jMusicMetaManager: Contains the jMusicMetaManager 
		source and bytecode files, as well as other 
		NetBeans project documents and general
		documentation.
	- ThirdPartyJars: Contains the third party software
		used by jMusicMetaManager. Further information
		is available in the licenses section of the
		manual.
	- UtilityClasses: General classes used by
		jMusicMetaManager.


-- RUNNING THE SOFTWARE -- 

A file named "jMusicMetaManager.jar" is produced upon
installation. The simplest way to start jMusicMetaManager is
to double click on this file.

The software may also be accessed via the command line (e.g. 
the DOS prompt). To access DOS under Windows, for example, go
to the Start Menu, select Run and type "cmd". Then use the 
"cd" command to move to the directory that contains the
jMusicMetaManager.jar file. In that directory, type:

	java -jar jMusicMetaManager.jar

However, it should be noted that the JRE does not always 
allocate sufficient memory for jMusicMetaManager to process
large music collections. Running jMusicMetaManager using
either of the above two methods could therefore result in an
out of memory error.

It is therefore preferable to manually allocate a greater 
amount of memory. 500 to 700 MB should be more than enough 
for most situations, but one should be careful to leave at
least 300MB unallocated under Windows XP. If you have 512 MB 
of RAM, you should run the jMusicMetaManager software as
follows at the command prompt:

	java -ms16M -mx200M -jar jMusicMetaManager.jar

If you have 1 GB or more of RAM, you should run
jMusicMetaManager as follows:

	java -ms16M -mx700M -jar jMusicMetaManager.jar


-- LICENSING -- 

jMusicMetaManager 1.2.1
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

A copy of the GNU General Public License is available via
the HTML manual.

The jMusicMetaManager software makes use of two third-party
products. The de.vdheide.mp3 package is used to parse the ID3
tags of MP3 files. This software is also available under the
GNU GPL. This product also includes software developed by the
Apache Software Foundation (http://www.apache.org/). This is
the Xerces library, which is used to parse XML files. The
Xerces license is available via the manual as well. 


-- UPDATES SINCE VERSION 1.0 -- 

jMusicMetaManager 1.2.1
- Update to iTunes XML parsing functionality, including the
ability to access web parsing functionality through a proxy
server.
- Updated UtilityClasses support libraries.

jMusicMetaManager 1.2
- Includes updated support libraries (UtilityClasses and
Xerces 2.9.1).

jMusicMetaManager 1.01
- The effect of the album filter was reversed, so that
recordings will be reported as potential duplicates only if 
they are on different albums when this filter is applied.
- Tutorial, Hints & Suggestions, Problems & Solutions,
Codaich and Version Update History sections were added
to the manual, as well as the Codaich and OMEN
ISMIR papers and various other small additions.
- Verification was implemented to ensure that the user
saves report files with a .html extension.
- File streams were closed if an error occurred, thereby
preventing some operating systems from marking
report folders as busy when errors occurred.
- A bug causing the + sign to be improperly parsed from
iTunes files was corrected.
- The clarity of error messages relating to the need
to access the internet was improved.
- The About dialog box was updated to reflect the GPL
licensing.
- The name of the Information menu was corrected.
- All directory paths in HTML reports use a forward
slash instead of the system default.