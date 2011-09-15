/*
 * OuterFrame.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import mckay.utilities.gui.progressbars.*;
import mckay.utilities.gui.templates.*;
import mckay.utilities.webservices.NetworkSearchDialog;
import jwebminer2.AnalysisProcessor;


/**
 * The outer component of the jWebMiner GUI that is used to hold tabbed
 * panes and menus.
 *
 * <p>The Search Words tab allows the user to set the key words to base feature
 * extraction on. This may involve either cooccurrence or cross tabulation.
 * Synonyms may also be specified using in-text tags.
 *
 * <p>The Required Filter Words tab allows the user to set words that must be
 * found on a site in order for it to be counted as a hit. Pattern-based phrases
 * may also by set using in-text tags
 *
 * <p>The Excluded Filter Words tab allows the suer to set words that may not
 * be found on a site if it is to be counted as a hit.
 *
 * <p>The Site Weightings tab allows the user to set the sites to extract features
 * from and to vary the weights assigned to each using tags.
 *
 * <p>The Options tab allows the user to set feature extraction preferences.
 *
 * <p>The Results tab displays the results of the last feature extraction.
 *
 * <p>The EXTRACT FEATURES button at the bottom of the panel begins feature
 * extraction using the settings specified in all tabs. A progress bar will
 * appear showing the progress of extraction and giving the user an opportunity
 * to cancel processing. An error will be reported to the user if a problem
 * occurs. Note that each query will be submitted to a given web services up
 * to 3 times if it is unsuccesful before it is reported to the user, as
 * sometimes the service is simply busy when at the moment when a particular
 * query is submitted. The user then has the option to either continue trying
 * or cancel the feature extraction.
 *
 * <p>The Perform Test Search menu item brings up a Network Search dialog box
 * that can be used to perform test searches with one or two different web
 * services. Parameters of the search are defaulted in this dialog box to the
 * settings selected in the GUI panels. If a search dialog is already open then
 * it is replaced with a new one with these parameters.
 *
 * <p>The Help menu item opens a window displaying the interactive window. The
 * About menu item displays version and ownership information.
 *
 * <p>The AnalysisProcessor object stored in the analysis_processor field
 * performs the actual web-based feature extractions and stores the results.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class OuterFrame
     extends JFrame
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     /**
      * Contains the tabbed panels.
      */
     private   JTabbedPane         tabbed_pane;

     /**
      * Allows the user to set the key words to base feature extraction on. This
      * may involve either cooccurrence  or cross tabulation.
      */
     private   SearchWordsPanel    search_words_panel;

     /**
      * Allows the user to set words that must be found on a site in order for
      * it to be counted as a hit.
      */
     private   StringEntryPanel    required_filter_words_panel;

     /**
      * Allows the user to set words that may not be present on a site in order
      * for it to be counted as a hit.
      */
     private   StringEntryPanel    excluded_filter_words_panel;

     /**
      * Allows the user to set the sites to extract features from and to vary
      * the weights assigned to each.
      */
     private   StringEntryPanel    site_weightings_panel;

     /**
      * Allows the user to set feature extraction preferences.
      */
     private   OptionsPanel        options_panel;

     /**
      * Shows the results of the last feature extraction.
      */
     private   ResultsReportPanel results_panel;

     /**
      * Displays the on-line manual.
      */
     private   HelpDialog          help_dialog;

     /**
      * A dialog box where test searches may be performed and top hits reported.
      */
     private   NetworkSearchDialog search_dialog;

     /**
      * A dialog box allowing the user to choose the particular field to
      * extract from MP3 recordings.
      */
     public    MetaDataChooserDialog itunes_field_chooser;

     /**
      * Holds menu items.
      */
     private   JMenuBar            menu_bar;

     /**
      * The Search menu.
      */
     private   JMenu               search_menu;

     /**
      * Makes the NetworkSearchDialog visible.
      */
     private   JMenuItem           search_menu_item;

     /**
      * The Information menu.
      */
     private   JMenu               information_menu;

     /**
      * Displays ownership and version information.
      */
     private   JMenuItem           about_menu_item;

     /**
      * Makes the HelpDialog visible.
      */
     private   JMenuItem           help_menu_item;

     /**
      * The button that begins feature extraction using the settings specified
      * in all tabs.
      */
     private   JButton             extract_button;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Sets up and displays the jMusicMetaManager GUI.
      */
     public OuterFrame()
     {
          // Set the title of the window
          setTitle("jWebMiner 2.0");

          // Set up the menus
          menu_bar = new JMenuBar();
          search_menu = new JMenu("Search");
          search_menu.setMnemonic('s');
          search_menu_item = new JMenuItem("Perform Test Search");
          search_menu_item.setMnemonic('p');
          search_menu_item.addActionListener(this);
          search_menu.add(search_menu_item);
          menu_bar.add(search_menu);
          information_menu = new JMenu("Information");
          information_menu.setMnemonic('i');
          about_menu_item = new JMenuItem("About");
          about_menu_item.setMnemonic('a');
          about_menu_item.addActionListener(this);
          information_menu.add(about_menu_item);
          help_menu_item = new JMenuItem("Help");
          help_menu_item.setMnemonic('h');
          help_menu_item.addActionListener(this);
          information_menu.add(help_menu_item);
          menu_bar.add(information_menu);

          // Set up the tabs
          tabbed_pane = new JTabbedPane();
          search_words_panel = new SearchWordsPanel(this);
          tabbed_pane.addTab("Search Words", search_words_panel);
          required_filter_words_panel = new StringEntryPanel(new ListInputTextParser(this));
          tabbed_pane.addTab("Required Filter Words", required_filter_words_panel);
          excluded_filter_words_panel = new StringEntryPanel(new ListInputTextParser(this));
          tabbed_pane.addTab("Excluded Filter Words", excluded_filter_words_panel);
          site_weightings_panel = new StringEntryPanel(new ListInputTextParser(this));
          tabbed_pane.addTab("Site Weightings", site_weightings_panel);
          options_panel = new OptionsPanel();
          tabbed_pane.addTab("Options", options_panel);
          results_panel = new ResultsReportPanel();
          tabbed_pane.addTab("Results", results_panel);

          // Set the default selected pane to the options_panel
          tabbed_pane.setSelectedIndex(0);

          // Disable the results_panel by default
          enableResultsPanel(false);

          // Set up the extract_button
          extract_button = new JButton("EXTRACT FEATURES");
          extract_button.addActionListener(this);

          // Set up help dialog box
          help_dialog = new HelpDialog("ProgramFiles" + File.separator + "Manual" + File.separator + "contents.html",
               "ProgramFiles" + File.separator + "Manual" + File.separator + "splash.html" );

          // Set up the itunes_field_chooser
          itunes_field_chooser = new MetaDataChooserDialog(this);

          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;
          Container content_pane = getContentPane();
          content_pane.setLayout(new BorderLayout(horizontal_gap, vertical_gap));

          // Add items to the GUI
          setJMenuBar(menu_bar);
          content_pane.add(tabbed_pane, BorderLayout.CENTER);
          content_pane.add(extract_button, BorderLayout.SOUTH);

          // Combine GUI elements into this frame at the left corner of the
          // screen with a size of 800 x 600
          setBounds(200, 50, 1000, 800); // (0, 0, 800, 600)

          // Display the GUI
          this.setVisible(true);

          // Cause program to quit when the exit box is pressed
          addWindowListener(new WindowAdapter()
          {
               public void windowClosing(WindowEvent e)
               {
                    System.exit(0);
               }
          });

          // Cause the GUI focus to be placed on appropriate text areas by
          // default when they are activated by switching to their panes.
          search_words_panel.focusOnTextInput();
          tabbed_pane.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(ChangeEvent e)
               {
                    JTabbedPane pane = (JTabbedPane)e.getSource();
                    int pane_index = pane.getSelectedIndex();
                    if (pane_index == 0) search_words_panel.focusOnTextInput();
                    if (pane_index == 1) required_filter_words_panel.focusOnTextInput();
                    if (pane_index == 2) excluded_filter_words_panel.focusOnTextInput();
                    if (pane_index == 3) site_weightings_panel.focusOnTextInput();
               }
          });
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when actions are performed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the search_menu_item
          if (event.getSource().equals(search_menu_item))
          {
               try
               {
                    // Get strings from GUI
                    String[] temp;
                    String first_string = "";
                    temp = search_words_panel.getOrganizedPrimarySearchStrings();
                    if (temp != null) first_string = temp[0];
                    String second_string = "";
                    temp = search_words_panel.getOrganizedSecondarySearchStrings();
                    if (search_words_panel.isCrossTabulation() && temp != null) second_string = temp[0];
                    String specific_domain = "";
                    temp = site_weightings_panel.getOrganizedStrings();
                    if (temp != null) specific_domain = temp[0];
                    String[] strings_to_exclude = {"", ""};
                    temp = excluded_filter_words_panel.getOrganizedStrings();
                    if (temp != null)
                    {
                         strings_to_exclude[0] = temp[0];
                         if (temp.length > 1) strings_to_exclude[1] = temp[1];
                    }

                    // Close existing dialog if already open
                    if (search_dialog != null)
                         search_dialog.dispose();

                    // Open new dialog
                    search_dialog = new NetworkSearchDialog(
                         first_string,
                         second_string,
                         options_panel.getIsLiteralSearch(),
                         options_panel.getIsOrSearch(),
                         options_panel.getIncludeNonMatchingSimilarHits(),
                         strings_to_exclude[0],
                         strings_to_exclude[1],
                         specific_domain,
                         options_panel.getLanguageFilter(),
                         options_panel.getCountryFilter(),
                         options_panel.getRegionToSearchFrom(),
                         options_panel.getFileTypeFilter(),
                         options_panel.getSuppressSimilarHits(),
                         options_panel.getSuppressAdultContent(),
                         10,
                         options_panel.getYahooApplicationID(),
                         options_panel.getGoogleLicenseKey() );
               }
               catch(Exception e)
               {
				  //  e.printStackTrace();
				   JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			   }
          }

          // React to the about_menu_item
          if (event.getSource().equals(about_menu_item))
               new AboutDialog(this, "jWebMiner 2.0.1", "Cory McKay (1.x) and Gabriel Vigliensoni (2.x)",
                    "2010 (GNU GPL)", "McGill University");

          // React to the view_manual_menu_item
          if (event.getSource().equals(help_menu_item))
               help_dialog.setVisible(true);

          // React to the extract_button
          else if (event.getSource().equals(extract_button))
               extractFeatures();
     }


     /**
      * Enable or disable (i.e. grey out) the Results Panel. If enable is true,
      * focus is placed on the Results Panel. If it is false and focus is
      * already on it, focus is removed to the Search Words Panel.
      *
      * @param enable    Whether or not to enable the Results Panel.
      */
     public void enableResultsPanel(boolean enable)
     {
          tabbed_pane.setEnabledAt(5, enable);

          if (enable)
               tabbed_pane.setSelectedIndex(5);
          else if (tabbed_pane.getSelectedIndex() == 5)
               tabbed_pane.setSelectedIndex(0);
     }


     /**
      * Set the contents of the text on the Results panel, replacing anything
      * that is already there. Also stores on feature values that can be saved.
      *
      * @param results        The new text to display.
      * @param feature_values The extracted feature values.
      * @param row_labels     The labels for the rows (first dimension) of
      *                       feature_values.
      * @param column_labels  The labels for the columns (second dimension) of
      *                       feature_values.
      */
     public void setResultsText(String results, double[][] feature_values,
          String[] row_labels, String[] column_labels)
     {
          results_panel.storeFeatureValues(feature_values, row_labels, column_labels);
          results_panel.setText(results);
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Perform a web-based feature extraction using the settings stored in the
      * panes of the GUI. Displays an error message if a problem occurs. Sends
      * results to the results_panel.
      */
     private void extractFeatures()
     {
          // Performs processing and stores results
          AnalysisProcessor searcher = null;

          // Perform processing
          try
          {
               // Store search parameters and initialize searcher
               searcher = new AnalysisProcessor(
                    this,
                    search_words_panel.isCrossTabulation(),
                    options_panel.getScoringFunction(search_words_panel.isCrossTabulation()),
                    options_panel.getShouldNormalizeAcrossWebServices(),
                    options_panel.getShouldNormalizeAcrossWebSites(),
                    options_panel.getShouldNormalizeScores(),
                    options_panel.getYahooApplicationID(),
                    options_panel.getGoogleLicenseKey(),
                    options_panel.getLastFMEnabled(),
                    options_panel.getLastFMLicenseKey(),
                    options_panel.getIsLiteralSearch(),
                    options_panel.getIsOrSearch(),
                    options_panel.getIncludeNonMatchingSimilarHits(),
                    options_panel.getSuppressSimilarHits(),
                    options_panel.getSuppressAdultContent(),
                    options_panel.getLanguageFilter(),
                    options_panel.getCountryFilter(),
                    options_panel.getRegionToSearchFrom(),
                    options_panel.getFileTypeFilter(),
                    excluded_filter_words_panel.getOrganizedStrings(),
                    required_filter_words_panel.getOrganizedStrings(),
                    site_weightings_panel.getOrganizedStrings(),
                    search_words_panel.getOrganizedPrimarySearchStrings(),
                    search_words_panel.getOrganizedSecondarySearchStrings(),
                    options_panel.getReportOptions() );

               // Prepare progress bar
               int progress_bar_polling_interval = 333; // in milliseconds
               DoubleProgressBarTaskCoordinator progress_bar_coordinator = new DoubleProgressBarTaskCoordinator(searcher);
               DoubleProgressBarDialog progress_bar = new DoubleProgressBarDialog(this,
                    progress_bar_coordinator,
                    progress_bar_polling_interval);
               searcher.setDoubleProgressBarTaskCoordinator(progress_bar_coordinator);
               progress_bar_coordinator.setProgressBarDialog(progress_bar);

               // Begin processing
               progress_bar_coordinator.go();
          }
          catch (Throwable t) // note that this clause may not be reached by some exceptions, as some parts of the processing thread display their own error dialog boxes
          {
               java.awt.Toolkit.getDefaultToolkit().beep();
               if (t.toString().equals("java.lang.OutOfMemoryError"))
                    JOptionPane.showMessageDialog(this, "The Java Runtime ran out of memory.\nPlease rerun this program with a higher amount of memory assigned to the Java Runtime heap.\nFeature extraction cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
               else
               {
                    JOptionPane.showMessageDialog(this, t.getMessage() + "\n\nFeature extraction cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    // t.printStackTrace();
               }
          }
     }
}