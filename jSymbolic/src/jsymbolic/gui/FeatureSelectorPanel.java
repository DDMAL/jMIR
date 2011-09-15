/*
 * FeatureSelectorPanel.java
 * Version 1.2.1
 *
 * Last modified on June 24, 2010.
 * McGill University and the University of Waikato
 */

package jsymbolic.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.LinkedList;
import javax.sound.midi.Sequence;
import ace.datatypes.FeatureDefinition;
import jsymbolic.datatypes.RecordingInfo;
import jsymbolic.features.*;
import jsymbolic.gui.FeatureSelectorTableModel;
import jsymbolic.processing.MIDIFeatureProcessor;


/**
 * A window that allows users to select which features to save as well as some
 * basic parameters relating to these features. These parameters include the
 * window length to use for analyses and the amount of overlap between analysis
 * windows. The user may also see the features that can be extracted and some
 * details about them.
 *
 * <p>The resulting feature values and the features used are saved to the
 * specified feature_vector_file and a feature_key_file respectively.
 *
 * <p>Note that some features need other features in order to be extracted. Even
 * if a feature is not checked for saving, it will be extracted (but not saved)
 * if another feature that needs it is checked for saving.
 *
 * <p>The table allows the user to view all features which are possible to
 * extract. The click box indicates whether this feature is to be saved during
 * feature extraction. The Dimensions indicate how many values are produced for
 * a given feature each time that it is extracted. Double clicking on a feature
 * brings up a window describing it.
 *
 * <p>The Do Not Use Windows checkbox allows the user to decide whether features
 * are to be extracted for the recording overall or in individual windows. The
 * Save Features For Each Window and Save For Overall Recordings check boxes
 * apply only to windowed feature extraction. The former allows features to be
 * saved for each window and the latter causes the averages and standard
 * deviations of each feature accross all windows in each recording to be saved.
 *
 * <p>The Window Size indicates the duration in seconds of each window that
 * features are to be extracted for.
 *
 * <p>The Window Size indicates the duration in seconds of each window that
 * features are to be extracted for.
 *
 * <p>The Window Overlap indicates the fraction, from 0 to 1, of overlap between
 * adjacent analysis windows.
 *
 * <p>The Feature Values Save Path and Feature Definitions Save Path allow the
 * user to choose what paths to save extracted feature values and feature
 * definitions respectively.
 *
 * <p>The Extract Features button extracts all appropriate features and from the
 * loaded recordings, and saves the results to disk.
 *
 * @author Cory McKay
 */
public class FeatureSelectorPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * Holds a reference to the JPanel that holds objects of this class.
      */
     public    OuterFrame                    outer_frame;
     
     /**
      * Holds references to all of the features that it's possible to extract.
      */
     private	MIDIFeatureExtractor[]       feature_extractors;
     
     /**
      * The default as to whether each feature is to be saved after feature
      * extraction. Indices correspond to those of feature_extractors.
      */
     private   boolean[]                     feature_save_defaults;
     
     /**
      * GUI panels
      */
     private   JPanel                        features_panel;
     private   JScrollPane                   features_scroll_pane;
     
     /**
      * GUI table-related fields
      */
     private   JTable                        features_table;
     private   FeatureSelectorTableModel     features_table_model;
     
     /**
      * GUI text areas
      */
     private   JTextArea                     window_length_text_field;
     private   JTextArea                     window_overlap_fraction_text_field;
     private   JTextArea                     values_save_path_text_field;
     private   JTextArea                     definitions_save_path_text_field;
     
     /**
      * GUI check boxes
      */
     private   JCheckBox                     extract_overall_only_check_box;
     private   JCheckBox                     save_window_features_check_box;
     private   JCheckBox                     save_overall_file_featurese_check_box;
     
     /**
      * GUI buttons
      */
     private   JButton                       values_save_path_button;
     private   JButton                       definitions_save_path_button;
     private   JButton                       extract_features_button;
     
     /**
      * GUI dialog boxes
      */
     private   JFileChooser                  save_file_chooser;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Set up frame.
      *
      * @param outer_frame	The GUI element that contains this object.
      */
     public FeatureSelectorPanel(OuterFrame outer_frame)
     {
          // Store containing panel
          this.outer_frame = outer_frame;
          
          // Set the file chooser to null initially
          save_file_chooser = null;
          
          // General container preparations containers
          int horizontal_gap = 6; // horizontal space between GUI elements
          int vertical_gap = 11; // horizontal space between GUI elements
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          
          // Find the list of available feature extractors
          populateFeatureExtractors();
          
          // Set up the list of feature extractors
          setUpFeatureTable();
          
          // Add an overall title for this panel
          add(new JLabel("FEATURES:"), BorderLayout.NORTH);
          
          // Set up buttons and text area
          JPanel control_panel = new JPanel(new GridLayout(7, 2, horizontal_gap, vertical_gap));
          extract_overall_only_check_box = new JCheckBox("Do Not Use Windows", true);
          extract_overall_only_check_box.setEnabled(false); // this is currently greyed out because windowing in the breakSequenceIntoWindows method of the MIDIMethods class remains to be finished
          extract_overall_only_check_box.addActionListener(this);
          control_panel.add(extract_overall_only_check_box);
          save_window_features_check_box = new JCheckBox("Save Features For Each Window", false);
          control_panel.add(save_window_features_check_box);
          control_panel.add(new JLabel(""));
          save_overall_file_featurese_check_box = new JCheckBox("Save For Overall Recordings", true);
          control_panel.add(save_overall_file_featurese_check_box);
          control_panel.add(new JLabel("Window Length (seconds):"));
          window_length_text_field = new JTextArea("10", 1, 20);
          control_panel.add(window_length_text_field);
          control_panel.add(new JLabel("Window Overlap (fraction):"));
          window_overlap_fraction_text_field = new JTextArea("0.0", 1, 20);
          control_panel.add(window_overlap_fraction_text_field);
          values_save_path_button = new JButton("Feature Values Save Path:");
          values_save_path_button.addActionListener(this);
          control_panel.add(values_save_path_button);
          values_save_path_text_field = new JTextArea("feature_values_1.xml", 1, 20);
          control_panel.add(values_save_path_text_field);
          definitions_save_path_button = new JButton("Feature Definitions Save Path:");
          definitions_save_path_button.addActionListener(this);
          control_panel.add(definitions_save_path_button);
          definitions_save_path_text_field = new JTextArea("feature_definitions_1.xml", 1, 20);
          control_panel.add(definitions_save_path_text_field);
          control_panel.add(new JLabel(""));
          extract_features_button = new JButton("Extract Features");
          extract_features_button.addActionListener(this);
          control_panel.add(extract_features_button);
          add(control_panel, BorderLayout.SOUTH);
          
          // Set up the greyed out options
          enableDisableWindowingOptions();
          
          // Cause the table to respond to double clicks
          addTableMouseListener();
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the values_save_path_button
          if (event.getSource().equals(values_save_path_button))
               browseFeatureValuesSavePath();
          
          // React to the definitions_save_path_button
          else if (event.getSource().equals(definitions_save_path_button))
               browseFeatureDefinitionsSavePath();
          
          // React to the extract_features_button
          else if (event.getSource().equals(extract_features_button))
               extractFeatures();
          
          // React to the extract_overall_only_check_box
          else if (event.getSource().equals(extract_overall_only_check_box))
               enableDisableWindowingOptions();
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Allow the user to choose a save path for the feature_vector_file XML
      * file where feature values are to be saved. The selected path is entered
      * in the values_save_path_text_field.
      */
     private void browseFeatureValuesSavePath()
     {
          String path = chooseSavePath();
          if (path != null)
               values_save_path_text_field.setText(path);
     }
     
     
     /**
      * Allow the user to choose a save path for the feature_key_file XML
      * file where feature values are to be saved. The selected path is entered
      * in the definitions_save_path_text_field.
      */
     private void browseFeatureDefinitionsSavePath()
     {
          String path = chooseSavePath();
          if (path != null)
               definitions_save_path_text_field.setText(path);
     }
     
     
     /**
      * Extract the features from all of the files added in the GUI. Use the
      * features and feature settings entered in the GUI. Save the results in a
      * feature_vector_file and the features used in a feature_key_file.
      */
     private void extractFeatures()
     {
          try
          {
               // Get the control parameters
               boolean save_features_for_each_window = save_window_features_check_box.isSelected();
               boolean save_overall_recording_features = save_overall_file_featurese_check_box.isSelected();
               String feature_values_save_path = values_save_path_text_field.getText();
               String feature_definitions_save_path = definitions_save_path_text_field.getText();
               double window_size = Double.parseDouble(window_length_text_field.getText());
               double window_overlap = Double.parseDouble(window_overlap_fraction_text_field.getText());
               
               // Get the recordings to extract features from and throw an exception
               // if there are none
               RecordingInfo[] recordings = outer_frame.recording_selector_panel.recording_list;
               if (recordings == null)
                    throw new Exception("No recordings available to extract features from.");
               
               // Find which features are selected to be saved
               boolean[] features_to_save = new boolean[feature_extractors.length];
               for (int i = 0; i < features_to_save.length; i++)
                    features_to_save[i] = ((Boolean) features_table_model.getValueAt(i, 0)).booleanValue();
               
               // Prepare to extract features
               MIDIFeatureProcessor processor = new MIDIFeatureProcessor( extract_overall_only_check_box.isSelected(),
                    window_size,
                    window_overlap,
                    feature_extractors,
                    features_to_save,
                    save_features_for_each_window,
                    save_overall_recording_features,
                    feature_values_save_path,
                    feature_definitions_save_path );
               
               // Extract features from recordings one by one and save them in XML files
               for (int i = 0; i < recordings.length; i++)
               {
                    File load_file = new File(recordings[i].file_path);
                    processor.extractFeatures(load_file);
               }
               
               // Finalize saved XML files
               processor.finalize();
               JOptionPane.showMessageDialog(null, "Features successfully extracted and saved.", "DONE", JOptionPane.INFORMATION_MESSAGE );
          }
          catch (Throwable t)
          {
               // React to the Java Runtime running out of memory
               if (t.toString().startsWith("java.lang.OutOfMemoryError"))
               {
                    JOptionPane.showMessageDialog(null, "The Java Runtime ran out of memory. Please rerun this program\n" +
                         "with a higher maximum amount of memory assignable to the Java\n" +
                         "Runtime heap.", "ERROR", JOptionPane.ERROR_MESSAGE);
               }
               else if (t instanceof Exception)
               {
                    Exception e = (Exception) t;
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }
          }
     }
     
     
     /**
      * Initialize the table displaying the features which can be extracted.
      */
     private void setUpFeatureTable()
     {
          // Find the descriptions of features that can be extracted
          FeatureDefinition[] feature_definitions = new FeatureDefinition[feature_extractors.length];
          for (int i = 0; i < feature_definitions.length; i++)
               feature_definitions[i] = feature_extractors[i].getFeatureDefinition();
          
          // Initialize features_table and features_table_model
          Object[] column_names = { new String("Save"),
          new String("Feature"),
          new String("Dimensions") };
          features_table_model = new FeatureSelectorTableModel( column_names,
               feature_definitions.length);
          features_table_model.fillTable(feature_definitions, feature_save_defaults);
          features_table = new JTable(features_table_model);
          
          // Set up and display the table
          features_scroll_pane = new JScrollPane(features_table);
          features_panel = new JPanel(new GridLayout(1, 1));
          features_panel.add(features_scroll_pane);
          add(features_panel, BorderLayout.CENTER);
          features_table_model.fireTableDataChanged();
          repaint();
          outer_frame.repaint();
     }
     
     
     /**
      * Makes it so that if a row is double clicked on, a description of the
      * corresponding feature is displayed along with its dependencies.
      */
     public void addTableMouseListener()
     {
          features_table.addMouseListener(new MouseAdapter()
          {
               public void mouseClicked(MouseEvent event)
               {
                    if (event.getClickCount() == 2)
                    {
                         int[] row_clicked = new int[1];
                         row_clicked[0] = features_table.rowAtPoint(event.getPoint());
                         FeatureDefinition definition = feature_extractors[row_clicked[0]].getFeatureDefinition();
                         String text =
                              "NAME: " + definition.name + "\n" +
                              "DESCRIPTION: " + definition.description + "\n" +
                              "IS SEQUENTIAL: " + definition.is_sequential + "\n" +
                              "DIMENSIONS: " + definition.dimensions;
                         JOptionPane.showMessageDialog(null, text, "FEATURE DETAILS", JOptionPane.INFORMATION_MESSAGE );
                         
                    }
               }
          });
     }
     
     
	 /**
	  * Returns an array of all the available feature extractors. An empty LinkedList may also
	  * be provided specifying whether or not each feature is to be extracted by default.
	  * 
	  * @param defaults	An <b>empty</b> Linked list that will be filled with whether or not
	  *					each feature is to be extracted by default. Entries will be in the
	  *					same order as the features in the returned array. If this is not
	  *					needed, then null can be specified for this argument.
	  * @return			An array of all the available features.
	  */
	 public static MIDIFeatureExtractor[] getAllAvailableFeatureExtractors(LinkedList<Boolean> defaults)
	 {
          // The list to hold available features
          LinkedList<MIDIFeatureExtractor> extractors = new LinkedList<MIDIFeatureExtractor>();

		  // Make a dummy list if none is specified
          if (defaults == null) defaults = new LinkedList<Boolean>();

          // Add non-sequential features
          extractors.add(new DurationFeature());
          defaults.add(new Boolean(true));

          // Add one-dimensional sequential features
          extractors.add(new AcousticGuitarFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AmountOfArpeggiationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageMelodicIntervalFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageNoteDurationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageNoteToNoteDynamicsChangeFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageNumberOfIndependentVoicesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageRangeOfGlissandosFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageTimeBetweenAttacksFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageTimeBetweenAttacksForEachVoiceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new AverageVariabilityOfTimeBetweenAttacksForEachVoiceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new BrassFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ChangesOfMeterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ChromaticMotionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new CombinedStrengthOfTwoStrongestRhythmicPulsesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new CompoundOrSimpleMeterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new DirectionOfMotionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new DistanceBetweenMostCommonMelodicIntervalsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new DominantSpreadFeature());
          defaults.add(new Boolean(true));
          extractors.add(new DurationOfMelodicArcsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ElectricGuitarFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ElectricInstrumentFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new GlissandoPrevalenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new HarmonicityOfTwoStrongestRhythmicPulsesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ImportanceOfBassRegisterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ImportanceOfHighRegisterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ImportanceOfLoudestVoiceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ImportanceOfMiddleRegisterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new InitialTempoFeature());
          defaults.add(new Boolean(true));
          extractors.add(new IntervalBetweenStrongestPitchClassesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new IntervalBetweenStrongestPitchesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MaximumNoteDurationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MaximumNumberOfIndependentVoicesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MelodicFifthsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MelodicIntervalsInLowestLineFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MelodicOctavesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MelodicThirdsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MelodicTritonesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MinimumNoteDurationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MostCommonMelodicIntervalFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MostCommonMelodicIntervalPrevalenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MostCommonPitchClassFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MostCommonPitchClassPrevalenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MostCommonPitchFeature());
          defaults.add(new Boolean(true));
          extractors.add(new MostCommonPitchPrevalenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NoteDensityFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfCommonMelodicIntervalsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfCommonPitchesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfModeratePulsesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfPitchedInstrumentsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfRelativelyStrongPulsesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfStrongPulsesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new NumberOfUnpitchedInstrumentsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new OrchestralStringsFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new OverallDynamicRangeFeature());
          defaults.add(new Boolean(true));
          extractors.add(new PercussionPrevalenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new PitchClassVarietyFeature());
          defaults.add(new Boolean(true));
          extractors.add(new PitchVarietyFeature());
          defaults.add(new Boolean(true));
          extractors.add(new PolyrhythmsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new PrimaryRegisterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new QualityFeature());
          defaults.add(new Boolean(true));
          extractors.add(new QuintupleMeterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RangeFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RangeOfHighestLineFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RelativeNoteDensityOfHighestLineFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RelativeRangeOfLoudestVoiceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RelativeStrengthOfMostCommonIntervalsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RelativeStrengthOfTopPitchClassesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RelativeStrengthOfTopPitchesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RepeatedNotesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RhythmicLoosenessFeature());
          defaults.add(new Boolean(true));
          extractors.add(new RhythmicVariabilityFeature());
          defaults.add(new Boolean(true));
          extractors.add(new SaxophoneFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new SecondStrongestRhythmicPulseFeature());
          defaults.add(new Boolean(true));
          extractors.add(new SizeOfMelodicArcsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StaccatoIncidenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StepwiseMotionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StrengthOfSecondStrongestRhythmicPulseFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StrengthOfStrongestRhythmicPulseFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StrengthRatioOfTwoStrongestRhythmicPulsesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StringEnsembleFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StringKeyboardFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StrongestRhythmicPulseFeature());
          defaults.add(new Boolean(true));
          extractors.add(new StrongTonalCentresFeature());
          defaults.add(new Boolean(true));
          extractors.add(new TripleMeterFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariabilityOfNoteDurationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariabilityOfNotePrevalenceOfPitchedInstrumentsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariabilityOfNotePrevalenceOfUnpitchedInstrumentsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariabilityOfNumberOfIndependentVoicesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariabilityOfTimeBetweenAttacksFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariationOfDynamicsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VariationOfDynamicsInEachVoiceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VibratoPrevalenceFeature());
          defaults.add(new Boolean(true));
          extractors.add(new ViolinFractionFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VoiceEqualityDynamicsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VoiceEqualityMelodicLeapsFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VoiceEqualityNoteDurationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VoiceEqualityNumberOfNotesFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VoiceEqualityRangeFeature());
          defaults.add(new Boolean(true));
          extractors.add(new VoiceSeparationFeature());
          defaults.add(new Boolean(true));
          extractors.add(new WoodwindsFractionFeature());
          defaults.add(new Boolean(true));

          // Add multi-dimensional sequential features
          extractors.add(new BasicPitchHistogramFeature());
          defaults.add(new Boolean(false));
          extractors.add(new BeatHistogramFeature());
          defaults.add(new Boolean(false));
          extractors.add(new FifthsPitchHistogramFeature());
          defaults.add(new Boolean(false));
          extractors.add(new InitialTimeSignatureFeature());
          defaults.add(new Boolean(false));
          extractors.add(new MelodicIntervalHistogramFeature());
          defaults.add(new Boolean(false));
          extractors.add(new NotePrevalenceOfPitchedInstrumentsFeature());
          defaults.add(new Boolean(false));
          extractors.add(new NotePrevalenceOfUnpitchedInstrumentsFeature());
          defaults.add(new Boolean(false));
          extractors.add(new PitchClassDistributionFeature());
          defaults.add(new Boolean(false));
          extractors.add(new PitchedInstrumentsPresentFeature());
          defaults.add(new Boolean(false));
          extractors.add(new TimePrevalenceOfPitchedInstrumentsFeature());
          defaults.add(new Boolean(false));
          extractors.add(new UnpitchedInstrumentsPresentFeature());
          defaults.add(new Boolean(false));

          // Return the results
		  return extractors.toArray(new MIDIFeatureExtractor[1]);
	 }


     /**
      * Returns the types of feature extractors that are currently available.
      *
      * @return	The available feature extractors.
      */
     private void populateFeatureExtractors()
     {
          LinkedList<Boolean> defaults = new LinkedList<Boolean>();
          feature_extractors = getAllAvailableFeatureExtractors(defaults);
          Boolean[] defaults_temp = defaults.toArray(new Boolean[1]);
          feature_save_defaults = new boolean[defaults_temp.length];
          for (int i = 0; i < feature_save_defaults.length; i++)
               feature_save_defaults[i] = defaults_temp[i].booleanValue();
     }
     
     
     /**
      * Allows the user to select or enter a file path using a JFileChooser.
      * If the selected path does not have an extension of .XML, it is given
      * this extension. If the chosen path refers to a file that already exists,
      * then the user is asked if s/he wishes to overwrite the selected file.
      *
      * <p>No file is actually saved or overwritten by this method. The selected
      * path is simply returned.
      *
      * @return	The path of the selected or entered file. A value of null is
      *		returned if the user presses the cancel button or chooses
      *		not to overwrite a file.
      */
     private String chooseSavePath()
     {
          // Create the JFileChooser if it does not already exist
          if (save_file_chooser == null)
          {
               save_file_chooser = new JFileChooser();
               save_file_chooser.setCurrentDirectory(new File("."));
               String[] accepted_extensions = {"xml"};
               save_file_chooser.setFileFilter(new mckay.utilities.general.FileFilterImplementation(accepted_extensions));
          }
          
          // Process the user's entry
          String path = null;
          int dialog_result = save_file_chooser.showSaveDialog(FeatureSelectorPanel.this);
          if (dialog_result == JFileChooser.APPROVE_OPTION) // only do if OK chosen
          {
               // Get the file the user chose
               File to_save_to = save_file_chooser.getSelectedFile();
               
               // Make sure has .xml extension
               path = to_save_to.getPath();
               String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(path);
               if (ext == null)
               {
                    path += ".xml";
                    to_save_to = new File(path);
               }
               else if (!ext.equals(".xml"))
               {
                    path = mckay.utilities.staticlibraries.StringMethods.removeExtension(path) + ".xml";
                    to_save_to = new File(path);
               }
               
               // See if user wishes to overwrite if a file with the same name exists
               if (to_save_to.exists())
               {
                    int overwrite = JOptionPane.showConfirmDialog( null,
                         "This file already exists.\nDo you wish to overwrite it?",
                         "WARNING",
                         JOptionPane.YES_NO_OPTION );
                    if (overwrite != JOptionPane.YES_OPTION)
                         path = null;
               }
          }
          
          // Return the selected file path
          return path;
     }
     
     
     /**
      * Disable or enable the windowing options based on the
      * extract_overall_only_check_box.
      */
     private void enableDisableWindowingOptions()
     {
          boolean enabled = !extract_overall_only_check_box.isSelected();
          save_window_features_check_box.setEnabled(enabled);
          save_overall_file_featurese_check_box.setEnabled(enabled);
          window_length_text_field.setEnabled(enabled);
          window_overlap_fraction_text_field.setEnabled(enabled);
     }
}