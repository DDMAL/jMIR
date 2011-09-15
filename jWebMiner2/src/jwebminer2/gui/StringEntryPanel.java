/*
 * StringEntryPanel.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import mckay.utilities.gui.templates.ListInputPanel;
import mckay.utilities.gui.templates.ListInputParser;


/**
 * A JPanel that stores an editable non-wrapping scrollable JTextArea. This
 * text area can be "organized" automatically in various ways, including
 * removing blank lines, removing duplicate entries and alphabetically sorting
 * the contents. This text area is intended to provide an interface so that
 * users can enter lists where each line corresponds to a different item.
 *
 * <p>The <i>Load</i> button brings up a file chooser to load strings from a
 * file and adds them to the text area. They are not cleaned or processed upon
 * loading, they are simply dumped. They also do not replace any text that
 * is already there, they are rather appended to the text. This is currently
 * greyed out, as this option is not yet available
 *
 * <p>The <i>Save</i> button for each text area will bring up a file chooser
 * allowing the user to enter the location of a file. The contents of the text
 * area will be saved as a text file to the chosen location.
 *
 * <p>The <i>Clear</i> button erases all of the contents of the text area.
 *
 * <p>The <i>Organize</i> button causes the contents of the text area to be
 * sorted, with empty lines and duplicate lines removed.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class StringEntryPanel
     extends JPanel
{
     /* FIELDS ****************************************************************/


     /**
      * A text area that the user can use to enter words. Each line corresponds
      * to a different entry in a list. Buttons are incldued to load text from
      * files, delete the contents of the text area or organize the text area.
      */
     private   ListInputPanel   list_enterer;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates a new instance of StringEntryPanel
      *
      * @param file_parser    An object that can bring up a file chooser and
      *                       parse selected files. May be null if the Load
      *                       button on this panel is to be greyed out.
      */
     public StringEntryPanel(ListInputParser file_parser)
     {
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;

          // Initialize the list enterer
          list_enterer = new ListInputPanel(file_parser);

          // Prepare the layout
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          list_enterer.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, vertical_gap, horizontal_gap));
          add(list_enterer);
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Takes the contents of the text area and breaks them into an array with
      * an entry for each line in the text area. Blank lines are removed,
      * duplicate entries are removed and the remaining entries are sorted
      * alphabetically.
      *
      * @return     The processed contents of the text area. Null is returned if
      *             the text area is empty.
      */
     public String[] getOrganizedStrings()
     {
          return list_enterer.getProcessedStrings();
     }


     /**
      * Place the GUI focus on the text area.
      */
     public void focusOnTextInput()
     {
          list_enterer.focusOnTextInput();
     }
}