package com.cgfay.filterlibrary.glfilter.stickers;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerNormalData;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;


import java.nio.FloatBuffer;

/**
 * 绘制普通贴纸(非前景贴纸)
 */
public class DynamicStickerNormalFilter extends DynamicStickerBaseFilter {

    // 视椎体缩放倍数，具体数据与setLookAt 和 frustumM有关
    // 备注：setLookAt 和 frustumM 设置的结果导致了视点(eye)到近平面(near)和视点(eye)到贴纸(center)恰好是2倍的关系
    private static final float ProjectionScale = 2.0f;

    // 变换矩阵句柄
    private int mMVPMatrixHandle;

    // 贴纸变换矩阵
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    // 长宽比
    private float mRatio;

    // 贴纸坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 贴纸顶点
    private float[] mStickerVertices = new float[8];

    public DynamicStickerNormalFilter(Context context, DynamicSticker sticker) {
        super(context, sticker, OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_normal.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_normal.glsl"));
        // 创建贴纸加载器列表
        if (mDynamicSticker != null && mDynamicSticker.dataList != null) {
            for (int i = 0; i < mDynamicSticker.dataList.size(); i++) {
                if (mDynamicSticker.dataList.get(i) instanceof DynamicStickerNormalData) {
                    String path = mDynamicSticker.unzipPath + "/" + mDynamicSticker.dataList.get(i).stickerName;
                    mStickerLoaderList.add(new DynamicStickerLoader(this, mDynamicSticker.dataList.get(i), path));
                }
            }
        }
        initMatrix();
        initBuffer();
    }

    /**
     * 初始化纹理
     */
    private void initMatrix() {
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * 初始化缓冲
     */
    private void initBuffer() {
        releaseBuffer();
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    /**
     * 释放缓冲
     */
    private void releaseBuffer() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        } else {
            mMVPMatrixHandle = OpenGLUtils.GL_NOT_INIT;
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        mRatio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1.0f, 1.0f, 3.0f, 9.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void release() {
        super.release();
        releaseBuffer();
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 绘制到FBO
        int stickerTexture = drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        // 绘制显示
        return super.drawFrame(stickerTexture, vertexBuffer, textureBuffer);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 1、先将图像绘制到FBO中
        Matrix.setIdentityM(mMVPMatrix, 0);
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);

        return mFrameBufferTextures[0];
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        }
        // 绘制到FBO中，需要开启混合模式
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE, GLES30.GL_ONE);
    }

    @Override
    public void onDrawFrameAfter() {
        super.onDrawFrameAfter();
        GLES30.glDisable(GLES30.GL_BLEND);
    }


}
