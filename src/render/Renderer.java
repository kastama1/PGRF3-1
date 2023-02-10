package render;

import lwjglutils.OGLBuffers;
import lwjglutils.ShaderUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import solids.Grid;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private Camera camera;
    private Mat4 projection;

    private int shaderProgram;
    private OGLBuffers buffers;
    private Grid grid;

    @Override
    public void init() {
        glEnable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        camera = new Camera()
                .withPosition(new Vec3D(0.f, 0f, 0f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(false)
                .withRadius(3);
        projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 50.f);

        shaderProgram = ShaderUtils.loadProgram("/shaders/Basic");
        glUseProgram(shaderProgram);

        // Vertex buffer
        float[] vertexBuffer = {
                -1.f, -1.f,     1.f, 0.f, 0.f,
                 1.f,  0.f,     0.f, 1.f, 0.f,
                 0.f,  1.f,     0.f, 0.f, 1.f,
        };

        // Index buffer
        int[] indexBuffer = {
                0, 1, 2
        };

        // OPENGL: VertexBuffer
        int vb = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vb);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // OPENGL: IndexBuffer
        int ib = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Attributes
        OGLBuffers.Attrib[] attribs = new OGLBuffers.Attrib[] {
                new OGLBuffers.Attrib("inPosition", 2),
                new OGLBuffers.Attrib("inColor", 3)
        };

        // Buffers
        buffers = new OGLBuffers(vertexBuffer, attribs, indexBuffer);

        // Grid
        grid = new Grid(10, 10);
    }

    @Override
    public void display() {
        // Uniforms
        int loc_uColor = glGetUniformLocation(shaderProgram, "uColor");
        glUniform3f(loc_uColor, 1.f, 1.f, 0.f);

        int loc_uView = glGetUniformLocation(shaderProgram, "uView");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());
        int loc_uProj = glGetUniformLocation(shaderProgram, "uProj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        //buffers.draw(GL_TRIANGLES, shaderProgram);
        grid.getBuffers().draw(GL_TRIANGLES, shaderProgram);
    }

    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
        }
    };

    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
        @Override
        public void invoke(long window, int button, int action, int mods) {
        }
    };

    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
        }
    };

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
