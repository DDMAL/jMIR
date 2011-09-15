/*
 * Main.java
 * Version 1.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package jMusicMetaManager;


/**
 * Runs the jMusicMetaManager software. This software is designed to clean up
 * inconsistencies and redundancies in the metadata associated with MP3 files
 * as well as to profile music collections. This metadata can be extracted from
 * an iTunes XML file and/or from the ID3 tags of MP3 files themselves. Reports
 * are generated and saved in HTML format.
 *
 * <p>Only the GUI version of this software is currently available. A command
 * line version is planned for the future.
 *
 * <p>Analysis preferences are stored in an AnalysisPreferences object,
 * metadata is parsed from sources and stored by RecordingMetaData objects and
 * actual processing is performed by an AnalysisProcessor object.
 *
 * @author Cory McKay
 */
public class Main
{
     /**
      * Instantiate the GUI. Command line arguments are ignored.
      *
      * @param args The command line arguments
      */
     public static void main(String[] args)
     {
          try
          {
               jMusicMetaManager.gui.OuterFrame gui = new jMusicMetaManager.gui.OuterFrame();
          }
          catch (Exception e)
          {
               System.out.println("ERROR: " + e.getMessage());
               e.printStackTrace();
          }
     }
}
