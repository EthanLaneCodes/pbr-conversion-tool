package pbr.converter.gui;

import pbr.converter.processor.PBRConverter;
import pbr.converter.processor.TextureProcessor;

import java.io.File;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DnDConstants;
import java.awt.GridBagConstraints;
import java.awt.dnd.DropTargetAdapter;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDropEvent;

/**
 * PBRConverterGUI provides a graphical user interface for selecting
 * configuration settings and initiating the conversion process.
 * 
 * PBRConverterGUI represents the view in the MVC design pattern.
 * 
 * This GUI does not handle the conversions itself, rather it tasks the
 * Controller {@link PBRConverter} to delegate the operations to the Model
 * {@link TextureProcessor}
 * 
 * @author Ethan B. Lane
 */
public class PBRConverterGUI {
	/**
	 * The main method. Creates the GUI and calls functions based on user
	 * interaction.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		Color gray = new Color(58, 60, 63);
		Color darkGray = new Color(45, 45, 50);
		Color lightGray = new Color(62, 62, 66);
		String versionNumber = "Version 1.0.0"; // Version Number

		JFrame frame = new JFrame("PBR Converter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 700);

		// Dark gray background
		frame.getContentPane().setBackground(gray);

		// Main content panel
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margins
		contentPanel.setBackground(gray);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10); // Larger spacing
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title row
		JLabel titleLabel = new JLabel("PBR Texture Converter", SwingConstants.LEADING);
		titleLabel.setFont(new Font("Calibri", Font.PLAIN, 32));
		titleLabel.setForeground(Color.CYAN);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 4; // Span across all columns
		contentPanel.add(titleLabel, gbc);

		// Labels
		JLabel baseColorLabel = createCustomLabel("Base Color Texture:");
		JLabel metalnessLabel = createCustomLabel("Metalness Texture:");
		JLabel roughnessLabel = createCustomLabel("Roughness Texture:");
		JLabel outputFolderLabel = createCustomLabel("Output Folder:");
		JLabel metalnessConstantLabel = createCustomLabel("Metalness Constant (Default: 0.28):");
		JLabel dielectricConstantLabel = createCustomLabel("Dielectric Constant (Default: 0.05):");

		// Text fields
		JTextField baseColorField = createDraggableTextField("Drag Base Color here");
		JTextField metalnessField = createDraggableTextField("Drag Metalness here");
		JTextField roughnessField = createDraggableTextField("Drag Roughness here");
		JTextField outputFolderField = createDraggableTextField("Drag Output Folder here");
		JTextField metalnessConstantField = createCustomTextField("0.28");
		JTextField dielectricConstantField = createCustomTextField("0.05");

		// Description area
		JTextArea descriptionArea = new JTextArea(
				"This tool allows you to drag and drop PBR textures of type Base Color (albedo)/Metallic/Roughness and convert them to Diffuse/Specular/Glossiness while maintaining control over conversion constants. \n\n"
						+ "The checkbox for embedding glossiness is for use in applications such as UDK, where you can have materials set up to use the alpha channel of the specular texture to store glossiness information. This will save you from having to combine them manually. \n\n"
						+ "Constants:\n"
						+ "Metalness Constant: Affects how metallic surfaces are rendered by defining the minimum amount of base color that will show up in the diffuse.\n\n"
						+ "Dielectric Constant: Also known as the reflectivity constant, defines the base reflectivity of non-metallic surfaces. "
						+ "Adjust these values if needed or leave as default.");
		descriptionArea.setEditable(false);
		descriptionArea.setBackground(gray);
		descriptionArea.setForeground(Color.CYAN);
		descriptionArea.setFont(new Font("Calibri", Font.PLAIN, 16)); // Larger font
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setLineWrap(true);
		descriptionArea.setBorder(BorderFactory.createLineBorder(Color.WHITE));

		// Buttons
		JButton baseColorButton = createCustomButton("Browse", darkGray);
		JButton metalnessButton = createCustomButton("Browse", darkGray);
		JButton roughnessButton = createCustomButton("Browse", darkGray);
		JButton outputFolderButton = createCustomButton("Browse", darkGray);
		JButton convertButton = createCustomButton("Convert", lightGray);

		// Checkbox
		JCheckBox embedGlossinessCheckbox = new JCheckBox("Embed Glossiness in Alpha of Specular");
		embedGlossinessCheckbox.setBackground(gray);
		embedGlossinessCheckbox.setForeground(Color.WHITE);

		// Add action listeners for file selection
		baseColorButton.addActionListener(e -> browseFile(frame, baseColorField));
		metalnessButton.addActionListener(e -> browseFile(frame, metalnessField));
		roughnessButton.addActionListener(e -> browseFile(frame, roughnessField));
		outputFolderButton.addActionListener(e -> browseFolder(frame, outputFolderField));

		convertButton.addActionListener(e -> {
			File baseColor = new File(baseColorField.getText());
			File metalness = new File(metalnessField.getText());
			File roughness = new File(roughnessField.getText());
			String outputFolder = outputFolderField.getText();

			double metalnessConstant;
			double dielectricConstant;
			try {
				metalnessConstant = Double.parseDouble(metalnessConstantField.getText());
				dielectricConstant = Double.parseDouble(dielectricConstantField.getText());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame, "Please enter valid numbers for the constants.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (!baseColor.exists() || !metalness.exists() || !roughness.exists()) {
				JOptionPane.showMessageDialog(frame, "Please select valid input files.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			boolean embedGloss = embedGlossinessCheckbox.isSelected();

			PBRConverter converter = new PBRConverter(outputFolder, metalnessConstant, dielectricConstant, embedGloss);
			converter.convertTextures(baseColor, metalness, roughness);
			JOptionPane.showMessageDialog(frame, "Conversion completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
		});

		// Add components to content panel
		addToGrid(contentPanel, baseColorLabel, 0, 1, gbc, 1);
		addToGrid(contentPanel, baseColorField, 1, 1, gbc, 2);
		addToGrid(contentPanel, baseColorButton, 3, 1, gbc, 1);

		addToGrid(contentPanel, metalnessLabel, 0, 2, gbc, 1);
		addToGrid(contentPanel, metalnessField, 1, 2, gbc, 2);
		addToGrid(contentPanel, metalnessButton, 3, 2, gbc, 1);

		addToGrid(contentPanel, roughnessLabel, 0, 3, gbc, 1);
		addToGrid(contentPanel, roughnessField, 1, 3, gbc, 2);
		addToGrid(contentPanel, roughnessButton, 3, 3, gbc, 1);

		addToGrid(contentPanel, outputFolderLabel, 0, 4, gbc, 1);
		addToGrid(contentPanel, outputFolderField, 1, 4, gbc, 2);
		addToGrid(contentPanel, outputFolderButton, 3, 4, gbc, 1);

		addToGrid(contentPanel, metalnessConstantLabel, 0, 5, gbc, 1);
		addToGrid(contentPanel, metalnessConstantField, 1, 5, gbc, 2);

		addToGrid(contentPanel, dielectricConstantLabel, 0, 6, gbc, 1);
		addToGrid(contentPanel, dielectricConstantField, 1, 6, gbc, 2);

		addToGrid(contentPanel, convertButton, 3, 5, gbc, 1);
		addToGrid(contentPanel, embedGlossinessCheckbox, 3, 6, gbc, 1);

		// Description panel
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setBackground(gray);
		Font desc = new Font("Calibri", Font.PLAIN, 16); // Attributes about the "Description" title
		descriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE),
				"Description", 0, 0, desc, Color.CYAN));
		descriptionPanel.setLayout(new BorderLayout());
		descriptionPanel.add(descriptionArea);
		descriptionPanel.setFont(new Font("Calibri", Font.PLAIN, 20));

		frame.setVisible(true);

		// Version row
		JLabel versionLabel = new JLabel(versionNumber + "  -  Ethan B. Lane", SwingConstants.LEADING);
		versionLabel.setBorder(new EmptyBorder(3, 5, 2, 0)); // Margins
		versionLabel.setFont(new Font("Calibri", Font.PLAIN, 12));
		versionLabel.setForeground(Color.CYAN);

		frame.setLayout(new BorderLayout());
		frame.add(contentPanel, BorderLayout.NORTH);
		frame.add(descriptionPanel, BorderLayout.CENTER);
		frame.add(versionLabel, BorderLayout.SOUTH);

		frame.setVisible(true);
	}

	private static JLabel createCustomLabel(String text) {
		JLabel label = new JLabel(text);
		label.setForeground(Color.CYAN);
		label.setFont(new Font("Calibri", Font.PLAIN, 16)); // Larger font
		return label;
	}

	private static JTextField createDraggableTextField(String placeholder) {
		JTextField textField = createCustomTextField(placeholder);
		textField.setPreferredSize(new Dimension(300, 30)); // Larger size

		// Enable drag-and-drop functionality
		new DropTarget(textField, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					List<File> droppedFiles = (List<File>) dtde.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
					if (!droppedFiles.isEmpty()) {
						textField.setText(droppedFiles.get(0).getAbsolutePath());
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Failed to process the dropped file.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		return textField;
	}

	private static JTextField createCustomTextField(String placeholder) {
		JTextField textField = new JTextField(placeholder);
		Color darkGray = new Color(45, 45, 50);
		textField.setBackground(darkGray);
		textField.setForeground(Color.CYAN);
		textField.setCaretColor(Color.CYAN);
		textField.setFont(new Font("Calibri", Font.PLAIN, 16)); // Larger font
		textField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		return textField;
	}

	private static JButton createCustomButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setBackground(color);
		button.setForeground(Color.CYAN);
		button.setFont(new Font("Calibri", Font.PLAIN, 16)); // Larger font
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		return button;
	}

	private static void addToGrid(JPanel panel, Component comp, int x, int y, GridBagConstraints gbc, int gridWidth) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = gridWidth; // Allows spanning multiple columns
		gbc.weightx = 1.0; // Enable horizontal scaling
		panel.add(comp, gbc);
	}

	private static void browseFile(JFrame frame, JTextField textField) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	private static void browseFolder(JFrame frame, JTextField textField) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}
}
