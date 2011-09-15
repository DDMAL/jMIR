/*
 * DataBoard.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import java.io.*;
import java.util.LinkedList;
import weka.core.*;
import weka.core.converters.ArffLoader;


/**
 * Stores the data needed for training, testing and using classifiers. Stores
 * a taxonomy, feature definitions, feature vectors of instances and model
 * classifications of instances.
 *
 * <p>The contents of objects of this class can be loaded from ACE XML files or a Weka ARFF file using
 * one of the constructors. Methods are also implemented for saving and loading
 * objects of this class directly as serializable objects. The contents of an
 * object of this class may also be separated and saved as individual XML files.
 *
 * <p>A method is also available for generating a Weka ARFF file from an object
 * of this class. This method also generates an array of strings identifying
 * the source of each line in the resulting ARFF file.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class DataBoard
     implements Serializable
{
     /* FIELDS ****************************************************************/

     /**
      * The taxonomy that instances are classified into. May be hierarchical.
      *
      * <p>May be null if clustering algorithms are to be used or if the
      * taxonomy is to be derived from the model_classifications field.
      */
     public    Taxonomy                      taxonomy;


     /**
      * Holds meta-data about the feautres that characterize instances.
      *
      * <p>May be null if the feature_vectors have sufficient self-contained
      * infromation, although this is not recommended.
      */
     public    FeatureDefinition[]           feature_definitions;


     /**
      * Feature vectors for a set of instances. Can include features for
      * sub-sections of instances as well as for instances as a whole.
      *
      * <p>In general, these should be taken in conjunction with
      * feature_definitions in order to minimize storage space and processing
      * overhead.
      */
     public    DataSet[]                     feature_vectors;


     /**
      * The model classifications that are used in supervised training.
      * Can include classifications for sub-sections of instances as well
      * as for instances as a whole.
      *
      * <p>Class names should correspond with those in the taxonomy field.
      * Instances should correspond to those in the feature_vectors field.
      *
      * <p>May be null if clustering algorithms are to be used of if this
      * DataBoard is being used to classify novel patterns with already
      * trained classifiers.
      */
     public    SegmentedClassification[]     model_classifications;

     /**
      * An identifier for use in serialization.
      */
     private   static final long             serialVersionUID = 5L;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Generates an empty DataBoard.
      */
     public DataBoard()
     {
          taxonomy = null;
          feature_definitions = null;
          feature_vectors = null;
          model_classifications = null;
          /*taxonomy = new Taxonomy();
          feature_definitions = new FeatureDefinition[0];
          feature_vectors = new DataSet[0];
          model_classifications = new SegmentedClassification[0];*/
     }


     /**
      * Generates a DataBoard with the fields specified in the parameters.
      * Note that if feature definitions and feature vectors are both provided
      * then the feature vectors will be compacted and ordered automatically
      * based on the feature definitions. Some validation is performed on the
      * loaded values.
      *
      * @param	taxonomy		The taxonomy to classify instances into.
      * @param	feature_definitions	Descriptiosn of features to characterize
      *					features with.
      * @param	feature_vectors		The feature vectors characterizing
      *					instances.
      * @param	model_classifications	Model classifications for use in
      *					supervised training.
      * @throws	Exception		An informative exception is thrown
      *					if any of the data in the providedfields
      *                                 are incompatible with one another.
      */
     public DataBoard( Taxonomy taxonomy,
          FeatureDefinition[] feature_definitions,
          DataSet[] feature_vectors,
          SegmentedClassification[] model_classifications )
          throws Exception
     {
          // Set the fields
          this.taxonomy = taxonomy;
          this.feature_definitions = feature_definitions;
          this.feature_vectors = feature_vectors;
          this.model_classifications = model_classifications;

          /* Commented August 7th 2009 when ACE was changed to automatically generate
          feature definitions.*/
          // Reconcile feature vectors with feature definitions
//          if (feature_definitions != null && feature_vectors != null)
//               for (int i = 0; i < feature_vectors.length; i++)
//                    feature_vectors[i].orderAndCompactFeatures(feature_definitions, true);

          // Verify the compatibility of the loaded data.
          validateFieldCompatibility();
     }


     /**
      * Generates a DataBoard based on the contents of the given XML files.
      * Note that if feature definitions and feature vectors are both provided
      * then the feature vectors will be compacted and ordered automatically
      * based on the feature definitions. Some validation is performed on the
      * loaded values.
      *
      * @param	classifications_file    The path of a classifications_file XML
      *					file holding a taxonomy. May be null
      *					if clustering is to be used to derive
      *					a new taxonomy or if a provided set of
      *					model classificatios will be used to
      *					construct a taxonomy. An entry of "" is
      *					considered equivalent to null.
      * @param	feature_key_file	The path of a feature_key_file XML
      *					file holding feature descriptions. May
      *					be null if the provided feature vectors
      *					have enough self-contained information,
      *					but this is not recommended. An entry of
      *					"" is considered equivalent to null.
      * @param	feature_vector_files	An array of file paths referring to
      *					feature_vector_files holding eature
      *                                 vectors for a set of instances. If a
      *                                 feature_key_file was provided, the
      *                                 feature_vector files are ordered and
      *                                 compacted based on it. An entrys of "" i
      *                                 considered equivalent to null.
      * @param	taxonomy_file		The path of a taxonomy_file XML file
      *					holding the model classifications that
      *                                 are used in supervised training using
      *                                 the given feature_vector_files. May be
      *                                 null if	clustering algorithms are to be
      *                                 used of if this DataBoard is being used
      *                                 to classify novel patterns with already
      *                                 trained	classifiers. An entry of "" is
      *                                 considered equivalent to null.
      * @throws	Exception		An informative exception is thrown
      *					if any of the file paths provided are
      *					invalid or if the data contained in the
      *					files is incompatible with one another.
      */
     public DataBoard( String taxonomy_file,
          String feature_key_file,
          String[] feature_vector_files,
          String classifications_file )
          throws Exception
     {
          // Parse the provided classifications_file and store its contents
          taxonomy = null;
          if (taxonomy_file != null)
               if (!taxonomy_file.equals(""))
                    taxonomy = Taxonomy.parseTaxonomyFile(taxonomy_file);

          // Parse the provided feature_key_file and store its contents
          feature_definitions = null;
          if (feature_key_file != null)
               if (!feature_key_file.equals(""))
                    feature_definitions = FeatureDefinition.parseFeatureDefinitionsFile(feature_key_file);

          // Parse the provided feature_vector_files and store their contents.
          // If feature definitions are available, the features in the feature
          // vectors are ordered and compacted.
          feature_vectors = null;
          if (feature_vector_files != null)
               if (!feature_vector_files[0].equals(""))
                    feature_vectors = DataSet.parseDataSetFiles(feature_vector_files, feature_definitions);

          // Parse the provided model_classifications and store its contents
          model_classifications = null;
          if (classifications_file != null)
               if (!classifications_file.equals(""))
                    model_classifications = SegmentedClassification.parseClassificationsFile(classifications_file);

          // Verify the compatibility of the loaded data.
          validateFieldCompatibility();
     }

     /**
      * Generates the ACE datatypes from a Weka ARFF file. Used for classifiying data from an ARFF file.
      * Because of the restrictions of Weka ARFF files, the created taxonomy will
      * be flat (will have no hierarchical structure), instances will be numbered
      * (since they have no unique identifier in ARFF format), and instances will
      * only have one classification.
      *
      * @param arff_file    The Weka ARFF file containing the Instances to be stored in this DataBoard.
      * @throws Exception   If an error occurs.
      */
     public DataBoard(String arff_file)
             throws Exception
     {
         // Get Weka Instances from ARFF file
         Instances instances;
         ArffLoader reader = new ArffLoader();
         reader.setFile(new File(arff_file));
         instances = reader.getDataSet();
         instances.setClassIndex(instances.numAttributes() - 1);

         // instantiate ACE datatypes
         taxonomy = new Taxonomy(instances);
         feature_definitions = new FeatureDefinition[instances.numAttributes()-1];
         for(int i = 0; i < instances.numAttributes()-1; i++)
         {
             feature_definitions[i] = new FeatureDefinition(instances, i);
         }
         feature_vectors = new DataSet[instances.numInstances()];
         model_classifications = new SegmentedClassification[instances.numInstances()];
         for(int i = 0; i < instances.numInstances(); i ++)
         {
             model_classifications[i] = new SegmentedClassification(instances.instance(i), i);
             feature_vectors[i] = new DataSet(instances.instance(i), i);
         }
     }

     /* PUBLIC METHODS ********************************************************/

     /**
      * Returns the taxonomy that instances are to be classified into. May be
      * null if clustering algorithms are to be used or if the taxonomy is to
      * be derived from the model_classifications file.
      *
      * @return The Taxonomy object of this DataBoard.
      */
     public Taxonomy getTaxonomy()
     {
          return taxonomy;
     }

     /**
      * Returns meta-data about the feautres that characterize instances.
      * This may be null if the feature_vectors have sufficient self-contained
      * infromation, although this is not recommended.
      *
      * @return The array of FeatureDefinition objects of this DataBoard.
      */
     public FeatureDefinition[] getFeatureDefinitions()
     {
          return feature_definitions;
     }

     /**
      * Returns feature vectors for a set of instances. This can include
      * features for sub-sections of instances as well as for instances as a
      * whole.
      *
      * <p>In general, these should be taken in conjunction with
      * FeatureDefinitions in order to minimize storage space and processing
      * overhead.
      *
      * @return The array of DataSet objects of this DataBoard.
      */
     public DataSet[] getFeatureVectors()
     {
          return feature_vectors;
     }

     /**
      * Returns the model classifications that are used in supervised training.
      * This can include classifications for sub-sections of instances as well
      * as for instances as a whole.
      *
      * <p>Class names should correspond with those in the Taxonomy.
      * Instances should correspond to those in the DataSet feature vectors.
      *
      * <p>Will return null if clustering algorithms are to be used of if this
      * DataBoard is being used to classify novel patterns with already
      * trained classifiers.
      *
      * @return The array of SegmentedClassification objects of this DataBoard.
      */
     public SegmentedClassification[] getModelClassifications()
     {
          return model_classifications;
     }


     /**
      * Returns the names of the features stored in the feature_definitions
      * field. Returns null if nothing is stored in this field.
      *
      * @return     The names of the features in the feature_definitions field,
      *             or null if there are none stored there.
      */
     public String[] getFeatureNames()
     {
          if (feature_definitions == null)
               return null;

          String[] feature_names = new String[feature_definitions.length];
          for (int i = 0; i < feature_names.length; i++)
               feature_names[i] = feature_definitions[i].name;
          return feature_names;
     }


     /**
      * Returns the number of dimensions of each of the features stored in the
      * feature_definitions field. Returns null if nothing is stored in this
      * field.
      *
      * @return     The number of dimensions of each of the features stored in
      *             the feature_definitions field, or null if there are none
      *             stored there.
      */
     public int[] getFeatureDimensionalities()
     {
          if (feature_definitions == null)
               return null;

          int[] dimensionalities = new int[feature_definitions.length];
          for (int i = 0; i < dimensionalities.length; i++)
               dimensionalities[i] = feature_definitions[i].dimensions;
          return dimensionalities;
     }


     /**
      * Returns the names of all meta-data fields stored in the contents of any
      * of the instances stored in the model_classifications field. Returns null
      * if model_classifications is empty or if there are no meta-data fields
      * stored.
      *
      * @return	The names of the meta-data fields, or null if there are none.
      */
     public String[] getInstanceMetaDataFields()
     {
          if (model_classifications == null)
               return null;

          String unique_field_names[] = null;
          for (int i = 0; i < model_classifications.length; i++)
          {
               if (model_classifications[i].misc_info_key != null)
               {
                    String[] new_fields = model_classifications[i].misc_info_key;
                    if (unique_field_names == null)
                         unique_field_names = new_fields;
                    else
                    {
                         for (int j = 0; j < new_fields.length; j++)
                         {
                              if (!mckay.utilities.staticlibraries.StringMethods.isStringInArray(new_fields[j], unique_field_names))
                              {
                                   String[] temp = new String[unique_field_names.length + 1];
                                   for (int k = 0; k < unique_field_names.length; k++)
                                        temp[k] = unique_field_names[k];
                                   temp[temp.length - 1] = new_fields[j];
                                   unique_field_names = temp;
                              }
                         }
                    }
               }
          }

          return unique_field_names;
     }


     /**
      * Searches the model_classifications stored in this DataBoard with an
      * identifier that matches the identifier of the given DataSet. Null is
      * returned if no SegmentedClassifications are available or no matching one
      * is present.
      *
      * @param	data_set The DataSet to attempt to find a matching model
      *                  classification for.
      * @return          The SegmentedClassification that has the same
      *			 identifier as the given DataSet.
      */
     public SegmentedClassification getMatchingModelClassification(DataSet data_set)
     {
          if (model_classifications == null)
               return null;
          for (int i = 0; i < model_classifications.length; i++)
               if (model_classifications[i].identifier.equals(data_set.identifier))
                    return model_classifications[i];
          return null;
     }


     /**
      * Uses the feature definitions and taxonomy stored in this DataBoard to
      * return an empty set of Weka Instances. If no taxonomy is available in
      * this DataBoard then model classifications are used to find class names.
      *
      * <p>The returned set includes all feature names, including numbered
      * feature names for multi-dimensional features, as well as class names.
      * Class names are put in the last Attribute. Only leaf class names are
      * used.
      *
      * <p>Note that Attribute information may not be changed after this method
      * is called.
      *
      * @param	data_set_name		The name to assign to the relation.
      * @param	initial_capacity	The initial capacity of the set.
      * @return				The empty set of WekaInstances with
      *                                 properly set Attributes.
      * @throws	Exception		An informative exception is thrown if
      *					insufficient information is available
      *					to construct the Attributes.
      */
     public Instances getInstanceAttributes( String data_set_name,
          int initial_capacity )
          throws Exception
     {
          // Verify that instances can be extracted
          if (feature_definitions == null)
               throw new Exception( "Cannot set up instances because no feature\n" +
                    "definitions are available." );
          if (feature_vectors == null)
               throw new Exception( "Cannot set up instances because no feature\n" +
                    "vectors are available." );

          // Find the class names from the taxonomy. If a taxonomy is not available,
          // then find them from the model classifications
          String[] class_names = getClassNames();
          if (class_names == null)
               throw new Exception( "Cannot set up instances because no class\n" +
                    "names available." );

          // Find the feature names (both single and multi-dimensional)
          LinkedList<String> feature_name_list = new LinkedList<String>();
          for (int i = 0; i < feature_definitions.length; i++)
          {
               if (feature_definitions[i].dimensions == 1)
                    feature_name_list.add(feature_definitions[i].name);
               else
                    for (int j = 0; j < feature_definitions[i].dimensions; j++)
                         feature_name_list.add(feature_definitions[i].name + " DIM " + j);
          }
          String[] feature_names = feature_name_list.toArray(new String[1]);

          // Fill the attributes with feature names and, in the last entry,
          // class names
          FastVector attributes_vector = new FastVector(feature_names.length + 1); // extra 1 is for class name
          for (int feat = 0; feat < feature_names.length; feat++)
               attributes_vector.addElement(new Attribute(feature_names[feat]));
          FastVector class_names_vector = new FastVector(class_names.length);
          for (int cat = 0; cat < class_names.length; cat++)
               class_names_vector.addElement(class_names[cat]);
          attributes_vector.addElement(new Attribute("Class", class_names_vector));

          // Generate and return instances
          Instances instances = new Instances( data_set_name,
               attributes_vector,
               initial_capacity );
          instances.setClassIndex(instances.numAttributes() - 1);
          return instances;
     }


     /**
      * Extracts the feature values and model classifications stored in this
      * DataBoard object and stores them in the given set of Weka Instances.
      *
      * <p>Both pre-classified and unclassified data may be dealt with.
      * Both overal data sets and data sets involving sub-sections may be
      * dealt with.
      *
      * <p>If the model_classifications field is null, no model classes are
      * saved. If the taxonomy field is null, then the class names are extracted
      * from the model_classifications field if it is not null.
      *
      * <p><b>IMPORTANT:</b> Since ARFF files cannot accomodate multiple classes
      * per instance, the feature vector for an instance with multiple classes
      * is repeated twice, once for each class.
      *
      * @param	set_of_instances             The Weka Instances object to store
      *                                      individual instances in.
      * @param	use_top_level_features       Whether or not to store overall
      *                                      classifications for individual
      *                                      instances.
      * @param	use_sub_section_features     Whether or not to store the sub-
      *                                      sections of instances.
      * @throws	Exception                    An exception is thrown if no
      *                                      feature definitions or no
      *                                      feature vectors are available. An
      *                                      exception is also thrown if both
      *                                      of the boolean parameters are false.
      */
     public void storeInstances( Instances set_of_instances,
          boolean use_top_level_features,
          boolean use_sub_section_features )
          throws Exception
     {
          // Throw exceptions if the feature definitions or feature vectors
          // are not available or if it is specified not to record any features
          if (feature_definitions == null)
               throw new Exception( "Cannot set up instances because no feature\n" +
                    "definitions are available." );
          if (feature_vectors == null)
               throw new Exception( "Cannot set up instances because no feature\n" +
                    "vectors are available." );
          if (!use_top_level_features && !use_sub_section_features)
               throw new Exception( "Cannot set up instances because it has been\n" +
                    "specified to store neither top-level nor\n" +
                    "sub-section features." );

          // Find the model classifications of data sets overall and of their sections
          String[][] model_classifications_overall = null;
          String[][][] model_classifications_sections = null;
          if (model_classifications != null)
          {
               if (use_top_level_features)
                    model_classifications_overall =
                         SegmentedClassification.getOverallLabelsOfDataSets( feature_vectors,
                         model_classifications );
               if (use_sub_section_features)
                    model_classifications_sections =
                         SegmentedClassification.getSubSectionLabelsOfDataSets( feature_vectors,
                         model_classifications );
          }

          // Store the feature vectors and the model classifications, if any
          for (int i = 0; i < feature_vectors.length; i++)
          {
               // Process top-level overall features
               if (use_top_level_features)
               {
                    // Find the top-level overall feature values
                    String[][] top_feat_vals = feature_vectors[i].getFeatureValuesOfTopLevel(feature_definitions);

                    // Write the top-level overall feature values and model classifications
                    if (top_feat_vals != null)
                    {
                         // May need to repeat a given instance multiple times
                         // if it has multiple classes
                         int classes = 1;
                         if (model_classifications_overall != null)
                              if (model_classifications_overall[i] != null)
                                   classes = model_classifications_overall[i].length;

                         // Store the feature values and model classifications
                         for (int cla = 0; cla < classes; cla++)
                         {
                              Instance this_instance = new Instance(set_of_instances.numAttributes());
                              this_instance.setDataset(set_of_instances);
                              int current_attribute = 0;

                              for (int j = 0; j < top_feat_vals.length; j++)
                                   for (int k = 0; k < top_feat_vals[j].length; k++)
                                   {
                                   // Store feature values in instance
                                   if (!top_feat_vals[j][k].equals("?"))
                                        this_instance.setValue(current_attribute, (new Double(top_feat_vals[j][k])).doubleValue());
                                   current_attribute++;

                                   // Store model classification in instance
                                   if ( j == top_feat_vals.length - 1 && k == top_feat_vals[j].length - 1 )
                                        if (model_classifications_overall != null)
                                             if (model_classifications_overall[i] != null)
                                                  this_instance.setClassValue(model_classifications_overall[i][cla]);

                                   }

                              // Add this instance to the list
                              set_of_instances.add(this_instance);
                         }
                    }
               }

               // Process features of sub-sections
               if (use_sub_section_features)
               {
                    // Find the sub-section feature values
                    String[][][] sec_feat_vals = feature_vectors[i].getFeatureValuesOfSubSections(feature_definitions);

                    // Write the sub-section feature values and model classifications
                    // for each sub-section
                    if (sec_feat_vals != null)
                    {
                         for (int sec = 0 ; sec < sec_feat_vals.length; sec++)
                         {
                              if (sec_feat_vals[sec] != null)
                              {
                                   // May need to repeat a given instance multiple times
                                   // if it has multiple classes
                                   int classes = 1;
                                   if (model_classifications_sections != null)
                                        if (model_classifications_sections[i] != null)
                                             if (model_classifications_sections[i][sec] != null)
                                                  classes = model_classifications_sections[i][sec].length;

                                   // Write the feature values and model classifications
                                   for (int cla = 0; cla < classes; cla++)
                                   {
                                        Instance this_instance = new Instance(set_of_instances.numAttributes());
                                        this_instance.setDataset(set_of_instances);
                                        int current_attribute = 0;

                                        for (int j = 0; j < sec_feat_vals[sec].length; j++)
                                             for (int k = 0; k < sec_feat_vals[sec][j].length; k++)
                                             {
                                             // Store feature values in instance
                                             if (!sec_feat_vals[sec][j][k].equals("?"))
                                                  this_instance.setValue(current_attribute, (new Double(sec_feat_vals[sec][j][k])).doubleValue());
                                             current_attribute++;

                                             // Write the model classification if features done
                                             if ( j == sec_feat_vals[sec].length - 1 && k == sec_feat_vals[sec][j].length - 1 )
                                                  if (model_classifications_sections != null)
                                                       if (model_classifications_sections[i] != null)
                                                            if (model_classifications_sections[i][sec] != null)
                                                                 this_instance.setClassValue(model_classifications_sections[i][sec][cla]);
                                             }

                                        // Add this instance to the list
                                        set_of_instances.add(this_instance);
                                   }
                              }
                         }
                    }
               }
          }
     }


     /**
      * Classify the given set of Instances using the given AttributeSelection
      * and the given Classifier. Return the results in a new
      * SegmentedClassification object.
      *
      * <p>No reference is mad to any model classifications.
      *
      * <p><b>IMPORTANT:</b> The order of the instances must not have been
      * changed from the time that they were constructed by a call to the
      * storeInstances method. If they have, or if the attribute_selector
      * reorders instances, then this method will not work properly.
      *
      * <p><b>IMPORTANT:</b> The use_top_level_features and
      * use_sub_section_features parameters must be the same as when the
      * instances were constructed with the storeInstances method.
      *
      * @param instances                The Weka Instances object to that
      *                                 individual instances tob be classified
      *                                 are stored in. In general, should
      *                                 have been generated with the
      *                                 storeInstances method.
      * @param save_intermediate_arffs	Whether or not to save testing data to
      *					an arff file after after feature
      *                                 selection, if any. Useful for testing.
      * @param trained                  Serializable object containing reference
      *                                 the Weka objects needed for classification
      *                                 (Classifier, AttributeSelection, Attribute (class attribute))
      * @param use_top_level_features	Whether or not to store overall
      *					classifications for individual instances.
      * @param use_sub_section_features Whether or not to store the sub-
      *					sections of instances.
      * @return                         The resulting classifications stored in
      *                                 an array of <i>SegmentedClassification</i>
      *                                 objects.
      * @throws	Exception		An exception occurs if Weka encounters
      *					a problem.
      */
     public SegmentedClassification[] getClassifiedResults( Instances instances,
          boolean save_intermediate_arffs,
          TrainedModel trained,
          boolean use_top_level_features,
          boolean use_sub_section_features )
          throws Exception
     {
          // The list of classificaiton resluts
          LinkedList<SegmentedClassification> overall_classifications = new LinkedList<SegmentedClassification>();
          // Apply the attribute selector
          if (trained.attribute_selector != null)
               instances = trained.attribute_selector.reduceDimensionality(instances);
          // Save a snapshot of the instances
          if (save_intermediate_arffs)
               saveInstancesAsARFF(instances, "testing_data_after_dimensionality_reduction.arff");

          // This indice of the instance in instances currently being dealt with
          int current_instance = 0;

          // Go through the input data sets one by one
          for (int set = 0; set < feature_vectors.length; set++)
          {
               // Match the DataSet that a set of features are stored in and the
               // SegmentedClassification that its classification will be stored in
               SegmentedClassification this_classification = new SegmentedClassification();
               this_classification.identifier = feature_vectors[set].identifier;

               // Process top-level overall features
               if (use_top_level_features)
                    if (feature_vectors[set].feature_values != null)
                    {
                    double predicted = trained.classifier.classifyInstance(instances.instance(current_instance));
                    current_instance++;
                    String classification = trained.class_attribute.value((int) predicted);
                    this_classification.classifications = new String[1];
                    this_classification.classifications[0] = classification;

                    // Store the classificaiton(s) for this data set if there are
                    // no sub-sections to store
                    if (!use_sub_section_features)
                         overall_classifications.add(this_classification);
                    else if (feature_vectors[set].sub_sets == null)
                         overall_classifications.add(this_classification);

                    }

               // Process features of sub-sections
               if (use_sub_section_features)
                    if (feature_vectors[set].sub_sets != null)
                    {
                    // The list of classificaitons for each sub-secion
                    LinkedList<SegmentedClassification> sub_section_classifications = new LinkedList<SegmentedClassification>();

                    // Go through the sub-sections one by one
                    for (int sec = 0 ; sec < feature_vectors[set].sub_sets.length; sec++)
                    {
                         DataSet this_sub_section = feature_vectors[set].sub_sets[sec];

                         if (this_sub_section.feature_values != null)
                         {
                              double predicted = trained.classifier.classifyInstance(instances.instance(current_instance));
                              current_instance++;
                              String classification = trained.class_attribute.value((int) predicted);

                              SegmentedClassification sub_section_result = new SegmentedClassification();
                              sub_section_result.classifications = new String[1];
                              sub_section_result.classifications[0] = classification;

                              sub_section_result.start = this_sub_section.start;
                              sub_section_result.stop = this_sub_section.stop;

                              sub_section_classifications.add(sub_section_result);
                         }
                    }

                    // Store the list of sub-section classificaitons
                    this_classification.sub_classifications = sub_section_classifications.toArray(new SegmentedClassification[1]);

                    // Store the classificaiton(s) for this data set
                    overall_classifications.add(this_classification);
                    }
          }

          // Return the classifications
          return overall_classifications.toArray(new SegmentedClassification[1]);
     }


     /**
      * Produces a Weka ARFF file based on the contents of this object. One
      * option is to save only the overall classifications for each instance.
      * Alternatively, the user can opt to save only the overall classifications
      * for each sub-section of each instance, without the overall
      * classifications. Finally, both can be saved together in the same file if
      * the user wishes.
      *
      * <p>If the model_classifications field is null, no model classes are
      * saved. If the taxonomy field is null, then the class names are extracted
      * from the model_classifications field if it is not null.
      *
      * <p>An array of strings is returned. There is one entry for each data
      * line saved to the ARFF file, with the entry identifying the data set and
      * (if appropriate) the section that each ARFF data line corresponds to.
      *
      * <p><b>IMPORTANT:</b> Since ARFF files cannot accomodate multiple classes
      * per instance, the feature vector for an instance with multiple classes
      * is repeated twice, once for each class.
      *
      * <p><b>IMPORTANT:</b> All class names and feature names have blank
      * spaces replaced by underscores in the ARFF file.
      *
      * @param	relation_name                The name of the relation that is
      *                                      being saved to the ARFF file.
      * @param	databoard_file               The ARFF file to be saved into.
      * @param	use_top_level_features       Whether or not to save overall
      *                                      classifications for individual
      *                                      instances.
      * @param	use_sub_section_features     Whether or not to save the sub-
      *                                      sections of instances.
      * @return                              The data set and section
      *                                      corresponding to each feature
      *                                      vector line saved in the ARFF
      *                                      file.
      * @throws	Exception                    An exception is thrown if no
      *                                      feature definitions or no
      *                                      feature vectors are provided. An
      *                                      exception is also thrown if both
      *                                      of the boolean parameters are
      *                                      false.
      */
     public String[] saveToARFF( String relation_name,
          File databoard_file,
          boolean use_top_level_features,
          boolean use_sub_section_features )
          throws Exception
     {
          // Throw exceptions if the feature definitions or feature vectors
          // are not available or if it is specified not to record any features
          if (feature_definitions == null)
               throw new Exception( "Cannot save ARFF file because no feature\n" +
                    "definitions are available." );
          if (feature_vectors == null)
               throw new Exception( "Cannot save ARFF file because no feature\n" +
                    "vectors are available." );
          if (!use_top_level_features && !use_sub_section_features)
               throw new Exception( "Cannot save ARFF file because it has been\n" +
                    "specified to store neither top-level nor\n" +
                    "sub-section features." );

          // Prepare stream writer
          FileOutputStream to = new FileOutputStream(databoard_file);
          DataOutputStream writer = new DataOutputStream(to);

          // Write the relation name
          writer.writeBytes("@relation " + relation_name + "\n\n");

          // Write the feature names
          for (int i = 0; i < feature_definitions.length; i++)
          {
               if (feature_definitions[i].dimensions == 1)
                    writer.writeBytes("@attribute " + feature_definitions[i].name.replace(' ', '_') + " numeric\n");
               else
                    for (int j = 0; j < feature_definitions[i].dimensions; j++)
                         writer.writeBytes("@attribute " + feature_definitions[i].name.replace(' ', '_') + "_" + j + " numeric\n");
          }

          // Write the class names
          String[] class_names = getClassNames();
          if (class_names != null)
          {
               writer.writeBytes("@attribute class? { ");
               for (int i = 0; i < class_names.length; i++)
               {
                    writer.writeBytes(class_names[i].replace(' ', '_'));
                    if (i != class_names.length - 1)
                         writer.writeBytes(", ");
                    else
                         writer.writeBytes(" }\n");
               }
          }

          // Find the model classifications of data sets overall and of their sections
          String[][] model_classifications_overall = null;
          String[][][] model_classifications_sections = null;
          if (model_classifications != null)
          {
               if (use_top_level_features)
                    model_classifications_overall =
                         SegmentedClassification.getOverallLabelsOfDataSets( feature_vectors,
                         model_classifications );
               if (use_sub_section_features)
                    model_classifications_sections =
                         SegmentedClassification.getSubSectionLabelsOfDataSets( feature_vectors,
                         model_classifications );
          }

          // Write the feature vectors and the model classifications, if any
          writer.writeBytes("\n@data\n");
          LinkedList<String> identifiers = new LinkedList<String>();
          for (int i = 0; i < feature_vectors.length; i++)
          {
               // Process top-level overall features
               if (use_top_level_features)
               {
                    // Find the top-level overall feature values
                    String[][] top_feat_vals = feature_vectors[i].getFeatureValuesOfTopLevel(feature_definitions);

                    // Write the top-level overall feature values and model classifications
                    if (top_feat_vals != null)
                    {
                         // May need to repeat a given instance multiple times
                         // if it has multiple classes
                         int classes = 1;
                         if (model_classifications_overall != null)
                              if (model_classifications_overall[i] != null)
                                   classes = model_classifications_overall[i].length;

                         // Write the feature values and model classifications
                         for (int cla = 0; cla < classes; cla++)
                              for (int j = 0; j < top_feat_vals.length; j++)
                                   for (int k = 0; k < top_feat_vals[j].length; k++)
                                   {
                              // Write the feature value
                              writer.writeBytes(top_feat_vals[j][k]);

                              // Write the model classification if features done
                              if ( j == top_feat_vals.length - 1 && k == top_feat_vals[j].length - 1 )
                              {
                                   if (model_classifications_overall != null)
                                   {
                                        if (model_classifications_overall[i] != null)
                                             writer.writeBytes(", " + model_classifications_overall[i][cla].replace(' ', '_'));
                                        else
                                             writer.writeBytes(", ?");
                                   }
                                   writer.writeBytes("\n");

                                   // Store the identifier
                                   identifiers.add(feature_vectors[i].identifier);
                              }
                              else
                                   writer.writeBytes(", ");
                                   }
                    }
               }

               // Process features of sub-sections
               if (use_sub_section_features)
               {
                    // Find the sub-section feature values
                    String[][][] sec_feat_vals = feature_vectors[i].getFeatureValuesOfSubSections(feature_definitions);

                    // Write the sub-section feature values and model classifications
                    // for each sub-section
                    if (sec_feat_vals != null)
                    {
                         for (int sec = 0 ; sec < sec_feat_vals.length; sec++)
                         {
                              if (sec_feat_vals[sec] != null)
                              {
                                   // May need to repeat a given instance multiple times
                                   // if it has multiple classes
                                   int classes = 1;
                                   if (model_classifications_sections != null)
                                        if (model_classifications_sections[i] != null)
                                             if (model_classifications_sections[i][sec] != null)
                                                  classes = model_classifications_sections[i][sec].length;

                                   // Write the feature values and model classifications
                                   for (int cla = 0; cla < classes; cla++)
                                        for (int j = 0; j < sec_feat_vals[sec].length; j++)
                                             for (int k = 0; k < sec_feat_vals[sec][j].length; k++)
                                             {
                                        // Write the feature value
                                        writer.writeBytes(sec_feat_vals[sec][j][k]);

                                        // Write the model classification if features done
                                        if ( j == sec_feat_vals[sec].length - 1 && k == sec_feat_vals[sec][j].length - 1 )
                                        {
                                             if (model_classifications_sections != null)
                                                  if (model_classifications_sections[i] != null)
                                                  {
                                                  if (model_classifications_sections[i][sec] != null)
                                                       writer.writeBytes(", " + model_classifications_sections[i][sec][cla].replace(' ', '_'));
                                                  else
                                                       writer.writeBytes(", ?");
                                                  }
                                             writer.writeBytes("\n");

                                             // Store the identifier
                                             identifiers.add(feature_vectors[i].identifier + ":  Start=" + feature_vectors[i].sub_sets[sec].start + "Stop=" + feature_vectors[i].sub_sets[sec].stop);
                                        }
                                        else
                                             writer.writeBytes(", ");
                                             }
                              }
                         }
                    }
               }
          }

          // Close the output streams
          writer.close();
          to.close();

          // Return the identifiers
          return identifiers.toArray(new String[1]);
     }


     /**
      * Saves the stored taxonomy, feature definitions, feature vectors and/or
      * model classifications stored in this DataBoard to individual XML files
      * of the respectively appropriate type. Each file is only saved if the
      * corresponding parameter is not null.
      *
      * @param	taxonomy_file           The file to save the taxonomy to. Null
      *                                 if the taxonomy is not to be saved.
      * @param	feature_key_file        The file to save the feature defintions
      *                                 to. Null if the definitions are not to
      *                                 be saved.
      * @param	feature_vector_file     The file to save the feature vectors to.
      *                                 Null if the vectors are not to be saved.
      * @param	classifications_file	The file to save the model
      *                                 classifications to. Null if the
      *                                 classificaitons not to be saved.
      * @throws	Exception               An informative exception is thrown if
      *                                 a request is made to save a file type
      *                                 whose corresponding field is empty.
      */
     public void saveXMLFiles( File taxonomy_file,
          File feature_key_file,
          File feature_vector_file,
          File classifications_file )
          throws Exception
     {
          if (taxonomy_file != null && taxonomy == null)
               throw new Exception("No taxonomy is stored to be saved.");
          if (feature_key_file != null && feature_definitions == null)
               throw new Exception("No feature definitinos are stored to be saved.");
          if (feature_vector_file != null && feature_vectors == null)
               throw new Exception("No feature vectors are stored to be saved.");
          if (classifications_file != null && model_classifications == null)
               throw new Exception("No model classifications are stored to be saved.");

          if (taxonomy_file != null)
               Taxonomy.saveTaxonomy(taxonomy, taxonomy_file, new String(""));
          if (feature_key_file != null)
               FeatureDefinition.saveFeatureDefinitions(feature_definitions, feature_key_file, new String(""));
          if (feature_vector_file != null)
               DataSet.saveDataSets(feature_vectors, feature_definitions, feature_vector_file, new String(""));
          if (classifications_file != null)
               SegmentedClassification.saveClassifications(model_classifications, classifications_file, new String(""));
     }


     /**
      * Save the contents of this DataBoard to a File.
      *
      * @param	databoard_file	The File to save to.
      * @param	to_save         The DataBoard to save.
      * @throws	Exception       if an error occurs during saving.
      */
     public static void saveDataBoard(DataBoard to_save, File databoard_file)
     throws Exception
     {
          FileOutputStream save_stream = new FileOutputStream(databoard_file);
          ObjectOutputStream object_stream = new ObjectOutputStream(save_stream);
          object_stream.writeObject(to_save);
          object_stream.flush();
          save_stream.close();
     }


     /**
      * Load the specified DataBoard serialized object file and return its
      * contents.
      *
      * @param	databoard_file	The File to load.
      * @return			The loaded DataBoard.
      * @throws	Exception	Throws an exception if an error occurs during
      *				loading.
      */
     public static DataBoard loadDataBoard(File databoard_file)
     throws Exception
     {
          FileInputStream load_stream = new FileInputStream(databoard_file);
          ObjectInputStream object_stream = new ObjectInputStream(load_stream);
          DataBoard board = (DataBoard) object_stream.readObject();
          load_stream.close();
          return board;
     }


     /**
      * Save the given Weka Instances as an arff file with the given path.
      *
      * @param instances      The weka instances to save.
      * @param file_path      The path of the arff file to save.
      * @throws Exception     Throws an exception if cannot save the given
      *                       instances.
      */
     public static void saveInstancesAsARFF(Instances instances, String file_path)
     throws Exception
     {
          String file_contents = instances.toString();
          try
          {
               File save_file = new File(file_path);
               FileOutputStream to = new FileOutputStream(save_file);
               DataOutputStream writer = new DataOutputStream(to);
               writer.writeBytes(file_contents);
          }
          catch (Exception e)
          {
               throw new Exception("Could not save to file " + file_path + ". " + e.getMessage());
          }
     }

     /**
      * Gets array of unique identifiers and hierarchy codes for each instance in the
      * array of DataSet objects of this DataBoard.
      *
      * @param num_overall  The number of top-level instances (instances that are not
      *                     subsections) contained in this DataBoard.
      * @param identifiers  A String array that will be filled with unique identifiers for
      *                     each instance of this DataBoard.
      * @param hierarchy    A String array that will be filled to distinguish top-level
      *                     instances from subsections and link subsections to their
      *                     corresponding top-level instance.
      */
     public void getInstanceIdentifiersAndHierarchy(int num_overall, String[] identifiers, String[] hierarchy)
     {
         /*Maybe the hierarchy array could be modified to avoid String comparisons. Maybe
          use 1, 1.1, 1.2, 1.3 instead of 1, 1_1, 1_2, 1_3.*/

         // Get identifier and hierarchy code for each feature vector
         int k = 0;
         for(int i = 0; i < num_overall; i++)
         {
             identifiers[k] = feature_vectors[i].identifier;
             hierarchy[k] = String.valueOf(i);
             if (feature_vectors[i].sub_sets != null)
             {
                 for (int j = 0; j < feature_vectors[i].sub_sets.length; j++)
                 {
                     k++;
                     identifiers[k] = j + "_" + feature_vectors[i].identifier;
                     hierarchy[k] = String.valueOf(i) + "_" + String.valueOf(j);
                 }
             }
             k++;
         }
     }



     /* PRIVATE METHODS *******************************************************/


     /**
      * Verifies the compatibility of the fields of this class. Throws an
      * Exception if there is a problem.
      *
      * @throws	Exception     An informative exception is thrown if the model
      *                       classifications contain multiple occurences of the
      *                       same data set or if classes are present in the
      *                       model classifications but not in the taxonomy.
      */
     private void validateFieldCompatibility()
     throws Exception
     {
          if (model_classifications != null)
               if (!SegmentedClassification.verifyUniquenessOfIdentifiers(model_classifications))
                    throw new Exception( "The provided model classifications are invalid\n" +
                         "because two instances have the same identifier." );

          if (model_classifications != null && taxonomy != null)
          {
              String[] classes = taxonomy.getClassesInClassificationsButNotTaxonomy(model_classifications);
               if (classes != null)
               {
                   String formatted = "";
                   for(int i = 0; i < classes.length; i++)
                   {
                       formatted = formatted + (i+1) + ": " + classes[i] + "\n";
                   }
                    throw new Exception( "The provided model classifications contain classes\n"+
                         "that are not in the taxonomy. The following classes were not found " +
                         "in the taxonomy file:\n" + formatted);

               }
          }
     }


     /**
      * Returns an array holding all leaf classes. No duplicates are present.
      * If a taxonomy is loaded, these are extracted from the taxonomy. If
      * not, then they are extracted from the model classifications. If
      * neither are present, then null is returned.
      */
     private String[] getClassNames()
     {
          if (taxonomy != null)
               return taxonomy.getLeafLabels();
          else if (model_classifications != null)
               return SegmentedClassification.getLeafClasses(model_classifications);
          else
               return null;
     }



     /**
      * Returns true if either the array of DataSet objects or SegmentedClassification
      * object of this DataBoard has sub-sections.
      *
      * @return     True if either the DataSet of SegmentedClassification of this
      *             DataBoard has sub-sections.
      */
     public boolean hasSections()
     {
         if(feature_vectors!=null)
         {
            for(int i = 0; i < feature_vectors.length; i++)
                 if (feature_vectors[i].sub_sets != null)
                 {
                     return true;
                 }
         }
         if(model_classifications!= null)
         {
            for(int i = 0; i < model_classifications.length; i++)
                if (model_classifications[i].sub_classifications != null)
                     return true;
         }
         return false;
     }

     /**
      * Returns the number of top-level instances contained in this DataBoard.
      *
      * @return the number of top-level instances contained in this DataBoard.
      */
     public int getNumOverall()
     {
         int num_overall = feature_vectors.length;
         return num_overall;
     }

     /**
      * Returns the total number of instances (top-level and subsections) contained in
      * this DataBoard.
      *
      * @return the total number of instances contained in this DataBoard.
      */
     public int getNumTotal()
     {
         int num_overall = getNumOverall();
         int num_total = num_overall;
         for(int i=0; i < num_overall; i++)
         {
            if (feature_vectors[i].sub_sets != null)
                num_total += feature_vectors[i].sub_sets.length;

         }
         return num_total;
     }

}