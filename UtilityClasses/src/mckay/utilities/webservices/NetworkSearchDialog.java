/*
 * NetworkSearchDialog.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.webservices;

import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import mckay.utilities.gui.progressbars.SimpleProgressBarDialog;


/**
 * A JFrame dialog box that can be used to perform searches using web services
 * that extend the NetworkSearch class. The names of the available web services
 * are listed in the included_web_services field of this class. Although the
 * intended purpose of this clas is to debug data mining applications, it can
 * certainly be adapted to other purposes, including simply performing searches.
 *
 * <p>The top section of this dialog box allows the user to choose the
 * parameters of the search to be performed. This provides an interface that
 * is independent of the particular search service being used for any given
 * search. The parameters are as follows:
 *
 * <ul><li>The <i>Search string 1</i> and <i>Search string 2</i> fields allow
 * the user to specify the strings to search for. These fields may each contain
 * multiple words separated by spaces if desired. No special query formatting
 * specific to a particular search service should be applied to these strings,
 * as this interface is intended to be service-independent, and this dialog
 * auto-formats each request in a way that is specific to each service.
 *
 * <li>The <i>Treat strings literally</i> checkbox controls whether all search
 * queries should be literal searches (e.g. for the query "heavy metal", hits
 * must have the two words adjacent if the search is literal). This is also
 * sometimes known as an exact search or a phrase search.
 *
 * <li>The <i>Perform search as OR instead of AND</i> checkbox controls whether
 * search queries in <i>Search string 1</i> and <i>Search string 2</i> fields
 * need only contain one of the specified query words in order to result in a
 * hit. This OR applies to both the individual words in each of the fields as
 * well as the combination of the fields. If this is true, then only one of the
 * query words must be present. If this is false, then all of them must be
 * present (although not necessarily in the specified order, unless the <i>Treat
 * strings literally</i> checkbox is selected).
 *
 * <li>The <i>Include non-matching similar hits</i> checkbox controls whether
 * results returned by search queries may include hits that do not contain one
 * or more of the specified search string(s) but do contain terms very similar
 * to them (e.g. alternative spellings).
 *
 * <li>The <i>Excluded string 1</i> and <i>Excluded string 2</i> text fields
 * set strings to exclude in all search queries performed by this object (i.e
 * filter strings).Search hits may not contain these filter strings. These
 * excluded strings are treated as literal (i.e. must appear in the same order).
 *
 * <li>The <i>Limit to site</i> text field sets a network site that will be
 * exclusively searched in search queries. Leaving this blank means that all
 * available network stites will be searched.
 *
 * <li>The <i>Limit to language</i> combo box field sets the name of a language
 * that hits must be in in order to be included in search results.
 *
 * <li>The <i>Limit to country</i> combo box field sets the name of a country
 * that sites must be in in order to be included in search results.
 *
 * <li>The <i>Search from region</i> combo box field sets the name of a country
 * where the search will be performed (i.e. where the search service is
 * located). Results are not limited to this country, however.
 *
 * <li>The <i>Limit to file type</i> combo box field sets the name of a file
 * extension that a document must have in order to be returned as a hit in
 * search results.
 *
 * <li>The <i>Suppress similar hits</i> checkbox controls whether to suppress
 * similar hits when reporting results. Similar in this context means either
 * sites with identical titles and/or descriptions, or multiple hits from the
 * same host.
 *
 * <li>The <i>Suppress adult content</i> checkbox controls whether to suppress
 * hits that are classified as containing adult content by the search service in
 * question.
 *
 * <li>The <i>Maximum resluts returned</i> combo box sets the maximum number of
 * results returned by each search query.</ul>
 *
 * <p>The middle section of this dialog box displays search results. Each search
 * can be performed using up to two search services, and the results are
 * listed side by side. Above each results field is a combo box allowing the
 * user to chosse which search service to utilize (if any), as well as
 * <i>Prev</i> and <i>Next</i> buttons for navigating large sets of results.
 * The number of results moved when a Prev or Next button is pressed is set by
 * the <i>Maximum resluts returned</i> combo box. Results also include the
 * total number of hits found, any relevant limitations of the particular search
 * service used and information on the actual query sent to the web service
 * that generated the result.
 *
 * <p>Searches can be initiated by pressing the <i>PERFORM SEARCH</i> button
 * or by pressing Enter while the cursor is in one of the text fields.
 *
 * @author Cory McKay
 */
public class NetworkSearchDialog
     extends JFrame
     implements ActionListener, KeyListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * A string to base the search on, consisting of one or more words. This
      * must be present.
      */
     private   JTextField     first_search_string_field;
     
     /**
      * Another string to base the search on, consisting of one or more words.
      * This may be left blank if wished.
      */
     private   JTextField     second_search_string_field;
     
     /**
      * Whether the strings specified in first_search_string_field and
      * second_search_string_field are to be searched literally. If so, all
      * words must occur exactly as they appear and in the same order as in the
      * first_search_string_field and second_search_string_field to result in
      * a hit. This is also sometimes known as an exact search or a phrase
      * search.
      */
     private   JCheckBox      literal_search_checkbox;
     
     /**
      * Whether all strings in both first_search_string_field and
      * second_search_string_field are searched based on a boolean OR or AND.
      * In the OR case, hits will occur if some but not all of the search words
      * are present and in the AND case hits will only occur if all search words
      * are present.
      */
     private   JCheckBox      any_not_all_search_strings_checkbox;
     
     /**
      * Whether hits may be included that do not contain one or more of the
      * specified search string(s) but do contain terms very similar to them
      * (e.g. alternative spellings).
      */
     private   JCheckBox      include_hits_with_similar_search_strings_checkbox;
     
     /**
      * A string that may not be contained in a hit. Also sometimes referred to
      * as a filter string. This is optional.
      */
     private   JTextField     first_excluded_string_field;
     
     /**
      * Another string that may not be contained in a hit. Also sometimes
      * referred to as a filter string. This is optional.
      */
     private   JTextField     second_excluded_string_field;
     
     /**
      * Specifies a specific network domain that a search can be limited to. Can
      * be left blank if all available network sites are to be searched.
      */
     private   JTextField     specific_domain_field;
     
     /**
      * Excludes hits that to not belong to the specified language. An entry of
      * "No Limitations" places no language-based restrictions.
      */
     private   JComboBox      limit_to_language_combobox;
     
     /**
      * Excludes hits that are not located in the specified country. An entry of
      * "No Limitations" places no location-based restrictions.
      */
     private   JComboBox      limit_to_country_combobox;
     
     /**
      * Performs the search using services located in the specified country, if
      * possible (i.e. where the search service is located). Results are not
      * limited to this country.An entry of "No Limitations" causes the default
      * service to be used.
      */
     private   JComboBox      searching_from_region_combobox;
     
     /**
      * Only searches for files of the specified type (extension). An entry of
      * "No Limitations" places no file type-based restrictions.
      */
     private   JComboBox      limit_to_file_type_combobox;
     
     /**
      * Whether to suppress similar hits, namely ones with identical titles
      * and descriptions or multiple hits from the same host.
      */
     private   JCheckBox      suppress_similar_hits_checkbox;
     
     /**
      * Whether to suppress hits that are classified as adult by the search
      * service.
      */
     private   JCheckBox      suppress_adult_content_checkbox;
     
     /**
      * Specifies the maximum number of results for which the details will be
      * reported. This is not a maximum on the number of overall hits reported.
      */
     private   JComboBox      max_results_combobox;
     
     /**
      * The web services to use for performing the search whose results are
      * reported in the left_results_pane.
      */
     private   JComboBox      left_web_service_combobox;
     
     /**
      * The web services to use for performing the search whose results are
      * reported in the right_web_service_combobox.
      */
     private   JComboBox      right_web_service_combobox;
     
     /**
      * Reports the results of searches performed using the serivce specified
      * in the left_web_service_combobox.
      */
     private   JEditorPane    left_results_pane;
     
     /**
      * Reports the results of searches performed using the serivce specified
      * in the right_web_service_combobox.
      */
     private   JEditorPane    right_results_pane;
     
     /**
      * Causes the left_results_pane to display earlier ranked results if they
      * are available.
      */
     private   JButton        left_back_button;
     
     /**
      * Causes the left_results_pane to display later ranked results if they
      * are available.
      */
     private   JButton        left_next_button;
     
     /**
      * Causes the right_results_pane to display earlier ranked results if they
      * are available.
      */
     private   JButton        right_back_button;
     
     /**
      * Causes the right_results_pane to display later ranked results if they
      * are available.
      */
     private   JButton        right_next_button;
     
     /**
      * The ranking of the first search result currently displayed in the
      * left_results_pane, indexed starting at 1, not 0.
      */
     private   int            current_left_start = 0;
     
     /**
      * The ranking of the last search result currently displayed in the
      * left_results_pane, indexed starting at 1, not 0.
      */
     private   int            current_left_end = 0;
     
     /**
      * The ranking of the first search result currently displayed in the
      * right_results_pane, indexed starting at 1, not 0.
      */
     private   int            current_right_start = 0;
     
     /**
      * The ranking of the last search result currently displayed in the
      * right_results_pane, indexed starting at 1, not 0.
      */
     private   int            current_right_end = 0;
     
     /**
      * Causes a search to be performed using the parameters specified in this
      * dialog box.
      */
     private   JButton        begin_search_button;
     
     /**
      * The Application ID needed to identify the applciation to Yahoo! web
      * services
      */
     private  String          yahoo_application_id;
     
     /**
      * The Google license key that is to be used to access the Google SOAP web
      * services.
      */
     private   String         google_license_key;
     
     /**
      * The web services that can be accessed by this dialog box.
      */
     private   String[]       included_web_services;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Set up the dialog box with the default GUI parameters selected. Store
      * the codes needed to access web services.
      *
      * @param yahoo_application_id     The Application ID needed to identify
      *                                 the applciation to Yahoo!
      * @param google_license_key       The Google license key that is to be
      *                                 used to access the Google SOAP web
      *                                 services.
      * @throws Exception               Throws an informative exception if
      *                                 any of the web services keys are null
      *                                 or empty.
      */
     public NetworkSearchDialog(String yahoo_application_id, String google_license_key)
     throws Exception
     {
          // Call the superclass' constructor
          super();
          
          // Validate web service information and store keys
          processWebServices(yahoo_application_id, google_license_key);
          
          // Initialize the layout of the dialog box
          initializeLayout();
          
          // Set GUI parameters to their defaults
          literal_search_checkbox.setSelected(true);
          any_not_all_search_strings_checkbox.setSelected(false);
          include_hits_with_similar_search_strings_checkbox.setSelected(false);
          limit_to_language_combobox.setSelectedIndex(0);
          limit_to_country_combobox.setSelectedIndex(0);
          searching_from_region_combobox.setSelectedIndex(0);
          limit_to_file_type_combobox.setSelectedIndex(0);
          suppress_similar_hits_checkbox.setSelected(false);
          suppress_adult_content_checkbox.setSelected(false);
          max_results_combobox.setSelectedIndex(9);
          int web_services_available = included_web_services.length - 1;
          left_web_service_combobox.setSelectedIndex(web_services_available);
          web_services_available--;
          if (web_services_available == 0) right_web_service_combobox.setSelectedIndex(0);
          else right_web_service_combobox.setSelectedIndex(web_services_available);
          
          // Make the dialog box visible
          setVisible(true);
     }
     
     
     /**
      * Set up the dialog box with the  GUI parameters specified. Strings
      * parameters may be passed as "", in which case the defaults will be used.
      *
      * @param first_search_string      A string to base the search on,
      *                                 consisting of one or more words.
      * @param second_search_string     Another string to base the search on,
      *                                 consisting of one or more words.
      * @param is_literal_search        Whether the strings specified in
      *                                 first_search_string and
      *                                 second_search_string are to be searched
      *                                 literally. If so, all words must occur
      *                                 exactly as they appear and in the same
      *                                 order as in first_search_string and
      *                                 second_search_string to result in a hit.
      *                                 This is also sometimes known as an exact
      *                                 search or a phrase search.
      * @param is_boolean_or            Whether all strings in both
      *                                 first_search_string and
      *                                 second_search_string are searched based
      *                                 on a boolean OR or AND. In the OR case,
      *                                 hits will occur if some but not all of
      *                                 the search words are present and in the
      *                                 AND case hits will only occur if all
      *                                 search words are present.
      * @param include_similar_hits     Whether hits may be included that do not
      *                                 contain a specified search string but do
      *                                 contain something very similar to it
      *                                 (e.g. alternative spellings).
      * @param first_excluded_string    A string that may not be contained in a
      *                                 hit. Also sometimes referred to as a
      *                                 filter string. Set to "" if there are no
      *                                 exclusions.
      * @param second_excluded_string   Another  string that may not be
      *                                 contained in ahit. Also sometimes
      *                                 referred to as a filter string. Set to
      *                                 "" if there are no exclusions.
      * @param specific_domain          A specific domain that a search can be
      *                                 limited to. Set to "" if all
      *                                 available sites are to be searched.
      * @param specific_language        Excludes hits that to not belong to the
      *                                 specified language. An entry of
      *                                 "No Limitations" places no language-
      *                                 based restrictions.
      * @param specific_country         Excludes hits that are not located in
      *                                 the specified country. An entry of
      *                                 "No Limitations" places no location-
      *                                 based restrictions.
      * @param search_origin            Performs the search using services
      *                                 located in the specified country, if
      *                                 possible. An entry of "No Limitations"
      *                                 causes the default service to be used.
      * @param specific_file_type       Only searches for files of the specified
      *                                 type (extension). An entry of "No
      *                                 Limitations" places no file type-based
      *                                 restrictions.
      * @param suppress_similar_hits    Whether to suppress similar hits, namely
      *                                 ones with identical titles and
      *                                 descriptions or multiple hits from the
      *                                 same host.
      * @param suppress_adult_content   Whether to suppress hits that are
      *                                 classified as adult by the search
      *                                 service.
      * @param max_results              Specifies the maximum number of results
      *                                 for which the details will be reported.
      *                                 This is not a maximum on the number of
      *                                 overall hits reported.
      * @param yahoo_application_id     The Application ID needed to identify
      *                                 the applciation to Yahoo!
      * @param google_license_key       The Google license key that is to be
      *                                 used to access the Google SOAP web
      *                                 services.
      * @throws Exception               Throws an informative exception if
      *                                 any of the web services keys are null
      *                                 or empty.
      */
     public NetworkSearchDialog( String first_search_string,
          String second_search_string,
          boolean is_literal_search,
          boolean is_boolean_or,
          boolean include_similar_hits,
          String first_excluded_string,
          String second_excluded_string,
          String specific_domain,
          String specific_language,
          String specific_country,
          String search_origin,
          String specific_file_type,
          boolean suppress_similar_hits,
          boolean suppress_adult_content,
          int max_results,
          String yahoo_application_id,
          String google_license_key )
          throws Exception
     {
          // Call the superclass' constructor
          super();
          
          // Validate web service information and store keys
          processWebServices(yahoo_application_id, google_license_key);
          
          // Initialize the layout of the dialog box
          initializeLayout();
          
          // Initialize GUI settings based on specified values
          first_search_string_field.setText(first_search_string);
          second_search_string_field.setText(second_search_string);
          literal_search_checkbox.setSelected(is_literal_search);
          any_not_all_search_strings_checkbox.setSelected(is_boolean_or);
          include_hits_with_similar_search_strings_checkbox.setSelected(include_similar_hits);
          first_excluded_string_field.setText(first_excluded_string);
          second_excluded_string_field.setText(second_excluded_string);
          specific_domain_field.setText(specific_domain);
          limit_to_language_combobox.setSelectedItem(specific_language);
          limit_to_country_combobox.setSelectedItem(specific_country);
          searching_from_region_combobox.setSelectedItem(search_origin);
          limit_to_file_type_combobox.setSelectedItem(specific_file_type);
          suppress_similar_hits_checkbox.setSelected(suppress_similar_hits);
          suppress_adult_content_checkbox.setSelected(suppress_adult_content);
          try
          {max_results_combobox.setSelectedIndex(max_results - 1);}
          catch (Exception e)
          {max_results_combobox.setSelectedIndex(0);}
          int web_services_available = included_web_services.length - 1;
          left_web_service_combobox.setSelectedIndex(web_services_available);
          web_services_available--;
          if (web_services_available == 0) right_web_service_combobox.setSelectedIndex(0);
          else right_web_service_combobox.setSelectedIndex(web_services_available);
          
          // Make the dialog box visible
          setVisible(true);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the begin_search_button
          if (event.getSource().equals(begin_search_button))
          {
               current_left_start = 1;
               current_right_start = 1;
               current_left_end = 0;
               current_right_end = 0;
               performSearch(current_left_start, current_right_start);
          }
          
          // React to the left_back_button
          if (event.getSource().equals(left_back_button))
          {
               current_left_start = current_left_start - ((Integer) max_results_combobox.getSelectedItem()).intValue();
               if (current_left_start < 1) current_left_start = 1;
               performSearch(current_left_start, 0);
          }
          
          // React to the left_next_button
          if (event.getSource().equals(left_next_button))
          {
               current_left_start = current_left_end + 1;
               performSearch(current_left_start, 0);
          }
          
          // React to the right_back_button
          if (event.getSource().equals(right_back_button))
          {
               current_right_start = current_right_start - ((Integer) max_results_combobox.getSelectedItem()).intValue();
               if (current_right_start < 1) current_right_start = 1;
               performSearch(0, current_right_start);
          }
          
          // React to the right_next_button
          if (event.getSource().equals(right_next_button))
          {
               current_right_start = current_right_end + 1;
               performSearch(0, current_right_start);
          }
          
          // React to the left_web_service_combobox
          if (event.getSource().equals(left_web_service_combobox))
               left_results_pane.setText("");
          
          // React to the right_web_service_combobox
          if (event.getSource().equals(right_web_service_combobox))
               right_results_pane.setText("");
     }
     
     
     /**
      * Performs a search if the enter key is released while the cursor is in
      * one of the two search strings boxes.
      *
      * @param e An event generated by a key being released.
      */
     public void keyReleased(KeyEvent e)
     {
          if (e.getKeyCode() == KeyEvent.VK_ENTER)
          {
               current_left_start = 1;
               current_right_start = 1;
               current_left_end = 0;
               current_right_end = 0;
               performSearch(current_left_start, current_right_start);
          }
     }
     
     
     /**
      * Does nothing if a key is pressed.
      *
      * @param e An event generated by a key being pressed.
      */
     public void keyPressed(KeyEvent e)
     {
     }
     
     
     /**
      * Does nothing if a key is typed.
      *
      * @param e An event generated by a key being typed.
      */
     public void keyTyped(KeyEvent e)
     {
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Validate web service information and throw an exception if parameters
      * are invalid. Store the appropriate labels in the included_web_services
      * field and store the yahoo_application_id and google_license_key in
      * memory.
      *
      * @param yahoo_application_id     The Application ID needed to identify
      *                                 the applciation to Yahoo!
      * @param google_license_key       The Google license key that is to be
      *                                 used to access the Google SOAP web
      *                                 services.
      * @throws Exception               Throws an informative exception if
      *                                 any of the web services keys are null
      *                                 or empty.
      */
     private void processWebServices(String yahoo_application_id,
          String google_license_key)
          throws Exception
     {
          // Throw an exception if necessary
          if (yahoo_application_id == null && google_license_key == null)
               throw new Exception("No web services specified.");
          if (yahoo_application_id != null)
               if (yahoo_application_id.equals(""))
                    throw new Exception("No Yahoo Application ID code specified.");
          if (google_license_key != null)
               if (google_license_key.equals(""))
                    throw new Exception("No Google License Key code specified.");
          
          // Store the available web services in included_web_services
          LinkedList<String> included_web_services_list = new LinkedList<String>();
          included_web_services_list.add("None");
          if (yahoo_application_id != null) included_web_services_list.add("Yahoo");
          if (google_license_key != null) included_web_services_list.add("Google");
          included_web_services = included_web_services_list.toArray(new String[1]);
          
          // Store web service codes
          this.yahoo_application_id = yahoo_application_id;
          this.google_license_key = google_license_key;
     }
     
     
     /**
      * Initializes the layout of the GUI.
      */
     private void initializeLayout()
     {
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;
          
          // Give the dialog box its title
          setTitle("Network Search");
          
          // Prepare row 1 of controls
          first_search_string_field = new JTextField();
          first_search_string_field.addKeyListener(this);
          JPanel first_search_string_panel = getLabeledContainer( "Search string 1:",
               first_search_string_field,
               horizontal_gap );
          second_search_string_field = new JTextField();
          second_search_string_field.addKeyListener(this);
          JPanel second_search_string_panel = getLabeledContainer( "Search string 2:",
               second_search_string_field,
               horizontal_gap );
          
          // Lay out row 1 of controls
          JPanel control_panel_1 = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          control_panel_1.add(first_search_string_panel);
          control_panel_1.add(second_search_string_panel);
          
          // Prepare row 2 of controls
          literal_search_checkbox = new JCheckBox("Treat search strings literally");
          any_not_all_search_strings_checkbox = new JCheckBox("Perform search as OR instead of AND");
          include_hits_with_similar_search_strings_checkbox  = new JCheckBox("Include non-matching similar hits");
          
          // Lay out row 2 of controls
          JPanel control_panel_2 = new JPanel(new GridLayout(1, 3, horizontal_gap, vertical_gap));
          control_panel_2.add(literal_search_checkbox);
          control_panel_2.add(any_not_all_search_strings_checkbox);
          control_panel_2.add(include_hits_with_similar_search_strings_checkbox);
          
          // Prepare row 3 of controls
          first_excluded_string_field = new JTextField();
          JPanel first_excluded_string_panel = getLabeledContainer( "Excluded string 1:",
               first_excluded_string_field,
               horizontal_gap );
          first_excluded_string_field.addKeyListener(this);
          second_excluded_string_field = new JTextField();
          JPanel second_excluded_string_panel = getLabeledContainer( "Excluded string 2:",
               second_excluded_string_field,
               horizontal_gap );
          second_excluded_string_field.addKeyListener(this);
          specific_domain_field = new JTextField();
          JPanel specific_site_panel = getLabeledContainer( "Limit to site:",
               specific_domain_field,
               horizontal_gap );
          specific_domain_field.addKeyListener(this);
          
          // Lay out row 3 of controls
          JPanel control_panel_3 = new JPanel(new GridLayout(1, 3, horizontal_gap, vertical_gap));
          control_panel_3.add(first_excluded_string_panel);
          control_panel_3.add(second_excluded_string_panel);
          control_panel_3.add(specific_site_panel);
          
          // Prepare row 4 of controls
          limit_to_language_combobox = new JComboBox(NetworkSearch.included_languages);
          JPanel limit_to_language_panel = getLabeledContainer( "Limit to language:",
               limit_to_language_combobox,
               horizontal_gap );
          limit_to_country_combobox = new JComboBox(NetworkSearch.included_countries);
          JPanel limit_to_country_panel = getLabeledContainer( "Limit to country:",
               limit_to_country_combobox,
               horizontal_gap );
          
          // Lay out row 4 of controls
          JPanel control_panel_4 = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          control_panel_4.add(limit_to_language_panel);
          control_panel_4.add(limit_to_country_panel);
          
          // Prepare row 5 of controls
          limit_to_file_type_combobox = new JComboBox(NetworkSearch.included_file_types);
          JPanel limit_to_file_type_panel = getLabeledContainer( "Limit to file type:",
               limit_to_file_type_combobox,
               horizontal_gap );
          searching_from_region_combobox = new JComboBox(NetworkSearch.included_countries);
          JPanel searching_from_region_panel = getLabeledContainer( "Search from region:",
               searching_from_region_combobox,
               horizontal_gap );
          
          // Lay out row 5 of controls
          JPanel control_panel_5 = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          control_panel_5.add(searching_from_region_panel);
          control_panel_5.add(limit_to_file_type_panel);
          
          // Prepare row 6 of controls
          suppress_similar_hits_checkbox = new JCheckBox("Suppress similar hits");
          suppress_adult_content_checkbox = new JCheckBox("Suppress adult content");
          Integer[] permitted_number_of_results = new Integer[100];
          for (int i = 0; i < permitted_number_of_results.length; i++)
               permitted_number_of_results[i] = new Integer(i + 1);
          max_results_combobox = new JComboBox(permitted_number_of_results);
          JPanel max_results_panel = getLabeledContainer( "Maximum results returned:",
               max_results_combobox,
               horizontal_gap );
          
          // Lay out row 6 of controls
          JPanel control_panel_6 = new JPanel(new GridLayout(1, 3, horizontal_gap, vertical_gap));
          control_panel_6.add(suppress_similar_hits_checkbox);
          control_panel_6.add(suppress_adult_content_checkbox);
          control_panel_6.add(max_results_panel);
          
          // Lay out the combined control panels
          JPanel combined_control_panels = new JPanel(new GridLayout(6, 1, horizontal_gap, vertical_gap));
          combined_control_panels.add(control_panel_1);
          combined_control_panels.add(control_panel_2);
          combined_control_panels.add(control_panel_3);
          combined_control_panels.add(control_panel_4);
          combined_control_panels.add(control_panel_5);
          combined_control_panels.add(control_panel_6);
          
          // Prepare the results chooser
          left_web_service_combobox = new JComboBox(included_web_services);
          left_web_service_combobox.addActionListener(this);
          JPanel left_web_service_panel = getLabeledContainer( "Service:",
               left_web_service_combobox,
               horizontal_gap );
          left_back_button = new JButton("<< Prev");
          left_back_button.addActionListener(this);
          left_back_button.setEnabled(false);
          left_next_button = new JButton("Next >>");
          left_next_button.addActionListener(this);
          left_next_button.setEnabled(false);
          right_web_service_combobox = new JComboBox(included_web_services);
          right_web_service_combobox.addActionListener(this);
          JPanel right_web_service_panel = getLabeledContainer( "Service:",
               right_web_service_combobox,
               horizontal_gap );
          right_back_button = new JButton("<< Prev");
          right_back_button.addActionListener(this);
          right_back_button.setEnabled(false);
          right_next_button = new JButton("Next >>");
          right_next_button.addActionListener(this);
          right_next_button.setEnabled(false);
          
          // Lay out the results chooser
          JPanel left_chooser_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          left_chooser_panel.add(left_back_button, BorderLayout.WEST);
          left_chooser_panel.add(left_web_service_combobox, BorderLayout.CENTER);
          left_chooser_panel.add(left_next_button, BorderLayout.EAST);
          JPanel right_chooser_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          right_chooser_panel.add(right_back_button, BorderLayout.WEST);
          right_chooser_panel.add(right_web_service_combobox, BorderLayout.CENTER);
          right_chooser_panel.add(right_next_button, BorderLayout.EAST);
          JPanel results_chooser = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          results_chooser.add(left_chooser_panel);
          results_chooser.add(right_chooser_panel);
          
          // Prepare the results display
          left_results_pane = new JEditorPane();
          left_results_pane.setContentType("text/html");
          left_results_pane.setEditable(false);
          right_results_pane = new JEditorPane();
          right_results_pane.setContentType("text/html");
          right_results_pane.setEditable(false);
          
          // Lay out the results display
          JScrollPane left_results_scroll_pane = new JScrollPane( left_results_pane,
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          JScrollPane right_results_scroll_pane = new JScrollPane( right_results_pane,
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          JPanel combined_results_panel = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          combined_results_panel.add(left_results_scroll_pane);
          combined_results_panel.add(right_results_scroll_pane);
          
          // Lay out the results chooser and results panel
          JPanel results_chooser_and_results_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          results_chooser_and_results_panel.add(results_chooser, BorderLayout.NORTH);
          results_chooser_and_results_panel.add(combined_results_panel, BorderLayout.CENTER);
          
          // Set up the extract_button
          begin_search_button = new JButton("PERFORM SEARCH");
          begin_search_button.addActionListener(this);
          
          // Add borders
          JPanel control_panel_border_panel_inner = new JPanel();
          JPanel control_panel_border_panel_outer = new JPanel();
          control_panel_border_panel_inner.add(combined_control_panels);
          control_panel_border_panel_outer.add(control_panel_border_panel_inner);
          combined_control_panels.setBorder(BorderFactory.createEmptyBorder(vertical_gap/2, horizontal_gap/2, vertical_gap/2, horizontal_gap/2));
          control_panel_border_panel_inner.setBorder(BorderFactory.createEtchedBorder());
          control_panel_border_panel_outer.setBorder(BorderFactory.createEmptyBorder(vertical_gap, 0, horizontal_gap/2, 0));
          results_chooser_and_results_panel.setBorder(BorderFactory.createEmptyBorder(0, horizontal_gap, vertical_gap, horizontal_gap));
          
          // Lay out the overall panel
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          add(control_panel_border_panel_outer, BorderLayout.NORTH);
          add(results_chooser_and_results_panel, BorderLayout.CENTER);
          add(begin_search_button, BorderLayout.SOUTH);
          
          // Place dialog at the left corner of the screen with a size of
          // 750 x 90% of the screen height
          int screen_height = (Toolkit.getDefaultToolkit().getScreenSize()).height;
          setBounds(0, 0, 775, (int) (0.9 * screen_height));
          
          // Cause to truly close when close box closed
          setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     }
     
     
     /**
      * Performs a separate search for each (or one, based on the parameters
      * passed to this method) of the two web serveices selected for the
      * JEditorPanes. Both searches are based on the same parameters selected
      * in this dialog box. The results of each search are displayed in the
      * jEditorPanes. A progress bar shows the progress of the searches. A
      * dialog box is displayed with an informative message if a problem occurs.
      *
      * @param left_start_rank     The ranking of the first search result to
      *                            display in the left_results_panel. The
      *                            indexing starts at 1, not 0. A value of
      *                            0 means that no search is to be performed
      *                            by the left web searcher.
      * @param right_start_rank    The ranking of the first search result to
      *                            display in the right_results_panel. The
      *                            indexing starts at 1, not 0. A value of
      *                            0 means that no search is to be performed
      *                            by the left web searcher.
      */
     private void performSearch(int left_start_rank, int right_start_rank)
     {
          // Clear the two results panels
          if (left_start_rank != 0)
               left_results_pane.setText("");
          if (right_start_rank != 0)
               right_results_pane.setText("");
          
          // Prepare the progress bar
          int number_tasks = 1; // first task is initializing
          if (left_start_rank > 0) number_tasks++;
          if (right_start_rank > 0) number_tasks++;
          SimpleProgressBarDialog progress_bar = new SimpleProgressBarDialog(number_tasks, this);
          
          // Set the start rankings
          int[] start_ranks = {left_start_rank, right_start_rank};
          
          // Initialize the searchers
          NetworkSearch[] searchers = new NetworkSearch[2];
          String[] service_names = {(String) left_web_service_combobox.getSelectedItem(), (String) right_web_service_combobox.getSelectedItem()};
          for (int i = 0; i < searchers.length; i++)
          {
               searchers[i] = null;
               if (start_ranks[i] != 0)
               {
                    if (service_names[i].equals("Yahoo"))
                         searchers[i] = new YahooWebSearch(yahoo_application_id);
                    else if (service_names[i].equals("Google"))
                         searchers[i] = new GoogleWebSearch(google_license_key);
               }
          }
          
          // Configure the searchers
          for (int i = 0; i < searchers.length; i++)
          {
               try
               {
                    if (searchers[i] != null)
                    {
                         // Set boolean configurations
                         searchers[i].setLiteralSearch(literal_search_checkbox.isSelected());
                         searchers[i].setOrBasedSearch(any_not_all_search_strings_checkbox.isSelected());
                         searchers[i].setIncludeSimilarButNonMatchingStrings(include_hits_with_similar_search_strings_checkbox.isSelected());
                         searchers[i].setSuppressSimilarHits(suppress_similar_hits_checkbox.isSelected());
                         searchers[i].setSuppressAdultContent(suppress_adult_content_checkbox.isSelected());
                         
                         // Set exclusion strings
                         int number_exlusions = 0;
                         if (!first_excluded_string_field.getText().equals(""))
                              number_exlusions++;
                         if (!second_excluded_string_field.getText().equals(""))
                              number_exlusions++;
                         if (number_exlusions != 0)
                         {
                              String[] exclusions = new String[number_exlusions];
                              int j = 0;
                              if (!first_excluded_string_field.getText().equals(""))
                              {
                                   exclusions[j] = first_excluded_string_field.getText();
                                   j++;
                              }
                              if (!second_excluded_string_field.getText().equals(""))
                                   exclusions[j] = second_excluded_string_field.getText();
                              searchers[i].setSearchStringsToExclude(exclusions);
                         }
                         
                         // Set site to limit to
                         if (!specific_domain_field.getText().equals(""))
                              searchers[i].setSpecificSiteToSearch(specific_domain_field.getText());
                         
                         // Set combo box configurations
                         searchers[i].setLanguageResultsMustBeIn((String) limit_to_language_combobox.getSelectedItem());
                         searchers[i].setCountryResultsMustBeIn((String) limit_to_country_combobox.getSelectedItem());
                         searchers[i].setRegionToSearchFrom((String) searching_from_region_combobox.getSelectedItem());
                         searchers[i].setFileTypeResultsMustBelongTo((String) limit_to_file_type_combobox.getSelectedItem());
                    }
               }
               catch (Exception e)
               {JOptionPane.showMessageDialog(null, e.getMessage(), service_names[i] + " ERROR", JOptionPane.ERROR_MESSAGE);}
          }
          
          // Prepare search strings
          String[] search_strings = null;
          int number_search_strings = 0;
          if (!first_search_string_field.getText().equals(""))
               number_search_strings++;
          if (!second_search_string_field.getText().equals(""))
               number_search_strings++;
          if (number_search_strings != 0)
          {
               search_strings = new String[number_search_strings];
               int j = 0;
               if (!first_search_string_field.getText().equals(""))
               {
                    search_strings[j] = first_search_string_field.getText();
                    j++;
               }
               if (!second_search_string_field.getText().equals(""))
                    search_strings[j] = second_search_string_field.getText();
          }
          
          // Update the progress bar
          progress_bar.incrementStatus();
          
          // Perform the searches and report the results
          int max_results = ((Integer) max_results_combobox.getSelectedItem()).intValue();
          JButton[] previous_buttons = {left_back_button, right_back_button};
          JButton[] next_buttons = {left_next_button, right_next_button};
          for (int i = 0; i < searchers.length; i++)
          {
               try
               {
                    if (searchers[i] != null)
                    {
                         // Prepare to hold results
                         String[][] results = null;
                         String[] number_hits = new String[1];
                         String[] query_used = new String[1];
                         
                         // Get results
                         results = searchers[i].getSearchResults(search_strings, start_ranks[i], max_results, number_hits, query_used);
                         
                         // Display results
                         if (i == 0)
                         {
                              left_results_pane.setText(searchers[i].getHTMLFormattedSearchResults(results, start_ranks[i], number_hits[0], query_used[0], searchers[i].getSeachServiceName()));
                              left_results_pane.setCaretPosition(0);
                         }
                         else if (i == 1)
                         {
                              right_results_pane.setText(searchers[i].getHTMLFormattedSearchResults(results, start_ranks[i], number_hits[0], query_used[0], searchers[i].getSeachServiceName()));
                              right_results_pane.setCaretPosition(0);
                         }
                         
                         // Update back button
                         if (start_ranks[i] == 1) previous_buttons[i].setEnabled(false);
                         else previous_buttons[i].setEnabled(true);
                         
                         // Update next buttons and current right indexes
                         boolean later_hits_exist = false;
                         int total_hits_found = Integer.parseInt(number_hits[0]);
                         int current_end_index = start_ranks[i] + results.length - 1;
                         if (current_end_index < total_hits_found) later_hits_exist = true;
                         if (i == 0) // left case
                         {
                              current_left_end = current_end_index;
                              left_next_button.setEnabled(later_hits_exist);
                         }
                         if (i == 1) // right case
                         {
                              current_right_end = current_end_index;
                              right_next_button.setEnabled(later_hits_exist);
                         }
                    }
               }
               catch (Exception e)
               {
                    // Terminate loop if is a problem that will affect all web
                    // services (in order to eliminate redundant errors)
                    if ( e.getMessage().equals("Unable to access the internet.") ||
                         e.getMessage().equals("No search strings specified in query.") )
                    {
                         JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                         
                         for (int j = 0; j < previous_buttons.length; j++)
                         {
                              previous_buttons[j].setEnabled(false);
                              next_buttons[j].setEnabled(false);
                         }
                         
                         i = searchers.length;
                         progress_bar.done();
                    }
                    
                    // Continue the loop if problem is specific to a particular
                    // web service
                    else
                    {
                         JOptionPane.showMessageDialog(null, e.getMessage(), service_names[i] + " ERROR", JOptionPane.ERROR_MESSAGE);
                         
                         previous_buttons[i].setEnabled(false);
                         next_buttons[i].setEnabled(false);
                    }
               }
               
               // Update the progress bar
               progress_bar.incrementStatus();
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
