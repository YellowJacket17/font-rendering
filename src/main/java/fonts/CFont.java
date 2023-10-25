package fonts;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * This class represents a font.
 */
public class CFont {

    // FIELDS
    private final String filePath;
    private final int fontSize;
    private int width, height, lineHeight;
    private HashMap<Integer, CharInfo> charMap = new HashMap<>();
    private int textureId;


    // CONSTRUCTOR
    /**
     * Constructs a CFont instance.
     *
     * @param filePath file path of font from resources directory
     * @param fontSize font scale (controls font resolution)
     */
    public CFont(String filePath, int fontSize) {
        this.filePath = filePath;
        this.fontSize = fontSize;
        generateBitmap();
    }


    // METHODS
    /**
     * Generates a bitmap for this font.
     */
    public void generateBitmap() {

        // Create new font from loaded file.
        Font font = new Font(this.filePath, Font.PLAIN, fontSize);

        // Create fake image to get font information.
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // Initialize fake image dimensions.
        int estimatedWidth = (int)Math.sqrt(font.getNumGlyphs()) * font.getSize() + 1;
        width = 0;
        height = fontMetrics.getHeight();
        lineHeight = fontMetrics.getHeight();
        int x = 0;
        int y = (int)(fontMetrics.getHeight() * 1.4f);

        // Loop through all glyphs and calculate what actual image dimensions must be.
        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                CharInfo charInfo = new CharInfo(x, y, fontMetrics.charWidth(i), fontMetrics.getHeight());
                charMap.put(i, charInfo);
                width = Math.max(x + fontMetrics.charWidth(i), width);                                                  // Take whichever width is bigger.
                x += charInfo.getWidth();
                if (x > estimatedWidth) {
                    x = 0;
                    y += fontMetrics.getHeight() + 1.4f;
                    height += fontMetrics.getHeight() * 1.4f;
                }
            }
        }
        height += fontMetrics.getHeight() * 1.4f;

        // Dispose of graphics context of fake image since no longer needed.
        g2d.dispose();

        // Create real image.
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        // Draw glyphs onto real image.
        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                CharInfo info = charMap.get(i);
                charMap.get(i).calculateTextureCoordinates(width, height);
                g2d.drawString("" + (char)i, info.getSourceX(), info.getSourceY());
            }
        }

        // Dispose of graphics context of real image (buffered image still contains all data).
        g2d.dispose();

        // Create image in file system.
//        try {
//            File file = new File("temp.png");
//            ImageIO.write(image, "png", file);
//        } catch (IOException e) {
//            // TODO : Handle this exception differently.
//            e.printStackTrace();
//        }

        // Create texture from real image.
        uploadTexture(image);
    }


    /**
     * Uploads a texture of the passed image to the GPU.
     *
     * @param image target image
     */
    private void uploadTexture(BufferedImage image) {

        // Place all pixels from image into an array.
        int[] pixels = new int[image.getHeight() * image.getWidth()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        // Create ByteBuffer.
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);                     // Multiply by four since four bytes (rgba) are in one integer.
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                byte alphaComponent = (byte)((pixel >> 24) & 0xFF);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
            }
        }
        buffer.flip();  // TODO : Necessary?

        // Upload to GPU.
        textureId = glGenTextures();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(),
                0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        buffer.clear();                                                                                                 // Clear allocated memory for buffer.
    }


    /**
     * Retrieves a target character from this font.
     *
     * @param codepoint character to retrieve (!, A, B, C, etc.)
     * @return character
     */
    public CharInfo getCharacter(int codepoint) {

        return charMap.getOrDefault(codepoint, new CharInfo(0, 0, 0, 0));
    }


    // GETTER
    public int getTextureId() {
        return textureId;
    }
}
