package pbr.converter.processor;

import java.io.File;

import pbr.converter.gui.PBRConverterGUI;

/**
 * The PBRConverter class controls conversions from PBR textures to non-PBR
 * textures. It reads inputs from the GUI such as file paths of textures, and
 * user-defined values for constants. Invokes {@link TextureProcessor} class to
 * handle conversion math.
 * 
 * PBRConverter represents the controller in the MVC design pattern.
 * 
 * @author Ethan B. Lane
 */
public class PBRConverter {

	/**
	 * Default value for Metalness Constant
	 */
	private static final double METALNESS_CONSTANT = 0.28;
	/**
	 * Default value for Dielectric Constant
	 */
	private static final double DIELECTRIC_CONSTANT = 0.05;
	/**
	 * Boolean for whether or not to embed glossiness texture in the alpha channel
	 * of the specular output texture. Truth value is received from
	 * {@link PBRConverterGUI} checkbox and before being passed to
	 * {@link TextureProcessor}.
	 */
	boolean embedGloss = false;
	/**
	 * Affects how metallic surfaces are rendered by defining the minimum amount of
	 * base color that will show up in the diffuse.
	 */
	private double metalnessConstant = 0.28;
	/**
	 * Also known as the reflectivity constant, defines the base reflectivity of
	 * non-metallic surfaces. Adjust these values if needed or leave as default.
	 */
	private double dielectricConstant = 0.05;
	/**
	 * String value for output folder location on the system.
	 */
	private String outputFolderPath;

	/**
	 * Constructor that takes arguments for folder path, constants, and embedding
	 * glossiness.
	 * 
	 * @param outputFolderPath Desired location for the output folder on the system.
	 * @param metalConst       Metalness Constant.
	 * @param dielConst        Dielectric Constant.
	 * @param embedGloss       Whether or not to embed gloss in spec.
	 */
	public PBRConverter(String outputFolderPath, double metalConst, double dielConst, boolean embedGloss) {
		this.outputFolderPath = outputFolderPath;
		this.dielectricConstant = dielConst;
		this.metalnessConstant = metalConst;
		this.embedGloss = embedGloss;
	}

	/**
	 * Single-arg constructor for PBRConverter that constructs constants to default
	 * values.
	 * 
	 * @param outputFolderPath File path for output folder
	 */
	public PBRConverter(String outputFolderPath) {
		this.outputFolderPath = outputFolderPath;
		this.dielectricConstant = DIELECTRIC_CONSTANT;
		this.metalnessConstant = METALNESS_CONSTANT;
	}

	/**
	 * Initiates conversion of textures using a {@link TextureProcessor} object and
	 * calling its process method. Handles errors relating to directories and
	 * propagates exceptions to {@link TextureProcessor}.
	 * 
	 * @param baseColor File for base color being converted
	 * @param metalness File for metalness being converted
	 * @param roughness File for roughness being converted
	 */
	public void convertTextures(File baseColor, File metalness, File roughness) {
		// Ensure the output folder exists
		File outputFolder = new File(outputFolderPath);
		if (!outputFolder.exists()) {
			if (!outputFolder.mkdirs()) {
				System.err.println("Error: Failed to create output folder at " + outputFolderPath);
				return;
			}
		}

		try {
			TextureProcessor processor = new TextureProcessor(metalnessConstant, dielectricConstant);
			processor.processTextures(baseColor, metalness, roughness, outputFolderPath, embedGloss);
			System.out.println("Textures successfully converted and saved in: " + outputFolderPath);
		} catch (Exception e) {
			System.err.println("Error during texture conversion: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
