/*
 * ResultsReportPanel.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2.gui;

import java.awt.*;
import javax.swing.*;
import mckay.utilities.gui.templates.ResultsDisplayPanel;


/**
 * A JPanel for displaying and saving data. It consists of a JEditorPane for
 * viewing the results, a Save button for saving it and a JComboBox for choosing
 * the file format to save the data in.
 *
 * <p>Actual saving is performed using a FeatureValueFileSaver object.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class ResultsReportPanel
     extends JPanel
{
     /* FIELDS ****************************************************************/


     /**
      * Holds the display panel and the save button and the save button combo
      * box.
      */
     private ResultsDisplayPanel   display_panel;

     /**
      * The final scores arrived at via feature extraction, taking into account
      * the formula chosen, the search options chosen, site weighting,
      * different web services, normalization, etc., all as specified in the
      * OptionsPanel and calculated in the AnalysisProcessor.
      */
     public double[][]           feature_values;

     /**
      * The labels for the rows (first dimension) of feature_values.
      */
     public String[]             row_labels;

     /**
      * The labels for the columns (second dimension) of feature_values.
      */
     public String[]             column_labels;




     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates a new instance of ResultsReportPanel
      */
     public ResultsReportPanel()
     {
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;

          // Initialize the display panel
          display_panel = new ResultsDisplayPanel(new jwebminer2.FeatureValueFileSaver(this));

          // Prepare the layout
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          display_panel.setBorder(BorderFactory.createEmptyBorder(vertical_gap, horizontal_gap, vertical_gap, horizontal_gap));
          add(display_panel);
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Erases any text already in the display pane and replaces it by the
      * contents of new_text. The caret is set to the beginning of the text.
      *
      * @param new_text  The new text to add. Nothing is done if this is null.
      */
     public void setText(String new_text)
     {
          display_panel.setText(new_text);
     }


     /**
      * Store already extracted feature values and their labels in this object.
      *
      * @param feature_values The final scores arrived at via feature
      *                       extraction, taking into account the formula
      *                       chosen, the search options chosen, site weighting,
      *                       different web services, normalization, etc., all
      *                       as specified in the OptionsPanel and calculated in
      *                       the AnalysisProcessor.
      * @param row_labels     The labels for the rows (first dimension) of
      *                       feature_values.
      * @param column_labels  The labels for the columns (second dimension) of
      *                       feature_values.
      */
     public void storeFeatureValues(double[][] feature_values,
          String[] row_labels, String[] column_labels)
     {
          this.feature_values = feature_values;
          this.row_labels = row_labels;
          this.column_labels = column_labels;
     }
}
