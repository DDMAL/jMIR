/*
 * FeatureValueFileSaver.java
 * Version 2.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jwebminer2;

import java.io.*;
import weka.core.*;
import weka.core.converters.ArffSaver;
import mckay.utilities.gui.progressbars.SimpleProgressBarDialog;
import ace.datatypes.DataSet;
import ace.datatypes.FeatureDefinition;
import mckay.utilities.gui.progressbars.SimpleProgressBarDialog;
import jwebminer2.gui.ResultsReportPanel;


/**
 * Objects of this class are used by ResultsReportPanels (or other classes) to
 * perform actual saving of data in a variety of formats. This implementation
 * allows the saving of HTML, tab delimited text, ACE Feature Vector XML or Weka
 * ARFF files.
 *
 * <p>HTML files include the entire displayed contents of a ResultsReportPanel,
 * but other formats only include the table of feature values and their labels.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class FeatureValueFileSaver
     extends mckay.utilities.general.FileSaver
{
     /* FIELDS ****************************************************************/


     /**
      * The extensiosn of the file formats that may be saved by this class.
      */
     private final String[]        available_formats = {"ACE XML", "Weka ARFF", "TXT", "HTML"};

     /**
      * The object holding the table of feature values and their labels that can
      * be saved.
      */
     private ResultsReportPanel    results_panel;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Creates a new instance of FeatureValueFileSaver.
      *
      * @param results_panel  Can be used to gain access to feature values to
      *                       be saved.
      */
     public FeatureValueFileSaver(ResultsReportPanel results_panel)
     {
          this.results_panel = results_panel;
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Provides an array of the extensions of file formats that may be saved
      * with this object.
      *
      * @return  The available file extensions.
      */
     public String[] getFileFormatExtension()
     {
          return available_formats;
     }


     /**
      * Save the given text to the given location in the given format or
      * save the stored feature values, depending on the chosen_file_extension.
      * A progress bar is displayed (although not incremented).
      *
      * @param chosen_file_extension The file extension (corresponding to one
      *                              of the extensions published by the
      *                              getFileFormatExtension method) to use when
      *                              saving data_to_save, and the corresponding
      *                              file format.
      * @param data_to_save          The HTML code displayed on-screen. May be
      *                              null for non-HTML saving.
      * @param save_location         The file to save data_to_save to.
      * @throws Exception            Throws an Exception if the file cannot be
      *                              saved.
      */
     public void saveContents(String chosen_file_extension, String data_to_save,
          File save_location)
          throws Exception
     {
          // Prepare the progress bar
          SimpleProgressBarDialog progress_bar = new SimpleProgressBarDialog(1, results_panel);

          // Write the whole contents of data_to_save verbatim as an HTML file
          // if an HTML file is to be saved
          if (chosen_file_extension.equals("HTML"))
          {
               DataOutputStream writer = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(save_location);
               writer.writeBytes(data_to_save);
               writer.close();
          }

          // Only save the table of final feature values itself if a non-HTML
          // file format is to be saved
          else
          {
               // Access information to store
               double[][] feature_table = results_panel.feature_values;

               String[] column_labels = results_panel.column_labels;
               String[] row_labels = results_panel.row_labels;
               String[] orig_column_labels = column_labels;

               if (AnalysisProcessor.lastfm_enabled && AnalysisProcessor.is_cross_tabulation && (AnalysisProcessor.yahoo_application_id != null || AnalysisProcessor.google_license_key != null))
                   {
                   String[] column_labels_lastfm_websearch = new String[2 * column_labels.length];
                   for(int i = 0; i<column_labels.length; i++)
                       {
                       column_labels_lastfm_websearch[i] = column_labels[i]+ "_WS";
                       column_labels_lastfm_websearch[i + column_labels.length] = column_labels[i] + "_LastFM";
                       }
                   column_labels = column_labels_lastfm_websearch;
                   }
               else {column_labels = orig_column_labels;}


               // Save as tab delimited text file
               if (chosen_file_extension.equals("TXT"))
               {
                    // Calculate the table to save
                    String[][] results_table = new String[row_labels.length + 1][column_labels.length + 1];
                    results_table[0][0] = "";
                    for (int i = 0; i < results_table.length; i++)
                    {
                         for (int j = 0; j < results_table[i].length; j++)
                         {
                              if (i == 0)
                              {
                                   if (j != 0) results_table[i][j] = column_labels[j - 1];
                              }
                              else
                              {
                                   if (j == 0)
                                        results_table[i][j] = row_labels[i - 1];
                                   else
                                        results_table[i][j] = String.valueOf(feature_table[i - 1][j - 1]);
                              }
                         }
                    }

                    // Save the table
                    DataOutputStream writer = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(save_location);
                    for (int i = 0; i < results_table.length; i++)
                    {
                         for (int j = 0; j < results_table[i].length; j++)
                         {
                              // Write the table entry
                              writer.writeBytes(results_table[i][j]);

                              // Add a tab or a line break
                              if (j == results_table[i].length - 1) writer.writeBytes("\n");
                              else writer.writeBytes("\t");
                         }
                    }

                    // Close the writing stream
                    writer.close();
               }

               // Save as ACE XML file
               else if (chosen_file_extension.equals("ACE XML"))
               {
                    // Set the name of the dataset to the name of the file
                    // that is tob be saved
                    String data_set_name = mckay.utilities.staticlibraries.StringMethods.removeExtension(save_location.getName());

                    // Prepare feature definitions and store feature names to
                    // put in DataSets
                    FeatureDefinition[] feature_definitions = new FeatureDefinition[column_labels.length];
                    String[] feature_names = new String[column_labels.length];
                    for (int feat = 0; feat < feature_definitions.length; feat++)
                    {
                         feature_definitions[feat] = new FeatureDefinition(column_labels[feat], "", false, 1);
                         feature_names[feat] = column_labels[feat];
                    }

                    // Prepare the the DataSets to write
                    DataSet[] data_sets = new DataSet[row_labels.length];
                    for (int instance = 0; instance < data_sets.length; instance++)
                    {
                         // Instantiate the DataSet
                         data_sets[instance] = new DataSet();

                         // Store the instance names
                         data_sets[instance].identifier = row_labels[instance];

                         // Store the names of the features
                         data_sets[instance].feature_names = feature_names;

                         // Store the features for this DataSet as well as the
                         // feature names
                         double[][] these_feature_values = new double[feature_table[instance].length][1];
                         for (int feat = 0; feat < these_feature_values.length; feat++)
                              these_feature_values[feat][0] = feature_table[instance][feat];
                         data_sets[instance].feature_values = these_feature_values;

                         // Validate, order and compact the DataSet
                         data_sets[instance].orderAndCompactFeatures(feature_definitions, true);
                    }

                    // Save the feature values
                    DataSet.saveDataSets(data_sets, feature_definitions, save_location, "Features extracted with jWebMiner 2.0");
               }

               // Save as Weka ARFF file
               else if (chosen_file_extension.equals("Weka ARFF"))
               {
                    // Set the name of the dataset to the name of the file
                    // that is to be saved
                    String data_set_name = mckay.utilities.staticlibraries.StringMethods.removeExtension(save_location.getName());

                    // Set the Attributes (feature names and class names)
                    FastVector attributes_vector = new FastVector(column_labels.length + 1); // extra 1 is for class name
                    for (int feat = 0; feat < column_labels.length; feat++)
                         attributes_vector.addElement(new Attribute(column_labels[feat]));
                    FastVector class_names_vector = new FastVector(column_labels.length);
                    for (int cat = 0; cat < orig_column_labels.length; cat++)
                         class_names_vector.addElement(orig_column_labels[cat]);
                    attributes_vector.addElement(new Attribute("Class", class_names_vector));

                    // Store attributes in an Instances object
                    Instances instances = new Instances(data_set_name, attributes_vector, row_labels.length);
                    instances.setClassIndex(instances.numAttributes() - 1);

                    // Store the feature values and model classifications
                    for (int inst = 0; inst < row_labels.length; inst++)
                    {
                         // Initialize an instance
                         Instance this_instance = new Instance(instances.numAttributes());
                         this_instance.setDataset(instances);
                         int current_attribute = 0;

                         // Set feature values for the instance
                         for (int feat = 0; feat < column_labels.length; feat++)
                              this_instance.setValue(feat, feature_table[inst][feat]);

                         // Set the class value for the instance
                         // this_instance.setClassValue("a");
                         instances.setRelationName("jWebMiner2");

                         // Add this instance to instances
                         instances.add(this_instance);
                    }

                    // Prepare the buffer to save to and add comments indicating
                    // the names of the rows
                    DataOutputStream writer = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(save_location);
                    writer.writeBytes("% INSTANCES (DATA ROWS) BELOW CORRESPOND TO:\n%\n");
                    for (int inst = 0; inst < row_labels.length; inst++)
                         writer.writeBytes("%    " + (inst + 1) + ") " + row_labels[inst] + "\n");
                    writer.writeBytes("%\n");

                    // Save the ARFF file
                    ArffSaver arff_saver = new ArffSaver();
                    arff_saver.setInstances(instances);
                    arff_saver.setFile(save_location);
                    arff_saver.setDestination(writer);
                    try
                    {arff_saver.writeBatch();}
                    catch (Exception e)
                    {
                         throw new Exception("File only partially saved.\n\nTry resaving the file with a .arff extension.");
                    }

                    // Close the writer
                    writer.close();
               }
          }

          // Terminate the progress bar
          progress_bar.done();
     }
}
