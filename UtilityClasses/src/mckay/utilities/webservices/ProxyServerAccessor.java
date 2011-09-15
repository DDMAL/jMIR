/*
 * ProxyServerAccessor.java
 * Version 3.0.1
 *
 * Last modified on July 7, 2010.
 * University of Waikato
 */
package mckay.utilities.webservices;

import mckay.utilities.staticlibraries.FileMethods;
import mckay.utilities.staticlibraries.StringMethods;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Provides functionality for configuring web access via a proxy server. First
 * checks to see if web access is enabled, and does nothing more if it is.
 * If web access is not enabled, then checks a settings file to see if proxy
 * server setting presets are available, and configures proxy access using
 * them if they are. If such a settings file is not present or does not contain
 * valid settings, then the user is presented with a dialog box allowing them to
 * enter these settings and, if desired, save them to a settings file
 * so they do not need to be entered again. Either way, proxy server access is
 * then configured. Proxy settings files are saved so that only the user has
 * read or write access, and they are encrypted.
 *
 * @author Cory McKay
 */
public class ProxyServerAccessor
		extends JFrame
		implements ActionListener
{
	/* FIELDS *****************************************************************/


	// The path of the log file for saving and reading proxy access settings
	private String settings_save_path;

	// A URL to use to test access to the web
	private String test_url;

	// Whether to save entered settings so that the user does not have to
	// enter them repeatedly.
	private boolean save_settings = true;

	// Proxy server access settings
	private String proxy_server = "";
	private String port = "80";
	private String username = "";
	private String password = "";

	// GUI user entry fields
	private TextField proxy_server_field;
	private TextField port_field;
	private TextField username_field;
	private TextField password_field;
	private JCheckBox save_settings_checkbox;

	// GUI button to indicate that the proxy server should not be accessed
	private JButton cancel_button;

	// GUI button to access the proxy server using the entered settings, and
	// save the entered settings to disk if this option is selected
	private JButton ok_button;


	/* CONSTRUCTOR ************************************************************/


	/**
	 * Check if web access is possible. If so, do nothing more. If not, check if
	 * a valid proxy settings file is available at the specified path. If so,
	 * parse the settings file and use it to configure the proxy server. If not,
	 * construct the GUI so that settings can be entered and potentially saved.
	 * An error dialog box is shown if any problems occur.
	 *
	 * @param settings_save_path	The path of the proxy configuration file to
	 *								be parsed or saved. Defaulted to
	 *								"|user_home|/|user_name|_proxy_settings.cfg"
	 *								if null. This file will be generated and
	 *								parsed by objects of this class, and is
	 *								optional.
	 * @param test_url				A URL to use to test if web access is
	 *								available. Defaulted to
	 *								"http://www.google.com" if null.
	 */
	public ProxyServerAccessor(String settings_save_path, String test_url)
	{
		try
		{
			// Validate parameters
			if (settings_save_path == null)
			{
				String home_directory = System.getProperty("user.home");
				String user_name = System.getProperty("user.name");
				String suffix = "_proxy_settings.cfg";
				String fs = System.getProperty("file.separator");

				settings_save_path = home_directory + fs + user_name + suffix;
			}
			if (test_url == null)
				test_url = "http://www.google.com";

			// Test web access
			boolean works = isURLAccessible(test_url, null);

			// Continue on if web access is not available
			if (!works)
			{
				// Store fields
				this.settings_save_path = settings_save_path;
				this.test_url = test_url;

				// Attempt to load and configure the proxy server with the config file
				try { loadAndConfigureBasedOnSettingsFile(); }

				// Set up and display the GUI if the prsoxy server config file was
				// not succesfully accessed and used
				catch (Exception e) { initializeGUI(); }
			}
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}


	/* PUBLIC STATIC METHODS **************************************************/


	/**
	 * Tests to see if the given URL can be accessed.
	 *
	 * @param	test_url		The URL to use for testing web access. Note that
	 *							the protocol must be specified.
	 *							Example: "http://www.google.com".
	 * @param	exception_store	An array of size 1. If an Exception is thrown
	 *							during testing, then the first entry of this
	 *							array will be set to this exception. If null is
	 *							passed here, or if the passed array has a size
	 *							other than 1, then it is ignored.
	 * @return					True if the URL could be succesfully accessed,
	 *							false otherwise.
	 */
	public static boolean isURLAccessible(final String test_url,
		Exception[] exception_store)
	{
		boolean accessible = false;
		try
		{
			URLConnection url_connection = (new URL(test_url)).openConnection();
			HttpURLConnection connection = (HttpURLConnection) url_connection;
			connection.setConnectTimeout(1500); // 1.5 seconds
			connection.setReadTimeout(1500); // 1.5 seconds
			connection.setInstanceFollowRedirects(true);

			connection.connect();
			connection.getContent();
			connection.disconnect();

			accessible = true;
		}
		catch (Exception e)
		{
			if (exception_store != null)
				if(exception_store.length != 1)
					exception_store[0] = e;
		}
		return accessible;
	}


	/**
	 * Enables http web access through a proxy server. This method only needs
	 * to be called once, and all http web access from then on in the current
	 * instantiation of the JRE will function without any further interaction.
	 *
	 * @param proxy_server	The network address of the proxy server
	 *						(e.g. "wwwcache.cs.waikato.ac.nz").
	 * @param port			The port used with the proxy server (e.g. "80").
	 * @param username		The username needed to access the proxy server.
	 *						If this is not required, then "" should be passed.
	 * @param password		The password needed to access the proxy server.
	 *						If this is not required, then "" should be passed.
	 */
	public static void accessWebViaProxyServer(final String proxy_server,
			final String port,
			final String username,
			final String password)
	{
		System.setProperty("http.proxyHost", proxy_server);
		System.setProperty("http.proxyPort", port);

		if (!username.equals("") || !password.equals(""))
		{
			Authenticator.setDefault(new java.net.Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});
		}
	}


	/* PUBLIC METHODS *********************************************************/


	/**
	 * Calls the appropriate methods when the buttons are pressed.
	 *
	 * @param	event		The event that is to be reacted to.
	 */
	public void actionPerformed(ActionEvent event)
	{
		// React to the cancel button
		if (event.getSource().equals(cancel_button))
		{
			done();
		}

		// React to the ok button
		else if (event.getSource().equals(ok_button))
		{
			try {configureProxy();}
			catch (Exception e)
			{
				// e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	/* PRIVATE METHODS ********************************************************/

	
	/**
	 * Attempt to load the proxy server settings file. If this is done
	 * succesfully then the proxy server accesss settings are configured
	 * based on this file and a test is run to see if the web can
	 * be accessed then. Note that the file is decrypted.
	 * 
	 * @throws Exception	Thrown if the settings file is null, inaccessible
	 *						or invalid. Also thrown if the web is still not
	 *						accessible using these settings.
	 */
	private void loadAndConfigureBasedOnSettingsFile()
			throws Exception
	{
		// Read in the settings file as an array of bytes
		File input_file = new File(settings_save_path);
		FileMethods.validateFile(input_file, true, false);
		byte encrypted_bytes[] = new byte[(int)input_file.length()];
		FileInputStream input_stream = new FileInputStream(input_file);
		input_stream.read(encrypted_bytes);
		input_stream.close();

		// Decrypt the bytes using the user's username as a key
		String key = System.getProperty("user.home");
		String decrypted_text = StringMethods.passwordBasedDecrypt(encrypted_bytes, key);

		// Parse the decrypted text
		String[] parsed_lines = StringMethods.breakIntoTokens(decrypted_text, "\n");

		// Set fields based on the parsed data
		proxy_server = parsed_lines[0];
		port = parsed_lines[1];
		username = parsed_lines[2];
		password = parsed_lines[3];

		// Setup proxy server access
		accessWebViaProxyServer(proxy_server, port, username, password);

		// Test web access
		boolean works = isURLAccessible(test_url, null);
		if (!works) throw new Exception("Could not access " + test_url);
	}


	/**
	 * Save the proxy server settings file. The file is saved such that only the
	 * user has read or write access, and it is encrypted.
	 *
	 * @throws Exception An exception is thrown if a problem occurs.
	 */
	private void saveSettingsFile()
			throws Exception
	{
		// Prepare the data to save
		String nl = System.getProperty("line.separator");
		String data_to_save = proxy_server + nl + port + nl + username + nl + password + nl;

		// Encrypt the data to save using the user's username as a key
		String key = System.getProperty("user.home");
		byte[] encrypted = StringMethods.passwordBasedEncrypt(data_to_save, key);

		// Save the data
		File save_file = FileMethods.getNewFileForWriting(settings_save_path, true);
		FileOutputStream writer = new FileOutputStream(save_file);
		writer.write(encrypted);
		writer.close();

		// Set file permissions so only the user has read access
		save_file.setReadable(false, false);
		save_file.setReadable(true, true);
	}


	/**
	 * Initialize and display the GUI. Then waits for user interaction.
	 *
	 * @throws Exception An informative exception is thrown if a problem occurs.
	 */
	private synchronized void initializeGUI()
			throws Exception
	{
		// Configure overall window settings
		setTitle("Proxy Server Settings");
		int preferred_width = 350; // preferred width of each field field
		int preferred_height = 20; // preferred height of each field field
		int horizontal_gap = 6; // horizontal space between GUI elements
		int vertical_gap = 11; // horizontal space between GUI elements

		// Cause program to react as if the cancel button were pressed when the
		// exit box is pressed
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				done();
			}
		});

		// Instantiate and configure display fields
		proxy_server_field = new TextField();
		proxy_server_field.setEditable(true);
		proxy_server_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
		port_field = new TextField();
		port_field.setEditable(true);
		port_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
		username_field = new TextField();
		username_field.setEditable(true);
		username_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
		password_field = new TextField();
		password_field.setEditable(true);
		password_field.setPreferredSize(new Dimension(preferred_width, preferred_height));

		// Mask password entry
		password_field.setEchoChar('*');

		// Instantiate and configure buttons and their ActionListener
		cancel_button = new JButton("Do Not Use Proxy");
		cancel_button.addActionListener(this);
		ok_button = new JButton("Enable Proxy Access");
		ok_button.addActionListener(this);

		// Instantiate checkbox and add an ActionListener
		save_settings_checkbox = new JCheckBox("Save these settings");
		save_settings_checkbox.addActionListener(this);

		// Set default UI values
		setDefaultUIValues();

		// Prepare panels
		Container content_pane = getContentPane();
		content_pane.setLayout(new GridLayout(6, 1, horizontal_gap, vertical_gap));
		JPanel proxy_server_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
		JPanel port_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
		JPanel username_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
		JPanel password_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
		JPanel field_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
		JPanel button_panel = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));

		// Add labels and previously created elements to panels and container
		proxy_server_panel.add(new JLabel("Proxy server:"), BorderLayout.WEST);
		proxy_server_panel.add(proxy_server_field, BorderLayout.EAST);
		content_pane.add(proxy_server_panel);
		port_panel.add(new JLabel("Port:"), BorderLayout.WEST);
		port_panel.add(port_field, BorderLayout.EAST);
		content_pane.add(port_panel);
		username_panel.add(new JLabel("Username:"), BorderLayout.WEST);
		username_panel.add(username_field, BorderLayout.EAST);
		content_pane.add(username_panel);
		password_panel.add(new JLabel("Password:"), BorderLayout.WEST);
		password_panel.add(password_field, BorderLayout.EAST);
		content_pane.add(password_panel);
		button_panel.add(cancel_button);
		button_panel.add(ok_button);
		content_pane.add(save_settings_checkbox);
		content_pane.add(button_panel);

		// Put elements together
		pack();

		// Center the dialog box
		this.setLocationRelativeTo(null);

		// Display this dialog box
		this.setVisible(true);

		// Wait for the user's interaction
		wait();
	}


	/**
	 * Set UI values based on the field values.
	 */
	private	void setDefaultUIValues()
	{
		proxy_server_field.setText(proxy_server);
		port_field.setText(port);
		username_field.setText(username);
		password_field.setText(password);
		save_settings_checkbox.setSelected(save_settings);
	}


	/**
	 * Read the data stored in the UI components and store it in the fields.
	 */
	private void readUIFields()
	{
		// Read JTextFields
		if (proxy_server_field.getText().length() > 0)
			proxy_server = proxy_server_field.getText();
		if (port_field.getText().length() > 0)
			port = port_field.getText();
		if (username_field.getText().length() > 0)
			username = username_field.getText();
		if (password_field.getText().length() > 0)
			password = password_field.getText();

		// Read the JCheckBox
		save_settings = save_settings_checkbox.isSelected();
	}


	/**
	 * Configure proxy server access using the settings entered on the GUI, save
	 * the settings file if this option is selected, test to see if the
	 * connection works with these settings and dispose of this window if it did
	 * work. The file is saved such that only the user has read or write access,
	 * and it is encrypted.
	 *
	 * @throws Exception An informative exception is thrown if a problem occurs.
	 */
	private void configureProxy()
			throws Exception
	{
		// Update setting fields based on entered values
		readUIFields();

		// Save the configuration file
		if (save_settings) saveSettingsFile();

		// Configure the proxy
		accessWebViaProxyServer(proxy_server, port, username, password);

		// Test the connection now
		boolean works = isURLAccessible(test_url, null);
		if (!works)
			JOptionPane.showMessageDialog(null, "Could not access the web with these settings.", "ERROR", JOptionPane.ERROR_MESSAGE);

		// Stop waiting for user input and dispose of this window
		if (works)
			done();
	}


	/**
	 * Stop waiting for user input and dispose of this window.
	 */
	private synchronized void done()
	{
		notify();
		dispose();
	}
}