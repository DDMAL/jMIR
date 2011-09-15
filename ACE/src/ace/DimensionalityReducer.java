/*
 * DimensionalityReducer.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace;

import java.util.LinkedList;
import java.io.*;
import weka.attributeSelection.*;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.core.*;

/**
 * Applies dimensionality reduction techniques to a set of Weka instances.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class DimensionalityReducer
{
// PROBLEM: need to make ClassifierSubsetEval not just train on its own data
// Try with cross validation
// Vary evaluators and classifiers and classifier parameters like k
// Vary search methods


	/**
	 * Returns a string detailing the features that have been selected.
	 *
	 * @param feature_selection		The feature selection to describe.
	 * @param original_number_features	The number of features before selection.
	 * @param instances			The instances to extract feature names from.
	 *					May be null, in which case a pre-generated
	 *					report will be given.
         * @return                              String detailing the features that have been selected.
         * @throws Exception                    If an error occurs.
	 */
	public static String getSelectedFeatureNames( AttributeSelection feature_selection,
                int original_number_features,
                Instances instances )
		throws Exception
	{
		String report = new String();

		if (instances != null)
		{
			int[] selected_features = feature_selection.selectedAttributes();
			String[] feature_names = new String[selected_features.length - 1];
			for (int i = 0; i < feature_names.length; i++)
			{
				Attribute this_attribute = instances.attribute(selected_features[i]);
				feature_names[i] = this_attribute.name();
			}

			report += "\nSELECTED FEATURES (" + feature_names.length + " of " + original_number_features + "):";
			for (int i = 0; i < feature_names.length; i++)
				report += "\n\t" + feature_names[i];
		}
		else
			report = feature_selection.toResultsString();

		return report + "\n";
	}

     /**
     * Perform a variety of features selection techniques on the given instances in
     * order to produce a variety of sets of instances with different selected features.
     * These features selection techniques consist of the full set of features available
     * in the given instances as well as each of the techniques specified in the
     * getFeatureSelectors method.
     *
     * @param	instances		Instances that may be used to train some
     *					feature selectors and serve as the basis
     *					for the returned sets of instances.
     * @param   selectors               List that contains the codes for the different
     *                                  dimensionality reduction that will be performed.
     *                                  In this case (experimentation) all possible
     *                                  codes will be present.
     * @param	selector_descriptions	This list will be filled with descriptions
     *					of the returned feature selectors. Warning: any
     *					pre-existing contents will be erased.
     * @param	features_selected	This list will be filled with descriptions
     *					of the features selected by each feature selector.
     *					Warning: any pre-existing contents will be erased.
     * @param	preparation_times	The array to be filled with times in seconds
     *					needed to prepare each feature selector. The
     *					first dimension should have size 1 and the
     *					second dimension should be empty.
     * @param   out                     OutputStream to which status updates will
     *                                  be written.
     * @param   verbose                 Whether or not to store detailed information
     *                                  about the dimensionality reduction that
     *                                  was performed.
     * @param max_attribute             If the number of attributes for a
     *                                  data set is larger than this number,
     *                                  an exhaustive search will be not be
     *                                  performed.
     * @return				Sets of instances based on the provided instances
     *					but after each of the feature selection techniques
     *					has been applied. The last index in the array
     *                                  will contain the original instances with no
     *                                  dimensionality reduction applied to them.
     * @throws	Exception		Throws an exception if a problem occurs.
     */
    public static Instances[] getDimensionallyReducedInstances(Instances instances,
            LinkedList<String> selectors,
            LinkedList<String> selector_descriptions,
            LinkedList<String> features_selected,
            double[][] preparation_times,
            OutputStream out,
            boolean verbose,
            int max_attribute)
            throws Exception
    {
        // Note the original amount of features
        int original_number_features = instances.numAttributes() - 1;

        // Delete any contents already in selector_descriptions or features_selected
        selector_descriptions.clear();
        features_selected.clear();

        // For finding calculation times
        long start_time;
        long stop_time;

        // Prepare the feature selectors
        out.write("\nPreparing feature selectors...".getBytes());
        AttributeSelection[] feature_selections = getFeatureSelectors(instances,
                selectors,
                selector_descriptions,
                preparation_times,
                max_attribute,
                out);

        // Find the feature selections
        Instances[] instances_dimensionally_reduced = new Instances[feature_selections.length + 1];
        Instances these_instances = null;
        for (int i = 0; i < instances_dimensionally_reduced.length; i++)
        {
            // Give users a progress report
            out.write(("\nPerforming selection " + (i + 1) + " of " + instances_dimensionally_reduced.length + "...").getBytes());

            // Add the instances with no dimensionality reduction if this is the last
            // iteration
            if (i == instances_dimensionally_reduced.length - 1)
            {
                these_instances = instances;
                selector_descriptions.add("No feature selection");

                features_selected.add(getFormattedSelectedFeatureNames(these_instances, original_number_features));

                double[][] temp_times = new double[1][preparation_times[0].length + 1];
                for (int j = 0; j < preparation_times[0].length; j++)
                {
                    temp_times[0][j] = preparation_times[0][j];
                }
                temp_times[0][preparation_times[0].length] = 0.0;
                preparation_times[0] = temp_times[0];

                instances_dimensionally_reduced[i] = these_instances;
            } // Deal with all dimensionality reducers
            else
            {
                these_instances = new Instances(instances);
                start_time = System.currentTimeMillis();
                feature_selections[i].SelectAttributes(these_instances);

                these_instances = feature_selections[i].reduceDimensionality(these_instances);
                stop_time = System.currentTimeMillis();
                if (verbose)
                    features_selected.add(feature_selections[i].toResultsString());
                else
                    features_selected.add(getFormattedSelectedFeatureNames(these_instances, original_number_features));

                preparation_times[0][i] += (((double) (stop_time - start_time)) / 60000.0);

                instances_dimensionally_reduced[i] = these_instances;
            }
        }
        return instances_dimensionally_reduced;
    }


        /**
	 * Returns a formatted string describing the features available in the given
	 * instances.
	 *
	 * @param	instances			The instances to get feature names from.
         * @param	original_number_features	Number of features previously in instances
         * @return      Description of the features available in the given instances.
	 */
	public static String getFormattedSelectedFeatureNames( Instances instances,
		                                                 int original_number_features )
	{
		int num_features = instances.numAttributes() -1;
		String results = "SELECTED FEATURES (" + num_features + " of " + original_number_features + "):\n";
		for (int i = 0; i < num_features; i++)
			if (i != instances.classIndex())
				results += ("\t" + instances.attribute(i).name() + "\n");
		return results + "\n";
	}

        /**
	 * Return an array of AttributeSelection objects that can be applied to instances.
         * This method is called by <i>getDimensionallyReducedInstances</i> during experimentation
         * and returns an array containing each available type of AttributeSelection object.
         * This method is also called directly from <i>Trainer</i> but only an array of size one
         * will be returned and will contain the type of AttributeSelection object that was
         * specified.
         *
	 * Note that the given instances are used for training only, and that no dimensionality
	 * reduction is applied to them. The given selector_descriptions LinkedList will be erased and filled with
	 * descriptions of each of the returned feature selectors.
	 *
	 * @param	instances		Instances that may be used to train some
	 *					feature selectors. These are not altered
	 *					by this method.
         * @param       selectors               Contains codes specifying which
         *                                      AttributeSelection objects to be
         *                                      prepared. If called by Experimenter,
         *                                      all possible AttributeSelection objects
         *                                      will be specified. If called by Trainer
         *                                      only one will be specified.
	 * @param	selector_descriptions	This list will be filled with descriptions
	 *					of the returned feature selectors. Warning: any
	 *					pre-existing contents will be erased.
	 * @param	preparation_times	The array to be filled with times in seconds
	 *					needed to prepare each feature selector. The
	 *					first dimension should have size 1 and the
	 *					second dimension should be empty.
         * @param       max_attribute           If the number of attributes for a
         *                                      data set is larger than this number,
         *                                      an exhaustive search will be not be
         *                                      performed.
         * @param       out                     OutputStream to which warning is
         *                                      printed if exhaustive search will
         *                                      not be performed.
         * @return				AttributSelection objects that can be applied
	 *					to instances for dimensionality reduction.
	 * @throws	Exception		Throws an exception if a problem occurs.
	 */
	public static AttributeSelection[] getFeatureSelectors( Instances instances,
                                                                LinkedList<String> selectors,
	                                                        LinkedList<String> selector_descriptions,
								double[][] preparation_times,
                                                                int max_attribute, OutputStream out)
		throws Exception
	{
		// The list of feature selectors to apply
		LinkedList<AttributeSelection> feature_selectors = new LinkedList<AttributeSelection>();

		// The list of preparation times and related variables
		LinkedList<Double> preparation_times_list = new LinkedList<Double>();
		long start_time;
		long stop_time;

		// Delete any contents already in selection_descriptions
		selector_descriptions.clear();

		// The current search method to use for finding the reduced feature set
		ASSearch this_search_method = null;

		// The current way to evaluate feature subsets
		ASEvaluation this_evaluation_method = null;

		// The current classifier that may be used for feature subset evaluation
		Classifier this_evaluation_classifier = null;

		// The current feature selector
		AttributeSelection this_feature_selector = null;

                // Go through each code, prepare and add appropriate feature selector if present
                if(selectors.contains("PCA"))
                {
		// Prepare a PCA AttributeSelection
                    start_time = System.currentTimeMillis();
                    this_search_method = new Ranker();
                    this_evaluation_method = new PrincipalComponents();
                    this_feature_selector = new AttributeSelection();
                    this_feature_selector.setSearch(this_search_method);
                    this_feature_selector.setEvaluator(this_evaluation_method);
                    stop_time = System.currentTimeMillis();
                    feature_selectors.add(this_feature_selector);
                    selector_descriptions.add("Principal Components Analysis (PCA)");
                    preparation_times_list.add(new Double(((double) (stop_time - start_time)) / 60000.0));
                }

                if(selectors.contains("EXB"))
                {
		// Prepare an exhaustive search using a naive Bayesian evaluator if there
		// are 6 or less features
                    if (instances.numAttributes() <= max_attribute)
                    {
			start_time = System.currentTimeMillis();
			this_search_method = new ExhaustiveSearch();
			this_evaluation_method = new ClassifierSubsetEval();
			this_evaluation_classifier = new NaiveBayes();
			((ClassifierSubsetEval) this_evaluation_method).setClassifier(this_evaluation_classifier);
			((ClassifierSubsetEval) this_evaluation_method).buildEvaluator(instances);
			((ClassifierSubsetEval) this_evaluation_method).setUseTraining(true);
			this_feature_selector = new AttributeSelection();
			this_feature_selector.setSearch(this_search_method);
			this_feature_selector.setEvaluator(this_evaluation_method);
			stop_time = System.currentTimeMillis();
			feature_selectors.add(this_feature_selector);
			selector_descriptions.add("Exhaustive search using naive Bayesian classifier");
			preparation_times_list.add(new Double(((double) (stop_time - start_time)) / 60000.0));
                    }
                    else
                        out.write(("WARNING: Exhaustive search will not be performed because data has more than " + max_attribute + " attributes.").getBytes());
                }

                if(selectors.contains("GNB"))
                {
		// Prepare a genetic search using a naive Bayesian evaluator
                    start_time = System.currentTimeMillis();
                    this_search_method = new GeneticSearch();
                    this_evaluation_method = new ClassifierSubsetEval();
                    this_evaluation_classifier = new NaiveBayes();
                    ((ClassifierSubsetEval) this_evaluation_method).setClassifier(this_evaluation_classifier);
                    ((ClassifierSubsetEval) this_evaluation_method).buildEvaluator(instances);
                    ((ClassifierSubsetEval) this_evaluation_method).setUseTraining(true);
                    this_feature_selector = new AttributeSelection();
                    this_feature_selector.setSearch(this_search_method);
                    this_feature_selector.setEvaluator(this_evaluation_method);
                    stop_time = System.currentTimeMillis();
                    feature_selectors.add(this_feature_selector);
                    selector_descriptions.add("Genetic search using naive Bayesian classifier");
                    preparation_times_list.add(new Double(((double) (stop_time - start_time)) / 60000.0));

                    // Store the preparation times
                    Double[] times = preparation_times_list.toArray(new Double[1]);
                    preparation_times[0] = new double[times.length];
                    for (int i = 0; i < preparation_times.length; i++)
                    preparation_times[0][i] = times[i].doubleValue();
                }

		// Return the feature selection
		return feature_selectors.toArray(new AttributeSelection[1]);
	}
}