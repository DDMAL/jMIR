/**
 *
 */
package jAudioFeatureExtractor.Aggregators;

import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import jAudioFeatureExtractor.AudioFeatures.FeatureExtractor;

import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Vector;

/**
 * Container object that handles the creation of aggregators.
 *
 * @author Daniel McEnnis
 *
 */
public class AggregatorContainer {

	Vector<Aggregator> aggregatorTemplate;

	Vector<Aggregator> aggregatorList;

	Vector<FeatureExtractor> featureList;

	Vector<Integer> featureIndecis2FeatureListMapping;


	public AggregatorContainer() {
		aggregatorTemplate = new Vector<Aggregator>();
		aggregatorList = new Vector<Aggregator>();
		featureList = new Vector<FeatureExtractor>();
		featureIndecis2FeatureListMapping = new Vector<Integer>();
	}

	public void add(Aggregator[] aggs) throws Exception{
		for (int i = 0; i < aggs.length; ++i) {
			aggregatorTemplate.add(aggs[i]);
		}
		if(featureList.size() > 0){
			buildAggregatorList();
		}
	}

	public void add(FeatureExtractor[] feature) throws Exception{
		boolean[] toggle = new boolean[feature.length];
		Arrays.fill(toggle, true);
		add(feature, toggle);

	}

	public void add(FeatureExtractor[] feature, boolean[] toggle) throws Exception{
		featureList.clear();
		for (int i = 0; i < feature.length; ++i) {
			if (toggle[i]) {
				featureList.add(feature[i]);
				featureIndecis2FeatureListMapping.add(i);
			}
		}
		if(aggregatorTemplate.size()>0){
			buildAggregatorList();
		}
	}

	public FeatureDefinition[] getFeatureDefinitions() {
		FeatureDefinition[] ret = new FeatureDefinition[aggregatorList.size()];
		for (int i = 0; i < aggregatorList.size(); ++i) {
			ret[i] = aggregatorList.get(i).getFeatureDefinition();
		}
		return ret;
	}

	public void aggregate(double[][][] values) throws Exception{
		for (int i = 0; i < aggregatorList.size(); ++i) {
			aggregatorList.get(i).aggregate(values);
		}
	}

	/**
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputACEFeatureKeyEntries(DataOutputStream output)
			throws Exception {
		for (int i = 0; i < aggregatorList.size(); ++i) {
			aggregatorList.get(i).outputACEFeatureKeyEntries(output);
		}
	}

	/**
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputACEValueEntries(DataOutputStream output) throws Exception {
		for (int i = 0; i < aggregatorList.size(); ++i) {
			aggregatorList.get(i).outputACEValueEntries(output);
		}
	}

	/**
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputARFFHeaderEntries(DataOutputStream output) throws Exception {
		for (int i = 0; i < aggregatorList.size(); ++i) {
			aggregatorList.get(i).outputARFFHeaderEntries(output);
		}
		output.writeBytes("@DATA"+System.getProperty("line.separator"));
	}

	/**
	 *
	 * @param output
	 * @throws Exception
	 */
	public void outputARFFValueEntries(DataOutputStream output) throws Exception {
		for (int i = 0; i < aggregatorList.size(); ++i) {
			aggregatorList.get(i).outputARFFValueEntries(output);
			if(i< aggregatorList.size()-1){
				output.writeBytes(",");
			}
		}
		output.writeBytes(Aggregator.LINE_SEP);
	}

	void buildAggregatorList() throws Exception{
		aggregatorList.clear();
		for(int i=0;i<aggregatorTemplate.size();++i){
			String[] list = aggregatorTemplate.get(i).getFeaturesToApply();
			if(list == null){
				for(int j=0; j<featureList.size();++j){
					Aggregator entry = ((Aggregator)aggregatorTemplate.get(i).clone());
					entry.setSource(featureList.get(j));
					entry.init(new int[]{featureIndecis2FeatureListMapping.get(j)});
					aggregatorList.add(entry);
				}
			}else{
				boolean good = false;
				int[] indeci = new int[list.length];
				for(int j=0;j<list.length;++j){
					good = false;
					for(int k=0;k<featureList.size();++k){
						if(featureList.get(k).getFeatureDefinition().name.equals(list[j])){
							good = true;
							indeci[j] = featureIndecis2FeatureListMapping.get(k);
							break;
						}
					}
					if(!good){
						break;
					}
				}
				if(good){
					aggregatorTemplate.get(i).init(indeci);
					aggregatorList.add(aggregatorTemplate.get(i));
				}
			}
		}
	}

}
