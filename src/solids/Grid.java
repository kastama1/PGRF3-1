package solids;

import lwjglutils.OGLBuffers;

public class Grid {
    private OGLBuffers buffers;

    /**
     * @param m - počet vrcholů v řádku
     * @param n - počet vrcholů ve sloupci
     */
    public Grid(int m, int n) {
        float[] vertexBuffer = new float[m * n * 2];

        // rozsah <0;1>
        int index = 0;
        for(int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                vertexBuffer[index++] = j / (float) (m - 1);
                vertexBuffer[index++] = i / (float) (n - 1);
            }
        }



        int[] indexBuffer = new int[6 * (m - 1) * (n - 1)];
        index = 0;
        for(int i = 0; i < n - 1; i++) {
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

        OGLBuffers.Attrib[] attribs = new OGLBuffers.Attrib[] {
                new OGLBuffers.Attrib("inPosition", 2)
        };

        buffers = new OGLBuffers(vertexBuffer, attribs, indexBuffer);
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }
}
