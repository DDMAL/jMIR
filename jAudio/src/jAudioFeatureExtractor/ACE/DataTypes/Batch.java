package jAudioFeatureExtractor.ACE.DataTypes;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import jAudioFeatureExtractor.DataModel;
import jAudioFeatureExtractor.Aggregators.Aggregator;
import jAudioFeatureExtractor.DataTypes.RecordingInfo;
import jAudioFeatureExtractor.jAudioTools.AudioSamples;

/**
 * Data type used to represent a batch file.
 *
 * @author Daniel McEnnis
 */
public class Batch implements Serializable {

	static final long serialVersionUID = 1;

	String name;

	RecordingInfo[] recording = new RecordingInfo[0];

	int windowSize;

	double windowOverlap;

	double samplingRate;

	boolean normalise;

	boolean perWindow;

	boolean overall;

	String destinationFK = null;

	String destinationFV = null;

	int outputType;

	transient DataModel dm_;

	HashMap<String, Boolean> activated;

	HashMap<String, String[]> attributes;

	String[] aggregatorNames;

	String[][] aggregatorFeatures;

	String[][] aggregatorParameters;

	/**
	 * Set the data model against which this batch is executed.
	 *
	 * @param dm
	 *            Context of this batch.
	 */
	public void setDataModel(DataModel dm) {
		dm_ = dm;
	}

	/**
	 * Execute this batch by first setting the context ass specified in the
	 * batch, then executing using the data model.
	 *
	 * @throws Exception
	 */
	public void execute() throws Exception {
		applyAttributes();
		dm_.extract(windowSize, windowOverlap, samplingRate, normalise,
				perWindow, overall, recording, outputType);
	}

	/**
	 * Sets the recordings that this batch will load and execute.
	 *
	 * @param files
	 *            recordings which are to be scheduled for porcessing.
	 * @throws Exception
	 */
	public void setRecordings(File[] files) throws Exception {
		recording = new RecordingInfo[files.length];
		// Go through the files one by one
		for (int i = 0; i < files.length; i++) {
			// Verify that the file exists
			if (files[i].exists()) {
				try {
					// Generate a RecordingInfo object for the loaded file
					recording[i] = new RecordingInfo(files[i].getName(),
							files[i].getPath(), null, false);
				} catch (Exception e) {
					recording = null;
					throw e;
				}
			} else {
				recording = null;
				throw new Exception("The selected file " + files[i].getName()
						+ " does not exist.");
			}
		}
	}

	/**
	 * Sets the attributes for how the features are to be extracted when
	 * executed.
	 *
	 * @param windowSize
	 *            Size of the analysis window in samples.
	 * @param windowOverlap
	 *            Percent overlap of the windows. Must be greater than or equal
	 *            to 0 and less than 1.
	 * @param samplingRate
	 *            number of samples per second of audio.
	 * @param normalise
	 *            should the files be normalised before execution.
	 * @param perWindow
	 *            should features be extracted for each window in each file.
	 * @param overall
	 *            should overall features be extracted for each files.
	 * @param outputType
	 *            what format should the extracted features be saved in.
	 */
	public void setSettings(int windowSize, double windowOverlap,
			double samplingRate, boolean normalise, boolean perWindow,
			boolean overall, int outputType) {
		this.windowSize = windowSize;
		this.windowOverlap = windowOverlap;
		this.samplingRate = samplingRate;
		this.normalise = normalise;
		this.perWindow = perWindow;
		this.overall = overall;
		this.outputType = outputType;
	}

	/**
	 * Sets where the extracted features should be stored.
	 *
	 * @param FK
	 *            Location where feature descriptions should be stored.
	 * @param FV
	 *            Location where extracted features should be stored.
	 */
	public void setDestination(String FK, String FV) {
		destinationFK = FK;
		destinationFV = FV;
	}

	/**
	 * Sets which features are active and the parameters of these features.
	 *
	 * @param activated
	 *            Which features are to be extracted.
	 * @param attributes
	 *            settings of parameters of these features.
	 */
	public void setFeatures(HashMap<String, Boolean> activated,
			HashMap<String, String[]> attributes) {
		this.activated = activated;
		this.attributes = attributes;
	}

	/**
	 * Returns the name of this batch.
	 *
	 * @return name assigned to this batch.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this batch. This name must be unique.
	 *
	 * @param name
	 *            Name of this batch.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Apply the stored attributes against the current feature list.
	 *
	 * @throws Exception
	 */
	private void applyAttributes() throws Exception {

		for (int i = 0; i < dm_.features.length; ++i) {
			String name = dm_.features[i].getFeatureDefinition().name;
			if (attributes.containsKey(name)) {
				dm_.defaults[i] = activated.get(name);
				String[] tmp = attributes.get(name);
				for (int j = 0; j < tmp.length; ++j) {
					dm_.features[i].setElement(j, tmp[j]);
				}
			}else{
				dm_.defaults[i] = false;
			}
		}
		LinkedList<Aggregator> aggregatorList = new LinkedList<Aggregator>();
		for(int i=0;i<aggregatorNames.length;++i){
			Aggregator tmp = (Aggregator)dm_.aggregatorMap.get(aggregatorNames[i]).clone();
//			if(!tmp.getAggregatorDefinition().generic){
				tmp.setParameters(aggregatorFeatures[i],aggregatorParameters[i]);
//			}
			aggregatorList.add(tmp);
		}
		if(overall && (aggregatorList.size()==0)){
			throw new Exception("Attempting to get overall stats without specifying any aggregators to create it");
		}
		dm_.aggregators = aggregatorList.toArray(new Aggregator[]{});
	}

	public Aggregator[] getAggregator() throws Exception{
		LinkedList<Aggregator> aggregatorList = new LinkedList<Aggregator>();
		if(aggregatorNames != null){
			for(int i=0;i<aggregatorNames.length;++i){
				Aggregator tmp = (Aggregator)dm_.aggregatorMap.get(aggregatorNames[i]).clone();
//			if(!tmp.getAggregatorDefinition().generic){
				tmp.setParameters(aggregatorFeatures[i],aggregatorParameters[i]);
//			}
				aggregatorList.add(tmp);
			}
		}
		if(overall && (aggregatorList.size()==0)){
			throw new Exception("Attempting to get overall stats without specifying any aggregators to create it");
		}
		return aggregatorList.toArray(new Aggregator[]{});

	}

	/**
	 * Output this batch in XML format.
	 *
	 * @return String contains a complete batch XML file.
	 */
	public String outputXML() {
		StringBuffer ret = new StringBuffer();
		String sep = System.getProperty("line.separator");
		ret.append("\t<batch ID=\"").append(name).append("\">").append(sep);
		ret.append("\t\t<fileSet>").append(sep);
		for (int i = 0; i < recording.length; ++i) {
			ret.append("\t\t\t<file>").append(recording[i].file_path).append(
					"</file>").append(sep);
		}
		ret.append("\t\t</fileSet>").append(sep);
		ret.append("\t\t<settings>").append(sep);
		ret.append("\t\t\t<windowSize>").append(windowSize).append(
				"</windowSize>").append(sep);
		ret.append("\t\t\t<windowOverlap>").append(windowOverlap).append(
				"</windowOverlap>").append(sep);
		ret.append("\t\t\t<samplingRate>").append(samplingRate).append(
				"</samplingRate>").append(sep);
		ret.append("\t\t\t<normalise>").append(normalise)
				.append("</normalise>").append(sep);
		ret.append("\t\t\t<perWindowStats>").append(perWindow).append(
				"</perWindowStats>").append(sep);
		ret.append("\t\t\t<overallStats>").append(overall).append(
				"</overallStats>").append(sep);
		if (outputType == 0) {
			ret.append("\t\t\t<outputType>ACE</outputType>").append(sep);
		} else {
			ret.append("\t\t\t<outputType>ARFF</outputType>").append(sep);
		}
		Set s = attributes.entrySet();
		for (Iterator<Map.Entry<String, String[]>> iterator = s.iterator(); iterator
				.hasNext();) {
			Map.Entry<String, String[]> i = iterator.next();
			String name = i.getKey();
			String[] att = i.getValue();
			ret.append("\t\t\t<feature>").append(sep);
			ret.append("\t\t\t\t<name>").append(name).append("</name>").append(
					sep);
			ret.append("\t\t\t\t<active>").append(activated.get(name)).append(
					"</active>").append(sep);
			for (int j = 0; j < att.length; ++j) {
				ret.append("\t\t\t\t<attribute>").append(att[j]).append(
						"</attribute>").append(sep);
			}
			ret.append("\t\t\t</feature>").append(sep);

		}
		for(int i=0;i<aggregatorNames.length;++i){
			ret.append("\t\t\t<aggregator>").append(sep);
			ret.append("\t\t\t\t<aggregatorName>").append(aggregatorNames[i]).append("</aggregatorName>").append(sep);
			if(aggregatorFeatures[i] != null){
				for(int j=0;j<aggregatorFeatures[i].length;++j){
					ret.append("\t\t\t\t<aggregatorFeature>").append(aggregatorFeatures[i][j]).append("</aggregatorFeature>").append(sep);
				}
			}
			if(aggregatorParameters[i]!= null){
				for(int j=0;j<aggregatorParameters[i].length;++j){
					ret.append("\t\t\t\t<aggregatorAttribute>").append(aggregatorParameters[i][j]).append("</aggregatorAttribute>").append(sep);
				}
			}
			ret.append("\t\t\t</aggregator>").append(sep);
		}
		ret.append("\t\t</settings>").append(sep);
		ret.append("\t\t<destination>").append(destinationFK).append(
				"</destination>").append(sep);
		ret.append("\t\t<destination>").append(destinationFV).append(
				"</destination>").append(sep);
		ret.append("\t</batch>").append(sep);
		return ret.toString();
	}

	/**
	 * apply this batch against info needed for a datamodel so that it can be
	 * executed.
	 *
	 * @param recording
	 *            list of files to be analyzed
	 * @param windowSize
	 *            size of the analysis window in samples
	 * @param windowOverlap
	 *            percent overlap as a value between 0 and 1.
	 * @param samplingRate
	 *            number of samples per second
	 * @param normalise
	 *            should the file be normalized before execution
	 * @param perWindow
	 *            should features be extracted on a window bby window basis
	 * @param overall
	 *            should global features be extracted
	 * @param destinationFK
	 *            location of the feature declaration file
	 * @param destinationFV
	 *            location where extracted features should be stored
	 * @param outputType
	 *            what output format should extracted features be stored in.
	 */
	public void applySettings(RecordingInfo[][] recording, int[] windowSize,
			double[] windowOverlap, double[] samplingRate, boolean[] normalise,
			boolean[] perWindow, boolean[] overall, String[] destinationFK,
			String[] destinationFV, int[] outputType) {
		try {
			applyAttributes();
			dm_.featureDefinitions = new FeatureDefinition[dm_.features.length];
			for (int i = 0; i < dm_.featureDefinitions.length; ++i) {
				dm_.featureDefinitions[i] = dm_.features[i]
						.getFeatureDefinition();
			}
			dm_.recordingInfo = this.recording;
		} catch (Exception e) {
			System.err.println("INTERNAL ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		recording[0] = this.recording;
		windowSize[0] = this.windowSize;
		windowOverlap[0] = this.windowOverlap;
		samplingRate[0] = this.samplingRate;
		normalise[0] = this.normalise;
		perWindow[0] = this.perWindow;
		overall[0] = this.overall;
		destinationFK[0] = this.destinationFK;
		destinationFV[0] = this.destinationFV;
		outputType[0] = this.outputType;
	}

	public HashMap<String, String[]> getAttributes() {
		return attributes;
	}

	public HashMap<String, Boolean> getActivated() {
		return activated;
	}

	public void setAttributes(HashMap<String, String[]> attributes) {
		this.attributes = attributes;
	}

	public String getDestinationFK() {
		return destinationFK;
	}

	public void setDestinationFK(String destinationFK) {
		this.destinationFK = destinationFK;
	}

	public String getDestinationFV() {
		return destinationFV;
	}

	public void setDestinationFV(String destinationFV) {
		this.destinationFV = destinationFV;
	}

	public boolean isNormalise() {
		return normalise;
	}

	public void setNormalise(boolean normalise) {
		this.normalise = normalise;
	}

	public int getOutputType() {
		return outputType;
	}

	public void setOutputType(int outputType) {
		this.outputType = outputType;
	}

	public boolean isOverall() {
		return overall;
	}

	public void setOverall(boolean overall) {
		this.overall = overall;
	}

	public boolean isPerWindow() {
		return perWindow;
	}

	public void setPerWindow(boolean perWindow) {
		this.perWindow = perWindow;
	}

	public double getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(double samplingRate) {
		this.samplingRate = samplingRate;
	}

	public double getWindowOverlap() {
		return windowOverlap;
	}

	public void setWindowOverlap(double windowOverlap) {
		this.windowOverlap = windowOverlap;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setRecording(RecordingInfo[] recording) {
		this.recording = recording;
	}

	public RecordingInfo[] getRecording() {
		return recording;
	}

	public DataModel getDataModel() {
		return dm_;
	}

	public void setAggregators(String[] aggNames, String[][] aggFeatures,
			String[][] aggParam) {
		if ((aggNames.length == aggFeatures.length)
				&& (aggFeatures.length == aggParam.length)) {
			aggregatorNames = aggNames;
			aggregatorFeatures = aggFeatures;
			aggregatorParameters = aggParam;
		} else {
			System.out
					.println("INTERNAL ERROR: Parameters are not of the same length - implying differing numbers of aggregators to define:"
							+ aggNames.length
							+ " "
							+ aggFeatures.length
							+ " "
							+ aggParam.length);
		}
	}
	public String[] getAggregatorNames(){
		return aggregatorNames;
	}

	public String[][] getAggregatorFeatures(){
		return aggregatorFeatures;
	}

	public String[][] getAggregatorParameters(){
		return aggregatorParameters;
	}
}
