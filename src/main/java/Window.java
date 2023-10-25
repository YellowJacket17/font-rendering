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

public class Window {

    // FIELDS
    private long window;
    private CFont font;


    // CONSTRUCTOR
    public Window() {
        init();
        font = new CFont("/fonts/Arimo-mO92.ttf", 128);
    }


    // METHODS
    private void init() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(1280, 720, "Font Rendering", NULL, NULL);
        if (window == NULL) {
            System.out.println("Could not create window.");
            glfwTerminate();
            return;
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // Initialize gl functions for windows using GLAD
        GL.createCapabilities();
    }


    public void run() {

        Sdf.generateCodepointBitmap('A', "src/main/resources/fonts/Arimo-mO92.ttf", 32);

        Shader fontShader = new Shader("/shaders/fontShader.glsl");
        fontShader.compileAndLink();
        FontBatch batch = new FontBatch();
        batch.setShader(fontShader);
        batch.setFont(font);
        batch.init();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        while (!glfwWindowShouldClose(window)) {

            glClear(GL_COLOR_BUFFER_BIT);
            glClearColor(1, 1, 1, 1);

            batch.addString("Hello, World!", 0, 0, 0.5f, 0xAA01BB);
            batch.flushBatch();                                                                                         // Must flush at the end of the frame to actually fill up batch.

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
