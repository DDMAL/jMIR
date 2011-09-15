/**
 *
 */
package jAudioFeatureExtractor.Aggregators;

import java.io.DataOutputStream;

import jAudioFeatureExtractor.ACE.DataTypes.AggregatorDefinition;
import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import jAudioFeatureExtractor.AudioFeatures.FeatureExtractor;
import jAudioFeatureExtractor.GeneralTools.StringMethods;

/**
 * Aggregator is an interface for specifying the mechanism for collapsing
 * frame-by-frame features into per-file data. There exists two types of
 * aggregators - specific aggregators and generic aggregators.
 * <p>
 * Generic aggregators aggregate for each feature (seperately) that is to be
 * saved and should override init and setSource methods. Specific aggregators
 * can aggregate any number of features, but these features must be specified in
 * advance.
 *
 * @author Daniel McEnnis
 *
 */
public abstract class Aggregator {

	double[] result;

	AggregatorDefinition metadata;

	FeatureDefinition definition;

	/**
	 * Convenience variable containing the end of line characters for this
	 * system.
	 */
	public static final String LINE_SEP = System.getProperty("line.separator");

	/**
	 * Provide a list of features that are to be aggregated by this feature.
	 * Returning null indicates that this aggregator accepts only one feature
	 * and every feature avaiable should be used.
	 *
	 * @return list of features to be used by this aggregator or null
	 */
	public String[] getFeaturesToApply() {
		return null;
	}

	/**
	 * Provide a list of the values of all parameters this aggregator uses.
	 * Aggregators without parameters return null.
	 *
	 * @return list of the values of parmeters or null.
	 */
	public String[] getParamaters() {
		return null;
	}

	/**
	 * Create a new aggregator of the same class
	 *
	 */
	public Object clone() {
		return null;
	}

	/**
	 * Description of a particular instantiation of an aggregate. This should
	 * not be called until after the specific features have been specified by
	 * the init function.
	 *
	 * @return Feature Definition describing this instantiation of this
	 *         aggregate object
	 */
	public FeatureDefinition getFeatureDefinition() {
		return definition;
	}

	public AggregatorDefinition getAggregatorDefinition() {
		return metadata;
	}

	/**
	 * Specifies which Features are to be extracted and the index of these
	 * features in the values array that will passed into the aggregate function
	 *
	 * @param featureIndecis
	 *            Indecis of these features in the array passed in aggregate
	 * @throws Exception
	 *             if either parameter is null, of dicffering lengths, or
	 *             contain invalid index values.
	 */
	public void init(int[] featureIndecis) throws Exception {

	}

	public void setSource(FeatureExtractor feature) {

	}

	/**
	 * Aggregates the values of the features specified by the init function
	 * accross all windows of the data recieved.
	 *
	 * @param values
	 *            complete array of the extracted features. Indecis are window,
	 *            feature, and then feature value.
	 */
	public void aggregate(double[][][] values) throws Exception {

	}

	/**
	 * Output the feature definition entry (for an ACE feature definition file)
	 * for this particular instantiation of the aggreagtor.
	 *
	 * @param output
	 *            output stream to be used.
	 * @throws Exception
	 */
	public void outputACEFeatureKeyEntries(DataOutputStream output)
			throws Exception {
		output.writeBytes("	<feature>" + LINE_SEP);
		output.writeBytes("		<name>" + definition.name + "</name>" + LINE_SEP);
		output.writeBytes("		<description>" + definition.description
				+ "</description>" + LINE_SEP);
		output.writeBytes("		<is_sequential>" + definition.is_sequential
				+ "</is_sequential>" + LINE_SEP);
		output.writeBytes("		<parallel_dimensions>" + definition.dimensions
				+ "</parallel_dimensions>" + LINE_SEP);
		output.writeBytes("	</feature>" + LINE_SEP);

	}

	/**
	 * Output the data definition array
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputACEValueEntries(DataOutputStream output) throws Exception {
		output.writeBytes("		<feature>" + LINE_SEP);
		output.writeBytes("			<name>" + definition.name + "</name>" + LINE_SEP);
		for (int i = 0; i < result.length; ++i) {
			output.writeBytes("			<v>"
					+ StringMethods.getDoubleInScientificNotation(result[i], 4)
					+ "</v>" + LINE_SEP);
		}
		output.writeBytes("		</feature>" + LINE_SEP);
	}

	/**
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputARFFHeaderEntries(DataOutputStream output)
			throws Exception {
		for (int i = 0; i < definition.dimensions; ++i) {
			output.writeBytes("@ATTRIBUTE \"" + definition.name + i
					+ "\" NUMERIC" + LINE_SEP);
		}
	}

	/**
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputARFFValueEntries(DataOutputStream output)
			throws Exception {
		output.writeBytes(StringMethods.getDoubleInScientificNotation(
				result[0], 4));
		for (int i = 1; i < definition.dimensions; ++i) {
			output
					.writeBytes(","
							+ StringMethods.getDoubleInScientificNotation(
									result[i], 4));
		}
	}

	public void setParameters(String[] featureNames, String[] params)
			throws Exception {

	}

	protected int calculateOffset(double[][][] values, int[] featureList) {
		int ret = 0;
		for (int i = 0; i < featureList.length; ++i) {
			int offset = 0;
			while (values[offset][featureList[i]] == null) {
				offset++;
			}
			if (offset > ret) {
				ret = offset;
			}
		}
		return ret;
	}

	protected int[][] collapseFeatures(double[][][] values, int[] indecis) {
		int count = 0;
		for (int i = 0; i < indecis.length; ++i) {
			if (values[values.length - 1][indecis[i]] != null) {
				count += values[values.length - 1][indecis[i]].length;
			}
		}
		int[][] ret = new int[count][2];
		count = 0;
		for (int i = 0; i < indecis.length; ++i) {
			if (values[values.length - 1][indecis[i]] != null) {
				for (int j = 0; j < values[values.length - 1][indecis[i]].length; ++j) {
					ret[count][0] = indecis[i];
					ret[count][1] = j;
					count++;
				}
			}
		}
		return ret;
	}

}
