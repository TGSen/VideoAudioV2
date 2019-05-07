package com.cgfay.cameralibrary.media.surface;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.util.Log;
import android.view.Surface;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * Created by guoheng-iri on 2016/9/1.
 */
public class CodecOutputSurface implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "EncodeDecodeSurface";
    private static final boolean VERBOSE = false;           // lots of logging

    private TextureRender mTextureRender;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    public Surface EncodeSurface;
    private int mInputTexture;
    // 矩阵
    private final float[] mMatrix = new float[16];
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLContext mEGLContextEncoder = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private EGLSurface mEGLSurfaceEncoder = EGL14.EGL_NO_SURFACE;
    int mWidth;
    int mHeight;

    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;

    // 更新帧的锁
    private final Object mSyncFrameNum = new Object();
    private final Object mSyncFence = new Object();
    private int mFrameNum;


    /**
     * Creates a CodecOutputSurface backed by a pbuffer with the specified dimensions.  The
     * new EGL context and surface will be made current.  Creates a Surface that can be passed
     * to MediaCodec.configure().
     */
    public CodecOutputSurface(int width, int height, Surface surface) {
        if (width <= 0 || height <= 0) {
            return;
        }
        mWidth = width;
        mHeight = height;

        eglSetup(surface);
        makeCurrent(0);
        setSurfaceCreated();
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);

        /**
         * 渲染器设置
         */
        OffSVideoRenderManager.getInstance().setTextureSize(width, height);
        OffSVideoRenderManager.getInstance().setDisplaySize(width, height);

    }

    /**
     * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
     */
    private void setSurfaceCreated() {
        mTextureRender = new TextureRender();
        mTextureRender.surfaceCreated();
        mInputTexture = mTextureRender.getTextureId();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mSurface = new Surface(mSurfaceTexture);

    }

    /**
     * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
     */
    private void eglSetup(Surface surface) {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            return;
        }

        // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }

        EGLConfig configEncoder = getConfig(2);

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                attrib_list, 0);
        checkEglError("eglCreateContext");
        if (mEGLContext == null) {
            throw new RuntimeException("null context");
        }

        mEGLContextEncoder = EGL14.eglCreateContext(mEGLDisplay, configEncoder, mEGLContext,
                attrib_list, 0);
        checkEglError("eglCreateContext");
        if (mEGLContextEncoder == null) {
            throw new RuntimeException("null context2");
        }

        // Create a pbuffer surface.
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, mWidth,
                EGL14.EGL_HEIGHT, mHeight,
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);


        checkEglError("eglCreatePbufferSurface");
        if (mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }


        int[] surfaceAttribs2 = {
                EGL14.EGL_NONE
        };
        mEGLSurfaceEncoder = EGL14.eglCreateWindowSurface(mEGLDisplay, configEncoder, surface,
                surfaceAttribs2, 0);   //creates an EGL window surface and returns its handle
        checkEglError("eglCreateWindowSurface");
        if (mEGLSurfaceEncoder == null) {
            throw new RuntimeException("surface was null");
        }
        EncodeSurface = surface;

    }

    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;

        mSurface.release();

        mTextureRender = null;
        mSurface = null;
        EncodeSurface = null;
        mSurfaceTexture = null;
    }

    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurfaceEncoder, nsecs);
        checkEglError("eglPresentationTimeANDROID");
    }


    public boolean swapBuffers() {
        boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurfaceEncoder);
        checkEglError("eglSwapBuffers");
        return result;
    }

    private EGLConfig getConfig(int version) {
        int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
        if (version >= 3) {
            renderableType |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
        }

        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            return null;
        }
        return configs[0];
    }


    public void makeCurrent(int index) {
        if (index == 0) {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        } else {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurfaceEncoder, mEGLSurfaceEncoder, mEGLContextEncoder);
        }

    }

    /**
     * Returns the Surface.
     */
    public Surface getSurface() {
        return mSurface;
    }


    public void awaitNewImage() {
        final int TIMEOUT_MS = 2500;

        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        break;
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen

                }
            }
            mFrameAvailable = false;
        }
        mSurfaceTexture.updateTexImage();

    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     *
     * @param invert if set, render the image with Y inverted (0,0 in top left)
     */
    public void drawImage(boolean invert) {
          mTextureRender.drawFrame(mSurfaceTexture, invert);
//        // 如果存在新的帧，则更新帧
//        synchronized (mSyncFrameNum) {
//            synchronized (mSyncFence) {
//                if (mSurfaceTexture != null) {
//                    while (mFrameNum != 0) {
//                        Log.e("Harrison","mFrameNum:"+mFrameNum);
//                        mSurfaceTexture.updateTexImage();
//                        --mFrameNum;
//                    }
//                }
//            }
//        }
//        mSurfaceTexture.getTransformMatrix(mMatrix);
//        // 绘制渲染
//        OffSVideoRenderManager.getInstance().drawFrame(mInputTexture, mMatrix);

    }

    // SurfaceTexture callback
//    @Override
//    public void onFrameAvailable(SurfaceTexture st) {
//        synchronized (mSyncFrameNum) {
//            ++mFrameNum;
//            Log.e("Harrison","onFrameAvailable mFrameNum:"+mFrameNum);
//        }
//    }

    // SurfaceTexture callback
    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        //new frame available
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                // throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                return;
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();

        }
    }


    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
}
