/**
 *
 */
package jAudioFeatureExtractor.Aggregators;

import jAudioFeatureExtractor.ACE.DataTypes.AggregatorDefinition;
import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;

/**
 * @author mcennis
 *
 */
public class AreaMoments extends Aggregator {

	String[] featureNames = null;
	int[] featureNameIndecis = null;

	public AreaMoments(){
		metadata = new AggregatorDefinition("Area Moments","Calculates the first 10 2D statistical moments for the given features",false,null);
	}

	@Override
	public void aggregate(double[][][] values) {
		result = new double[10];
		double x,y,x2,xy,y2,x3,x2y,xy2,y3;
		x=y=x2=xy=y2=x3=x2y=xy2=y3=0.0;
		int offset = super.calculateOffset(values,featureNameIndecis);
		int[][] featureIndecis = super.collapseFeatures(values,featureNameIndecis);
		result[0] = 0.0;
		for (int i=offset;i<values.length;++i){
			for(int j=0;j<featureIndecis.length;++j){
				result[0] += values[i][featureIndecis[j][0]][featureIndecis[j][1]];
			}
		}
		if(result[0] == 0.0){
			java.util.Arrays.fill(result,0.0);
		}else{
			for (int i = offset; i < values.length; ++i) {
				for (int j = 0; j < featureIndecis.length; ++j) {
					int feature = featureIndecis[j][0];
					int dimension = featureIndecis[j][1];
					double tmp = values[i][feature][dimension] / result[0];
					x += tmp * j;
					y += tmp * i;
					x2 += tmp * j * j;
					xy += tmp * i * j;
					y2 += tmp * j * j;
					x3 += tmp * i * i * i;
					x2y += tmp * i * j * j;
					xy2 += tmp * i * i * j;
					y3 += tmp * i * i * i;
				}
			}
			result[1] = x;
			result[2] = y;
			result[3] = x2 - x * x;
			result[4] = xy - x * y;
			result[5] = y2 - y * y;
			result[6] = 2 * Math.pow(x, 3.0) - 3 * x * x2 + x3;
			result[7] = 2 * x * xy - y * x2 + x2 * y;
			result[8] = 2 * y * xy - x * y2 + y2 * x;
			result[9] = 2 * Math.pow(y, 3.0) - 3 * y * y2 + y3;

		}
	}

	@Override
	public Object clone() {
		AreaMoments ret = new AreaMoments();
		if(featureNames != null){
			ret.featureNames = featureNames.clone();
		}
		if(featureNameIndecis != null){
			ret.featureNameIndecis = featureNameIndecis.clone();
		}
		return new AreaMoments();
	}

	@Override
	public FeatureDefinition getFeatureDefinition() {
		return definition;
	}

	@Override
	public String[] getFeaturesToApply() {
		return featureNames;
	}

	@Override
	public void init(int[] featureIndecis) throws Exception {
		if(featureIndecis.length != featureNames.length){
			throw new Exception("INTERNAL ERROR (Agggregator.AreaMoments): number of feature indeci does not match number of features");
		}
		this.featureNameIndecis = featureIndecis;
	}

	@Override
	public void setParameters(String[] featureNames, String[] params) throws Exception {
		this.featureNames = featureNames;
		String names = featureNames[0];
		for(int i=1;i<featureNames.length;++i){
			names += " " + featureNames[i];
		}
		definition = new FeatureDefinition("Area Moments: "+names,"2D moments constructed from features "+names+".",true,10);
	}

}
