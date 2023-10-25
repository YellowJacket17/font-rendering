package fonts;

import org.joml.Vector2f;

/**
 * This class represents data for a single font character (positioning, etc.)
 */
public class CharInfo {

    // FIELDS
    private int sourceX;
    private int sourceY;
    private int width;
    private int height;
    private Vector2f textureCoords[] = new Vector2f[4];


    // CONSTRUCTOR
    /**
     * Constructs a CharInfo instance.
     *
     * @param sourceX
     * @param sourceY
     * @param width
     * @param height
     */
    public CharInfo(int sourceX, int sourceY, int width, int height) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.width = width;
        this.height = height;
    }


    // METHOD
    /**
     * Calculates the texture coordinate of this character on the parent texture.
     *
     * @param fontWidth
     * @param fontHeight
     */
    public void calculateTextureCoordinates(int fontWidth, int fontHeight) {

        float x0 = (float)sourceX / (float)fontWidth;
        float x1 = (float)(sourceX + width) / (float)fontWidth;
        float y0 = (float)(sourceY - height) / (float)fontHeight;
        float y1 = (float)sourceY / (float)fontHeight;

        textureCoords[0] = new Vector2f(x0, y1);
        textureCoords[1] = new Vector2f(x1, y0);
    }


    // GETTERS
    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Vector2f[] getTextureCoords() {
        return textureCoords;
    }
}
