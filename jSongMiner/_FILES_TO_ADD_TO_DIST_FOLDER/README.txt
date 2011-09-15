==========================================================================
 jSongMiner 1.0
 by Cory McKay and David Bainbridge
 Copyright (C) 2010 (GNU GPL)
==========================================================================


-- OVERVIEW -- 

jSongMiner is a software package for auto-identifying songs and extracting metadata about them from various sources on the web and elsewhere. This software was originally designed for use in the context of digital libraries, but it can certainly be adopted for other purposes as well. For example, it could be used as a means of obtaining cultural features for automatic music classification, or even for annotating personal music collections. For those users who do wish to use jSongMiner in the context of a digital library, it has been designed specifically to be integrated with the Greenstone digital library software (http://www.greenstone.org), and Greenstone modules have been implemented for using jSongMiner to build Greenstone collections.

jSongMiner begins by identifying unknown audio files using fingerprinting. Alternatively, it can also identify songs using metadata queries, either using metadata that is embedded in an audio file or using known metadata about a song. Once jSongMiner has identified a song, it can then extract metadata about the song from various sources, such as from The Echo Nest and Last.FM web services, or from miscellaneous metadata embedded in the audio file. In addition to extracting metadata about songs, jSongMiner can also extract metadata about artists and albums associated with songs as distinct resources.

Once metadata has been extracted relating to a song, artist or album, this metadata can be saved as an ACE XML 1.1 Classifications file or as a return-delimited text file, or it can simply be printed to standard out. Each piece of metadata extracted by jSongMiner includes the metadata field label, the metadata value and the source from which the metadata was derived. Users can also opt to have the extracted metadata presented using unqualified or qualified Dublin Core tags, if desired.

In all, jSongMiner can extract over 100 song, artist and album fields. Many of these fields can have multiple values (e.g. there may be multiple songs similar to a given song).

jSongMiner is part of the jMIR project, and as such can be easily integrated with other jMIR applications. However, it is also designed to be used entirely independently if desired.

Like the rest of jMIR, jSongMiner is open-source and available for free. It is implemented in Java in order to maximize cross-platform utilization. The one exception to this is its use of the Echo Nest fingerprinting codegen binary, but the Echo Nest provides versions of this binary for use with Windows, OS X and Linux. In any case, jSongMiner can certainly be used without the local fingerprinting functionality offered by the Echo Nest codegen if necessary.  

Since jSongMiner is designed primarily for use with digital libraries and automatic music classification software, its primary users are expected to have a certain basic level of computer proficiency. The majority of the interface design effort has been put into providing a powerful and flexible command line and configuration file interface, and a GUI has not been implemented yet. Also, some basic setting of configurations must be done upon installation, due to lisencing constraints imposed by some of the technologies used by jSongMiner. Having noted this, every effort has been made to make jSongMiner as easy to use as possible, with ample documentation in the manual, so even users with only moderate computer backgrounds should still have little difficulty using the software. In addition to including a command line interface that makes the jSongMiner easy to run from other software, jSongMiner also includes a well-documented API in order to facilitate the use of jSongMiner as a library incorporated into other software.

Please contact Cory McKay (cory.mckay@mail.mcgill.ca) with any bug reports or questions relating to the software. 


-- MANUAL -- 

A file entitled "Manual.html" is extracted upon installation. This file and the HTML files that it links to provide many more details on the software than this README document.


-- COMPATIBILITY --

The jSongMiner software is written in Java, which means that it can theoretically be run on any system that has the Java Runtime Environment (JRE) installed on it. It is particularly recommended that this software be used with Windows XP or CentOS Linux, however, as it was developed and tested under these operating systems. Although the software should likely run perfectly well on other versions of Windows, OS X, other Linux distributions, Solaris or any other operating system with the JRE installed on it, users should be advised that the software has not yet been fully tested on other platforms, so difficulties may be encountered.

This software was developed with version 1.6.0 of the JDK (Java Development Kit), so it is suggested that the corresponding version or higher of the JRE be installed on the user's computer.


-- INSTALLING THE JAVA RUNTIME ENVIRONMENT --

If your system already has the JRE installed, as will most typically be the case, you may skip this section. If not, you will need to install the JRE in order to run jSongMiner. The JRE can be downloaded for free from the java.sun.com web site. The JDK includes the JRE.

When the JRE download is complete, follow the installation instructions that come with it in order to install it. 


-- INSTALLING jSongMiner --

The jSongMiner software is available at http://jmir.sourceforge.net. It is delivered in a zipped file, from which jSongMiner can be extracted using any of a variety of dearchiving utilities (e.g. ZipGenius).

There are two versions of jSongMiner, namely the development version and the user version. The user version contains everything needed to run jSongMiner, but does not include any source code. The developer version does include source code.

The user version unzips into a single directory. Installation simply involves extracting this directory to any desired disk location. See the section of the manual on the command line for information on running the program. A file entitled Manual.html is among the files extracted, and it provides useful information about the software.

The developer version presents jSongMiner in the form of a NetBeans project. Five directories are contained in the zipped distribution file:

* jSongMiner: Contains the jSongMiner source and bytecode files, as well as other NetBeans project documents and general documentation.

* ThirdPartyJars: Contains the distributable third party software used by jSongMiner. Further information is available in the licenses section of the manual.

* UtilityClasses: General jMIR classes used by jSongMiner, including source code.

* ACE: The jMIR ACE NetBeans project, including source code. jSongMiner uses some classes from this software.

Both the user and developer versions of jSongMiner make use of several configuration and log files, some of which are generated at runtime, and some of which are provided with the distribution. These are described in the installation section of the manual.

Also, regardless of whether the user or developer version of jSongMiner is used, the licensing agreements of some of the third-party resources accessed by jSongMiner require that additional free components be downloaded or that free personal usage keys be acquired from them before they may be used with jSongMiner. The installation section of the manual describe how all such software and usage keys can be acquired, and how jSongMiner can be configured to use them once they are obtained. Although it is certainly possible to use jSongMiner without these components, it is recommended that the effort be made to acquire them, as they do substantially increase the information made available by jSongMiner.


-- RUNNING THE SOFTWARE -- 

A file named "jSongMiner.jar" is produced upon installation. jSongMiner is used by running this file using command line arguments specifying what kind of processing is to be performed.

All of the command line arguments accepted by jSongMiner consist of flag/value pairs, where the flag comes first and is preceded with a "-". The one exception to this is that a lone flag of "-help" will print a list of valid flags with explanations of what they are. Invalid command line arguments will also result in the printing out of a list of valid flags with explanations of what they are.

Most of the jSongMiner command line flags are used to choose the piece of music which is to be identified and labeled with metadata, and to specify how this information is to be output. In addition, a few of the flags provide access to jSongMiner's utility functions, which is to say that they allow the jSongMiner software to be used to perform basic functions distinct from its central function of identifying songs and extracting metadata associated with them. These utility flags may not be used in combination with any other flags, as they provide functionality that is neither directly related to jSongMiner's core functionality, nor to the functionality offered by other utility flags.

The following is a list of the flags that may be used to access jSongMiner's functionality:

-audio: The path of an audio file to identify and extract metadata for, if available.

-title: The suspected title of the song to extract metadata for, if known.

-artist: The suspected name of the artist associated with the song to extract metadata for, if known.

-album: The suspected title of the album most associated with the song to extract metadata for, if known.

-savedir: The directory to save extracted metadata in. This overrides any and all output directories specified in the configuration file, and applies to both text and ACE XML output. Only a directory should be specified, as file names will be auto-generated based on content. May not be used in conjunction with any of the -savefile, -saveacexmlfile or -savetxtfile flags.

-savefile: The file path to save extracted metadata to. This overrides configuration settings relating to output directories, and sets the package_song_artist_album configuration setting to true so that song, artist and album metadata are combined into one file. If the value specified here has a .xml extension, then the output will be saved as ACE XML. If it has a .txt extension, then the output will be saved as text. May not be used in conjunction with any of the -savedir, -saveacexmlfile or -savetxtfile flags.

-saveacexmlfile: The file path of the ACE XML file to save extracted metadata to. This overrides configuration settings relating to output directories, sets the save_output_as_ace_xml configuration setting to true, and sets the package_song_artist_album setting to true so that song, artist and album metadata are combined into one file. This flag may be used either alone or in conjunction with the -savetxtfile flag.

-savetxtfile: The file path of the text file to save extracted metadata to. This overrides configuration settings relating to output directories, sets the save_output_as_txt configuration setting to true, and sets the package_song_artist_album setting to true so that song, artist and album metadata are combined into one file. This flag may be used either alone or in conjunction with the -saveacexmlfile flag.

-eraselogs: Erases the logs of previously extracted artists and/or albums, if any. This does not alter the reextract_known_artist_metadata or reextract_known_album_metadata configuration settings, but it does erase any existing logs before the new processing begins. The value following this flag must be "artist", "album" or "all", and specifies which log(s) are erased.

-urldecode: URL decodes the text following this flag and prints the results to standard out. Uses UTF-8 decoding. This is a terminal utility function, so no other processing is performed other than this, and this flag cannot be used in combination with any other flags.

-acexmltotxt: Converts the ACE XML 1.1 Classifications file whose path is specified after this flag into the type of text produced by jSongMiner, which is to say field names are printed to odd lines and field values are printed to the following even lines. This is output to standard out. URL decoding is not performed. This is a terminal utility function, so no other processing is performed other than this, and this flag cannot be used in combination with any other flags.

-acexmltotxtud: Converts the ACE XML 1.1 Classifications file whose path is specified after this flag into the type of text produced by jSongMiner, which is to say field names are printed to odd lines and field values are printed to the following even lines. This is output to standard out. URL decoding is performed (UTF-8). This is a terminal utility function, so no other processing is performed other than this, and this flag cannot be used in combination with any other flags.

-help: Will print to standard out a list of valid flags with explanations of what they are.

A few usage examples are shown below demonstrating how jSongMiner can be run. Many more examples are available in the section of the manual on command line arguments. Note that, as is standard practice, flag values containing spaces are enclosed in quotation marks. Note also that the "-mx500M" Java Virtual Machine option is also used in these examples to reserve 500 MB of memory for the virtual machine to operate. Although this is not strictly necessary for jSongMiner processing, it is a good idea in general to ensure that the VM does not run out of memory.

Example 1) Identify and extract metadata for the audio file unknown.mp3 found in the current directory, and save the results in a way specified in the configuration settings file:

	java -mx500M -jar jSongMiner.jar -audio ./unknown.mp3

Example 2) Extract metadata for the audio file unknown.mp3 found in the current directory, with the assumption that it is the song "Wandering Star" by Portishead from the album "Dummy". This approach should only be used if it can be assumed that this information comes from a reliable source, otherwise the approach shown in Example 1 should be used:

	java -mx500M -jar jSongMiner.jar -audio ./unknown.mp3 -title "Wandering Star" -artist Portishead -album Dummy

Example 3) Extract metadata for the song "Freedom" by Charles Mingus from the album "Mingus Mingus Mingus Mingus Mingus", even though access to an audio recording is unavailable:

	java -mx500M -jar jSongMiner.jar -title Freedom -artist "Charles Mingus" -album "Mingus Mingus Mingus Mingus Mingus"

Example 4) Identify and extract metadata for the audio file unknown.mp3, and save the song, artist and/or album metadata in a single ACE XML file called unknown_out.xml in the current directory. Note that the output file would be in text format if a .txt extension were specified instead of a .xml extension:

	java -mx500M -jar jSongMiner.jar -audio ./unknown.mp3 -savefile ./unknown_out.xml


-- LICENSING AND LIABILITY -- 

jSongMiner 1.0
Copyright (C) 2010
Cory McKay

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

jSongMiner also makes use of several third-party Java libraries, web services and binaries. Details are available in the manual, and licenses for each of these is packaged with the jSongMiner distribution.