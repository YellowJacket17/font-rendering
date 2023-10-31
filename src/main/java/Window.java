import fonts.CFont;
import org.lwjgl.opengl.GL;
import rendering.FontBatch;
import rendering.Shader;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Core class that houses the main loop and initializes the application.
 */
public class Window {

    // FIELDS
    /**
     * Memory address of GLFW window in memory space.
     */
    private long window;

    /**
     * Loaded font.
     */
    private CFont font1, font2;


    // CONSTRUCTOR
    /**
     * Constructs a Window instance.
     */
    public Window() {
        init();
        font1 = new CFont("/fonts/Arimo-mO92.ttf", 128);
        font2 = new CFont("/fonts/ArimoBold-dVDx.ttf", 128);
//        font2 = new CFont("/fonts/TrulyMadlyDpad-a72o.ttf", 128);
    }


    // METHODS
    /**
     * Initializes the GLFW window.
     */
    private void init() {

        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(1280, 720, "Font Rendering", NULL, NULL);

        if (window == NULL) {

            System.out.println("Failed to create GLFW window.");
            glfwTerminate();
            return;
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();                                                                                        // Initialize gl functions for windows using GLAD.
    }


    /**
     * Starts the main application loop.
     */
    public void run() {

//        Sdf.generateCodepointBitmap('A', "src/main/resources/fonts/Arimo-mO92.ttf", 32);

        Shader fontShader = new Shader("/shaders/fontShader.glsl");
        fontShader.compileAndLink();

        FontBatch batch1 = new FontBatch();
        batch1.setShader(fontShader);
        batch1.setFont(font1);
        batch1.init();

        FontBatch batch2 = new FontBatch();
        batch2.setShader(fontShader);
        batch2.setFont(font2);
        batch2.init();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        while (!glfwWindowShouldClose(window)) {

            glClear(GL_COLOR_BUFFER_BIT);
            glClearColor(1, 1, 1, 1);

            batch1.addString("Hello, World! g p y", 0, 0, 0.5f, 0xAA01BB);
            batch1.flush();                                                                                             // Must flush at the end of the frame to actually render entire batch.

            batch2.addString("Hello, yorld! g p", 0, 60, 0.5f, 0xAAAAA);
            batch2.flush();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
