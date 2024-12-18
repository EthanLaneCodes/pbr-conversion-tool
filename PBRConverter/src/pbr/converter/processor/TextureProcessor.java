package pbr.converter.processor;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import pbr.converter.gui.PBRConverterGUI;

/**
 * TextureProcessor class controls the primary calculations used in the
 * conversions of the images (Base Color, Metalness, Roughness) -> (Diffuse,
 * Specular, Glossiness).
 * 
 * Handles reading image data, plugging data into conversion equation, and
 * writing results.
 * 
 * TextureProcessor represents the Model in the MVC design pattern.
 * 
 * @author Ethan B. Lane
 */
public class TextureProcessor {
	/**
	 * Metalness Constant Which will be read in from the {@link PBRConverter} class,
	 * where further definitions can be found.
	 * 
	 * (Effectively the minimum amount of base color found in metals).
	 */
	private double metalnessConstant;
	/**
	 * Dielectric Constant which will be read in from the {@link PBRConverter}
	 * class, where further definitions can be found.
	 * 
	 * (Effectively controls how shiny non-metals are)
	 */
	private double dielectricConstant;

	/**
	 * Two arg constructor taking in the metalness and dielectric constants.
	 * 
	 * @param metalnessConstant  Metalness Constant
	 * @param dielectricConstant Dielectric Constant
	 */
	public TextureProcessor(double metalnessConstant, double dielectricConstant) {
		this.metalnessConstant = metalnessConstant;
		this.dielectricConstant = dielectricConstant;
	}

	/**
	 * Called after "Convert" is selected in the GUI. Handles the
	 * conversion/processing using the files provided by the user and whether or not
	 * to embed glossiness.
	 * 
	 * @param baseColorFile          File with Base Color
	 * @param metalnessFile          File with Metalness
	 * @param roughnessFile          File with Roughness
	 * @param outputFolderPath       Location of output folder on the system
	 * @param embedGlossinessInAlpha Truth value for embedding glossiness passed
	 *                               from checkbox in {@link PBRConverterGUI}
	 * @throws IllegalArgumentException with custom message if images do not have
	 *                                  the same dimensions.
	 */
	public void processTextures(File baseColorFile, File metalnessFile, File roughnessFile, String outputFolderPath,
			boolean embedGlossinessInAlpha) throws Exception {
		BufferedImage baseColor = ImageIO.read(baseColorFile);
		BufferedImage metalness = ImageIO.read(metalnessFile);
		BufferedImage roughness = ImageIO.read(roughnessFile);

		if (!areDimensionsEqual(baseColor, metalness, roughness)) {
			throw new IllegalArgumentException("Input images must have the same dimensions.");
		}

		int width = baseColor.getWidth();
		int height = baseColor.getHeight();

		// Declare new images for diff/spec/gloss with dimensions and type ARGB
		BufferedImage diffuse = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage specular = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage glossiness = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < height; y++) { // Loops over vertical pixels
			for (int x = 0; x < width; x++) { // Loops over horizontal pixels
				int baseColorRGB = baseColor.getRGB(x, y); // Grab value of pixel from each input texture.
				int metalnessRGB = metalness.getRGB(x, y);
				int roughnessRGB = roughness.getRGB(x, y);

				double[] baseColorValues = extractValues(baseColorRGB);
				double metalnessValue = extractValues(metalnessRGB)[0]; // Assume metalness is grayscale
				double roughnessValue = extractValues(roughnessRGB)[0]; // Assume roughness is grayscale

				// Declare new variables for the RGB value of diff and spec at the given pixel
				double[] diffuseValues = new double[3];
				double[] specularValues = new double[3];

				// Loop through R, G, and B, doing math for diff and spec
				for (int i = 0; i < 3; i++) {

					// Core conversion calculations

					// Diffuse Equation
					// The min function ensures the base color is not multiplied by a value greater
					// than 1 (effectively clamps the brightness of non-metals)
					diffuseValues[i] = (Math.min(1.0, (1 - metalnessValue) + metalnessConstant)) * baseColorValues[i];

					// Specular Equation
					specularValues[i] = (baseColorValues[i] * metalnessValue)
							+ (dielectricConstant * (1 - metalnessValue));
				}

				// Declare and initialize glossiness value with conversion applied
				// TODO - Fix glossiness looking too dark
				double glossinessValue = 1 - roughnessValue;

				// Sets pixel of diffuse texture from the resulting calculations and
				// converts back into ARGB format
				diffuse.setRGB(x, y, convertToRGB(diffuseValues));

				if (embedGlossinessInAlpha) {
					int alphaChannel = (int) (glossinessValue * 255);
					specular.setRGB(x, y, (alphaChannel << 24) | (convertToRGB(specularValues) & 0x00FFFFFF));
				} else {
					specular.setRGB(x, y, convertToRGB(specularValues));
					glossiness.setRGB(x, y,
							convertToRGB(new double[] { glossinessValue, glossinessValue, glossinessValue }));
				}
			}
		}

		ImageIO.write(diffuse, "png", new File(outputFolderPath, "diffuse.png"));
		ImageIO.write(specular, "png", new File(outputFolderPath, "specular.png"));

		// Write glossiness texture only if not embedded
		if (!embedGlossinessInAlpha) {
			ImageIO.write(glossiness, "png", new File(outputFolderPath, "glossiness.png"));
		}
	}

	/**
	 * Helper method. Determines if dimensions of the user-provided textures are the
	 * same.
	 * 
	 * @param images Images to compare.
	 * @return True if dimensions are equal. False otherwise.
	 */
	private boolean areDimensionsEqual(BufferedImage... images) {
		int width = images[0].getWidth();
		int height = images[0].getHeight();
		for (BufferedImage img : images) {
			if (img.getWidth() != width || img.getHeight() != height) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper method used for taking the integer ARGB value of a pixel from the
	 * source image {@link ImageIO} and converting it into a more usable normalized
	 * RGB value for calculations.
	 * 
	 * The integer input is separated into sets of 8 bits, where the first 8 bits of
	 * the ARGB value is blue, second 8 are green, and next 8 bits are red. Value is
	 * then divided by 255 to be normalized to a more convenient value between 0 and
	 * 1.
	 * 
	 * @param rgb RGB value of the given pixel
	 * @return An array of double values for red/green/blue between 0 and 1
	 */
	private double[] extractValues(int rgb) {
		// Divided by 255 to be normalized between a value of 0 and 1
		double red = ((rgb >> 16) & 0xFF) / 255.0; // Extract red channel - Third set of 8 bits in ARGB value
		double green = ((rgb >> 8) & 0xFF) / 255.0; // Extract green channel - Second 8 bits in ARGB value
		double blue = (rgb & 0xFF) / 255.0; // Extract blue channel - First 8 bits in ARGB value
		return new double[] { red, green, blue };
	}

	/**
	 * Helper method used for taking the normalized RGB value of a pixel after
	 * calculations and turning it back into an integer ARGB value to be used by
	 * {@link BufferedImage}.
	 * 
	 * @param values normalized RGB values that were used for calculations.
	 * @return integer ARGB value for the completed conversion at the given pixel.
	 */
	private int convertToRGB(double[] values) {
		int red = (int) (values[0] * 255);
		int green = (int) (values[1] * 255);
		int blue = (int) (values[2] * 255);
		return (0xFF << 24) | (red << 16) | (green << 8) | blue; // Combine channels into ARGB
	}
}