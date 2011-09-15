/*
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace;

import ace.datatypes.TrainedModel;
import java.io.*;
import weka.core.*;

/**
 * Trains a Weka Classifier based on a set of Weka training Instances.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class Trainer
{

    /* PUBLIC METHODS ********************************************************/

    /**
     * Trains the Weka Classifier contained in the given TrainedModel object based on the
     * given traning Instances.

     * @param   instances                       The Weka Instances to use for training.
     * @param   trained                         The serializeable TrainedModel object containing
     *                                          reference to the Weka object needed
     *                                          for classification.
     * @throws  Exception                       If an errror is encountered.
     */
    public static void train(Instances instances,
            TrainedModel trained)
            throws Exception
    {
        //Train the classifier
        trained.classifier.buildClassifier(instances);

        // Save the class attribues used by classifier
        trained.class_attribute = instances.classAttribute();

    }


    /**
     * Saves the given Weka Instances as an arff file with the given path.
     *
     * @param instances     The Weka Instances to save.
     * @param file_path     The path of the arff file to save.
     * @throws Exception    Throws an exception if cannot save the given instances.
     */
    public static void saveInstancesAsARFF(Instances instances, String file_path)
            throws Exception
    {
        String file_contents = instances.toString();
        try
        {
            File save_file = new File(file_path);
            FileOutputStream to = new FileOutputStream(save_file);
            DataOutputStream writer = new DataOutputStream(to);
            writer.writeBytes(file_contents);
        } catch (Exception e)
        {
            throw new Exception("Could not save to file " + file_path + ". " + e.getMessage());
        }
    }
}
