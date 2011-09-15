==========================================================================
 jAudio 2.0.0 pre-release
 by Cory McKay and Daniel McEnnis
==========================================================================


-- OVERVIEW -- 

jAudio 2.0.0 is an experimental expansion built on jAudio 0.4.5. jAudio 0.4.5
is still the official release, and users are advised to only use jAudio 2
with the understanding that it is very much a work in progress.

jAudio 0.4.5 is available from:

http://jaudio.sourceforge.net
http://sourceforge.net/projects/jaudio/


-- SUMMARY OF CHANGES IN JAUDIO 2.0 -- 

- jAudio has been ported into NetBeans project in order to facilitate
modifications. OCVolume has also been ported directly into the jAudio code.
- jAudio may now extract features from Ogg Vorbis audio files, in addition
to the formats previously supported.
- Project linking has been updated so that it is no longer necessary to place
any files in the JRE ext folder.
- An installer is no longer needed or provided.


-- KNOWN ISSUES --

- It is necessary to manually set the <pluginFolder> field of the 
features.xml file to point to the folder holding new features. No change
is necessary if only the default features are being used.
- The on-line help in the GUI has been temporarily disabled. 


-- NEW THIRD PARTY SOFTWARE --

In addition to the third-party software already used by jAudio 0.4.5,
jAudio 2 now uses more Tritonus software (www.tritonus.org), as well as
JCraft JOrbis (www.jcraft.com/jorbis). Both Tritornus and JObis are
distributed under the GNU GPL.