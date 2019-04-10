package com.cgfay.cameralibrary.media.surface;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * Created by guoheng on 2016/8/31.
 */
public class TextureRender {
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final String TAG = "STextureRendering";

    private static final float TRANSFORM_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f

    };


    private static final float TRANSFORM_RECTANGLE_TEX_COORDS[] = {
            0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            0f, 0f, 1f, 1.0f,
            1.0f, 0f, 1.0f, 1.0f

    };

    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f, 1.0f,   // 0 bottom left
            1.0f, -1.0f, 1.0f,   // 1 bottom right
            -1.0f, 1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f, 1.0f   // 3 top right
    };

    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 1.0f, 1f, 1.0f,    // 0 bottom left
            1.0f, 1.0f, 1f, 1.0f,     // 1 bottom right
            0.0f, 0.0f, 1f, 1.0f,    // 2 top left
            1.0f, 0.0f, 1f, 1.0f     // 3 top right
    };

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            OpenGLUtils.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            OpenGLUtils.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);


    private static final FloatBuffer TRANSFORM_RECTANGLE_BUF =
            OpenGLUtils.createFloatBuffer(TRANSFORM_RECTANGLE_COORDS);
    private static final FloatBuffer TRANSFORM_RECTANGLE_TEX_BUF =
            OpenGLUtils.createFloatBuffer(TRANSFORM_RECTANGLE_TEX_COORDS);


    private FloatBuffer mTriangleVertices;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec4 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = uSTMatrix * aTextureCoord;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec4 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord.xy);" +
                    "}\n";


    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID = -12345;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    public TextureRender() {
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public int getTextureId() {
        return mTextureID;
    }

    /**
     * Draws the external texture in SurfaceTexture onto the current EGL surface.
     */
    public void drawFrame(SurfaceTexture st, boolean invert) {
        checkGlError("onDrawFrame start");
        st.getTransformMatrix(mSTMatrix);
        if (invert) {
            mSTMatrix[5] = -mSTMatrix[5];
            mSTMatrix[13] = 1.0f - mSTMatrix[13];
        }

        // (optional) clear to green so we can see if we're failing to set pixels
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);


        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * FLOAT_SIZE_BYTES, TRANSFORM_RECTANGLE_BUF);

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureHandle);

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureHandle, 4,
                GLES20.GL_FLOAT, false, 4 * FLOAT_SIZE_BYTES, TRANSFORM_RECTANGLE_TEX_BUF);

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);


        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");


        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTextureHandle);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);

    }


    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        mProgram = OpenGLUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            return;
        }

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(maPositionHandle, "aPosition");
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkLocation(maTextureHandle, "aTextureCoord");

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(muMVPMatrixHandle, "uMVPMatrix");
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkLocation(muSTMatrixHandle, "uSTMatrix");

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = OpenGLUtils.createOESTexture();
    }

    /**
     * Replaces the fragment shader.  Pass in null to reset to default.
     */
    public void changeFragmentShader(String fragmentShader) {
        if (fragmentShader == null) {
            fragmentShader = FRAGMENT_SHADER;
        }
        GLES20.glDeleteProgram(mProgram);
        mProgram = OpenGLUtils.createProgram(VERTEX_SHADER, fragmentShader);
        if (mProgram == 0) {
            return;
        }
    }


    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            return;
        }
    }

    public static void checkLocation(int location, String label) {
        if (location < 0) {
            return;
        }
    }
}
