/*
 * Experimenter.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */
package ace;

import ace.datatypes.*;
import java.io.*;
import java.text.*;
import java.util.LinkedList;
import java.util.Arrays;
import weka.classifiers.*;
import weka.core.*;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;
import weka.attributeSelection.AttributeSelection;


/**
 * Finds the best classification method for the given instances.
 *
 * <p>Finds the best classification approach by making repeated calls to
 * the <i>CrossValidator</i> class. Different types of Classifiers are tested with multiple types
 * of dimensionality reduction. Dimensionality reduction is performed first. <i>Experimenter</i>
 * calls <i>DimensionalityReducer</i> to get an array of Instances objects, each cell containing
 * a different dimensionality reduced version of the original instances. Each type of Classifier
 * is cross validated with each set of dimensionality reduced instances. Results are saved
 * in multiple files. One file will be created for each set of dimensionality reduced instances.
 * A summary of the results for each cross validation for each dimensionality reduction
 * will be written in the file for the corresponding set of instances. The results for the
 * best found Classifier for each dimensionality reduction are
 * printed at the beginning of each file. The best classification overall is chosen by comparing
 * the best results for each dimensionality reduction. A copy of the results summary for the
 * best found classification approach will be written in a separate file called
 * "experimentation_results_best_results_overall.txt" After the best classification has been chosen,
 * validation is performed using a
 * publication set that was set aside at the beginning of the experiment. A new Classifier
 * of the chosen type is created and trained on the chosen type of dimensionality reduced
 * instances (all instances are included except for the publication set). The newly trained
 * Classifier is tested on the publication set and results are saved to the
 * "experimentation_results_best_results_overall.txt" file.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */

public class Experimenter
{
    /* FIELDS ****************************************************************/

    /**
     * Status updates are sent to this OutputStream.
     * If ACE is being run from the command line, this OutputStream will be the standard out.
     */
    OutputStream out;

    /* CONSTRUCTOR ***********************************************************/

    /**
     * Constructs an instance if an Experimenter.
     *
     * @param out  Progress reports are sent here as the experimentation runs.
     */
    public Experimenter(OutputStream out)
    {
        this.out= out;
    }

    /* PUBLIC METHOD *********************************************************/

    /**
     * Apply a variety of dimensionality reduction and classification techniques
     * to the given instances. Results are calculated using cross-validation.
     * A report of the details of the best performing approach are returned.
     * A file issaved for every type of dimensionality reduction performed. Each
     * file gives the results for a variety of Classifiers.
     *
     * <p>Apply a variety of dimensionality reduction techniques in order
     * to get several sets of instances, each with different features selected.
     * Acquire information about the names of the feature selectors, the features
     * they selected and how long each took.
     *
     * <p>Note that progress is printed to given OutputStream. Results are saved to files
     * with a default base file name of "experimentation_results" unless another
     * file name was specified using the -sres flag at the command line.
     *
     * @param	folds				The number of folds to perform cross-
     *						validation over.
     * @param	instances			The instances to use for performing cross-
     *						validation.
     * @param	results_base_file_name		The basic file path to use for saving
     *						results files. No files are saved if
     *						this is null.
     * @param	save_intermediate_arffs		Whether or not to save training data to an
     *						arff file at various intermediate stages.
     *						Useful for testing.
     * @param   verbose                         Whether or not to include detailed
     *                                          information in the experimentation results.
     * @param   max_attribute                   If the number of attributes for a
     *                                          data set is larger than this number,
     *                                          an exhaustive search will be not be
     *                                          performed.
     * @param   identifiers                     The unique identifiers for the given
     *                                          set of Instances. This is used when
     *                                          outputting the detailed results of
     *                                          the validation when the verbose option
     *                                          is specified.
     * @param num_overall       The number of overall instances contained in the given set
     *                          of instances.
     * @param hierarchy     Contains information about how the given Instances might
     *                      have been modified or rearranged. Maps current Instances
     *                      to original set (pre randomization/deletion).
	 * @return					A report detailing the best approach found.
     * @throws  Exception                       If an error occurs.
     */
    public String crossValidateMultiApproaches(int folds,
            Instances instances,
            String results_base_file_name,
            boolean save_intermediate_arffs,
            boolean verbose,
            int max_attribute,
            String[] identifiers,
            int num_overall,
            String[] hierarchy)
            throws Exception
    {
        LinkedList<String> classifier_descriptions_list = new LinkedList<String>();
        LinkedList<String> selector_descriptions_list = new LinkedList<String>();
        LinkedList<String> features_selected_list = new LinkedList<String>();
        LinkedList<String> selectors = new LinkedList<String>();
        selectors.add("PCA");
        selectors.add("EXB");
        selectors.add("GNB");
        double[][] preparation_times = new double[1][];
        DecimalFormat df = new DecimalFormat("####0.0#");

        // Set aside publication data
        int num_validation = (int)Math.ceil(num_overall/(folds+1));
        LinkedList <String> pub_names = new LinkedList<String>();
        Instances valid = getPublicationSet(instances, pub_names, identifiers, hierarchy, num_validation, num_overall);
        num_overall = num_overall-num_validation;
        Object[] hierarchy2 = mckay.utilities.staticlibraries.ArrayMethods.removeNullEntriesFromArray(hierarchy);
                hierarchy = new String[hierarchy2.length];
        for(int i=0; i < hierarchy2.length; i++)
        {
            hierarchy[i] = hierarchy2[i].toString();
        }
        // Get array of Instances, all with different dimensionality reduction performed
        Instances[] instances_array = DimensionalityReducer.getDimensionallyReducedInstances(instances,
                selectors,
                selector_descriptions_list,
                features_selected_list,
                preparation_times,
                out,
                verbose,
                max_attribute);

        // Store the results for the feature selections
        String[] selector_descriptions = selector_descriptions_list.toArray(new String[1]);
        String[] features_selected = features_selected_list.toArray(new String[1]);
        //selector_descriptions_list = null;
        //features_selected_list = null;

        // Structures used for finding the best classifier and the best feature selector
        double[] best_error_rates_accross_feature_sets = new double[instances_array.length];
        String[] best_classifiers_accross_feature_sets = new String[instances_array.length];
        CrossValidationResults[][] cvres = new CrossValidationResults[instances_array.length][];
        int[] best_indices = new int[instances_array.length];

        // Apply a variety of classifiers to each set of instances
        for (int inst = 0; inst < instances_array.length; inst++)
        {
            // A summary of the results of this experiment
            StringBuffer cv_results = new StringBuffer();

            // Give user status update
            // Print the results of the feature selection that was performed
            // for this set of instances
            out.write("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n".getBytes());
            out.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n".getBytes());
            out.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n\n\n".getBytes());
            out.write(("Processing feature set " + (inst + 1) + " of " + instances_array.length + "...\n").getBytes());
            out.write(("DIMENSIONALITY REDUCTION: " + selector_descriptions[inst] + "\n").getBytes());
            out.write(("TIME TAKEN: " + preparation_times[0][inst] + " minutes\n").getBytes());
            out.write((features_selected[inst] + "\n").getBytes());

            // Prepare a variety of untrained classifiers to perform tests with
            Classifier[] classifiers = Coordinator.getAllUntrainedClassifiers(classifier_descriptions_list);

            // This array specifies the partitioning of the Instances for the experimentation.
            // The same partitioning will be used for all cross validations.
            int[] partition_array = CrossValidator.generatePartitionArray(folds, num_overall);
            // Perform cross-validataion with each classifier, storing the classifier
            // description and its performance and erasing the classifier

            CrossValidator cver = new CrossValidator(instances_array[inst], partition_array, folds, hierarchy, identifiers);
            cvres[inst] = CrossValidationResults.generateArray(classifier_descriptions_list);

            for (int i = 0; i < classifiers.length; i++)
            {
                // Trained Model to be used during Cross Validation
                TrainedModel trained = new TrainedModel();
                trained.classifier = classifiers[i];

                // Report current progress to standard out
                out.write(("Testing classifier " + (i + 1) + " of " + classifiers.length + "...\n").getBytes());

                // This should always be null because Experimenter coordinates the
                // saving of its own file.
                String file_name = null;

                // Cross validate this set of Instances with this Classifier
                cver.crossValidate(trained, cvres[inst], instances_array[inst],
                        out, cv_results, file_name, cvres[inst][i].classifier_descriptions,
                        save_intermediate_arffs, verbose, i);
            }

            // Store error rates of each cross validation
            double[] error_rates = new double[cvres[inst].length];
            for(int i = 0; i<cvres[inst].length;i++)
                error_rates[i] = cvres[inst][i].error_rates;

            // Find the best classification approach by comparing error rates
            StringBuffer best_results = new StringBuffer();
            int best_indice = mckay.utilities.staticlibraries.MathAndStatsMethods.getIndexOfSmallest(error_rates);
            cv_results.append("\n\n==================================================\n");
            cv_results.append("==================================================\n\n");
            best_results.append("\n\n------------------------------------------------------------------\n");
            best_results.append("---------------------------BEST RESULTS---------------------------\n");
            best_results.append("------------------------------------------------------------------\n\n");
            best_results.append("BEST CLASSIFIER: " + classifier_descriptions_list.get(best_indice));
            best_results.append("\nBEST DIMENSIONALITY REDUCTION: " + selector_descriptions[inst]);
            best_results.append("\nTIME TAKEN: " + preparation_times[0][inst] + " minutes\n");
            best_results.append(features_selected[inst] + "\n");
            best_results.append("\nBEST SUCCESS RATE: "+ df.format(100-cvres[inst][best_indice].error_rates) + "%");
            best_results.append("\nBEST ERROR RATE: " + df.format(cvres[inst][best_indice].error_rates) + "%");
            best_results.append("\nBEST STANDARD DEVIATION: " + df.format(cvres[inst][best_indice].standard_deviation) + "%");
            best_results.append("\nNUMBER OF FOLDS: " + folds + "\n");
            best_results.append("\nCONFUSION MATRIX: \n" + cvres[inst][best_indice].cross_validation_confusion_matrices);

            // Store the best classification approach so that it can be matched
            // later with the best feature selector
            best_error_rates_accross_feature_sets[inst] = error_rates[best_indice];
            best_indices[inst] = best_indice;
            best_classifiers_accross_feature_sets[inst] = best_results.toString();
            best_results.append("\n\n------------------------------------------------------------------\n");
            best_results.append("----------------------INDIVIDUAL CLASSIFIERS----------------------\n");
            best_results.append("------------------------------------------------------------------\n\n");
            best_results.append(cv_results);

            // Store the results for all classifiers for this dimensionality reduction
            // technique to disk.
            // results_base_file_name should never be null, it's default value is "experimentation results".
            if (results_base_file_name != null)
            {
                File save_file = new File(results_base_file_name + "_" + (inst + 1) + ".txt");
                FileOutputStream to = new FileOutputStream(save_file);
                DataOutputStream writer = new DataOutputStream(to);
                writer.writeBytes(best_results.toString());
            }
        }
        out.flush();

        // Find the best result accross all feature selection methods and classifiers
        int best_indice_overall = mckay.utilities.staticlibraries.MathAndStatsMethods.
                getIndexOfSmallest(best_error_rates_accross_feature_sets);

        // Prepare the final report
        StringBuffer best_results_report = new StringBuffer();
        int best_indice = best_indices[best_indice_overall];

        // Validate best found Classifier by testing the publication set
        // Get TrainedModel that was used for best classification
        TrainedModel publication_model = cvres[best_indice_overall][best_indice].trained;

        // Create new Classifier to train
        resetClassifier(publication_model);

        // Filter the instances based on the selected features if best classification
        // approach used dimensionality reduction (the last Instances object in the
        // array has no dimensionality reduction performed).
        if( best_indice_overall < best_indices.length-1)
        {
            AttributeSelection[] selects = DimensionalityReducer.getFeatureSelectors(instances,
                selectors,
                selector_descriptions_list,
                preparation_times,
                max_attribute,
                out);
            selects[best_indice_overall].SelectAttributes(instances);
            selects[best_indice_overall].reduceDimensionality(instances);
            valid = selects[best_indice_overall].reduceDimensionality(valid);
        }

        // Train and test publication set against all other instances
        Trainer.train(instances_array[best_indice_overall], publication_model);
        Instances validated = InstanceClassifier.classifyInstances(publication_model, valid, save_intermediate_arffs);

        // Get statistics
        String[] classes = CrossValidator.getClassNames(instances);
        double correct = InstanceClassifier.getCorrectCount(valid, validated);
        double success_rate = 100.0 * correct/(double)num_validation;
        double error_rate = 100.0 - success_rate;
        double[][] confusion_matrix = InstanceClassifier.getConfusionMatrix(valid, validated, classes);
        String confusion_string = InstanceClassifier.formatConfusionMatrix(confusion_matrix, classes);

        // Write validation results
        best_results_report.append("\n\n------------------------------------------------------------------\n");
        best_results_report.append("-----------VALIDATION RESULTS FOR BEST FOUND CLASSIFIER-----------\n");
        best_results_report.append("------------------------------------------------------------------\n");
        if(verbose)// Add individual classifications of the publication set to the results
        {
            String[][] names_array = new String[2][num_validation];
            // Put valid_names in second index because they are testing instances
            names_array[1] = pub_names.toArray(names_array[1]);
            best_results_report.append((CrossValidator.getClassifications(valid, validated, instances_array[best_indice_overall], names_array)));
        }
        best_results_report.append("\nBEST FOUND CLASSIFIER: " + classifier_descriptions_list.get(best_indice));
        best_results_report.append("\nDIMENSIONALITY REDUCTION: " + selector_descriptions[best_indice_overall]);
        //best_results_report.append("\nTIME TAKEN: " + preparation_times[0][best_indice_overall] + " minutes\n");
        best_results_report.append("\n" + features_selected[best_indice_overall] + "\n");
        best_results_report.append("\nSUCCESS RATE: " + df.format(success_rate) + "%");
        best_results_report.append("\nERROR RATE: " + df.format(error_rate) + "%");
        best_results_report.append("\nCONFUSION MATRIX: \n" + confusion_string);

        // Save the final report to disk
        // results_base_file_name should never be null, it defaults to "experimentation_results"
        if (results_base_file_name != null)
        {
            File save_file = new File(results_base_file_name + "_best_overall.txt");
            FileOutputStream to = new FileOutputStream(save_file);
            DataOutputStream writer = new DataOutputStream(to);
            writer.writeBytes(best_results_report.toString()+ best_classifiers_accross_feature_sets[best_indice_overall]);
        }
        return best_classifiers_accross_feature_sets[best_indice_overall] + best_results_report.toString();
    }

    /**
     * Instantiates a new instance of the Weka Classifier contained in the given
     * TrainedModel. This method is called when validating the best found classification
     * approach by testing the publication data set. The best found Classifier needs
     * to be reset and retrained to test the publication set.
     *
     * @param trained               The TrainedModel with reference to the best
     *                              found classification approach for this experiment.
     * @throws java.lang.Exception  If an error occurs.
     */
    public static void resetClassifier(TrainedModel trained)
            throws Exception
    {
        // Hold the options of the old Classifier to be applied to the new Classifier
        String[] options = trained.classifier.getOptions();
        // Find the type of the Classifier and dimensionality reduction used
        if(trained.classifier instanceof IBk)
            trained.classifier = new IBk();
        if(trained.classifier instanceof NaiveBayes)
            trained.classifier = new NaiveBayes();
        if(trained.classifier instanceof SMO)
            trained.classifier = new SMO();
        if(trained.classifier instanceof J48)
            trained.classifier = new J48();
        if(trained.classifier instanceof MultilayerPerceptron)
            trained.classifier = new MultilayerPerceptron();
        if(trained.classifier instanceof AdaBoostM1)
            trained.classifier = new AdaBoostM1();
        if(trained.classifier instanceof Bagging)
            trained.classifier = new Bagging();
        // Set options of new Classifier
        // Unsure that new Classifier is identical to given best found Classifier
        trained.classifier.setOptions(options);
    }

    /**
     * Randomly selects instances to be a part of the publication set and removes
     * them from the Instances object that will be used during experimentation. Only overall
     * instances are selected randomly, however, all subsections belonging to the selected
     * overall instances are also added to the publication set.
     *
     * @param instances         The instances from which to extract the publication
     *                          set and which will be used for the rest of the
     *                          experimentation.
     * @param pub_names         The names of the overall instances that have been
     *                          slected to be a part of the publication set.
     * @param identifiers       List of identifiers of all the instances.
     * @param hierarchy         Maps instances to their original order. Links subsections
     *                          with their corresponding overall instance.
     * @param num_validation    The number of overall instances to be set aside in the
     *                          publication set.
     * @param num_overall       The number of overall instances contained in the given set
     *                          of instances.
     * @return                  The publication set.
     */
    private Instances getPublicationSet(Instances instances, LinkedList<String> pub_names,
                String[] identifiers, String[] hierarchy, int num_validation, int num_overall)
    {
        int[] overall = CrossValidator.getIndecesOfOverallInstances(hierarchy, num_overall);
        Instances valid = new Instances(instances, num_validation);

        // Store names and indeces in LinkedLists for easier manipulation
        LinkedList <String> names_list = new LinkedList<String>();
        LinkedList <Integer> overall_indeces = new LinkedList<Integer>();
        LinkedList <Integer> indeces_list = new LinkedList<Integer>();
        for(int i=0; i<overall.length; i++ )
        {
            names_list.add(identifiers[i]);
            overall_indeces.add(overall[i]);
        }

        // Randomly select overall instances to be a part of the publication set
        for(int inst = 0; inst<num_validation; inst++)
        {
            // Randomly choose an overall instance to be part of the publication set
            int random = mckay.utilities.staticlibraries.MathAndStatsMethods.
                    generateRandomNumber(overall_indeces.size());

            /* Add the overall instance to the publication set and remove it from
            the experimentation set */
            int indece = overall_indeces.get(random).intValue();
            valid.add(instances.instance(indece));
            indeces_list.add(overall_indeces.get(random));

            // Keep track of the names of the instances being added
            pub_names.add(names_list.get(random));

            /* Get indeces of all subsections corresponding to the overall instance
            that was just added*/
            Integer[] subsection_indeces = CrossValidator.getIndecesOfSubsections
                    (hierarchy, overall_indeces.get(random).toString());

            /* Add all corresponding subsections to publication set and delete
            from experimentation set*/
            for (int i = 0; i < subsection_indeces.length; i++)
            {
                valid.add(instances.instance(subsection_indeces[i].intValue()));
                indeces_list.add(subsection_indeces[i]);
                pub_names.add(names_list.get(subsection_indeces[i].intValue()));
            }

            // Remove index and identifier from their respective arrays
            overall_indeces.remove(random);
            names_list.remove(random);
        }

        /* Create */
        Instances instances2 = new Instances(instances);
        String[] hierarchy2 = hierarchy;
        instances.delete();
        int j = 0;
        for(int i=0; i<instances2.numInstances(); i++)
        {
            if(!indeces_list.contains(new Integer(i)))
            {
                instances.add(instances2.instance(i));
                hierarchy[j] = hierarchy2[i];
                j++;
            }
        }
        if(j<hierarchy.length)
        {
            for(int k=j; k < hierarchy.length; k++)
                hierarchy[k] = null;
        }
        return valid;
    }
}
