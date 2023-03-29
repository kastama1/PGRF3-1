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
    private Mat4 projection, model, transMat, scaleMat, rotXMat, rotYMat, rotZMat;
    private int shaderProgram, loc_uModel, loc_uView, loc_uProj;
    private int loc_uTypeGrid, loc_uTypeColor, loc_uTime;
    private int loc_uLightSource, loc_uAmbient, loc_uDiffuse, loc_uSpecular, loc_uSpecularPower;
    private int loc_uConstantAttenuation, loc_uLinearAttenuation, loc_uQuadraticAttenuation;
    private Grid grid;
    private int modePolygon = 0, indexTopology = 0, typeShader = 0, typeGrid = 0, typeProjection = 0, typeColor = 0;
    private OGLTexture2D texture;
    private final int[] polygonModes = {GL_FILL, GL_LINE, GL_POINT};
    private final int[] topology = {GL_TRIANGLES, GL_TRIANGLE_STRIP};
    private int m = 50;

    private double scale = 1, x = 0, y = 0, z = 0, rotX = 0, rotY = 0, rotZ = 0;

    public Renderer(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        initShader();

        camera = new Camera()
                .withPosition(new Vec3D(10.f, 10f, 5f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(true)
                .withRadius(3);

        initProjection();
        model = new Mat4Identity();

        glShadeModel(GL_SMOOTH);

        renderGrid();

        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);

        // Display shader by typeShader
        if (typeShader == 0) {
            shaderDisplayMain();
        } else if (typeShader == 1) {
            shaderDisplayLight();
        }

        glUseProgram(shaderProgram);

        grid.getBuffers().draw(topology[indexTopology], shaderProgram);
    }

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    // Movement
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
                    // Transformation of object
                    // Movement of object
                    case GLFW_KEY_J:
                        x = 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_L:
                        x = -0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_I:
                        y = 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_K:
                        y = -0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_U:
                        z = 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_O:
                        z = -0.1;
                        setModelMat();
                        break;
                    // Scale of object
                    case GLFW_KEY_KP_1:
                        scale = 1.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_KP_2:
                        scale = 0.9;
                        setModelMat();
                        break;
                    // Rotation of object
                    case GLFW_KEY_KP_4:
                        rotX += 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_KP_7:
                        rotX -= 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_KP_5:
                        rotY += 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_KP_8:
                        rotY -= 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_KP_6:
                        rotZ += 0.1;
                        setModelMat();
                        break;
                    case GLFW_KEY_KP_9:
                        rotZ -= 0.1;
                        setModelMat();
                        break;
                    // Change polygon mode
                    case GLFW_KEY_M:
                        modePolygon = (++modePolygon) % 3;
                        changePolygonMode(modePolygon);
                        break;
                    // Change polygon mode
                    case GLFW_KEY_T:
                        indexTopology = (++indexTopology) % 2;
                        renderGrid();
                        break;
                    // Change shader
                    case GLFW_KEY_H:
                        typeShader = (++typeShader) % 2;
                        initShader();
                        break;
                    // Change grid
                    case GLFW_KEY_G:
                        typeGrid = (++typeGrid) % 7;
                        break;
                    // Change projection (Persp/Ortho)
                    case GLFW_KEY_P:
                        typeProjection = (++typeProjection) % 2;
                        initProjection();
                        break;
                    // Change color (Position/Texture)
                    case GLFW_KEY_C:
                        typeColor = (++typeColor) % 2;
                        break;
                    // Change number of rendered triangles
                    case GLFW_KEY_KP_ADD:
                        m += 10;
                        renderGrid();
                        break;
                    case GLFW_KEY_KP_SUBTRACT:
                        if (m > 10) {
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
                camera = camera.addAzimuth(Math.PI * (ox - x) / width)
                        .addZenith(Math.PI * (oy - y) / width);
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
                camera = camera.addAzimuth(Math.PI * (ox - x) / width)
                        .addZenith(Math.PI * (oy - y) / width);
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

    public void renderGrid() {
        grid = new Grid(m, m, topology[indexTopology]);
    }

    public void setModelMat() {
        transMat = new Mat4Transl(x, y, z);

        scaleMat = new Mat4Scale(scale);

        rotXMat = new Mat4RotX(rotX);
        rotYMat = new Mat4RotY(rotY);
        rotZMat = new Mat4RotZ(rotZ);

        model = model.mul(transMat).mul(scaleMat).mul(rotXMat).mul(rotYMat).mul(rotZMat);

        x = 0;
        y = 0;
        z = 0;
    }

    public void changePolygonMode(int mode) {
        glPolygonMode(GL_FRONT_AND_BACK, polygonModes[mode]);
    }

    public void initProjection() {
        if (typeProjection == 0) {
            projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 50.f);
        } else {
            projection = new Mat4OrthoRH((double) width / 90, (double) height / 90, 0.1f, 50.f);
        }
    }

    // Init shader by typeShader
    public void initShader() {
        if (typeShader == 0) {
            try {
                shaderMain();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (typeShader == 1) {
            shaderLight();
        }
    }

    // Init shader Light/Main
    public void shaderLight() {
        shaderProgram = ShaderUtils.loadProgram("/shaders/Light/Main");

        loc_uModel = glGetUniformLocation(shaderProgram, "uModel");
        loc_uView = glGetUniformLocation(shaderProgram, "uView");
        loc_uProj = glGetUniformLocation(shaderProgram, "uProj");

        loc_uLightSource = glGetUniformLocation(shaderProgram, "uLightSource");

        loc_uAmbient = glGetUniformLocation(shaderProgram, "uAmbient");
        loc_uDiffuse = glGetUniformLocation(shaderProgram, "uDiffuse");
        loc_uSpecular = glGetUniformLocation(shaderProgram, "uSpecular");
        loc_uSpecularPower = glGetUniformLocation(shaderProgram, "uSpecularPower");
        loc_uConstantAttenuation = glGetUniformLocation(shaderProgram, "uConstantAttenuation");
        loc_uLinearAttenuation = glGetUniformLocation(shaderProgram, "uLinearAttenuation");
        loc_uQuadraticAttenuation = glGetUniformLocation(shaderProgram, "uQuadraticAttenuation");
    }

    // Display shader Light/Main
    public void shaderDisplayLight() {
        glUniformMatrix4fv(loc_uModel, false, model.floatArray());
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        glUniform3f(loc_uLightSource, (float) -camera.getPosition().getX(), (float) -camera.getPosition().getY(), (float) -camera.getPosition().getZ());
        glUniform4f(loc_uAmbient, 0.3f, 0.3f, 0.3f, 1f);
        glUniform4f(loc_uDiffuse, 0.6f, 0.6f, 0.6f, 1f);
        glUniform4f(loc_uSpecular, 1f, 1f, 1f, 1f);
        glUniform1f(loc_uSpecularPower, 10f);
        glUniform1f(loc_uConstantAttenuation, 0.01f);
        glUniform1f(loc_uLinearAttenuation, 0.01f);
        glUniform1f(loc_uQuadraticAttenuation, 0.01f);
    }

    // Init shader Main/Main
    public void shaderMain() throws IOException {
        shaderProgram = ShaderUtils.loadProgram("/shaders/Main/Main");

        texture = new OGLTexture2D("textures/bricks.jpg");
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        loc_uModel = glGetUniformLocation(shaderProgram, "uModel");
        loc_uView = glGetUniformLocation(shaderProgram, "uView");
        loc_uProj = glGetUniformLocation(shaderProgram, "uProj");

        loc_uTypeGrid = glGetUniformLocation(shaderProgram, "uTypeGrid");
        loc_uTime = glGetUniformLocation(shaderProgram, "uTime");

        loc_uTypeColor = glGetUniformLocation(shaderProgram, "uTypeColor");
        texture.bind(shaderProgram, "uTextureID", 0);
    }

    // Display shader Main/Main
    public void shaderDisplayMain() {
        glUniformMatrix4fv(loc_uModel, false, model.floatArray());
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());
        glUniform1i(loc_uTypeGrid, typeGrid);
        glUniform1f(loc_uTime, (float) glfwGetTime());

        glUniform1i(loc_uTypeColor, typeColor);
    }
}
