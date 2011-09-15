/*
 * InstancesTableModel.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import javax.swing.table.*;
import ace.datatypes.*;
import java.text.*;


/**
 * A TableModel used for displaying instances derived from DataSet and
 * SegmentedClassification objects.
 *
 * <p>Provides a method to fill the table row by row after deleting everything
 * in it. Also makes all cells editable.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class InstancesTableModel
     extends DefaultTableModel
{
     /* FIELDS ****************************************************************/


     /**
      * Whether or not to display additional columns of meta-data for instances.
      */
     private	boolean	display_meta_data;


     /**
      * Whether or not to display additional columns of feature values for
      * instances.
      */
     private	boolean	display_feature_values;

     /**
      * Whether or not this table includes instances that have sub-sections.
      */
     private    boolean has_sections;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Same constructor as DefaultTableModel. Constructs a InstancesTableModel
      * with as many columns and rows as there are elements in column_names and
      * and row_count. Each column's name is taken from the column_names array.
      */
     InstancesTableModel()
     {
          super();
          display_meta_data = false;
          display_feature_values = false;
          has_sections = false;
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Deletes everything in the table and then fills it up one row at a time
      * based on the given DataBoard and booleans. This method is called whenever
      * something happens in the GUI that requires the table to be updated (e.g.
      * loading a new file, checking a checkbox)
      *
      * @param	data_board		Data to place in the table.
      * @param	identifiers_to_expand	The names of the top-level instances
      *                                 that should be expanded to show
      *                                 sub-sections. If this is null, then none
      *                                 are expanded. Any instances that are not
      *                                 mentioned are not expanded. The names of
      *                                 instances correspond to the identifier
      *                                 field of SegmentedClassification or
      *                                 DataSet objects.
      * @param	display_meta_data	Whether or not to include on the table
      *                                 additional meta-data contained in the
      *                                 SegmentedClassification	in the
      *                                 DataBoard.
      * @param	display_feature_values	Whether or not to include on the table
      *                                 actual feature values stored in the
      *                                 DataBoard.
      * @throws Exception
      */
     public void fillTable( DataBoard data_board,
          String[] identifiers_to_expand,
          boolean display_meta_data,
          boolean display_feature_values )
          throws Exception
     {
          // Remove the contents of the table
          clearTable();

          // Update whether or not feature valuses and additional meta-data
          // are to be displayed
          this.display_meta_data = display_meta_data;
          this.display_feature_values = display_feature_values;

          // Add columns
          Object[] column_names = getColumnNames(data_board);
          for (int i = 0; i < column_names.length; i++)
               addColumn(column_names[i]);

          // Find the number of meta-data columns in the table
          int numb_meta_data_cols = 0;
          if (data_board.getInstanceMetaDataFields() != null)
               numb_meta_data_cols = data_board.getInstanceMetaDataFields().length;

          // If feature DataSets are available, then add them to the table, along with
          // SegmentedClassification, if they are present
          if (data_board.feature_vectors != null )
          {
              // Throw exception if feature definitions are not loaded
//              if (data_board.feature_definitions == null)
//                  generateFeatureDefinitions()
//                  //throw new Exception ("\nFeature Definitions must be loaded in order to load Feature Vectors.");

               // Find the model classifications of data sets overall and of their sections
               String[][] model_classifications_overall = null;
               String[][][] model_classifications_sections = null;
               if (data_board.model_classifications != null)
               {
                    model_classifications_overall =
                         SegmentedClassification.getOverallLabelsOfDataSets( data_board.feature_vectors,
                         data_board.model_classifications );
                    if (identifiers_to_expand != null)
                         model_classifications_sections =
                              SegmentedClassification.getSubSectionLabelsOfDataSets( data_board.feature_vectors,
                              data_board.model_classifications );
               }

               // Add the data for the feature vectors one row at a time
               for (int i = 0; i < data_board.feature_vectors.length; i++)
               {
                    // Find the class for this top-level instance
                    String[] overall_classes = null;
                    if (model_classifications_overall != null)
                         if (model_classifications_overall[i] != null)
                              overall_classes = model_classifications_overall[i];
                         else
                         {
                             overall_classes = new String[1];
                             overall_classes[0] = "?";
                         }

                    // Find the top-level meta-data
                    String[] overall_meta_data = null;
                    String[] overall_meta_data_key = null;
                    if (display_meta_data)
                    {
                         SegmentedClassification matching_model =
                              data_board.getMatchingModelClassification(data_board.feature_vectors[i]);
                         if (matching_model != null)
                         {
                              overall_meta_data = matching_model.misc_info_info;
                              overall_meta_data_key = matching_model.misc_info_key;
                         }
                    }

                    // Find the top-level overall feature values
                    String[][] top_feat_vals = null;
                    if (display_feature_values)
                         top_feat_vals = data_board.feature_vectors[i].getFeatureValuesOfTopLevel(data_board.feature_definitions);
                    // Check if this instance should be expanded into sub-sections
                    boolean expand_this_instance = false;
                    if (data_board.feature_vectors[i].sub_sets != null)
                    {
                         if (identifiers_to_expand != null)
                              if (mckay.utilities.staticlibraries.StringMethods.isStringInArray(data_board.feature_vectors[i].identifier, identifiers_to_expand))
                                   expand_this_instance = true;

                    }

                    // Add the row for the top-level instance
                    addRowToTable( false,
                         expand_this_instance,
                         data_board.hasSections(),
                         data_board.feature_vectors[i].identifier,
                         null,
                         null,
                         overall_classes,
                         overall_meta_data,
                         overall_meta_data_key,
                         numb_meta_data_cols,
                         top_feat_vals );

                    // Create rows for sections as well, if appropriate
                   if (expand_this_instance)
                   {
                       // Prepare array to hold classifications
                       String[][] array_to_pass = new String[data_board.feature_vectors[i].sub_sets.length][1];

                       //for each subset of this instance, add a row to table
                       for (int j = 0; j < data_board.feature_vectors[i].sub_sets.length; j++)
                       {
                           // Find matching classification
                           if (data_board.model_classifications != null)
                           {
                               SegmentedClassification matching = SegmentedClassification.findMatchingClassification(data_board.feature_vectors[i], data_board.model_classifications);

                               // Get classifications of sections
                               if (matching != null)
                               {
                                   String[] section_classes = getMergedSectionalClassificationsStrings(matching.sub_classifications,
                                           data_board.feature_vectors[i].sub_sets);
                                   for (int k = 0; k < section_classes.length; k++)
                                       array_to_pass[k][0] = section_classes[k];
                               }
                           }
                           else
                           {
                               for (int k = 0; k < array_to_pass.length; k++)
                                   array_to_pass[k][0] = null;
                           }

                           // Find the top-level meta-data
                           String[] section_meta_data = null;
                           String[] section_meta_data_key = null;
                           if (display_meta_data)
                           {
                               SegmentedClassification matching_model_section =
                                       data_board.getMatchingModelClassification(data_board.feature_vectors[i].sub_sets[j]);
                               if (matching_model_section != null)
                               {
                                   section_meta_data = matching_model_section.misc_info_info;
                                   section_meta_data_key = matching_model_section.misc_info_key;
                               }
                           }

                           // Find the sectional feature values
                           String[][] section_feat_vals = null;
                           if (display_feature_values)
                               section_feat_vals = data_board.feature_vectors[i].sub_sets[j].getFeatureValuesOfTopLevel(data_board.feature_definitions);
                           String start = String.valueOf(data_board.feature_vectors[i].sub_sets[j].start);
                           String stop = String.valueOf(data_board.feature_vectors[i].sub_sets[j].stop);
                           boolean has = data_board.hasSections();
                           addRowToTable(true,
                                   expand_this_instance,
                                   has,
                                   ("     Section " + (j + 1)),
                                   start,
                                   stop,
                                   array_to_pass[j],
                                   section_meta_data,
                                   section_meta_data_key,
                                   numb_meta_data_cols,
                                   section_feat_vals);
                       }
                   }
               }
          }

          // If SegmentedClassification model classifications are available, and
          // feature DataSets are not, then add the SegmentedClassification to the table
          else if (data_board.model_classifications != null)
          {
               for (int i = 0; i < data_board.model_classifications.length; i++)
               {
                    // Check if this instance should be expanded into sub-sections
                    boolean expand_this_instance = false;
                    if (data_board.model_classifications[i].sub_classifications != null)
                    {
                         if (identifiers_to_expand != null)
                              if (mckay.utilities.staticlibraries.StringMethods.isStringInArray(data_board.model_classifications[i].identifier, identifiers_to_expand))
                                   expand_this_instance = true;
                    }

                    // Add the row for the top-level instance in model classifications
                    //check to see if section, de hard code
                    addRowToTable( false,
                         expand_this_instance,
                         data_board.hasSections(),
                         data_board.model_classifications[i].identifier,
                         String.valueOf(data_board.model_classifications[i].start),
                         String.valueOf(data_board.model_classifications[i].stop),
                         data_board.model_classifications[i].classifications,
                         data_board.model_classifications[i].misc_info_info,
                         data_board.model_classifications[i].misc_info_key,
                         numb_meta_data_cols,
                         null );

                    // Create rows for sections as well, if appropriate
                    if (expand_this_instance)
                    {
                        //for each subset of this instance, add a row to table
                        for(int j  = 0; j < data_board.model_classifications[i].sub_classifications.length; j++)
                        {
                           addRowToTable(true,
                                   expand_this_instance,
                                   data_board.hasSections(),
                                   "     Section " + (j+1),
                                   String.valueOf(data_board.model_classifications[i].sub_classifications[j].start),
                                   String.valueOf(data_board.model_classifications[i].sub_classifications[j].stop),
                                   data_board.model_classifications[i].sub_classifications[j].classifications,
                                   data_board.model_classifications[i].sub_classifications[j].misc_info_info,
                                   data_board.model_classifications[i].sub_classifications[j].misc_info_key,
                                   numb_meta_data_cols,
                                   null);
                        }
                    }
               }
          }
     }


     /**
      * Returns true for all cells, thereby indicating that all cells are
      * editable.
      *
      * @param	row	The row whose value is to be queried.
      * @param	column	The column whose value is to be queried.
      * @return		Whether or not the given cell is editable
      */
     public boolean isCellEditable(int row, int column)
     {
// change this for some columns?
          return true;
     }


     /**
      * Returns the type of class used for each column.
      * Necessary in order for text boxes to be properly displayed.
      *
      * @param	column	Column to check.
      * @return		The class contained in the given column.
      */
     public Class getColumnClass(int column)
     {
         Object value=this.getValueAt(0,column);
         return (value==null?Object.class:value.getClass());

     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Removes all contents of the table.
      */
     private void clearTable()
     {
          while (getRowCount() != 0)
               removeRow(0);
     }


     /**
      * Determines the headings to use for each of the columns in the table.
      * These headings always include Show Sections, Identifier, Start, Stop and
      * Classes. Meta-data headings will also be included if display_meta_data
      * is true and meta-data is available in the given data_board. Feature
      * names will be included if display_feature_values is true and there are
      * feature names available.
      *
      * @return     An array of objects, each of which is a String containing
      *             the name of a column.
      */
     private Object[] getColumnNames(DataBoard data_board)
     {
          // A running count of the number of columns to be in the table
          int number_columns = 0;

          // The column headings that will be in all tables
          String[] basic_headings = {new String("Identifier")};
          number_columns = basic_headings.length;

          /* If data has subsections, table will have "Start", "Stop", and "Show
          Sections" columns*/
          if (data_board.hasSections())
          {
               number_columns += 3;
              has_sections = true;
          }
          // Display classifications if present, need extra column
          if(data_board.getModelClassifications() != null)
          {
             number_columns++;
          }

          // Optional column names relating to meta-data stored on instances
          // New column for each type of meta-data
          String[] meta_data_headings = null;
          if (display_meta_data)
          {
               meta_data_headings = data_board.getInstanceMetaDataFields();
               if (meta_data_headings != null)
                    number_columns += meta_data_headings.length;
          }

          // Optional column names relating to feature values
          // New column for each feature
          String[] feature_names = null;
          if (display_feature_values)
          {
               feature_names = data_board.getFeatureNames();
               if (feature_names != null)
                    number_columns += feature_names.length;
          }

          // Set up the actual column headings
          Object[] column_headings = new Object[number_columns];
          int current_column = 0;
          if (has_sections)
          {
              column_headings[current_column] = "Show Sections";
              current_column++;
          }

          // Basic Headings
          for (int i = 0; i < basic_headings.length; i++)
               column_headings[i + current_column] = new String(basic_headings[i]);
          current_column += basic_headings.length;

          // Start/Stop
          if (has_sections)
          {
              column_headings[current_column] = "Start";
              column_headings[current_column + 1] = "Stop";
              current_column += 2;
          }

          // Meta Data
          if (meta_data_headings != null)
          {
               for (int i = 0; i < meta_data_headings.length; i++)
                    column_headings[i + current_column] = new String(meta_data_headings[i]);
               current_column += meta_data_headings.length;
          }

          // Classifications
          if (data_board.getModelClassifications() != null)
          {
              column_headings[current_column + 0] = "Classes";
              current_column++;
          }

          // Features
          if (feature_names != null)
          {
               for (int i = 0; i < feature_names.length; i++)
                    column_headings[i + current_column] = new String(feature_names[i]);
               current_column += feature_names.length;
          }

          // Return the column headings
          return column_headings;
     }


     /**
      * Adds the given data as a row at the end of the table. A value of ? is
      * inserted for unknown classes, meta-data and feature values. A value of
      * NA is inserted in the Start and Stop columns of overall instances.
      *
      * @param	is_section		True if is a sub-section of an instance.
      * @param	should_be_expanded	True if is not a sub-section and if it
      *                                 should be expanded to show sub-sections.
      *                                 Ignored if is a	sub-section.
      * @param	identifier		The identifier of the instance. Is
      *                                 ignored if true	is passed to the
      *                                 is_section parameter.
      * @param	start			The start identifier of a sub-section.
      *                                 Is ignored if is a top level section.
      * @param	stop			The end identifier of a sub-section. Is
      *                                 ignored if is a top level section.
      * @param	classes			The classes that an instance belongs to.
      *                                 May be null if no classes are available.
      * @param	meta_data		The meta-data corresponding to a top
      *                                 level instance.	Is ignored if is a
      *                                 sub-section. Must be in the same order
      *                                 as the meta-data columns of the table
      *                                 model.	May be null if no meta-data is
      *                                 available or none is to be displayed.
      * @param	meta_data_key		The column headings corresponding to
      *                                 each of the entries passed to the
      *                                 meta_data parameter. Is ignored if null
      *                                 is passed to the meta_data parameter.
      * @param	numb_meta_data_cols     The number of columns in the table that
      *                                 are for	displaying meta-data. Is ignored
      *                                 if null is passed to the meta_data
      *                                 parameter.
      * @param	feature_values		The feature values of the instance. Must
      *                                 be in the same order as the feature
      *                                 columns of the table model. May be null
      *                                 if no feature values are available or
      *                                 none are to be displayed. The first
      *                                 indice indicates the feature and the
      *                                 second indicates the dimension (for
      *                                 multi-dimensional features).
      */
     private void addRowToTable( boolean is_section,
          boolean should_be_expanded,
          boolean subsections_exist,
          String identifier,
          String start,
          String stop,
          String[] classes,
          String[] meta_data,
          String[] meta_data_key,
          int numb_meta_data_cols,
          String[][] feature_values )
     {
          // Prepare the array that will hold the contents of the row
          Object[] row_contents = new Object[getColumnCount()];
          // Index to keep track of column index
          int current_column = 1;
          // Add the expand section checkbox
          // Note: custom cell renderer is used for this column
          if (subsections_exist)
          {
              if(is_section)
                 row_contents[0] = null;
              else if (should_be_expanded)
                  row_contents[0] = new Boolean(true);
              else
                  row_contents[0] = new Boolean(false);
              row_contents[1] = identifier;
              current_column = 2;
          }
          else
              row_contents[0] = identifier;

          // Add the start and stop markers
          if (subsections_exist)
          {
              if (is_section)
              {
                row_contents[current_column] = start;
                row_contents[current_column+1] = stop;
              }
              else
              {
                row_contents[current_column] = new String("NA");
                row_contents[current_column+1] = new String("NA");
              }
                current_column+=2;
          }

          // Display the meta-data
          if (display_meta_data)
          {
               int last_meta_col = current_column + numb_meta_data_cols - 1;
               if (meta_data != null)
                    for (int i = 0; i < meta_data.length; i++)
                         for (int j = current_column; j <= last_meta_col; j++)
                              if ( meta_data_key[i].equals(getColumnName(j)) )
                              {
                    row_contents[j] = meta_data[i];
                    j = current_column + numb_meta_data_cols;
                              }
               while (current_column <= last_meta_col)
               {
                    if (row_contents[current_column] == null)
                         row_contents[current_column] = "?";
                    current_column++;
               }
          }

          // Add the classes
          if(classes != null && classes.length != 0 )
          {
               row_contents[current_column] = new String(mckay.utilities.staticlibraries.StringMethods.concatenateArrayOfStrings(classes));
               current_column++;
          }

          // Display the feature vectors
          if (display_feature_values)
          {
               if (feature_values != null)
                    for (int i = 0; i < feature_values.length; i++)
                    {
// deal with multi-dimensional features, not just single
                    row_contents[current_column] = feature_values[i][0];
                    current_column++;
                    }
               else
                    while (current_column < row_contents.length)
                    {
                    row_contents[current_column] = "?";
                    current_column++;
                    }
          }


/*System.out.println(row_contents.length);
for (int i = 0; i < row_contents.length; i++)
{
System.out.println(row_contents[i]);
}
System.out.println("\n--\n");*/

          // Add the row
          addRow(row_contents);
     }

     /**
      * Given sub-feature vectors and sub-classifications for the same overall instance,
      * this method prepares a String for each sub-section detailing which sub-classifications
      * are present in the time frame of the subsection. The formatted Strings will
      * specify the time frame within the subsection that the sub-classification
      * applies.
      *
      * @param sub_classes  The sub-classifications for the overall instance.
      * @param sub_set      The sub-feature values/sub-sections for the same overall
      *                     instance.
      * @return             A String for each sub-set detailing the classes that
      *                     are present within it.
      */
     private String[] getMergedSectionalClassificationsStrings(SegmentedClassification[] sub_classes, DataSet[] sub_set)
     {
         // Limit the values shown in the table to 2 decimal placs
         DecimalFormat df = new DecimalFormat("####0.##");

         int num_sections = sub_set.length;
         double[][][] times = new double[num_sections][sub_classes.length][2];
         String[] formatted = new String[num_sections];

         /* Get classifications for each sub-set
          1st index is sub-sets, 2nd index is arrays of classes that are present
          in corresponding sub-set*/
         String[][][] classes = SegmentedClassification.getMergedSectionalClassifications(sub_classes, sub_set, times);

         /* For each sub section, concatenate the classes to which the sub section belongs
            Format this concatenation to display the time slot during which the
            subsection belongs to each class*/
         for(int i = 0; i < num_sections; i++)
         {
             StringBuffer this_section = new StringBuffer();
             for(int j = 0; j < classes[i].length; j++)
             {
                 if(classes[i][j] != null)
                 {
                    this_section.append(mckay.utilities.staticlibraries.StringMethods.concatenateArrayOfStrings(classes[i][j]));
                    this_section.append("("+ df.format(times[i][j][0]) + "-" + df.format(times[i][j][1]) + ")");
                    if (classes[i][j+1] != null)
                        this_section.append(", ");
                 }
             }
             String classification = this_section.toString();
             if (classification != null)
                 formatted[i] = classification;
             else
                 formatted[i] = "?";
         }
         return formatted;
     }
}