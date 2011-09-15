/*
 * CommandLine.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */
package ace;

import ace.datatypes.*;
import ace.xmlparsers.*;
import weka.core.*;
import java.io.*;
import java.util.LinkedList;


/**
 * Parses and processes the flags and options given at the command line.
 *
 * Upon instantiation, this class reads all commands from the command line and stores
 * them as public fields. These fields are accessed by <i>Main</i> to determine
 * whether ACE should be run from the GUI or the CLI. The method <i>processRequests</i>
 * will direct the flow of execution if <i>Main</i> decides that ACE should be run
 * from the command line.
 *
 * <p> NOTE: for <i>training_classifier_type</i> and <i>cross_validation_classifier_type</i>,
 * the codes for the types of WEKA classifiers are as follows:
 * <li>Unweighted k-nn (k = 1): IBk
 * <li>Naive Bayesian (Gaussian): NaiveBayes
 * <li>Support Vector Machine: SMO
 * <li>C4.5 Decision Tree: J48
 * <li>Backdrop Neural Network: MultilayerPerceptron
 * <li>AdaBoost seeded with C4.5 Decision Trees: AdaBoostM1
 * <li>Bagging seeded with C4.5 Decision Trees: Bagging
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class CommandLine
        implements Serializable
{
    /* FIELDS ****************************************************************/

    // General Options

    /**
     * The type of Weka Classifier to be trained.
     */
    public String classifier_type;

    /**
     * The file to which the results or the TrainedModel (depending on which
     * processing is being performed) will be saved.
     */
    public String save_file;

    /**
     * A code specifying what type of dimensionality reduction to be performed.
     */
    public String feature_selector;

    /**
     * If true, instances are ordered randomly before training, cross validation, or experimenting.
     */
    public boolean order_randomly = false;

    /**
     * The maximum ratio of instances that are permitted belonging to different classes.
     */
    public double max_spread = 0.0;

    /**
     * The maximum number of instances that may belong to any one class.
     */
    public double max_membership = 0.0;

    /**
     * Whether or not to save training or testing data to an arff file after parsing, after
     * thinning, and again after feature selection, if any.
     */
    public boolean save_intermediate_arffs = false;

    // Loading Options

    /**
     * Specifies the path to the ACE XML project file if loading data from an ACE
     * project file or an ACE zip file.
     */
    public String project_path;

    /**
     * Specifies the path to the ACE zip file if loading data from an ACE zip file.
     */
    public String load_zip;

    /**
     * The path to the ACE XML taxonomy file for this project.
     */
    public String taxonomy_file;

    /**
     * The path to the ACE XML feature definitions file for this project.
     */
    public String feature_key_file;

    /**
     * The path to the ACE XML classifications file for this project.
     */
    public String classifications_file;

    /**
     * Array of paths to ACE XML feature vectors files for this project.
     */
    public String[] feature_vector_files;

    /**
     * If Instances are being loaded from an ARFF file, this specifies the path
     * to that file.
     */
    public String arff_file;

    // Processing Options

    /**
     * Whether or not training is to be performed.
     */
     public boolean train = false;

    /**
     * The name of file from which the TrainModel object to be used for classification
     * is to be read.
     */
    public String testing_to_load_classifiers_file;

    /**
     * Specifies the number of folds to be used in cross validation.
     */
    public String cross_validation_string;

    /**
     * Specifies the number of folds to be used for cross validation during experimentation.
     */
    public String experimentation_string;

    /**
     * Specifies whether or not to include detailed information in the results output.
     */
    public boolean verbose = false;

    // Zip Utilities
    /**
     * The name of the previously existing zipfile to be edited or the name of the
     * new zipfile to be created.
     */
    public String zip_file;

    /**
     * The type of ACE XML file to be extracted from a previously existing ACE zip file.
     */
    public String file_type;

    /**
     * True if the user has specified that an ACE zip file is to be decompressed.
     */
    public boolean unzip;

    /**
     * The file(s) or folder(s) to be included in the ACE zipfile to be created.
     */
    public String[] dozip = null;

    /**
     * The file(s) or folder(s) to be added to the specified previously existing
     * ACE zip file.
     */
    public String[] zip_add;

    /**
     * The name of the single file that the user would like to exract from the specified
     * zip file.
     */
    public String zip_extract;

    /**
     * The directory to which the zip file (if specified) will be unzipped.
     * If not specified at the command line, the default directory name is simply
     * the name of the zip file with the extension removed.
     */
    public String zip_directory;


    /* CONSTRUCTOR ***********************************************************/


    /**
     * Reads options from the command line and sets fields accordinly.
     * Instantiates objects needed for training and testing operations.
     * Variables are set to null if their value is not specified at the command line.
     * This constructor only parses the command line arguments, no actual
     * processing occurs other than the decompressing and parsing of ACE zip
     * files and the parsing of ACE project files.
     *
     * @param options       The command line arguments.
     * @throws Exception    If invalid command line arguments are used.
     */
    public CommandLine(String[] options)
            throws Exception
    {
        // Zip Utilities
        zip_file = Utils.getOption("zipfile", options);
        if (zip_file.length() == 0)
            zip_file = null;
        file_type = Utils.getOption("filetype", options);
        if (file_type.length() == 0)
            file_type = null;
        zip_directory = Utils.getOption("zip_dir", options);
        if (zip_directory.length() == 0)
            zip_directory = null;
        //zip_add = Utils.getOption("zip_add", options);
        //if(zip_add.length() == 0)
            //zip_add = null;
        zip_extract = Utils.getOption("zip_extract", options);
        if (zip_extract.length() == 0 && file_type == null)
            zip_extract = null;
        unzip = Utils.getFlag("unzip", options);

        if (Utils.getFlag("dozip", options))
        {
            dozip = mckay.utilities.staticlibraries.StringMethods.removeEmptyStringsFromArray(options);
        }
        if (Utils.getFlag("zip_add", options))
        {
            zip_add = mckay.utilities.staticlibraries.StringMethods.removeEmptyStringsFromArray(options);
        }

        // zip Exceptions
        if(file_type != null && unzip)
            throw new Exception ("The -filename command is only to be used in conjunction with the -zip_extract command.");
        if (((dozip != null || unzip ) && (zip_add != null|| zip_extract != null))
                            || ((dozip != null || zip_add != null) && (unzip  || zip_extract != null)))
                        throw new Exception ("You may only specify one zip operation at a time.");
        if ((unzip || zip_extract != null || dozip != null || zip_add != null) && zip_file == null)
            throw new Exception ("Please specify name of zipfile using the -zip_file flag.");


        // Set zip defaults
        if ((unzip || zip_extract != null) && zip_directory == null)
        {
            if(mckay.utilities.staticlibraries.StringMethods.getExtension(zip_file) != null)
                zip_directory = mckay.utilities.staticlibraries.StringMethods.removeExtension(zip_file);
            else
                zip_directory = zip_file;
            /*DateFormat dateFormat = new SimpleDateFormat("yyy/MM/dd HH:mm:ss");
            Date date = new Date();
            zip_directory = "zipped_" + dateFormat.format (date) + ".zip";*/
        }

        // General options
            if (Utils.getFlag("help", options))
            {
                printHelpMessage();
                System.exit(0);
            }
            classifier_type = Utils.getOption("learner", options);
            if(classifier_type.length() == 0)
                classifier_type = null;
            save_file = Utils.getOption("sres", options);
            if (save_file.length() == 0)
                save_file = null;
            String max_spread_option = Utils.getOption("max_spread", options);
            String max_membersip_option = Utils.getOption("max_memb", options);
            feature_selector = Utils.getOption("dr", options);
            if (feature_selector.length() == 0)
                feature_selector = null;
            if (max_spread_option.length() != 0)
                max_spread = (new Double(max_spread_option)).doubleValue();
            if (max_membersip_option.length() != 0)
                max_membership = (new Double(max_membersip_option)).doubleValue();
            if (Utils.getFlag("rand_ord", options))
                order_randomly = true;
            if (Utils.getFlag("sarff", options))
                save_intermediate_arffs = true;
            if (Utils.getFlag("verbose", options))
                verbose = true;

            // Loading Options
            // Load previously saved ACE project
             project_path = Utils.getOption("proj", options);
            if (project_path.length() == 0)
                project_path = null;
            load_zip = Utils.getOption("lzip", options);
            if (load_zip.length() == 0)
                load_zip = null;

            // Load ARFF file
            arff_file = Utils.getOption("arff", options);
            if (arff_file.length() == 0)
                arff_file = null;

            // Load ACE XML files
            // Load zero to one taxonomy file
            taxonomy_file = Utils.getOption("ltax", options);
            if (taxonomy_file.length() == 0)
                taxonomy_file = null;
            // Load zero or more feature key file(s) options
            String[] feature_key_files;
            feature_key_file = Utils.getOption("lfkey", options);
            if (feature_key_file.length() == 0)
            {
                feature_key_files = null;
                feature_key_file = null;
            }/* to be de-commented when we want to allow multiple feature key files
            else
            {
                LinkedList<String> feature_key_file_list = new LinkedList<String>();
                while (!(feature_key_file.equals("")))
                {
                    feature_key_file_list.add(feature_key_file);
                    feature_key_file = Utils.getOption("lfvec", options);
                }
                feature_key_files = feature_key_file_list.toArray(new String[1]);
            }*/

            //Load zero or more classifications file(s) options
            String[] classifications_files;
            classifications_file = Utils.getOption("lmclas", options);
            if (classifications_file.length() == 0)
            {
                classifications_files = null;
                classifications_file = null;
            }/* to be de commented when we wish to allow multiple classifications files
            else
            {
                LinkedList<String> classifications_file_list = new LinkedList<String>();
                while (!(classifications_file.equals("")))
                {
                    classifications_file_list.add(classifications_file);
                    classifications_file = Utils.getOption("lmclas", options);
                }
                classifications_files = classifications_file_list.toArray(new String[1]);
            }*/
            // Load zero or more feature vector file(s) options
            String feature_vector_file = Utils.getOption("lfvec", options);
            if (feature_vector_file.length() == 0)
                feature_vector_files = null;
            else
            {
                LinkedList<String> feature_vector_file_list = new LinkedList<String>();
                while (!(feature_vector_file.equals("")))
                {
                    feature_vector_file_list.add(feature_vector_file);
                    feature_vector_file = Utils.getOption("lfvec", options);
                }
                feature_vector_files = feature_vector_file_list.toArray(new String[1]);
            }

            // Training options
            if(Utils.getFlag("train", options))
                train = true;

            // Testing options
            testing_to_load_classifiers_file = Utils.getOption("classify", options);
            if (testing_to_load_classifiers_file.length() == 0)
                testing_to_load_classifiers_file = null;

            // Cross Validation options
            cross_validation_string = Utils.getOption("cv", options);
            if (cross_validation_string.length() == 0)
                cross_validation_string = null;

            // Experimentation options
            experimentation_string = Utils.getOption("exp", options);
            if (experimentation_string.length() == 0)
                experimentation_string = null;

            // Throw exception if needed options not specified
            if (train == true && testing_to_load_classifiers_file != null)
                throw new Exception("Must specify either training or testing, not both.");
            if ((train == true || testing_to_load_classifiers_file != null) && (experimentation_string != null || cross_validation_string != null))
                throw new Exception("May not train or test if cross-validating or experimenting.");
            if(cross_validation_string != null && experimentation_string != null)
                throw new Exception("Must specify either cross validation or experimentation, not both");
            if(cross_validation_string != null && classifier_type == null)
                throw new Exception("Must specify number of folds AND Classifier type.");
            if(train == true && classifier_type == null)
                throw new Exception ("Must specify Classifier type");
            if(train == true && save_file == null)
                throw new Exception ("Must specify file name of trained Classifier.");
            if(project_path != null && load_zip != null)
                throw new Exception ("May either specify ACE project file or ACE zip file, not both.");
            if(arff_file != null && (project_path != null || feature_vector_files != null || load_zip != null))
                throw new Exception("Please specify either a Weka ARFF file or ACE XML files, not both.");
            if((project_path!=null || load_zip != null) && feature_vector_files != null)
                throw new Exception ("Unable to load individual ACE XML files when loading " +
                        "project from an ACE project file or ACE zip file");

            // Load ACE XML from project file or zip file
             if(load_zip != null)
             {
                 // Give the zip directory the same name as the specified zipfile if no directory name was specified
                 String temp_directory = "TEMPZIP";
                 project_path = ParseACEZipFile.parseZip(load_zip, temp_directory);
             }
             if(project_path != null)
             {
                 // Get ACE XML file paths from Project object
                 Project project = new Project();
                 project.parseProjectFile(project_path);
                 taxonomy_file = project.taxonomy_path;
                 feature_key_file = project.feature_settings_paths[0];
                 classifications_file = project.classification_paths[0];
                 // Default to only one feature vector file for now until we introduce multiple file functionality
                 feature_vector_files = new String[1];
                 feature_vector_files[0] = project.feature_vectors_paths[0];
             }
    }


    /* PUBLIC METHODS ********************************************************/


    /**
     * Performs actions based on the options specified at the command line.
     * Uses the fields that were set in the constructor of this class to direct the flow of execution.
     * This method is called by <i>Main</i> if it decidedes (based on the fields of this class)
     * that ACE should be run from the command line (as opposed to the GUI).
     * This method is never called by the GUI.
     *
     * @throws Exception       If invalid input files were specified.
     */
     public void processRequests()
             throws Exception
     {
         // Throw exception if needed options not specified
         if (arff_file == null)
         {
             if (feature_key_file == null)
                 throw new Exception("No feature definitions specified.");
             if (feature_vector_files == null || feature_vector_files[0] == null)
                 throw new Exception("No feature values specified.");
         }

         // Coordinates the training, classification, cross validation, and
         // experimentation of the given data.
         Coordinator doer = null;

         try
         {
             DataBoard data_board = null;

             // Load Instances into DataBoard object
             // Parse the XML files
             if (arff_file == null)
                 data_board = new DataBoard(taxonomy_file,
                        feature_key_file,
                        feature_vector_files,
                        classifications_file);
             else
                 // if using data from an ARFF file, instances are converted to ACE datatypes stored in this data_board
                 data_board = new DataBoard(arff_file);

            //Initialize Coordinator object
            doer = new Coordinator(data_board, arff_file, save_intermediate_arffs);

            // Set maximum number of attributes permitted for Exhaustive Search
            // Exhasutive search is not permitted to be performed on data sets with
            // more attributes than this because it will cause ACE to run very slowly.
            int max_attribute = 6;

            // TRAIN THE CLASSIFIER
            if (train)
            {
                // Status updates will be sent to standard out.
                OutputStream out = System.out;

                // Train the classifier
                TrainedModel trained = doer.train(max_spread,
                        max_membership,
                        order_randomly,
                        feature_selector,
                        classifier_type,
                        out,
                        max_attribute,
                        verbose);

                // Save the classifier
                File saved = new File (save_file);
                FileOutputStream save_stream = new FileOutputStream(saved);
      System.out.println("save_file" + save_file);
                ObjectOutputStream object_stream = new ObjectOutputStream(save_stream);
                object_stream.writeObject(trained);
                object_stream.flush();
                save_stream.close();

                // Confirm success
                System.out.println("\nClassifier succesfully trained.\n");
            }

            // PERFORM CLASSIFICATIONS
            else if (testing_to_load_classifiers_file != null)
            {
                TrainedModel trained;
                // Load the trained classifier
                try
                {
                    FileInputStream load_stream = new FileInputStream(new File(testing_to_load_classifiers_file));
                    ObjectInputStream object_stream = new ObjectInputStream(load_stream);
                    trained = (TrainedModel) object_stream.readObject();
                    load_stream.close();
                }
                catch (IOException e)
                {
                    throw new IOException("Invalid classifier file: " + testing_to_load_classifiers_file);
                }

                // Perform the classification
                SegmentedClassification[] resulting_classifications = doer.classify(save_file, trained);

                // Print classifications to standard out, including subsections
                // Print the success rates if model classifications are available
                System.out.println("\n");
                if (doer.data_board.model_classifications != null)
                {
                    StringBuffer out = new StringBuffer();
                    String success = InstanceClassifier.getSuccessString(doer.data_board.model_classifications,
                            resulting_classifications, out);
                    System.out.println(out + success);
                }
                else
                {
                    for (int i = resulting_classifications.length - 1; i >= 0; i--)
                    {
                        if (resulting_classifications[i].sub_classifications != null)
                            for (int j = 0; j < resulting_classifications[i].sub_classifications.length; j++)
                            // Output is formatted such that sub-sections are indented more than their top level instance
                            {
                                System.out.println("\t\tSUBSECTION: " + resulting_classifications[i].sub_classifications[j].start + " to " +
                                        resulting_classifications[i].sub_classifications[j].stop +
                                        " \n\t\t\tCLASSIFICATION: " + resulting_classifications[i].sub_classifications[j].classifications[0]);
                            }
                        else
                            System.out.println("INSTANCE: " + resulting_classifications[i].identifier +
                                    " \n\tCLASSIFICATION: " + resulting_classifications[i].classifications[0]);
                    }
                }
            }

            // PERFORM CROSS VALIDATION
            else if (cross_validation_string != null)
            {
                String file_name = save_file;
                int number_folds = (new Integer(cross_validation_string)).intValue();
                if (number_folds < 2)
                    throw new Exception("Must be at least 2 cross-validation folds");
                OutputStream out = System.out;
                // Results are always printed to standard out
                System.out.println(doer.crossValidate(max_spread,
                        max_membership,
                        order_randomly,
                        file_name,
                        classifier_type,
                        feature_selector,
                        number_folds,
                        max_attribute,
                        out,
                        verbose));
            }

            // PERFORM EXPERIMENTATION
            else if (experimentation_string != null)
            {
                // Default results base file name is changed if another name is specified in the command line
                String file_name = "experimentation_results";
                if(save_file != null)
                {
                    file_name = save_file;
                    // Remove extension if present
                    if(mckay.utilities.staticlibraries.StringMethods.getExtension(file_name) != null)
                        file_name = mckay.utilities.staticlibraries.StringMethods.removeExtension(file_name);
                }

                int number_folds = (new Integer(experimentation_string)).intValue();
                if (number_folds < 2)
                    throw new Exception("Must be at least 2 cross-validation folds");
                // Progress will be written to standard out
                OutputStream output = System.out;
                String out = doer.experiment(max_spread,
                        max_membership,
                        order_randomly,
                        file_name,
                        number_folds,
                        output,
                        verbose,
                        max_attribute);
                System.out.println(out);
            } // Throw an exception if neither training nor testing nor cross-validation nor experimentation is specified.
            else
                throw new Exception("Neither training nor testing nor cross-validation nor experimentation specified.");
        }
        catch (Exception e)
        {
            System.out.println("ERROR: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
     }

     /**
      * Passes the appropriate command line options to the zip utility method to
      * be used.
      * This method is called when any of the zip utilities are being used.
      *
      * @throws Exception  If an error occurs.
      */
     public void processZip()
             throws Exception
     {
         if(dozip != null)
         {
             ParseACEZipFile.saveZip(dozip, zip_file);
         }
         else if (unzip)
             ParseACEZipFile.parseZip(zip_file, zip_directory);
         else if (zip_add != null)
              ParseACEZipFile.add(zip_file, zip_add);
         else if (zip_extract != null)
             ParseACEZipFile.extract(zip_extract, zip_file, file_type, zip_directory);


     }

     /**
      * Prints a help message to standard out with brief instructions redarding
      * the use of ACE from the command line.
      * All possible command line flags are listed and described.
      */
     public static void printHelpMessage()
     {
         System.out.println("\nThis is the ACE classification system.\n"+
                "\nThis utility loads a taxonomy, set of feature " +
                "definitions, feature definitions and/or model " +
                "classifications and uses them to train a, " +
                "classifier or extract a classification from " +
                "a trined classifier.\n\nThe command line " +
                "options are as follows:\n\n" +
                "GENERAL OPTIONS:\n" +
                "-help: Dispay a guide to this utility. No option is needed\n" +
                "\nZIP UTILITIES:\n" +
                "-dozip: Compress following list of files into a zip file. -zipfile" +
                "flag is required.\n" +
                "-unzip: Decompress entire contents of given zip file. The -zipfile " +
                "flag is required.\n" +
                "-zip_add: Add a single file or single directory to the given zip file." +
                "The -zipfile flag is required\n" +
                "-zip_extract: Extract a single file or all files of a specified ACE " +
                "XML type (if -filetype flag is present) from the given zip file." +
                "The -zipfile flag is required.\n" +
                "-filetype: Specifies the type of files to be extracted. This flag" +
                "is only used in conjunction with the -extract flag. All files of " +
                "the specified type will be extracted from the given zip file. \n" +
                "Accepted file types are:\n" +
                "\t>\"taxonomy_file\"\n" +
                "\t>\"feature_key_file\"\n" +
                "\t>\"feature_vector_file\"\n" +
                "\t>\"classifications_file\"\n" +
                "\t>\"project_file\"\n" +
                "-zipfile: \n" +
                "\tWhen decompressing: the zip file from which to extract.\n" +
                "\tWhen compressing: the name of the zip file into which files " +
                "will be compressed." +
                "-zip_dir: The directory into which the files should be extracted.\n" +
                "\nLOADING OPTIONS:\n" +
                "-proj: Automatically load an ACE project from the ACE XML project " +
                "file specified by the single opiton.\n" +
                "-lzip: Load an ACE project from an ACE zip file. Option is " +
                "the path name of zip file.\n" +
                "-ltax: Load the specified taxonomy_file XML file.\n" +
                "-lfkey: Load the specified feature_key_file XML file.\n" +
                "-lfvec: Load the specified feature_vector_file XML file(s).\n" +
                "-lmclas: Load the specified classifications_file XML file(s).\n" +
                "-arff: Load training or testing data from an ARFF file instead of XML files(s)." +
                "Note that it is assumed that the class attribute is the last attribute.\n" +
                "\nTRAINING OPTIONS:\n" +
                "-train: Train the Classifier\n" +
                "-learner: (required flag) Specify the type of Classifier to be trained.\n" +
                "Types of classifiers can be specified in accordance to the following codes:\n" +
                "\t>Unweighted k-nn (k = 1): IBk\n" +
                "\t>Naive Bayesian: NaiveBayes\n" +
                "\t>Support Vector Machine: SMO\n" +
                "\t>C4.5 Decision Tree: J48\n" +
                "\t>Backprop Neural Network: MultilayerPerceptron\n" +
                "\t>AdaBoost: AdaBoostM1" +
                "\t>AdaBoost: Bagging\n" +
                "-sres: (required flag) Specify the name of the file in which the trained classifier will be saved.\n" +
                "-dr: Takes single option specifying the type of dimensionality reduction to be performed.\n" +
                "If null, no dimensionality reduction will be performed.\n" +
                "Codes for feature selectors are as follows:\n" +
                "\t>Principal Componants: PCA\n" +
                "\t>Exhaustive search using naive Bayesian classifier: EXB\n" +
                "\t>Genetic search using naive Bayesian classifier: GNB\n" +
                "-sarff: Saves training data to an arff file after parsing, after" +
                "thinning and again after feature selection, if any. Useful for testing.\n" +
                "-max_spread: The maximum ratio between the number of training instances" +
                "belonging to any class compared to the least populous class.\n" +
                "-max_memb: The maximum number of training instances that may belong to each class.\n" +
                "-rand_ord: The presence of this flag causes training instances to be randomly reordered.\n" +
                "-verbose: Prints detailed information about the dimensionality reduction that was performed.\n" +
                "\nCLASSIFYING OPTIONS:\n" +
                "-classify: Perform classifications using a trained classifier." +
                "Load the given classifier info to perform the classification.\n" +
                "-sarff: Saves testing data to an arff file after parsing and again " +
                "after feature selection, if any. Useful for testing.\n" +
                "-sres: Save the test results in an ACE XML classifications file" +
                "or an arff file, depending on the filetype of the input data.\n" +
                "\nCROSS-VALIDATING OPTIONS:\n" +
                "-cv: Perform a cross validation. Must specify number of folds as option.\n" +
                "-learner: (required flag) Specify the type of Weka Classifier to be used during cross validation\n" +
                "Types of classifiers can be specified in accordance to the following codes:\n" +
                "\t>Unweighted k-nn (k = 1): IBk\n" +
                "\t>Naive Bayesian: NaiveBayes\n" +
                "\t>Support Vector Machine: SMO\n" +
                "\t>C4.5 Decision Tree: J48\n" +
                "\t>Backprop Neural Network: MultilayerPerceptron\n" +
                "\t>AdaBoost: AdaBoostM1\n" +
                "\t>AdaBoost: Bagging\n" +
                "-sres: Saves results in a text file with the given name. If not " +
                "present, results are only printed to standard out.\n" +
                "-fs: Takes single option specifying the type of dimensionality to be performed." +
                "If null, no feature selection will be performed.\n" +
                "Codes for feature selectors are as follows:\n" +
                "\t>Principal Componants: PCA\n" +
                "\t>Exhaustive search using naive Bayesian classifier: EXB\n" +
                "\t>Genetic search using naive Bayesian classifier: GNB\n" +
                "-sarff: Saves training data to an arff file after parsing, after " +
                "thinning and again after feature selection, if any. Useful for testing.\n" +
                "-max_spread: The maximum ratio between the number of training instances " +
                "belonging to any class compared to the least populous class.\n" +
                "-max_memb: The maximum number of training instances that may belong to each class.\n" +
                "-rand_ord: The presence of this flag causes training instances to be randomly reordered.\n" +
                "-verbose: The results for the paritioning and classification of each individual instance is printed and saved " +
                "as well as detailed information about the dimensionality reduction " +
                "that was performed. Incorrect classifications are marked with an asterix.\n" +
                "\nEXPERIMENTATION OPTIONS\n" +
                "-exp: Perform a cross-validation and output the results to standard out. Specifies the number of cross-validation folds.\n" +
                "-sres: Saves results in files with the given base file name. If not present, results are saved with default base file name.\n" +
                "-sarff: Saves training data to an arff file after parsing, after thinning and again after feature selection, if any. Useful for testing.\n" +
                "-max_spread: The maximum ratio between the number of training instances belonging to any class compared to the least populous class.\n" +
                "-max_memb: The maximum number of training instances that may belong to each class.\n" +
                "-rand_ord: The presence of this flag causes training instances to be randomly reordered.\n" +
                "-verbose: The presence of this flag causes extra information about the dimensionality reduction to be printed and saved.");
     }

     /**
      * Prints all command line options to standard out. Useful for testing.
      */
     public void dumpContents()
     {
         System.out.println("project_path: " + project_path);
         System.out.println("zip_file: " + zip_file);
         System.out.println("zip_directory: " + zip_directory);
         System.out.println("train: " + train);
         System.out.println("classifier_type: " + classifier_type);
         System.out.println("testing_to_load_classifiers_file: " + testing_to_load_classifiers_file);
         System.out.println("save_file: " + save_file);
         System.out.println("cross_validation_string: " + cross_validation_string);
         System.out.println("experimentation_string: " + experimentation_string);
         System.out.println("dimensionality reduction: " + feature_selector);
         System.out.println("order_randomly: " + order_randomly);
         System.out.println("max_spread: " + max_spread);
         System.out.println("max_membership: " + max_membership);
         System.out.println("save_intermediate_arffs: " + save_intermediate_arffs);
         System.out.println("taxonomy_file: " + taxonomy_file);
         System.out.println("feature_key_file: " + feature_key_file);
         System.out.println("classifications_file: " + classifications_file);
         System.out.println("feature_vector_files[0]: " + feature_vector_files[0]);
         System.out.println("arff_file: " + arff_file);
     }


}
