/*
 * ResultsDisplayPanel.java
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
import javax.swing.event.*;
import mckay.utilities.general.FileSaver;


/**
 * A JPanel for displaying and saving data. It consists of a JEditorPane for
 * viewing the results, a Save button for saving it and a JComboBox for choosing
 * the file format to save the data in. HTML may be used to fill the 
 * JEditorPane, and it will follow links clicked on.
 *
 * <p>Actual saving is performed using the FileSaver object passsed to the
 * constructor. The basic FileSaver class may be extended to allow more
 * flexible saving.
 *
 * @author Cory McKay
 */
public class ResultsDisplayPanel
     extends JPanel
     implements ActionListener, HyperlinkListener     
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * Pane to dislplay results
      */
     private JEditorPane     display_pane;
     
     /**
      * A button that will cause the contents of the display_pane to be saved
      * using a Save As dialog box in the format selected in the
      * save_format_combo_box.
      */
     private JButton         save_button;
     
     /**
      * Allows the user to choose the format to save the contents of the
      * display_pane when the save_button is pressed.
      */
     private JComboBox       save_format_combo_box;
     
     /**
      * A file chooser allowing the user to choose the location so save the
      * contents of the text area.
      */
     private JFileChooser    save_chooser;
     
     /**
      * Performs the actual saving of files.
      */
     private FileSaver       saver;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of ResultsDisplayPanel with initialized
      * components.
      *
      * @param   saver   Object for performing actual saving.
      */
     public ResultsDisplayPanel(FileSaver saver)
     {
          // Call superclass constructor
          super(new BorderLayout(4, 4));
          
          // Store the saver
          this.saver = saver;
          
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;
          
          // Prepare the results display
          display_pane = new JEditorPane();
          display_pane.setContentType("text/html");
          display_pane.setEditable(false);
          display_pane.addHyperlinkListener(this);
          JScrollPane scroll_pane = new JScrollPane(display_pane);
          
          // Prepare the save_panel
          save_button = new JButton("Save");
          save_button.addActionListener(this);
          save_format_combo_box = new JComboBox(saver.getFileFormatExtension());
          JPanel combo_panel = getLabeledContainer("SAVE FORMAT:", save_format_combo_box, horizontal_gap);
          JPanel save_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          save_panel.add(save_button, BorderLayout.WEST);
          save_panel.add(new JLabel(""));
          save_panel.add(combo_panel, BorderLayout.EAST);
          
          // Add contents to this JPanel
          add(save_panel, BorderLayout.NORTH);
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
          // React to the save_button
          if (event.getSource().equals(save_button))
               saveText();
     }
     
     
     /**
      * Cause the cursor to change when it passes over a hyperlink. Also load
      * any links clicked on.
      *
      * @param event     The HyperlinkEvent that occured.
      */
     public void hyperlinkUpdate( HyperlinkEvent event )
     {        
          if (event.getEventType() == HyperlinkEvent.EventType.ENTERED)
               this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          else if (event.getEventType() == HyperlinkEvent.EventType.EXITED)
               this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
          {
               try {display_pane.setPage(event.getURL());}
               catch (Exception f) {display_pane.setText("Unable to find requested page:\n" + event.getURL());}
          }
     }
     
     
     /**
      * Erases any text already in the display_pane and replaces it by the
      * contents of new_text. The caret is set to the beginning of the text.
      *
      * @param new_text  The new text to add. Nothing is done if this is null.
      */
     public void setText(String new_text)
     {
          if (new_text != null)
          {
               display_pane.setText(new_text);
               display_pane.setCaretPosition(0);
          }
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Bring up a file chooser allowing the user to choose a location to save
      * the contents shown in the display_pane as a file in the format selected
      * in the save_format_combo_box. Saves it to this file. Displays an error
      * message if a problem occurs or if the file already exists.
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
                         saver.saveContents((String) save_format_combo_box.getSelectedItem(), display_pane.getText(), selected_file);
                    }
                    catch (Exception e)
                    {
                         JOptionPane.showMessageDialog(null, e.getMessage() + "\n\nUnable to write file.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
          }
     }
     
     
     /**
      * Returns a BorderLayout JPanel with the given label on the left and the
      * given Container on the right.
      *
      * @param label_text     The label to give the container.
      * @param container      The Container to put on the right of the JPanel.
      * @param gap            The gap in pixels between the label and the
      *                       container.
      * @return               The formatted JPanel.
      */
     private static JPanel getLabeledContainer(String label_text,
          Container container,
          int gap)
     {
          JPanel panel = new JPanel(new BorderLayout(gap, gap));
          panel.add(new JLabel(label_text), BorderLayout.WEST);
          panel.add(container, BorderLayout.CENTER);
          return panel;
     }
}