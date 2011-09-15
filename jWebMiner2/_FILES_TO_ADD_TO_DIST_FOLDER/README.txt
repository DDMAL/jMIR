==========================================================================
 jWebMiner 2.0.1
 by Cory McKay (jWebMiner 1.x) and Gabriel Vigliensoni (jWebMiner 2.x)
 Copyright (C) 2010 (GNU GPL)
==========================================================================


-- OVERVIEW -- 

jWebMiner is an open source software system for automatically extracting
cultural features from the web for use in classifying and/or measuring
similarity between text terms. It is designed with the particular needs
of music research in mind, but may easily be used in many other areas of
research.

At its most basic level, the software operates by using web services to
extract hit counts from search engines and to extract information related
to Last.FM social tags. Functionality is also available for calculating a
variety of statistical features based on search engine counts, for
variably weighting web sites or limiting searches only to particular sites,
for excluding hits that do or do not contain particular filter terms, for
defining synonym relationships between search strings, and for applying a 
number of additional search configurations. A particular emphasis has also
been placed on extensibility so that new functionality can be easily added.

jWebMiner can parse terms from delimited text files, from class names 
found in Weka ARFF or ACE XML files, or from a variety of different fields
of iTunes XML files. They can also be entered manually by the user into
the jWebMiner GUI.

Detailed results and configuration records can be saved in HTML reports.
Final feature values can also be exported to ACE XML, Weka ARFF or 
newline delimited text files.

jWebMiner was developed as part of the jMIR music classification research
software suite, and may be used either as part of this suite or
independently. More information on jMIR is available at
http://jmir.sourceforge.net.

Please contact Gabriel Vigliensoni (gabriel@music.mcgill.ca) or Cory
McKay (cory.mckay@mail.mcgill.ca) with any bug reports or questions
relating to the software. 


-- MANUAL -- 

A file entitled "Manual.html" is extracted upon installation. This 
file and the HTML files that it links to provide many more details on
the software than this README document.

IMPORTANT NOTE: This manual was written for jWebMiner 1, which did not
include jWebMiner 2's Last.FM functionality. The differences between
the interfaces of the two versions of jWebMiner are relatively minimal,
and there are plans to update the manual soon.


-- COMPATIBILITY --

The jWebMiner software is written in Java, which means that it can
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
section. If not, you will need to install it in order to run jWebMiner.
The JRE can be downloaded for free from the java.sun.com web site. The
JDK includes the JRE.

When the download is complete, follow the installation instructions 
that come with it in order to install it. 


-- INSTALLING jWebMiner --

The jWebMiner software is delivered in a zipped file, from which 
jWebMiner can be extracted using any of a variety of dearchiving 
utilities (e.g. WinZip).

There are two versions of jWebMiner, namely the development version
and the user version. The user version contains everything needed to
run jWebMiner, but does not include any source code. The developer 
version does include source code.

The user version unzips into a single directory. Installation simply 
involves extracting this directory to any desired disk location. A 
file entitled "Manual.html" is among the files extracted, and it can
be used to view instructions on using the software.

The developer version presents jWebMiner in the form of a NetBeans 
project. Five directories are contained in the zipped distribution
file:

	- jWebMiner2: Contains the jWebMiner source and bytecode 
	files, as well as other NetBeans project documents and 
	general documentation.
	- Third_Party_Jars: Contains the third party software used by
	jWebMiner. Further information is available in the licenses
	section of the manual.
	- UtilityClasses: General jMIR classes, including source code,
	used by jWebMiner.
	- ACE: The jMIR ACE NetBeans project, including source code. 
	jWebMiner uses some classes from this software.
	- jMusicMetaManager: The jMIR jMusicMetaManager NetBeans 
	project, including source code. jWebMiner uses some classes 
	from this software.


-- RUNNING THE SOFTWARE -- 

A file named "jWebMiner2.jar" is produced upon installation. The
simplest way to start jWebMiner is to simply double click on this 
file.

The software may also be accessed via the command line (e.g. the DOS
prompt). To access DOS under Windows, for example, go to the Start 
Menu, select Run and type "cmd". Then use the "cd" command to move to
the directory that contains the jWebMiner2.jar file. In that directory, 
type:

	java -jar jWebMiner2.jar

It should be noted that the JRE does not always allocate sufficient
memory for jWebMiner to process large music collections. Running 
jWebMiner using either of the above two methods could therefore result
in an out of memory error (although this is relatively rare).

It is therefore sometimes preferable to manually allocate a greater
amount of memory to jWebMiner before running it. 250 MB should be more
than enough for most situations. This can be done by entering the 
following at the command prompt:

	java -ms16M -mx250M -jar jWebMiner2.jar

Note that your computer must be connected to the internet for jWebMiner
to work. It may also be necessary to modify your firewall to allow it 
access.


-- LICENSING AND LIABILITY -- 

jWebMiner 2.0
Copyright (C) 2010 Cory McKay and Gabriel Vigliensoni

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

A copy of the GNU General Public License is available via the HTML
manual.

The jWebMiner software makes use of four included third-party products.
The University of Waikato Weka data mining package is used to parse and
save Weka ARFF files. This software is also available under the GNU GPL,
and more information on it is available at 
http://www.cs.waikato.ac.nz/ml/weka. jWebMiner also includes software
developed by the Apache Software Foundation (http://www.apache.org),
namely the Xerces library, which is used to parse XML files. The Xerces
license can be accessed via the manual.

The Yahoo! SDK is used to submit queries to the Yahoo! engine. This SDK 
comes with a BSD license, which may be accessed via the jWebMiner manual. 
Queries submitted to Yahoo's Web Services must comply with Yahoo!'s terms 
of service, available at http://docs.yahoo.com/info/terms. 

Similarly, a license key is needed to query the LastFM engine. You can test
jWebMiner with the sample license key for online testing provided, but
the user should provide another one (available at 
http://www.last.fm/api/account). Queries submitted to the LastFM's web
services must comply with their terms and conditions, available at
http://www.last.fm/api/tos


-- UPDATES SINCE VERSION 1.0 -- 

jWebMiner 2.0.1:
- Added functionality to enable web access via a proxy server
- Fixed minor inconsistency related to package names
- Fixed a bug related to parsing ACE XML and iTunes XML files.
- Updated UtilityClasses, ACE and jMusicMetaManager support libraries.

jWebMiner 2.0:
- Last.FM cross-tabulation feature extraction now supported, and results may
be combined with existing Yahoo! features
- Updated OS X compatibility (tested in version 10.5.)
- Google searches disabled due to Google's deprecation of SOAP web services
- Includes updated support libraries (ACE, UtilityClasses and Xerces 2.9.1).
