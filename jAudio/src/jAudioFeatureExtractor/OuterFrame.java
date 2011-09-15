/*
 * @(#)OuterFrame.java	1.0	April 5, 2005.
 *
 * McGill Univarsity
 */

package jAudioFeatureExtractor;

import jAudioFeatureExtractor.actions.ExecuteBatchAction;

import java.awt.*;
import javax.swing.*;
// import javax.help.*;
import java.net.*;

/**
 * A panel holding various components of the jAudio Feature Extractor GUI
 *
 * @author Cory McKay
 */
public class OuterFrame extends JFrame {
	/* FIELDS ***************************************************************** */

	static final long serialVersionUID = 1;

	/**
	 * A panel allowing the user to select files to extract features from.
	 * <p>
	 * Audio files may also be played, edited and generated here. MIDI files may
	 * be converted to audio.
	 */
	public RecordingSelectorPanel recording_selector_panel;

	/**
	 * A panel allowing the user to select features to extract from audio files
	 * and extract the features. Basic feature parameters may be set and feature
	 * values and definitions can be saved to disk.
	 */
	public FeatureSelectorPanel feature_selector_panel;

	/**
	 * A class that contains all the logic for handling events fired from this
	 * gui. Utilizes the Mediator pattern to control dependencies between
	 * objects. Also contains all the menu bar actions.
	 */
	public Controller controller;

	/**
	 * Global menu bar for this application
	 */
	public JMenuBar menu;

	/**
	 * Radio button for choosing the ACE data format
	 */
	public JRadioButtonMenuItem ace;

	/**
	 * Radio button for chosing the ARFF data format
	 */
	public JRadioButtonMenuItem arff;

	/* CONSTRUCTOR ************************************************************ */

	/**
	 * Basic constructor that sets up the GUI.
	 */
	public OuterFrame(Controller c) {
		// Set window title
		setTitle("jAudio Feature Extractor");

		// Make quit when exit box pressed
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// set controller
		controller = c;

		ace = new JRadioButtonMenuItem("ACE");
		arff = new JRadioButtonMenuItem("ARFF");
		ButtonGroup bg = new ButtonGroup();
		bg.add(ace);
		bg.add(arff);


		// Instantiate panels
		recording_selector_panel = new RecordingSelectorPanel(this, c);
		feature_selector_panel = new FeatureSelectorPanel(this, c);

		controller.normalise = new JCheckBoxMenuItem("Normalise Recordings",
				false);

		Color blue = new Color((float) 0.75, (float) 0.85, (float) 1.0);
		this.getContentPane().setBackground(blue);
		feature_selector_panel.setBackground(blue);
		recording_selector_panel.setBackground(blue);

		ace.setSelected(true);
		ace.addActionListener(controller.outputTypeAction);
		arff.addActionListener(controller.outputTypeAction);
		controller.extractionThread = new ExtractionThread(controller, this);

		controller.executeBatchAction = new ExecuteBatchAction(controller, this);

		JRadioButtonMenuItem sample8 = new JRadioButtonMenuItem("8");
		JRadioButtonMenuItem sample11 = new JRadioButtonMenuItem("11.025");
		JRadioButtonMenuItem sample16 = new JRadioButtonMenuItem("16");
		JRadioButtonMenuItem sample22 = new JRadioButtonMenuItem("22.05");
		JRadioButtonMenuItem sample44 = new JRadioButtonMenuItem("44.1");
		ButtonGroup sr = new ButtonGroup();
		sr.add(sample8);
		sr.add(sample11);
		sr.add(sample16);
		sr.add(sample22);
		sr.add(sample44);
		sample16.setSelected(true);
		sample8.addActionListener(controller.samplingRateAction);
		sample11.addActionListener(controller.samplingRateAction);
		sample16.addActionListener(controller.samplingRateAction);
		sample22.addActionListener(controller.samplingRateAction);
		sample44.addActionListener(controller.samplingRateAction);
		controller.samplingRateAction.setTarget(new JRadioButtonMenuItem[] {
				sample8, sample11, sample16, sample22, sample44 });

		controller.removeBatch = new JMenu();
		controller.viewBatch = new JMenu();

		JMenuItem helpTopics = new JMenuItem("Help Topics");

		menu = new JMenuBar();
		menu.setBackground(blue);
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(c.saveAction);
		fileMenu.add(c.saveBatchAction);
		fileMenu.add(c.loadAction);
		fileMenu.add(c.loadBatchAction);
		fileMenu.addSeparator();
		fileMenu.add(c.addBatchAction);
		fileMenu.add(c.executeBatchAction);
		controller.removeBatch = new JMenu("Remove Batch");
		controller.removeBatch.setEnabled(false);
		fileMenu.add(c.removeBatch);
		controller.viewBatch = new JMenu("View Batch");
		controller.viewBatch.setEnabled(false);
		fileMenu.add(c.viewBatch);
		fileMenu.addSeparator();
		fileMenu.add(c.exitAction);
		JMenu editMenu = new JMenu("Edit");
		editMenu.add(c.cutAction);
		editMenu.add(c.copyAction);
		editMenu.add(c.pasteAction);
		JMenu recordingMenu = new JMenu("Recording");
		recordingMenu.add(c.addRecordingsAction);
		recordingMenu.add(c.editRecordingsAction);
		recordingMenu.add(c.removeRecordingsAction);
		recordingMenu.add(c.recordFromMicAction);
		recordingMenu.add(c.synthesizeAction);
		recordingMenu.add(c.viewFileInfoAction);
		recordingMenu.add(c.storeSamples);
		recordingMenu.add(c.validate);
		JMenu analysisMenu = new JMenu("Analysis");
		analysisMenu.add(c.globalWindowChangeAction);
		c.outputType = new JMenu("Output Format");
		c.outputType.add(ace);
		c.outputType.add(arff);
		analysisMenu.add(c.outputType);
		c.sampleRate = new JMenu("Sample Rate (kHz)");
		c.sampleRate.add(sample8);
		c.sampleRate.add(sample11);
		c.sampleRate.add(sample16);
		c.sampleRate.add(sample22);
		c.sampleRate.add(sample44);
		analysisMenu.add(c.sampleRate);
		analysisMenu.add(controller.normalise);
		JMenu playbackMenu = new JMenu("Playback");
		playbackMenu.add(c.playNowAction);
		playbackMenu.add(c.playSamplesAction);
		playbackMenu.add(c.stopPlayBackAction);
		playbackMenu.add(c.playMIDIAction);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(helpTopics);
		helpMenu.add(c.aboutAction);

//		HelpSet hs = getHelpSet("Sample.hs");
//		HelpBroker hb = hs.createHelpBroker();

//		CSH.setHelpIDString(helpTopics, "top");
//		helpTopics.addActionListener(new CSH.DisplayHelpFromSource(hb));

		menu.add(fileMenu);
		menu.add(editMenu);
		menu.add(recordingMenu);
		menu.add(analysisMenu);
		menu.add(playbackMenu);
		menu.add(helpMenu);
		// Add items to GUI
		setLayout(new BorderLayout(8, 8));
		add(recording_selector_panel, BorderLayout.WEST);
		add(feature_selector_panel, BorderLayout.EAST);
		add(menu, BorderLayout.NORTH);
		// Display GUI
		pack();
		setVisible(true);

	}

	/**
	 * This method creates the online help system.
	 *
	 * @param helpsetfile
	 *            Name of the file where the help file metadata is stored.
	 * @return Reference to a newly created help set.
	 */
/*	public HelpSet getHelpSet(String helpsetfile) {
		HelpSet hs = null;
		ClassLoader cl = this.getClass().getClassLoader();
		try {
			URL hsURL = HelpSet.findHelpSet(cl, helpsetfile);
			hs = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			System.out.println("HelpSet: " + ee.getMessage());
			System.out.println("HelpSet: " + helpsetfile + " not found");
		}
		return hs;
	}
*/
 }