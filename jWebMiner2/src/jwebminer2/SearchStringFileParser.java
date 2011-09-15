/*
 * SearchStringFileParser.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import weka.core.*;
import weka.core.converters.ArffLoader;
import ace.datatypes.SegmentedClassification;
import jMusicMetaManager.RecordingMetaData;
import mckay.utilities.general.FileFilterImplementation;
import mckay.utilities.gui.progressbars.SimpleProgressBarDialog;
import jwebminer2.gui.MetaDataChooserDialog;
import jwebminer2.gui.OuterFrame;


/**
 * An implementation of the ListInputParser interface for use by the
 * ListInputPanel class in choosing and parsing of files. Displays a file
 * chooser and parses the selected file(s).
 *
 * <p>May parse class names from Weka ARFF files or ACE XML Classifications
 * Files, as well as from text files (strings delimited by line breaks). iTunes
 * XML files may also be parsed, and a particular field chosen as the basis for
 * input.
 *
 * <p>Duplicates are removed from iTunes, ACE XML and text files.
 *
 * <p>Note that the file type is determined based on its extension, so the file
 * must have a correct extension (.xml, .arff, .arf or .txt).
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class SearchStringFileParser
     implements mckay.utilities.gui.templates.ListInputParser
{
     /* FIELDS ****************************************************************/


     /**
      * The OuterFrame opening the file chooser.
      */
     private OuterFrame               parent;

     /**
      * A file chooser used to choose files.
      */
     private JFileChooser             file_chooser;

     /**
      * A progress bar displaying parsing progress.
      */
     private SimpleProgressBarDialog  progress_bar;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates a new instance of ListInputTextParser.
      *
      * @param parent    The OuterFrame opening the file chooser.
      */
     public SearchStringFileParser(OuterFrame parent)
     {
          // Store the parameters
          this.parent = parent;

          // Prepare the file_chooser to only display text files
          String[] accepted_extensions = {"txt", "arff", "arf", "xml"};
          file_chooser = new JFileChooser();
          file_chooser.setFileFilter(new FileFilterImplementation(accepted_extensions));
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Opens a file browser and load strings from the selected textfile. The
      * parsed file is considered to comprise a list.
      *
      * An array of strings is returned with one entry for each item in the
      * list. This array is not sorted or otherwise processed, aside from the
      * removal of duplicates in iTunes, ACE XML and text files, but no entries
      * may be null. Null is returned if the load is cancelled or if the file
      * does not contain any pertinent information. A descriptive exception is
      * thrown if a problem occurs during paring.
      *
      * <p>Note that the file type is determined based on its extension, so the
      * file must have a correct extension (.xml, .arff or .txt).
      *
      * @return               The parsed contents of the file. Null is returned
      *                       if the parsed file does not return any relevant
      *                       information or if Cancel is pressed by the user.
      * @throws     Exception An informative description of any problem that
      *                       occurs during parsing.
      */
     public String[] getStrings()
     throws Exception
     {
          // Show the file chooser
          int browse_return = file_chooser.showOpenDialog(parent);

          // Parse the file if the user chooses a file
          if (browse_return == JFileChooser.APPROVE_OPTION)
          {
               // Get the file to parse
               File chosen_file = file_chooser.getSelectedFile();

               // Get the case-independent extension of the file to parse
               String extension = mckay.utilities.staticlibraries.StringMethods.getExtension(chosen_file.getName());
               extension = extension.toLowerCase();

               // Parse the appropriate type of file
               if (extension.equals(".txt"))
               {
                    return parseTextFile(chosen_file);
               }
               else if (extension.equals(".arff") || extension.equals(".arf"))
               {
                    // Access the ARFF file
                    ArffLoader arff_loader = new ArffLoader();
                    arff_loader.setFile(chosen_file);

                    // Access the class names
                    Instances data_set = arff_loader.getDataSet();
                    data_set.setClassIndex(data_set.numAttributes() - 1);
                    Attribute class_attribute = data_set.classAttribute();
                    java.util.Enumeration class_enumeration = class_attribute.enumerateValues();

                    // Return null if there are no class names
                    if (!class_enumeration.hasMoreElements())
                         return null;

                    // Store the class name
                    LinkedList<String> class_list = new LinkedList<String>();
                    while (class_enumeration.hasMoreElements())
                         class_list.add((String) class_enumeration.nextElement());

                    // Return the class names
                    return class_list.toArray(new String[1]);
               }
               else if (extension.equals(".xml"))
               {
                    // Try parsing as an iTunes XML file
                    try
                    {
                         // Bring the progress bar back to the front
                         progress_bar.setVisible(true);

                         // Parse the file
                         RecordingMetaData[] itunes_data = RecordingMetaData.extractMetaDataFromiTunesXML(chosen_file, false);
                         if (itunes_data == null) return null;
                         String results[] = new String[itunes_data.length];

                         // Determine what to parse
                         parent.itunes_field_chooser.activateDialog(this);
                         int selection = parent.itunes_field_chooser.getSelection();

                         // Bring the progress bar back to the front
                         progress_bar.setVisible(true);

                         // Extract the chosen field. Null is returned if
                         // cancel is chosen.
                         if (selection == MetaDataChooserDialog.CANCEL_CHOSEN)
                              return null;
                         else if (selection == MetaDataChooserDialog.TITLE_CHOSEN)
                         {
                              for (int i = 0; i < results.length; i++)
                                   results[i] = itunes_data[i].title;
                         }
                         else if (selection == MetaDataChooserDialog.ARTIST_CHOSEN)
                         {
                              for (int i = 0; i < results.length; i++)
                                   results[i] = itunes_data[i].artist;
                         }
                         else if (selection == MetaDataChooserDialog.COMPOSER_CHOSEN)
                         {
                              for (int i = 0; i < results.length; i++)
                                   results[i] = itunes_data[i].composer;
                         }
                         else if (selection == MetaDataChooserDialog.ALBUM_CHOSEN)
                         {
                              for (int i = 0; i < results.length; i++)
                                   results[i] = itunes_data[i].album;
                         }
                         else if (selection == MetaDataChooserDialog.GENRE_CHOSEN)
                         {
                              LinkedList<String> results_list = new LinkedList<String>();
                              for (int i = 0; i < itunes_data.length; i++)
                              {
                                   if (itunes_data[i] != null)
                                        for (int j = 0; j < itunes_data[i].genres.length; j++)
                                             results_list.add(itunes_data[i].genres[j]);
                              }
                              results = results_list.toArray(new String[1]);
                         }
                         else return null;

                         // Return the parsed file contents with doubles
                         // duplicate strings removed
                         return mckay.utilities.staticlibraries.StringMethods.removeDoubles(results);
                    }
                    catch (Exception e)
                    {}

                    // Try parsing as an ACE XML file
                    try
                    {
                         SegmentedClassification[] classifications = SegmentedClassification.parseClassificationsFile(chosen_file.getAbsolutePath());
                         return SegmentedClassification.getLeafClasses(classifications);
                    }
                    catch (Exception e)
                    {}

                    // Throw an exceptino if the XML file is not valid or is
                    // of an unrecognized type.
                    throw new Exception(chosen_file.getName() + " is not a valid XML file of a recognized type.\nIt must be either an iTunes XML file or an ACE XML Classifications file.\n\n" );
               }
               else
               {
                    throw new Exception("Unrecognized extension (" + extension + ")\n\nFiles must have an extension of .txt, .arff or .xml to parse.\n\n");
               }
          }

          // Return null if user cancels load cancelled
          return null;
     }


     /**
      * Stores a reference to a progress bar that is being used to measure
      * parsing progress.
      *
      * @param progress_bar    The progress bar.
      */
     public void setProgressBar(SimpleProgressBarDialog progress_bar)
     {
          this.progress_bar = progress_bar;
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Parses the given file. The parsed file is considered to comprise a list.
      * Each line is counted as a separate item in the list. Blank lines are
      * treated as an item in the list consisting of "". An array of strings is
      * returned with one entry for each item in the list. This array is not
      * sorted or otherwise processed, aside from the fact that duplicates are
      * removed, but no entries may be null. A descriptive  exception is thrown
      * if a problem occurs during paring.
      *
      * @return               The parsed contents of the file.
      * @throws     Exception An informative description of any problem that
      *                       occurs during parsing.
      */
     private String[] parseTextFile(File file_to_parse)
     throws Exception
     {
          String[] results = mckay.utilities.staticlibraries.FileMethods.parseTextFileLines(file_to_parse);
          return mckay.utilities.staticlibraries.StringMethods.removeDoubles(results);
     }
}