package render;

import lwjglutils.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import solids.Grid;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private Camera camera;
    double ox, oy;
    boolean mouseButton1 = false;
    private Mat4 projection;
    private int shaderProgram;
    private Grid grid;
    private final int width = 800, height = 600;

    @Override
    public void init() {
        glEnable(GL_DEPTH_TEST);

        camera = new Camera()
                .withPosition(new Vec3D(0.f, 0f, 0f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(false)
                .withRadius(3);

        projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 50.f);

        shaderProgram = ShaderUtils.loadProgram("/shaders/Basic");
        glUseProgram(shaderProgram);

        // Grid
        grid = new Grid(10, 10);
    }

    @Override
    public void display() {
        int loc_uColor = glGetUniformLocation(shaderProgram, "uColor");
        glUniform3f(loc_uColor, 1.f, 1.f, 0.f);

        int loc_uView = glGetUniformLocation(shaderProgram, "uView");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        int loc_uProj = glGetUniformLocation(shaderProgram, "uProj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

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
}
