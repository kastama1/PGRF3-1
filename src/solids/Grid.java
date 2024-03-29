package solids;

import lwjglutils.OGLBuffers;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLE_STRIP;

public class Grid {
    private OGLBuffers buffers;

    public Grid(int m, int n, int type) {
        float[] vertexBuffer = new float[m * n * 2];

        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                vertexBuffer[index++] = j / (float) (m - 1);
                vertexBuffer[index++] = i / (float) (n - 1);
            }
        }

        int[] indexBuffer;

        if (type == GL_TRIANGLE_STRIP) {
            indexBuffer = new int[2 * m * (n - 1) + m];
            index = 0;
            for (int i = 0; i < n - 1; i++) {
                int offset = i * m;
                if (i % 2 == 0) {
                    for (int j = 0; j < m; j++) {
                        indexBuffer[index++] = j + offset;
                        indexBuffer[index++] = j + m + offset;
                        if (j == m - 1) {
                            indexBuffer[index++] = j + m + offset;
                            indexBuffer[index++] = j + m + offset;
                        }
                    }
                } else {
                    for (int j = m - 1; j >= 0; j--) {
                        indexBuffer[index++] = j + m + offset;
                        indexBuffer[index++] = j + offset;
                    }
                }
            }
        } else {
            indexBuffer = new int[6 * (m - 1) * (n - 1)];
            index = 0;
            for (int i = 0; i < n - 1; i++) {
                int offset = i * m;
                for (int j = 0; j < m - 1; j++) {
                    indexBuffer[index++] = j + offset;
                    indexBuffer[index++] = j + m + offset;
                    indexBuffer[index++] = j + 1 + offset;

                    indexBuffer[index++] = j + 1 + offset;
                    indexBuffer[index++] = j + m + offset;
                    indexBuffer[index++] = j + m + 1 + offset;
                }
            }
        }

        OGLBuffers.Attrib[] attribs = new OGLBuffers.Attrib[]{
                new OGLBuffers.Attrib("inPosition", 2)
        };

        buffers = new OGLBuffers(vertexBuffer, attribs, indexBuffer);
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }
}
