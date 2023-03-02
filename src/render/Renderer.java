package render;

import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import solids.Grid;
import transforms.*;

import java.io.IOException;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private Camera camera;
    double ox, oy;
    boolean mouseButton1 = false;
    private Mat4 projection, ortho;
    private int shaderProgram, loc_uView, loc_uProj, loc_uOrtho, loc_uTypeGrid, loc_uTypeProjection, loc_uTime, loc_uTypeColor;
    private Grid grid;
    private final int width = 800, height = 600;
    private int mode = 0, typeGrid = 0, typeProjection = 0, typeColor = 0;
    private OGLTexture2D texture;
    private final int[] polygonModes = {GL_FILL, GL_LINE, GL_POINT};
    private int m = 500;

    @Override
    public void init() throws IOException {
        shaderProgram = ShaderUtils.loadProgram("/shaders/Basic");

        loc_uView = glGetUniformLocation(shaderProgram, "uView");
        loc_uProj = glGetUniformLocation(shaderProgram, "uProj");
        loc_uOrtho = glGetUniformLocation(shaderProgram, "uOrtho");
        loc_uTypeGrid = glGetUniformLocation(shaderProgram, "uTypeGrid");
        loc_uTypeProjection = glGetUniformLocation(shaderProgram, "uTypeProjection");
        loc_uTypeColor = glGetUniformLocation(shaderProgram, "uTypeColor");
        loc_uTime = glGetUniformLocation(shaderProgram, "uTime");

        camera = new Camera()
                .withPosition(new Vec3D(3.f, 3f, 2f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(true)
                .withRadius(3);

        projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 50.f);
        ortho = new Mat4OrthoRH((double) width / 90, (double) height / 90, 0.1f, 50.f);

        texture = new OGLTexture2D("textures/bricks.jpg");
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        renderGrid();

        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);

        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());
        glUniformMatrix4fv(loc_uOrtho, false, ortho.floatArray());
        glUniform1i(loc_uTypeGrid, typeGrid);
        glUniform1i(loc_uTypeProjection, typeProjection);
        glUniform1i(loc_uTypeColor, typeColor);
        glUniform1f(loc_uTime, (float) glfwGetTime());

        texture.bind(shaderProgram, "uTextureID", 0);

        glUseProgram(shaderProgram);

        grid.getBuffers().draw(GL_TRIANGLES, shaderProgram);
    }

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W:
                        camera = camera.forward(0.5);
                        break;
                    case GLFW_KEY_S:
                        camera = camera.backward(0.5);
                        break;
                    case GLFW_KEY_A:
                        camera = camera.left(0.5);
                        break;
                    case GLFW_KEY_D:
                        camera = camera.right(0.5);
                        break;
                    case GLFW_KEY_SPACE:
                        camera = camera.up(0.5);
                        break;
                    case GLFW_KEY_LEFT_CONTROL:
                        camera = camera.down(0.5);
                        break;
                    case GLFW_KEY_M:
                        mode = (++mode) % 3;
                        changePolygonMode(mode);
                        break;
                    case GLFW_KEY_G:
                        typeGrid = (++typeGrid) % 7;
                        break;
                    case GLFW_KEY_P:
                        typeProjection = (++typeProjection) % 2;
                        break;
                    case GLFW_KEY_C:
                        typeColor = (++typeColor) % 2;
                        break;
                    case GLFW_KEY_KP_1:
                        m += 10;
                        renderGrid();
                        break;
                    case GLFW_KEY_KP_2:
                        if (m > 5) {
                            m -= 10;
                            renderGrid();
                        }
                        break;

                }
            }
        }
    };

    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mouseButton1) {
                camera = camera.addAzimuth((double) Math.PI * (ox - x) / width)
                        .addZenith((double) Math.PI * (oy - y) / width);
                ox = x;
                oy = y;
            }
        }
    };

    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                mouseButton1 = true;
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                ox = xBuffer.get(0);
                oy = yBuffer.get(0);
            }

            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) {
                mouseButton1 = false;
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);
                camera = camera.addAzimuth((double) Math.PI * (ox - x) / width)
                        .addZenith((double) Math.PI * (oy - y) / width);
                ox = x;
                oy = y;
            }
        }
    };

    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
        }
    };

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    @Override
    public GLFWScrollCallback getScrollCallback() {
        return scrollCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mbCallback;
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cpCallbacknew;
    }

    public void changePolygonMode(int mode) {
        glPolygonMode(GL_FRONT_AND_BACK, polygonModes[mode]);
    }

    public void renderGrid() {
        grid = new Grid(m, m, GL_TRIANGLES);
    }
}
