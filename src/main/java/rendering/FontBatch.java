package rendering;

import fonts.CFont;
import fonts.CharInfo;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;

/**
 * This class holds a batch of CFont instances to be sent to the GPU and rendered in a single call.
 */
public class FontBatch {
    
    // FIELDS
    /**
     * Base vertex indices.
     */
    private int[] indices = {
            0, 1, 3,
            1, 2, 3
    };

    /**
     * Number of vertices that can be stored in this batch.
     * As an aside, 100 vertices equals 25 quads.
     */
    private final int batchSize = 100;

    /**
     * Total number of indices in the vertex array.
     */
    private final int vertexSize = 7;

    /**
     * Vertex array.
     */
    private float[] vertices = new float[batchSize * vertexSize];

    /**
     * Actual number of vertices stored in this batch.
     */
    private int size = 0;

    /**
     * Projection matrix for shader purposes.
     */
    private Matrix4f projection = new Matrix4f();

    /**
     * Vertex array object ID.
     */
    private int vaoId;

    /**
     * Vertex buffer object ID.
     */
    private int vboId;

    /**
     * rendering.Shader attached to this batch.
     */
    private Shader shader;

    /**
     * Font used in this batch.
     */
    private CFont font;


    // CONSTRUCTOR
    /**
     * Constructs a FontBatch instance.
     */
    public FontBatch() {}


    // METHODS
    /**
     * Initializes this batch.
     * All necessary data is created on the GPU.
     * In other words, space is allocated on the GPU.
     */
    public void init() {

        // Initialize projection matrix.
        projection.identity();                                                                                          // Sets the projection matrix to equal the identity matrix.
        projection.ortho(0, 800, 600, 0, 1f, 100f);                                                                     // Treat screen as if it's 800 x 600 pixels tall with positive Y down.

        // Generate and bind a vertex array object.
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Allocate space for vertices.
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexSize * batchSize * Float.BYTES, GL_DYNAMIC_DRAW);

        // Generate and bind element buffer object.
        generateEbo();

        // Enable buffer attribute pointers.
        int stride = 7 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }


    /**
     * Generates an element buffer object large enough to hold the number of vertices specified by the batch size.
     */
    public void generateEbo() {

        int elementSize = batchSize * 3;                                                                                // Multiply by three since there are three indices per triangle.
        int[] elementBuffer = new int[elementSize];

        for (int i = 0; i < elementSize; i++) {

            elementBuffer[i] = indices[(i % 6)] + ((i / 6) * 4);                                                        // Use pattern set by indices array.
        }
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);                                                                     // Bind array.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);                                           // Buffer array to GPU.
    }


    /**
     * Adds a string of character to this batch.
     *
     * @param text text to render
     * @param x screen coordinate (leftmost x)
     * @param y screen coordinate (topmost y)
     * @param scale scale factor compared to native font size
     * @param rgb color in hexadecimal format
     */
    public void addString(String text, int x, int y, float scale, int rgb) {

        for (int i = 0; i < text.length(); i++) {                                                                       // Add each character from the string to the batch, one at a time.

            char c = text.charAt(i);
            CharInfo charInfo = font.getCharacter(c);

            if (charInfo.getWidth() == 0) {

                // TODO : Could generate error for unknown character.
            }
            float xPos = x;
            float yPos = y;
            addCharacter(xPos, yPos, scale, charInfo, rgb);                                                             // Add character to batch.                                                    // Adds character to batch.
            x += charInfo.getWidth() * scale;                                                                           // Prepare for next character in string.
        }
    }


    /**
     * Adds a single character to this batch.
     *
     * @param x screen coordinate (leftmost x)
     * @param y screen coordinate (topmost y)
     * @param scale sale factor compared to native font size
     * @param charInfo character data
     * @param rgb color in hexadecimal format
     */
    public void addCharacter(float x, float y, float scale, CharInfo charInfo, int rgb) {

        if (size >= (batchSize - 4)) {

            flushBatch();                                                                                               // Flush batch (i.e., render then clear) to start fresh.
        }
        float r = (float)(((rgb >> 16) & 0xFF) / 255.0);                                                                // Extract red information from hexadecimal.
        float g = (float)(((rgb >> 8) & 0xFF) / 255.0);                                                                 // Extract green information from hexadecimal.
        float b = (float)(((rgb >> 0) & 0xFF) / 255.0);                                                                 // Extract blue information from hexadecimal.

        float x0 = x;                                                                                                   // Top-left corner (remember that positive Y is down).
        float y0 = y;                                                                                                   // ^^^
        float x1 = x + (scale * charInfo.getWidth());                                                                   // Bottom-right corner (remember that positive Y is down).
        float y1 = y + (scale * (charInfo.getHeight() + charInfo.getDescent()));                                        // ^^^ (also, modifying this value affects how "stretched" the text appears)

        float ux0 = charInfo.getTextureCoords()[0].x;
        float uy0 = charInfo.getTextureCoords()[1].y;                                                                   // Flipped with `uy1` since positive Y is defined as down here.
        float ux1 = charInfo.getTextureCoords()[1].x;
        float uy1 = charInfo.getTextureCoords()[0].y;

        int index = size * 7;                                                                                           // First vertex with position, color, and texture coordinates; seven floats per vertex.
        vertices[index] = x1;                                                                                           // Position (X).
        vertices[index + 1] = y0;                                                                                       // Position (Y).
        vertices[index + 2] = r;                                                                                        // Color (red).
        vertices[index + 3] = g;                                                                                        // Color (green).
        vertices[index + 4] = b;                                                                                        // Color (blue).
        vertices[index + 5] = ux1;                                                                                      // Texture coordinate (X).
        vertices[index + 6] = uy0;                                                                                      // Texture coordinate (Y).

        index += 7;                                                                                                     // Second vertex with position, color, and texture coordinates.
        vertices[index] = x1;
        vertices[index + 1] = y1;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux1;
        vertices[index + 6] = uy1;

        index += 7;                                                                                                      // Third vertex with position, color, and texture coordinates.
        vertices[index] = x0;
        vertices[index + 1] = y1;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux0;
        vertices[index + 6] = uy1;

        index += 7;                                                                                                     // Fourth vertex with position, color, and texture coordinates.
        vertices[index] = x0;
        vertices[index + 1] = y0;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux0;
        vertices[index + 6] = uy0;

        size += 4;                                                                                                      // Four vertices have now been added.
    }


    /**
     * Flushes a batch.
     * This must be called to actually render text to the screen.
     */
    public void flushBatch() {

        // Clear buffer on GPU.
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexSize * batchSize * Float.BYTES, GL_DYNAMIC_DRAW);                           // Allocate memory on GPU.

        // Upload CPU contents (vertex data).
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        // Draw buffer that was just uploaded.
        shader.use();
        glActiveTexture(GL_TEXTURE);
        glBindTexture(GL_TEXTURE_BUFFER, font.getTextureId());
        shader.uploadTexture("uFontTexture", 0);
        shader.uploadMat4f("uProjection", projection);
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);

        // Reset batch for use on next call.
        size = 0;
    }


    // SETTERS
    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public void setFont(CFont font) {
        this.font = font;
    }
}
