package render;

import lwjglutils.OGLTextRenderer;
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
    private Mat4 projection;
    private Mat4 model = new Mat4Identity(), modelLight = new Mat4Identity();
    private int shaderProgram, loc_uModel, loc_uView, loc_uProj;
    private int loc_uModeObject, loc_uModeColor, loc_uModeLight, loc_uModeSpot, loc_uTime;
    private int loc_uLightSource, loc_uSpecularPower;
    private int loc_uConstantAttenuation, loc_uLinearAttenuation, loc_uQuadraticAttenuation;
    private int loc_uSpotCutOff, loc_uSpotDirection, loc_uLightTransform;
    private int shaderProgramLight, loc_uModelLight, loc_uViewLight, loc_uProjLight;
    private Grid grid, light;
    private int modePolygon = 0, modeTopology = 0, modeObject = 0, modeProjection = 0, modeColor = 0;
    private int modeLight = 0, modeMovement = 0;
    private boolean modeSpot = false;
    private final int[] polygonModes = {GL_FILL, GL_LINE, GL_POINT};
    private final int[] topology = {GL_TRIANGLES, GL_TRIANGLE_STRIP};
    private int m = 50;
    private double scale = 1, x = 0, y = 0, z = 0, rotX = 0, rotY = 0, rotZ = 0;
    private float lightX = 5, lightY = 5, lightZ = 2;
    private float spotCutOff = 0.8f;
    private final String[] textTopology = {"GL_TRIANGLES", "GL_TRIANGLE_STRIP"};
    private final String[] textMovement = {"Camera", "Object", "Rotation", "Light", "Light with camera", "Light with object"};
    private final String[] textPolygon = {"GL_FILL", "GL_LINE", "GL_POINT"};
    private final String[] textColor = {"Color", "Position X, Y, Z", "Normal", "Texture coord U, V", "Depth", "Texture", "Lightning", "Texture and Lightning", "Distance from light", "Normal mapping texture"};
    private final String[] textLight = {"Ambient", "Ambient + Diffuse", "Ambient + Diffuse + Specular", "Ambient + Diffuse + Specular + Attenuation"};
    private OGLTexture2D texture, textureNormal;

    public Renderer(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() throws IOException {
        shaderProgram = ShaderUtils.loadProgram("/shaders/Main/Main");
        shaderProgramLight = ShaderUtils.loadProgram("/shaders/Light/Main");

        loc_uModel = glGetUniformLocation(shaderProgram, "uModel");
        loc_uView = glGetUniformLocation(shaderProgram, "uView");
        loc_uProj = glGetUniformLocation(shaderProgram, "uProj");
        loc_uLightTransform = glGetUniformLocation(shaderProgram, "uModelLight");

        texture = new OGLTexture2D("textures/bricks.jpg");
        textureNormal = new OGLTexture2D("textures/bricksn.png");
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        loc_uModeObject = glGetUniformLocation(shaderProgram, "uModeObject");
        loc_uModeColor = glGetUniformLocation(shaderProgram, "uModeColor");
        loc_uModeLight = glGetUniformLocation(shaderProgram, "uModeLight");
        loc_uModeSpot = glGetUniformLocation(shaderProgram, "uModeSpot");

        loc_uTime = glGetUniformLocation(shaderProgram, "uTime");

        loc_uLightSource = glGetUniformLocation(shaderProgram, "uLightSource");

        loc_uSpecularPower = glGetUniformLocation(shaderProgram, "uSpecularPower");
        loc_uConstantAttenuation = glGetUniformLocation(shaderProgram, "uConstantAttenuation");
        loc_uLinearAttenuation = glGetUniformLocation(shaderProgram, "uLinearAttenuation");
        loc_uQuadraticAttenuation = glGetUniformLocation(shaderProgram, "uQuadraticAttenuation");

        loc_uSpotCutOff = glGetUniformLocation(shaderProgram, "uSpotCutOff");
        loc_uSpotDirection = glGetUniformLocation(shaderProgram, "uSpotDirection");

        loc_uSpotDirection = glGetUniformLocation(shaderProgram, "uSpotDirection");

        camera = new Camera()
                .withPosition(new Vec3D(10.f, 10f, 5f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(true)
                .withRadius(3);

        initProjection();

        glShadeModel(GL_SMOOTH);

        renderGrid();
        renderLight();

        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

        textRenderer = new OGLTextRenderer(width, height);
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, width, height);

        changePolygonMode();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        drawGrid();

        if (modeMovement != 4) {
            drawLight();
        }

        String text = "Change object [O] " + modeObject + "; ";
        text += "Movement mode [M] " + textMovement[modeMovement] + "; ";
        text += "Topology mode [T] " + textTopology[modeTopology] + "; ";
        text += "Polygon mode [P] " + textPolygon[modePolygon] + "; ";
        text += "Number of vertex [+, -] " + m + "; ";

        textRenderer.addStr2D(10, 30, text);

        if (modeMovement == 0) {
            text = "Camera movement [WSAD]; Up/Down [QE]; ";
        } else if (modeMovement == 1) {
            text = "Object movement [WSAD]; Up/Down [QE]; ";
        } else if (modeMovement == 2) {
            text = "Object rotation X [WS]; Object rotation Y [AD]; Object rotation Z [QE]; ";
        } else if (modeMovement == 3) {
            text = "Light movement [WSAD]; Up/Down [QE]; ";
        } else if (modeMovement == 4) {
            text = "Light with camera movement [WSAD]; Up/Down [QE]; ";
        } else if (modeMovement == 5) {
            text = "Light with object movement [WSAD]; Up/Down [QE]; ";
        }

        textRenderer.addStr2D(10, 50, text);

        text = "Object scale [1,2]; ";

        textRenderer.addStr2D(10, 70, text);

        text = "Color mode [C] " + textColor[modeColor] + "; ";
        if (modeColor == 6 || modeColor == 7 || modeColor == 9) {
            text += "Lighting mode [L] " + textLight[modeLight] + "; ";
            if (modeColor == 6 || modeColor == 7) {
                text += "Flash light [F] " + modeSpot + "; ";
                text += "Spot cut off [Up, Down] " + spotCutOff + "; ";
            }
        }

        textRenderer.addStr2D(10, 70, text);
    }

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    // Movement
                    // modeMovement = 0 -> camera movement
                    // modeMovement = 1 -> object movement
                    // modeMovement = 2 -> object rotation
                    // modeMovement = 3 -> light movement
                    // modeMovement = 4 -> Light with camera movement
                    // modeMovement = 5 -> Light with object movement
                    case GLFW_KEY_W:
                        switch (modeMovement) {
                            case 0:
                            case 4:
                                camera = camera.forward(0.5);
                                break;
                            case 1:
                                y = 0.1;
                                setModelMat();
                                break;
                            case 2:
                                rotX = 0.1;
                                setModelMat();
                                break;
                            case 3:
                                lightY += 0.1f;
                                y = 0.1f;
                                setLightModelMat();
                                break;
                            case 5:
                                lightY += 0.1f;
                                y = 0.1f;
                                setLightModelMat();
                                y = 0.1;
                                setModelMat();
                                break;
                        }
                        break;
                    case GLFW_KEY_S:
                        switch (modeMovement) {
                            case 0:
                            case 4:
                                camera = camera.backward(0.5);
                                break;
                            case 1:
                                y = -0.1;
                                setModelMat();
                                break;
                            case 2:
                                rotX = -0.1;
                                setModelMat();
                                break;
                            case 3:
                                lightY -= 0.1f;
                                y = -0.1f;
                                setLightModelMat();
                                break;
                            case 5:
                                lightY -= 0.1f;
                                y = -0.1f;
                                setLightModelMat();
                                y = -0.1;
                                setModelMat();
                                break;
                        }
                        break;
                    case GLFW_KEY_A:
                        switch (modeMovement) {
                            case 0:
                            case 4:
                                camera = camera.left(0.5);
                                break;
                            case 1:
                                x = 0.1;
                                setModelMat();
                                break;
                            case 2:
                                rotY = 0.1;
                                setModelMat();
                                break;
                            case 3:
                                lightX += 0.1f;
                                x = 0.1f;
                                setLightModelMat();
                                break;
                            case 5:
                                lightX += 0.1f;
                                x = 0.1f;
                                setLightModelMat();
                                x = 0.1;
                                setModelMat();
                                break;
                        }
                        break;
                    case GLFW_KEY_D:
                        switch (modeMovement) {
                            case 0:
                            case 4:
                                camera = camera.right(0.5);
                                break;
                            case 1:
                                x = -0.1;
                                setModelMat();
                                break;
                            case 2:
                                rotY = -0.1;
                                setModelMat();
                                break;
                            case 3:
                                lightX -= 0.1f;
                                x = -0.1f;
                                setLightModelMat();
                                break;
                            case 5:
                                lightX -= 0.1f;
                                x = -0.1f;
                                setLightModelMat();
                                x = -0.1;
                                setModelMat();
                                break;
                        }
                        break;
                    case GLFW_KEY_Q:
                        switch (modeMovement) {
                            case 0:
                            case 4:
                                camera = camera.up(0.5);
                                break;
                            case 1:
                                z = 0.1;
                                setModelMat();
                                break;
                            case 2:
                                rotZ = 0.1;
                                setModelMat();
                                break;
                            case 3:
                                lightZ += 0.1f;
                                z = 0.1f;
                                setLightModelMat();
                                break;
                            case 5:
                                lightZ += 0.1f;
                                z = 0.1f;
                                setLightModelMat();
                                z = 0.1;
                                setModelMat();
                                break;
                        }
                        break;
                    case GLFW_KEY_E:
                        switch (modeMovement) {
                            case 0:
                            case 4:
                                camera = camera.down(0.5);
                                break;
                            case 1:
                                z = -0.1;
                                setModelMat();
                                break;
                            case 2:
                                rotZ = -0.1;
                                setModelMat();
                                break;
                            case 3:
                                lightZ -= 0.1f;
                                z = -0.1f;
                                setLightModelMat();
                                break;
                            case 5:
                                lightZ -= 0.1f;
                                z = -0.1f;
                                setLightModelMat();
                                z = -0.1;
                                setModelMat();
                                break;
                        }
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
                    // Change polygon mode
                    case GLFW_KEY_P:
                        modePolygon = (++modePolygon) % 3;
                        changePolygonMode();
                        break;
                    // Change movement mode
                    case GLFW_KEY_M:
                        modeMovement = (++modeMovement) % 6;
                        break;
                    // Change topology mode
                    case GLFW_KEY_T:
                        modeTopology = (++modeTopology) % 2;
                        renderGrid();
                        break;
                    // Change object
                    case GLFW_KEY_O:
                        modeObject = (++modeObject) % 7;
                        break;
                    // Change color
                    case GLFW_KEY_C:
                        modeColor = (++modeColor) % 10;
                        break;
                    // Change lighting
                    case GLFW_KEY_L:
                        if (modeColor == 6 || modeColor == 7 || modeColor == 9) {
                            modeLight = (++modeLight) % 4;
                        }
                        break;
                    // Change spot
                    case GLFW_KEY_F:
                        if (modeColor == 6 || modeColor == 7) {
                            modeSpot = !modeSpot;
                        }
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
                    case GLFW_KEY_UP:
                        if (modeColor == 6 || modeColor == 7) {
                            spotCutOff += 0.01f;
                        }
                        break;
                    case GLFW_KEY_DOWN:
                        if (modeColor == 6 || modeColor == 7) {
                            spotCutOff -= 0.01f;
                        }
                        break;
                    case GLFW_KEY_R:
                        modeProjection = (++modeProjection) % 2;
                        initProjection();
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
        grid = new Grid(m, m, topology[modeTopology]);
    }

    public void setModelMat() {
        Mat4 transMat = new Mat4Transl(x, y, z);
        Mat4 scaleMat = new Mat4Scale(scale);

        Mat4 rotXMat = new Mat4RotX(rotX);
        Mat4 rotYMat = new Mat4RotY(rotY);
        Mat4 rotZMat = new Mat4RotZ(rotZ);

        model = model.mul(transMat).mul(scaleMat).mul(rotXMat).mul(rotYMat).mul(rotZMat);

        x = 0;
        y = 0;
        z = 0;

        rotX = 0;
        rotY = 0;
        rotZ = 0;
    }

    public void changePolygonMode() {
        glPolygonMode(GL_FRONT_AND_BACK, polygonModes[modePolygon]);
    }

    public void initProjection() {
        if (modeProjection == 0) {
            projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 50.f);
        } else {
            projection = new Mat4OrthoRH((double) width / 90, (double) height / 90, 0.1f, 50.f);
        }
    }

    void drawGrid() {
        glUseProgram(shaderProgram);

        glUniformMatrix4fv(loc_uModel, false, model.floatArray());
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());
        glUniformMatrix4fv(loc_uLightTransform, false, modelLight.floatArray());

        glUniform1i(loc_uModeObject, modeObject);
        glUniform1i(loc_uModeColor, modeColor);
        glUniform1i(loc_uModeLight, modeLight);
        glUniform1i(loc_uModeSpot, modeSpot ? 1 : 0);

        glUniform1f(loc_uTime, (float) glfwGetTime());

        if (modeMovement == 4) {
            glUniform3f(loc_uLightSource, (float) camera.getEye().getX(), (float) camera.getEye().getY(), (float) camera.getEye().getZ());
            glUniform3f(loc_uSpotDirection, (float) camera.getEye().getX(), (float) camera.getEye().getY(), (float) camera.getEye().getZ());
        } else {
            glUniform3f(loc_uLightSource, lightX, lightY, lightZ);
            glUniform3f(loc_uSpotDirection, lightX, lightY, lightZ);
        }
        glUniform1f(loc_uSpecularPower, 1f);
        glUniform1f(loc_uConstantAttenuation, 0.5f);
        glUniform1f(loc_uLinearAttenuation, 0.2f);
        glUniform1f(loc_uQuadraticAttenuation, 0.05f);

        glUniform1f(loc_uSpotCutOff, spotCutOff);

        texture.bind(shaderProgram, "uTextureID", 0);
        textureNormal.bind(shaderProgram, "uTextureNormal", 1);

        grid.getBuffers().draw(topology[modeTopology], shaderProgram);
    }

    public void renderLight() {
        light = new Grid(10, 10, GL_TRIANGLES);

        loc_uModelLight = glGetUniformLocation(shaderProgramLight, "uModel");
        loc_uViewLight = glGetUniformLocation(shaderProgramLight, "uView");
        loc_uProjLight = glGetUniformLocation(shaderProgramLight, "uProj");

        Mat4 transMat = new Mat4Transl(lightX, lightY, lightZ);

        modelLight = modelLight.mul(transMat);
    }

    void drawLight() {
        glUseProgram(shaderProgramLight);

        glUniformMatrix4fv(loc_uModelLight, false, modelLight.floatArray());
        glUniformMatrix4fv(loc_uViewLight, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(loc_uProjLight, false, projection.floatArray());

        light.getBuffers().draw(GL_TRIANGLES, shaderProgramLight);
    }

    public void setLightModelMat() {
        Mat4 transMat = new Mat4Transl(x, y, z);

        modelLight = modelLight.mul(transMat);

        x = 0;
        y = 0;
        z = 0;
    }
}
