/*
 * AnalysisProcessor.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2;

import java.lang.StringBuffer;
import java.util.LinkedList;
import javax.swing.JFrame;
import mckay.utilities.general.HTMLWriter;
import mckay.utilities.gui.progressbars.DoubleProgressBarTaskCoordinator;
import mckay.utilities.staticlibraries.ArrayMethods;
import mckay.utilities.webservices.*;
import jwebminer2.gui.OptionsPanel;
import jwebminer2.gui.OuterFrame;
import net.roarsoftware.lastfm.*;
import java.util.Collection;


/**
 * The class that performs web searches, calculates corresponding statistics
 * makes the results available.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class AnalysisProcessor
     implements mckay.utilities.gui.progressbars.ProcessCoordinator
{
     /* FIELDS ****************************************************************/


     /**
      * The window calling this.
      */
     private   OuterFrame      parent_window;

     /**
      * Cross tabulation is to performed if this is true and co-occurrence is to
      * be performed if it is false.
      */
     static   boolean        is_cross_tabulation;

     /**
      * The code indicating the scoring function that is to be used. One of
      * the final codes in the OptionsPanel class.
      */
     private   int            scoring_function_code;

     /**
      * Whether the hits from web services should be normalized before the
      * frequency_scores field is calculated. This ensures that services that
      * produce fewer hits overall are not underweighted in the final scores.
      */
     private   boolean        normalize_across_web_services;

     /**
      * Whether the hits from different web services should be normalized before
      * the final feature scores are calculated. This ensures that sites that
      * produce fewer hits overall are not underweighted in the  final scores.
      * This is separate and in addition to the manual weights that can be
      * imposed on the Site Weightings panel.
      */
     private   boolean        normalize_across_sites;

     /**
      * An array indicating whether (first entry) the array of final feature
      * values should be normalized and, if so, whether (second entry) this
      * normalization should be performed on a row by row basis (true) or as an
      * overall normalization (false) for the table of scores.
      */
     private   boolean[]      should_normalize_scores;

     /**
      * The Yahoo Application ID that is needed to access Yahoo web services. If
      * this is null then Yahoo will not be searched.
      */
     static   String         yahoo_application_id;

     /**
      * The Google License Key that is needed to access Google web services. If
      * this is null then Google will not be searched.
      */
     static   String         google_license_key;

     /**
      * Whether or not the Last.FM report should be generated.
      */
     static    boolean        lastfm_enabled;

     /**
      * The Last.FM License Key that is needed to access Last.FM web services. If
      * this is null then Last.fm will not be searched. By default, the sample
      * license key for online testing is given.
      */
     private   String         lastfm_license_key;

     /**
      * The Last.FM report table stating the ranking of the queried tag in the
      * in the multiples tags of an artist, staring from 1.
      */
     private   String[][]     lastfm_report_table;

     /**
      * The final normalized score for the Last.FM query. The actual values of
      * the position of each genre in the tag list are scaled between 0 and 1.
      */
     private   String[][]     lastfm_report_table_norm;

     /**
      * The LastFM and websearch report scores formatted into one table to be
      * saved as the desired format by FeatureValueFileSaver.
      */
     public    double[][]     lastfm_websearch_report_table_norm;

     /**
      * Whether all search queries should be literal searches (e.g. for the
      * query "heavy metal", hits must have the two words adjacent if the search
      * is literal). This is also sometimes known as an exact search or a phrase
      * search.
      */

     private   String[][]     lastfm_full_tags_table;

     /**
      * Report LastFM normalized table with columns
      */

     private   String[][]     lastfm_report_table_norm_with_columns;



     /**
      * The string of the final scores representing the averaging of the LastFM
      * normalizedresults and Websearch resulting using co-ocurrence or
      * crosstabulation.
      */
     private   String[][]     averaged_websearch_lastfm;



     private   boolean        literal_search;

     /**
      * Whether search queries need only contain one of the specified query
      * words in order to result in a hit. If this is true, then only one of the
      * query strings must be present. If this is false, then all of them must
      * be present (although not necessarily in the specified order, unless the
      * literal_search field is true).
      */
     private   boolean        or_based_search;

     /**
      * Whether results returned by search queries performed may include hits
      * that do not contain one or more of the specified search string(s) but do
      * contain terms very similar to them (e.g. alternative spellings).
      */
     private   boolean        include_non_matching;

     /**
      * Whether to suppress similar hits when reporting results. Similar in this
      * context means either:
      *
      * <ul><li>Sites with identical titles and/or descriptions.</li>
      * <li>Multiple hits from the same host.</li></ul>
      */
     private   boolean        suppress_similar_hits;

     /**
      * Whether to suppress hits that are classified as containing adult
      * content by the search service in question.
      */
     private   boolean        suppress_adult_content;

     /**
      * Name of a language that hits must be in in order to be included in
      * search results. A value of "No Limitations" means that any language is
      * permissible. A value of null is not permitted.
      */
     private   String         limit_to_language;

     /**
      * Name of a country that sites must be in in order to be included in
      * search results. A value of "No Limitations" means that sites in any
      * country are permissible. A value of null is not permitted.
      */
     private   String         limit_to_country;

     /**
      * Name of a country where the search will be performed (i.e. where
      * the search service is located). Results are not limited to this country.
      * An entry of "No Limitations" causes the default service location to be
      * used. A value of null is not permitted.
      */
     private   String         region_to_search_from;

     /**
      * A file extension a document must have in order to be returned as a hit
      * in search results. An entry of "No Limitations" means that file type
      * will not by used to filter results. A value of null is not permitted.
      */
     private   String         limit_to_file_type;

     /**
      * An array of strings that may not be present on any pages for them to be
      * counted as hits (i.e filter strings). These excluded strings are each
      * treated as literal (i.e. words must appear in the same order). There is
      * one entry for each such string. This is null if there are no exclusion
      * strings.
      */
     private   String[]       excluded_filter_strings;

     /**
      * Strings that must be present on a page in order for it to be counted as
      * a hit. These are in addition to the search strings themselves, and these
      * required filter strings apply to all searches. These are earch treated
      * as literal (i.e. words must appear in the same order). A value of null
      * means that none of these are present. Pattern-based search phrases are
      * NOT present here, and are rather found in the
      * required_filter_strings_pattern_based field.
      */
     private   String[]       required_filter_strings_basic;

     /**
      * Strings that must be present on a page in order for it to be counted as
      * a hit. These are in addition to the search strings themselves, and these
      * required filter strings apply to all searches. These are earch treated
      * as literal (i.e. words must appear in the same order). A value of null
      * means that none of these are present. These are all pattern-based search
      * phrases. This means that during searches the corresponding filter string
      * for each search will have the particular string from search_strings_a or
      * search_strings_b used to replace the <PRIMARY_SEARCH_STRING> or
      * <SECONDARY_SEARCH_STRING> tag, respectively during searches. In the case
      * of co-occurrence searches (as opposed to cross tabulation searches), the
      * <SECONDARY_SEARCH_STRING> tag is replaced by another string from
      * search_strings_a rather than a string from search_strings_b.
      *
      * <p>The first dimension of the array corresponds to different phrases.
      * The second dimension corresponds to different parts of each phrase.
      * These may include fixed text that is the same for all search strings, or
      * may consist of either <PRIMARY_SEARCH_STRING> or
      * <SECONDARY_SEARCH_STRING> tags that indicate that a substitution is
      * needed.
      *
      * <p>If synonyms are present, only the first one is used to replace
      * <PRIMARY_SEARCH_STRING> or <SECONDARY_SEARCH_STRING>.
      *
      * <p>Basic, non-pattern-based required filter strings are found in the
      * required_filter_strings_basic field, not here.
      */
     private   String[][]     required_filter_strings_pattern_based;

     /**
      * Network sites that will be separately and exclusively searched in all
      * search queries performed by this object. All spaces are stripped and
      * the strings are UTF-8 encoded. A value of null for an entry corresponds
      * to the entire network. This field itself is never null. Indices
      * correspond to site_weightings.
      */
     private   String[]       site_addresses;

     /**
      * Weights for the network sites specified in site_addresses. Indices
      * correspond to those in site_addresses. Values range from 0.0 to 1.0 and
      * are normalized.
      */
     private   double[]       site_weightings;

     /**
      * The first set of search strings that are used in both co-occurrence
      * and cross tabulation experiments. The first dimension corresponds to
      * the index of the query. The second dimension allows the specification
      * of synonyms that are equivalent and should be combined as OR terms
      * in each search.
      */
     private   String[][]     search_strings_a;

     /**
      * The second set of search strings that are used only in cross tabulation
      * experiments. Null if a co-occurence experiment is to be performed.  The
      * first dimension corresponds to the index of the query. The second
      * dimension allows the specification of synonyms that are equivalent and
      * should be combined as OR terms in each search.
      */
     private   String[][]     search_strings_b;

     /**
      * The array of the web searchers to use for performing searches. There is
      * 1 entry for each web service provider used.
      */
     private   NetworkSearch[] web_services;

     /**
      * An array detailing which reports should be prepared to be displayed in
      * the Results Panel. These appear in the following order:
      *
      * <li>display_feature_scores
      * <li>display_combined_raw_counts
      * <li>display_individual_raw_counts
      * <li>display_queries_used
      * <li>display_search_settings_used
      * <li>display_combined_processed_counts_checkbox
      */
     private   boolean[]      reports_to_generate;

     /**
      * The actual queries actually made. Useful for debugging and reference.
      * These may often not include all parameters of searches, however, as these
      * are sometimes necessary to include directly in NetworkSearch objects
      * rather than the queries that they submit.
      *
      * <p>The first dimension corresponds to the top level tasks of calculating
      * C_a, C_b or C_a_b. Index 0 is C_a, index 1 is C_b and index 2 is C_a_b.
      * These are each null if they are not calculated.
      *
      * <p>The second dimension corresponds to the web service used. The third
      * corresponds to the site addresses. The fourth corresponds to the
      * individual search terms.
      */
     private   String[][][][] queries;

     /**
      * The absolute hit counts for the first set of search strings, combined
      * (added) across (potentially normalized) web services and (potentially
      * weighted) sites. Used in both co-occurrence and cross tabulation
      * analyses. Left as null when not used.
      */
     private   double[]        C_a_combined_processed;

     /**
      * The absolute hit counts for the first set of search strings. Used in
      * both co-occurrence and cross tabulation analyses. Left as null when not
      * used. The first dimension is the web service used (as per the
      * web_services field), the second dimension is the site searched (as per
      * the site_addresses field, this number is not yet weighted) and the third
      * dimension is the particular query.
      */
     private   long[][][]     C_a;

     /**
      * The absolute hit counts for the second set of search strings, combined
      * (added) across (potentially normalized) web services and (potentially
      * weighted) sites. Used only in both cross tabulation analyses. Left as
      * null when not used.
      */
     private   double[]       C_b_combined_processed;

     /**
      * The absolute hit counts for the second set of search strings. Used only
      * in both cross tabulation analyses. Left as null when not used. The first
      * dimension is the web service used (as per the web_services field), the
      * second dimension is the site searched (as per the site_addresses field,
      * this number is not yet weighted) and the third dimension is the
      * particular query.
      */
     private   long[][][]     C_b;

     /**
      * The hit counts for the combination of the two search strings, combined
      * (added) across (potentially normalized) web services and (potentially
      * weighted) sites. The first dimension is the same as the last dimension
      * of C_a. If co-occurrence is performed, then the last dimension also
      * corresponds to the same dimension as the last dimension of C_a. If cross
      * tabulation is performed then the last dimension corresponds to the last
      * dimension of C_b.
      */
     private   double[][]     C_a_b_combined_processed;

     /**
      * The hit counts for the combination of the two search strings. The first
      * dimension is the web service used (as per the web_services field), the
      * second dimension is the site searched (as per the site_addresses field,
      * this number is not yet weighted), the third dimension is the same as
      * the last dimension of C_a. If co-occurrence is performed then the last
      * dimension of C_a_b also corresponds to the same dimension as the last
      * dimension of C_a. If cross tabulation is performed then the last
      * dimension corresponds to the last dimension of C_b.
      */
     private   long[][][][]   C_a_b;

     /**
      * The final scores representing the final feature values. These are
      * calculated depending on scoring_function_code and C_a, C_b and/or
      * C_a_b, as appropriate. Calculations are based on combined values
      * accross web services and weighted sites. These may be normalized,
      * as appropriate. The first dimension is search_strings_a. The second
      * dimension is also search_strings_a in the case of co-occurrence
      * processing and search_strings_b in the case of cross tabulation.
      */
     private   double[][]     frequency_scores;



     /**
      * The progress bar coordinator.
      */
     private   DoubleProgressBarTaskCoordinator   progress_bar;

     /**
      * The number of times that a given query is to be submitted unsuccesfully
      * to a web service before the problem is reported to the user.
      */
     private   static final int    TRIES_BEFORE_REPORTING_ERROR = 3;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Instantiate an object and store the given parameters in it. Initialize
      * results fieds.
      *
      * @param parent_window            The window calling this. May be null.
      * @param is_cross_tabulation      If this is true then a cross tabulation
      *                                 analysis is to be performed using both
      *                                 search_strings_a and search_strings_b
      *                                 If it is false, then a cooccurrence
      *                                 analysis is to be performed using only
      *                                 search_strings_a.
      * @param scoring_function_code    The code indicating the scoring function
      *                                 that is to be used. One of the final
      *                                 codes in the OptionsPanel class.
      * @param normalize_across_ws      Whether the hits from web services
      *                                 should be normalized before the
      *                                 frequency_scores field is calculated.
      *                                 This ensures that services that produce
      *                                 fewer hits overall are not underweighted
      *                                 in the final scores.
      * @param normalize_across_sites   Whether the hits from different web
      *                                 services should be normalized before the
      *                                 final feature scores are calculated.
      *                                 This ensures that sites that produce
      *                                 fewer hits overall are not underweighted
      *                                 in the  final scores.This is separate
      *                                 and in addition to the manual weights
      *                                 that can be imposed on the Site
      *                                 Weightings panel.
      * @param should_normalize_scores  An array indicating whether (first
      *                                 entry) the array of final feature values
      *                                 should be normalized and, if so, whether
      *                                 (second entry) this normalization should
      *                                 be performed on a row by row basis
      *                                 (true) or as an overall normalization
      *                                 (false) for the table of scores.
      * @param yahoo_application_id     The Yahoo Application ID that is needed
      *                                 to access Yahoo web services. If this is
      *                                 null then Yahoo will not be searched.
      * @param google_license_key       The Google License Key that is needed
      *                                 to access Google web services. If this
      *                                 is null then Google will not be
      *                                 searched.
      * @param lastfm_license_key       The Last.FM License Key that is needed
      *                                 to access Last.FM web services. If this
      *                                 is null then Last.FM will not be
      *                                 searched.
      * @param literal_search           Whether all search queries should be
      *                                 literal searches (e.g. for the query
      *                                 "heavy metal", hits must have the two
      *                                 words adjacent if the search is
      *                                 literal). This is also sometimes known
      *                                 as an exact search or a phrase search.
      * @param or_based_search          Whether search queries need only contain
      *                                 one of the specified query words in
      *                                 order to result in a hit. If this is
      *                                 true, then only one of the query strings
      *                                 must be present. If this is false, then
      *                                 all of them must be present (although
      *                                 not necessarily in the specified order,
      *                                 unless the* literal_search parameter
      *                                 is true).
      * @param include_non_matching     Whether results returned by search
      *                                 queries performed may include hits
      *                                 that do not contain one or more of the
      *                                 specified search string(s) but do
      *                                 contain terms very similar to them (e.g.
      *                                 alternative spellings).
      * @param suppress_similar_hits    Whether to suppress similar hits when
      *                                 reporting results. This means either
      *                                 sites with identical titles and/or
      *                                 descriptions or multiple hits from the
      *                                 same host.
      * @param suppress_adult_content   Whether to suppress hits that are
      *                                 classified as containing adult content
      *                                 by the search service in question.
      * @param limit_to_language        Name of a language that hits must be in
      *                                 in order to be included in search
      *                                 results. A value of "No Limitations"
      *                                 means that any language is permissible.
      *                                 A value of null is not permitted.
      * @param limit_to_country         Name of a country that sites must be in
      *                                 in order to be included in search
      *                                 results. A value of "No Limitations"
      *                                 means that sites in any country are
      *                                 permissible. A value of null is not
      *                                 permitted.
      * @param region_to_search_from    Name of a country where the search will
      *                                 be performed (i.e. where the search
      *                                 service is located). Results are not
      *                                 limited to this country. An entry of "No
      *                                 Limitations" causes the default service
      *                                 location to be used. A value of null is
      *                                 not permitted.
      * @param limit_to_file_type       A file extension a document must have in
      *                                 order to be returned as a hit in search
      *                                 results. An entry of "No Limitations"
      *                                 means that file type will not by used to
      *                                 filter results. A value of null is not
      *                                 permitted.
      * @param excluded_filter_strings  An array of strings that may not be
      *                                 present on any pages for them to be
      *                                 counted as hits (i.e filter strings).
      *                                 These excluded strings are each treated
      *                                 as literal (i.e. words must appear in
      *                                 the same order). There is one entry for
      *                                 each such string. This is null if there
      *                                 are no exclusion strings.
      * @param required_filter_strings  Strings that must be present on a page
      *                                 in order for it to be counted as a hit.
      *                                 These are in addition to the search
      *                                 strings themselves, and these required
      *                                 filter strings apply to all searches.
      *                                 These are earch treated as literal (i.e.
      *                                 words must appear in the same order). A
      *                                 value of null means that none of thes3
      *                                 are present. Pattern-based search
      *                                 phrases may also be used if the
      *                                 <PRIMARY_SEARCH_STRING> or
      *                                 <SECONDARY_SEARCH_STRING> tags are
      *                                 present. This means that the
      *                                 corresponding filter string for each
      *                                 search has the particular string from
      *                                 search_strings_a or search_strings_b
      *                                 used to replace the
      *                                 <PRIMARY_SEARCH_STRING> or
      *                                 <SECONDARY_SEARCH_STRING> tag,
      *                                 respectively. In the case of
      *                                 co-occurrence searches (as opposed to
      *                                 cross tabulation searches), the
      *                                 <SECONDARY_SEARCH_STRING> tag is used to
      *                                 refer to another string from
      *                                 search_strings_a rather than a string
      *                                 from search_strings_b. If synonyms are
      *                                 present, only the first one is used to
      *                                 replace <PRIMARY_SEARCH_STRING> or
      *                                 <SECONDARY_SEARCH_STRING>.
      * @param weighted_sites           Network sites that will be separately
      *                                 and exclusively searched in all search
      *                                 queries performed by this object. A
      *                                 value of null means that the entire
      *                                 available network should be searched.
      *                                 Specific sites as well as an entry
      *                                 of "<WHOLE_NETWORK>" (without quotes)
      *                                 means that searches will be done
      *                                 separately for each of the specified
      *                                 sites as well as the whole network.
      *                                 Weights of 1.0 are assigned by default
      *                                 to all sites (and  <WHOLE_NETWORK>).
      *                                 Alternative weightings are assigned to
      *                                 specific sites (or  <WHOLE_NETWORK>) for
      *                                 entries that enclude " <WEIGHT> "
      *                                 (without quotes) and then a decimal
      *                                 number after the site address. Weights
      *                                 are normalized during final processing.
      * @param search_strings_a         A set of search strings to be used in
      *                                 either cross tabulation analyses or
      *                                 cooccurrence analyses. An exception is
      *                                 thrown if this is null. These should
      *                                 already have been cleaned by having
      *                                 blank lines and duplicate lines removed.
      *                                  An entry that contains words or sets of
      *                                 words followed by " <SYNONYM> " tag(s)
      *                                 (without the quotes) specifies that each
      *                                 string on the left and right of the
      *                                 synonym tag(s) are to be treated as
      *                                 synonyms and results for all synonyms in
      *                                 an entry are to be combined during
      *                                 scoring caluculations.
      * @param search_strings_b         A set of search strings to be used only
      *                                 in cross tabulation analyses. An
      *                                 exception is thrown if a cross
      *                                 tabulation analysis is to be performed
      *                                 and this is null. Is ignored if a
      *                                 co-occurrence analysis is performed.
      *                                 These should already have been cleaned
      *                                 by having blank lines and duplicate
      *                                 lines removed.  An entry that contains
      *                                 words or sets of words followed by
      *                                 " <SYNONYM> " tag(s) (without the
      *                                 quotes) specifies that each string on
      *                                 the left and right of the synonym tag(s)
      *                                 are to be treated as synonyms and
      *                                 results for all synonyms in an entry are
      *                                 to be combined during scoring
      *                                 caluculations.
      * @param reports_to_generate      An array detailing which reports should
      *                                 be generated for display in the Results
      *                                 Panel. These appear in the following
      *                                 order: display_feature_scores,
      *                                 display_combined_raw_counts,
      *                                 display_individual_raw_counts,
      *                                 display_queries_used and
      *                                 display_search_settings_used.
      * @throws     Exception           An exception is thrown if invalid
      *                                 parameters are provided, if an invalid
      *                                 search engine key is used, if the
      *                                 maximum number of permitted searches
      *                                 have been performed or if there is some
      *                                 other problem.
      */
     public AnalysisProcessor(OuterFrame parent_window,
          boolean is_cross_tabulation,
          int scoring_function_code,
          boolean normalize_across_ws,
          boolean normalize_across_sites,
          boolean[] should_normalize_scores,
          String yahoo_application_id,
          String google_license_key,
          boolean lastfm_enabled,
          String lastfm_license_key,
          boolean literal_search,
          boolean or_based_search,
          boolean include_non_matching,
          boolean suppress_similar_hits,
          boolean suppress_adult_content,
          String limit_to_language,
          String limit_to_country,
          String region_to_search_from,
          String limit_to_file_type,
          String[] excluded_filter_strings,
          String[] required_filter_strings,
          String[] weighted_sites,
          String[] search_strings_a,
          String[] search_strings_b,
          boolean[] reports_to_generate )
          throws Exception
     {
          // Remove focus from parent windows Result Panel
          parent_window.enableResultsPanel(false);

          // Validate the given parameters
          if (search_strings_a == null)
               throw new Exception("No primary search strings provided.\n\n" +
                    "These may be entered in the Search Words Panel.");
          if (!is_cross_tabulation && search_strings_a.length == 1)
               throw new Exception("Only one primary search string provided.\n\n" +
                    "At least two are needed for co-occurrence.");
          if (is_cross_tabulation && search_strings_b == null)
               throw new Exception("No secondary search strings provided.\n" +
                    "These are needed for cross tabulations.\n\n" +
                    "These may be entered in the Search Words Panel.");
          if (yahoo_application_id == null && google_license_key == null && lastfm_license_key == null)
               throw new Exception("No web services selected to be searched.\n" +
                    "These may be chosen in the Options Panel.");
          if (yahoo_application_id == null && google_license_key == null && !is_cross_tabulation)
               throw new Exception("No search-based web services selected to be searched for co-occurrence extraction.\n" +
                    "These may be chosen in the Options Panel.");
          // excepciones

          // Parse and store search terms, considering synonyms
          this.search_strings_a = getParsedSynonyms(search_strings_a);
          this.search_strings_b = getParsedSynonyms(search_strings_b);

          // Set up array of sites to search and their corresponding weights
          this.site_addresses = getSiteAddresses(weighted_sites);
          this.site_weightings = getSiteWeightings(weighted_sites);

          // Set up basic and pattern-based required filter strings
          this.required_filter_strings_basic = getBasicRequiredFilterStrings(required_filter_strings);
          this.required_filter_strings_pattern_based = getPatternBasedRequiredFilterStrings(required_filter_strings);

          // Store remaining parameters
          this.parent_window = parent_window;
          this.is_cross_tabulation = is_cross_tabulation;
          this.scoring_function_code = scoring_function_code;
          this.normalize_across_web_services = normalize_across_ws;
          this.normalize_across_sites = normalize_across_sites;
          this.should_normalize_scores = should_normalize_scores;
          this.yahoo_application_id = yahoo_application_id;
          this.google_license_key = google_license_key;
          this.lastfm_enabled = lastfm_enabled;
          this.lastfm_license_key = lastfm_license_key;
          if(is_cross_tabulation)
          {
          this.lastfm_report_table = new String[search_strings_a.length][search_strings_b.length + 1];
          this.lastfm_report_table_norm = new String[search_strings_a.length][search_strings_b.length + 1];
          this.lastfm_websearch_report_table_norm = new double[search_strings_a.length][2*search_strings_b.length];
          this.lastfm_report_table_norm_with_columns = new String[search_strings_a.length][search_strings_b.length + 1];
          this.averaged_websearch_lastfm = new String[search_strings_a.length][search_strings_b.length + 1];
          }
          this.literal_search = literal_search;
          this.or_based_search = or_based_search;
          this.include_non_matching = include_non_matching;
          this.suppress_similar_hits = suppress_similar_hits;
          this.suppress_adult_content = suppress_adult_content;
          this.limit_to_language = limit_to_language;
          this.limit_to_country = limit_to_country;
          this.region_to_search_from = region_to_search_from;
          this.limit_to_file_type = limit_to_file_type;
          this.excluded_filter_strings = excluded_filter_strings;
          this.reports_to_generate = reports_to_generate;
          if (!is_cross_tabulation) search_strings_b = null;

          // Set up array of the web searchers to use (1 entry for each
          // searcher). They are not configured with search parameters.
          this.web_services = getUnconfiguredSearchers();

          // Prepare to store actual queries made for later reference
          this.queries = new String[3][site_addresses.length][web_services.length][];
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Set the coordinator for the progress bar itself.
      *
      * @param task_coordinator    The task coordinator.
      */
     public void setDoubleProgressBarTaskCoordinator(DoubleProgressBarTaskCoordinator task_coordinator)
     {
          progress_bar = task_coordinator;
     }


     /**
      * Find the total number of top-level tasks that must be performed. Finding
      * C_a, C_b or C_a_b are each considered a top-level task. The generation
      * of reports is also considered a top-level task.
      *
      * @return     The number of top-level tasks.
      */
     public int findNumberOfTopLevelTasks()
     {
          if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_1_CODE)
               return 1 + 1;
          if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_2_CODE)
               return 1 + 2;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_1_CODE)
               return 1 + 2;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_2_CODE)
               return 1 + 2;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_3_CODE)
               return 1 + 1;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_4_CODE)
               return 1 + 1;
          else return -1;
     }


     /**
      * Find the total number of different queries that need to be submitted.
      * This consists of the unique queries multiplied by the number of sites
      * they must be submitted regarding multiplied by the number of web
      * services that they are submitted to.
      *
      * @param      number_network_addresses The number of different network
      *                                      addresses that must be searched
      *                                      separately using each query.
      * @param      number_web_services      The number of different web
      *                                      services being used.
      * @return                              The total number of queries.
      */
     public int findRawNumberOfQueries(int number_network_addresses,
          int number_web_services)
     {
          // The number of PC(a) query terms
          int C_a_operations = search_strings_a.length;

          // The number of PC(b) query terms
          int C_b_operations = 0;
          if (search_strings_b != null)
               C_b_operations = search_strings_b.length;

          // The number of PC(a, b) query terms
          int C_a_b_operations = 0;
          if (!is_cross_tabulation)
               for (int i = 1; i < search_strings_a.length; i++)
                    for (int j = 0; j < i; j++)
                         C_a_b_operations++;
          else C_a_b_operations = search_strings_a.length * search_strings_b.length;

          // Find the number of times that each query must be submitted
          int multiplier = number_network_addresses * number_web_services;

          // Return the appropriate number of queries
          if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_1_CODE)
               return C_a_b_operations * multiplier;
          if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_2_CODE)
               return (C_a_operations + C_a_b_operations) * multiplier;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_1_CODE)
               return (C_a_operations + C_a_b_operations) * multiplier;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_2_CODE)
               return (C_b_operations + C_a_b_operations) * multiplier;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_3_CODE)
               return C_a_b_operations * multiplier;
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_4_CODE)
               return C_a_b_operations * multiplier;
          else return -1;
     }


     /**
      * Perform actual processing.
      *
      * @throws     Exception An informative report of any problem that occurs.
      */
    @SuppressWarnings("empty-statement")
     public void performProcessing()
     throws Exception
     {
		// Disable parent window
          if (parent_window != null)
               parent_window.setEnabled(false);

// If web searching is to be done
if (web_services[0] != null)
{
          // Configure web searcb services with all parameters passed during
          // instantiation except for site addresses and search strings
          configureWebServices(web_services);

          // Perform actual queries, record queries, fill in C_a, C_b, and/or
          // C_a_b and C_a_combined_processed, C_b_combined_processed and/or
          // C_a_b_combined_processed and calculate non-normalized
          // frequency_scores
          if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_1_CODE)
          {
               // Find C_a_b
               C_a_b = findHitCountsTwoDimensional(search_strings_a, search_strings_a, 2);

               // Combine C_a_b across web services (potentially normalized) and
               // web sites (potentially weighted and/or normalized)
               findCombinedCounts();

               // Calculate pre-normalized final_scores
               frequency_scores = new double[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length];
               for (int a = 0; a < frequency_scores.length; a++)
                    for (int b = 0; b < frequency_scores[a].length; b++)
                    {
                    double numerator = C_a_b_combined_processed[a][b];
                    double first_sum = 0.0;
                    for (int y = 0; y < frequency_scores[a].length; y++)
                         if (y != a)
                              first_sum += C_a_b_combined_processed[a][y];
                    double second_sum = 0.0;
                    for (int x = 0; x < frequency_scores.length; x++)
                         if (x != b)
                              second_sum += C_a_b_combined_processed[x][b];
                    double denominator = 1.0 + (first_sum * second_sum);
                    frequency_scores[a][b] = numerator / denominator;
                    }
          }
          if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_2_CODE)
          {
               // Find C_a
               C_a = findHitCountsOneDimensional(search_strings_a, 0);

               // Find C_a_b
               C_a_b = findHitCountsTwoDimensional(search_strings_a, search_strings_a, 2);

               // Combine C_a and C_a_b across web services (potentially
               // normalized) and web sites (potentially weighted and/or normalized)
               findCombinedCounts();

               // Find the largest C_a_combined_processed
               double largest_count = C_a_combined_processed[0];
               for (int i = 0; i < C_a_combined_processed.length; i++)
                    if (C_a_combined_processed[i] > largest_count)
                         largest_count = C_a_combined_processed[i];

               // Calculate pre-normalized final_scores
               frequency_scores = new double[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length];
               for (int a = 0; a < frequency_scores.length; a++)
                    for (int b = 0; b < frequency_scores[a].length; b++)
                    {
                    double first_part = C_a_b_combined_processed[a][b] / C_a_combined_processed[b];
                    double second_part = (Math.abs(C_a_combined_processed[a] - C_a_combined_processed[b])) / largest_count;
                    frequency_scores[a][b] = first_part * (1.0 - second_part);
                    }
          }
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_1_CODE)
          {
               // Find C_a
               C_a = findHitCountsOneDimensional(search_strings_a, 0);

               // Find C_a_b
               C_a_b = findHitCountsTwoDimensional(search_strings_a, search_strings_b, 2);

               // Combine C_a and C_a_b across web services (potentially
               // normalized) and web sites (potentially weighted and/or normalized)
               findCombinedCounts();

               // Calculate pre-normalized final_scores
               frequency_scores = new double[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length];
               for (int a = 0; a < frequency_scores.length; a++)
                    for (int b = 0; b < frequency_scores[a].length; b++)
                         frequency_scores[a][b] = C_a_b_combined_processed[a][b] / C_a_combined_processed[a];
          }
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_2_CODE)
          {
               // Find C_b
               C_b = findHitCountsOneDimensional(search_strings_b, 1);

               // Find C_a_b
               C_a_b = findHitCountsTwoDimensional(search_strings_a, search_strings_b, 2);

               // Combine C_b and C_a_b across web services (potentially
               // normalized) and web sites (potentially weighted and/or normalized)
               findCombinedCounts();

               // Calculate pre-normalized final_scores
               frequency_scores = new double[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length];
               for (int a = 0; a < frequency_scores.length; a++)
                    for (int b = 0; b < frequency_scores[a].length; b++)
                         frequency_scores[a][b] = C_a_b_combined_processed[a][b] / C_b_combined_processed[b];
          }
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_3_CODE)
          {
               // Find C_a_b
               C_a_b = findHitCountsTwoDimensional(search_strings_a, search_strings_b, 2);

               // Combine C_a_b across web services (potentially normalized) and
               // web sites (potentially weighted and/or normalized)
               findCombinedCounts();

               // Calculate pre-normalized final_scores
               frequency_scores = new double[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length];
               for (int a = 0; a < frequency_scores.length; a++)
                    for (int b = 0; b < frequency_scores[a].length; b++)
                    {
                    double sum = 0.0;
                    for (int i = 0; i < frequency_scores.length; i ++)
                         sum += C_a_b_combined_processed[i][b];
                    frequency_scores[a][b] = C_a_b_combined_processed[a][b] / (1.0 + sum);
                    }
          }
          if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_4_CODE)
          {
               // Find C_a_b
               C_a_b = findHitCountsTwoDimensional(search_strings_a, search_strings_b, 2);

               // Combine C_a_b across web services (potentially normalized) and
               // web sites (potentially weighted and/or normalized)
               findCombinedCounts();

               // Calculate pre-normalized final_scores
               frequency_scores = new double[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length];
               for (int a = 0; a < frequency_scores.length; a++)
                    for (int b = 0; b < frequency_scores[a].length; b++)
                    {
                    double sum = 0.0;
                    for (int i = 0; i < frequency_scores[a].length; i ++)
                         sum += C_a_b_combined_processed[a][i];
                    frequency_scores[a][b] = C_a_b_combined_processed[a][b] / (1.0 + sum);
                    }
          }

          // Normalize the frequency scores, if appropriate
          if (should_normalize_scores[0])
          {
               if (should_normalize_scores[1])
                    frequency_scores = mckay.utilities.staticlibraries.MathAndStatsMethods.normalize(frequency_scores);
               else
                    frequency_scores = mckay.utilities.staticlibraries.MathAndStatsMethods.normalizeEntirely(frequency_scores);
          }
}


          // Perform Last.FM querying
          if(lastfm_enabled && is_cross_tabulation)
          {
              for(int i=0; i<search_strings_a.length; i++) // looping through ARTISTS
              {
				  Collection<String> topTags = Artist.getTopTags(search_strings_a[i][0], lastfm_license_key);
                  boolean foundMatch = false;
                  for(int j=0; j<search_strings_b.length; j++) // looping through GENRES
                  {
                      int tag_position_counter = 1;
                      genre_tag_comparing:
                          for (String t : topTags) // looping through TAGS
                          {
                              if(search_strings_b[j][0].equalsIgnoreCase(t)) // Comparing GENRES / TAGS
                              {
                                  lastfm_report_table[i][j] = String.valueOf(tag_position_counter);
                                  foundMatch = true;
                                  break genre_tag_comparing;
                              }
                              tag_position_counter++;
                          }
                          if(!foundMatch)
                          {
                              lastfm_report_table[i][j] = (null);
                          }
                  }
              }
              // Last.FM tag results normalization function
              for(int i=0; i<search_strings_a.length; i++)
              {
                  double row_sum = 0;
                  double except_value = 0;
                  for(int j=0; j<search_strings_b.length; j++)
                  {
                      if (lastfm_report_table[i][j] == null)
                      {
                          except_value = 0;
                      }
                      else
                      {
                          except_value = 1 / Float.valueOf(lastfm_report_table[i][j]);
                      }
                      row_sum = row_sum + except_value;
                  }
                  for(int j=0; j<search_strings_b.length; j++)
                  {
                      if (lastfm_report_table[i][j] == null)
                      {
                          lastfm_report_table_norm[i][j] = String.valueOf(0);
                      }
                      else
                      {
                          lastfm_report_table_norm[i][j] = String.valueOf(1/(row_sum * Double.valueOf(lastfm_report_table[i][j])));
                      }
                  }
              }
              // New matrix with row names from Search String A
              for(int i=0; i<search_strings_a.length; i++)
              {
                  for(int j=0; j<search_strings_b.length; j++)
                  {
                      lastfm_report_table_norm_with_columns[i][j+1] = lastfm_report_table_norm[i][j];
                      lastfm_report_table_norm_with_columns[i][0] = search_strings_a[i][0];
                  }
              }
          }

		  // If Last.FM only
		  if (web_services[0] == null && lastfm_enabled && is_cross_tabulation)
		  {
				frequency_scores = new double[search_strings_a.length][search_strings_b.length + 1];
				for (int i = 0; i < frequency_scores.length; i++)
					for (int j = 0; j < (frequency_scores[i].length - 1); j++)
                        frequency_scores[i][j] = Double.valueOf(lastfm_report_table_norm[i][j]);
		  }

          if(lastfm_enabled && is_cross_tabulation && (yahoo_application_id != null || google_license_key != null)) // New matrix with averaged results from LastFM and websearch averaged results
          {
              for(int i=0; i<search_strings_a.length; i++)
              {
                  for(int j=0; j<search_strings_b.length; j++)
                  {
                    averaged_websearch_lastfm[i][j+1] = String.valueOf(0.5*(frequency_scores[i][j] + Double.valueOf(lastfm_report_table_norm[i][j])));
                    averaged_websearch_lastfm[i][0] = search_strings_a[i][0];
                  }
              }

			  // If no results are available from one of the  possible sources, then double values to preserve normalization
              for(int i=0; i<search_strings_a.length; i++)
              {
					double row_total = 0.0;
					for(int j=0; j<search_strings_b.length; j++)
					{
						row_total += (new Double(averaged_websearch_lastfm[i][j+1])).doubleValue();
					}
					if (row_total != 1.0)
					{
						for(int j=0; j<search_strings_b.length; j++)
						{
							double this_value = (new Double(averaged_websearch_lastfm[i][j+1])).doubleValue();
							averaged_websearch_lastfm[i][j+1] = String.valueOf(this_value / row_total);
							if (row_total == 0.0)
								averaged_websearch_lastfm[i][j+1] = String.valueOf(1.0 / (double) search_strings_b.length);
						}
					}
			  }
          }

          // Generate the report to display in the Results Panel
          verifyNotCancelled();
          String report_html = generateHTMLResultsReport();

          // Cause the report to be displayed in the ResultsReportPanel and
          // store the feature values in it
          verifyNotCancelled();
          String[] row_labels = getResultsLabels(search_strings_a);
          String[] column_labels = null;
          if (is_cross_tabulation)
          {
              column_labels = getResultsLabels(search_strings_b);
              if (lastfm_enabled && (yahoo_application_id !=null || google_license_key != null))
              {
                  for(int i=0; i< search_strings_a.length; i++)
                  {
                      for(int j=0; j< search_strings_b.length; j++)
                      {
                      lastfm_websearch_report_table_norm[i][j] = frequency_scores[i][j];
                      lastfm_websearch_report_table_norm[i][j + search_strings_b.length] = Double.valueOf(lastfm_report_table_norm[i][j]);
                      }
                 }
              }
          }
          else column_labels = row_labels;

          if (lastfm_enabled && is_cross_tabulation && (yahoo_application_id != null || google_license_key != null))
              {frequency_scores = lastfm_websearch_report_table_norm;}
          parent_window.setResultsText(report_html, frequency_scores, row_labels, column_labels);

          // Place focus in parent window on Result Panel
          parent_window.enableResultsPanel(true);

          // Stop the progress bar when processing is complete
          progress_bar.markTaskComplete();

          // Reenable parent window
          if (parent_window != null)
               parent_window.setEnabled(true);
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Check to see if the cancel button has been pressed on the dialog box.
      * If so, then throw an Exception showing so.
      *
      * @throws     Exception Indicates Cancel button press on dialog box.
      */
     private void verifyNotCancelled()
     throws Exception
     {
          if (progress_bar.isCancelled()) throw new Exception("Processing cancelled by user.");
     }


     /**
      * Returns the given search_strings as an array where each corresponding
      * entry has been split where based on every occurence of " <SYNONYM> "
      * (without quotes).
      *
      * @param search_strings The strings to split.
      * @return               The first dimension corresponds to the dimensions
      *                       of search_strings. The second dimension
      *                       corresponds to each split.
      */
     private static String[][] getParsedSynonyms(String[] search_strings)
     {
          if (search_strings == null)
               return null;

          // Parse
          String[][] results = new String[search_strings.length][];
          for (int i = 0; i < results.length; i++)
               results[i] = search_strings[i].split(" <SYNONYM> ");

          // Return results
          return results;
     }


     /**
      * Returns the names of the addresses of all network locations to be
      * searched without weights. Any spaces are stripped and the URL is
      * UTF-8 encoded.
      *
      * @param weighted_sites Network sites to be separately and exclusively
      *                       searched in all search queries performed. A
      *                       value of null means that the entire available
      *                       network should be searched. Specific sites as well
      *                       as an entry of "<WHOLE_NETWORK>" (without quotes)
      *                       means that searches will be done separately for
      *                       each of the specified sites as well as the whole
      *                       network. Weights of 1.0 are assigned by default
      *                       to all sites (and  <WHOLE_NETWORK>). Alternative
      *                       weightings are assigned to specific sites (or
      *                       <WHOLE_NETWORK>) for entries that enclude
      *                       " <WEIGHT> " (without quotes) and then a decimal
      *                       number after the site address. This method strips
      *                       away these weights in the returned array.
      * @return     The network addresses to search. An entry of null
      *             corresponds to the entire available network. A null array is
      *             never returned. No weights are included in the returned
      *             entries.
      */
     private static String[] getSiteAddresses(String[] weighted_sites)
     {
          // The addresses that will be returned
          String[] site_addresses;

          // Prepare a one entry array containing null if only the whole network
          // is to be searched
          if (weighted_sites == null)
          {
               site_addresses = new String[1];
               site_addresses[0] = null;
          }

          // Deal with multiple sites specified
          else
          {
               site_addresses = new String[weighted_sites.length];
               for (int i = 0; i < site_addresses.length; i++)
               {
                    // Strip weighting specifications
                    site_addresses[i] = weighted_sites[i].split(" <WEIGHT> ")[0];

                    // Note if the whole network is to be searched
                    if (site_addresses[i].equals("<WHOLE_NETWORK>"))
                         site_addresses[i] = null;
               }
          }

          // Strip spaces and UTF-8 encode
          for (int i = 0; i < site_addresses.length; i++)
               if (site_addresses[i] != null)
               {
               site_addresses[i] = site_addresses[i].replaceAll(" ", "");
               try
               {site_addresses[i] = java.net.URLEncoder.encode(site_addresses[i], "UTF-8");}
               catch (Exception e)
               {}
               }

          // Return results
          return site_addresses;
     }


     /**
      * Returns a normalized set of weights from 0 to 1.0 for the sites
      * returned by the getSiteAddresses method.
      *
      * @param weighted_sites Network sites to be separately and exclusively
      *                       searched in all search queries performed. A
      *                       value of null means that the entire available
      *                       network should be searched. Specific sites as well
      *                       as an entry of "<WHOLE_NETWORK>" (without quotes)
      *                       means that searches will be done separately for
      *                       each of the specified sites as well as the whole
      *                       network. Weights of 1.0 are assigned by default
      *                       to all sites (and  <WHOLE_NETWORK>). Alternative
      *                       weightings are assigned to specific sites (or
      *                       <WHOLE_NETWORK>) for entries that enclude
      *                       " <WEIGHT> " (without quotes) and then a decimal
      *                       number after the site address. Weights are
      *                       normalized during final processing.
      * @return               Normalized site weights.
      */
     private static double[] getSiteWeightings(String[] weighted_sites)
     {
          // The weights that will be returned
          double[] site_weightings ;

          // Calculate the number of entries in the array
          if (weighted_sites == null) site_weightings = new double[1];
          else site_weightings = new double[weighted_sites.length];

          // Initialize all weightings to 1.0
          for (int i = 0; i < site_weightings.length; i++)
               site_weightings[i] = 1.0;

          // Parse out weightings
          if (weighted_sites != null)
               for (int i = 0; i < site_weightings.length; i++)
               {
               String[] this_split = weighted_sites[i].split(" <WEIGHT> ");
               if (this_split.length > 1)
                    site_weightings[i] = Double.valueOf(this_split[1]);
               }

          // Normalize weights
          site_weightings = mckay.utilities.staticlibraries.MathAndStatsMethods.normalize(site_weightings);

          // Return the results
          return site_weightings;
     }


     /**
      * Returns a copy of the given array of strings with all entries that
      * contain <PRIMARY_SEARCH_STRING> and/or <SECONDARY_SEARCH_STRING>
      * removed.
      *
      * @param original_filter_strings  An array of strings, some of which may
      *                                 contain the tags <PRIMARY_SEARCH_STRING>
      *                                 and/or <SECONDARY_SEARCH_STRING>.
      * @return                         The original_filter_strings parameter
      *                                 with all entries containing
      *                                 <PRIMARY_SEARCH_STRING> and/or
      *                                 <SECONDARY_SEARCH_STRING> removed. This
      *                                 array will be of equal or smaller size
      *                                 to original_filter_strings. Null is
      *                                 returned if there are no remaining
      *                                 entries.
      */
     private static String[] getBasicRequiredFilterStrings(String[] original_filter_strings)
     {
          if (original_filter_strings == null) return null;

          String[] copy = ArrayMethods.castArrayAsStrings(ArrayMethods.getCopyOfArray(original_filter_strings));
          for (int i = 0; i < copy.length; i++)
               if (copy[i].indexOf("<PRIMARY_SEARCH_STRING>") != -1 || copy[i].indexOf("<SECONDARY_SEARCH_STRING>") != -1)
                    copy[i] = null;
          return ArrayMethods.castArrayAsStrings(ArrayMethods.removeNullEntriesFromArray(copy));
     }


     /**
      * Returns the given original_filter_strings in a new 2-D array. The first
      * dimensions corresponds to different elements in original_filter_strings.
      * Each of these elements are broken up into arrays with either
      * <PRIMARY_SEARCH_STRING> or <SECONDARY_SEARCH_STRING> acting as
      * segmenters. If neither of these are present in an entry then it is
      * not included in the results. Null is returned if these are not present
      * in any of the elements of original_filter_strings.
      *
      * @param original_filter_strings  An array of strings, some of which may
      *                                 contain the tags <PRIMARY_SEARCH_STRING>
      *                                 and/or <SECONDARY_SEARCH_STRING>.
      * @return                         The segmented and reduced array or null.
      */
     private static String[][] getPatternBasedRequiredFilterStrings(String[] original_filter_strings)
     {
          // Return null if the original_filter_strings are null
          if (original_filter_strings == null) return null;

          // To hold accumulated results
          LinkedList<String[]> results = new LinkedList<String[]>();

          // The terms used to split strings
          String[] splitters = {"<PRIMARY_SEARCH_STRING>", "<SECONDARY_SEARCH_STRING>"};

          // Perform processing
          for (int i = 0; i < original_filter_strings.length; i++)
          {
               // Check to see if a splitter is contained in this string. If
               // not, do not add it to results.
               boolean contains_a_splitter = false;
               for (int j = 0; j < splitters.length; j++)
                    if (original_filter_strings[i].indexOf(splitters[j]) != -1)
                         contains_a_splitter = true;
               if (contains_a_splitter)
               {
                    // To store the locations of splitters, form left to right
                    LinkedList<Integer> splitter_locations = new LinkedList<Integer>();
                    LinkedList<Integer> splitter_ids = new LinkedList<Integer>();

                    // Keep looking until there are no more splitters
                    boolean still_more = true;
                    int last_found = 0;
                    while (still_more)
                    {
                         // Find the first occurence since last_found of each
                         // splitter
                         int[] lowest_indexes = new int[splitters.length];
                         for (int j = 0; j < splitters.length; j++)
                              lowest_indexes[j] = original_filter_strings[i].indexOf(splitters[j], last_found);

                         // See if any were found
                         int min_splitter = -5;
                         for (int k = 0; k < lowest_indexes.length; k++)
                              if (lowest_indexes[k] != -1)
                              {
                              min_splitter = k;
                              k = lowest_indexes.length;
                              }

                         // Done if no more
                         if (min_splitter == -5)
                              still_more = false;

                         // Add what was found
                         else
                         {
                              // Find the lowest non -1 index
                              for (int k = 0; k < lowest_indexes.length; k++)
                                   if (lowest_indexes[k] < lowest_indexes[min_splitter] && lowest_indexes[k] != -1)
                                        min_splitter = k;

                              // Store the result or terminate the while loop
                              splitter_locations.add(new Integer(lowest_indexes[min_splitter]));
                              splitter_ids.add(new Integer(min_splitter));

                              // Update last_found
                              last_found = lowest_indexes[min_splitter] + 1;
                         }
                    }

                    // Prepare the arrays of integers
                    Integer[] inexes_of_splitters = splitter_locations.toArray(new Integer[1]);
                    Integer[] which_splitters = splitter_ids.toArray(new Integer[1]);

                    // To hold the segmented string
                    LinkedList<String> this_filter_string = new LinkedList<String>();

                    // Fill this_filter_string
                    int previous_index = 0;
                    for (int j = 0; j < inexes_of_splitters.length; j++)
                    {
                         // If non-splitter text needs to be added before this
                         // splitter
                         if (inexes_of_splitters[j] != previous_index)
                         {
                              this_filter_string.add(original_filter_strings[i].substring(previous_index, inexes_of_splitters[j]));
                              previous_index = inexes_of_splitters[j];
                         }

                         // Add splitter
                         this_filter_string.add(splitters[which_splitters[j]]);
                         previous_index = inexes_of_splitters[j] + splitters[which_splitters[j]].length();
                    }

                    // Add any trailing text, if necessary
                    if (previous_index < original_filter_strings[i].length())
                         this_filter_string.add(original_filter_strings[i].substring(previous_index, original_filter_strings[i].length()));

                    // Store the results for this string
                    results.add(this_filter_string.toArray(new String[1]));
               }
          }

          // Return null if none of the elements of original_filter_strings had
          // the tags
          if (results.isEmpty()) return null;

          // Retrun the parsed and potentially reduced results
          return results.toArray(new String[1][1]);
     }


     /**
      * Set up array of the web searchers to use (1 entry for each  searcher) to
      * perform queries. They are not yet configured with search parameters.
      *
      * @return     Unconfigured search objects.
      */
     private NetworkSearch[] getUnconfiguredSearchers()
     {
          LinkedList<NetworkSearch> web_services_list = new LinkedList<NetworkSearch>();
          if (yahoo_application_id != null)
               web_services_list.add(new YahooWebSearch(yahoo_application_id));
          if (google_license_key != null)
               web_services_list.add(new GoogleWebSearch(google_license_key));
          return web_services_list.toArray(new NetworkSearch[1]);
     }


     /**
      * Configure the objects that will perform actual queries. Configuration
      * includes all parameters passed to this AnalysisProcessor during
      * instantiation except for specific network addresses and query terms.
      * These two exceptions are because these parameters can be configured
      * differently for every search.
      *
      * @param web_services   The array of web services to configure.
      * @throws     Exception Throws an informative exception if the paramters
      *                       are invalid.
      */
     private void configureWebServices(NetworkSearch[] web_services)
     throws Exception
     {
		 for (int i = 0; i < web_services.length; i++)
          {
               web_services[i].setLiteralSearch(literal_search);
               web_services[i].setOrBasedSearch(or_based_search);
               web_services[i].setIncludeSimilarButNonMatchingStrings(include_non_matching);
               web_services[i].setSuppressSimilarHits(suppress_similar_hits);
               web_services[i].setSuppressAdultContent(suppress_adult_content);
               web_services[i].setLanguageResultsMustBeIn(limit_to_language);
               web_services[i].setCountryResultsMustBeIn(limit_to_country);
               web_services[i].setRegionToSearchFrom(region_to_search_from);
               web_services[i].setFileTypeResultsMustBelongTo(limit_to_file_type);
               web_services[i].setSearchStringsToExclude(excluded_filter_strings);
          }
     }


     /**
      * Return the absolute hit counts for the given set of search strings.
      * Synonyms in the given search_strings parameter are taken into account.
      *
      * <p>Used for calculating C_a or C_b.
      *
      * <p>Uses the configured web_services NetworkSearch[] field to perform
      * searches. The web_services field should already have been configured by
      * the configureWebServices method when this method is called.
      *
      * <p>The queries made by this task take into account the contents of the
      * required_filter_strings_basic, required_filter_strings_pattern_based
      * and site_addresses. Weighting of site results according to the
      * site_weightings field in NOT performed by this method, however.
      *
      * <p>The task performed by this method is a subtask in the double progress
      * bar field progress_bar. This method therefore updates the progress_bar
      * field appropriately.
      *
      * <p>A record of all queries maded is added to the queries field at the
      * given queries_index.
      *
      * @param search_strings The set of search strings to find hit counts for.
      *                       The first dimension corresponds to the index of
      *                       the query. The second dimension allows the
      *                       specification of synonyms that are equivalent and
      *                       should be combined as OR terms in each search,
      *                       with the hit counts consisting of the combined
      *                       hits. These must be either search_strings_a
      *                       or search_strings_b.
      * @param queries_index  The index in the first dimension of the queries
      *                       field where a record of the queries made is to
      *                       be added.
      * @return               The absolute number of hits corresponding to the
      *                       search_strings parameter. The first dimension is
      *                       the web service used (as per the web_services
      *                       field), the second dimension is the site searched
      *                       (as per the site_addresses field, this number is
      *                       not yet weighted) and the third dimension is the
      *                       particular query.
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs
      */
     private long[][][] findHitCountsOneDimensional(String[][] search_strings,
          int queries_index)
          throws Exception
     {
          // Prepare arrays to store the results and the queries used
          long[][][] results = new long[web_services.length][site_addresses.length][search_strings.length];
          queries[queries_index] = new String[web_services.length][site_addresses.length][search_strings.length];

          // Calculate the number of tasks to be performed and intialize
          // the index of the current task and the progress bar
          int number_sub_tasks = web_services.length * site_addresses.length * search_strings.length;
          int current_task = 0;
          String task_identifier = "";
          if (search_strings == search_strings_a) task_identifier = " for primary search strings";
          if (search_strings == search_strings_b) task_identifier = " for secondary search strings";
          String task_count_info = " (" + number_sub_tasks + " of " + findRawNumberOfQueries(site_addresses.length, web_services.length) + " queries).";
          progress_bar.startNewSubTask(number_sub_tasks, "Acquiring hit counts" + task_identifier + task_count_info);

          // Go through each web service
          for (int service_i = 0; service_i < web_services.length; service_i++)
          {
               // Go through each specific site
               for (int site_i = 0; site_i < site_addresses.length; site_i++)
               {
                    // Set the site to search (or the whole network)
                    web_services[service_i].setSpecificSiteToSearch(site_addresses[site_i]);

                    // Search each query
                    for (int query_i = 0; query_i < search_strings.length; query_i++)
                    {
                         // Prepare the query
                         String[] these_search_strings = null;
                         if (search_strings == search_strings_a)
                              these_search_strings = getQueryTerms(search_strings[query_i], null);
                         else if (search_strings == search_strings_b)
                              these_search_strings = getQueryTerms(null, search_strings[query_i]);

                         // To store the query actually used by the web service
                         String[] this_actual_query_used = new String[1];

                         // Submit the query and archive it in the query field
                         verifyNotCancelled();
                         results[service_i][site_i][query_i] =
                              web_services[service_i].getNumberHits(these_search_strings, this_actual_query_used, TRIES_BEFORE_REPORTING_ERROR);

                         // Store the actual query used
                         queries[queries_index][service_i][site_i][query_i] = this_actual_query_used[0];

                         // Update the progress bar
                         current_task++;
                         progress_bar.setSubTaskProgressValue(current_task);
                    }
               }
          }

          // Return the results
          return results;
     }


     /**
      * Return the combined absolute hit counts for the two given sets of search
      * strings. Synonyms in the given search_strings parameter are taken into
      * account.
      *
      * <p>Used for calculating C_a_b.
      *
      * <p>Uses the configured web_services NetworkSearch[] field to perform
      * searches. The web_services field should already have been configured by
      * the configureWebServices method when this method is called.
      *
      * <p>The queries made by this task take into account the contents of the
      * required_filter_strings_basic, required_filter_strings_pattern_based
      * and site_addresses. Weighting of site results according to the
      * site_weightings field in NOT performed by this method, however.
      *
      * <p>The task performed by this method is a subtask in the double progress
      * bar field progress_bar. This method therefore updates the progress_bar
      * field appropriately.
      *
      * <p>A record of all queries maded is added to the queries field at the
      * given queries_index.
      *
      * @param search_strings The first set of search strings to be combined
      *                       with the second set. The first dimension
      *                       corresponds to the index of the query. The second
      *                       dimension allows the specification of synonyms
      *                       that are equivalent and should be combined as OR
      *                       terms in each search, with the hit counts
      *                       consisting of the combined hits. These must be
      *                       either search_strings_a.
      * @param search_strings The first set of search strings to be combined
      *                       with the second set. The first dimension
      *                       corresponds to the index of the query. The second
      *                       dimension allows the specification of synonyms
      *                       that are equivalent and should be combined as OR
      *                       terms in each search, with the hit counts
      *                       consisting of the combined hits. These must be
      *                       either search_strings_a in the case of
      *                       cooccurrence, or search_strings_b in the case
      *                       of cross tabulation.
      * @param queries_index  The index in the first dimension of the queries
      *                       field where a record of the queries made is to
      *                       be added.
      * @return               The absolute number of hits corresponding to the
      *                       search_strings parameter. The first dimension is
      *                       the web service used (as per the web_services
      *                       field), the second dimension is the site searched
      *                       (as per the site_addresses field, this number is
      *                       not yet weighted), the third dimension corresponds
      *                       to the first_search_strings parameter and the
      *                       forth dimension corresponds to the
      *                       second_search_strings parameter.
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs
      */
     private long[][][][] findHitCountsTwoDimensional(String[][] first_search_strings,
          String[][] second_search_strings,
          int queries_index)
          throws Exception
     {
          // Calculate the number of queries that need to be performed per
          // web service per search engine
          int number_queries = 0;
          if (!is_cross_tabulation)
               for (int i = 1; i < first_search_strings.length; i++)
                    for (int j = 0; j < i; j++)
                         number_queries++;
          else number_queries = first_search_strings.length * second_search_strings.length;

          // Prepare arrays to store the results and the queries used
          long[][][][] results = new long[web_services.length][site_addresses.length][first_search_strings.length][second_search_strings.length];
          queries[queries_index] = new String[web_services.length][site_addresses.length][number_queries];

          // Calculate the number of tasks to be performed and intialize
          // the index of the current task and the progress bar
          int number_sub_tasks = web_services.length * site_addresses.length * number_queries;
          int current_task = 0;
          String task_count_info = " (" + number_sub_tasks + " of " + findRawNumberOfQueries(site_addresses.length, web_services.length) + " queries).";
          progress_bar.startNewSubTask(number_sub_tasks, "Acquiring combined hit counts" + task_count_info);

          // Go through each web service
          for (int service_i = 0; service_i < web_services.length; service_i++)
          {
               // Go through each specific site
               for (int site_i = 0; site_i < site_addresses.length; site_i++)
               {
                    // To number the queries field
                    int current_queries_index = 0;

                    // Set the site to search (or the whole network)
                    web_services[service_i].setSpecificSiteToSearch(site_addresses[site_i]);

                    // Search each query for co-occurrence
                    if (!is_cross_tabulation)
                    {
                         for (int query_i = 1; query_i < first_search_strings.length; query_i++)
                         {
                              for (int query_j = 0; query_j < query_i; query_j++)
                              {
                                   // Prepare the query
                                   String[] these_search_strings = getQueryTerms(first_search_strings[query_i], second_search_strings[query_j]);

                                   // To store the query actually used by the web service
                                   String[] this_actual_query_used = new String[1];

                                   // Submit the query and archive it in the query field
                                   verifyNotCancelled();
                                   results[service_i][site_i][query_i][query_j] =
                                        web_services[service_i].getNumberHits(these_search_strings, this_actual_query_used, TRIES_BEFORE_REPORTING_ERROR);

                                   // Store the actual query used
                                   queries[queries_index][service_i][site_i][current_queries_index] = this_actual_query_used[0];
                                   current_queries_index++;

                                   // Store the reflected version of the result
                                   results[service_i][site_i][query_j][query_i] = results[service_i][site_i][query_i][query_j];

                                   // Update the progress bar
                                   current_task++;
                                   progress_bar.setSubTaskProgressValue(current_task);
                              }
                         }
                    }

                    // Search each query for cross tabulation
                    else
                    {
                         for (int query_i = 0; query_i < first_search_strings.length; query_i++)
                         {
                              for (int query_j = 0; query_j < second_search_strings.length; query_j++)
                              {
                                   // Prepare the query
                                   String[] these_search_strings = getQueryTerms(first_search_strings[query_i], second_search_strings[query_j]);

                                   // To store the query actually used by the web service
                                   String[] this_actual_query_used = new String[1];

                                   // Submit the query and archive it in the query field
                                   verifyNotCancelled();
                                   results[service_i][site_i][query_i][query_j] =
                                        web_services[service_i].getNumberHits(these_search_strings, this_actual_query_used, TRIES_BEFORE_REPORTING_ERROR);

                                   // Store the actual query used
                                   queries[queries_index][service_i][site_i][current_queries_index] = this_actual_query_used[0];
                                   current_queries_index++;

                                   // Update the progress bar
                                   current_task++;
                                   progress_bar.setSubTaskProgressValue(current_task);
                              }
                         }
                    }
               }
          }

          // Return the results
          return results;
     }


     /**
      * Find the set of terms to search for in one particular search. This
      * takes into account the given synonyms as well as the settings of the
      * required_filter_strings_basic and required_filter_strings_pattern_based
      * fields.
      *
      * <p><b>IMPORTANT:</b> This method makes the assumption that web services
      * enclose strings in quotes to make them literal and use the OR operator.
      *
      * @param synonyms_a  A set of search terms that are each considered
      *                    equivalent, or just one search term. This parameter
      *                    should only be non-null if C_a or C_a_b is being
      *                    calculated. May only be from the search_strings_a
      *                    field.
      * @param synonyms_b  A set of search terms that are each considered
      *                    equivalent, or just one search term. This parameter
      *                    should only be non-null if C_b or C_a_b is being
      *                    calculated. May be from the search_strings_a field
      *                    for co-occurrence and from the search_strings_b
      *                    field for cross tabulation.
      * @return            The set of query terms to use.
      */
     private String[] getQueryTerms(String[] synonyms_a, String[] synonyms_b)
     {
          // For basic search term and synonyms
          int number_terms = 0;
          if (synonyms_a != null) number_terms++;
          if (synonyms_b != null) number_terms++;

          // Terms for the required_filter_strings_basic and
          // required_filter_strings_pattern_based
          if (required_filter_strings_basic != null) number_terms += required_filter_strings_basic.length;
          if (required_filter_strings_pattern_based != null) number_terms += required_filter_strings_pattern_based.length;

          // The query terms to return
          String[] query_terms = new String[number_terms];
          int current_indice = 0;

          // Prepare synonyms_1
          if (synonyms_a != null)
          {
               if (synonyms_a.length == 1)
                    query_terms[current_indice] = synonyms_a[0];
               else
               {
                    query_terms[current_indice] = "";
                    for (int i = 0; i < synonyms_a.length; i++)
                    {
                         if (i != 0)
                              query_terms[current_indice] += " OR ";
                         query_terms[current_indice] += "\"" + synonyms_a[i] + "\"";
                    }
               }
               current_indice++;
          }

          // Prepare synonyms_2
          if (synonyms_b != null)
          {
               if (synonyms_b.length == 1)
                    query_terms[current_indice] = synonyms_b[0];
               else
               {
                    query_terms[current_indice] = "";
                    for (int i = 0; i < synonyms_b.length; i++)
                    {
                         if (i != 0)
                              query_terms[current_indice] += " OR ";
                         query_terms[current_indice] += "\"" + synonyms_b[i] + "\"";
                    }
               }
               current_indice++;
          }

          // Add basic required filter strings
          if (required_filter_strings_basic != null)
               for (int i = 0; i < required_filter_strings_basic.length; i++)
               {
               if (query_terms[current_indice] == null) query_terms[current_indice] = "";
               query_terms[current_indice] += "\"" + required_filter_strings_basic[i] + "\"";
               current_indice++;
               }

          // Add pattern-based filter strings
          if (required_filter_strings_pattern_based != null)
               for (int i = 0; i < required_filter_strings_pattern_based.length; i++)
               {
               query_terms[current_indice] = "\"";
               for (int j = 0; j < required_filter_strings_pattern_based[i].length; j++)
               {
                    if (required_filter_strings_pattern_based[i][j].equals("<PRIMARY_SEARCH_STRING>"))
                    {
                         if (synonyms_a != null)
                              query_terms[current_indice] += synonyms_a[0];
                    }
                    else if (required_filter_strings_pattern_based[i][j].equals("<SECONDARY_SEARCH_STRING>"))
                    {
                         if (synonyms_b != null)
                              query_terms[current_indice] += synonyms_b[0];
                    }
                    else
                         query_terms[current_indice] += required_filter_strings_pattern_based[i][j];
               }
               query_terms[current_indice] += "\"";
               current_indice++;
               }

          // Return the results
          return query_terms;
     }


     /**
      * Combine C_a, C_b and/or C_a_b across web services (potentially
      * normalized) and web sites (potentially weighted and/or normalized). In
      * other words, calculate C_a_combined, C_b_combined and/or C_a_b_combined.
      */
     private void findCombinedCounts()
     {
          // Find the web service normalizing multiplier. This will be 1.0 for
          // all web services if the normalize_across_web_services field is
          // false. If it is true, then the multipler of the web service with
          // the lowest number of total hits will be 1.0, and the others will
          // be scaled down correspondingly.
          double[] ws_normalizing_multiplier = new double[web_services.length];
          for (int i = 0; i < ws_normalizing_multiplier.length; i++)
               ws_normalizing_multiplier[i] = 1.0;
          if (normalize_across_web_services)
          {
               long[] total_hits_per_service = new long[web_services.length];
               for (int service_i = 0; service_i < total_hits_per_service.length; service_i++)
                    total_hits_per_service[service_i] = 0;
               for (int service_i = 0; service_i < web_services.length; service_i++)
               {
                    for (int site_i = 0; site_i < site_addresses.length; site_i++)
                    {
                         if (C_a_b != null)
                              for (int i = 0; i < C_a_b[service_i][site_i].length; i++)
                                   for (int j = 0; j < C_a_b[service_i][site_i][i].length; j++)
                                        total_hits_per_service[service_i] += C_a_b[service_i][site_i][i][j];
                         if (C_a != null)
                              for (int i = 0; i < C_a[service_i][site_i].length; i++)
                                   total_hits_per_service[service_i] += C_a[service_i][site_i][i];
                         if (C_b != null)
                              for (int i = 0; i < C_b[service_i][site_i].length; i++)
                                   total_hits_per_service[service_i] += C_b[service_i][site_i][i];
                    }
               }

               long smallest = total_hits_per_service[0];
               for (int i = 0; i < total_hits_per_service.length; i++)
                    if (total_hits_per_service[i] < smallest)
                         smallest = total_hits_per_service[i];

               for (int i = 0; i < ws_normalizing_multiplier.length; i++)
               {
                    if (total_hits_per_service[i] == 0)
                         ws_normalizing_multiplier[i] = 0.0;
                    else
                         ws_normalizing_multiplier[i] = (1.0 / ((double) total_hits_per_service[i])) * ((double) smallest);
               }
          }

          // Find the web site normalizing multiplier. This will be 1.0 for
          // all web site if the normalize_across_sites field is
          // false. If it is true, then the multipler of the web site with
          // the lowest number of total hits will be 1.0, and the others will
          // be scaled down correspondingly.
          double[] site_normalizing_multiplier = new double[site_addresses.length];
          for (int i = 0; i < site_normalizing_multiplier.length; i++)
               site_normalizing_multiplier[i] = 1.0;
          if (normalize_across_sites)
          {
               long[] total_hits_per_site = new long[site_addresses.length];
               for (int site_i = 0; site_i < total_hits_per_site.length; site_i++)
                    total_hits_per_site[site_i] = 0;
               for (int site_i = 0; site_i < site_addresses.length; site_i++)
               {
                    for (int service_i = 0; service_i < web_services.length; service_i++)
                    {
                         if (C_a_b != null)
                              for (int i = 0; i < C_a_b[service_i][site_i].length; i++)
                                   for (int j = 0; j < C_a_b[service_i][site_i][i].length; j++)
                                        total_hits_per_site[site_i] += C_a_b[service_i][site_i][i][j];
                         if (C_a != null)
                              for (int i = 0; i < C_a[service_i][site_i].length; i++)
                                   total_hits_per_site[site_i] += C_a[service_i][site_i][i];
                         if (C_b != null)
                              for (int i = 0; i < C_b[service_i][site_i].length; i++)
                                   total_hits_per_site[site_i] += C_b[service_i][site_i][i];
                    }
               }

               long smallest = total_hits_per_site[0];
               for (int i = 0; i < total_hits_per_site.length; i++)
                    if (total_hits_per_site[i] < smallest)
                         smallest = total_hits_per_site[i];

               for (int i = 0; i < site_normalizing_multiplier.length; i++)
               {
                    if (total_hits_per_site[i] == 0)
                         site_normalizing_multiplier[i] = 0.0;
                    else
                         site_normalizing_multiplier[i] = (1.0 / ((double) total_hits_per_site[i])) * ((double) smallest);
               }
          }

          // Find the combined and weighted counts for C_a_b, if appropriate
          if (C_a_b != null)
          {
               C_a_b_combined_processed = new double[C_a_b[0][0].length][C_a_b[0][0][0].length];
               for (int i = 0; i < C_a_b_combined_processed.length; i++)
                    for (int j = 0; j < C_a_b_combined_processed[i].length; j++)
                    {
                    double total = 0.0;
                    for (int service_i = 0; service_i < web_services.length; service_i++)
                         for (int site_i = 0; site_i < site_addresses.length; site_i++)
                              total += ((double) C_a_b[service_i][site_i][i][j]) * ws_normalizing_multiplier[service_i] * site_normalizing_multiplier[site_i] * site_weightings[site_i];
                    C_a_b_combined_processed[i][j] = total;
                    }
          }

          // Find the combined and weighted counts for C_a, if appropriate
          if (C_a != null)
          {
               C_a_combined_processed = new double[C_a[0][0].length];
               for (int i = 0; i < C_a_combined_processed.length; i++)
               {
                    double total = 0.0;
                    for (int service_i = 0; service_i < web_services.length; service_i++)
                         for (int site_i = 0; site_i < site_addresses.length; site_i++)
                              total += ((double) C_a[service_i][site_i][i]) * ws_normalizing_multiplier[service_i] * site_normalizing_multiplier[site_i] * site_weightings[site_i];
                    C_a_combined_processed[i] = total;
               }
          }

          // Find the combined and weighted counts for C_b, if appropriate
          if (C_b != null)
          {
               C_b_combined_processed = new double[C_b[0][0].length];
               for (int i = 0; i < C_b_combined_processed.length; i++)
               {
                    double total = 0.0;
                    for (int service_i = 0; service_i < web_services.length; service_i++)
                         for (int site_i = 0; site_i < site_addresses.length; site_i++)
                              total += ((double) C_b[service_i][site_i][i]) * ws_normalizing_multiplier[service_i] * site_normalizing_multiplier[site_i] * site_weightings[site_i];
                    C_b_combined_processed[i] = total;
               }
          }
     }


     /**
      * Returns the text of an HTML file detailing the information specified
      * in the reports_to_generate field. This report is intended to be
      * displayed in the GUI's Results Panel. This information can include:
      *
      * <li>The final feature values, as stored in the frequency_scores field.
      *
      * <li>The combined raw hit counts. These consist of C_a, C_b and C_a_b,
      * depending upon which were used. The hits for all web services and
      * network sites are combined (weighted, in the latter class).
      *
      * <li>The individual raw hit counts. These consist of C_a, C_b and C_a_b,
      * depending upon which were used. The separate values for each web service
      * and network site specified are each specified individually.
      *
      * <li>The search queries used during feature extractoin, as stored in the
      * queries field. Note that these queries will not include information that
      * was stored in the actual web service objects themselves.
      *
      * <li>The search settings used. These include the details of all of the
      * search parameters and terms that the feature edxtraction was based upon.
      * These utilize a variety of fields.
      *
      * <p>Note that this method should only be called when all processing has
      * been completed and the queries, C_a, C_b, C_a_b and frequency_scores
      * fields have all been filled.
      *
      * <p>This method should be called as the last overall task in the
      * progress_bar. The corresponding subtasks in the progress_bar are kept
      * updated as processing occurs.
      *
      * @return     A string representing the text of a full HTML file that can
      *             be saved or displayed.
      */
     private String generateHTMLResultsReport()
     {
          // Prepare a title that includes the date of extraction
          java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
          String date = date_format.format(new java.util.Date());

          // Acquire the beginning of the report
          StringBuffer html_report = HTMLWriter.startNewHTMLFile("#e4e4e4", "#000000", "jWebMiner Feature Extraction Results " + date, true);
          HTMLWriter.addHorizontalRule(html_report);




          // Find the number of subtasks to perform
          int number_sub_tasks = 0;
          for (int i = 0; i < reports_to_generate.length; i++)
               if (reports_to_generate[i]) number_sub_tasks++;
          if (reports_to_generate[2] && reports_to_generate[3])
               number_sub_tasks--;

          // Note if no reports are to be generated
          if (number_sub_tasks == 0)
          {
               HTMLWriter.addParagraph("No reports requested by the user, as specified in the Options Panel.", html_report);
               HTMLWriter.addParagraph("Feature values are, however, available to be saved as Weka ARFF, ACE XML or newline delimited text files if desired.", html_report);
          }

          // Prepare the progress bar
          progress_bar.startNewSubTask(number_sub_tasks, "Preparing reports");
          int current_task = 0;

          // Report the final feature values
          if (reports_to_generate[0])
          {
               // Prepare the column headings
               String[] column_headings = getColumnLabels();

               // Prepare the row headings
               String[] row_headings = getRowLabels(false);

               // Convert scores to strings
               String[][] string_frequency_scores = new String[frequency_scores.length][frequency_scores[0].length + 1];
               for (int i = 0; i < string_frequency_scores.length; i++)
                    for (int j = 0; j < string_frequency_scores[i].length; j++)
                    {
                    if (j == 0)
                         string_frequency_scores[i][j] = row_headings[i];
                    else
                    {
                         if (!is_cross_tabulation && i == j - 1)
                              string_frequency_scores[i][j] = "-";
                         else
                              string_frequency_scores[i][j] = String.valueOf(frequency_scores[i][j - 1]);
                    }
                    }


            if (lastfm_enabled && is_cross_tabulation && (yahoo_application_id != null || google_license_key != null))
            {
                HTMLWriter.addParagraph("<h2>Averaged Websearch and Last.FM results</h2>", html_report);
                HTMLWriter.addTableHighlightingHighestInRow(averaged_websearch_lastfm, column_headings, false, html_report);
                HTMLWriter.addHorizontalRule(html_report);
            }

            if(lastfm_enabled && is_cross_tabulation)
            {
                HTMLWriter.addParagraph("<h2>Last.FM tags normalized ranking score</h2>", html_report);
                HTMLWriter.addTableHighlightingHighestInRow(lastfm_report_table_norm_with_columns, column_headings, false, html_report);
                HTMLWriter.addHorizontalRule(html_report);
            }
            if (yahoo_application_id != null || google_license_key != null)
            {
                HTMLWriter.addParagraph("<h2>Websearch normalized feature score</h2>", html_report);
                HTMLWriter.addTableHighlightingHighestInRow(string_frequency_scores, column_headings, true, html_report);
                HTMLWriter.addHorizontalRule(html_report);
            }

               // Update the progress bar
               current_task++;
               progress_bar.setSubTaskProgressValue(current_task);
          }

          // Report the combined processed hit counts
          if (reports_to_generate[5])
          {
               // Write the combined counts for C_a_b_combined_processed, if appropriate
               if (C_a_b_combined_processed != null)
               {
                    // Prepare the column headings
                    String[] column_headings = getColumnLabels();

                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Convert counts to strings and convert diagonal to "-" in
                    // the case of co-occurrence processing
                    String[][] report = new String[C_a_b_combined_processed.length][C_a_b_combined_processed[0].length + 1];
                    for (int i = 0; i < report.length; i++)
                         for (int j = 0; j < report[i].length; j++)
                         {
                         if (j == 0)
                              report[i][j] = row_headings[i];
                         else
                         {
                              if (!is_cross_tabulation && i == j - 1)
                                   report[i][j] = "-";
                              else report[i][j] = mckay.utilities.staticlibraries.StringMethods.getRoundedDoubleWithCommas(C_a_b_combined_processed[i][j - 1], 1);
                         }
                         }

                    // Write the HTML
                    HTMLWriter.addParagraph("<h2>Hit counts for C(x,y) combined across all web services and sites, including web service normalization and web site weighting and/or normalization:</h2>", html_report);
                    HTMLWriter.addTable(report, column_headings, true, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Write the combined counts for C_a_combined_processed, if appropriate
               if (C_a_combined_processed != null)
               {
                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Convert counts to strings
                    String[][] report = new String[C_a_combined_processed.length][2];
                    for (int i = 0; i < report.length; i++)
                    {
                         report[i][0] = row_headings[i];
                         report[i][1] = mckay.utilities.staticlibraries.StringMethods.getRoundedDoubleWithCommas(C_a_combined_processed[i], 1);
                    }

                    // Write the HTML
                    HTMLWriter.addParagraph("<h2>Hit counts for C(a) combined across web services and sites, including web service normalization and web site weighting and/or normalization:</h2>", html_report);
                    HTMLWriter.addTable(report, null, true, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Write the combined counts for C_b_combined_processed, if appropriate
               if (C_b_combined_processed != null)
               {
                    // Prepare the row headings
                    String[] row_headings = getRowLabels(true);

                    // Convert counts to strings
                    String[][] report = new String[C_b_combined_processed.length][2];
                    for (int i = 0; i < report.length; i++)
                    {
                         report[i][0] = row_headings[i];
                         report[i][1] = mckay.utilities.staticlibraries.StringMethods.getRoundedDoubleWithCommas(C_b_combined_processed[i], 1);
                    }

                    // Write the HTML
                    HTMLWriter.addParagraph("<h2>Hit counts for C(b) combined across web services and sites, including web service normalization and web site weighting and/or normalization:</h2>", html_report);
                    HTMLWriter.addTable(report, null, true, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Update the progress bar
               current_task++;
               progress_bar.setSubTaskProgressValue(current_task);
          }

          // Report the combined raw hit counts
          if (reports_to_generate[1])
          {
               // Write the combined counts for C_a_b, if appropriate
               if (C_a_b != null)
               {
                    // Prepare the column headings
                    String[] column_headings = getColumnLabels();

                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Convert counts to strings
                    String[][] c_a_b_combined = new String[C_a_b[0][0].length][C_a_b[0][0][0].length + 1];
                    for (int i = 0; i < c_a_b_combined.length; i++)
                         for (int j = 0; j < c_a_b_combined[i].length; j++)
                         {
                         if (j == 0)
                              c_a_b_combined[i][j] = row_headings[i];
                         else
                         {
                              long total = 0;
                              for (int m = 0; m < C_a_b.length; m++)
                                   for (int n = 0; n < C_a_b[m].length; n++)
                                        total += C_a_b[m][n][i][j - 1];
                              c_a_b_combined[i][j] = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(total);

                              // Set zeroes to "-" on diagonal for co-occurrence
                              if (!is_cross_tabulation && i == j - 1)
                                   c_a_b_combined[i][j] = "-";
                         }
                         }

                    // Write the HTML
                    HTMLWriter.addParagraph("<h2>Hit counts for C(x,y) combined across all web services and sites, <i>not</i> including web service normalization or web site weighting and/or normalization:</h2>", html_report);
                    HTMLWriter.addTable(c_a_b_combined, column_headings, true, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Write the combined counts for C_a, if appropriate
               if (C_a != null)
               {
                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Convert counts to strings
                    String[][] c_a_combined = new String[C_a[0][0].length][2];
                    for (int i = 0; i < c_a_combined.length; i++)
                    {
                         c_a_combined[i][0] = row_headings[i];

                         long total = 0;
                         for (int m = 0; m < C_a.length; m++)
                              for (int n = 0; n < C_a[m].length; n++)
                                   total += C_a[m][n][i];
                         c_a_combined[i][1] = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(total);
                    }

                    // Write the HTML
                    HTMLWriter.addParagraph("<h2>Hit counts for C(a) combined across web services and sites, <i>not</i> including web service normalization or web site weighting and/or normalization:</h2>", html_report);
                    HTMLWriter.addTable(c_a_combined, null, true, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Write the combined counts for C_b, if appropriate
               if (C_b != null)
               {
                    // Prepare the row headings
                    String[] row_headings = getRowLabels(true);

                    // Convert counts to strings
                    String[][] c_b_combined = new String[C_b[0][0].length][2];
                    for (int i = 0; i < c_b_combined.length; i++)
                    {
                         c_b_combined[i][0] = row_headings[i];

                         long total = 0;
                         for (int m = 0; m < C_b.length; m++)
                              for (int n = 0; n < C_b[m].length; n++)
                                   total += C_b[m][n][i];
                         c_b_combined[i][1] = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(total);
                    }

                    // Write the HTML
                    HTMLWriter.addParagraph("<h2>Hit counts for C(b) combined across web services and sites, <i>not</i> including web service normalization or web site weighting and/or normalization:</h2>", html_report);
                    HTMLWriter.addTable(c_b_combined, null, true, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Update the progress bar
               current_task++;
               progress_bar.setSubTaskProgressValue(current_task);
          }

          // Report the individual raw hit counts and/or queries generated
          if ((reports_to_generate[2] || reports_to_generate[3])  && (yahoo_application_id != null || google_license_key != null))
          {
               // Note that queries reported may be incomplete
               if (reports_to_generate[3])
               {
                    HTMLWriter.addParagraph("<h2>Note that reported queries may not include all search parameters, as it is sometimes necessary to include them directly in web search objects in order to avoid exceeding maximum query lengths.</h2>", html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Prepare names of web sites and web services
               String[] web_service_names = new String[web_services.length];
               String[] web_site_names = new String[site_addresses.length];
               for (int i = 0; i < web_services.length; i++)
                    web_service_names[i] = web_services[i].getSeachServiceName();
               for (int i = 0; i < site_addresses.length; i++)
               {
                    if (site_addresses[i] == null) web_site_names[i] = "on the whole network:";
                    else web_site_names[i] = " limited to the " + site_addresses[i] + " site:";
               }

               // Prepare the report titles
               String[][][] report_titles = new String[queries.length][web_service_names.length][web_site_names.length];
               for (int i = 0; i < report_titles.length; i ++)
               {
                    // Determine the type of calculation the report is for
                    String title_beginning = null;
                    if (i == 0 && C_a != null) title_beginning = "C(a)";
                    else if (i == 1 && C_b != null) title_beginning = "C(b)";
                    else if (i == 2 && C_a_b != null) title_beginning = "C(x,y)";

                    // Determine the first part of the title
                    if (title_beginning != null)
                    {
                         if (reports_to_generate[2] && reports_to_generate[3])
                              title_beginning = "Individual raw hit counts and queries used for " + title_beginning;
                         else if (reports_to_generate[2])
                              title_beginning = "Individual raw hit counts for " + title_beginning;
                         else if (reports_to_generate[3])
                              title_beginning = "Queries used for " + title_beginning;
                    }

                    // Finalize the titles
                    for (int j = 0; j < web_service_names.length; j++)
                         for (int k = 0; k < web_site_names.length; k++)
                         {
                         report_titles[i][j][k] = title_beginning + " for the " + web_service_names[j] + " web service " + web_site_names[k];
                         report_titles[i][j][k] = HTMLWriter.convertSpecialCharacters(report_titles[i][j][k]);
                         report_titles[i][j][k] = "<h2>" + report_titles[i][j][k] + "</h2>";
                         }
               }

               // Write the individual raw hit counts and/or queries for C_a_b,
               // if appropriate
               if (C_a_b != null)
               {
                    // Prepare the column headings
                    String[] column_headings = getColumnLabels();

                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Find entries for each web service and each web site
                    String[][][][] results_table = new String[C_a_b.length][C_a_b[0].length][C_a_b[0][0].length][C_a_b[0][0][0].length + 1];
                    for (int i = 0; i < results_table.length; i++)
                    {
                         for (int j = 0; j < results_table[i].length; j++)
                         {
                              int queries_index = 0;
                              for (int m = 0; m < results_table[i][j].length; m++)
                              {
                                   for (int n = 0; n < results_table[i][j][m].length; n++)
                                   {
                                        // Add the row heading
                                        if (n == 0)
                                             results_table[i][j][m][n] = row_headings[m];

                                        // Add the non-heading entries
                                        else
                                        {
                                             results_table[i][j][m][n] = "";
                                             if (reports_to_generate[2])
                                             {
                                                  // Set zeroes to "-" on diagonal for co-occurrence
                                                  if (!is_cross_tabulation && m == n - 1)
                                                       results_table[i][j][m][n] = "-";

                                                  // Store numbers
                                                  else
                                                       results_table[i][j][m][n] = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(C_a_b[i][j][m][n - 1]);

                                                  // Add a line break if queries are to be reported
                                                  if (reports_to_generate[3])
                                                       results_table[i][j][m][n] += "<br>";
                                             }

                                             // Fill in the queries used for cross tabulation case
                                             if (reports_to_generate[3] && is_cross_tabulation)
                                             {
                                                  results_table[i][j][m][n] += HTMLWriter.convertSpecialCharacters(queries[2][i][j][queries_index]);
                                                  queries_index++;
                                             }
                                        }
                                   }
                              }

                              // Fill in the queries used for co-occurrence case
                              if (reports_to_generate[3] && !is_cross_tabulation)
                              {
                                   for (int m = 1; m < results_table[i][j].length; m++)
                                   {
                                        for (int n = 0; n < m; n++)
                                        {
                                             results_table[i][j][m][n + 1] += HTMLWriter.convertSpecialCharacters(queries[2][i][j][queries_index]);
                                             results_table[i][j][n][m + 1] += HTMLWriter.convertSpecialCharacters(queries[2][i][j][queries_index]);
                                             queries_index++;
                                        }
                                   }
                                   for (int m = 0; m < results_table[i][j].length; m++)
                                        results_table[i][j][m][m + 1] += "-";
                              }
                         }
                    }

                    // Write the HTML
                    for (int i = 0; i < results_table.length; i++)
                         for (int j = 0; j < results_table[i].length; j++)
                         {
                         HTMLWriter.addParagraph(report_titles[2][i][j], html_report);
                         HTMLWriter.addTable(results_table[i][j], column_headings, true, html_report);
                         HTMLWriter.addHorizontalRule(html_report);
                         }
               }

               // Write the individual raw hit counts and/or queries for C_a, if
               // appropriate
               if (C_a != null)
               {
                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Find entries for each web service and each web site
                    String[][][][] results_table = new String[C_a.length][C_a[0].length][C_a[0][0].length][2];
                    for (int i = 0; i < results_table.length; i++)
                         for (int j = 0; j < results_table[i].length; j++)
                              for (int m = 0; m < results_table[i][j].length; m++)
                                   for (int n = 0; n < results_table[i][j][m].length; n++)
                                   {
                         // Add the row heading
                         if (n == 0)
                              results_table[i][j][m][n] = row_headings[m];

                         // Add the non-heading entry
                         else
                         {
                              results_table[i][j][m][n] = "";
                              if (reports_to_generate[2])
                              {
                                   results_table[i][j][m][n] = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(C_a[i][j][m]);
                                   if (reports_to_generate[3])
                                        results_table[i][j][m][n] += "<br>";
                              }
                              if (reports_to_generate[3])
                                   results_table[i][j][m][n] += HTMLWriter.convertSpecialCharacters(queries[0][i][j][m]);
                         }
                                   }

                    // Write the HTML
                    for (int i = 0; i < results_table.length; i++)
                         for (int j = 0; j < results_table[i].length; j++)
                         {
                         HTMLWriter.addParagraph(report_titles[0][i][j], html_report);
                         HTMLWriter.addTable(results_table[i][j], null, true, html_report);
                         HTMLWriter.addHorizontalRule(html_report);
                         }
               }

               // Write the individual raw hit counts and/or queries for C_b, if
               // appropriate
               if (C_b != null)
               {
                    // Prepare the row headings
                    String[] row_headings = getRowLabels(false);

                    // Find entries for each web service and each web site
                    String[][][][] results_table = new String[C_b.length][C_b[0].length][C_b[0][0].length][2];
                    for (int i = 0; i < results_table.length; i++)
                         for (int j = 0; j < results_table[i].length; j++)
                              for (int m = 0; m < results_table[i][j].length; m++)
                                   for (int n = 0; n < results_table[i][j][m].length; n++)
                                   {
                         // Add the row heading
                         if (n == 0)
                              results_table[i][j][m][n] = row_headings[m];

                         // Add the non-heading entry
                         else
                         {
                              results_table[i][j][m][n] = "";
                              if (reports_to_generate[2])
                              {
                                   results_table[i][j][m][n] = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(C_b[i][j][m]);
                                   if (reports_to_generate[3])
                                        results_table[i][j][m][n] += "<br>";
                              }
                              if (reports_to_generate[3])
                                   results_table[i][j][m][n] += HTMLWriter.convertSpecialCharacters(queries[1][i][j][m]);
                         }
                                   }

                    // Write the HTML
                    for (int i = 0; i < results_table.length; i++)
                         for (int j = 0; j < results_table[i].length; j++)
                         {
                         HTMLWriter.addParagraph(report_titles[1][i][j], html_report);
                         HTMLWriter.addTable(results_table[i][j], null, true, html_report);
                         HTMLWriter.addHorizontalRule(html_report);
                         }
               }

               // Update the progress bar
               current_task++;
               progress_bar.setSubTaskProgressValue(current_task);
          }

          // Report the search settings used
          if (reports_to_generate[4])
          {
               // Prepare the primary search string and synonyms report
               HTMLWriter.addParagraph("<h2>Primary search strings and synonyms:</h2>", html_report);
               String[][] primary_search_strings_parsed = HTMLWriter.addLineBreaksToArray(search_strings_a);
               HTMLWriter.addTable(primary_search_strings_parsed, null, false, html_report);
               HTMLWriter.addHorizontalRule(html_report);

               // Prepare the secondary search string and synonyms report
               if (search_strings_b != null)
               {
                    HTMLWriter.addParagraph("<h2>Secondary search strings and synonyms:</h2>", html_report);
                    String[][] secondary_search_strings_parsed = HTMLWriter.addLineBreaksToArray(search_strings_b);
                    HTMLWriter.addTable(secondary_search_strings_parsed, null, false, html_report);
                    HTMLWriter.addHorizontalRule(html_report);
               }

               // Prepare the specific sites and weightings report
               HTMLWriter.addParagraph("<h2>Site weightings:</h2>", html_report);
               String[][] site_weigntings_table = new String[site_addresses.length][2];
               for (int i = 0; i < site_weigntings_table.length; i++)
               {
                    if (site_addresses[i] == null)
                         site_weigntings_table[i][0] = "<font color=\"#0000FF\">WHOLE NETWORK</font>";
                    else
                    {
                         site_weigntings_table[i][0] = site_addresses[i];
                         try
                         {site_weigntings_table[i][0] = java.net.URLDecoder.decode(site_weigntings_table[i][0], "UTF-8");}
                         catch (Exception e)
                         {}
                         site_weigntings_table[i][0] = HTMLWriter.convertSpecialCharacters(site_weigntings_table[i][0]);
                    }

                    site_weigntings_table[i][1] = mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(site_weightings[i], 3);
               }
               String[] column_headings = {"Site", "Normalized Weight"};
               HTMLWriter.addTable(site_weigntings_table, column_headings, false, html_report);
               HTMLWriter.addHorizontalRule(html_report);

               // Prepare the non-pattern based required filter strings report
               if (required_filter_strings_basic == null)
                    HTMLWriter.addParagraph("<h2>No non-pattern-based required filter strings specified</h2>", html_report);
               else
               {
                    HTMLWriter.addParagraph("<h2>Non-pattern-based required filter strings:</h2>", html_report);
                    HTMLWriter.addList(HTMLWriter.convertSpecialCharacters(required_filter_strings_basic), false, html_report);
               }
               HTMLWriter.addHorizontalRule(html_report);

               // Prepare the pattern based required filter strings report
               if (required_filter_strings_pattern_based == null)
                    HTMLWriter.addParagraph("<h2>No pattern-based required filter strings specified</h2>", html_report);
               else
               {
                    HTMLWriter.addParagraph("<h2>Pattern-based required filter strings:</h2>", html_report);
                    String[] pattern_based = new String[required_filter_strings_pattern_based.length];
                    for (int i = 0; i < pattern_based.length; i++)
                    {
                         pattern_based[i] = "";
                         for (int j = 0; j < required_filter_strings_pattern_based[i].length; j++)
                         {
                              if (required_filter_strings_pattern_based[i][j].equals("<PRIMARY_SEARCH_STRING>") ||
                                   required_filter_strings_pattern_based[i][j].equals("<SECONDARY_SEARCH_STRING>"))
                                   pattern_based[i] += "<font color=\"#0000FF\">";
                              pattern_based[i] += HTMLWriter.convertSpecialCharacters(required_filter_strings_pattern_based[i][j]);
                              if (required_filter_strings_pattern_based[i][j].equals("<PRIMARY_SEARCH_STRING>") ||
                                   required_filter_strings_pattern_based[i][j].equals("<SECONDARY_SEARCH_STRING>"))
                                   pattern_based[i] += "</font>";
                         }
                    }
                    HTMLWriter.addList(pattern_based, false, html_report);
               }
               HTMLWriter.addHorizontalRule(html_report);

               // Prepare the excluded filter strings report
               if (excluded_filter_strings == null)
                    HTMLWriter.addParagraph("<h2>No excluded filter strings specified</h2>", html_report);
               else
               {
                    HTMLWriter.addParagraph("<h2>Excluded filter strings:</h2>", html_report);
                    HTMLWriter.addList(HTMLWriter.convertSpecialCharacters(excluded_filter_strings), false, html_report);
               }
               HTMLWriter.addHorizontalRule(html_report);

               // Prepare the general settings report
               HTMLWriter.addParagraph("<h2>General extraction settings:</h2>", html_report);
               String[][] general_settings = new String[15][2];
               general_settings[0][0] = "Experiment type:";
               if (is_cross_tabulation) general_settings[0][1] = "Cross Tabulation";
               else general_settings[0][1] = "Co-occurrence";
               general_settings[1][0] = "Scoring function:";
               if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_1_CODE)
                    general_settings[1][1] = "S(a1,a2) = PC(a1,a2) / (1 + (SUMcfromAcnota1(PC(a1,c)) x SUMdfromAdnota2(PC(d,a2))))";
               else if (scoring_function_code == OptionsPanel.COOCCURRENCE_FUNCTION_2_CODE)
                    general_settings[1][1] = "S(a1,a2, c) = (PC(a1,a2) / PC(a2)) * (1 - (|PC(a1) - PC(a2)| / PC(c))) where c is the greatest PC(A)";
               else if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_1_CODE)
                    general_settings[1][1] = "S(a,b) = PC(a,b) / PC(a)";
               else if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_2_CODE)
                    general_settings[1][1] = "S(a,b) = PC(a,b) / PC(b)";
               else if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_3_CODE)
                    general_settings[1][1] = "S(a,b) = PC(a,b) / (1 + SUMcfromA(PC(c,b)))";
               else if (scoring_function_code == OptionsPanel.CROSS_TAB_FUNCTION_4_CODE)
                    general_settings[1][1] = "S(a,b) = PC(a,b) / (1 + SUMcfromB(PC(a,c)))";
               general_settings[2][0] = "Hits across web services normalized:";
               general_settings[2][1] = String.valueOf(normalize_across_web_services);
               general_settings[3][0] = "Hits across web sites normalized:";
               general_settings[3][1] = String.valueOf(normalize_across_sites);
               general_settings[4][0] = "Normalizations:";
               general_settings[4][1] = "";
               if (normalize_across_web_services) general_settings[4][1] += "Across web services";
               if (normalize_across_sites)
               {
                    if (!general_settings[4][1].equals("")) general_settings[4][1] += "<br>";
                    general_settings[4][1] += "Across web sites";
               }
               if (should_normalize_scores[0])
               {
                    if (!general_settings[4][1].equals("")) general_settings[4][1] += "<br>";

                    if (!should_normalize_scores[1]) general_settings[4][1] += "Features normalized overall";
                    else general_settings[4][1] += "Features normalized by row";
               }
               if (general_settings[4][1].equals("")) general_settings[4][1] = "None";
               general_settings[5][0] = "Web services used:";
               if (yahoo_application_id != null) general_settings[5][1] = "Yahoo! REST";
               if (google_license_key!= null)
               {
                    if (yahoo_application_id != null) general_settings[5][1] += "<br>Google SOAP";
                    else general_settings[5][1] = "Google SOAP";
               }
               if (lastfm_enabled)
               {
                    if (yahoo_application_id != null) general_settings[5][1] += "<br>Last.FM";
                    else general_settings[5][1] = "Last.FM";
               }

               general_settings[6][0] = "Search strings treated literally:";
               general_settings[6][1] = String.valueOf(literal_search);
               general_settings[7][0] = "Searches perfomed using OR instead of AND:";
               general_settings[7][1] = String.valueOf(or_based_search);
               general_settings[8][0] = "Non-matching similar hits included:";
               general_settings[8][1] = String.valueOf(include_non_matching);
               general_settings[9][0] = "Similar hits suppressed:";
               general_settings[9][1] = String.valueOf(suppress_similar_hits);
               general_settings[10][0] = "Adult content suppressed:";
               general_settings[10][1] = String.valueOf(suppress_adult_content);
               general_settings[11][0] = "Language limitation:";
               general_settings[11][1] = limit_to_language;
               general_settings[12][0] = "Country limitation:";
               general_settings[12][1] = limit_to_country;
               general_settings[13][0] = "Regional search service used:";
               general_settings[13][1] = region_to_search_from;
               general_settings[14][0] = "File type limitation:";
               general_settings[14][1] = limit_to_file_type;
               HTMLWriter.addTable(general_settings, null, true, html_report);
               HTMLWriter.addHorizontalRule(html_report);

               // Update the progress bar
               current_task++;
               progress_bar.setSubTaskProgressValue(current_task);
          }

          // Finalize the report
          HTMLWriter.endHTMLFile(html_report, false);

          // Return the report
          return html_report.toString();
     }


     /**
      * Returns the labels that can be used for the rows of C_a, C_b, C_a_b or
      * frequency_scores. These are all derived from search_strings_a, unless
      * the is_for_C_b is set to true, in which case they are derived from
      * search_strings_b. Synonyms are placed in the same entry, separated by a
      * <br> tag.
      *
      * @param      is_for_C_b     Whether or not the labels are for labelling
      *                            the C_b field.
      * @return                    The row labels.
      */
     private String[] getRowLabels(boolean is_for_C_b)
     {
          String[][] rows_2d;
          if (is_for_C_b)
               rows_2d = HTMLWriter.addLineBreaksToArray(search_strings_b);
          else
               rows_2d = HTMLWriter.addLineBreaksToArray(search_strings_a);

          String[] rows = new String[rows_2d.length];
          for (int i = 0; i < rows_2d.length; i++)
               rows[i] = rows_2d[i][0];
          return rows;
     }


     /**
      * Returns the labels that can be used for the columns C_a_b or
      * frequency_scores. These are derived from either search_strings_a or
      * search_strings_b, depending on whether a co-occurrence or cross
      * tabulation analysis is being performed (respectively). Synonyms
      * are placed in the same entry, separated by a <br> tag. A blank first
      * entry is provided in order to correspond to row names.
      *
      * @return     The column labels.
      */
     private String[] getColumnLabels()
     {
          String[] initial_results;
          if (is_cross_tabulation)
          {
               String[][] columns_2d = HTMLWriter.addLineBreaksToArray(search_strings_b);
               initial_results = new String[columns_2d.length];
               for (int i = 0; i < columns_2d.length; i++)
                    initial_results[i] = columns_2d[i][0];
          }
          else initial_results = getRowLabels(false);

          String[] final_results = new String[initial_results.length + 1];
          final_results[0] = "";
          for (int i = 0; i < initial_results.length; i++)
               final_results[i + 1] = initial_results[i];

          return final_results;
     }


     /**
      * Returns a 1-D array with the entries in each row of the given array
      * separated by " / ".
      *
      * @param source    The array to project into one-dimension.
      * @return          The 1-D array of strings.
      */
     private static String[] getResultsLabels(String[][] source)
     {
          String[] results = new String[source.length];
          for (int i = 0; i < results.length; i++)
          {
               results[i] = "";
               for (int j = 0; j < source[i].length; j++)
               {
                    if (j != 0) results[i] += " / ";
                    results[i] += source[i][j];
               }
          }
          return results;
     }
}
