/*
 * FeatureDefinitionsTableModel.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import javax.swing.table.DefaultTableModel;
import ace.datatypes.FeatureDefinition;


/**
 * A TableModel used for displaying FeatureSetting objects.
 *
 * <p>Provides a method to fill the table row by row after deleting everything
 * in it. Also makes all cells editable.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class FeatureDefinitionsTableModel
     extends DefaultTableModel
{
     /* CONSTRUCTORS **********************************************************/


     /**
      * Same constructor as DefaultTableModel. Constructs a
      * FeatureDefinitionsTableModel with as many columns and rows as there are
      * elements in column_names and and row_count. Each column's name is taken
      * from the column_names array.
      *
      * @param	column_names  An array containing the names of the new columns.
      *                       If this is null then the model has no columns.
      * @param	row_count     The number of rows the table holds.
      */
     FeatureDefinitionsTableModel(Object[] column_names, int row_count)
     {
          super(column_names, row_count);
     }


     /* PUBLIC METHODS ********************************************************/


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
          return true;
     }


     /**
      * Deletes everything in the table and then fills it up one row at a time
      * based on the given FeatureDefinition array.
      *
      * @param	feature_definitions	Data to place in the table.
      */
     public void fillTable(FeatureDefinition[] feature_definitions)
     {
          // Remove the contents of the table
          clearTable();

          // Populate each row one by one
          if (feature_definitions != null)
          {
               for (int i = 0; i < feature_definitions.length; i++)
               {
               // Set up row
               Object[] row_contents = new Object[4];
               row_contents[0] = feature_definitions[i].name;
               row_contents[1] = new Integer(feature_definitions[i].dimensions);
               row_contents[2] = new Boolean(feature_definitions[i].is_sequential);
               row_contents[3] = feature_definitions[i].description;

               // Add the row
               addRow(row_contents);
               }
          }
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
          return getValueAt(0, column).getClass();
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
}