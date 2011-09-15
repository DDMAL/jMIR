/*
 * ListInputPanel.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.templates;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import mckay.utilities.gui.progressbars.SimpleProgressBarDialog;


/**
 * A JPanel that stores an editable non-wrapping scrollable JTextArea. Includes
 * methods for processing this text area in various ways, including
 * removing blank lines, removing duplicate entries and alphabetically sorting
 * the contents. This text area is intended to provide an interface so that
 * users can enter lists where each line corresponds to a different item.
 *
 * <p>The <i>Load</i> button brings up a file chooser to load strings from a
 * file and adds them to the text area. They are not cleaned or processed upon
 * loading, they are simply dumped. They also do not replace any text that
 * is already there, they are rather appended to the text. The parsing is
 * performed by the object passed to the constructor of this object.
 *
 * <p>The <i>Save</i> button will bring up a file chooser allowing the user to
 * enter the location of a file. The contents of the text area will be saved as
 * a text file to the chosen location.
 *
 * <p>The <i>Clear</i> button erases all of the contents of the text area.
 *
 * <p>The <i>Organize</i> button causes the contents of the text area to be
 * sorted, with empty lines and duplicate lines removed.
 *
 * <p>The get methods can be used to get the contents of the text area without
 * modifying the text area in any way. The set and append methods can be used to
 * change the contents of the text area.
 *
 * @author Cory McKay
 */
public class ListInputPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * An object that can bring up a file chooser, parse selected files and
      * append the extracted contents to this object's text area.
      */
     private ListInputParser  file_parser;
     
     /**
      * The text area stored in this object.
      */
     private JTextArea        text_area;
     
     /**
      * A button that will bring up a file chooser to load strings from a file
      * and add them to the text area. They are not cleaned or processed upon
      * loading, they are simply dumped. They also do not replace any text that
      * is already there, they are rather appended to the text.
      */
     private   JButton        load_button;
     
     /**
      * A button that will cause the unprocessed contents of the text area
      * to be saved as a text file to the location of the user's choosing.
      */
     private   JButton        save_button;
     
     /**
      * A button that erases the contents of the text area.
      */
     private   JButton        clear_button;
     
     /**
      * A button that alphabetizes the contents of the primary text area and
      * also removes blank lines and removes duplicate lines.
      */
     private   JButton        organize_button;
     
     /**
      * A file chooser allowing the user to choose the location so save the
      * contents of the text area.
      */
     private   JFileChooser   save_chooser;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of ListInputPanel with initialized components.
      *
      * @param file_parser    An object that can bring up a file chooser, parse
      *                       selected files and append the extracted contents
      *                       to this object's text area. The Load button is
      *                       disabled if this is null.
      */
     public ListInputPanel(ListInputParser file_parser)
     {
          // Call superclass constructor
          super(new BorderLayout(4, 4));
          
          // Store the file parser
          this.file_parser = file_parser;
          
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;
          
          // Set up the button panel and attach action listeners to the buttons
          JPanel button_panel = new JPanel(new GridLayout(1, 4));
          load_button = new JButton("Load");
          save_button = new JButton("Save");
          clear_button = new JButton("Clear");
          organize_button = new JButton("Organize");
          button_panel.add(load_button);
          button_panel.add(save_button);
          button_panel.add(clear_button);
          button_panel.add(organize_button);
          save_button.addActionListener(this);
          load_button.addActionListener(this);
          clear_button.addActionListener(this);
          organize_button.addActionListener(this);
          
          // Disable the load button if appropriate
          if (file_parser == null) load_button.setEnabled(false);
          
          // Prepare text_area
          text_area = new JTextArea();
          text_area.setLineWrap(false);
          text_area.setEditable(true);
          text_area.setText("");
          
          // Make scrollable
          JScrollPane scroll_pane = new JScrollPane(text_area);
          
          // Add contents to this JPanel
          add(button_panel, BorderLayout.NORTH);
          add(scroll_pane, BorderLayout.CENTER);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Calls the appropriate methods when the specified actions are performed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the load_button
          if (event.getSource().equals(load_button))
               loadStrings();
          
          // React to the save_button
          else if (event.getSource().equals(save_button))
               saveText();
          
          // React to the clear_button
          else if (event.getSource().equals(clear_button))
               clearText();
          
          // React to the organize_button
          else if (event.getSource().equals(organize_button))
               organizeText();
     }
     
     
     /**
      * Place the GUI focus on the text area.
      */
     public void focusOnTextInput()
     {
          text_area.requestFocusInWindow();
     }
     
     
     /**
      * Delete all text in the text area.
      */
     public void clearText()
     {
          text_area.setText("");
     }
     
     
     /**
      * Erases any text already in the text area and replaces it by the contents
      * of new_text. The caret is set to the beginning of the text.
      *
      * @param new_text  The new text to add. Nothing is done if this is null.
      */
     public void setText(String new_text)
     {
          if (new_text != null)
          {
               text_area.setText(new_text);
               text_area.setCaretPosition(0);
          }
     }
     
     
     /**
      * Clears the text area and adds each string in the new_strings parameter
      * as a new line in the text area. The caret is set to the beginning of the
      * text.
      *
      * @param new_strings    The strings to add. Each entry is added to a new
      *                       line. This method performs no action other than
      *                       clearing the text area if this parameter is empty
      *                       or null.
      */
     public void setStrings(String[] new_strings)
     {
          clearText();
          appendStrings(new_strings);
     }
     
     
     /**
      * Appends the contents of new_text to the text area without any
      * modifications. The caret is set to the beginning of the text.
      *
      * @param new_text  The new text to add. Nothing is done if this is null.
      */
     public void appendText(String new_text)
     {
          if (new_text != null)
          {
               text_area.append(new_text);
               text_area.setCaretPosition(0);
          }
     }
     
     
     /**
      * Appends each string in the new_strings parameter as a new line in the
      * text area. Sets the caret is set to the beginning of the text.
      *
      * @param new_strings    The strings to add. Each entry is added to a new
      *                       line. This method performs no action if this
      *                       parameter is empty or null.
      */
     public void appendStrings(String[] new_strings)
     {
          if (new_strings != null)
               if (new_strings.length != 0)
               {
               // Append the text
               for (int i = 0; i < new_strings.length; i++)
               {
                    if (i == 0)
                    {
                         if (!text_area.getText().equals(""))
                              text_area.append("\n");
                    }
                    else text_area.append("\n");
                    
                    text_area.append(new_strings[i]);
               }
               
               // Reset the caret position
               text_area.setCaretPosition(0);
               }
     }
     
     
     /**
      * Returns a dump of the contents of the text area.
      *
      * @return     The contents of the text area.
      */
     public String getText()
     {
          return text_area.getText();
     }
     
     
     /**
      * Takes the contents of the text area and breaks them into an array with
      * an entry for each line in the text area. Blank lines are removed but
      * no other processing is performed.
      *
      * @return               The lines found in text area. Blank lines are
      *                       removed. Null is returned if the text area is
      *                       empty.
      */
     public String[] getStrings()
     {
          String field_contents = text_area.getText();
          if (field_contents == null) return null;
          if (field_contents.equals("")) return null;
          
          String[] search_strings = mckay.utilities.staticlibraries.StringMethods.breakIntoTokens(field_contents, "\n");
          if (search_strings.length == 0) return null;
          
          return search_strings;
     }
     
     
     /**
      * Takes the contents of the text area and breaks them into an array with an
      * entry for each line in the text area. Blank lines are removed, duplicate
      * entries are removed and the remaining lines are sorted alphabetically.
      *
      * @return               The processed contents of the text area. Null is
      *                       returned if text_area is empty.
      */
     public String[] getProcessedStrings()
     {
          // Get the contents of text_area with blank lines removed
          String[] uncleaned_strings = getStrings();
          
          if (uncleaned_strings != null)
          {
               // Remove duplicate strings
               String[] duplicate_removed_strings = mckay.utilities.staticlibraries.StringMethods.removeDoubles(uncleaned_strings);
               
               // Alphabetically sort the strings
               String[] sorted_strings = mckay.utilities.staticlibraries.SortingMethods.sortArray(duplicate_removed_strings);
               
               // Return the results
               return sorted_strings;
          }
          else return null;
     }
     
     
     /**
      * Cleans the contents of the text area by removing blank lines and
      * duplicate lines and then sorts the contents. The text areas's contents
      * are then replaced with these cleaned contents.
      */
     public void organizeText()
     {
          // Get and clean the search strings
          String[] cleaned_contents = getProcessedStrings();
          
          // Replace the previous contents of text_area with a cleaned version
          if (cleaned_contents != null)
               setStrings(cleaned_contents);
     }
     
     
     /**
      * As per the JPanle superclass setEnabled method. Also calls the
      * setEnabled method of the text area, load button, clear button and
      * organize button on this panel.
      *
      * @param enable    Whether to enable or disable this panel and its
      *                  components.
      */
     public void setEnabled(boolean enable)
     {
          super.setEnabled(enable);
          
          text_area.setEnabled(enable);
          load_button.setEnabled(enable);
          save_button.setEnabled(enable);
          clear_button.setEnabled(enable);
          organize_button.setEnabled(enable);
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Open a file browser and load strings from the selected file into the
      * text area. These loaded strings are added to the text that is already
      * there, they do not replace it. No change is made if the load is
      * cancelled and an error message is displayed if a problem occurs.
      *
      * <p>A progress bar is displayed to show parsing progress.
      */
     private void loadStrings()
     {
          String[] strings_to_add = null;
          SimpleProgressBarDialog progress_bar = new SimpleProgressBarDialog(2, this);
          file_parser.setProgressBar(progress_bar);
          try
          {strings_to_add = file_parser.getStrings();}
          catch (Exception e)
          {
               progress_bar.done();
               JOptionPane.showMessageDialog(null, e.getMessage(), " ERROR", JOptionPane.ERROR_MESSAGE);
          }
          progress_bar.incrementStatus();
          if (strings_to_add != null) appendStrings(strings_to_add);
          progress_bar.done();
     }
     
     
     /**
      * Bring up a file chooser allowing the user to choose a location to save
      * the unchanged contents of the text area as a text file. Saves it to
      * this file. Displays an error message if a problem occurs or if the
      * file already exists.
      */
     private void saveText()
     {
          if (save_chooser == null)
               save_chooser = new JFileChooser();
          int browse_return = save_chooser.showSaveDialog(this);
          if (browse_return == JFileChooser.APPROVE_OPTION)
          {
               String selected_path = save_chooser.getSelectedFile().getAbsolutePath();
               File selected_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(selected_path, false);
               if (selected_file != null)
               {
                    try
                    {
                         System.out.println("a");
                         DataOutputStream writer = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(selected_file);
                         System.out.println("b");
                         writer.writeBytes(getText());
                         System.out.println("c");
                    }
                    catch (Exception e)
                    {JOptionPane.showMessageDialog(null, "Unable to write file.", "ERROR", JOptionPane.ERROR_MESSAGE);}
               }
          }
     }
}