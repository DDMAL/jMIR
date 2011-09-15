/*
 * SearchWordsPanel.java
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


/**
 * A GUI panel allowing users to select the search strings to use as a basis
 * for extracting cultural features from the web.
 *
 * <p>There are two basic types of automated web searches that can be performed.
 * The first, a <i>cooccurrence extraction</i>, means that the relative internet
 * cooccurrence of each word in the PRIMARY SEARCH STRINGS text area is
 * measured when compared with every other word in the same text area. The
 * SECONDARY SEARCH STRINGS text area is ignored in this kind of analysis.
 *
 * <p>The second type of search, a <i>cross tabulation extraction</i>, involves
 * calculating the relative internet cooccurrence of each word int the
 * PRIMARY SEARCH STRINGS text area with every word in the SECONDARY SEARCH
 * STRINGS text area.
 *
 * <p>The two radio buttons at the top of this panel allow the user to select
 * which one of these two analyses they wish to perform. Selecting the
 * Cooccurrence Extraction radio button will temporarily disable the SECONDARY
 * SEARCH  STRINGS text area, as it is irrelevant for this type of analysis.
 *
 * <p>The PRIMARY SEARCH STRINGS text area on the left and the SECONDARY SEARCH
 * STRINGS text area on the right allow users to enter the search terms that
 * they wish to use. These may be entered by directly typing or editing the
 * text areas or by copying and pasting existing text into them. Search terms
 * may also be added using the <i>Load</i> button corresponding to the
 * appropriate text area, which parses strings from an existing text file and
 * appends them to the the appropriate text area. Note that each search string
 * may contain multiple words and that each line in a text area corresponds to a
 * separate search string.
 *
 * <p>A line that contains words or sets of words followed by " <SYNONYM> "
 * tag(s) (without the quotes) can be used to specify that each string on the
 * left and right of the synonym tag(s) are to be treated as synonyms and
 * results for all synonyms in an entry are to be combined during scoring
 * caluculations.
 *
 * <p>The <i>Save</i> button for each text area will bring up a file chooser
 * allowing the user to enter the location of a file. The contents of the text
 * area will be saved as a text file to the chosen location.
 *
 * <p>The <i>Clear</i> buttons for each text area erase all of the contents of
 * the corresponding text area.
 *
 * <p>The <i>Organize</i> buttons for each text area cause the contents of the
 * corresponding text area to be sorted, with empty lines and duplicate lines
 * removed.
 *
 * <p>The isCrossTabulation method allows other classes to determine which
 * radio button is selected. The getOrganizedPrimarySearchStrings and
 * getOrganizedSecondarySearchStrings methods allow other classes to access
 * the organized (sorted and redundancies and blank lines removed) contents
 * of each of the text areas.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class SearchWordsPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     /**
      * The radio button indicating that a coocurrence search is to be performed
      * involving only one set of search strings
      */
     private   JRadioButton   cooccurrence_button;

     /**
      * The radio button indicating that a cross tabulation search is to be
      * performed involving two sets of search string.
      */
     private   JRadioButton   cross_tabulation_button;

     /**
      * The set of search strings that will be used in order to measure
      * cooccurrence in cooccurrence searches, or that will comprise the
      * rows in cross tabulation searches.
      */
     private   ListInputPanel primary_text_area;

     /**
      * The set of search strings that will comprise the columns in cross
      * tabulation searches. Ignored in simple coocurrence searches.
      */
     private   ListInputPanel secondary_text_area;

     /**
      * A label for the primary search strings panel.
      */
     private   JLabel         primary_search_strings_label;

     /**
      * A label for the secondeary search strings panel.
      */
     private   JLabel         secondary_search_strings_label;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates and displays a new instance of PreferencesPanel.
      *
      * @param parent    The GUI object holding this object.
      */
     public SearchWordsPanel(OuterFrame parent)
     {
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;

          // Set up the radio_button_panel and attach action listeners to
          // corresponding radio buttons
          JPanel radio_button_panel = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          ButtonGroup radio_button_group = new ButtonGroup();
          cooccurrence_button = new JRadioButton("Co-Occurrence Extraction");
          cross_tabulation_button = new JRadioButton("Cross Tabulation Extraction");
          radio_button_group.add(cooccurrence_button);
          radio_button_group.add(cross_tabulation_button);
          radio_button_panel.add(cooccurrence_button);
          radio_button_panel.add(cross_tabulation_button);
          cooccurrence_button.addActionListener(this);
          cross_tabulation_button.addActionListener(this);

          // Set up the left text areas
          primary_text_area = new ListInputPanel(new jwebminer2.SearchStringFileParser(parent));
          secondary_text_area = new ListInputPanel(new jwebminer2.SearchStringFileParser(parent));

          // Set up the left panel
          JPanel left_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          primary_search_strings_label = new JLabel("PRIMARY SEARCH STRINGS:");
          left_panel.add(primary_search_strings_label, BorderLayout.NORTH);
          left_panel.add(primary_text_area, BorderLayout.CENTER);

          // Set up the right panel
          JPanel right_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          secondary_search_strings_label = new JLabel("SECONDARY SEARCH STRINGS:");
          right_panel.add(secondary_search_strings_label, BorderLayout.NORTH);
          right_panel.add(secondary_text_area, BorderLayout.CENTER);

          // Set up the text areas panel
          JPanel text_areas_panel = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          text_areas_panel.add(left_panel);
          text_areas_panel.add(right_panel);

          // Add borders to the inner layouts
          JPanel radio_button_panel_outer = new JPanel();
          radio_button_panel_outer.add(radio_button_panel);
          radio_button_panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          radio_button_panel_outer.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, 0, horizontal_gap));
          left_panel.setBorder(BorderFactory.createEmptyBorder(0, horizontal_gap, vertical_gap, horizontal_gap/2));
          right_panel.setBorder(BorderFactory.createEmptyBorder(0, horizontal_gap/2, vertical_gap, horizontal_gap));

          // Prepare outer layouts
          JPanel outer_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          outer_panel.add(radio_button_panel_outer, BorderLayout.NORTH);
          outer_panel.add(text_areas_panel, BorderLayout.CENTER);
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          add(outer_panel);

          // Initialize settings
          setCrossTabulationMode(true);
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the specified actions are performed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the cooccurrence_button
          if (event.getSource().equals(cooccurrence_button))
               setCrossTabulationMode(false);

          // React to the cross_tabulation_button
          else if (event.getSource().equals(cross_tabulation_button))
               setCrossTabulationMode(true);
     }


     /**
      * Place the GUI focus on the primary text area.
      */
     public void focusOnTextInput()
     {
          primary_text_area.focusOnTextInput();
     }


     /**
      * Returns whether a cross tabulation analysis or a cooccurrence analysis
      * is to be performed.
      *
      * @return     True if a cross tabulation analysis is to be performed and
      *             false if cooccurrence analysis is to be performed.
      */
     public boolean isCrossTabulation()
     {
          return cross_tabulation_button.isSelected();
     }


     /**
      * Returns the cleaned contents of the Primary Search Strings text area.
      * These are cleaned in the sense that blank lines are removed, duplicate
      * lines are removed and the remaining contents are sorted.
      *
      * @return     The cleaned contents of the Primary Search Strings text
      *             area. Each entry in the returned array corresponds to a
      *             different line in the text area. Null is returned if the
      *             text area is empty.  An entry that contains words or sets
      *             of words followed by " <SYNONYM> " tag(s) (without the
      *             quotes) specifies that each string on the left and right of
      *             the synonym tag(s) are to be treated as synonyms and results
      *             for all synonyms in an entry are to be combined during
      *             scoring caluculations.
      */
     public String[] getOrganizedPrimarySearchStrings()
     {
          return primary_text_area.getProcessedStrings();
     }


     /**
      * Returns the cleaned contents of the Secondary Search Strings text area.
      * These are cleaned in the sense that blank lines are removed, duplicate
      * lines are removed and the remaining contents are sorted.
      *
      * @return     The cleaned contents of the Secondary Search Strings text
      *             area. Each entry in the returned array corresponds to a
      *             different line in the text area. Null is returned if the
      *             text area is empty.  An entry that contains words or sets
      *             of words followed by " <SYNONYM> " tag(s) (without the
      *             quotes) specifies that each string on the left and right of
      *             the synonym tag(s) are to be treated as synonyms and results
      *             for all synonyms in an entry are to be combined during
      *             scoring caluculations.
      */
     public String[] getOrganizedSecondarySearchStrings()
     {
          return secondary_text_area.getProcessedStrings();
     }


     /**
      * Sets whether or not this component and its contents are enabled. A
      * component that is enabled may respond to user input, while a component
      * that is not enabled cannot respond to user input.
      *
      * @param enabled   Whether or not to enable this window and its contents.
      */
     public void setEnabled(boolean enabled)
     {
          super.setEnabled(enabled);
          cooccurrence_button.setEnabled(enabled);
          cross_tabulation_button.setEnabled(enabled);
          primary_text_area.setEnabled(enabled);
          secondary_text_area.setEnabled(enabled);
          primary_search_strings_label.setEnabled(enabled);
          secondary_search_strings_label.setEnabled(enabled);
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Enable or disable the secondary_text_area and related buttons and set
      * either the cooccurrence_button or the cross_tabulation_button to
      * selected.
      *
      * @param set  True sets the cross_tabulation_button and activates the
      *             secondary_text_area and related buttons. False sets the
      *             cooccurrence_button and deactivates the secondary_text_area.
      */
     private void setCrossTabulationMode(boolean set)
     {
          secondary_text_area.setEnabled(set);
          secondary_search_strings_label.setEnabled(set);

          if (set) cross_tabulation_button.setSelected(true);
          else cooccurrence_button.setSelected(true);
     }
}