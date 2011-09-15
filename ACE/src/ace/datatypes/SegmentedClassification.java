/*
 * SegmentedClassification.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import java.io.*;
import ace.xmlparsers.XMLDocumentParser;
import java.util.LinkedList;
import weka.core.Instance;


/**
 * Objects of this class each hold classifications for an instance. These
 * classifications can be model classifications or the output of a classifier.
 *
 * <p>Each SegmentedClassification object may be divided into sections, each
 * with its own class. If only overall classifications are needed for an
 * instance, then there is no need for sub-sections, as each
 * SegmentedClassification can have its own class(es). Each instance may be
 * classified as belonging to one, multiple or no classes.
 *
 * <p>Meta-information can also be stored regarding each SegmentedClassification
 * object.
 *
 * <p>Static methods are provided to extract the labels (both overall and for
 * sub-sections) of DataSets based on SegmentedClassifications.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class SegmentedClassification
     implements Serializable
{
     /* FIELDS ****************************************************************/


     /**
      * The name of the dataset referred to by a top-level
      * SegmentedClassification. Should be set to null for
      * SegmentedClassifications that correspond to sub-sections. Should never
      * be null for top-level SegmentedClassifications.
      */
     public	String						identifier;


     /**
      * The class(es) that this top-level SegmentedClassification or section
      * belongs to. It is possible to have zero, one or no classifications for a
      * given instance. If no classifications are present, then this should be
      * null.
      */
     public	String[]					classifications;


     /**
      * Can store various pieces of meta-data regarding an instance. Entries
      * correspond to entries of the misc_info_key field. Should be set to null
      * for SegmentedClassifications that correspond to sub-sections. Set to
      * null if no meta-data is stored.
      */
     public String[]						misc_info_info;


     /**
      * Stores titles identifying the meta-data in the misc_info_info field.
      * Entries correspond to entries of the misc_info_key field. Should be
      * set to null for SegmentedClassifications that correspond to
      * sub-sections. Set to null if no meta-data is stored.
      */
     public String[]						misc_info_key;


     /**
      * Can be used internally by ACE to determine what a particular instance
      * is for (e.g. training, testing, resulting classification of an
      * unknown. Set to null if not used.
      */
     public String						role;


     /**
      * Classifications corresponding to sub-sections of an instance. Set to
      * null if there are no sub-sections.
      */
     public	SegmentedClassification[]                       sub_classifications;


     /**
      * Identifies the start of a sub-classification. Set to NaN if this
      * object is a top-level SegmentedClassification.
      */
     public	double						start;


     /**
      * Identifies the end of a sub-classification. Set to NaN if this
      * object is a top-level SegmentedClassification.
      */
     public	double						stop;


     /**
      * An identifier for use in serialization.
      */
     private static final long	serialVersionUID = 4L;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Generate an empty SegmentedClassification with the name "Undefined
      * Segmented Classification".
      */
     public SegmentedClassification()
     {
          identifier = null;
          classifications = null;
          misc_info_info = null;
          misc_info_key = null;
          role = null;
          sub_classifications = null;
          start = Double.NaN;
          stop = Double.NaN;
     }

     /**
      * Explicitly create a new Classification.
      *
      * @param identifier           Unique identifier for this instance.
      * @param start                Time at which this section begins.
      * @param stop                 Time at which this section ends.
      * @param misc_info            Miscellaneous information about this instance.
      * @param classifications      Array of classifications for this instance.
      * @param sub_classifications  The sub-classifications of this classification.
      * @param misc_info_key        Specifies what type of meta data is contained
      *                             in misc_info.
      */
     public SegmentedClassification(String identifier, double start, double stop,
             String[] classifications, String[] misc_info, String[] misc_info_key,
             SegmentedClassification[] sub_classifications)
     {
         this.identifier = identifier;
         this.classifications = classifications;
         this.misc_info_info = misc_info;
         this.misc_info_key = misc_info_key;
         this.role = null;
         this.sub_classifications = sub_classifications;
         this.start = start;
         this.stop = stop;
     }

     /**
      * Generates a SegmentedClassification from a Weka ARFF file. Identifier is
      * in the format #_feature values where # is the number of the instance and
      * feature values are separated by a commma just as they are represented in
      * the original Weka ARFF file.
      *
      * @param instance     The classified Weka Instance.
      * @param i            A counter that increments each time a new SegmentedClassification
      *                     object is created. Used when naming instances to guarantee
      *                     uniqueness of identifiers.
      */
     public SegmentedClassification(Instance instance, int i)
     {
         // Set the values of the fields of this SegmentedClassification ONLY if the given instances are classified
         if(!(instance.classIsMissing()))
         {
             identifier = i + "_" + instance.toString().substring(0, instance.toString().lastIndexOf(","));
             classifications = new String[1];
             classifications[0] = instance.classAttribute().value((int)instance.classValue());
             misc_info_info = null;
             misc_info_key = null;
             role = null;
             sub_classifications = null;
             start = Double.NaN;
             stop = Double.NaN;
         }
         else
         {
             identifier = null;
             classifications = null;
             misc_info_info = null;
             misc_info_key = null;
             role = null;
             sub_classifications = null;
             start = Double.NaN;
             stop = Double.NaN;
         }
     }

     /* PUBLIC METHODS ********************************************************/


     /**
      * Generate a formatted String detailing the contents of this
      * SegmentedClassification.
      *
      * @param	depth	How deep this SegmentedClassification is in a hierarchy
      *			of SegmentedClassification (i.e. through the
      *			sub_classifications field). This parameter should
      *			generally be 0 when called externally, as this method
      *			operates recursively.
      * @return		A formatted string describing this
      *                 SegmentedClassification.
      */
     public String getClassificationDescription(int depth)
     {
          // Prepare the indent
          String indent = new String("");
          for (int i = 0; i < depth; i++)
               indent += "   ";

          // Prepare the string identifying the dataset referred to by a top-level
          // SegmentedClassification as well as any related meta-data and the
          // role, if any
          String id_string = new String("");
          if (identifier != null)
          {
               id_string = "DATASET " + identifier + ": ";
               if (sub_classifications != null)
                    id_string += sub_classifications.length + " sub-sections ";
               else
                    id_string += "0 sub-sections ";
               if (classifications != null)
                    id_string += classifications.length + " overall classes\n";
               else
                    id_string += "0 overall classes\n";
               if (misc_info_info != null)
                    for (int i = 0; i < misc_info_info.length; i++)
                         id_string += misc_info_key[i] + ": " + misc_info_info[i] + "\n";
               if (role != null)
                    id_string += "Role: " + role + "\n";
               id_string += "\n";
          }

          // Add identification of start and stop if is a sub-section
          else
               id_string += indent + "START: " + start + "     STOP: " + stop + "\n\n";

          // Add classifications
          String classifications_string = new String("");
          if (classifications != null)
          {
               for (int i = 0; i < classifications.length; i++)
                    classifications_string += indent + indent + "Class: " + classifications[i] + "\n";
               classifications_string += "\n";
          }

          // Add sub-sections of this dataset
          String sub_section_string = new String("");
          if (sub_classifications != null)
               for (int set = 0; set < sub_classifications.length; set++)
                    sub_section_string += sub_classifications[set].getClassificationDescription(depth + 1);

          // Return the results
          return id_string + classifications_string + sub_section_string;
     }


     /**
      * Returns a formatted text description of the given
      * SegmentedClassification objects.
      *
      * @param	seg_classes	The classifications to describe.
      * @return			The formatted description.
      */
     public static String getClassificationDescriptions(SegmentedClassification[] seg_classes)
     {
          String combined_classificationss = new String();
          for (int i = 0; i < seg_classes.length; i++)
               combined_classificationss += seg_classes[i].getClassificationDescription(0);
          return combined_classificationss;
     }


     /**
      * Returns the number of given instances that belong to the given class.
      * Only top level class membership is considered (i.e. the classes that
      * sections belong to are ignored).
      *
      * @param	model_classifications	The model classifications to search
      *					for the number of instances belonging
      *					to the given class.
      * @param	class_of_interest	The class to search for.
      * @return				The number of instances in
      *                                 model_classifications that belong to
      *                                 class_of_interest.
      */
     public static int getNumberOverallInstancesBelongingToClass( SegmentedClassification[] model_classifications,
          String class_of_interest )
     {
          int count = 0;
          for (int inst = 0; inst < model_classifications.length; inst++)
          {
               String[] classes = model_classifications[inst].classifications;
               if (classes != null)
                    for (int clas = 0; clas < classes.length; clas++)
                         if (classes[clas].equals(class_of_interest))
                         {
                    clas = classes.length;
                    count++;
                         }
          }
          return count;
     }


     /**
      * Returns the number of given instances that have sections belonging
      * to the given class. Only section level class membership is considered
      * (i.e. the classes that instances belong to as a whole are ignored).
      *
      * @param	model_classifications	The model classifications to search
      *                                 for the number of instance sections
      *					belonging to the given class.
      * @param	class_of_interest	The class to search for.
      * @return				The number of sections of instances in
      *					model_classifications that  belong to
      *					class_of_interest.
      */
     public static int getNumberSectionsInInstancesBelongingToClass( SegmentedClassification[] model_classifications,
          String class_of_interest )
     {
          int count = 0;
          for (int inst = 0; inst < model_classifications.length; inst++)
          {
               SegmentedClassification[] sub_sections = model_classifications[inst].sub_classifications;
               if (sub_sections != null)
                    count += getNumberOverallInstancesBelongingToClass(sub_sections, class_of_interest);
          }
          return count;
     }


     /**
      * Returns an array containing the names of all classes that any instances
      * or sub-sections of the given SegmentedClassification belong to. All
      * duplicates removed, so each class is referred to once and only once.
      *
      * @param	seg_classes   The classifications whose classes are to be
      *                       returned.
      * @return               The classes, with duplicates removed.
      */
     public static String[] getLeafClasses(SegmentedClassification[] seg_classes)
     {
          // Get a list of all leaf classes in the given seg_classes
          LinkedList<String> classes_including_doubles = new LinkedList<String>();
          for (int set = 0; set < seg_classes.length; set++)
          {
               if (seg_classes[set].classifications != null)
                    for (int cla = 0; cla < seg_classes[set].classifications.length; cla++)
                         classes_including_doubles.add(seg_classes[set].classifications[cla]);

               if (seg_classes[set].sub_classifications != null)
                    for (int sub = 0; sub < seg_classes[set].sub_classifications.length; sub++)
                         for (int cla = 0; cla < seg_classes[set].sub_classifications[sub].classifications.length; cla++)
                              classes_including_doubles.add(seg_classes[set].sub_classifications[sub].classifications[cla]);
          }

          // Remove doubles of the same classes
          Object[] obj = classes_including_doubles.toArray();
          String[] unshortened = new String[obj.length];
          for (int i = 0; i < unshortened.length; i++)
               unshortened[i] = (String) obj[i];
          String[] shortened = mckay.utilities.staticlibraries.StringMethods.removeDoubles(unshortened);

          // Return the results
          return shortened;
     }


     /**
      * Verifies that none of the given set of SegmentedClassification refer to
      * data sets with the same identifiers.
      *
      * @param	seg_classes   The SegmentedClassifications to verify the
      *                       uniqueness of.
      * @return               True if the identifiers are all unique, false if
      *                       they are not.
      */
     public static boolean verifyUniquenessOfIdentifiers(SegmentedClassification[] seg_classes)
     {
          boolean unique = true;
          for (int i = 0; i < seg_classes.length - 1; i++)
               for (int j = i + 1; j < seg_classes.length; j++)
                    if ( seg_classes[i].identifier.equals(seg_classes[j].identifier) )
                    {
               unique = false;
               i = seg_classes.length;
               j = seg_classes.length;
                    }
          return unique;
     }


     /**
      * Returns a 2-D array describing the top-level label(s) of the given
      * DataSets, according to the given SegmentedClassifications. The first
      * indice of the returned array identifies the DataSet, and entries
      * correspond in number and order to the data_sets parameter. The second
      * indice indentifies the label(s) for the given DataSet. No order is
      * enforced on the labels.
      *
      * <p>The returned array has a first dimension size equal to the number of
      * data sets in the data_sets parameter. The first dimension will be set to
      * null if the corresponding DataSet does not have a corresponding entry in
      * the given SegmentedClassifications with the same identifier or if the
      * classifications in the corresponding SegmentedClassifications entry are
      * null.
      *
      * @param	data_sets               The DataSets to find top-level labels
      *                                 for.
      * @param	set_classifications	Model classifications.
      * @return				The top-level labels for the data_sets
      *                                 parameter.
      * @throws	Exception		An exception is thrown if the given
      *					SegmentedClassification contain multiple
      *					data sets with the same identifier.
      */
     public static String[][] getOverallLabelsOfDataSets( DataSet[] data_sets,
          SegmentedClassification[] set_classifications )
          throws Exception
     {
          if (!SegmentedClassification.verifyUniquenessOfIdentifiers(set_classifications))
               throw new Exception( "Given classifications contain multiple references\n" +
                    "to instances with the same identifier." );

          String[][] data_set_overall_labels = new String[data_sets.length][];
          for (int set = 0; set < data_sets.length; set++)
          {
               data_set_overall_labels[set] = null;
               for (int clas = 0; clas < set_classifications.length; clas++)
               {
                    if (data_sets[set].identifier.equals(set_classifications[clas].identifier))
                    {
                         data_set_overall_labels[set] = set_classifications[clas].classifications;
                         clas = set_classifications.length;
                    }
               }
          }

          return data_set_overall_labels;
     }


     /**
      * Returns a 3-D array describing the sub-section label(s) of the given
      * DataSets, according to the given SegmentedClassifications. The first
      * indice of the returned array identifies the DataSet, and entries
      * correspond in number and order to the data_sets parameter. The second
      * indice identifies the sub-section, and entries correspond in number and
      * order to the data_sets parameter. The third indice indentifies the
      * label(s) for the given sub-section. No order is enforced on the labels.
      *
      * <p>The returned array has a first dimension size equal to the number of
      * data sets in the data_sets parameter. The first dimension of the
      * returned array will be set to null if the corresponding DataSet does not
      * have a corresponding entry in the given SegmentedClassifications with
      * the same identifier. The second dimension of the returned array will be
      * null if there is no corresponding section is available in the
      * set_classifications for the given section. The third dimension of the
      * returned array will be null if no classifications are available in the
      * corresponding SegmentedClassification for the given section.
      *
      * @param	data_sets               The DataSets to find sub-section labels
      *                                 for.
      * @param	set_classifications	Model classifications.
      * @return				The labels of all sub-sections.
      * @throws	Exception		An exception is thrown if the given
      *					SegmentedClassification contain multiple
      *					data sets with the same identifier.
      */
     public static String[][][] getSubSectionLabelsOfDataSets( DataSet[] data_sets,
          SegmentedClassification[] set_classifications )
          throws Exception
     {
          if (!SegmentedClassification.verifyUniquenessOfIdentifiers(set_classifications))
               throw new Exception( "Given classifications contain multiple references\n" +
                    "to instances with the same identifier." );

          String[][][] labels = new String[data_sets.length][][];
          for (int set = 0; set < data_sets.length; set++)
          {
               labels[set] = null;
               for (int clas = 0; clas < set_classifications.length; clas++)
               {
                    // Find the DataSet and the SegmentedClassification that have
                    // the same identifier
                    if (data_sets[set].identifier.equals(set_classifications[clas].identifier))
                    {
                         // Refer to the sub-sections of both the DataSet and the
                         // SegmentedClassification
                         DataSet[] data_sub_set = data_sets[set].sub_sets;
                         SegmentedClassification[] classification_sections = set_classifications[clas].sub_classifications;

                         if (data_sub_set != null && classification_sections != null)
                         {
                              // Prepare the label holder for this DataSet
                              labels[set] = new String[data_sub_set.length][];

                              // Find the ranges of influence for each sub-section of
                              // the DataSet
                              double[] data_low_bound = new double[data_sub_set.length];
                              double[] data_high_bound = new double[data_sub_set.length];
                              for (int sub = 0; sub < data_sub_set.length; sub++)
                              {
                                   data_low_bound[sub] = data_sub_set[sub].start;
                                   data_high_bound[sub] = data_sub_set[sub].stop;
                              }

                              // Find the ranges of influence for each sub-section of
                              // the SegmentedClassification
                              double[] classifications_low_bound = new double[classification_sections.length];
                              double[] classifications_high_bound = new double[classification_sections.length];
                              for (int cl_sub = 0; cl_sub < classification_sections.length; cl_sub++)
                              {
                                   classifications_low_bound[cl_sub] = classification_sections[cl_sub].start;
                                   classifications_high_bound[cl_sub] = classification_sections[cl_sub].stop;
                              }

                              // Go through each sub-section of the DataSet and find
                              // the appropriate label(s) for it
                              for (int sub = 0; sub < data_sub_set.length; sub++)
                              {
                                   labels[set][sub] = null;
                                   double[] fraction_in = new double[classification_sections.length];
                                   double data_set_length = data_high_bound[sub] - data_low_bound[sub];
                                   for (int cl_sub = 0; cl_sub < classification_sections.length; cl_sub++)
                                   {
                                        // Case with no intersection
                                        if ( data_high_bound[sub] < classifications_low_bound[cl_sub] ||
                                             data_low_bound[sub] > classifications_high_bound[cl_sub] )
                                             fraction_in[cl_sub] = 0;

                                        // Case where data is fully within classification
                                        else if ( data_low_bound[sub] >= classifications_low_bound[cl_sub] &&
                                             data_high_bound[sub] <= classifications_high_bound[cl_sub] )
                                             fraction_in[cl_sub] = 1.0;

                                        // Case where classification is fully within data
                                        else if ( data_low_bound[sub] <= classifications_low_bound[cl_sub] &&
                                             data_high_bound[sub] >= classifications_high_bound[cl_sub] )
                                             fraction_in[cl_sub] = (classifications_high_bound[cl_sub] - classifications_low_bound[cl_sub]) / data_set_length;

                                        // Case where data is partially outside of the classification
                                        // (to the left)
                                        else if ( data_low_bound[sub] <= classifications_low_bound[cl_sub] &&
                                             data_high_bound[sub] >= classifications_low_bound[cl_sub] &&
                                             data_high_bound[sub] <= classifications_high_bound[cl_sub] )
                                             fraction_in[cl_sub] = (data_high_bound[sub] - classifications_low_bound[cl_sub]) / data_set_length;

                                        // Case where data is partially outside of the classification
                                        // (to the right)
                                        else if ( data_low_bound[sub] >= classifications_low_bound[cl_sub] &&
                                             data_high_bound[sub] >= classifications_high_bound[cl_sub] &&
                                             data_low_bound[sub] <= classifications_high_bound[cl_sub] )
                                             fraction_in[cl_sub] = (classifications_high_bound[cl_sub] - data_low_bound[sub]) / data_set_length;

                                        else fraction_in[cl_sub] = 0;
                                   }
                                   int best_cl_sub = mckay.utilities.staticlibraries.MathAndStatsMethods.getIndexOfLargest(fraction_in);
                                   labels[set][sub] = classification_sections[best_cl_sub].classifications;
                              }
                         }
                    }
               }
          }

          return labels;
     }




     /**
      * Parses a classifications_file XML file and returns an array of
      * SegmentedClassification objects holding its contents. An exception is
      * thrown if the file is invalid in some way.
      *
      * @param	model_classification_file_path	The path of the XML file to parse.
      * @return                                 Array of SegmentedClassification
      *                                         objects that were derived from the
      *                                         given ACE XML model classification
      *                                         file.
      * @throws	Exception                       Informative exceptions is thrown if an
      *                                         invalid file or file path is specified.
      */
     public static SegmentedClassification[] parseClassificationsFile(String model_classification_file_path)
     throws Exception
     {
          // Parse the file
          Object[] results = (Object[]) XMLDocumentParser.parseXMLDocument(model_classification_file_path, "classifications_file");
          SegmentedClassification[] parse_results = new SegmentedClassification[results.length];
          for (int i = 0; i < parse_results.length; i++)
               parse_results[i] = (SegmentedClassification) results[i];

          // Return the results
          return parse_results;
     }


     /**
      * Saves a classifications_file XML file with the contents specified
      * in the given SegmentedClassification array and the comments specified
      * in the comments parameter.
      *
      * @param	seg_classifications	The SegmentedClassifications to save.
      * @param	to_save_to		The file to save to.
      * @param	comments		Any comments to be saved inside the
      *					comments element of the XML file.
      * @throws	Exception		An informative exception is thrown if
      *					the file cannot be saved.
      */
     public static void saveClassifications( SegmentedClassification[] seg_classifications,
          File to_save_to,
          String comments )
          throws Exception
     {
          try
          {
               // Prepare stream writer
               FileOutputStream to = new FileOutputStream(to_save_to);
               DataOutputStream writer = new DataOutputStream(to);

               // Write the header and the first element of the XML file
               String pre_tree_part = new String
                    (
                    "<?xml version=\"1.0\"?>\n" +
                    "<!DOCTYPE classifications_file [\n" +
                    "   <!ELEMENT classifications_file (comments, data_set+)>\n" +
                    "   <!ELEMENT comments (#PCDATA)>\n" +
                    "   <!ELEMENT data_set (data_set_id, misc_info*, role?, classification)>\n" +
                    "   <!ELEMENT data_set_id (#PCDATA)>\n" +
                    "   <!ELEMENT misc_info (#PCDATA)>\n" +
                    "   <!ATTLIST misc_info info_type CDATA \"\">\n" +
                    "   <!ELEMENT role (#PCDATA)>\n" +
                    "   <!ELEMENT classification (section*, class*)>\n" +
                    "   <!ELEMENT section (start, stop, class+)>\n" +
                    "   <!ELEMENT class (#PCDATA)>\n" +
                    "   <!ELEMENT start (#PCDATA)>\n" +
                    "   <!ELEMENT stop (#PCDATA)>\n" +
                    "]>\n\n" +
                    "<classifications_file>\n\n" +
                    "   <comments>" + comments + "</comments>\n\n"
                    );
               writer.writeBytes(pre_tree_part);

               // Write the XML code to represent the contents of each DataSet
               for (int set = 0; set < seg_classifications.length; set++)
               {
                    writer.writeBytes("   <data_set>\n");
                    writer.writeBytes("      <data_set_id>" + seg_classifications[set].identifier + "</data_set_id>\n");
                    if (seg_classifications[set].misc_info_key != null)
                         for (int met = 0; met < seg_classifications[set].misc_info_key.length; met++)
                              writer.writeBytes( "      <misc_info info_type=\"" + seg_classifications[set].misc_info_key[met] +
                                   "\">" + seg_classifications[set].misc_info_info[met] + "</misc_info>\n");
                    if (seg_classifications[set].role != null)
                         writer.writeBytes("      <role>" + seg_classifications[set].role + "</role>\n");
                    writer.writeBytes("      <classification>\n");
                    if (seg_classifications[set].sub_classifications != null)
                    {
                         SegmentedClassification[] sub_secs = seg_classifications[set].sub_classifications;
                         for (int sub = 0; sub < sub_secs.length; sub++)
                         {
                              writer.writeBytes("         <section>\n");
                              writer.writeBytes("            <start>" + sub_secs[sub].start + "</start>\n");
                              writer.writeBytes("            <stop>" + sub_secs[sub].stop + "</stop>\n");
                              for (int cla = 0; cla < sub_secs[sub].classifications.length; cla++)
                                   writer.writeBytes("            <class>" + sub_secs[sub].classifications[cla] + "</class>\n");
                              writer.writeBytes("         </section>\n");
                         }
                    }
                    if (seg_classifications[set].classifications != null)
                         for (int cla = 0; cla < seg_classifications[set].classifications.length; cla++)
                              writer.writeBytes("         <class>" + seg_classifications[set].classifications[cla] + "</class>\n");
                    writer.writeBytes("      </classification>\n");
                    writer.writeBytes("   </data_set>\n\n");
               }
               writer.writeBytes("</classifications_file>");

               // Close the output stream
               writer.close();
          }
          catch (Exception e)
          {
               throw new Exception("Unable to write file " + to_save_to.getName() + ".");
          }
     }


     /**
      * Merges any overlapping adjacent sub-sections of the given
      * SegmentedClassification if they belong to the same class(es) and the end
      * of the first matches the start of the second. No change is made if there
      * are no such sections or if there are no sub-sections at all.
      *
      * <p><b>IMPORTANT:</B> It is assumed that sections whose start and stop
      * sections overlap have adjacent indices, as these are all that are
      * checked. It is also assumed that the start of a section is before the
      * start of the next section in memory and that the stop of a section is
      * before the stop of the next section in memory.
      *
      * @param	verbose_classifications	The classifications whose sections are
      *                                 to be merged if appropriate.
      */
     public static void mergeAdjacentSections(SegmentedClassification verbose_classifications)
     {
          if (verbose_classifications.sub_classifications == null)
               return;
          if (verbose_classifications.sub_classifications.length == 1)
               return;

          LinkedList<SegmentedClassification> new_sections = new LinkedList<SegmentedClassification>();
          SegmentedClassification[] old_sections = verbose_classifications.sub_classifications;
          SegmentedClassification this_section = old_sections[0];
          for (int sec = 0; sec < old_sections.length - 1; sec++)
          {
               // Check if this section and the next have the same categories and if the end of
               // this section corresponds to the start of the next
               if ( mckay.utilities.staticlibraries.StringMethods.doStringArraysMatch(this_section.classifications, old_sections[sec + 1].classifications) &&
                    this_section.stop >= old_sections[sec + 1].start )
               {
                    this_section.stop = old_sections[sec + 1].stop;

                    if (sec == old_sections.length - 2)
                         new_sections.add(this_section);
               }
               else
               {
                    new_sections.add(this_section);
                    this_section = old_sections[sec + 1];
                    if (sec == old_sections.length - 2)
                         new_sections.add(this_section);
               }
          }
          verbose_classifications.sub_classifications = new_sections.toArray(new SegmentedClassification[1]);
     }

     /**
      * Finds the timeframes in which the sub-sets overlap with the sub-classifications.
      * Used when displaying the DataSet objects and SegmentedClassification objects
      * in the Instances Panel in the GUI.
      * Assumes that sub_set and sub_classes are sections of the same overall instance.
      *
      *
      * @param sub_classes  The classifications of the subsections of this instance.
      * @param sub_set      The subsections of the overall instance.
      * @param times        Will be filled with the exact start and stop time that
      *                     sub classifications overlap with the subsections. These
      *                     start and stop times will be included in a formatted
      *                     String that will be displayed in the "Classes" column.
      * @return             A 3D array containing the classifications for each subsection.
      *                     1st index indicated the subsection, 2nd index indicates
      *                     the subclassifications that overlap with the subsections.
      *                     These subclassifications are arrays themselves, hence
      *                     the 3rd index.
      */
     public static String[][][] getMergedSectionalClassifications(SegmentedClassification[] sub_classes, DataSet[] sub_set, double[][][] times)
     {
         // The array of class names to return
         String[][][] classes = new String[sub_set.length][sub_classes.length][];

         // Go through each sub set and compare its start and stop times to those of each sub classification
         for (int j = 0; j < sub_set.length; j++)
         {
             int class_index = 0;
             for(int i = 0; i < sub_classes.length; i++)
             {
                 // Ensure that given classifications are of sections
                 if (sub_classes[i].start != Double.NaN && sub_classes[i].stop != Double.NaN)
                 {
                    // Compare start and stop times, loop for overlap
                     if(sub_classes[i].start <= sub_set[j].start && sub_classes[i].stop >= sub_set[j].start
                             || sub_classes[i].start >= sub_set[j].start && sub_classes[i].start <= sub_set[j].stop)
                     {
                         // Sections overlap, add class name to array
                         classes[j][class_index] = sub_classes[i].classifications;

                         // Find exact times of overlap
                         if(sub_classes[i].start >= sub_set[j].start)
                         {
                             times[j][class_index][0] = sub_classes[i].start;
                             if(sub_classes[i].stop <= sub_set[j].stop)
                                 times[j][class_index][1] = sub_classes[i].stop;
                             else if (sub_classes[i].stop > sub_set[j].stop)
                                times[j][class_index][1] = sub_set[j].stop;
                         }
                         else
                         {
                             times[j][class_index][0] = sub_set[j].start;
                             if(sub_classes[i].stop <= sub_set[j].stop)
                                 times[j][class_index][1] = sub_classes[i].stop;
                             else if (sub_classes[i].stop > sub_set[j].stop)
                                times[j][class_index][1] = sub_set[j].stop;
                         }

                         // increment counter
                         class_index++;
                     }
                 }
             }
         }

       return classes;
     }

     /**
      * Compares the given DataSet object to each of the SegmentedClassification
      * objects in order to find a SegmentedClassification object with the same
      * identifier as the given DataSet object. This is used in the GUI when displaying
      * DataSet and SegmentedClassification objects in the same Instances Panel.
      *
      * @param instance     The overall section to match with a classification.
      * @param classes      An array of all possible classifications.
      *
      * @return     The SegmentedClassification in the given array that corresponds to
      *             the given DataSet. Returns null if none of the classifications match.
      */
     public static SegmentedClassification findMatchingClassification(DataSet instance, SegmentedClassification[] classes)
     {
         for(int i = 0; i < classes.length; i++)
             if(instance.identifier.equals(classes[i].identifier))
                 return classes[i];
         return null;
     }
}