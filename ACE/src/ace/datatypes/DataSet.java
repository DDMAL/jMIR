/*
 * DataSet.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import java.io.*;
import java.util.LinkedList;
import ace.xmlparsers.XMLDocumentParser;
import weka.core.Instance;


/**
 * Objects of this class each hold feature values for an item to be classified.
 * Methods are included for displaying these values as formatted strings,
 * saving them to disk or loading them from disk. A method is also available
 * for reconciling these objects with FeatureDefinition objects. Methods are
 * also available for extracting feature values in String form.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class DataSet
     implements Serializable
{
     /* FIELDS ****************************************************************/


     /**
      * The name of the data set. This name should be unique among each group of
      * data sets. Should be null for non-top-level DataSets.
      */
     public	String				identifier;


     /**
      * Sub-sets of this DataSet. Each such sub-set can serve as an instance
      * that is individually classifiable. For example, sub-sets could consist
      * of windows of audio extracted from the recording that makes the overall
      * DataSet. The sub_sets field should be null if there are no sub-sets that
      * can be individually classified.
      */
     public	DataSet[]			sub_sets;


     /**
      * Identifies the start of a sub-set of a DataSet. Set to NaN if this
      * object is a top-level DataSet.
      */
     public	double				start;


     /**
      * Identifies the end of a sub-set of a DataSet. Set to NaN if this
      * object is a top-level DataSet.
      */
     public	double				stop;


     /**
      * The feature values for this DataSet as a whole. If there are any
      * sub-sets, they will store there own feature values, and these will not
      * be referenced here. The first indice identifies the feature and the
      * second indice identifies the dimension of the feature. It is clear that
      * features of arbitrary dimensions may be accomodated. Features whose
      * value or values are missing are assigned a value of null. This field is
      * assigned a value of null if no features have been extracted. It is
      * assumed that the Java Class calling the DataSet knows the ordering and
      * identity of the features of the DataSet and its sub-sets. The
      * feature_values may be ordered based on FeatureDefinitions using the
      * orderAndCompactFeatures method. Individual features may also be assigned
      * null values if they are unknown or inappropriate.
      */
     public	double[][]			feature_values;


     /**
      * The names of the features in each corresponding (by first indice) entry
      * of feature_values. These are often only stored here temporarily until
      * they can be accessed and stored externally in a more efficient fashion.
      * This field is therefore often null, even when the feature_values field
      * is not.
      */
     public	String[]			feature_names;


     /**
      * If this object is a sub-set of another DataSet, this field points to that
      * parent dataset. Otherwise this field is null.
      */
     public	DataSet				parent;


     /**
      * An identifier for use in serialization.
      */
     private    static final long               serialVersionUID = 3L;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Generate an empty DataSet.
      */
     public DataSet()
     {
          identifier = null;
          sub_sets = null;
          start = Double.NaN;
          stop = Double.NaN;
          feature_values = null;
          feature_names = null;
          parent = null;
     }

     /**
      * Explicitly creates a DataSet
      *
      * @param identifier       The name of the data set.
      * @param sub_sets         Sub-sets of this DataSet.
      * @param start            Identifies the beginning of a sub-set of a DataSet.
      * @param stop             Identifies the end of a sub-set of a DataSet.
      * @param feature_values   The feature values for this DataSet as a whole.
      * @param feature_names    The names of the features in each corresponding
      *                         (by first indice) entry of feature_values.
      * @param parent           The parent dataset, or null if not a subsection.
      */
     public DataSet(String identifier, DataSet[] sub_sets, Double start, Double stop,
             double[][] feature_values, String[] feature_names, DataSet parent)
     {
         this.identifier = identifier;
         this.sub_sets = null;
         this.start = start.doubleValue();
         this.stop = stop.doubleValue();
         this.feature_values = feature_values;
         this.feature_names = feature_names;
         this.parent = parent;
     }

     /**
      * Generates a DataSet from a Weka ARFF file.
      * @param instance     The Weka Instance from which to get the feature values
      *                     and feature names of this DataSet.
      * @param inst         The index of this Instance in its parent Instances object.
      *                     This number precedes the identifier to ensure that all
      *                     identifiers are unique.
      */
     public DataSet (Instance instance, int inst)
     {
         // Identifier will contain the feature values of this instance except the class attribute
         String fvals = instance.toString();
         if(!instance.classIsMissing())
            fvals = fvals.substring(0, instance.toString().lastIndexOf(","));
         identifier = inst + "_" + fvals;
         sub_sets = null;
         start = Double.NaN;
         stop = Double.NaN;
         feature_values = new double[instance.numAttributes()-1][1];
         feature_names = new String[instance.numAttributes()];
         for(int i = 0; i < instance.numAttributes() -1; i++)
         {
             feature_values[i][0] = instance.value(i);
             feature_names[i] = instance.attribute(i).name();
         }
         parent = null;
     }

     /* PUBLIC METHODS ********************************************************/


     /**
      * EDIT August 7th 2009: This method is no longer used as ACE has been modified to
      * automatically generate feature definitions if feature values are present but
      * feature definitions are not.
      *
      * Processes this DataSet based on the given definitions parameter. The
      * feature values stored in the feature_values field are re-ordered based
      * on the correspondance between the feature_names field and the defintions
      * parameter. All features in feature_names that are not referred to in
      * definitions are deleted. All features referred to in definitions but
      * not present in feature_names are given a null entry in feature_values.
      * The feature_names field is set to null at the end of processing in order
      * to save memory.
      *
      * <p>This method also processes the sub_sets of this DataSet recursively.
      *
      * <p>The end result of running this method is that the features in
      * feature_values that are referred to in both feature_names and
      * definitions are given the same order as in definitions. Any features in
      * definitions that are not present in feature_names are set to null in
      * feature_values. Any features in feature_names that are not in
      * definitions are deleted. At the end of running this method,
      * feature_names is null and feature_values has the same number of entries
      * as definitions.
      *
      * <p>The purpose of running this method is to put this DataSet in a
      * configuration that can be stored and processed more efficiently and to
      * verify the validity of the stored features.
      *
      * @param	definitions   The feature definitions to order the
      *                       feature_values field by.
      * @param	is_top_level  True if this DataSet is a top-level DataSet
      *                       (i.e. not a sub-set of another DataSet).
      *                       This parameter should always be true when
      *                       this method is called externally.
      * @throws	Exception     An informative exception is thrown if the
      *                       dimensions of a stored feature does not match
      *                       the dimensions that it should have according to
      *                       its definition. An excpetion is also thrown if
      *                       features in the sub_sets that have values of
      *                       false for the is_sequential field of the
      *                       corresponding FeatureDefinition are present
      *                       in the sub-set.
      */
     public void orderAndCompactFeatures( FeatureDefinition[] definitions,
          boolean is_top_level )
          throws Exception
     {
          // Prepare new set of feature_values to store
          double[][] new_feature_values = new double[definitions.length][];

          // If no featue names or feature values are stored, then set
          // all features to null, as they cannot be identified
          if (feature_names == null || feature_values == null)
               for (int feat = 0; feat < new_feature_values.length; feat++)
                    new_feature_values[feat] = null;

          // Set up new_feature_values
          else
          {
               for (int def = 0; def < new_feature_values.length; def++)
               {
                    new_feature_values[def] = null;

                    for (int feat = 0; feat < feature_names.length; feat++)
                         if ( definitions[def].name.equals(feature_names[feat]) )
                         {
                         if (!is_top_level && !definitions[def].is_sequential)
                              throw new Exception( "Feature " + feature_names[feat] + " is present in a sub-set of\n" +
                                   "a DataSet, but is marked as non-sequential in its definition." );
                         if (feature_values[feat].length != definitions[def].dimensions)
                              throw new Exception( "Feature " + feature_names[feat] + " has " + feature_values[feat].length +
                                   " values, but should have " + definitions[def].dimensions + "\n" +
                                   "according to its definition." );

                         new_feature_values[def] = feature_values[feat];
                         feat = feature_values.length;
                         }
               }
          }

          // Update the fields of this object
          feature_names = null;
          feature_values = new_feature_values;

          /* Commented August 7th 2009 when ACE was changed to automatically generate
          feature definitions.*/
          // Apply to sub_sets
//          if (sub_sets != null)
//               for (int set = 0; set < sub_sets.length; set++)
//                    sub_sets[set].orderAndCompactFeatures(definitions, false);
     }


     /**
      * Returns the feature values stored in the feature_values field of this
      * object. The first indice of the returned array denotes the feature
      * and the second indice indicates the dimension of the feature (in order
      * to accomodate multi-dimensional features).
      *
      * <p>The returned array is null if no features have been extracted. If
      * a particular feature value is not available, then a question mark is
      * returned in the appropriate entry.
      *
      * @param	definitions	Feature definitions that are used to get the
      *				dimensions of unknown features.
      * @return			The array of feature values.
      */
     public String[][] getFeatureValuesOfTopLevel(FeatureDefinition[] definitions)
     {
          if (feature_values == null)
               return null;
          String[][] feature_values_str = new String[feature_values.length][];
          for (int i = 0; i < feature_values.length; i++)
          {
               if (feature_values[i] == null)
               {
                    feature_values_str[i] = new String[definitions[i].dimensions];
                    for (int j = 0; j < feature_values_str[i].length; j++)
                         feature_values_str[i][j] = "?";              }
               else
               {
                    feature_values_str[i] = new String[feature_values[i].length];
                    for (int j = 0; j < feature_values[i].length; j++)
                         feature_values_str[i][j] = String.valueOf(feature_values[i][j]);
               }
          }
          return feature_values_str;
     }


     /**
      * Returns the feature values stored in the DataSets in the sub_sets field
      * of this object. The first indice of the returned array denotes the
      * sub-section. The second indice indicates the feature and the third
      * indice indicates the dimension of the feature (in order to accomodate
      * multi-dimensional features).
      *
      * <p>The returned array is null if no sub-sections are available. The
      * first dimension is null if no features have been extracted for a given
      * sub-section. If a particular feature value is not available, then a
      * question mark is returned in the appropriate entry.
      *
      * @param	definitions   Feature definitions that are used to get the
      *                       dimensions of unknown features.
      * @return               The array of feature values.
      */
     public String[][][] getFeatureValuesOfSubSections(FeatureDefinition[] definitions)
     {
          if (sub_sets == null)
               return null;
          String[][][] feature_values_str = new String[sub_sets.length][][];
          for (int i = 0; i < sub_sets.length; i++)
               feature_values_str[i] = sub_sets[i].getFeatureValuesOfTopLevel(definitions);
          return feature_values_str;
     }


     /**
      * Generate a formatted strind detailing the contents of this DataSet.
      *
      * @param	depth    How deep this DataSet is in a hierarchy of DataSets
      *                  (i.e. through the sub_sets field). This parameter
      *                  should generally be 0 when called externally, as this
      *                  method	operates recursively.
      * @return          A formatted string describing this DataSet.
      */
     public String getDataSetDescription(int depth)
     {
          // Prepare the indent
          String indent = new String("");
          for (int i = 0; i < depth; i++)
               indent += "   ";

          // Prepare the name of the string identifying the dataset
          String id_string = new String("");
          if (identifier != null)
          {
               id_string = indent + "DATASET " + identifier + ": ";
               if (sub_sets != null)
                    id_string += sub_sets.length + " sub-sets ";
               else
                    id_string += "0 sub-sets ";
               if (feature_values != null)
                    id_string += feature_values.length + " features\n\n";
               else
                    id_string += "0 features\n\n";
          }
          else
               id_string = indent + "SUB-SET -> Start: " + start + "   Stop: " + stop + "\n";

          // Add features of this dataset
          String feature_string = new String("");
          if (feature_values != null)
          {
               for (int feat = 0; feat < feature_values.length; feat++)
               {
                    String this_feature = indent + indent;
                    if (feature_names != null)
                         this_feature += feature_names[feat] + ": ";
                    if (feature_values[feat] == null)
                         this_feature += "?";
                    else
                         for (int val = 0; val < feature_values[feat].length; val++)
                              this_feature += feature_values[feat][val] + " ";
                    this_feature += "\n";
                    feature_string += this_feature;
               }
               feature_string += "\n";
          }

          // Add sub-sections of this dataset
          String sub_set_string = new String("");
          if (sub_sets != null)
               for (int set = 0; set < sub_sets.length; set++)
                    sub_set_string += sub_sets[set].getDataSetDescription(depth + 1);

          // Return the results
          return id_string + feature_string + sub_set_string;
     }


     /**
      * Returns a formatted text description of the given DataSet
      * objects.
      *
      * @param	dataset     The data sets to describe.
      * @return             The formatted description.
      */
     public static String getDataSetDescriptions(DataSet[] dataset)
     {
          String combined_descriptions = new String();
          for (int i = 0; i < dataset.length; i++)
               combined_descriptions += dataset[i].getDataSetDescription(0);
          return combined_descriptions;
     }

     /**
      * Given an array of DataSet objects, this method inspects the feature_names field of
      * each DataSet object (and each subset) to accumlate a list of all feature names.
      * The returned list will be a concatenation of all feature_names field of each
      * DataSet object (and each subset).
      *
      * @param feature_vectors      The feature vectors from which to create the list of
      *                             feature names.
      * @return                     String array containing the names of all features that
      *                             are used in the given DataSet object array. The
      *                             returned array will contain one cell for each value in
      *                             the DataSet object array. The returned array will
      *                             likely have many cells that contain the name feature
      *                             name.
      * @throws java.lang.Exception If the feature_names field of one of the given DataSet
      *                             objects is found to be null.
      */
     public static String[] getFeatureNames(DataSet[] feature_vectors)
             throws Exception
     {
         LinkedList <String> names = new LinkedList<String>();
         for (int i=0; i < feature_vectors.length; i++)
         {
             if (feature_vectors[i].feature_names != null)
             {
                for (int j=0; j < feature_vectors[i].feature_names.length; j++)
                {
                   names.add(feature_vectors[i].feature_names[j]);
                }
                if (feature_vectors[i].sub_sets != null)
                {
                    for (int k=0; k < feature_vectors[i].sub_sets.length; k++)
                    {
                         for(int l=0; l < feature_vectors[i].sub_sets[k].feature_names.length; l++)
                        {
                             names.add(feature_vectors[i].sub_sets[k].feature_names[l]);
                        }
                    }
                }
             }
             else
                 throw new Exception (" Unable to access feature_names field of DataSet object. ");
         }
         return names.toArray(new String[1]);
     }

     /**
      * Parses a feature_vector_file XML file and returns an array of
      * DataSet objects holding its contents. An exception is thrown
      * if the file is invalid in some way.
      *
      * @param	data_set_file_path	The path of the XML file to parse.
      * @return                         An array of DataSet objects holding the
      *                                 contents of the given ACE XML feature vectors file.
      * @throws	Exception		Informative exception is thrown if an
      *					invalid file or file path is specified.
      */
     public static DataSet[] parseDataSetFile(String data_set_file_path)
     throws Exception
     {
          // Parse the file
          Object[] results = (Object[]) XMLDocumentParser.parseXMLDocument(data_set_file_path, "feature_vector_file");
          DataSet[] parse_results = new DataSet[results.length];
          for (int i = 0; i < parse_results.length; i++)
               parse_results[i] = (DataSet) results[i];

          // Return the results
          return parse_results;
     }


     /**
      * Parses a feature_vector_file XML file and returns an array of
      * DataSet objects holding its contents. An exception is thrown
      * if the file is invalid in some way.
      *
      * <p>Also processes each resulting DataSet in order to reconcile it
      * with the given definitions. See the orderAndCompactFeatures method
      * for details.
      *
      * @param	data_set_file_path	The path of the XML file to parse.
      * @param	definitions         FeatureDefinitions to use for formatting
      *                             and validating the contents of the file
      *                             to be parsed.
      * @return                     array of DataSet objects holding the contents
      *                             of the given ACE XML feature fectors file.
      * @throws	Exception           Informative exceptions is thrown if an
      *                             invalid file or file path is specified.
      *                             An exception is also thrown if the
      *                             given feature definitions are
      *                             incompatible with the contents of the file.
      */
     public static DataSet[] parseDataSetFile( String data_set_file_path,
          FeatureDefinition[] definitions )
          throws Exception
     {
          // Parse the file
          Object[] results = (Object[]) XMLDocumentParser.parseXMLDocument(data_set_file_path, "feature_vector_file");
          DataSet[] parse_results = new DataSet[results.length];
          for (int i = 0; i < parse_results.length; i++)
               parse_results[i] = (DataSet) results[i];

          /* Commented August 7th 2009 when ACE was changed to automatically generate
          feature definitions.*/
          // Reconcile the results with the definitions
//          for (int i = 0; i < parse_results.length; i++)
//               parse_results[i].orderAndCompactFeatures(definitions, true);

          // Return the results
          return parse_results;
     }


     /**
      * Parses a several feature_vector_file XML files and returns an array of
      * DataSet objects holding the combined contents of all of the files. An
      * exception is thrown if the file is invalid in some way.
      *
      * <p>Also processes each resulting DataSet in order to reconcile it
      * with the given definitions. See the orderAndCompactFeatures method
      * for details. This will not occur if the definitions parameter is null.
      *
      * @param	data_set_file_paths	The paths of the XML files to parse.
      * @param	definitions		FeatureDefinitions to use for formatting
      *					and validating the contents of the files
      *					to be parsed.
      * @return                         An array of DataSet objects holding the
      *                                 combined contents of all of the given ACE
      *                                 XML feature vectors files.
      * @throws	Exception		Informative exceptions is thrown if an
      *					invalid file or file path is specified.
      *					An exception is also thrown if the
      *					given feature definitions are
      *					incompatible with the contents of a
      *                                 file.
      */
     public static DataSet[] parseDataSetFiles( String[] data_set_file_paths,
          FeatureDefinition[] definitions )
          throws Exception
     {
          // Parse and process each of the files separately
          DataSet[][] segmented_data_sets = new DataSet[data_set_file_paths.length][];
          int number_data_sets = 0;
          for (int file = 0; file < segmented_data_sets.length; file++)
          {
               // Parse a file
               Object[] results = (Object[]) XMLDocumentParser.parseXMLDocument(data_set_file_paths[file], "feature_vector_file");
               DataSet[] parse_results = new DataSet[results.length];
               for (int i = 0; i < parse_results.length; i++)
                    parse_results[i] = (DataSet) results[i];

               /* Commented August 7th 2009 when ACE was changed to automatically generate
          feature definitions.*/
               // Reconcile the results with the definitions
//               if (definitions != null)
//                    for (int i = 0; i < parse_results.length; i++)
//                         parse_results[i].orderAndCompactFeatures(definitions, true);

               // Store the datasets parsed from the file
               segmented_data_sets[file] = parse_results;
               number_data_sets += parse_results.length;
          }

          // Combine the DataSets into one array
          DataSet[] combined_data_sets = new DataSet[number_data_sets];
          int current_set = 0;
          for (int file = 0; file < segmented_data_sets.length; file++)
               for (int set = 0; set < segmented_data_sets[file].length; set++)
               {
               combined_data_sets[current_set] = segmented_data_sets[file][set];
               current_set++;
               }

          // Return the results
          return combined_data_sets;
     }


     /**
      * Merges the different extracted features contained in multiple DataSet
      * objects that hold references to the same instances. This could be
      * useful, for example, for combining features extracted from jAudio with
      * features extracted with jWebMiner for the same songs.
      *
      * <p>Note that the different DataSets must each contain references to the
      * same instances, but with entirely different feature types. The original
      * DataSets ARE changed.
      *
      * <p>NOTE THAT THIS HAS NOT BEEN DESIGNED TO WORK WITH DATASETS THAT
      * HAVE SUBSETS YET (e.g. features separately extracted separately for
      * windows). Such subsets are currently just ignored.
      *
      * @param datasets_to_combine           An array of sets of DataSets to
      *                                      combine into one. The first
      *                                      dimension refers to the group of
      *                                      DataSets and the second dimension
      *                                      refers to the DataSets within the
      *                                      given group. Each DataSet in each
      *                                      group should have the same feature
      *                                      types, but the feature types in
      *                                      the different groups should be
      *                                      entirely different. There should
      *                                      be one version of each instance
      *                                      in each group.
      * @param combined_feature_definitions  The combined set of all
      *                                      FeatureDefinition objects for
      *                                      all features in all of the
      *                                      datasets_to_combine.
      * @param matching_identifier_keys      Sets of identifiers linking
      *                                      DataSet objects from different
      *                                      groups in the datasets_to_combine
      *                                      parameter. The first dimension
      *                                      indicates an instance (ordering
      *                                      does not matter) and the second
      *                                      dimension has one value for
      *                                      each value DataSet group indicating
      *                                      the corresponding identifier field,
      *                                      plus one more indicating the value
      *                                      to use in the identifier field
      *                                      in the returned DataSet[] (ordering
      *                                      does matter her). Note that keys
      *                                      must be unique within each column
      *  @return                             The instances with their features
      *                                      combined.
      * @throws	Exception                    An informative Exception is
      *                                      returned if a problem occurs.
      */
     public static DataSet[] getMergedFeatureTypes(DataSet[][] datasets_to_combine,
          FeatureDefinition[] combined_feature_definitions,
          String[][] matching_identifier_keys)
          throws Exception
     {
          // Throw exceptions if parameters are erroneous
          if (datasets_to_combine == null)
               throw new Exception("No feature vectors provided.");
          if (combined_feature_definitions == null)
               throw new Exception("No feature definitions provided.");
          if (matching_identifier_keys == null)
               throw new Exception("No matching keys provided.");
          if (datasets_to_combine.length == 1)
               throw new Exception("More than one set of feature vectors must " +
                    "be provided.");
          for (int i = 1; i < datasets_to_combine.length; i++)
          {
               if (datasets_to_combine[i].length != datasets_to_combine[0].length)
                    throw new Exception("All sets of feature vectors must have" +
                         " the same number of instances.");
          }
          if (matching_identifier_keys[0].length != datasets_to_combine.length + 1)
               throw new Exception("There must be exactly one more key provided" +
                    " than the number of sets of feature vectors.\nA total of " +
                    matching_identifier_keys[0].length + " keys were provided" +
                    " for " + datasets_to_combine.length + " sets of feature" +
                    "vectors.");
          for (int i = 1; i < matching_identifier_keys.length; i++)
          {
               if (matching_identifier_keys[i].length != matching_identifier_keys[0].length)
                    throw new Exception("There must be an equal number of keys " +
                         " for all instances.");
          }
          if (matching_identifier_keys.length != datasets_to_combine[0].length)
               throw new Exception("There must be an equal number of instances" +
                    " and of matching identifier keys.\n" +
                    datasets_to_combine[0].length + " instances were provided" +
                    " and " + matching_identifier_keys.length + " of identifier" +
                    " keys.");

          // Maxima
          int number_sets = datasets_to_combine.length;
          int number_instances = matching_identifier_keys.length;

          // To be returned
          DataSet[] output = new DataSet[number_instances];

          // For making sure that there are no duplicates
          boolean[][] used = new boolean[number_sets][number_instances];
          for (int i = 0; i < used.length; i++)
               for (int j = 0; j < used[i].length; j++)
                    used[i][j] = false;

          // To store indices across sets in datasets_to_combine
          int[][] mapping = new int[number_instances][number_sets];

          // Find mappings between the datasets
          for (int key_inst = 0; key_inst < number_instances; key_inst++)
          {
               // Generate the mappings
               for (int set = 0; set < number_sets; set++)
               {
                    boolean found_one = false;
                    for (int vect_inst = 0; vect_inst < datasets_to_combine[set].length; vect_inst++)
                    {
                         if (datasets_to_combine[set][vect_inst].identifier.equals(matching_identifier_keys[key_inst][set]))
                         {
                              // Note if this identifier has already been noted
                              if (used[set][vect_inst])
                                   throw new Exception("More than one occurence of the key " + datasets_to_combine[set][vect_inst].identifier + " in the same set of instances.");

                              // Note the mapping
                              mapping[key_inst][set] = vect_inst;

                              // Note that this identifier has been noted
                              used[set][vect_inst] = true;
                              found_one = true;

                              // Exit the loop
                              vect_inst = datasets_to_combine[set].length;
                         }
                    }

                    // Throw an excpetion if the instance was not found
                    if (!found_one)
                         throw new Exception("No instance with the identifiers " +
                              matching_identifier_keys[key_inst][set] + " was found in set " +
                              (set+1) + ".");
               }
          }

          // Merge and store the combined features
          for (int key_inst = 0; key_inst < matching_identifier_keys.length; key_inst++)
          {
               // Copy the settings for the instance in the 0th set
               output[key_inst] = datasets_to_combine[ 0 ][ mapping[key_inst][0] ];

               // Set the identifier to the chosen value
               output[key_inst].identifier = matching_identifier_keys[key_inst][ matching_identifier_keys[0].length - 1 ];

               // Perform the merge
               for (int set = 1; set < datasets_to_combine.length; set++)
               {
                    // Rename for clarity
                    DataSet to_merge_into = output[key_inst];
                    DataSet to_merge_in = datasets_to_combine[ set ][ mapping[key_inst][set] ];

                    // Merge feature values and feature names
                    double[][] new_feature_values = new double[to_merge_into.feature_values.length + to_merge_in.feature_values.length][];
                    String[] new_feature_names = null;
                    if (to_merge_into.feature_names != null && to_merge_in.feature_names != null)
                         new_feature_names = new String[ to_merge_into.feature_names.length + to_merge_in.feature_names.length ];
                    for (int feat = 0; feat < to_merge_into.feature_values.length; feat++)
                    {
                         new_feature_values[feat] = to_merge_into.feature_values[feat];
                         if (new_feature_names != null)
                              new_feature_names[feat] = to_merge_into.feature_names[feat];
                    }
                    for (int feat = 0; feat < to_merge_in.feature_values.length; feat++)
                    {
                         new_feature_values[feat + to_merge_into.feature_values.length] = to_merge_in.feature_values[feat];
                         if (new_feature_names != null)
                              new_feature_names[feat + to_merge_into.feature_names.length] = to_merge_in.feature_names[feat];
                    }

                    // Set the values
                    output[key_inst].feature_values = new_feature_values;
                    output[key_inst].feature_names = new_feature_names;
               }

               // Order and compact the features
               //  output[key_inst].orderAndCompactFeatures(combined_feature_definitions, true);
          }

          // Return the output
          return output;
     }


     /**
      * Saves a feature_vector_file XML file with the contents specified
      * in the given DataSet array and the comments specified in the
      * comments parameter. Uses the feature_names in each of the data_sets
      * if they are present, and uses those in the definitions parameter
      * if they are not present in a given DataSet. If all data_sets
      * contain feature_names, then the passed value of definitions
      * may be null. This method does not apply the orderAndCompactFeatures
      * method.
      *
      * <p>In general, it is best to have applied the orderAndCompactFeatures
      * method to data_sets before calling this saveDataSets method.
      *
      * @param	data_sets	The DataSets to save.
      * @param	definitions	The FeatureDefinitions to base feature names
      *				on if they are not present in individual
      *				DataSets. May be null.
      * @param	to_save_to	The file to save to.
      * @param	comments	Any comments to be saved inside the comments
      *				element of the XML file.
      * @throws	Exception	An informative exception is thrown if the
      *				file cannot be saved or if feature names
      *				are available in neither individual data_sets
      *				nor in definitions.
      */
     public static void saveDataSets( DataSet[] data_sets,
          FeatureDefinition[] definitions,
          File to_save_to,
          String comments )
          throws Exception
     {
          // Throw an exception if feature names are unavailable through either
          // feature definitions or the data sets themselves
          if (definitions == null)
               for (int set = 0; set < data_sets.length; set++)
                    if (data_sets[set].feature_names == null)
                         throw new Exception( "Could not save because no feature definitions\n" +
                              "were provided and DataSet " + data_sets[set].identifier + "\n" +
                              "does not hold the names of its features." );

          // Perform the save
          try
          {
               // Prepare stream writer
               FileOutputStream to = new FileOutputStream(to_save_to);
               DataOutputStream writer = new DataOutputStream(to);

               // Write the header and the first element of the XML file
               String pre_tree_part = new String
                    (
                    "<?xml version=\"1.0\"?>\n" +
                    "<!DOCTYPE feature_vector_file [\n" +
                    "   <!ELEMENT feature_vector_file (comments, data_set+)>\n" +
                    "   <!ELEMENT comments (#PCDATA)>\n" +
                    "   <!ELEMENT data_set (data_set_id, section*, feature*)>\n" +
                    "   <!ELEMENT data_set_id (#PCDATA)>\n" +
                    "   <!ELEMENT section (feature+)>\n" +
                    "   <!ATTLIST section start CDATA \"\"\n" +
                    "                     stop CDATA \"\">\n" +
                    "   <!ELEMENT feature (name, v+)>\n" +
                    "   <!ELEMENT name (#PCDATA)>\n" +
                    "   <!ELEMENT v (#PCDATA)>\n" +
                    "]>\n\n" +
                    "<feature_vector_file>\n\n" +
                    "   <comments>" + comments + "</comments>\n\n"
                    );
               writer.writeBytes(pre_tree_part);

               // Write the XML code to represent the contents of each DataSet
               for (int set = 0; set < data_sets.length; set++)
               {
                    writer.writeBytes("   <data_set>\n");
                    writer.writeBytes("      <data_set_id>" + data_sets[set].identifier + "</data_set_id>\n");

                    if (data_sets[set].sub_sets != null)
                    {
                         for (int sec = 0; sec < data_sets[set].sub_sets.length; sec++)
                         {
                              DataSet section_set = data_sets[set].sub_sets[sec];
                              writer.writeBytes("      <section start=\"" + section_set.start + "\" stop=\"" + section_set.stop +"\">\n");
                              for (int feat = 0; feat < section_set.feature_values.length; feat++)
                              {
                                   double[] values = section_set.feature_values[feat];
                                   if (values != null)
                                   {
                                        writer.writeBytes("         <feature>\n");
                                        String name;
                                        if (section_set.feature_names != null)
                                             writer.writeBytes("            <name>" + section_set.feature_names[feat] + "</name>\n");
                                        else
                                             writer.writeBytes("            <name>" + definitions[feat].name + "</name>\n");
                                        for (int val = 0; val < values.length; val++)
                                             writer.writeBytes("            <v>" + values[val] + "</v>\n");
                                        writer.writeBytes("         </feature>\n");
                                   }
                              }
                              writer.writeBytes("      </section>\n");
                         }
                    }

                    if (data_sets[set].feature_values != null)
                    {
                         for (int feat = 0; feat < data_sets[set].feature_values.length; feat++)
                         {
                              double[] values = data_sets[set].feature_values[feat];
                              if (values != null)
                              {
                                   writer.writeBytes("      <feature>\n");
                                   String name;
                                   if (data_sets[set].feature_names != null)
                                        writer.writeBytes("         <name>" + data_sets[set].feature_names[feat] + "</name>\n");
                                   else
                                        writer.writeBytes("         <name>" + definitions[feat].name + "</name>\n");
                                   for (int val = 0; val < values.length; val++)
                                        writer.writeBytes("         <v>" + values[val] + "</v>\n");
                                   writer.writeBytes("      </feature>\n");
                              }
                         }
                    }

                    writer.writeBytes("   </data_set>\n\n");
               }
               writer.writeBytes("</feature_vector_file>");

               // Close the output stream
               writer.close();
          }
          catch (Exception e)
          {
               throw new Exception("Unable to write file " + to_save_to.getName() + ".");
          }
     }
}