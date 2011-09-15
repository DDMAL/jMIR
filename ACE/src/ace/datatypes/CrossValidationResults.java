/*
 * CrossValidationResults.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import java.util.LinkedList;

/**
 * Holds the results of a cross validation.
 *
 * <p>Each cross validation will have one <i>CrossValidationResults</i> object.
 * Every <i>Experimenter</i> will have an array of arrays of <i>CrossValidationResults</i> objects,
 * one object for each cross validation of each classifier for each type of dimensionality reduction.
 * This class has one method that is called by <i>Experimenter</i> to instantiate an array
 * of <i>CrossValidationResults</i> objects. This method will be called once for each type
 * of dimensionality reduction and will create an array with a length equal to the
 * number of classifiers being tested. When a single cross validation is being performed,
 * <i>Coordinator</i> creates an array of <i>CrossValidationResults</i> objects of size 1 to be
 * passed to <i>CrossValidator</i>.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class CrossValidationResults {

    /* FIELDS ****************************************************************/

    /**
     * The serializable object containing reference to the Weka objects that were
     * use for classification in this cross validation. <i>AttributeSelection</i> field
     * may be null when experimenting.
     */
    public TrainedModel trained;

    /**
     * A table displaying the distribution of correct and incorrect instances.
     */
    public String cross_validation_confusion_matrices;

    /**
     * The type of classifier or classification algorithm being used for this cross validation.
     */
    public String classifier_descriptions;

    /**
     * The average error rate of this cross validation.
     */
    public double error_rates;

    /**
     * The time (in minutes) that it took for the cross validation to finish.
     */
    public double cross_val_times;

    /**
     * The standard deviation of error rates across folds.
     */
    public double standard_deviation;


    /* PUBLIC METHOD *********************************************************/

    /**
     * Instantiates an array of CrossValidationResults objects.
     * Used during Experimentation when many different cross validations of the
     * same instances are compared.
     *
     * @param classifier_descriptions_list  A LinkedList of Strings describing each
     *                                      classifier being used in the experimentation.
     *                                      The length of this list will also be the
     *                                      number of classifiers that are being
     *                                      tested in this experimentation.
     *                                      There will be as many cross validations
     *                                      as there are classifiers therefore the
     *                                      generated array of CrossValidationResults
     *                                      will be the same as the size of this list.
     * @return                              An array of CrossValidationResults objects.
     *                                      Only classifier_descriptions of field
     *                                      <i>CrossValidationResults</i> is initialized.
     */
    public static CrossValidationResults[] generateArray(LinkedList<String> classifier_descriptions_list)
    {
        //Prepare an array of CrossValidationResults objects. One CVR object for each classifier being used in the experimentation.
        CrossValidationResults[] cvrArray = new CrossValidationResults[classifier_descriptions_list.size()];
        for(int i=0; i<cvrArray.length&&classifier_descriptions_list.get(i)!=null; i++)
        {
            cvrArray[i] = new CrossValidationResults();
            // Initialize classifier_description field for each object in the array
            cvrArray[i].classifier_descriptions = classifier_descriptions_list.get(i);
        }
        return cvrArray;
    }
}
