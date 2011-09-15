/*
 * ListInputTextParser.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.templates;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import mckay.utilities.general.FileFilterImplementation;


/**
 * An implementation of the ListInputParser interface for use by the
 * ListInputPanel class in choosing and parsing of text files. Displays a file
 * chooser and parses the selected file(s).
 *
 * @author Cory McKay
 */
public class ListInputTextParser
     implements ListInputParser
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The GUI component opening the file chooser.
      */
     protected Component                parent;
     
     /**
      * A file chooser used to choose files.
      */
     protected JFileChooser             file_chooser;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of ListInputTextParser.
      *
      * @param parent    The GUI component opening the file chooser.
      */
     public ListInputTextParser(Component parent)
     {
          // Store the parameters
          this.parent = parent;
          
          // Prepare the file_chooser to only display text files
          String[] accepted_extensions = {"txt"};
          file_chooser = new JFileChooser();
          file_chooser.setFileFilter(new FileFilterImplementation(accepted_extensions));
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Opens a file browser and load strings from the selected textfile. The
      * parsed file is considered to comprise a list. Each line is counted as a
      * separate item in the list. Blank lines are treated as an item in the
      * list consisting of "". An array of strings is returned with one entry
      * for each item in the list. This array is not sorted or otherwise
      * processed, but no entries may be null. Null is returned if the load is
      * cancelled. A descriptive exception is thrown if a problem occurs during
      * paring.
      *
      * @return               The parsed contents of the file.
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
               File chosen_file = file_chooser.getSelectedFile();
               String[] parsed_contents = mckay.utilities.staticlibraries.FileMethods.parseTextFileLines(chosen_file);
               return parsed_contents;
          }
          
          // Return null if user cancels load cancelled
          return null;
     }
     
     
     /**
      * Does not do anything for this class, as the progress bar is not
      * manipulated directly.
      *
      * @param progress_bar    A progress bar.
      */
     public void setProgressBar(mckay.utilities.gui.progressbars.SimpleProgressBarDialog progress_bar)
     {}
}
