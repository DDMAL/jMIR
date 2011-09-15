/*
 * OptionsPanel.java
 * Version 1.0
 *
 * Created on April 2, 2007, 4:40 PM
 * Last modified on July 25, 2007.
 * McGill University
 */

package jwebminer2.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import mckay.utilities.webservices.NetworkSearch;


/**
 * A GUI panel allowing users to choose settings that will govern searches.
 * The left set of options controls the way the search is parameterized. The
 * right set of options control which web services are used to perform the
 * search. The options are as follows:
 *
 * <li>The <i>Yahoo!</i> checkbox controls whether the Yahoo! REST-like Web
 * Search web services are used to perform searches. The Yahoo! Application Key
 * is hard coded into this application,because Yahoo!'s quota is 5000 searches
 * per day <i>per IP,</i> so multiple users will not reduce the quota availble
 * to each user.
 *
 * <li>The <i>Google</i> checkbox controls whether the Google SOAP web services
 * are used to perform searches. A Google distributed License Key must be
 * entered in the <i>License key</i> field for Google to bea accessed. This is
 * not hard coded into this application because Google imposes a limit of 1000
 * searches per day with a particular key, so each user needs their own key
 * to avoid reducing the quoata available to other users.
 *
 * <li>The <i>Treat strings literally</i> checkbox controls whether all search
 * queries should be literal searches (e.g. for the query "heavy metal", hits
 * must have the two words adjacent if the search is literal). This is also
 * sometimes known as an exact search or a phrase search.
 *
 * <li>The <i>Perform search as OR instead of AND</i> checkbox controls whether
 * search queries need only contain one of the specified query strings in order
 * to result in a hit. If this is true, then only one of the query words must be
 * present. If this is false, then all of them must be present (although not
 * necessarily in the specified order, unless the <i>Treat strings literally</i>
 * checkbox is selected). It is recommended that the user leave this box
 * unchecked.
 *
 * <li>The <i>Include non-matching similar hits</i> checkbox controls whether
 * results returned by search queries may include hits that do not contain one
 * or more of the specified search string(s) but do contain terms very similar
 * to them (e.g. alternative spellings).
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
 * <p>The <i>Normalize hits across web services</i> checkbox controls whether
 * the hits from different web services should be normalized before the final
 * feature scores are calculated. This ensures that services that produce fewer
 * hits overall are not underweighted in the final scores. Values are normalized
 * to the number of hits of the service with the lowest number of hits.
 *
 * <p>The <i>Normalize hits across web sites</i> checkbox controls whether
 * the hits from different web services should be normalized before the final
 * feature scores are calculated. This ensures that sites that produce fewer
 * hits overall are not underweighted in the final scores. This is separate
 * and in addition to the manual weights that can be imposed on the Site
 * Weightings panel. Values are normalized to the number of hits of the site
 * with the lowest number of hits.
 *
 * <p>The <i>Normalize feature settings</i> checkbox controls whether the array
 * of final feature values should be normalized. If the <i>By row rather than
 * overall</i> checkbox is selected then this normalization is performed on a
 * row by row basis, otherwise it is an overall normalization for the table
 * of scores. Values are normalized to 1.0.
 *
 * <li>The Co-Occurrence Scoring Function options control which formula is
 * used to calculate feature scores in the case of a co-occurrence analysis, as
 * selected on the Search Words Panel. The notation for these is explained
 * below. In brief, these are:
 *
 * <ol>
 *
 * <li>S(a1,a2) = PC(a1,a2) / (1 + (SUMcfromAcnota1(PC(a1,c)) x SUMdfromAdnota2(PC(d,a2))))
 *
 * <li>S(a1,a2, c) = (PC(a1,a2) / PC(a2)) * (1 - (|PC(a1) - PC(a2)| / PC(c))) where c is the greatest PC(A)
 *
 * </ol>
 *
 * <li>The Cross-Tabulation Scoring Function options control which formula is
 * used to calculate feature scores in the case of a cross tabulation analysis,
 * as selected on the Search Words Panel. The notation for these is explained
 * below. In brief, these are:
 *
 * <ol>
 *
 * <li>S(a,b) = PC(a,b) / PC(a)
 *
 * <li>S(a,b) = PC(a,b) / PC(b)
 *
 * <li>S(a,b) = PC(a,b) / (1 + SUMcfromA(PC(c,b)))
 *
 * <li>S(a,b) = PC(a,b) / (1 + SUMcfromB(PC(a,c)))
 *
 * </ol>
 *
 * <li>The <i>Feature scores</i> checkbox controls whether or not to display the
 * final scores that will be saved as features in the Results Panel after
 * processing is complete.
 *
 * <li>The <i>Combined processed hit counts</i> checkbox controls whether or not
 * to display the combined (added) processed hit counts for all web services and
 * sites in the Results Panel after processing is complete. This does include
 * site weighting and/or web service normalization, depending on user settings.
 *
 * <li>The <i>Combined raw hit counts</i> checkbox controls whether or not to
 * display the combined (added) raw hit counts for all  web services and sites
 * in the Results Panel after processing is complete. This does not include site
 * weighting or web service normalization.
 *
 * <li>The <i>Individual raw hit counts</i> checkbox controls whether or not to
 * display the raw hit counts for each web service and (pre-weighted) network
 * site in the Results Panel after processing is complete.
 *
 * <li>The <i>Search queries used</i> checkbox controls whether or not to
 * display the queries actually sent to the web services in the Results Panel
 * after processing is complete. Note that these queries will not include
 * nformation that was stored in the actual web service objects themselves.
 *
 * <li>The <i>Search settings used</i> checkbox controls whether or not to
 * display the details of all of the settings used to perform searches in the
 * Results Panel after processing is complete.
 *
 * <p>The notation used for the scoring functions is as follows:
 *
 * <li>S() refers to the output scoring function.
 *
 * <li>a (from set A) refers to an element from the Primary Search Strings field
 * in the Search Words Panel.
 *
 * <li>b (from set B) refers to an element from the Secondary Search Strings
 * field in the Search Words Panel.
 *
 * <li>C() refers to the combined hit counts accross web services and weighted
 * network sites. If only one web service is used and only one site (including
 * the possibility of the whole network) is used, then this is just the raw
 * hit counts.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class OptionsPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     /**
      * Whether or not to access the Yahoo! web services.
      */
     private   JCheckBox      use_yahoo_checkbox;

     /**
      * Whether or not to access the Google web services.
      */
     private   JCheckBox      use_google_checkbox;

     /**
      * The Google License needed to access Google's SOAP web services.
      */
     private   JTextField     google_license_field;

     /**
      * Whether or not to access the Last.FM web services.
      */
     private   JCheckBox      use_lastfm_checkbox;

     /**
      * The license needed to access Last.FM's web services.
      */
     private   JTextField     lastfm_license_field;

     /**
      * Whether the query strings are to be searched literally. If so, all
      * words must occur exactly as they appear and in the same order as
      * specified to result in a hit. This is also sometimes known as an exact
      * search or a phrase search.
      */
     private   JCheckBox      literal_search_checkbox;

     /**
      * Whether the query strings are searched based on a boolean OR or AND.
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
      * Exclude hits that to not belong to the specified language. An entry of
      * "No Limitations" places no language-based restrictions.
      */
     private   JComboBox      limit_to_language_combobox;

     /**
      * Exclude hits that are not located in the specified country. An entry of
      * "No Limitations" places no location-based restrictions.
      */
     private   JComboBox      limit_to_country_combobox;

     /**
      * Perform the search using services located in the specified country, if
      * possible (i.e. where the search service is located). Results are not
      * limited to this country.An entry of "No Limitations" causes the default
      * service to be used.
      */
     private   JComboBox      searching_from_region_combobox;

     /**
      * Only search for files of the specified type (extension). An entry of
      * "No Limitations" places no file type-based restrictions.
      */
     private   JComboBox      limit_to_file_type_combobox;

     /**
      * Whether the hits from web services should be normalized before the
      * final feature scores are calculated. This ensures that services that
      * produce fewer hits overall are not underweighted in the final scores.
      */
     private   JCheckBox      normalize_across_web_services_checkbox;

     /**
      * Whether the hits from different web services should be normalized before
      * the final feature scores are calculated. This ensures that sites that
      * produce fewer hits overall are not underweighted in the final scores.
      * This is separate and in addition to the manual weights that can be
      * imposed on the Site* Weightings panel.
      */
     private   JCheckBox      normalize_across_web_sites_checkbox;

     /**
      * Whether final feature scores should be normalized when reported and
      * saved.
      */
     private   JCheckBox      normalize_scores_checkbox;

     /**
      * Whether final feature scores should be normalized when reported and
      * saved by row rather than overall (if normalize_scores_checkbox
      * is selected).
      */
     private   JCheckBox      normalize_scores_by_row_instead_of_overall_checkbox;

     /**
      * The scoring function S(a1,a2) = PC(a1,a2) / (1 + (SUMcfromAcnota1(PC(a1,c)) x SUMdfromAdnota2(PC(d,a2)))).
      */
     private   JRadioButton   cooccurrence_function_1_button;

     /**
      * The scoring function S(a1,a2, c) = (PC(a1,a2) / PC(a2)) * (1 - (|PC(a1) - PC(a2)| / PC(c)))
      * where c is the greatest PC(A).
      */
     private   JRadioButton   cooccurrence_function_2_button;

     /**
      * The scoring function S(a,b) = PC(a,b) / PC(a).
      */
     private   JRadioButton   cross_tab_function_1_button;

     /**
      * The scoring function S(a,b) = PC(a,b) / PC(b).
      */
     private   JRadioButton   cross_tab_function_2_button;

     /**
      * The scoring function S(a,b) = PC(a,b) / (1 + SUMcfromA(PC(c,b))).
      */
     private   JRadioButton   cross_tab_function_3_button;

     /**
      * The scoring function S(a,b) = PC(a,b) / (1 + SUMcfromB(PC(a,c))).
      */
     private   JRadioButton   cross_tab_function_4_button;

     /**
      * Whether or not to display the final scores that will be saved as
      * features in the Results Panel after processing is complete.
      */
     private   JCheckBox      display_feature_scores_checkbox;

     /**
      * Whether or not to display the combined (added) processed hit counts for
      * all web services and sites in the Results Panel after processing is
      * complete. This does include site weighting and/or web service
      * normalization, depending on user settings.
      */
     private   JCheckBox      display_combined_processed_counts_checkbox;

     /**
      * Whether or not to display the combined (added) raw hit counts for all
      * web services and sites in the Results Panel after processing is
      * complete. This does not include site weighting or web service
      * normalization.
      */
     private   JCheckBox      display_combined_raw_counts_checkbox;

     /**
      * Whether or not to display the raw hit counts for each web service and
      * (pre-weighted) network site in the Results Panel after processing is
      * complete.
      */
     private   JCheckBox      display_individual_raw_counts_checkbox;

     /**
      * Whether or not to display the queries actually sent to the web services
      * in the Results Panel after processing is complete. Note that these
      * queries will not include information that was stored in the actual
      * web service objects themselves.
      */
     private   JCheckBox      display_queries_used_checkbox;

     /**
      * Whether or not to display the details of all of the settings used to
      * perform searches in the Results Panel after processing is complete.
      */
     private   JCheckBox      display_search_settings_used_checkbox;


     /**
      * The Yahoo! Application ID needed to access Yahoo's web services.
      */
     private   static String    YAHOO_APPLICATION_ID = "coB91_PV34EPwpMeMjk2Jadyv4_NrWRf_4M7N_qXJNpHptn.vi_QDEf.oBqSBy0v";

     /**
      * The code identifying the scorring function
      * S(a1,a2) = PC(a1,a2) / (1 + (SUMcfromAcnota1(PC(a1,c)) x SUMdfromAdnota2(PC(d,a2))))
      */
     public    static final int    COOCCURRENCE_FUNCTION_1_CODE = 10;

     /**
      * The code identifying the scorring function
      * S(a1,a2, c) = (PC(a1,a2) / PC(a2)) * (1 - (|PC(a1) - PC(a2)| / PC(c)))
      * where c is the greatest PC(A).
      */
     public    static final int    COOCCURRENCE_FUNCTION_2_CODE = 20;

     /**
      * The code identifying the scorring function
      * S(a,b) = PC(a,b) / PC(a)
      */
     public    static final int    CROSS_TAB_FUNCTION_1_CODE = 100;

     /**
      * The code identifying the scorring function
      * S(a,b) = PC(a,b) / PC(b)
      */
     public    static final int    CROSS_TAB_FUNCTION_2_CODE = 200;

     /**
      * The code identifying the scorring function
      * S(a,b) = PC(a,b) / (1 + SUMcfromA(PC(c,b)))
      */
     public    static final int    CROSS_TAB_FUNCTION_3_CODE = 300;

     /**
      * The code identifying the scorring function
      * S(a,b) = PC(a,b) / (1 + SUMcfromB(PC(a,c)))
      */
     public    static final int    CROSS_TAB_FUNCTION_4_CODE = 400;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates a new instance of OptionsPanel, lays it out and sets GUI
      * elements to their defaults.
      */
     public OptionsPanel()
     {
          // Prepare layout
          initializeLayout();

          // Set GUI parameters to their defaults
          use_yahoo_checkbox.setSelected(true);
          use_google_checkbox.setSelected(false);
		  use_google_checkbox.setEnabled(false); // This option is disabled due to the deprecation of the Google SOAP API, and pending adaptation of Google's new web services
          google_license_field.setText("");
		  google_license_field.setEnabled(false); // This option is disabled due to the deprecation of the Google SOAP API, and pending adaptation of Google's new web services
          use_lastfm_checkbox.setSelected(true);
          lastfm_license_field.setText("b25b959554ed76058ac220b7b2e0a026"); // license key provided by Last.FM for online testing only. Should be used an original license key.
          literal_search_checkbox.setSelected(true);
          any_not_all_search_strings_checkbox.setSelected(false);
          include_hits_with_similar_search_strings_checkbox.setSelected(false);
          suppress_similar_hits_checkbox.setSelected(false);
          suppress_adult_content_checkbox.setSelected(false);
          limit_to_language_combobox.setSelectedIndex(0);
          limit_to_country_combobox.setSelectedIndex(0);
          searching_from_region_combobox.setSelectedIndex(0);
          limit_to_file_type_combobox.setSelectedIndex(0);
          normalize_across_web_services_checkbox.setSelected(true);
          normalize_across_web_sites_checkbox.setSelected(true);
          normalize_scores_checkbox.setSelected(true);
          normalize_scores_by_row_instead_of_overall_checkbox.setSelected(true);
          cooccurrence_function_1_button.setSelected(true);
          cross_tab_function_3_button.setSelected(true);
          display_feature_scores_checkbox.setSelected(true);
          display_combined_processed_counts_checkbox.setSelected(false);
          display_combined_raw_counts_checkbox.setSelected(false);
          display_individual_raw_counts_checkbox.setSelected(false);
          display_queries_used_checkbox.setSelected(false);
          display_search_settings_used_checkbox.setSelected(true);

          // Validate settings
          updateBasedOnCheckBoxes(use_google_checkbox);
          updateBasedOnCheckBoxes(use_lastfm_checkbox);
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the use_google_checkbox
          if (event.getSource().equals(use_google_checkbox))
               updateBasedOnCheckBoxes(use_google_checkbox);

          // React to the use_lastfm_checkbox
          if (event.getSource().equals(use_lastfm_checkbox))
               updateBasedOnCheckBoxes(use_lastfm_checkbox);

          // React to the normalize_scores_checkbox
          if (event.getSource().equals(normalize_scores_checkbox))
               updateBasedOnCheckBoxes(normalize_scores_checkbox);
     }


     /**
      * Return the Yahoo! Application ID neeeded to access Yahoo!'s web
      * services. Returns null if Yahoo is not to be searched. The Application
      * ID is hard coded into jWebMiner.
      *
      * @return     Yahoo! Application ID if a Yahoo! search is to be performed
      *             and null if it is not.
      */
     public String getYahooApplicationID()
     {
          if (!use_yahoo_checkbox.isSelected())
               return null;
          else
               return YAHOO_APPLICATION_ID;
     }


     /**
      * Return the Google License Key neeeded to access Google's web
      * services. Returns null if Google is not to be searched. The License Key
      * is accessed from the Google License Key field of this Options Panel.
      *
      * @return     Google License Key if a Google search is to be performed
      *             and null if it is not.
      * @throws Exception     Throws an informative exception if a Google search
      *                       is requested but no License Key is entered.
      */
     public String getGoogleLicenseKey()
     throws Exception
     {
          if (!use_google_checkbox.isSelected())
               return null;
          else
          {
               if (google_license_field.getText().equals(""))
                    throw new Exception("Google search requested but no Google License Key specified.\n\n" +
                         "This License Key may be entered in the Options Panel.");
               return google_license_field.getText();
          }
     }

     /**
      * Return the Last.FM License Key neeeded to access Last.FM's web
      * services. Returns null if Last.FM is not to be searched. The License Key
      * is accessed from the Last.FM License Key field of this Options Panel.
      *
      * @return     Last.FM License Key if a Last.FM search is to be performed
      *             and null if it is not.
      * @throws Exception     Throws an informative exception if a Last.FM search
      *                       is requested but no License Key is entered.
      */
     public String getLastFMLicenseKey()
     throws Exception
     {
          if (!use_lastfm_checkbox.isSelected())
               return null;
          else
          {
               if (lastfm_license_field.getText().equals(""))
                    throw new Exception("Last.FM search requested but no Last.FM License Key specified.\n\n" +
                         "This License Key may be entered in the Options Panel.");
               return lastfm_license_field.getText();
          }
     }


     /**
      * Return true if and only if the Last.FM box is checked.
      *
      * @return     The status of the Last.FM checkbox.
      * @throws Exception     Throws an informative exception if a Last.FM search
      *                       is requested but no License Key is entered.
      */
     public boolean getLastFMEnabled()
     throws Exception
     {
          return(use_lastfm_checkbox.isSelected());
     }


     /**
      * Returns whether the <i>Treat search strings literally</i> checkbox is
      * selected. This checkbox controls whether query strings are to be
      * searched literally. If so, all words must occur exactly as they appear
      * and in the same order as specified to result in a hit. This is also
      * sometimes known as an exact search or a phrase search.
      *
      * @return     Whether the checkbox is selected.
      */
     public boolean getIsLiteralSearch()
     {
          return literal_search_checkbox.isSelected();
     }


     /**
      * Returns whether the <i>Perform search as OR instead of AND</i> checkbox
      * is selected. This checkbox controls whether the query strings are
      * searched based on a boolean OR or AND. In the OR case, hits will occur
      * if some but not all of the search strings are present and in the AND
      * case  hits will only occur if all search words are present.
      *
      * @return     Whether the checkbox is selected.
      */
     public boolean getIsOrSearch()
     {
          return any_not_all_search_strings_checkbox.isSelected();
     }


     /**
      * Returns whether the <i>Include non-matching similar hits</i> checkbox is
      * selected. This checkbox controls whether hits may be included that do
      * not contain one or more of the specified search string(s) but do contain
      * terms very similar to them (e.g. alternative spellings).
      *
      * @return     Whether the checkbox is selected.
      */
     public boolean getIncludeNonMatchingSimilarHits()
     {
          return include_hits_with_similar_search_strings_checkbox.isSelected();
     }


     /**
      * Returns whether the <i>Suppress similar hits</i> checkbox is selected.
      * This checkbox controls whether to suppress similar hits, namely ones
      * with identical titles and descriptions or multiple hits from the same
      * host.
      *
      * @return     Whether the checkbox is selected.
      */
     public boolean getSuppressSimilarHits()
     {
          return suppress_similar_hits_checkbox.isSelected();
     }


     /**
      * Returns whether the <i>Suppress adult content</i> checkbox is selected.
      * This checkbox controls whether to suppress hits that are classified as
      * adult by the search service.
      *
      * @return     Whether the checkbox is selected.
      */
     public boolean getSuppressAdultContent()
     {
          return suppress_adult_content_checkbox.isSelected();
     }


     /**
      * Returns the contents of the <i>Limit to language</i> combo box. Excludes
      * hits that to not belong to the specified language. An entry of "No
      * Limitations" places no language-based restrictions.
      *
      * @return     Null or the contents of the combo box.
      */
     public String getLanguageFilter()
     {
          return (String) limit_to_language_combobox.getSelectedItem();
     }


     /**
      * Returns the contents of the <i>Limit to contry</i> combo box. Excludes
      * hits that are not located in the specified country. An entry of "No
      * Limitations" places no location-based restrictions.
      *
      * @return     Null or the contents of the combo box.
      */
     public String getCountryFilter()
     {
          return (String) limit_to_country_combobox.getSelectedItem();
     }


     /**
      * Returns the contents of the <i>Search from region</i> combo box.
      * Performs the search using services located in the specified country, if
      * possible (i.e. where the search service is located). Results are not
      * limited to this country. An entry of "No Limitations" causes the default
      * service to be used.
      *
      * @return     Null or the contents of the combo box.
      */
     public String getRegionToSearchFrom()
     {
          return (String) searching_from_region_combobox.getSelectedItem();
     }


     /**
      * Returns the contents of the <i>Limit to file type</i> combo box. Only
      * searches for files of the specified type (extension). An entry of
      * "No Limitations" places no file type-based restrictions.
      *
      * @return     Null or the contents of the combo box.
      */
     public String getFileTypeFilter()
     {
          return (String) limit_to_file_type_combobox.getSelectedItem();
     }


     /**
      * Returns whether the hits from web services should be normalized before
      * the final feature scores are calculated. This ensures that services that
      * produce fewer hits overall are not underweighted in the final scores.
      *
      * @return     Whether or not to normalize.
      */
     public boolean getShouldNormalizeAcrossWebServices()
     {
          return normalize_across_web_services_checkbox.isSelected();
     }


     /**
      * Returns whether the hits from different web services should be
      * normalized before the final feature scores are calculated. This ensures
      * that sites that produce fewer hits overall are not underweighted in the
      * final scores.This is separate and in addition to the manual weights that
      * can be imposed on the Site Weightings panel.
      *
      * @return     Whether or not to normalize.
      */
     public boolean getShouldNormalizeAcrossWebSites()
     {
          return normalize_across_web_sites_checkbox.isSelected();
     }


     /**
      * Returns a 2-D array indicating whether (first entry) the array of final
      * feature values should be normalized and, if so, whether (second entry)
      * this normalization should be performed on a row by row basis (true) or
      * as an overall normalization (false) for the table of scores.
      *
      * @return     The 2-D array.
      */
     public boolean[] getShouldNormalizeScores()
     {
          boolean[] results = {normalize_scores_checkbox.isSelected(), normalize_scores_by_row_instead_of_overall_checkbox.isSelected()};
          return results;
     }


     /**
      * Returns the code for the scoring function selected. The
      * is_cross_tabulation parameter indicates whether the code should be
      * returned for the co-occurrence group of for the cross tabulation group.
      *
      * @param is_cross_tabulation      If this is true then a cross tabulation
      *                                 analysis is to be performed, as
      *                                 opposed to a simple co-occurrence
      *                                 analysis.
      * @return                         Either COOCCURRENCE_FUNCTION_1_CODE,
      *                                 COOCCURRENCE_FUNCTION_2_CODE,
      *                                 CROSS_TAB_FUNCTION_1_CODE,
      *                                 CROSS_TAB_FUNCTION_2_CODE,
      *                                 CROSS_TAB_FUNCTION_3_CODE or
      *                                 CROSS_TAB_FUNCTION_1_CODE.
      */
     public int getScoringFunction(boolean is_cross_tabulation )
     {
          if (!is_cross_tabulation)
          {
               if (cooccurrence_function_1_button.isSelected())
                    return COOCCURRENCE_FUNCTION_1_CODE;
               if (cooccurrence_function_2_button.isSelected())
                    return COOCCURRENCE_FUNCTION_2_CODE;
          }
          else
          {
               if (cross_tab_function_1_button.isSelected())
                    return CROSS_TAB_FUNCTION_1_CODE;
               if (cross_tab_function_2_button.isSelected())
                    return CROSS_TAB_FUNCTION_2_CODE;
               if (cross_tab_function_3_button.isSelected())
                    return CROSS_TAB_FUNCTION_3_CODE;
               if (cross_tab_function_4_button.isSelected())
                    return CROSS_TAB_FUNCTION_4_CODE;
          }
          return -1;
     }


     /**
      * Returns an array detailing which reports should be displayed in the
      * Results Panel. These appear in the following order:
      *
      * <li>display_feature_scores
      * <li>display_combined_raw_counts
      * <li>display_individual_raw_counts
      * <li>display_queries_used
      * <li>display_search_settings_used
      * <li>display_combined_processed_counts_checkbox
      *
      * @return     What data should be displayed.
      */
     public boolean[] getReportOptions()
     {
          boolean[] results =
          {
               display_feature_scores_checkbox.isSelected(),
               display_combined_raw_counts_checkbox.isSelected(),
               display_individual_raw_counts_checkbox.isSelected(),
               display_queries_used_checkbox.isSelected(),
               display_search_settings_used_checkbox.isSelected(),
               display_combined_processed_counts_checkbox.isSelected()
          };

          return results;
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Initializes the layout of the GUI.
      */
     private void initializeLayout()
     {
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;

          // Initialize left options
          use_yahoo_checkbox = new JCheckBox("Yahoo!");
          use_google_checkbox = new JCheckBox("Google");
          google_license_field = new JTextField(18);
          use_lastfm_checkbox = new JCheckBox("Last.FM");
          lastfm_license_field = new JTextField(32);
          literal_search_checkbox = new JCheckBox("Treat search strings literally");
          any_not_all_search_strings_checkbox = new JCheckBox("Perform search as OR instead of AND");
          include_hits_with_similar_search_strings_checkbox  = new JCheckBox("Include non-matching similar hits");
          suppress_similar_hits_checkbox = new JCheckBox("Suppress similar hits");
          suppress_adult_content_checkbox = new JCheckBox("Suppress adult content");
          limit_to_language_combobox = new JComboBox(NetworkSearch.included_languages);
          JPanel limit_to_language_panel = getLabeledContainer( "Limit to language:",
               limit_to_language_combobox,
               horizontal_gap );
          limit_to_country_combobox = new JComboBox(NetworkSearch.included_countries);
          JPanel limit_to_country_panel = getLabeledContainer( "Limit to country:",
               limit_to_country_combobox,
               horizontal_gap );
          limit_to_file_type_combobox = new JComboBox(NetworkSearch.included_file_types);
          JPanel limit_to_file_type_panel = getLabeledContainer( "Limit to file type:",
               limit_to_file_type_combobox,
               horizontal_gap );
          searching_from_region_combobox = new JComboBox(NetworkSearch.included_countries);
          JPanel searching_from_region_panel = getLabeledContainer( "Search from region:",
               searching_from_region_combobox,
               horizontal_gap );

          // Add left options
          JPanel left_panel = new JPanel(new GridLayout(19, 1, horizontal_gap, vertical_gap));
          left_panel.add(new JLabel("WEB SERVICES TO SEARCH:"));
          left_panel.add(use_yahoo_checkbox);
          left_panel.add(getCheckboxTextfieldCombo(use_google_checkbox, "License key:", google_license_field, horizontal_gap, vertical_gap));
          left_panel.add(new JLabel(""));
          left_panel.add(new JLabel("Last.FM (for tag-based artist cross tabulation only):"));
          left_panel.add(getCheckboxTextfieldCombo(use_lastfm_checkbox, "License key:", lastfm_license_field, horizontal_gap, vertical_gap));
          left_panel.add(new JLabel(""));
          left_panel.add(new JLabel("GENERAL SEARCH SETTINGS:"));
          left_panel.add(literal_search_checkbox);
          left_panel.add(any_not_all_search_strings_checkbox);
          left_panel.add(include_hits_with_similar_search_strings_checkbox);
          left_panel.add(suppress_similar_hits_checkbox);
          left_panel.add(suppress_adult_content_checkbox);
          left_panel.add(new JLabel(""));
          left_panel.add(new JLabel("LANGUAGE, REGIONAL AND FILE TYPE FILTERS:"));
          left_panel.add(limit_to_language_panel);
          left_panel.add(limit_to_country_panel);
          left_panel.add(searching_from_region_panel);
          left_panel.add(limit_to_file_type_panel);

          // Initialize right options
          normalize_across_web_services_checkbox = new JCheckBox("Normalize hits across web services");
          normalize_across_web_sites_checkbox = new JCheckBox("Normalize hits across web sites");
          normalize_scores_checkbox = new JCheckBox("Normalize feature scores");
          normalize_scores_checkbox.addActionListener(this);
          normalize_scores_by_row_instead_of_overall_checkbox = new JCheckBox("By row rather than overall");
          JPanel normalize_scores_by_row_instead_of_overall_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          normalize_scores_by_row_instead_of_overall_panel.add(new JLabel("      "), BorderLayout.WEST);
          normalize_scores_by_row_instead_of_overall_panel.add(normalize_scores_by_row_instead_of_overall_checkbox, BorderLayout.CENTER);
          cooccurrence_function_1_button = new JRadioButton("S(a1,a2) = C(a1,a2) / (1 + (SUM(C(a1,c)) x SUM(C(d,a2))))");
          cooccurrence_function_2_button = new JRadioButton("S(a1,a2, c) = (C(a1,a2) / C(a2)) * (1 - (|C(a1) - C(a2)| / C(c)))");
          ButtonGroup cooccooccurrence_function_group = new ButtonGroup();
          cooccooccurrence_function_group.add(cooccurrence_function_1_button);
          cooccooccurrence_function_group.add(cooccurrence_function_2_button);
          cross_tab_function_1_button = new JRadioButton("S(a,b) = C(a,b) / C(a)");
          cross_tab_function_2_button = new JRadioButton("S(a,b) = C(a,b) / C(b)");
          cross_tab_function_3_button = new JRadioButton("S(a,b) = C(a,b) / (1 + SUM(C(c,b)))");
          cross_tab_function_4_button = new JRadioButton("S(a,b) = C(a,b) / (1 + SUM(C(a,c)))");
          ButtonGroup cros_tab_function_group = new ButtonGroup();
          cros_tab_function_group.add(cross_tab_function_1_button);
          cros_tab_function_group.add(cross_tab_function_2_button);
          cros_tab_function_group.add(cross_tab_function_3_button);
          cros_tab_function_group.add(cross_tab_function_4_button);
          display_feature_scores_checkbox = new JCheckBox("Feature scores");
          display_combined_processed_counts_checkbox = new JCheckBox("Combined processed hit counts");
          display_combined_raw_counts_checkbox = new JCheckBox("Combined raw hit counts");
          display_individual_raw_counts_checkbox = new JCheckBox("Individual raw hit counts");
          display_queries_used_checkbox = new JCheckBox("Search queries used");
          display_search_settings_used_checkbox = new JCheckBox("Search settings used");

          // Add right options
          JPanel right_panel = new JPanel(new GridLayout(23, 1, horizontal_gap, vertical_gap));
          right_panel.add(new JLabel("FEATURE SCORE CALCULATION SETTINGS:"));
          right_panel.add(normalize_across_web_services_checkbox);
          right_panel.add(normalize_across_web_sites_checkbox);
          right_panel.add(normalize_scores_checkbox);
          right_panel.add(normalize_scores_by_row_instead_of_overall_panel);
          right_panel.add(new JLabel(""));
          right_panel.add(new JLabel("CO-OCCURRENCE SCORING FUNCTION:"));
          right_panel.add(cooccurrence_function_1_button);
          right_panel.add(cooccurrence_function_2_button);
          right_panel.add(new JLabel(""));
          right_panel.add(new JLabel("CROSS TABULATION SCORING FUNCTION:"));
          right_panel.add(cross_tab_function_1_button);
          right_panel.add(cross_tab_function_2_button);
          right_panel.add(cross_tab_function_3_button);
          right_panel.add(cross_tab_function_4_button);
          right_panel.add(new JLabel(""));
          right_panel.add(new JLabel("INFORMATION TO REPORT:"));
          right_panel.add(display_feature_scores_checkbox);
          right_panel.add(display_combined_processed_counts_checkbox);
          right_panel.add(display_combined_raw_counts_checkbox);
          right_panel.add(display_individual_raw_counts_checkbox);
          right_panel.add(display_queries_used_checkbox);
          right_panel.add(display_search_settings_used_checkbox);

          // Set up scrolling
          JScrollPane left_scroll_pane = new JScrollPane( left_panel,
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          JScrollPane right_scroll_pane = new JScrollPane( right_panel,
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

          // Add borders
          JPanel left_scroll_panel = new JPanel(new GridLayout(1, 1));
          JPanel right_scroll_panel = new JPanel(new GridLayout(1, 1));
          left_scroll_panel.add(left_scroll_pane);
          right_scroll_panel.add(right_scroll_pane);
          JPanel outer_panel = new JPanel(new GridLayout(1, 2));
          outer_panel.add(left_scroll_panel);
          outer_panel.add(right_scroll_panel);
          JPanel outer_panel_outer = new JPanel(new GridLayout(1, 1));
          outer_panel.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, vertical_gap, horizontal_gap));
          left_scroll_panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, horizontal_gap));
          right_scroll_panel.setBorder(BorderFactory.createEmptyBorder(0, horizontal_gap, 0, 0));
          left_scroll_pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          left_panel.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, vertical_gap, horizontal_gap));
          right_scroll_pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          right_panel.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, vertical_gap, horizontal_gap));

          // Add layout to overall panel
          setLayout(new GridLayout(1, 1));
          add(outer_panel);
     }


     /**
      * Enable or disable components based on the value of a check box.
      *
      * @param check_box The check box to base the enabling on.
      */
     private void updateBasedOnCheckBoxes(JCheckBox check_box)
     {
          if (check_box == use_google_checkbox)
          {
               if (use_google_checkbox.isSelected())
                    google_license_field.setEnabled(true);
               else google_license_field.setEnabled(false);
          }
          if (check_box == use_lastfm_checkbox)
          {
               if (use_lastfm_checkbox.isSelected())
                    lastfm_license_field.setEnabled(true);
               else lastfm_license_field.setEnabled(false);
          }
          if (check_box == normalize_scores_checkbox)
          {
               if (normalize_scores_checkbox.isSelected())
                    normalize_scores_by_row_instead_of_overall_checkbox.setEnabled(true);
               else normalize_scores_by_row_instead_of_overall_checkbox.setEnabled(false);
          }
     }


     /**
      * Returns a JPanel with check_box on the left, a JLabel filled with
      * label in the middle and text_field on the right. The latter two are
      * right justified.
      *
      * <p>An ActionListener is also attached to the given check_box.
      *
      * @param check_box      The JCheckBox to add to the panel.
      * @param label          The label to give text_field.
      * @param text_field     The JTextField to add to the panel.
      * @param horizontal_gap The horizontal gap between components.
      * @param vertical_gap   The vertical gap between components.
      * @return               The constructed JPanel.
      */
     private JPanel getCheckboxTextfieldCombo(JCheckBox check_box, String label,
          JTextField text_field, int horizontal_gap, int vertical_gap)
     {
          check_box.addActionListener(this);

          JPanel panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          panel.add(check_box, BorderLayout.CENTER);
          JPanel right_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          right_panel.add(new JLabel(label), BorderLayout.WEST);
          right_panel.add(text_field, BorderLayout.CENTER);
          panel.add(right_panel, BorderLayout.EAST);
          return panel;
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
