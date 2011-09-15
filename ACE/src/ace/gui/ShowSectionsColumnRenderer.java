/*
 * ShowSectionsColumnRenderer.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A custom cell renderer for the "Show Sections" column of the Instances Panel.
 * This column contains a checkbox if the instance in that row is expandable (has
 * subsections). Otherwise, the cell should be blank.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class ShowSectionsColumnRenderer
        extends DefaultTableCellRenderer
{
    /* FIELDS ****************************************************************/


    /**
     * Contains the identifiers of the instances that are expandable.
     */
    String[] expandable;


    /* CONSTRUCTOR **********************************************************/


    /**
     * Basic constructor.
     *
     * @param expandable    Array containing the identifiers of the expandable
     *                      instances.
     */
    public ShowSectionsColumnRenderer(String[] expandable)
    {
        this.expandable = expandable;
    }


    /* PUBLIC METHOD ********************************************************/


    /**
     * Determines whether the cell should be blank or have a checkbox. If the
     * String in the "Identifier" column of this row is present in the given <code>
     * expandable</code> array, a checkbox will be present. The cell will be blank
     * otherwise.
     *
     * @param table         The <code>JTable</code>
     * @param value         The value to assign to the cell at
     *                      <code>[row, column]</code>
     * @param isSelected    True if cell is selected
     * @param hasFocus      True if cell has focus
     * @param row           The row of the cell to render
     * @param column        The column of the cell to render
     * @return              Either the default cell renderer if the instance in
     *                      question is expandable or a blank JLabel.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus,
                                                               int row, int column)
    {
        // Check to see if Identifier for the instance displayed in this row is
        // in the given array.
        if(mckay.utilities.staticlibraries.StringMethods.isStringInArray(table.getValueAt(row, 1).toString(), expandable))
        {
            // Cell will have a checkbox
            Component to_return = table.getDefaultRenderer(table.getColumnClass(column)).
                    getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return to_return;
        }
        else
        {
            // Cell will be blank
            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setText(null);
            return comp;
        }
    }
}
