/*
 * TrainedModel.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import weka.attributeSelection.*;
import weka.classifiers.*;
import weka.core.*;
import java.io.*;

/**
 * Serializable object that stores the Weka Objects associated with a Classification. The <i>Trainer</i>
 * class will train the Weka Classifier, set the fields of this class, and save
 * it to a file. The <i>InstanceClassifier</i> class will read this object from
 * a file and access its fields to be used for classification. This object will
 * also be used in the context of cross validation and experimentation. This class
 * has no methods; it is only used for storing and saving the objects needed for
 * classification.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class TrainedModel
        implements Serializable
{

    /* FIELDS ****************************************************************/

    /**
     * A Weka Classifier to be trained based on the training Instances.
     */
    public Classifier classifier;

    /**
     * A feature selector to be used to select features of a feature set.
     */
    public AttributeSelection attribute_selector;

    /**
     * Attribute describing the possible classes to which an Instance may belong.
     */
    public Attribute class_attribute;

    /**
     * An identifier for use in serialization.
     */
    private static final long serialVersionUID = 100L;


    /* CONSTRUCTORS *************************************************************/

    /**
     * Constructs an instance of a TrainedModel object. Fields are set to null.
     */
    public TrainedModel()
    {
        classifier = null;
        attribute_selector = null;
        class_attribute = null;
    }

    /**
     * Constructs an instance of a TrainedModel object. Fields are set to values
     * of parameters.
     *
     * @param classifier            The Weka Classifier to be trained, saved, and
     *                              used for classification.
     * @param attribute_selector    The Weka object used for dimensionality reduction.
     * @param class_attribute       The Weka object specifying the possible classes
     *                              into which a specific Instance may be classified.
     */
    public TrainedModel(Classifier classifier, AttributeSelection attribute_selector, Attribute class_attribute)
    {
        this.classifier = classifier;
        this.attribute_selector = attribute_selector;
        this.class_attribute = class_attribute;
    }

}
