==========================================================================
 UtilityClasses 3.0.1
 by Cory McKay
 Copyright (C) 2010 (GNU GPL)
==========================================================================


-- OVERVIEW -- 

UtilityClasses is an open source library of multi-purpose files that
implement commonly used functionality. As a library, it does not have any
executable components.

UtilityClasses was developed as part of the jMIR music classification
research software suite, and may be used either as part of this suite or
independently. More information on jMIR is available at
http://jmir.sourceforge.net.

Please contact Cory McKay (cory.mckay@mail.mcgill.ca) with any bug reports
or questions relating to UtilityClasses. 


-- COMPATIBILITY --

The UtilityClasses software is written in Java, which means that it can
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
UtilityClasses. The JRE can be downloaded for free from the java.sun.com
web site. The JDK includes the JRE.

When the download is complete, follow the installation instructions 
that come with it in order to install it. 


-- LICENSING AND LIABILITY -- 

UtilityClasses 3.0.1
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

The UtilityClasses software makes use of the Yahoo! SDK to submit
queries to the Yahoo! engine. This SDK comes with a BSD license.
Queries submitted to Yahoo's Web Services must comply with Yahoo!'s
terms of service, available at http://docs.yahoo.com/info/terms. 


-- UPDATES SINCE VERSION 1.0 -- 

UtilityClasses 3.0.1:
- mckay.utilities.webservices.ProxyServerAccessor.ProxyServerAccessor
	- A new class for testing for and configuring settings needing to
	access the web using a proxy server
- mckay.utilities.staticlibraries.StringMethods
	- Added the new passwordBasedDecrypt and passwordBasedEncryptin 
	methods for encrypting and decrypting strings
- mckay.utilities.staticlibraries.MiscellaneousMethods
	- A new class for performing miscellaneous taks. Includes a static
	method for running as a subprocess a command in a specified 
	environment and for collecting any resulting output. Also includes
	a method for parsing command line arguments.
- mckay.utilities.staticlibraries.NetworkMethods
	- A new class for performing network-related tasks. Includes static
	methods for making HTTP GET and POST requests to servers using Java.

UtilityClasses 3.0:
- Various bug fixes
- Various new functionality