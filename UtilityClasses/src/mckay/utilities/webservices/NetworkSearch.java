/*
 * NetworkSearch.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.webservices;

import java.math.BigInteger;
import javax.swing.JOptionPane;

/**
 * An abstract class for submitting various types of search queries to arbitrary
 * on-line services using arbitrary types of web service formats. Fields may be
 * set in order to set parameters for all subsequent searches performed by
 * objects of this class.
 *
 * @author Cory McKay
 */
public abstract class NetworkSearch
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * Whether all search queries performed by this object should be literal
      * searches (e.g. for the query "heavy metal", hits must have the two words
      * adjacent if the search is literal). This is also sometimes known as an
      * exact search or a phrase search. The default set by the constructor is
      * true.
      */
     protected boolean   literal_search;
     
     /**
      * Whether search queries performed by this object need only contain one
      * of the specified query words in order to result in a hit. If this is
      * true, then only one of the query strings must be present. If this is
      * false, then all of them must be present (although not necessarily in the
      * specified order, unless the literal_search field is true). The default
      * set by the constructor is false.
      */
     protected boolean   or_based_search;
     
     /**
      * Whether results returned by search queries performed by this object may
      * include hits that do not contain one or more of the specified search
      * string(s) but do contain terms very similar to them (e.g.
      * alternative spellings). The default set by the constructor is false.
      */
     protected boolean   include_similar_but_non_matching;
     
     /**
      * Strings to exclude in all search queries performed by this object (i.e
      * filter strings). Search hits may not contain these filter strings. These
      * excluded strings are treated as literal (i.e. must appear in the same
      * order). A value of null (the default set by the constructor) means that
      * no strings are excluded. No entries may be null.
      */
     protected String[]  strings_to_exclude;
     
     /**
      * Network site that will be exclusively searched in all search queries
      * performed by this object. A value of null means that the entire
      * available network should be searched. This is the default set by the
      * constructor.
      */
     protected String    specific_site;
     
     /**
      * Name of a language that hits must be in in order to be included in
      * search results. The language must be one of the terms in the
      * included_languages field. A value of "No Limitations" means that any
      * language is permissible. A value of null is not permitted. The default
      * value set by the constructor is "No Limitations".
      */
     protected String    limit_to_language;
     
     /**
      * Name of a country that sites must be in in order to be included in
      * search results. The country must be one of the terms in the
      * included_countries field. A value of "No Limitations" means that sites
      * in any country are permissible. A value of null is not permitted. The
      * default value set by the constructor is "No Limitations".
      */
     protected String    limit_to_country;
     
     /**
      * Name of a country where the search will be performed (i.e. where
      * the search service is located). Results are not limited to this country.
      * The country must be one of the terms in the included_countries field. An
      * entry of "No Limitations" causes the default service location to be
      * used. A value of null is not permitted. The default value set by the
      * constructor is "No Limitations".
      */
     protected String    region_to_search_from;
     
     /**
      * A file extension a document must have in order to be returned as a hit
      * in search results. The file type must be one of the terms in the
      * included_file_types field. An entry of "No Limitations" means that file
      * type will not by used to filter results. A value of null is not
      * permitted. The default value set by the constructor is "No Limitations".
      */
     protected String    limit_to_file_type;
     
     /**
      * Whether to suppress similar hits when reporting results. Similar in this
      * context means either:
      *
      * <ul><li>Sites with identical titles and/or descriptions.</li>
      * <li>Multiple hits from the same host.</li></ul>
      *
      * <p>The default value set by the constructor is false.
      */
     protected boolean   suppress_similar_hits;
     
     /**
      * Whether to suppress hits that are classified as containing adult
      * content by the search service in question. The default value set by the
      * constructor is true.
      */
     protected boolean   suppress_adult_content;
     
     /**
      * The languages that may be specified in searches.
      */
     public static final String[] included_languages =
     {"No Limitations", "English", "French", "Spanish", "Portuguese", "German", "Chinese", "Japanese", "Turkish", "Arabic"};
     
     /**
      * The countries that may be specified in searches.
      */
     public static final String[] included_countries =
     {"No Limitations", "Canada", "U.S.A.", "U.K.", "France", "Spain", "Germany", "Austria", "Brazil", "Japan", "China", "Turkey"};
     
     /**
      * The file types that may be specified in searches.
      */
     public static final String[] included_file_types =
     {"No Limitations", "html", "txt", "pdf", "doc", "ppt", "xls", "rss"};
     
     /**
      * The code used to identify searches using the GoogleWebSearch class that
      * extends this class.
      */
     public static final int       GOOGLE_SOAP_CODE    =    1;
     
     /**
      * The code used to identify searches using the YahooWebSearch class that
      * extends this class.
      */
     public static final int       YAHOO_REST_CODE     =    2;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of NetworkSearch with fields set to defaults.
      */
     public NetworkSearch()
     {
          literal_search = true;
          
          or_based_search = false;
          
          include_similar_but_non_matching = false;
          
          strings_to_exclude = null;
          
          specific_site = null;
          
          limit_to_language = "No Limitations";
          
          limit_to_country = "No Limitations";
          
          region_to_search_from = "No Limitations";
          
          limit_to_file_type = "No Limitations";
          
          suppress_similar_hits = false;
          
          suppress_adult_content = false;
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Sets whether all search queries performed by this object should be
      * literal searches (e.g. for the query "heavy metal", hits must have
      * the two words adjacent if the search is literal). The default set by
      * the constructor is true. Literal searches are also sometimes referred to
      * as exact or phrase searches.
      *
      * @param literal_search Whether or not literal searches are to be
      *                       performed.
      */
     public void setLiteralSearch(boolean literal_search)
     {
          this.literal_search = literal_search;
     }
     
     
     /**
      * Sets whether search queries performed by this object need only contain
      * one of the specified query words in order to result in a hit. If this is
      * true, then only one of the query strings must be present. If this is
      * false, then all of them must be present (although not necessarily in the
      * specified order, unless the literal_search field is true). The default
      * set by the constructor is false.
      *
      * @param or_based_search   Value to set field to.
      */
     public void setOrBasedSearch(boolean or_based_search)
     {
          this.or_based_search = or_based_search;
     }
     
     
     /**
      * Sets whether results returned by search queries performed by this object
      * may include hits that do not contain one or more of the specified search
      * string(s) but do contain terms very similar to them (e.g.
      * alternative spellings). The default set by the constructor is false.
      *
      * @param include_similar_but_non_matching   Value to set field to.
      */
     public void setIncludeSimilarButNonMatchingStrings(boolean include_similar_but_non_matching)
     {
          this.include_similar_but_non_matching = include_similar_but_non_matching;
     }
     
     
     /**
      * Sets strings to exclude in all search queries performed by this object.
      * (i.e. filter strings). Search hits may not contain these filter strings.
      * Note that these will each be treated literally regardless of whether the
      * search strings are treated literally.
      *
      * <p>A value of null (the default set by the constructor) means that no
      * strings are excluded. No entries may be null.
      *
      * @param strings_to_exclude  Unformatted strings that will be used to
      *                            filter searches.
      * @throws     Exception      Throws an informative exception if
      *                            strings_to_exclude is invalid.
      */
     public void setSearchStringsToExclude(String[] strings_to_exclude)
     throws Exception
     {
          // Check validity of strings_to_exclude
          if (strings_to_exclude != null)
               for (int i = 0; i < strings_to_exclude.length; i++)
                    if (strings_to_exclude[i] == null)
                         throw new Exception("Null search exclusion string specified.");
          
          // Set strings_to_exclude
          this.strings_to_exclude = strings_to_exclude;
     }
     
     
     /**
      * Set a specific network site that should be exclusively searched in all
      * search queries performed by this object. A value of null means that the
      * entire available network should be searched. This is the default set by
      * the constructor.
      *
      * @param specific_site  The network site to search.
      */
     public void setSpecificSiteToSearch(String specific_site)
     {
          this.specific_site = specific_site;
     }
     
     
     /**
      * Set a specific language that hits must be in in order to be included in
      * search results. The language must be one of the terms in the
      * included_languages field. A value of "No Limitations" means that any
      * language is permissible. A value of null is not permitted. The default
      * value set by the constructor is "No Limitations".
      *
      * @param      language       The language that results must be in, or "No
      *                            Limitations".
      * @throws     Exception      Throws an informative exception if the
      *                            language is invalid.
      */
     public void setLanguageResultsMustBeIn(String language)
     throws Exception
     {
          // Verify validity of language
          if (language == null) throw new Exception("Null language string provided.");
          boolean found = false;
          for (int i = 0; i < included_languages.length; i++)
               if (included_languages[i].equals(language))
                    found = true;
          if (!found) throw new Exception("Language of " + language + " specified.\nLimiting searches to this language is not permitted.");
          
          // Set language
          limit_to_language = language;
     }
     
     
     /**
      * Set a specific country that sites must be in in order to be included in
      * search results. The country must be one of the terms in the
      * included_countries field. A value of "No Limitations" means that sites
      * in any country are permissible. A value of null is not permitted. The
      * default value set by the constructor is "No Limitations".
      *
      * @param      country        The cournty that results must be in, or "No
      *                            Limitations".
      * @throws     Exception      Throws an informative exception if the
      *                            country is invalid.
      */
     public void setCountryResultsMustBeIn(String country)
     throws Exception
     {
          // Verify validity of country
          if (country == null) throw new Exception("Null country string provided.");
          boolean found = false;
          for (int i = 0; i < included_countries.length; i++)
               if (included_countries[i].equals(country))
                    found = true;
          if (!found) throw new Exception("Country of " + country + " specified.\nLimiting searches to this country is not permitted.");
          
          // Set country
          limit_to_country = country;
     }
     
     
     /**
      * Set the name of a country where the search will be performed (i.e. where
      * the search service is located). Results are not limited to this country.
      * The country must be one of the terms in the included_countries field. An
      * entry of "No Limitations" causes the default service location to be
      * used. A value of null is not permitted. The default value set by the
      * constructor is "No Limitations".
      *
      * @param      country        The cournty where the search will be
      *                            performed, or "No Limitations".
      * @throws     Exception      Throws an informative exception if the
      *                            country is invalid.
      */
     public void setRegionToSearchFrom(String country)
     throws Exception
     {
          // Verify validity of country
          if (country == null) throw new Exception("Null country string provided.");
          boolean found = false;
          for (int i = 0; i < included_countries.length; i++)
               if (included_countries[i].equals(country))
                    found = true;
          if (!found) throw new Exception("Country of " + country + " specified.\nPerforming searches in this country is not permitted.");
          
          // Set region
          region_to_search_from = country;
     }
     
     
     /**
      * Sets the file extension a document must have in order to be returned as
      * a hit in search results. The file type must be one of the terms in the
      * included_file_types field. An entry of "No Limitations" means that file
      * type will not by used to filter results. A value of null is not
      * permitted. The default value set by the constructor is "No Limitations".
      *
      * @param      file_type      The file type where the search will be
      *                            performed, or "No Limitations".
      * @throws     Exception      Throws an informative exception if the
      *                            file_type is invalid.
      */
     public void setFileTypeResultsMustBelongTo(String file_type)
     throws Exception
     {
          // Verify validity of file type
          if (file_type == null) throw new Exception("Null file type string provided.");
          boolean found = false;
          for (int i = 0; i < included_file_types.length; i++)
               if (included_file_types[i].equals(file_type))
                    found = true;
          if (!found) throw new Exception("File type of " + file_type + " specified.\nLimiting searches to this file type is not permitted.");
          
          // Set file type
          limit_to_file_type = file_type;
     }
     
     
     /**
      * Sets whether to suppress similar hits when reporting results. Similar
      * in this context means either:
      *
      * <ul><li>Sites with identical titles and/or descriptions.</li>
      * <li>Multiple hits from the same host.</li></ul>
      *
      * <p>The default value set by the constructor is false.
      *
      * @param suppress_similar_hits    Whether to suppress similar hits.
      */
     public void setSuppressSimilarHits(boolean suppress_similar_hits)
     {
          this.suppress_similar_hits = suppress_similar_hits;
     }
     
     
     /**
      * Sets whether to suppress hits that are classified as containing adult
      * content by the search service in question. The default value set by the
      * constructor is true.
      *
      * @param suppress_adult_content   Whether to suppress adult content.
      */
     public void setSuppressAdultContent(boolean suppress_adult_content)
     {
          this.suppress_adult_content = suppress_adult_content;
     }
     
     
     /**
      * Returns the top results for a query containing the given search string.
      * The search is subject to the NetworkSearch superclass' field settings.
      *
      * @param search_string  The string to base the query on. The query is
      *                       subject to the conditions of the NetworkSearch
      *                       superclass' field settings. The argument passed
      *                       to this parameter should not contain any special
      *                       formatting.
      * @param start_index    The index of the first hit to be returned. A value
      *                       of 1 refers to the highest ranked hit (there is no
      *                       index 0). If this index exceeds the available
      *                       number of hits then no hits are returned.
      * @param max_results    The maximum number of results to return. This
      *                       imposes an upper maximum on the size of the
      *                       returned array.
      * @param number_hits    A dummy array of size 1 that is filled with the
      *                       number of hits for the specified query by this
      *                       method. Ignored if null.
      * @param query_used     A dummy array of size 1 that is filled with the
      *                       actual search query constructed and used by this
      *                       method. Useful for debugging. Ignored if null.
      * @return               A matrix containing the search results. The first
      *                       dimension of the matrix corresponds to each
      *                       hit returned by the search, in the same order
      *                       as they are returned/ranked. The second dimension
      *                       specifes different types of information about each
      *                       corresponding hit, as follows:<ul>
      *                       <li>Entry 0: The document title.</li>
      *                       <li>Entry 1: The document URL.</li>
      *                       <li>Entry 2: The document summary.</li></ul>
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs.
      */
     public String[][] getSearchResults( String search_string,
          int start_index,
          int max_results,
          String[] number_hits,
          String[] query_used )
          throws Exception
     {
          String[] search_array = {search_string};
          return getSearchResults(search_array, start_index, max_results, number_hits, query_used);
     }
     
     
     /**
      * Returns the number of hits for a query containing the given search
      * strings. The search is subject to the NetworkSearch superclass' field
      * settings.
      *
      * @param search_string  The string to base the query on. The query is
      *                       subject to the conditions of the NetworkSearch
      *                       superclass' field settings. The argument passed
      *                       to this parameter should not contain any special
      *                       formatting.
      * @param query_used     A dummy array of size 1 that is filled with the
      *                       search actual query constructed and used by this
      *                       method. Useful for debugging. Ignored if null.
      * @return The number of hits for the specified search
      *                       string and corresponding field settings of the
      *                       NetworkSearch superclass.
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs.
      */
     public long getNumberHits(String search_string, String[] query_used)
     throws Exception
     {
          String[] search_array = {search_string};
          return getNumberHits(search_array, query_used);
     }
     
     
     /**
      * Takes the results produced by a NetworkSearch subclass search and
      * formats them into an HTML page, which is returned. This returned page
      * shows the total number of hits, details (name, URL and description) on a
      * selected number of these results, the query used and some important
      * qualifications and limitations of the specific web service used.
      *
      * @param search_results A matrix containing the search results. The first
      *                       dimension of the matrix corresponds to each
      *                       hit returned by the search, in the same order
      *                       as they are returned/ranked. The second dimension
      *                       specifes different types of information about each
      *                       corresponding hit, as follows:<ul>
      *                       <li>Entry 0: The document title.</li>
      *                       <li>Entry 1: The document URL.</li>
      *                       <li>Entry 2: The document summary.</li></ul>
      * @param start_rank     The numerical ranking corresponding to the first
      *                       hit.
      * @param total_hits     The approximate total number of hits found by
      *                       the search service.
      * @param query_used     The query used by the search service. Does not
      *                       include directly aspects of the query that were
      *                       directly parametrized into the search service's
      *                       web services.
      * @param service_name   The name of the web service used to perform the
      *                       search.
      * @return               The HTML-formatted search results.
      */
     public String getHTMLFormattedSearchResults(String[][] search_results,
          int start_rank, String total_hits, String query_used, String service_name)
     {
          String formatted_results = "<HTML>\n<HEAD>\n\t<TITLE>Search Results</TITLE>\n</HEAD>\n<BODY>\n";
          
          if (search_results.length == 0)
               formatted_results += "<p><b>No hits</b> found with the " + service_name + " web services.</p>\n";
          else
          {
               String total = mckay.utilities.staticlibraries.StringMethods.getNumberFormattedWithCommas(Integer.parseInt(total_hits));
               formatted_results += "<i>Results " + start_rank + " to " + (start_rank + search_results.length - 1) + " of about </i><b>" + total + "</b><i> hits found with the " + service_name + " web services.\n";
               formatted_results += "Note that the total hits reported by some web services are not filtered by certain query filters even when the actual results are.</i>\n<hr>\n";
               
               for (int i = 0; i < search_results.length; i++)
               {
                    formatted_results += "\n<p><b>RESULT " + (i + start_rank) + ":</b> " + search_results[i][0] + "<br>\n";
                    formatted_results += "<a href=\"" + search_results[i][1] + "\">" + search_results[i][1] + "</a><br>\n";
                    formatted_results += search_results[i][2] + "</p>\n";
               }
          }
          
          formatted_results += "<br><hr><br>\n<i>Search query used: </i><b>" + query_used + "</b><i>.<br>\n";
          formatted_results += "It is important to note that this query may not contain all of the query parameters used, however. This is because many web services impose maxima on query lengths, so it can be more effective to directly incorporate query parameters into web service objects when possible.</i>\n";
          formatted_results += "<p><hr><br>\n<i>" + getSearchServiceLimitations() + "</i>";
          
          formatted_results += "\n</BODY>\n</HTML>";
          
          return formatted_results;
     }
     
     
     /* ABSTRACT METHODS ******************************************************/
     
     
     /**
      * Returns the name of the web services used by the implementing class.
      *
      * @return     The implementing web services called.
      */
     public abstract String getSeachServiceName();
     
     
     /**
      * Returns the specific limitations of this web service in the context
      * of all of the search parameters available to the NetworkSearch class and
      * its subclasses.
      *
      * @return     Limitations of the specific web service. This is formatted
      *             as an unnumbered HTML list.
      */
     public abstract String getSearchServiceLimitations();
     
     
     /**
      * Returns the top results for a query containing the given search strings,
      * where the search is a boolean AND of the strings in the entries of the
      * search_strings parameter. The search is subject to the NetworkSearch
      * superclass' field settings.
      *
      * @param search_strings The strings to base the query on. The query
      *                       consists of a boolean AND or OR of all entries of
      *                       this array in addition to the conditions of the
      *                       NetworkSearch superclass' field settings. The
      *                       arguments passed to this parameter should not
      *                       contain any special formatting.
      * @param start_index    The index of the first hit to be returned. A value
      *                       of 1 refers to the highest ranked hit (there is no
      *                       index 0). If this index exceeds the available
      *                       number of hits then no hits are returned.
      * @param max_results    The maximum number of results to return. This
      *                       imposes an upper maximum on the size of the
      *                       returned array.
      * @param number_hits    A dummy array of size 1 that is filled with the
      *                       number of hits for the specified query by this
      *                       method. Ignored if null.
      * @param query_used     A dummy array of size 1 that is filled with the
      *                       search actual query constructed and used by this
      *                       method. Useful for debugging. Ignored if null.
      * @return               A matrix containing the search results. The first
      *                       dimension of the matrix corresponds to each
      *                       hit returned by the search, in the same order
      *                       as they are returned/ranked. The second dimension
      *                       specifes different types of information about each
      *                       corresponding hit, as follows:<ul>
      *                       <li>Entry 0: The document title.</li>
      *                       <li>Entry 1: The document URL.</li>
      *                       <li>Entry 2: The document summary.</li></ul>
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs.
      */
     public abstract String[][] getSearchResults( String[] search_strings,
          int start_index,
          int max_results,
          String[] number_hits,
          String[] query_used )
          throws Exception;
     
     
     /**
      * Returns the number of hits for a query containing the given search
      * strings, where the search is a boolean AND of the strings in the entries
      * of the search_strings parameter. The search is subject to the
      * NetworkSearch superclass' field settings.
      *
      * @param search_strings The strings to base the query on. The query
      *                       consists of a boolean AND or OR of all entries of
      *                       this array in addition to the conditions of the
      *                       NetworkSearch superclass' field settings. The
      *                       arguments passed to this parameter should not
      *                       contain any special formatting.
      * @param query_used     A dummy array of size 1 that is filled with the
      *                       search actual query constructed and used by this
      *                       method. Useful for debugging. Ignored if null.
      * @return               The number of hits for the specified search
      *                       strings and corresponding field settings of the
      *                       NetworkSearch superclass.
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs.
      */
     public abstract long getNumberHits( String[] search_strings,
          String[] query_used )
          throws Exception;
     
     
     /**
      * Returns the number of hits for a query containing the given search
      * strings, where the search is a boolean AND of the strings in the entries
      * of the search_strings parameter. The search is subject to the
      * NetworkSearch superclass' field settings.
      *
      * This query is submitted up to allowed_attempts times if it unsuccesful
      * (i.e. if the web service request throws an Exception). If the number
      * of unsuccesful attempts allowed exceeds allowed_attempts then the
      * user is presented with a dialog box providing the opportunity to either
      * continue trying or end processing. Processing is paused until the user
      * makes a selection. If the user choooses to cancel, then the original
      * Exception generated by the request is thrown. If the user chooses to
      * continue then another allowed_attempts attempts are made.
      *
      * @param search_strings      The strings to base the query on. The query
      *                            consists of a boolean AND or OR of all
      *                            entries of this array in addition to the
      *                            conditions of the NetworkSearch superclass'
      *                            field settings. The arguments passed to this
      *                            parameter should not contain any special
      *                            formatting.
      * @param query_used          A dummy array of size 1 that is filled with
      *                            the search actual query constructed and used
      *                            by this method. Useful for debugging. Ignored
      *                            if null.
      * @param allowed_attempts    The number of unsuccesful attempts allowed
      *                            before the user is notived.
      * @return                    The number of hits for the specified search
      *                            strings and corresponding field settings of
      *                            the NetworkSearch superclass.
      * @throws Exception          Throws an informative exception if a problem
      *                            occurs.
      */
     public long getNumberHits( String[] search_strings,
          String[] query_used, int allowed_attempts )
          throws Exception
     {
          int try_number = 1;
          int overall_tries = 1;
          while (try_number <= allowed_attempts)
          {
               try
               {return getNumberHits(search_strings, query_used);}
               catch (Exception e)
               {
                    if (try_number >= allowed_attempts)
                    {
                         // Find the date
                         java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
                         String date = date_format.format(new java.util.Date());
                         
                         // Prepare error message
                         String times_submitted_message = "Query submitted unsuccessfully " + overall_tries + " times.\n\n";
                         String date_message = "Last attempt at " + date + ".\n\n";
                         String request_message = "Do you wish to continue submitting this query?\n\n";
                         String overall_message = e.getMessage() + times_submitted_message + date_message + request_message;
                         
                         // Display request dialog
                         int choice = JOptionPane.showConfirmDialog(null, overall_message, "ERROR", JOptionPane.OK_CANCEL_OPTION);
                         if (choice == JOptionPane.CANCEL_OPTION) throw e;
                         else
                         {
                              overall_tries++;
                              try_number = 1;
                         }
                    }
                    
                    else
                    {
                         try_number++;
                         overall_tries++;
                    }
               }
          }
          throw new Exception("Query unsuccesful."); // Note that this line will never be executed.
     }
     
     
     /**
      * Returns a query formatted based on the settings of this superclass'
      * fields and the formatting conventions of the particular search service
      * used by the implementing subclass' particular search service.
      *
      * <p>This method should be called internally be each search method
      * implemented by the subclass before searches are actually performed.
      *
      * <p>When possible, configurations are implemented by the prepareSearcher
      * method instead in order to restrict query length, which is limited by
      * some web services.
      *
      * @param search_strings Search phrases to include in the search. The
      *                       search will combine these terms using a boolean
      *                       AND or OR. The arguments passed to this parameter
      *                       should not contain any special formatting.
      * @return               A formatted copy of the query. Note that some
      *                       configurations are performed by the
      *                       prepareSearcher method instead, and will not be
      *                       reflected here.
      * @throws Exception     Throws an informative exception if search_terms
      *                       is null or one of its entries is null.
      */
     protected abstract String formatSearchString(String[] search_strings) throws Exception;
     
     
     /**
      * Returns an object used to perform searches and/or configures an existing
      * search Object, based on the particular web services system in question.
      * The configuration is performed based on the search settings stored in
      * the fields of the superclass NetworkSearch object. The modified searcher
      * object is returned.
      *
      * <p>This method should be called internally be each search method
      * implemented by the subclass before searches are actually performed.
      *
      * <p>Some search settings will sometimes be incorporated directly into the
      * query string by the formatSearchString method instead of here, based on
      * the nature of the web services system in question. However, when
      * possible, configurations are implemented here in order to restrict query
      * length, which is limited by some web services.
      *
      * @param      searcher  The Object to perform the search with, as
      *                       defined by the particular web services system
      *                       in question. May be null if it is created by
      *                       this method.
      * @return               The configured searcher object.
      * @throws     Exception Throws an informative exception if a problem
      *                       occurs
      */
     protected abstract Object prepareSearcher(Object searcher) throws Exception;
     
     
     /**
      * Takes in an exception and then throws a new Exception that identifies
      * the problem that occured in a way that is standardized accross web
      * services.
      *
      * <p>This method can alternatively be calleded with null passed to the
      * exception parameter in order to check the acceptability of the
      * configurations stored in the fields of the NetworkSearch object that
      * will be used to perform searches.
      *
      * @param      exception      An exception to be formatted. May be null if
      *                            this method is being used to check field and
      *                            max_results compatibility.
      * @param      query          The query that generated the exception. May
      *                            be "" if not applicable
      * @param      max_results    The maximum number of hits that searches
      *                            are configured to return.
      * @throws     Exception      An exception indicating the problem that
      *                            occured in a descriptive way that is
      *                            standardized accross web services that
      *                            implement this method. The message stored in
      *                            this new exception must identify the search
      *                            service that generated the Exception and it
      *                            must be suitable for display in an error
      *                            dialog box.
      */
     protected abstract void formatErrorMessage(Exception exception, String query,
          int max_results) throws Exception;
}
