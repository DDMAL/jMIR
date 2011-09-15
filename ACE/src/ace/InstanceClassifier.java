/*
 * InstanceClassifier.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace;

import ace.datatypes.*;
import weka.core.*;
import java.io.*;
import java.text.*;
import weka.core.converters.ArffSaver;


/**
 * Classifies a set of Weka Instances using a trained Weka Classifier of a TrainedModel object.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class InstanceClassifier
{

    /**
     * Classify a set of instances using a trained Weka Classifier.
     *
     * @param   trained                         Object containing references to
     *                                          the Weka objects needed for Classification,
     *                                          including a trained Weka Classifier.
     * @param	data_board			Contains instances to classify and
     *                                          method to perform classification.
     * @param	instances			The Weka Instances to classify.
     * @param	save_intermediate_arffs         Whether or not to save testing data to an
     *						arff file after parsing and again after feature
     *						selection, if any. Useful for testing.
     * @param	results_file			The name of the file to which the
     *						classification results will be stored.
     *                                          Should have extention ".xml" if using
     *                                          ACE XML files or ".arff" if using
     *                                          Weka ARFF files. May be null if no
     *                                          file is to be saved.
     * @return                                  The classifications of each instance.
     * @throws Exception                        If an error is encountered.
     */
    public static SegmentedClassification[] classify(TrainedModel trained,
            DataBoard data_board,
            Instances instances,
            String results_file,
            boolean save_intermediate_arffs)
            throws Exception
    {
        boolean use_top_level_features = true;
        boolean use_sub_section_features = true;
        // Perform the classification and store the results in array of
        // SegmentedClassification objects to be returned
        SegmentedClassification[] resulting_classifications =
                data_board.getClassifiedResults(instances,
                save_intermediate_arffs,
                trained,
                use_top_level_features,
                use_sub_section_features);

        // Merge overlapping sections with the same classifications
        for (int i = 0; i < resulting_classifications.length; i++)
            SegmentedClassification.mergeAdjacentSections(resulting_classifications[i]);

        // Print to results file. (Will be an  ACE XML Classifications file or ARFF file.)
        // Extension of given results file name is checked in CommandLine
        if (results_file != null) {
            if (mckay.utilities.staticlibraries.StringMethods.getExtension(results_file).equals(".arff"))
            {
                ArffSaver saver = new ArffSaver();
                saver.setInstances(instances);
                saver.setFile(new File(results_file));
                saver.setDestination(new File(results_file));
                saver.writeBatch();
            }
            else if (mckay.utilities.staticlibraries.StringMethods.getExtension(results_file).equals(".xml"))
            {
                SegmentedClassification.saveClassifications(resulting_classifications,
                        new File(results_file),
                        "");
            }
        }
        return resulting_classifications;
    }

    /**
     * Classifies a set of Weka Instances. Returns a classified copy of the given
     * Instances. This method is used in the context of cross validation when only
     * Weka Instances objects are used to store the instances and never ACE datatypes
     * like SegmentedClassification and DataSet. Note that dimensionality reduction (if any)
     * has already been applied to the instances prior to being passed to this method.
     *
     * @param trained                   Object containing references to
     *                                  the Weka objects needed for Classification,
     *                                  including a trained Weka Classifier.
     * @param instances                 The Weka Instances to classify.
     * @param save_intermediate_arffs   Whether or not to save testing data to an
     *                                  arff file after parsing and again after feature
     *					selection, if any. Useful for testing.
     * @return                          A classified copy of the given Instances.
     * @throws java.lang.Exception      If an error occurs.
     */
    public static Instances classifyInstances(TrainedModel trained,
            Instances instances,
            boolean save_intermediate_arffs)
            throws Exception
    {
        // Make copy of given Instances
        Instances classified = new Instances(instances);

        // Classify each instance
        for(int inst = 0; inst < instances.numInstances(); inst++)
        {
            double predicted = trained.classifier.classifyInstance(instances.instance(inst));
            classified.instance(inst).setClassValue(predicted);
        }
        return classified;
    }

    /**
     * Gets the confusion matrix for a set of classified Instances. Compares the
     * model classifications to the classifications made by a trained Weka Classifier.
     *
     * @param model         The original Instances that were used for testing.
     * @param classified    The classified Instances to be evaluated.
     * @param classes       The possible classes into which an Instance may be classified.
     * @return              Table representing the correct and incorrect classifications
     *                      of this classification.
     */
    public static double[][] getConfusionMatrix(Instances model, Instances classified, String[] classes)
    {
        int predicted;
        int actual;
        int num_classes = classes.length;
        double[][] matrix = new double[num_classes][num_classes];
        for (int inst = 0; inst < model.numInstances(); inst++)
        {
            predicted = (int) classified.instance(inst).classValue();
            actual = (int) model.instance(inst).classValue();
            matrix[actual][predicted]++;
        }
        return matrix;
    }

    /**
     * Creates an easily readable version of a confusion matrix to be included in results output.
     *
     * @param matrix    Confusion matrix for a classification. Table representing
     *                  correct and incorrect classifications.
     * @param classes   The possible classes into which an instance may be classified.
     * @return          Easily readable table representing the correct and incorrect classifications.
     */
    public static String formatConfusionMatrix(double[][] matrix, String[] classes)
    {
        int num_classes = classes.length;
        StringBuffer text = new StringBuffer();
        char[] IDChars =
        {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
        };
        int IDWidth;
        boolean fractional = false;

        /*if (!m_ClassIsNominal) {
        throw new Exception("Evaluation: No confusion matrix possible!");
        }*/

        // Find the maximum value in the matrix
        // and check for fractional display requirement
        double maxval = 0;
        for (int i = 0; i < num_classes; i++)
        {
            for (int j = 0; j < num_classes; j++)
            {
                double current = matrix[i][j];
                if (current < 0)
                    current *= -10;
                if (current > maxval)
                    maxval = current;
                double fract = current - Math.rint(current);
                if (!fractional && ((Math.log(fract) / Math.log(10)) >= -2))
                    fractional = true;
            }
        }

        IDWidth = 1 + Math.max((int) (Math.log(maxval) / Math.log(10) + (fractional ? 3 : 0)),
                (int) (Math.log(num_classes) /
                Math.log(IDChars.length)));
        //text.append(title).append("\n");
        for (int i = 0; i < num_classes; i++)
        {
            if (fractional)
                text.append(" ").append(num2ShortID(i, IDChars, IDWidth - 3)).append("   ");
            else
                text.append(" ").append(num2ShortID(i, IDChars, IDWidth));
        }
        text.append("   <-- classified as\n");
        for (int i = 0; i < num_classes; i++)
        {
            for (int j = 0; j < num_classes; j++)
            {
                text.append(" ").append(
                        Utils.doubleToString(matrix[i][j],
                        IDWidth,
                        (fractional ? 2 : 0)));
            }
      text.append(" | ").append(num2ShortID(i,IDChars,IDWidth))
        .append(" = ").append(classes[i]).append("\n");
        }
        return text.toString();
    }

    /**
     * Method for generating indices for the confusion matrix.
     *
     * @param num 	integer to format
     * @param IDChars	the characters to use
     * @param IDWidth	the width of the entry
     * @return 		the formatted integer as a string
     */
    protected static String num2ShortID(int num, char[] IDChars, int IDWidth)
    {

        char ID[] = new char[IDWidth];
        int i;

        for (i = IDWidth - 1; i >= 0; i--)
        {
            ID[i] = IDChars[num % IDChars.length];
            num = num / IDChars.length - 1;
            if (num < 0)
            {
                break;
            }
        }
        for (i--; i >= 0; i--)
        {
            ID[i] = ' ';
        }

        return new String(ID);
    }

     /**
      * Compares the given classifications with the given model classifications
      * and returns number of correct classifications.
      *
      * <p>If an instance belongs to multiple classes in its model
      * classifications, and only a fraction of these are found, then the
      * calculation of the overall success rate will treat this as fractionally
      * succesful.
      *
      * @param	models      The model classifications.
      * @param	results     The classifications to compare to the models.
      * @return             The number of correct classifications.
      */
     public static double getCorrectCount( Instances models,
          Instances results )
     {
          // Score keeper
          double correct_count = 0.0;

          // Count the matching classifications
          for (int r = 0; r < results.numInstances(); r++)
          {
               String model_clas = models.instance(r).stringValue(models.classIndex());
               String result_clas = results.instance(r).stringValue(results.classIndex());

               if (model_clas != null && result_clas != null)
                    if (model_clas.equals(result_clas))
                        correct_count++;
          }
          // Return the results
          return correct_count;
     }

     /**
      * Gets a String describing the success rate of a classification.
      * Compares the given classifications with the given model classifications
      * and returns a string describing the success rates  for overall instances and/or sections of
      * instances, whichever is appropriate for the given data.
      *
      * @param models   The model classifications from the original instances.
      * @param results  The classifications predicted by the trained Classifier.
      * @param out      StringBuffer that will be passed to getSuccessRate and to
      *                 which the results of individual classifications will be printed.
      * @return         String describing the success rate of a classification.
      */
     public static String getSuccessString( SegmentedClassification[] models,
          SegmentedClassification[] results, StringBuffer out )
     {
         // Limit results to 2 decimal places
         DecimalFormat df = new DecimalFormat("####0.0#");

         // 1st index is number of correct classifications of overall instances
         // 2nd index is number of correct classifications of sections
         // 3rd index is total number of instances used to calculate success rate
         // (does not include instances that do not have model classifications)
         double[] correct = getSuccessRate(models, results, out);
         double total_count = correct[2];
         double total_sections = 0;
         for(int clas = 0; clas<results.length; clas++)
         {
             if(results[clas].sub_classifications!=null)
                total_sections+=results[clas].sub_classifications.length;
         }
         double correct_overall = correct[0];
         double correct_sections = correct[1];
         String overall_results = "";
         String section_results = "";

          if (total_count != 0.0 && correct_overall != 0.0)
          {
               double success_rate = 100.0 * correct_overall / (double) total_count;
               overall_results = "\nSUCCESS RATES FOR OVERALL CLASSIFICATIONS:\n" +
                    df.format(success_rate) + "%: " + correct_overall + " / " + total_count + "\n" +
                    df.format(total_count - correct_overall) + " misclassifications.\n\n";
          }
          if (total_sections != 0.0 && correct_sections != 0.0)
          {
               double success_rate = 100.0 * correct_sections / (double) total_sections;
               section_results = "SUCCESS RATES FOR CLASSIFICATION OF SECTIONS:\n" +
                    df.format(success_rate) + "%: " + correct_sections + " / " + total_sections + "\n" +
                    df.format(total_sections - correct_sections) + " misclassifications.\n\n";
          }
         return overall_results + section_results;
     }

    /**
     * Gets the number of correct classifications for overall instances and subsections
     * and appends the results of each classification to a given StringBuffer object.
     * Misclassified instances will be preceded by an asterisk(*). Partially misclassified
     * instances will be preceded by a caret(^).
     *
     * <p>If an instance belongs to multiple classes in its model
     * classifications, and only a fraction of these are found, then the
     * calculation of the overall success rate will treat this as fractionally
     * succesful.
     *
     * <p>The reported value for error rate includes wrong classifications
     * as well as additional classifications beyond the correct ones (sincce
     * a given instance may have an arbitrary number of correct classes).
     *
     * @param	models	The model classifications.
     * @param	results	The classifications to compare to the models.
     * @param   out     The StringBuffer to which results of the classification
     *                  of each individual instance will be printed. Misclassifications
     *                  will be preceded by an asterisk(*) and partially misclassified
     *                  instances will be preceeded by a caret(^).
     * @return		A array of 3 doubles. First cell is number of correct
     *                  classifications of overall instances. Second cell is number
     *                  of correct classifications of subsections. 3rd cell contains
     *                  total number of instances to be used during calculation
     *                  of success rate (instances without model classifications
     *                  are not included). Number of correct
     *                  classifications is calculated as a score taking into account
     *                  multiple classifications for single instances and overlapping
     *                  sections.
     */
    private static double[] getSuccessRate(SegmentedClassification[] models,
            SegmentedClassification[] results, StringBuffer out)
    {
        // This array will be returned containing score for overall instances in
        // the first cell, and score for subsections in the second cell, and total
        // number of instances to be used when calculating success rate in the third cell.
        double[] correct = new double[3];

        // Score keepers
        double correct_count = 0.0;
        int[] number_false_positives = new int[models.length];
        double sections_correct = 0.0;
        double sections_false_positives = 0;
        boolean unknowns = false;

        // Find the success rate for overall instances
        for (int r = 0; r < results.length; r++)
        {
            for (int m = 0; m < models.length; m++)
            {
                if (models[m].identifier.equals(results[r].identifier)) // find corresponding instances
                {   number_false_positives[m] = 0;
                    String[] model_clas = models[m].classifications;
                    String[] result_clas = results[r].classifications;
                    if (model_clas != null && result_clas != null)
                    {
                        correct[2]++;
                        double correct_number_classes = (double) model_clas.length;
                        double found_number_classes = 0;
                        for (int r_clas = 0; r_clas < result_clas.length; r_clas++)
                        {
                            boolean found = false;
                            for (int m_clas = 0; m_clas < model_clas.length; m_clas++)
                            {
                                if (model_clas[m_clas].equals(result_clas[r_clas]))
                                {
                                    found = true;
                                    found_number_classes++;
                                    m_clas = model_clas.length;
                                }
                            }
                            if (!found)
                            {
                                number_false_positives[m]++;//increases everytime a model class is not found in results
                            }
                        }
                        // Append * if instance was completely misclassified
                        if(number_false_positives[m]>0&&number_false_positives[m] == model_clas.length)
                            out.append("*");
                        // Append ^ if instance was only partially misclassified
                        if(result_clas.length < model_clas.length && number_false_positives[m]<result_clas.length)
                            out.append("^");
                        String classes = mckay.utilities.staticlibraries.StringMethods.concatenateArrayOfStrings(model_clas);
                            out.append("INSTANCE " + r + ": " + results[r].identifier +
                                    " \n\tPREDICTED CLASS: " + result_clas[0] +
                                    "     ACTUAL CLASS: " + classes + "\n");
                        double this_score = found_number_classes / correct_number_classes;
                        correct_count += this_score;
                    }
                    else
                    {
                        //String classes = mckay.utilities.staticlibraries.StringMethods.concatenateArrayOfStrings(model_clas);
                        out.append("INSTANCE " + r + ": " + results[r].identifier +
                                    " \n\tPREDICTED CLASS: " + result_clas[0] +
                                    "     ACTUAL CLASS: *UNKNOWN*\n");
                        unknowns = true;
                    }

                    // Refer to the sub-sections
                    SegmentedClassification[] mod_sec = models[m].sub_classifications;
                    SegmentedClassification[] res_sec = results[r].sub_classifications;

                    if (mod_sec != null && res_sec != null)
                    {
                        // Find the ranges of influence for each sub-section of
                        // the SegmentedClassifications
                        double[] mod_low_bound = new double[mod_sec.length];
                        double[] mod_high_bound = new double[mod_sec.length];
                        for (int i = 0; i < mod_sec.length; i++)
                        {
                            mod_low_bound[i] = mod_sec[i].start;
                            mod_high_bound[i] = mod_sec[i].stop;
                        }
                        double[] res_low_bound = new double[res_sec.length];
                        double[] res_high_bound = new double[res_sec.length];
                        for (int i = 0; i < res_sec.length; i++)
                        {
                            res_low_bound[i] = res_sec[i].start;
                            res_high_bound[i] = res_sec[i].stop;
                        }

                        // Go through each sub-section of the results and find
                        // the appropriate label(s) for it
                        String[][] model_labels_of_results = new String[res_sec.length][];
                        for (int r_sec = 0; r_sec < res_sec.length; r_sec++)
                        {
                            model_labels_of_results[r_sec] = null;
                            double[] fraction_in = new double[mod_sec.length];
                            double result_length = res_high_bound[r_sec] - res_low_bound[r_sec];
                            for (int m_sec = 0; m_sec < mod_sec.length; m_sec++)
                            {
                                // Case with no intersection
                                if (res_high_bound[r_sec] < mod_low_bound[m_sec] ||
                                        res_low_bound[r_sec] > mod_high_bound[m_sec])
                                    fraction_in[m_sec] = 0;
                                // Case where result is fully within model
                                else if (res_low_bound[r_sec] >= mod_low_bound[m_sec] &&
                                        res_high_bound[r_sec] <= mod_high_bound[m_sec])
                                    fraction_in[m_sec] = 1.0;
                                // Case where model is fully within result
                                else if (res_low_bound[r_sec] <= mod_low_bound[m_sec] &&
                                        res_high_bound[r_sec] >= mod_high_bound[m_sec])
                                    fraction_in[m_sec] = (mod_high_bound[m_sec] - mod_low_bound[m_sec]) / result_length;
                                // Case where result is partially outside of model (to the left)
                                else if (res_low_bound[r_sec] <= mod_low_bound[m_sec] &&
                                        res_high_bound[r_sec] <= mod_high_bound[m_sec] &&
                                        res_high_bound[r_sec] >= mod_low_bound[m_sec])
                                    fraction_in[m_sec] = (res_high_bound[r_sec] - mod_low_bound[m_sec]) / result_length;
                                // Case where result is partially outside of model (to the right)
                                else if (res_low_bound[r_sec] >= mod_low_bound[m_sec] &&
                                        res_high_bound[r_sec] >= mod_high_bound[m_sec] &&
                                        res_low_bound[r_sec] <= mod_high_bound[m_sec])
                                    fraction_in[m_sec] = (mod_high_bound[m_sec] - res_low_bound[r_sec]) / result_length;
                                else
                                    fraction_in[m_sec] = 0;
                            }
                            int best_m_sec = mckay.utilities.staticlibraries.MathAndStatsMethods.getIndexOfLargest(fraction_in);
                            model_labels_of_results[r_sec] = mod_sec[best_m_sec].classifications;
                        }

                        // Find the success stats for each section
                        for (int r_sec = 0; r_sec < res_sec.length; r_sec++)
                        {
                            String[] section_result_clas = res_sec[r_sec].classifications;
                            String[] section_model_clas = model_labels_of_results[r_sec];
                            double correct_number_classes = (double) section_model_clas.length;
                            double found_number_classes = 0;

                            for (int r_clas = 0; r_clas < section_result_clas.length; r_clas++)
                            {
                                boolean found = false;
                                for (int m_clas = 0; m_clas < section_model_clas.length; m_clas++)
                                {
                                    if (section_model_clas[m_clas].equals(section_result_clas[r_clas]))
                                    {
                                        found = true;
                                        found_number_classes++;
                                        m_clas = section_model_clas.length;
                                    }
                                }
                                out.append("\t\t");
                                if (!found)
                                {
                                    sections_false_positives++;
                                    out.append("*");
                                }
                                String sec_classes = mckay.utilities.staticlibraries.StringMethods.concatenateArrayOfStrings(section_model_clas);
                                out.append("SECTION " + r_sec + ": " + res_sec[r_sec].start + " to " + res_sec[r_sec].stop +
                                        " \n\t\t\tPREDICTED CLASS: " + section_result_clas[0] +
                                        "     ACTUAL CLASS: " + sec_classes + "\n");
                            }
                            double this_score = found_number_classes / correct_number_classes;
                            sections_correct += this_score;
                        }
                    }
                    m = models.length;
                }
            }
        }
        // Print warning if not all instances had model classifications
        if(unknowns)
            out.append("\nWARNING: Some model instances were unlabelled and were not " +
                    "included in the calculation of success rates.\n");
        correct[0] = correct_count;
        correct[1] = sections_correct;
        // Return the results
        return correct;
     }

}
