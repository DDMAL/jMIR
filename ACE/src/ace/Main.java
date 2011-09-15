/*
 * Main.java
 * Version 2.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package ace;

import ace.gui.*;

/**
 * Runs the ACE software based on command line flags.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class Main
{
    /**
     * Determines whether to run ACE from the GUI or the command line based on
     * command line flags.
     * <p>
     * <li> If one of -train, -test, -cv, or -exp are given at the command line,
     * ACE will run from the command line and the GUI will not open.
     * <li> If none of these options are specified, ACE will run from the GUI.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {
        try
        {// Load the GUI or load the command line, with a defualt project file if appropriate

            MainGUIFrame test;

            // Parse command line arguments
            CommandLine cmd = new CommandLine(args);
            //cmd.dumpContents(); // Prints values that were read from the command line, useful for testing

            // If training or testing or cross validation or experimentation is specified
            // at the command line, run ACE from the command line
            if(cmd.train == true
                    ||cmd.testing_to_load_classifiers_file!=null
                    ||cmd.cross_validation_string!=null
                    ||cmd.experimentation_string!=null)
                cmd.processRequests();
            else if(cmd.train == false
                    &&cmd.testing_to_load_classifiers_file==null
                    &&cmd.cross_validation_string==null
                    &&cmd.experimentation_string==null)
            {// If no processing request are made, go to GUI.
                if (cmd.dozip!=null||cmd.unzip||cmd.zip_add != null||cmd.zip_extract != null)
                    cmd.processZip();

                else if (cmd.load_zip != null)
                {
                    // Initialize GUI with project file from zipfile
                    test = new MainGUIFrame(cmd.project_path);
                    test.zipfile_loaded = true;
                    test.project_files_dialog_box.zip_path = cmd.load_zip;
                }
                else if(cmd.project_path != null)// Go to GUI with project file
                    test = new MainGUIFrame(cmd.project_path);
                else if(cmd.classifications_file != null || cmd.taxonomy_file != null
                        || cmd.feature_vector_files != null || cmd.feature_key_file != null)
                {
                    // Initialize GUI with ACE XML files that were specified at the command line.
                    String[] feature_key_files = {cmd.feature_key_file};
                    String[] model_classifications_files = {cmd.classifications_file};
                    test = new MainGUIFrame(cmd.taxonomy_file, feature_key_files,
                            cmd.feature_vector_files, model_classifications_files, null, null);
                }
                else if (cmd.arff_file != null) // Load GUI with arff file
                {
                    test = new MainGUIFrame(null, null, null, null, cmd.arff_file, null);
                }
                else // Load GUI without project file, start new project
                    test = new MainGUIFrame();
            }
        }
        catch (Exception e)
        {
            // If an error occurs, print instructional message containing all command line flags
            System.out.println("\nERROR: " + e.getMessage() + "\n\n ");
            e.printStackTrace();
            //CommandLine.printHelpMessage();
        }
    }
}