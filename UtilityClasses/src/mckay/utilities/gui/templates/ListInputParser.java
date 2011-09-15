/*
 * ListInputParser.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.templates;

/**
 * An interface for use by the ListInputParser class in choosing and parsing
 * files. Each implementing class must be able to display a file chooser and
 * parse the selected file(s).
 *
 * @author Cory McKay
 */
public interface ListInputParser
{
     /**
      * Open a file browser and load strings from the selected file. The parsed
      * items should comprise a list. An array of strings should then be created
      * with one entry for each item in the list. It is not necessary to sort
      * or otherwise organize this array, but no entries may be null. Null may
      * be returned if the load is cancelled. A descriptive exception should be
      * thrown if a problem occurs.
      *
      * @return               The parsed contents of the file.
      * @throws     Exception An informative description of any problem that
      *                       occurs during parsing.
      */
     public String[] getStrings() throws Exception;
     
     /**
      * Stores a progress bar that can be manipulated to display parsing 
      * progress. This is not necessary for all parsers, as a ListInputPanel
      * will generate and display a progress bar in any case, but it can be
      * preferable in some parsing situations to hand the progress bar directly
      * to the parser.
      *
      * @param progress_bar    The progress bar.
      */
     public void setProgressBar(mckay.utilities.gui.progressbars.SimpleProgressBarDialog progress_bar);
}
