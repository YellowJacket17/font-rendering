import com.mlomb.freetypejni.Face;
import com.mlomb.freetypejni.FreeType;
import com.mlomb.freetypejni.Library;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.mlomb.freetypejni.FreeType.*;
import static com.mlomb.freetypejni.FreeTypeConstants.FT_LOAD_RENDER;

/**
 * This class contains functions to use regarding SDFs (Signed Distance Fields).
 * SDFs are used for rendering text.
 */
public class Sdf {

    /**
     * Generates a bitmap of the target character.
     *
     * @param codepoint character to render
     * @param fontFile file path of font from program root
     * @param fontSize size of SDF to generate
     */
    public static void generateCodepointBitmap(int codepoint, String fontFile, int fontSize) {

        int padding = 15;                                                                                               // Set 15-pixel padding around characters.
        int upscaleResolution = 1024;                                                                                   // Set resolution that character will scale up to.
        int spread = upscaleResolution / 2;                                                                             // Set how far we will search within this image to find another pixel.

        Library library = FreeType.newLibrary();                                                                        // Load FreeType library.
        assert(library != null);                                                                                        // Ensure that FreeType can actually be used.

        Face font = library.newFace(fontFile, 0);                                                                       // Create font using the font file passed in.
        FT_Set_Pixel_Sizes(font.getPointer(), 0, upscaleResolution);

        if (FT_Load_Char(font.getPointer(), (char)codepoint, FT_LOAD_RENDER)) {

            // TODO : Generate error for FreeType not able to load target character.
            free(library, font);
            return;
        }
        int glyphWidth = font.getGlyphSlot().getBitmap().getWidth();
        int glyphHeight = font.getGlyphSlot().getBitmap().getRows();
        byte[] glyphBitmap = new byte[glyphHeight * glyphWidth];
        ByteBuffer imageBuffer = font.getGlyphSlot().getBitmap().getBuffer()
                .get(glyphBitmap, 0,  glyphWidth * glyphHeight);                                                        // Places buffer inside of byte array.

        float widthScale = (float)glyphWidth / (float)upscaleResolution;
        float heightScale = (float)glyphHeight / (float)upscaleResolution;
        int characterWidth = (int)((float)fontSize * widthScale);
        int characterHeight = (int)((float)fontSize * heightScale);
        int bitmapWidth = characterWidth + padding * 2;                                                                 // Output bitmap; multiplied by two since padding on both left and right.
        int bitmapHeight = characterHeight + padding * 2;                                                               // Output bitmap; multiplied by two since padding on both top and bottom.
        float bitmapScaleX = (float)glyphWidth / (float)characterWidth;                                                 // How much larger the upscaled font compared to the output font.
        float bitmapScaleY = (float)glyphHeight / (float)characterHeight;                                               // ^^^
        int[] bitmap = new int[bitmapWidth * bitmapHeight];                                                             // Output (SDF bitmap).

        for (int y = -padding; y < (characterHeight + padding); y++) {                                                  // Loop through negative padding to positive padding (below and above).

            for (int x = -padding; x < (characterWidth + padding); x++) {                                               // Loop though negative to positive padding (left and right).

                int pixelX = (int)mapRange(x, -padding, characterWidth + padding,
                        -padding * bitmapScaleX, (characterWidth + padding) * bitmapScaleX);                            // Map pixel X into big glyph range.
                int pixelY = (int)mapRange(y, -padding, characterHeight + padding,
                        -padding * bitmapScaleY, (characterHeight + padding) * bitmapScaleY);                           // Map pixel Y into big glyph range.
                float val = findNearestPixel(pixelX, pixelY, glyphBitmap,glyphWidth, glyphHeight, spread);
                bitmap[(x + padding) + ((y + padding) * bitmapWidth)] = (int)(val * 255.0f);                            // Place nearest pixel inside of bitmap.
            }
        }
        BufferedImage testImage = new BufferedImage(bitmapWidth, bitmapHeight, BufferedImage.TYPE_INT_ARGB);
        int x = 0;
        int y = 0;

        for (int byteAsInt : bitmap) {

            int argb = (255 << 24) | (byteAsInt << 16) | (byteAsInt << 8) | byteAsInt;
            testImage.setRGB(x, y, argb);
            x++;

            if (x >= bitmapWidth) {

                x = 0;
                y++;
            }

            if (y >= bitmapHeight) {

                break;
            }
        }

        try {
            File output = new File("test_sdf.png");
            ImageIO.write(testImage, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        free(library, font);
    }


    /**
     * Frees library and font memory
     *
     * @param library library
     * @param font font
     */
    private static void free(Library library, Face font) {

        FT_Done_Face(font.getPointer());
        FT_Done_FreeType(library.getPointer());
    }


    /**
     * Finds the nearest pixel to the passed X and Y of a different state inside the passed bitmap.
     * The Pythagorean theorem is used.
     *
     * @param pixelX target pixel (X)
     * @param pixelY target pixel (Y)
     * @param bitmap target bitmap
     * @param width bitmap width
     * @param height bitmap height
     * @param spread spread factor (i.e., radius of square region around pixel to check)
     * @return nearest pixel
     */
    private static float findNearestPixel(int pixelX, int pixelY, byte[] bitmap,
                                          int width, int height, int spread) {

        int state = getPixel(pixelX, pixelY, bitmap, width, height);
        int minX = pixelX - spread;
        int maxX = pixelX + spread;
        int minY = pixelY - spread;
        int maxY = pixelY + spread;
        float minDistance = spread * spread;

        for (int y = minY; y < maxY; y++) {

            for (int x = minX; x < maxX; x++) {

                int pixelState = getPixel(x, y, bitmap, width, height);
                float dxSquared = (x - pixelX) * (x - pixelX);
                float dySquared = (y - pixelY) * (y - pixelY);
                float distanceSquared = dxSquared + dySquared;

                if (pixelState != state) {

                    minDistance = Math.min(distanceSquared, minDistance);
                }
            }
        }
        minDistance = (float)Math.sqrt(minDistance);
        float output = (minDistance - 0.5f) / (spread - 0.5f);
        output *= ((state == 0) ? -1 : 1);                                                                              // If off (black) pixel, multiply by -1; if on (white) pixel, multiply by 1.

        return (output + 1) * 0.5f;                                                                                     // Map from [-1, 1] to [0, 1].
    }


    /**
     * Retrieves the target pixel.
     *
     * @param x target pixel (X)
     * @param y target pixel (Y)
     * @param bitmap target bitmap
     * @param width bitmap width
     * @param height bitmap height
     * @return pixel; returns 1 if found, 0 if not
     */
    private static int getPixel(int x, int y, byte[] bitmap, int width, int height) {

        if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {                                                      // Check if in range of bitmap.

            return (((bitmap[x + y * width] & 0xFF) == 0) ? 0 : 1);
        }
        return 0;
    }


    /**
     * Maps values into the specified range.
     *
     * @param val
     * @param inMin
     * @param inMax
     * @param outMin
     * @param outMax
     * @return output
     */
    private static float mapRange(float val, float inMin, float inMax, float outMin, float outMax) {

        return (val - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
