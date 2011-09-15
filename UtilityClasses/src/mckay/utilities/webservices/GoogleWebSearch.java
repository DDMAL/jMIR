/*
 * GoogleWebSearch.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.webservices;

import java.net.InetAddress;
import com.google.soap.search.*;


/**
 * Allows access to a variety of types of searches using the Google SOAP web
 * services.
 *
 * <p>Limitations of these web services are specified by the
 * getSearchServiceLimitations method.
 *
 * <p>Note that these sevices how now been deprecated by Google.
 *
 * @author Cory McKay
 */
public class GoogleWebSearch
     extends NetworkSearch
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * An object storing the Google license key that is used to perform
      * searches using the Google SOAP web services.
      */
     private   GoogleSearch   search_client;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of GoogleWebSearch, stores the Google licesne key
      * and sets the superclass' fields to their default values.
      *
      * @param google_license_key  The Google license key that is to be used
      *                            to access the Google SOAP web services.
      */
     public GoogleWebSearch(String google_license_key)
     {
          super();
          search_client = new GoogleSearch();
          search_client.setKey(google_license_key);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Returns the name of the web services used by this class.
      *
      * @return     The implementing web services called.
      */
     public String getSeachServiceName()
     {
          return "Google SOAP";
     }
     
     
     /**
      * Returns the specific limitations of this web service in the context
      * of all of the search parameters available to the NetworkSearch class and
      * its subclasses.
      *
      * @return     Limitations of the specific web service. This is formatted
      *             as an unnumbered HTML list.
      */
     public String getSearchServiceLimitations()
     {
          String limitations = "\nThe " + getSeachServiceName() + " web services have the following limitations in the context of all of the available search parameters.\n" +
               "<ul>\n" +
               "<li>There is no functionality for searching for similar but non-matching strings.\n" +
               "<li>There is no functionality for performing searches using a service located in a specific country.\n" +
               "<li>Searches can only be limited to one specific network site, not to multiple specified network sites. General network searches are permitted.\n" +
               "<li>There is a maximum of 10 words per query.\n" +
               "<li>Only up to 10 results may be returned per query.\n" +
               "<li>Only results up to the 1000th rank may be returned.\n" +
               "<li>Only up to 1000 queries may be performed per day. This quota refers to queries from all IP addresses combined.\n" +
               "</ul>\n";
          return limitations;
     }
     
     
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
     public String[][] getSearchResults( String[] search_strings,
          int start_index,
          int max_results,
          String[] number_hits,
          String[] query_used )
          throws Exception
     {
          // Initialize the results to return
          String[][] formatted_search_results = null;
          
          // Prepare the query to use
          String query = formatSearchString(search_strings);
          
          // Validate the compatibility of the search parameters with this web
          // service
          formatErrorMessage(null, query, max_results);
          
          // Perform the search and format the results
          try
          {
               // Configure the search_client
               search_client = (GoogleSearch) prepareSearcher(search_client);
               
               // Set the first result ranking and the maximum number of results
               search_client.setStartResult(start_index - 1);
               search_client.setMaxResults(max_results);
               
               // Perform the search
               search_client.setQueryString(query);
               GoogleSearchResult query_results = search_client.doSearch();
               GoogleSearchResultElement[] query_results_array = query_results.getResultElements();
               
               // Prepare the formatted results
               formatted_search_results = new String[query_results_array.length][3];
               for (int i = 0; i < formatted_search_results.length; i++)
               {
                    formatted_search_results[i][0] = query_results_array[i].getTitle();
                    formatted_search_results[i][1] = query_results_array[i].getURL();
                    formatted_search_results[i][2] = query_results_array[i].getSnippet();
               }
               
               // Remove HTML formatting from the title and summary
               for (int i = 0; i < formatted_search_results.length; i++)
               {
                    formatted_search_results[i][0] = formatted_search_results[i][0].replaceAll("<.*?>", "");;
                    formatted_search_results[i][2] = formatted_search_results[i][2].replaceAll("<.*?>", "");;
               }
               
               // Store the number of hits
               if (number_hits != null)
                    number_hits[0] = new Integer(query_results.getEstimatedTotalResultsCount()).toString();
               
               // Store the query used
               if (query_used != null)
                    query_used[0] = query;
          }
          
          // Interpret and rethrow exceptions
          catch (Exception e)
          {formatErrorMessage(e, query, max_results);}
          
          // Return results
          return formatted_search_results;
     }
     
     
     /**
      * Returns the number of hits for a query containing the given search
      * strings, where the search is a boolean AND of the strings in the entries
      * of the search_strings parameter. The search is subject to the NetworkSearch
      * superclass' field settings.
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
      * @return The number of hits for the specified search
      *                       strings and corresponding field settings of the
      *                       NetworkSearch superclass.
      * @throws Exception     Throws an informative exception if a problem
      *                       occurs.
      */
     public long getNumberHits(String[] search_strings, String[] query_used)
     throws Exception
     {
          // Prepare the query to use
          String query = formatSearchString(search_strings);
          
          // Validate the compatibility of the search parameters with this web
          // service
          formatErrorMessage(null, query, 0);
          
          // Perform the search and format the results
          try
          {
               // Configure the search_client
               search_client = (GoogleSearch) prepareSearcher(search_client);
               
               // Perform the search
               search_client.setQueryString(query);
               GoogleSearchResult results = search_client.doSearch();
               
               // Format the number of hits
               long number_hits = (long) results.getEstimatedTotalResultsCount();
               
               // Store the query used
               if (query_used != null)
                    query_used[0] = query;
               
               // Return results
               return number_hits;
          }
          
          // Interpret and rethrow exceptions
          catch (Exception e)
          {
               formatErrorMessage(e, query, 0);
               return 0; // dummy return that will never be executed
          }
     }
     
     
     /* PROTECTED METHODS *****************************************************/
     
     
     /**
      * Returns a query formatted based on the settings of the NetworkSearch
      * superclass' fields and the Google search formatting conventions.
      *
      * <p>This method should be called internally before each search is
      * performed by objects of this class.
      *
      * <p>When possible, configurations are implemented by the prepareSearcher
      * method instead in order to restrict query length, which is limited..
      *
      * @param search_strings Search phrases to include in the search. The
      *                       search will combine these terms using a boolean
      *                       AND or OR. The arguments passed to this parameter
      *                       should not contain any special formatting.
      * @return               A Google-formatted copy of the query. Note that
      *                       some configurations are performed by the
      *                       prepareSearcher method instead, and will not be
      *                       reflected here.
      * @throws Exception     Throws an informative exception if search_terms
      *                       is null or one of its entries is null.
      */
     protected String formatSearchString(String[] search_strings)
     throws Exception
     {
          // Verify the validity of search_terms
          if (search_strings == null)
               throw new Exception("No search strings specified in query."); // Do not change this error message as some external classes look for it specifically
          for (int i = 0; i < search_strings.length; i++)
               if (search_strings[i] == null)
                    throw new Exception("One or more search terms is null.");
          
          // Initialize the formatted query
          String formatted_query = "";
          
          // Combine the queries and meke them literal if appropriate and
          // specify whether is an AND or OR search
          for (int i = 0; i < search_strings.length; i++)
          {
               if (i != 0)
               {
                    if (or_based_search) formatted_query += " OR ";
                    else formatted_query += " ";
               }
               
               if (literal_search)
                    if (!search_strings[i].startsWith("\"") && !search_strings[i].endsWith("\""))
                         formatted_query += "\"" + search_strings[i] + "\"";
                    else
                    {
                    if (or_based_search)
                    {
                         String[] words = mckay.utilities.staticlibraries.StringMethods.breakIntoTokens(search_strings[i], " ");
                         String to_add = words[0];
                         if (words.length > 1)
                              for (int j = 1; j < words.length; j++)
                                   to_add += " OR " + words[j];
                         formatted_query +=  to_add;
                    }
                    else formatted_query += search_strings[i];
                    }
          }
          
          // Add strings to exclude if appropriate
          if (strings_to_exclude != null)
               for (int i = 0; i < strings_to_exclude.length; i++)
                    formatted_query += " -\"" + strings_to_exclude[i] + "\"";
          
          // Add specific URL if appropriate
          if (specific_site != null)
               formatted_query += " site:" + specific_site;
          
          // Limit the search to a particulr file type if appropriate
          if (!limit_to_file_type.equals("No Limitations"))
               formatted_query += " filetype:" + limit_to_file_type;
          
          // Return the result
          return formatted_query;
     }
     
     
     /**
      * Configures and returns the search_client GoogleSearch object based on
      * settings stored in this GoogleWebSearch superclass NetworkSearch
      * object's fields
      *
      * <p>Some search settings are incorporated directly into the query string
      * by the formatSearchString method instead of here. However, when
      * possible, configurations are implemented here in order to restrict query
      * length, which is limited.
      *
      * @param      searcher  The GoogleSearch object that will perform the
      *                       search.
      * @return               The configured GoogleSearch object.
      */
     protected Object prepareSearcher(Object searcher)
     {
          // Cast the searcher
          GoogleSearch this_searcher = (GoogleSearch) searcher;
          
          // Limit the search to a particular language if appropriate
          String language_code = getLanguageCode(limit_to_language);
          if (language_code != null)
               this_searcher.setLanguageRestricts(language_code);
          
          // Limit the search to a particular country if appropriate
          String country_code = getCountryCode(limit_to_country);
          if (country_code != null)
               this_searcher.setRestrict(country_code);
          
          // Suppress similar hits if appropriate
          this_searcher.setFilter(suppress_similar_hits);
          
          // Suppress addult content if appropriate
          this_searcher.setSafeSearch(suppress_adult_content);
          
          // Return the results
          return this_searcher;
     }
     
     
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
      *                            are configured to return. May be 0 if not
      *                            applicable.
      * @throws     Exception      An exception indicating the problem that
      *                            occured in a descriptive way that is
      *                            standardized accross web services that
      *                            implement this method. The message stored in
      *                            this new exception must identify the search
      *                            service that generated the Exception and it
      *                            must be suitable for display in an error
      *                            dialog box.
      */
     protected void formatErrorMessage(Exception exception, String query, int max_results)
     throws Exception
     {
          // Case where there is no exception yet and this method is being used
          // to validate compatibility query parameters with Google
          if (exception == null)
          {
               // Detect attempt to use a query demanding over the permitted
               // 10 results
               if (max_results > 10)
                    throw new Exception("Service error when using " + getSeachServiceName() + ".\n\n" +
                         "Requested a search with " + max_results + " results.\n" +
                         "Google only allows up to 10.\n\n");
          }
          
          // Case where there is an existing exception that needs to be
          // formatted
          else if (exception != null)
          {
               // Detect quota exceeded errors. Note also that the error message
               // thrown here should not be changed, as some external classes
               // look for it specifically.
               if (exception.toString().indexOf("quota") != -1)
               {
                    throw new Exception("Service error when attempting to use " + getSeachServiceName() + ".\n\n" +
                         "Daily search quota exceeded.\n" +
                         "Google only allows 1000 queries per day per authorization key (irregardless of IP address).\n" +
                         "You must wait for your quota to be cleared before you can perform more searches.\n\n");
               }
               
               // Detect invalid authorization key
               if (exception.toString().indexOf("Invalid authorization key") != -1)
               {
                    throw new Exception("Connection error when attempting to use " + getSeachServiceName() + ".\n\n" +
                         "Invalid Google license key used.\n" +
                         "Please correct this in the options.\n\n");
               }
               
               // Detect time out. Note that the error message thrown here
               // should not be changed, as some external classes look for it
               // specifically.
               if (exception.toString().indexOf("Connection timed out") != -1)
               {
                    throw new Exception("Service error when attempting to use " + getSeachServiceName() + ".\n\n" +
                         "Timed out while trying to contact Google.\n" +
                         "Service may be temporarily overloaded.\n" +
                         "Please retry your search immediately.\n\n");
               }
               
               // Attempt to access www.google.com to see if internet access is
               // available. Note that the error message thrown here should not
               // be changed, as some external classes look for it specifically.
               try
               {InetAddress address = InetAddress.getByName("www.google.com");}
               catch (Exception f)
               {throw new Exception("Unable to access the internet.\nPlease ensure that your computer is connected to the internet or other appropriate network.\n");}
               
               // Miscellaneous errors
               if (exception instanceof GoogleSearchFault)
               {
                    throw new Exception("Search error when using " + getSeachServiceName() + ".\n\n" +
                         exception.toString() + "\n\n" +
                         "Query used: " + query + "\n\n");
               }
               else
               {
                    throw new Exception("Search error when using " + getSeachServiceName() + ".\n\n" +
                         exception.toString() + "\n\n" +
                         "Query used: " + query + "\n\n");
               }
          }
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Converts a chosen one of the languages from the NetworkSearch
      * superclasse's included_languages field to the appropriate code used by
      * Google. A language of "No Limitations" results in null being returned,
      * as does an unknown language name.
      *
      * @param language  The language from included_languages to find the code
      *                  for.
      * @return          The language code or null.
      */
     private String getLanguageCode(String language)
     {
          String return_value = null;
          
          if (language.equals("English"))
               return_value = "lang_en";
          else if (language.equals("French"))
               return_value = "lang_fr";
          else if (language.equals("Spanish"))
               return_value = "lang_es";
          else if (language.equals("Portuguese"))
               return_value = "lang_pt";
          else if (language.equals("German"))
               return_value = "lang_de";
          else if (language.equals("Chinese"))
               return_value = "lang_zh-CN";
          else if (language.equals("Japanese"))
               return_value = "lang_ja";
          else if (language.equals("Turkish"))
               return_value = "lang_tr";
          else if (language.equals("Arabic"))
               return_value = "lang_ar";
          
          return return_value;
     }
     
     
     /**
      * Converts a chosen one of the countries from the NetworkSearch
      * superclasse's included_countries field to the appropriate code used by
      * Google. A country of "No Limitations" results in null being returned,
      * as does an unknown country name.
      *
      * @param country   The country from included_countries to find the code
      *                  for.
      * @return          The country code or null.
      */
     private String getCountryCode(String country)
     {
          String return_value = null;
          
          if (country.equals("Canada"))
               return_value = "countryCA";
          else if (country.equals("U.S.A."))
               return_value = "countryUS";
          else if (country.equals("U.K."))
               return_value = "countryUK";
          else if (country.equals("France"))
               return_value = "countryFR";
          else if (country.equals("Spain"))
               return_value = "countryES";
          else if (country.equals("Germany"))
               return_value = "countryDE";
          else if (country.equals("Austria"))
               return_value = "countryAT ";
          else if (country.equals("Brazil"))
               return_value = "countryBR ";
          else if (country.equals("Japan"))
               return_value = "countryJP";
          else if (country.equals("China"))
               return_value = "countryCN";
          else if (country.equals("Turkey"))
               return_value = "countryTR";
          
          return return_value;
     }
}