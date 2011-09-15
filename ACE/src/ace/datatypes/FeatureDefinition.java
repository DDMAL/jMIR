/*
 * FeatureDefinition.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import java.io.*;
import java.util.Vector;
import ace.xmlparsers.XMLDocumentParser;
import weka.core.Instances;


/**
 * Objects of this class each hold meta-data about a feature, as specified by
 * the four public fields. Objects of this class do not hold any feature values
 * of particular instances.
 *
 * <p>Methods are available for viewing the features, veryifying the uniqueness
 * of their names, saving them to disk and loading the, from disk.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class FeatureDefinition
     implements Serializable
{
     /* FIELDS ****************************************************************/


     /**
      * The name of the feature. This name should be unique among each set of
      * features.
      */
     public	String				name;


     /**
      * A description of what the feature represents. May be left as an empty
      * string.
      */
     public	String				description;


     /**
      * Specifies whether a feature can be applied to sub-section of a data
      * set (e.g. a window of audio). A value of true means that it can, and a
      * value of false means that the feature may only be extracted per data
      * set.
      */
     public	boolean				is_sequential;


     /**
      * The number of values that exist for the feature for a given section of a
      * data set. This value will be 1, except for multi-dimensional features.
      */
     public	int				dimensions;


     /**
      * An identifier for use in serialization.
      */
     private   static final long                serialVersionUID = 2L;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Generate an empty FeatureDefinition with the name "Undefined Feature".
      */
     public FeatureDefinition()
     {
          name = "Undefined Feature";
          description = new String("");
          is_sequential = false;
          dimensions = 1;
     }


     /**
      * Explicitly define a new Feature Definition.
      *
      * @param	name          The name of the feature. This name should be
      *                       unique
      *                       among each set of features.
      * @param	description   A description of what the feature represents. May
      *                       be left as an empty string.
      * @param	is_sequential Specifies whether a feature can be applied to
      *                       sequential windows of a data set. A value of true
      *                       means that it can, and a value of false means that
      *                       only one feature value may be extracted per data
      *                       set.
      * @param	dimensions    The number of values that exist for the feature
      *                       for a given section of a data set. This value will
      *                       be 1, except for multi-dimensional features.
      */
     public FeatureDefinition( String name,
          String description,
          boolean is_sequential,
          int dimensions )
     {
          this.name = name;
          this.description = description;
          this.is_sequential = is_sequential;
          this.dimensions = dimensions;
     }

     /**
      * Generates a FeatureDefintition from a Weka ARFF file.
      * @param instances        The WEKA instances that were extracted from the ARFF file.
      * @param index            Specifies which attribute of the WEKA ARFF file should be used for this FeatureDefintion.
      */
     public FeatureDefinition(Instances instances, int index)
     {
        name = instances.attribute(index).name();
        description = "";
        is_sequential = false;
        dimensions = 1;
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Returns a formatted text description of the FeatureDescription object.
      *
      * @return	The formatted description.
      */
     public String getFeatureDescription()
     {
          String info = "NAME: " + name + "\n";
          info += "DESCRIPTION: " + description + "\n";
          info += "IS SEQUENTIAL: " + is_sequential + "\n";
          info += "DIMENSIONS: " + dimensions + "\n\n";
          return info;
     }


     /**
      * Returns a formatted text description of the given FeatureDescription
      * objects.
      *
      * @param	definitions	The feature definitions to describe.
      * @return			The formatted description.
      */
     public static String getFeatureDescriptions(FeatureDefinition[] definitions)
     {
          String combined_descriptions = new String();
          for (int i = 0; i < definitions.length; i++)
               combined_descriptions += definitions[i].getFeatureDescription();
          return combined_descriptions;
     }


     /**
      * Parses a feature_key_file_path XML file and returns an array of
      * FeatureDefinition objects holding its contents. An exception is
      * thrown if the file is invalid in some way or if the file contains
      * multiple features with the same name.
      *
      * @param	feature_key_file_path	The path of the XML file to parse.
      * @return                         Array of FeatureDefinition objects representing
      *                                 the contents of the given ACE XML feature
      *                                 definitions file.
      * @throws	Exception               Informative exceptions is thrown if an
      *                                 invalid file or file path is specified
      *                                 or if the file holds multiple features
      *                                 with the same name.
      */
     public static FeatureDefinition[] parseFeatureDefinitionsFile(String feature_key_file_path)
     throws Exception
     {
          // Parse the file
          Object[] results = (Object[]) XMLDocumentParser.parseXMLDocument(feature_key_file_path, "feature_key_file");
          FeatureDefinition[] parse_results = new FeatureDefinition[results.length];
          for (int i = 0; i < parse_results.length; i++)
               parse_results[i] = (FeatureDefinition) results[i];

          // Throw an exception if the definitions have features with duplicate names
          String duplicates = verifyFeatureNameUniqueness(parse_results);
          if (duplicates != null)
               throw new Exception( "Could not parse because there are multiple\n" +
                    "occurences of the following feature names:\n" +
                    duplicates );

          // Return the results
          return parse_results;
     }


     /**
      * Saves a feature_key_file_path XML file with the contents specified
      * in the given FeatureDefinition array and the comments specified in the
      * comments parameter. Also verifies that all of the given definitions
      * have unique names, and throws an exception if they do not.
      *
      * @param	definitions	The FeatureDefinitions to save.
      * @param	to_save_to	The file to save to.
      * @param	comments	Any comments to be saved inside the comments
      *				element of the XML file.
      * @throws	Exception	An informative exception is thrown if the
      *				file cannot be saved or if any of the given
      *				definitions have the same name.
      */
     public static void saveFeatureDefinitions( FeatureDefinition[] definitions,
          File to_save_to,
          String comments )
          throws Exception
     {
          // Throw an exception if the definitions have features with duplicate names
          String duplicates = verifyFeatureNameUniqueness(definitions);
          if (duplicates != null)
               throw new Exception( "Could not save because there are multiple\n" +
                    "occurences of the following feature names:\n" +
                    duplicates );

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
                    "<!DOCTYPE feature_key_file [\n" +
                    "   <!ELEMENT feature_key_file (comments, feature+)>\n" +
                    "   <!ELEMENT comments (#PCDATA)>\n" +
                    "   <!ELEMENT feature (name, description?, is_sequential, parallel_dimensions)>\n" +
                    "   <!ELEMENT name (#PCDATA)>\n" +
                    "   <!ELEMENT description (#PCDATA)>\n" +
                    "   <!ELEMENT is_sequential (#PCDATA)>\n" +
                    "   <!ELEMENT parallel_dimensions (#PCDATA)>\n" +
                    "]>\n\n" +
                    "<feature_key_file>\n\n" +
                    "   <comments>" + comments + "</comments>\n\n"
                    );
               writer.writeBytes(pre_tree_part);

               // Write the XML code to represent the contents of each FeatureDefinition
               for (int feat = 0; feat < definitions.length; feat++)
               {
                    writer.writeBytes("   <feature>\n");
                    writer.writeBytes("      <name>" + definitions[feat].name + "</name>\n");
                    if (!definitions[feat].description.equals(""))
                         writer.writeBytes("      <description>" + definitions[feat].description + "</description>\n");
                    writer.writeBytes("      <is_sequential>" + definitions[feat].is_sequential + "</is_sequential>\n");
                    writer.writeBytes("      <parallel_dimensions>" + definitions[feat].dimensions + "</parallel_dimensions>\n");
                    writer.writeBytes("   </feature>\n\n");
               }
               writer.writeBytes("</feature_key_file>");

               // Close the output stream
               writer.close();
          }
          catch (Exception e)
          {
               throw new Exception("Unable to write file " + to_save_to.getName() + ".");
          }
     }


     /**
      * Checks if the given FeatureDefinitions hold any features with the same
      * names. Returns null if there are no duplicates and a formatted string of
      * the names which are duplicated if there are duplicates.
      *
      * @param	definitions   The FeatureDefinitions to check for duplicate
      *                       names.
      * @return               Null if there are no duplicates and the names of
      *                       the duplicates if there are duplicates.
      */
     public static String verifyFeatureNameUniqueness(FeatureDefinition[] definitions)
     {
          boolean found_duplicate = false;
          Vector<String> duplicates = new Vector<String>();
          for (int i = 0; i < definitions.length - 1; i++)
               for (int j = i + 1; j < definitions.length; j++)
                    if (definitions[i].name.equals(definitions[j].name))
                    {
               found_duplicate = true;
               duplicates.add(definitions[i].name);
               j = definitions.length;
                    }
          if (found_duplicate)
          {
               Object[] duplicated_names_obj = (Object[])duplicates.toArray();
               String[] duplicated_names = new String[duplicated_names_obj.length];
               for (int i = 0; i < duplicated_names.length; i++)
                    duplicated_names[i] = (String) duplicated_names_obj[i];
               String duplicates_formatted = new String();
               for (int i = 0; i < duplicated_names.length; i++)
               {
                    duplicates_formatted += duplicated_names[i];
                    if (i < duplicated_names.length - 1)
                         duplicates_formatted += ", ";
               }
               return duplicates_formatted;
          }
          else
               return null;
     }


     /**
      * Takes multiple different arrays of feature definitions and combines
      * them.
      *
      * @param to_combine     The feature definitions to combine. Ordering does
      *                       not matter and they will not be changed.
      * @return               The feature definitions combined together into
      *                       a single array.
      * @throws Exception     An informative exception is thrown if there are
      *                       any multiples of the same features.
      */
     public static FeatureDefinition[] getMergedFeatureDefinitions(FeatureDefinition[][] to_combine)
     throws Exception
     {
          // Prepare to hold the combined feature defintions
          int number_features = 0;
          for (int i = 0; i < to_combine.length; i++)
               number_features += to_combine[i].length;
          FeatureDefinition[] feature_definitions_combined = new FeatureDefinition[number_features];

          // Combine the features
          int index_so_far = 0;
          for (int i = 0; i < to_combine.length; i++)
               for (int j = 0; j < to_combine[i].length; j++)
               {
               feature_definitions_combined[index_so_far] = to_combine[i][j];
               index_so_far++;
               }

          // Throw an exception if the definitions have features with duplicate names
          String duplicates = verifyFeatureNameUniqueness(feature_definitions_combined);
          if (duplicates != null)
               throw new Exception( "Could not combine the feature types becauase the\n" +
                    "Feature Definitions to be combined contain multiple\n" +
                    "occurences of the following feature names:\n" + duplicates );

          // Return the results
          return feature_definitions_combined;
     }

     /**
      * Automatically generates a FeatureDefinitions array based on the features specified
      * in the given DataSet object. This method is called when feature vectors have been
      * loaded but feature definitions have not. This method is called by the GUI when
      * feature vectors are loaded prior to feature definitions.
      *
      * @param feature_vectors  DataSet object from which to automatically generate a
      *                         FeatureDefinition array.
      * @return                 The automatically generated FeatureDefinition array.
      * @throws Exception       If an error occurs while searching for feature names.
      */
     public static FeatureDefinition[] generateFeatureDefinitions(DataSet[] feature_vectors)
             throws Exception
     {
        String[] features = DataSet.getFeatureNames(feature_vectors);
        features = mckay.utilities.staticlibraries.StringMethods.removeDuplicateEntries(features);

        FeatureDefinition[] generated = new FeatureDefinition[features.length];
        for (int i=0; i < features.length; i++)
        {
            // Assumes not sequential and dimension of 1
            generated[i] = new FeatureDefinition(features[i], "", false, 1);
        }
        return generated;
     }
}