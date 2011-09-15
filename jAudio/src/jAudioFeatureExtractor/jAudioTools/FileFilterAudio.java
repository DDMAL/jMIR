/*
 * @(#)FileFilterAudio.java	1.02	June 25, 2010.
 *
 * McGill University
 */

package jAudioFeatureExtractor.jAudioTools;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * A file filter for the <code>JFileChooser</code> class.
 * Implements the two methods of the <code>FileFilter</code>
 * abstract class.
 *
 * <p>Filters all files except directories and files that
 * end with .wav, .wave, .aif, .aiff, .aifc, .au,  .snd,
 * .ogg, .oga and .mp3 (case is ignored).
 *
 * <p><b>WARNING:</b> Future additions will be made, as
 * the Java SDK comes to support more file types.
 *
 * @author	Cory McKay
 * @see		FileFilter
 */
public class FileFilterAudio
	extends FileFilter
{
	public boolean accept(File f)
	{
		boolean acceptable_extension = false;
		String lowercase_file_name = f.getName().toLowerCase();
		if ( f.isDirectory() ||
		     lowercase_file_name.endsWith(".wav") ||
		     lowercase_file_name.endsWith(".wave") ||
		     lowercase_file_name.endsWith(".aif") ||
		     lowercase_file_name.endsWith(".aiff") ||
		     lowercase_file_name.endsWith(".aifc") ||
		     lowercase_file_name.endsWith(".au") ||
		     lowercase_file_name.endsWith(".snd") ||
		     lowercase_file_name.endsWith(".ogg") ||
		     lowercase_file_name.endsWith(".oga") ||
		     lowercase_file_name.endsWith(".mp3") )
			acceptable_extension = true;
		return acceptable_extension;
	}

	public String getDescription()
	{
		return "Audio (wav, wave, aif, aiff, aifc, au, snd, ogg, oga and mp3) files";
	}
}