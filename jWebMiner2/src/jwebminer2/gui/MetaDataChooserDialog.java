/*
 * MetaDataChooserDialog.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jwebminer2.SearchStringFileParser;


/**
 * A dialog box allowing the user to choose the type of metadata to extract
 * from a file (recording title, artist name, composer name, album title or
 * genre). Cancel may also be selected.
 *
 * <p>The activateDialog method displays this dialog box and then waits for a
 * user response. Pressing the OK or Cancel butotn returns control, and the
 * getSelection method can then be called to access the user choice.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class MetaDataChooserDialog
     extends JDialog
     implements ActionListener
{
     /* FIELDS ****************************************************************/

     /**
      * Identifier of a user choice of cancel.
      */
     public static final int  CANCEL_CHOSEN = -1;

     /**
      * Identifier of a user choice of cancel.
      */
     public static final int  TITLE_CHOSEN = 1;

     /**
      * Identifier of a user choice of cancel.
      */
     public static final int  ARTIST_CHOSEN = 2;

     /**
      * Identifier of a user choice of cancel.
      */
     public static final int  COMPOSER_CHOSEN = 3;

     /**
      * Identifier of a user choice of cancel.
      */
     public static final int  ALBUM_CHOSEN = 4;

     /**
      * Identifier of a user choice of cancel.
      */
     public static final int  GENRE_CHOSEN = 5;

     /**
      * Stores the selection made by the user. Will be one of the static final
      * methods of this class.
      */
     private   int                      selection_made;

     /**
      * The parser object requesting the user to make a choice with this dialog
      * box.
      */
     private   SearchStringFileParser   parser;

     /**
      * The radio button corresponding to recording titles.
      */
     private   JRadioButton   title_radio_button;

     /**
      * The radio button corresponding to artist names.
      */
     private   JRadioButton   artist_radio_button;

     /**
      * The radio button corresponding to composer names.
      */
     private   JRadioButton   composer_radio_button;

     /**
      * The radio button corresponding to album titles.
      */
     private   JRadioButton   album_radio_button;

     /**
      * The radio button corresponding to genre names.
      */
     private   JRadioButton   genre_radio_button;

     /**
      * The button cancelling the operation.
      */
     private   JButton        cancel_button;

     /**
      * The button confirming the choice.
      */
     private   JButton        ok_button;

     /**
      * The parent GUI component.
      */
     private   JFrame         parent;

     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates a new instance of MetaDataChooserDialog and lay it out. It is
      * not displayed, however.
      *
      * @param parent    The GUI object instantiating this dialog box.
      */
     public MetaDataChooserDialog(JFrame parent)
     {
          // Give the dialog box its owner, its title and make it modal
          super(parent, "Field to extract", true);

          // Store the parent GUI component
          this.parent = parent;

          // Initialize parser and the selection
          parser = null;
          selection_made = CANCEL_CHOSEN;

          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;

          // Configure the radio buttons
          JPanel radio_button_panel = new JPanel(new GridLayout(5, 1, horizontal_gap, vertical_gap));
          ButtonGroup radio_button_group = new ButtonGroup();
          title_radio_button = new JRadioButton("Recording title");
          artist_radio_button = new JRadioButton("Artist name");
          composer_radio_button = new JRadioButton("Composer name");
          album_radio_button = new JRadioButton("Album title");
          genre_radio_button = new JRadioButton("Genre");
          radio_button_group.add(title_radio_button);
          radio_button_group.add(artist_radio_button);
          radio_button_group.add(composer_radio_button);
          radio_button_group.add(album_radio_button);
          radio_button_group.add(genre_radio_button);
          radio_button_panel.add(title_radio_button);
          radio_button_panel.add(artist_radio_button);
          radio_button_panel.add(composer_radio_button);
          radio_button_panel.add(album_radio_button);
          radio_button_panel.add(genre_radio_button);

          // Configure the OK and Cancel buttons
          JPanel button_panel = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          ok_button = new JButton("OK");
          cancel_button = new JButton("Cancel");
          ok_button.addActionListener(this);
          cancel_button.addActionListener(this);
          button_panel.add(ok_button);
          button_panel.add(cancel_button);

          // Add borders
          radio_button_panel.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, vertical_gap/2, horizontal_gap));
          button_panel.setBorder(BorderFactory.createEmptyBorder(vertical_gap/2, horizontal_gap, vertical_gap, horizontal_gap));

          // Set up the layout
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          add(radio_button_panel, BorderLayout.CENTER);
          add(button_panel, BorderLayout.SOUTH);

          // Display
          pack();
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the OK button
          if (event.getSource().equals(ok_button))
               storeChoice(false);

          // React to the Cancel button
          if (event.getSource().equals(cancel_button))
               storeChoice(true);
     }


     /**
      * Display this dialog box and wait for the user response.
      *
      * @param parser    The parser that needs the choice the user will make
      *                  with this dialog box.
      */
     public void activateDialog(SearchStringFileParser parser)
     {
          // Set the selectino and store the parser
          selection_made = 0;
          this.parser = parser;

          // Center relative to parent and make visible
          setLocationRelativeTo(parent);
          setVisible(true);
     }


     /**
      * Returns the selection made by the user on this dialog box, whether it
      * be one of the radio buttons if the OK button was pressed, or the
      * cancel button otherwise.
      *
      * <p>This method should be called externally after a selection made on
      * this dialog box by the user, with either the OK or cancel button
      * pressed.
      *
      * @return The static final field of this class indicating the seleciton
      *         made.
      */
     public int getSelection()
     {
          return selection_made;
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Stores the user choice in the selection_made field. CANCEL_CHOSEN is
      * stored if cancel_chosen is true. Otherwise stores the static final field
      * that corresponds to the radio button selected.
      *
      * <p>Hides the dialog box and returns control to the method that called
      * activateDialog.
      *
      * @param cancel_chosen  True if the cancel button resulted in this method
      *                       being called.
      */
     private void storeChoice(boolean cancel_chosen)
     {
          setVisible(false);

          if (cancel_chosen)
               selection_made = CANCEL_CHOSEN;
          else
          {
               if (title_radio_button.isSelected())
                    selection_made = TITLE_CHOSEN;
               else if (title_radio_button.isSelected())
                    selection_made = TITLE_CHOSEN;
               else if (artist_radio_button.isSelected())
                    selection_made = ARTIST_CHOSEN;
               else if (composer_radio_button.isSelected())
                    selection_made = COMPOSER_CHOSEN;
               else if (album_radio_button.isSelected())
                    selection_made = ALBUM_CHOSEN;
               else if (genre_radio_button.isSelected())
                    selection_made = GENRE_CHOSEN;
          }
     }
}