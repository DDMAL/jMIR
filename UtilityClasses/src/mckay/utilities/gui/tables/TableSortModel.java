/*
 * TableSortModel.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.tables;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JTable;
import javax.swing.table.*;
import mckay.utilities.general.SmartComparator;


/**
 * This class serves as an intermediate TableModel that can be used to implement
 * sorting of JTables with DefaultTableModel TableModels.
 *
 * <p>The setColumnWidths method also allows JTables to have the width of some
 * of their columns automatically set in order to fit the contents of the
 * columns.
 *
 * @author Cory McKay
 */
public class TableSortModel
     implements TableModel, TableModelListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The true TableModel of the JTable.
      */
     private	DefaultTableModel	real_model;
     
     /**
      * Intermediate indexes used to keep track of sorting
      */
     private	int[]			indexes;
     
     /**
      * For use in making comparisons to determine order during sorting
      */
     private	SmartComparator		comparator;
     
     
     /* CONSTRUCTOR ***********************************************************/
     
     
     /**
      * Attaches a MouseListener to the column headings of the given JTable so
      * that it will be sorted based on the contents of that column whenever the
      * column heading is pressed. This object is attached to the table as an
      * intermediate TableModel between it and its original DefaultTableModel.
      *
      * @param	table	The table that this object is to serve as an
      *			intermediateTableModel for. This may not have a null
      *                  TableModel when it is passed here, and its TableModel
      *                  must be a DefaultTableModel.
      * @throws	IllegalArgumentException	Throws an informative exception
      *						if a null argument is passed.
      */
     public TableSortModel(JTable table)
     throws IllegalArgumentException
     {
          if (table.getModel() == null)
               throw new IllegalArgumentException("A null TableModel was provided to the table sorter.");
          if ( !(table.getModel() instanceof DefaultTableModel) )
               throw new IllegalArgumentException("A non-DefaultTableModel TableModel was provided to the table sorter.");
          
          comparator = new SmartComparator();
          
          DefaultTableModel model = (DefaultTableModel) table.getModel();
          real_model = model;
          real_model.addTableModelListener(this);
          table.setModel(this);
          attachColumnClickListenerForSorting(table);
          allocate();
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * This method currently does nothing. Any code added to this method will
      * automatically be executed whenever the table is changed.
      *
      * @param event     Any TableModelEvent that could trigger some action.
      */
     public void tableChanged(TableModelEvent event)
     {
          
     }
     
     
     /**
      * Resets the column widths of the given JTable so that each of the given
      * columns has enough space to fit its header and all of its contents
      * in the column, and no more. No column will be sized larger than the
      * space needed to fit roughly the number of characters specified in the
      * max argument, however.
      *
      * @param	table		The table whose column widths are to be changed.
      * @param	columns_to_set	The columns to be resized. These must all be
      *				in the given table.
      * @param	max		The rough maximum width of each of the given
      *				columns, in characters.
      */
     public static void setColumnWidths( JTable table,
          TableColumn[] columns_to_set,
          int max )
     {
          // Find the maximum allowable column width
          String dummy = "";
          for (int i = 0; i < max; i++)
               dummy += "w";
          TableCellRenderer renderer =  table.getTableHeader().getDefaultRenderer();
          Component component = renderer.getTableCellRendererComponent(table, dummy, false, false, 0, 0);
          max = component.getPreferredSize().width;
          
          // Resize each of the columns
          for (int i = 0; i < columns_to_set.length; i++)
          {
               int width = getPreferredWidthForColumn(table, columns_to_set[i]);
               if (width > max)
                    width = max;
               columns_to_set[i].setMinWidth(width);
               columns_to_set[i].setMaxWidth(width);
          }
          table.doLayout();
     }
     
     
     /**
      * Returns the row index of the true table model that corresponds to the
      * row index in the sorted table model.
      *
      * @param	row	The row index of the TableSortModel to query.
      * @return		The corresponding row index of the true TableModel.
      */
     public int getTrueRowIndex(int row)
     {
          return indexes[row];
     }
     
     
     /**
      * Adds a row to the end of the TableModel.
      *
      * @param	row_data      The row to add to the TableModel.
      */
     public void addRow(Object[] row_data)
     {
          int[] temp = new int[indexes.length + 1];
          for (int i = 0; i < indexes.length; i++)
               temp[i] = indexes[i];
          temp[indexes.length] = indexes.length;
          indexes = temp;
          real_model.addRow(row_data);
     }
     
     
     /**
      * Removes the rows with the given indexes from the TableModel.
      *
      * @param	row_indexes	The indexes of the rows to delete.
      */
     public void removeRows(int[] row_indexes)
     {
          // Find the indices of the rows in the true TableModel
          int[] true_indexes = new int[row_indexes.length];
          for (int i = 0; i < row_indexes.length; i++)
               true_indexes[i] = indexes[row_indexes[i]];
          
          // Sort the indices to delete
          true_indexes = mckay.utilities.staticlibraries.SortingMethods.sortIntArray(true_indexes);
          
          // Compensate for how indices will change once the rows
          // are deleted one by one
          int decrement = 0;
          for (int i = 0; i < true_indexes.length; i++)
          {
               true_indexes[i] = true_indexes[i] - decrement;
               decrement++;
          }
          
          // Delete the rows one by one
          for (int i = 0; i < true_indexes.length; i++)
          {
               // Find the index of the row to delete from indexes
               int ind = 0;
               for (int j = 0; j < indexes.length; j++)
                    if (indexes[j] == true_indexes[i])
                    {
                    ind = j;
                    j = indexes.length;
                    }
               
               // Delete this row from indexes
               int[] temp = new int[indexes.length - 1];
               for (int j = 0; j < temp.length; j++)
               {
                    if (j < ind)
                         temp[j] = indexes[j];
                    else
                         temp[j] = indexes[j + 1];
               }
               indexes = temp;
               
               // Correct values in indexes
               for (int j = 0; j < indexes.length; j++)
                    if (indexes[j] > true_indexes[i])
                         indexes[j]--;
               
               // Remove the row from the table itself
               real_model.removeRow(true_indexes[i]);
          }
     }
     
     
     /**
      * Returns the corresponding contents of the real TableModel.
      *
      * @param	row	The index of the sorted table.
      * @param	column	The index of the column.
      * @return		The contents of the real TableModel at the given column
      *			and the corresponding row of the real TableModel.
      */
     public Object getValueAt(int row, int column)
     {
          return real_model.getValueAt(indexes[row], column);
     }
     
     
     /**
      * Sets the corresponding contents of the real TableModel.
      *
      * @param	value	The entry to place in the table.
      * @param	row	The index of the sorted table.
      * @param	column	The index of the column.
      */
     public void setValueAt(Object value, int row, int column)
     {
          real_model.setValueAt(value, indexes[row], column);
     }
     
     
     /**
      * Returns the number of rows in the table.
      *
      * @return    The number of rows in the table.
      */
     public int getRowCount()
     {
          return real_model.getRowCount();
     }
     
     
     /**
      * Returns the number of columns in the table.
      *
      * @return    The number of columns in the table.
      */
     public int getColumnCount()
     {
          return real_model.getColumnCount();
     }
     
     
     /**
      * Returns the name of the column at the given index.
      *
      * @param  column	 The index of the column.
      * @return          The name of the column at the given column index.
      */
     public String getColumnName(int column)
     {
          return real_model.getColumnName(column);
     }
     
     
     /**
      * Returns the class of the column at the given index.
      *
      * @param  column	The index of the column.
      * @return         The class of the column at the column index.
      */
     public Class getColumnClass(int column)
     {
          return real_model.getColumnClass(column);
     }
     
     
     /**
      * Returns the whether the given cell is editable or not.
      *
      * @param  row	The index of the sorted table.
      * @param  column	The index of the column.
      * @return         Whether the given cell is editable.
      */
     public boolean isCellEditable(int row, int column)
     {
          return real_model.isCellEditable(indexes[row], column);
     }
     
     
     /**
      * Adds the given listener to the table model.
      *
      * @param listener  The listener to add.
      */
     public void addTableModelListener(TableModelListener listener)
     {
          real_model.addTableModelListener(listener);
     }
     
     
     /**
      * Removes the given listener to the table model.
      *
      * @param listener The listener to remove from the table model.
      */
     public void removeTableModelListener(TableModelListener listener)
     {
          real_model.removeTableModelListener(listener);
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Attaches a MouseListener to the given table that will cause the given
      * table to be sorted whenever a column heading is pressed. Sorting will be
      * based on the column whose heading was pressed.
      *
      * @param	table                        The table to whom the sorting
      *                                      action is to be attached.
      * @throws	IllegalArgumentException     Throws an informatie exception if
      *                                      the given JTable does not have this
      *                                      TableSortModel attached to it.
      */
     private void attachColumnClickListenerForSorting(JTable table)
     throws IllegalArgumentException
     {
          if (table.getModel() != TableSortModel.this)
               throw new IllegalArgumentException("Given table does not have a model matching this TableSortModel.");
          final JTable this_table = table;
          MouseListener listener = new MouseAdapter()
          {
               public void mouseClicked(MouseEvent event)
               {
                    TableColumnModel tcm = this_table.getColumnModel();
                    int vc = tcm.getColumnIndexAtX(event.getX());
                    int mc = this_table.convertColumnIndexToModel(vc);
                    sort(mc);
               }
          };
          
          JTableHeader table_header = (JTableHeader) table.getTableHeader();
          table_header.addMouseListener(listener);
     }
     
     
     /**
      * Resets the indices of the intermediate representation.
      */
     private void allocate()
     {
          indexes = new int[getRowCount()];
          for (int i = 0; i < indexes.length; i++)
               indexes[i] = i;
     }
     
     
     /**
      * Sorts the table.
      *
      * @param	column	The column to base the sort on.
      */
     private void sort(int column)
     {
          int row_count = getRowCount();
          for (int i = 0; i < row_count; i++)
               for (int j = i + 1; j < row_count; j++)
                    if (compare(indexes[i], indexes[j], column) > 0)
                         swap(i,j);
     }
     
     
     /**
      * Swaps the two given entries in the indexes array.
      */
     private void swap(int i, int j)
     {
          int temp = indexes[i];
          indexes[i] = indexes[j];
          indexes[j] = temp;
     }
     
     
     /**
      * Returns 0 if the objects stored the table at the given indices of the table
      * are equal, returns - 1 if the entry in row i is less than that at row j and
      * returns 1 if the entry in row j is greater than the entry at row i.
      */
     private int compare(int i, int j, int column)
     {
          Object a = real_model.getValueAt(i, column);
          Object b = real_model.getValueAt(j, column);
          
          return comparator.compare(a, b);
     }
     
     
     /**
      * Finds the preferred width for the given column of the given table,
      * based on its contents and its header.
      *
      * @param	table	The table whose column width is to be examined.
      * @param	column	The column to be examined.
      * @return		The new preferred width of the column
      */
     private static int getPreferredWidthForColumn(JTable table, TableColumn column)
     {
          int header_width = getColumnHeaderWidth(table, column);
          int cell_width = getWidthOfWidestCellInColumn(table, column);
          
          if (header_width > cell_width)
               return header_width + 14;
          else
               return cell_width + 6;
     }
     
     
     /**
      * Finds the preferred width for the given column of the given table,
      * based on its header.
      *
      * @param	table	The table whose column width is to be examined.
      * @param	column	The column to be examined.
      * @return		The new preferred width of the column
      */
     private static int getColumnHeaderWidth(JTable table, TableColumn column)
     {
          TableCellRenderer renderer =  table.getTableHeader().getDefaultRenderer();
          Component component = renderer.getTableCellRendererComponent( table,
               column.getHeaderValue(),
               false,
               false,
               0,
               0 );
          return component.getPreferredSize().width;
     }
     
     
     /**
      * Finds the preferred width for the given column of the given table,
      * based on its contents.
      *
      * @param	table	The table whose column width is to be examined.
      * @param	column	The column to be examined.
      * @return		The new preferred width of the column
      */
     private static int getWidthOfWidestCellInColumn(JTable table, TableColumn column)
     {
          int c = column.getModelIndex();
          int width = 0;
          int max_width = 0;
          
          for (int r = 0; r < table.getRowCount(); r++)
          {
               TableCellRenderer renderer = table.getCellRenderer(r, c);
               Component component = renderer.getTableCellRendererComponent( table,
                    table.getValueAt(r, c),
                    false,
                    false,
                    r,
                    c );
               width = component.getPreferredSize().width;
               if (width > max_width)
                    max_width = width;
          }
          
          return max_width;
     }
}
