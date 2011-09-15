/*
 * Coordinator.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace;

import ace.datatypes.*;
import weka.core.*;
import java.io.*;
import java.util.LinkedList;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.instance.Randomize;
import weka.core.converters.ArffLoader;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;

/**
 * Coordinates ACE's main functionality.
 *
 * <p> This class allows for easy access to all of ACE's main functionality from any source.
 * The graphic user interface, the command line interface, and external APIs are
 * all directed through this class to access ACE's training, classification,
 * cross validation, and experiemtnation functionality.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class Coordinator {

    /* FIELDS ****************************************************************/

    /**
     * The instances to work with.
     */
    public DataBoard data_board;

    /**
     * The path of an arff file from which to derive instances.
     * This will be null if using ACE XML files.
     * Note that it is assumed that the class attribute is the last attribute.
     */
    public String arff_path;

    /**
     * A set of Weka Instances instantiated by a call to <i>loadInstances</i>.
     * All processing methods will load instances into this field prior to
     * applying anad filters, restrictions, or dimensionality reduction and before
     * passing them to ACE's processing classes.
     */
    public Instances instances;

    /**
     * Whether or not to save training data to an arff file after parsing,
     * after thinning and, and again after feature selection, if any.
     * Useful for testing.
     */
    public boolean save_intermediate_arffs;


    /* CONSTRUCTORS **********************************************************/


    /**
     * Constructs an instance of a Coordinator object.
     *
     * @param	data_board			The instances to work with.
     * @param	arff_path			The path of an arff file to derive instances
     *						from. This will be null if using ACE XML files.
     *                                          Note that it is assumed that the
     *                                          class attribute is the last attribute.
     * @param	save_intermediate_arffs		Whether or not to save training data to an
     *						arff file after parsing, after thinning and,
     *						and again after feature	selection, if any.
     *						Useful for testing.
     */
    public Coordinator(DataBoard data_board, String arff_path, boolean save_intermediate_arffs)
    {
        this.data_board = data_board;
        this.arff_path = arff_path;
        this.save_intermediate_arffs = save_intermediate_arffs;
    }


    /* PUBLIC METHODS ********************************************************/


    /**
     * Trains a Weka Classifier based on a set of sample Instances.
     *
     * <p> NOTE: for <i>training_classifier_type</i>,
     * the codes for the types of Weka classifiers are as follows:
     * <li>Unweighted k-nn (k = 1): IBk
     * <li>Naive Bayesian (Gaussian): NaiveBayes
     * <li>Support Vector Machine: SMO
     * <li>C4.5 Decision Tree: J48
     * <li>Backdrop Neural Network: MultilayerPerceptron
     * <li>AdaBoost seeded with C4.5 Decision Trees: AdaBoostM1
     * <li>Bagging seeded with C4.5 Decision Trees: Bagging
     *
     * @param   max_class_membership_spread     The maximum ratio of instances that are permitted
     *						belonging to different classes. For example,
     *						a value of 2 means that only up to twice
     *						the number of instances belonging to the
     *						class with the smallest number of training
     *						instances may be used for training.
     *						If a class has more training instances than
     *						this number, then a randomly selected set
     *						of instances up to the maximum are selected
     *						for use in training and all others are
     *						eliminated. A value of 0 means that no
     *						maximum spread is enforced and a value
     *						of 1 enforces a unifrom distribution.
     *						Instances may be reorderd.
     * @param   max_class_membership_count      The maximum number of instances that may
     *                                          belong to any one class. If a class has more
     *                                          training instances than this number, then
     *                                          a randomly selected set of instances up to
     *                                          the maximum are selected for use in training,
     *                                          and all others are eliminated. A value of 0
     *                                          means that no maximum is enforced.
     * @param   order_randomly                  Whether or not to randomly order the training
     *						instances.
     * @param   feature_selector                The command line code specifying
     *                                          what type of dimensionality reduction
     *                                          should be performed on the instances
     *                                          prior to training.
     * @param   classifier_type                 The type of classifier to be trained.
     * @param   out                             Status update and results of dimensionality
     *                                          reduction are sent here.
     * @param   max_attribute                   If the number of attributes for a
     *                                          data set is larger than this number,
     *                                          an exhaustive search will be not be
     *                                          performed.
     * @param   verbose                         Whether or not to print a detailed
     *                                          report of the dimensionality reduction
     *                                          that was performed.
     * @throws  Exception                       If a problem occurs.
     * @return                                  InstanceClassif object that can perform classifications with
     *                                          the trained Weka Classifier.
     */
    public TrainedModel train(double max_class_membership_spread,
            double max_class_membership_count,
            boolean order_randomly,
            String feature_selector,
            String classifier_type,
            OutputStream out,
            int max_attribute,
            boolean verbose)
            throws Exception
    {
        // Load and prepare Instances
        loadInstances("Training");
        prepareTrainingInstances(max_class_membership_spread, max_class_membership_count, order_randomly);

        // This TrainedModel will contain reference to the trained Weka Classifier after training
        TrainedModel trained = new TrainedModel();

        // This empty array will contain a description of the specified type of
        // Classifier after training, however, this string will not be accessed.
        // The parameter is used only in the context of a single cross validation
        // which calls the same getOneUntrainedClassifier method.
        String[] description = new String[1];

        // Prepare the specified type of Classifier (untrained)
        trained.classifier = getOneUntrainedClassifier(classifier_type, description);

        // Perform feature selection if specified and print to given OutputStream
        if(feature_selector!=null)
        {
          performDimensionalityReduction(trained, feature_selector, max_attribute, out, verbose);
        }

        try
        {
            // Train the Classifier
            Trainer.train(instances, trained);
        }
        catch(Exception e)
        {
            throw new Exception ("ACE was unable to train the Classifier."  + "\nERROR: " + e.getMessage());
        }
        return trained;
    }

    /**
     * Classifies a set of instances with the given trained classifier.
     * Saves the results in the given file in the form of an ACE XML Classifications
     * file (if ACE XML files are used) or Weka ARFF file (if a Weka ARFF file is used).
     * If using instances from a Weka ARFF file, instances will be converted into
     * ACE datatypes prior to classification.
     *
     * @param   results_file    The name of the ACE XML classifications file or Weka
     *                          ARFF file in which to store the results.
     * @param   trained         The TrainedModel in which the trained Weka Classifier
     *                          is contained.
     * @throws  Exception       If specified results file has incorrect file extension
     *                          or if classification was unsucessful.
     * @return                  Array of SegmentedClassification objects containing
     *                          the classification for each Instance.
     */
    public SegmentedClassification[] classify(String results_file, TrainedModel trained)throws Exception
    {
        // Load Instances
        loadInstances("Testing");

        // The object in which to store the resulting classifications to return
        SegmentedClassification [] resulting_classifications;

        // Test that results file has the appropriate file extension
        // If instances are from an ARFF file, results file should have extension ".arff".
        // Likewise if instances are from ACE XML files, results file should have extension ".xml".
        if(results_file != null)
        {
            // Check to see if specified results file has appropriate extension
            String extension = mckay.utilities.staticlibraries.StringMethods.getExtension(results_file);
            if (extension == null)
                throw new Exception ("Results file must have extension \".arff\" or \".xml\".");
            if (arff_path!=null)
            {
                if (!(mckay.utilities.staticlibraries.StringMethods.getExtension(results_file).equals(".arff")))
                    throw new Exception ("sres option must have .arff extention when using arff files.");
            }
            else
                if (!(mckay.utilities.staticlibraries.StringMethods.getExtension(results_file).equals(".xml")))
                    throw new Exception ("sres option must have .xml extention when using ACE XML files.");
        }
        try
        {
            // Perform classification
            resulting_classifications = InstanceClassifier.classify(trained, data_board, instances, results_file, save_intermediate_arffs);
        }
        catch(Exception e)
        {
            if(arff_path!=null)
                throw new Exception ("Could not classify "+ arff_path + "\nERROR: " + e.getMessage());
            else
                throw new Exception ("Could not classify "+ data_board.feature_vectors[0].identifier  + "\nERROR: " + e.getMessage());
        }
        return resulting_classifications;
    }

    /**
     * Cross validates a set of Weka Instances.
     *
     * The results of the cross validation are stored in a separate CrossValidationResults Object.
     *
     * <p> NOTE: for <i>cross_validation_classifier_type</i>,
     * the codes for the types of WEKA classifiers are as follows:
     * <li>Unweighted k-nn (k = 1): IBk
     * <li>Naive Bayesian (Gaussian): NaiveBayes
     * <li>Support Vector Machine: SMO
     * <li>C4.5 Decision Tree: J48
     * <li>Backdrop Neural Network: MultilayerPerceptron
     * <li>AdaBoost seeded with C4.5 Decision Trees: AdaBoostM1
     * <li>Bagging seeded with C4.5 Decision Trees: Bagging
     *
     * @param   max_class_membership_spread The maximum ratio of instances that are permitted
     *                                      belonging to different classes. For example,
     *                                      a value of 2 means that only up to twice
     *                                      the number of instances belonging to the
     *                                      class with the smallest number of training
     *                                      instances may be used for training.
     *                                      If a class has more training instances than
     *                                      this number, then a randomly selected set
     *                                      of instances up to the maximum are selected
     *                                      for use in training and all others are
     *                                      eliminated. A value of 0 means that no
     *                                      maximum spread is enforced and a value
     *                                      of 1 enforces a unifrom distribution.
     *                                      Instances may be reorderd.
     * @param max_class_membership_count    The maximum number of instances that may
     *                                      belong to any one class. If a class has more
     *                                      training instances than this number, then
     *                                      a randomly selected set of instances up to
     *                                      the maximum are selected for use in training,
     *                                      and all others are eliminated. A value of 0
     *                                      means that no maximum is enforced.
     * @param order_randomly                Whether or not to randomly order the training
     *					    instances.
     * @param file_name                     The name of the file to which to save the
     *                                      results of this cross validation. Results
     *                                      are not saved if this is null.
     * @param classifier_type               The code specifying the type of Weka
     *                                      Classifier to train.
     * @param feature_selector              The command line code specifying the
     *                                      type of dimensionality reduction to be
     *                                      performed.
     * @param folds                         The number of cross-validation folds
     * @param max_attribute                 If the number of attributes for a
     *                                      data set is larger than this number,
     *                                      an exhaustive search will be not be
     *                                      performed.
     * @param out                           Status updates are sent here.
     * @param verbose                       Whether or not to add detailed information
     *                                      about the individual classifications
     *                                      to the results String.
     * @throws Exception                    If unable to cross validate the given instances.
     * @return                              Summary of the cross validaiton results.
     */
    public String crossValidate(double max_class_membership_spread,
            double max_class_membership_count,
            boolean order_randomly,
            String file_name,
            String classifier_type,
            String feature_selector,
            int folds,
            int max_attribute,
            OutputStream out,
            boolean verbose)
            throws Exception
    {
        // Load Instances
        loadInstances("Training");

        int num_overall = data_board.getNumOverall();
        int num_total = data_board.getNumTotal();

        String[] identifiers = new String[num_total]; // identifers for each instance
        String[] hierarchy = new String[num_total]; // maps instances to their original order

        // Fill identifiers and hierarchy arrays
        data_board.getInstanceIdentifiersAndHierarchy(num_overall, identifiers, hierarchy);
        boolean subsections = data_board.hasSections();

        // If specified, apply filters and keep track of changes to the data set
        if (max_class_membership_spread != 0.0 || max_class_membership_count != 0.0 || order_randomly || subsections)
        {
            if (verbose || subsections)
            {
            // Reserve copy of instances for keeping track of instance identifiers
            Instances reserve = new Instances(instances);

            // Apply filters and or randomization
            prepareTrainingInstances(max_class_membership_spread, max_class_membership_count, order_randomly);

            // Modify the order of the identifiers to reflect the reordering that
            // was performed on the Instances
            identifiers = getIdentifiers(reserve, identifiers, hierarchy);
            }
            else
            {
                // Prepare instances
                prepareTrainingInstances(max_class_membership_spread, max_class_membership_count, order_randomly);
            }
        }

        // Check that data set includes enough instances for cross validation
        if ( instances.numInstances() < folds)
            throw new Exception (folds + " fold cross validation cannot be performed " +
                    "on this data set because it contains only " + instances.numInstances()
                    + " instances.");

        // This trained model will contain reference to the trained Weka Classifier
        TrainedModel trained = new TrainedModel();

        // Prepare the specified type of Classifier (untrained)
        String[] description = new String[1];
        trained.classifier = getOneUntrainedClassifier(classifier_type, description);

        // Perform dimensionality reduction and print summary to OutputStream
        if (feature_selector != null)
        {
            feature_selector = performDimensionalityReduction(trained, feature_selector, max_attribute, out, verbose);
        }

        // Initialize object to perform cross validation, Instances are partitioned
        CrossValidator cver = new CrossValidator(instances, folds, identifiers, num_overall, hierarchy);
        String results = "";
        try
        {
            // Initialize data structure to store results of the cross validation
            // classifier_description field is set
            LinkedList<String> descriptions = new LinkedList<String>();
            descriptions.add(description[0]);
            CrossValidationResults[] cvres = CrossValidationResults.generateArray(descriptions);

            // Store a report for this cross validation
            StringBuffer cv_results = new StringBuffer();

            // Perform the cross validation
            results =  cver.crossValidate(trained, cvres, instances, out, cv_results,
                    file_name, feature_selector, save_intermediate_arffs, verbose, 0);
        }
        catch(Exception e)
        {
           if(arff_path!=null)
                throw new Exception ("Could not cross validate "+ arff_path + "\nERROR: " + e.getMessage());
            else
                throw new Exception ("Could not cross validate "+ data_board.feature_vectors[0].identifier + "\nERROR: " + e.getMessage());
        }
        return results;

    }


    /**
     * Experiments on a set of Weka Instances.
     *
     * <p> Cross validates the Instances with a variety of different classifiers
     * and dimensionality reduction in order to find the best classification approach.
     *
     * @param   max_class_membership_spread     The maximum ratio of instances that are permitted
     *						belonging to different classes. For example,
     *						a value of 2 means that only up to twice
     *						the number of instances belonging to the
     *						class with the smallest number of training
     *						instances may be used for training.
     *						If a class has more training instances than
     *						this number, then a randomly selected set
     *						of instances up to the maximum are selected
     *						for use in training and all others are
     *						eliminated. A value of 0 means that no
     *						maximum spread is enforced and a value
     *						of 1 enforces a unifrom distribution.
     *						Instances may be reorderd.
     * @param   max_class_membership_count      The maximum number of instances that may
     *						belong to any one class. If a class has more
     *						training instances than this number, then
     *						a randomly selected set of instances up to
     *						the maximum are selected for use in training,
     *						and all others are eliminated. A value of 0
     *						means that no maximum is enforced.
     * @param   order_randomly                  Whether or not to randomly order the training
     *						instances.
     * @param   results_base_file_name          The results of the experimentation will be stored
     *                                          in multiple files with this as the base file name.
     * @param   folds                           The number of cross-validation folds to perform.
     * @param   out                             The OutputStream to which status reports are printed.
     * @param   verbose                         Whether or not to include detailed information
     *                                          about the dimensionality reduction of the best
     *                                          found classification approach and the individual
     *                                          classifications of the validation set.
     * @param max_attribute                     If the number of attributes for a
     *                                          data set is larger than this number,
     *                                          an exhaustive search will be not be
     *                                          performed.
     * @throws  Exception                       If an error occurs during experimentation.
     * @return                                  Summary of results for best found classifier.
     *
     */
    public String experiment(double max_class_membership_spread,
            double max_class_membership_count,
            boolean order_randomly,
            String results_base_file_name,
            int folds,
            OutputStream out,
            boolean verbose,
            int max_attribute)
            throws Exception
    {
        // Load Instances
        loadInstances("Training");

        int num_overall = data_board.getNumOverall();
        int num_total = data_board.getNumTotal();
        String[] identifiers = new String[num_total];
        String[] hierarchy = new String[num_total];
        data_board.getInstanceIdentifiersAndHierarchy(num_overall, identifiers, hierarchy);
        boolean subsections = data_board.hasSections();
        // Apply filters
        if (max_class_membership_spread != 0.0 || max_class_membership_count != 0.0 || order_randomly)
        {
            if (verbose || subsections)
            {
            // Reserve copy of instances for keeping track of Instance names
            Instances reserve = new Instances(instances);

            prepareTrainingInstances(max_class_membership_spread, max_class_membership_count, order_randomly);

            // Modify the order of the identifiers to reflect the reordering that
            // was performed on the Instances
            identifiers = getIdentifiers(reserve, identifiers, hierarchy);
            }
            else
            {
                // Prepare instances without keeping track of identifiers
                prepareTrainingInstances(max_class_membership_spread, max_class_membership_count, order_randomly);
            }
        }
        // Check that data set includes enough instances for cross validation
        if ( num_overall < folds)
            throw new Exception (folds + " fold cross validation cannot be performed " +
                    "on this data set because it contains only " + instances.numInstances()
                    + " instances.");

        String best = "";
        try
        {
            // Experiment on this set of Instances
            Experimenter exp = new Experimenter(out);
            best = exp.crossValidateMultiApproaches(folds, instances, results_base_file_name,
                    save_intermediate_arffs, verbose, max_attribute, identifiers, num_overall, hierarchy);
        }
        catch(Exception e)
        {
            if(arff_path!=null)
                throw new Exception ("Could not experiment on "+ arff_path + "\nERROR: " +
                        e.getMessage());
            else
                throw new Exception ("Could not experiment on "+ data_board.feature_vectors[0].
                        identifier + "\nERROR: " + e.getMessage());
        }
        return best;
    }

    /**
     * Loads the Instances from either ACE XML files or a Weka ARFF file.
     * No restrictions or alterations are applied..
     *
     * @param relation              String describing the purpose of these Instances.
     *                              Will likely be "Training" or "Testing"
     *
     * @throws java.lang.Exception  If an error occurs.
     */
    public void loadInstances(String relation) throws Exception
    {
        if (arff_path == null)
        {
            // Load Instances from ACE XML files
            // Set attributes to use for instances overall
            String relation_name = relation + "Relation";
            int initial_capacity = 100;
            instances = data_board.getInstanceAttributes(relation_name, initial_capacity);

            // Set up individual instances
            boolean use_top_level_features = true;
            boolean use_sub_section_features = true;
            data_board.storeInstances(instances, use_top_level_features, use_sub_section_features);
        }
        else
        {
            // Load Instances from Weka ARFF file
            ArffLoader arffloader = new ArffLoader();
            arffloader.setFile(new File(arff_path));
            instances = arffloader.getDataSet();
            instances.setClassIndex(instances.numAttributes() - 1);
        }
    }

    /**
     * Prepares a set of Weka Instances for from either an arff file or ACE XML files.
     * These Instances are prepared specifically for training. This method allows
     * for instances to be altered, re-ordered, saved, or restricted prior to training.
     *
     * <p> Note that instances are re-ordered within each class if either the
     * max_class_membership_spread or max_class_membership_count parameters.
     *
     *
     * @param	max_class_membership_spread	The maximum ratio of instances that are permitted
     *						belonging to different classes. For example,
     *						a value of 2 means that only up to twice
     *						the number of instances belonging to the
     *						class with the smallest number of training
     *						instances may be used for training.
     *						If a class has more training instances than
     *						this number, then a randomly selected set
     *						of instances up to the maximum are selected
     *						for use in training and all others are
     *						eliminated. A value of 0 means that no
     *						maximum spread is enforced and a value
     *						of 1 enforces a unifrom distribution.
     *						Instances may be reorderd.
     * @param	max_class_membership_count	The maximum number of instances that may
     *						belong to any one class. If a class has more
     *						training instances than this number, then
     *						a randomly selected set of instances up to
     *						the maximum are selected for use in training,
     *						and all others are eliminated. A value of 0
     *						means that no maximum is enforced.
     * @param	order_randomly                  Whether or not to randomly order the training
     *						instances.
     * @throws Exception                        If an error is encountered.
     *
     */
    public void prepareTrainingInstances(double max_class_membership_spread,
            double max_class_membership_count,
            boolean order_randomly)
            throws Exception
    {
        // Save a snapshot of the instances
        if (save_intermediate_arffs)
            Trainer.saveInstancesAsARFF(instances, "training_data_after_parsing.arff");

        // Choose a random number to seed where needed
        int random_seed = mckay.utilities.staticlibraries.MathAndStatsMethods.generateRandomNumber(Integer.MAX_VALUE);

        // Randomly order the training samples
        if (order_randomly)
        {
            Randomize order_randomizer = new Randomize();
            order_randomizer.setRandomSeed(random_seed);
            order_randomizer.setInputFormat(instances);
            instances = SpreadSubsample.useFilter(instances, order_randomizer);
        }

        // Enforce limits on the number of training instances that may
        // belong to each class
        if (max_class_membership_spread != 0.0 || max_class_membership_count != 0.0)
        {
            SpreadSubsample class_balance_filter = new SpreadSubsample();
            class_balance_filter.setDistributionSpread(max_class_membership_spread);
            class_balance_filter.setMaxCount(max_class_membership_count);
            class_balance_filter.setRandomSeed(random_seed);
            class_balance_filter.setInputFormat(instances);
            instances = SpreadSubsample.useFilter(instances, class_balance_filter);

            // Save a snapshot of the instances
            if (save_intermediate_arffs)
            {
                Trainer.saveInstancesAsARFF(instances, "training_data_after_thinning.arff");
            }
        }
    }

    /**
     * Performs dimensionality reduction on a set of Weka Instances. If the user
     * wishes to use dimensionality reduction, this method will be called prior
     * to testing and cross validation.
     *
     * @param trained               The TrainedModel in which the Weka AttributeSelection
     *                              object for this dimensionality reduction will be stored.
     * @param feature_selector      Code specifying the type of dimensionality reduction
     *                              to be performed.
     * @param max_attribute         The maximum number of attributes permitted for
     *                              exhaustive search.
     * @param out                   Output stream to which status reports and results
     *                              will be printed.
     * @param verbose
     * @return                      A String describing the dimensionality reduction
     *                              that was performed.
     * @throws java.lang.Exception  If an error occurs.
     */
    public String performDimensionalityReduction(TrainedModel trained, String feature_selector, int max_attribute, OutputStream out, boolean verbose)
            throws Exception
    {
        // Print status update
        out.write("\nPerforming dimensionality reduction...\n".getBytes());

        // Store number of original features in order to report them later
        int original_number_features = instances.numAttributes() - 1;

        // Prepare for feature selection
        LinkedList<String> selectors = new LinkedList<String>();
        selectors.add(feature_selector);
        LinkedList<String> selector_descriptions = new LinkedList<String>();
        double[][] preparation_times = new double[1][];

        // Get Feature Selecors, selector_descriptions and preparation times will be filled
        trained.attribute_selector = DimensionalityReducer.getFeatureSelectors(instances,
                selectors,
                selector_descriptions,
                preparation_times,
                max_attribute, out)[0];

        // Select Features
        trained.attribute_selector.SelectAttributes(instances);

        // Filter the instances based on the selected features
        instances = trained.attribute_selector.reduceDimensionality(instances);

        // Save a snapshot of the instances
        if (save_intermediate_arffs)
            Trainer.saveInstancesAsARFF(instances, "training_data_after_dimensionality_reduction.arff");

        // Store and print summary of the selected features
        out.write(("Dimensionality Reduction Performed: " + selector_descriptions.getFirst() + "\n").getBytes());
        String feature_selection_report = DimensionalityReducer.getSelectedFeatureNames(trained.attribute_selector, original_number_features, instances);
        if (verbose)
            out.write(trained.attribute_selector.toResultsString().getBytes());
        else
            out.write(feature_selection_report.getBytes());
        out.write("\n".getBytes());

        return selector_descriptions.getFirst();
    }

    /**
     * Return an array of untrained but parameterized classifiers that can
     * be used for a variety of purposes. The given LinkedList will be erased
     * and filled with descriptions of each of the returned classifiers. This
     * method is called during experimentation and all Classifiers are tested.
     *
     * @param	classifier_descriptions     This list will be filled with descriptions
     *                                      of the returned classifiers. Warning: any
     *                                      pre-existing contents will be erased.
     * @return                              Classifiers that may be trained and evaluated.
     */
    public static Classifier[] getAllUntrainedClassifiers(LinkedList<String> classifier_descriptions)
    {
        // The list to store Classifiers in
        LinkedList<Classifier> classifier_list = new LinkedList<Classifier>();

        // The current classifier being added to the list
        Classifier this_classifier = null;

        // A kind of classifier to use in classifier ensembles
        Classifier component_classifier = null;

        // Delete any contents already in classifier_descriptions
        classifier_descriptions.clear();

        // Prepare a set of k-nn classifiers with varying k's and weighted and
        // not weighted by distances and by similarity
        int max_k = 10;
        for (int k = 1; k <= max_k; k++)
        {
            this_classifier = new IBk();
            ((IBk) this_classifier).setKNN(k);
            classifier_list.add(this_classifier);
            classifier_descriptions.add("k-NN  (k = " + k + ", unweighted)");

            this_classifier = new IBk();
            ((IBk) this_classifier).setKNN(k);
            ((IBk) this_classifier).setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
            classifier_list.add(this_classifier);
            classifier_descriptions.add("k-NN  (k = " + k + ", distance weighted)");

            this_classifier = new IBk();
            ((IBk) this_classifier).setKNN(k);
            ((IBk) this_classifier).setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
            classifier_list.add(this_classifier);
            classifier_descriptions.add("k-NN  (k = " + k + ", similarity weighted)");
        }

        // Prepare a Gaussian naive Bayesian classifier
        this_classifier = new NaiveBayes();
        classifier_list.add(this_classifier);
        classifier_descriptions.add("Naive Bayes  (Gaussian)");

        // Prepare a naive Bayesian classifier
        this_classifier = new NaiveBayes();
        ((NaiveBayes) this_classifier).setUseKernelEstimator(true);
        classifier_list.add(this_classifier);
        classifier_descriptions.add("Naive Bayes  (kernel estimation)");

        // Prepare a support vector machine classifier
        this_classifier = new SMO();
        classifier_list.add(this_classifier);
        classifier_descriptions.add("Support Vector Machine");

        // Prepare a C4.5 decision tree classifier
        this_classifier = new J48();
        classifier_list.add(this_classifier);
        classifier_descriptions.add("C4.5 Decision Tree");

        // Prepare a backprop neural network classifier
        this_classifier = new MultilayerPerceptron();
        classifier_list.add(this_classifier);
        classifier_descriptions.add("Backprop Neural Network");

        // Prepare an AdaBoost classifier
        this_classifier = new AdaBoostM1();
        component_classifier = new J48();
        ((AdaBoostM1) this_classifier).setClassifier(component_classifier);
        classifier_list.add(this_classifier);
        classifier_descriptions.add("AdaBoost seeded with C4.5 Decision Trees");

        // Prepare a bagging classifier
        this_classifier = new Bagging();
        component_classifier = new J48();
        ((Bagging) this_classifier).setClassifier(component_classifier);
        classifier_list.add(this_classifier);
        classifier_descriptions.add("Bagging seeded with C4.5 Decision Trees");

        // Return the set of classifiers
        return classifier_list.toArray(new Classifier[1]);
    }

    /**
     * Prepares a  single Weka Classifier. The specified type of Classifier is created but not trained.
     *
     * @param classifier_type   The type of classifier to be prepared.
     * @param description       Will be of size 1 and will store a description of
     *                          the Classifier being instantiated.
     *
     * @return                  An untrained weka classifier of the specified type.
     * @throws Exception        If invalid Classifier type was specified.
     */
    public static Classifier getOneUntrainedClassifier(String classifier_type, String[] description) throws Exception
    {
        // Prepare the specified type of Classifier
        // Prepare an unweighted k-nn classifier with a k value of one
        Classifier classifier;
        if (classifier_type.equalsIgnoreCase("IBk"))
        {
            classifier = new IBk();
            ((IBk) classifier).setKNN(1);
            description[0] = "k-NN  (k = 1, unweighted)";
        }
        // Prepare a Gaussian naive Bayesian Classifier
        else if (classifier_type.equalsIgnoreCase("NaiveBayes"))
        {
            classifier = new NaiveBayes();
            description[0] = "Naive Bayes  (Gaussian)";
        }
        // Prepare a support vector machine Classifier
        else if (classifier_type.equalsIgnoreCase("SMO"))
        {
            classifier = new SMO();
            description[0] = "Support Vector Machine";
        }
        // Prepare a C4.5 decision tree Classifier
        else if (classifier_type.equalsIgnoreCase("J48"))
        {
            classifier = new J48();
            description[0] = "C4.5 Decision Tree";
        }
        // Prepare a backprop neural network Classifier
        else if (classifier_type.equalsIgnoreCase("MultilayerPerceptron"))
        {
            classifier = new MultilayerPerceptron();
            description[0] = "Backprop Neural Network";
        }
        // Prepare an AdaBoost Classifier
        else if (classifier_type.equalsIgnoreCase("AdaBoostM1"))
        {
            // Seeded with J48 stubs
            classifier = new AdaBoostM1();
            ((AdaBoostM1) classifier).setClassifier(new J48());
            description[0] = "AdaBoost seeded with C4.5 Decision Trees";
        }
        // Prepare a Bagging Classifier
        else if (classifier_type.equalsIgnoreCase("Bagging"))
        {
            classifier = new Bagging();
            ((Bagging) classifier).setClassifier(new J48());
            description[0] = "Bagging seeded with C4.5 Decision Trees";
        }
        else // Throw exception if invalid classifier type was specified
            throw new Exception ("ERROR: " + classifier_type + " is an invalid classifier type. See help message for codes.");

        return classifier;
    }

    /**
     * Finds the identifiers for the instances after the instances have been reordered.
     * <p>This method compares the feature values of the instances in the reserve set to
     * the feature values of the instances of the newly reordered set to determine
     * which identifier corresponds to which instance. The names of the instances
     * need to be kept track of so that they can be printed when the -verbose flag
     * is specified at the command line. If multiple instances have identical feature
     * values, their identifiers will be concatenated with a slash between them and
     * the same String will be used as the identifier for each.
     *
     * @param reserve       A copy of the original instances before and filtering
     *                      or ordering was applied.
     * @param identifiers   The unique identifiers that correspond to the instances
     *                      of <i>reserve</i>.
     * @return              The same identifiers that were passed are now ordered
     *                      to correspond with the global Instances (that have been
     *                      filtered and/or reordered).
     */
    private String[] getIdentifiers(Instances reserve, String[] identifiers, String[] hierarchy)
    {
        String[] new_identifiers = new String[instances.numInstances()];
        String[] old_hierarchy = hierarchy.clone();
        for(int new_inst = 0; new_inst < instances.numInstances(); new_inst++)
        {
            for(int res = 0; res < reserve.numInstances(); res++)
            {
                String new_features = instances.instance(new_inst).toString();
                String old_features = reserve.instance(res).toString();
                // Compare features of global Instances to reserved data set to
                // find the corresponding identifier.
                if(new_features.equals(old_features))
                {
                    // Resulting identifier will contain a concatenation of all
                    // identifiers of instances that share the same features.
                    if(new_identifiers[new_inst]!=null)
                        new_identifiers[new_inst].concat("/" + identifiers[res]);
                    else
                    {
                        new_identifiers[new_inst] = identifiers[res];
                        hierarchy[new_inst] = old_hierarchy[res];
                    }
                }
            }
        }
        return new_identifiers;
    }
}
