package com.example.ex07_homework;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    PointCloudRenderer mPointCloud;
    PlaneRenderer mPlane;
    Cube mCube;
    ObjRenderer mObj;
    ObjRenderer moon;

    boolean mViewportChanged;
    int mViewportWidth, mViewportHeight;
    RenderCallback mRenderCallback;


    MainRenderer(Context context, RenderCallback callback) {
        mRenderCallback = callback;
        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();
        mPlane = new PlaneRenderer(Color.BLUE, 0.7f);

        mCube = new Cube(0.3f, Color.YELLOW, 0.8f);
        mObj = new ObjRenderer(context, "earth.obj", "earth.png");
        moon = new ObjRenderer(context, "moon.obj", "moon.png");
    }

    interface RenderCallback {
        void preRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();
        mPointCloud.init();
        mPlane.init();
        mCube.init();
        mObj.init();
        moon.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderCallback.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();

        GLES20.glDepthMask(true);

        mPlane.draw();
        mCube.draw();
        mObj.draw();
        moon.draw();
    }

    void updateSession(Session session, int displayRotation) {
        if (mViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewportChanged = false;
        }
    }

    void setProjectionMatrix(float[] matrix) {
        mPointCloud.updateProjMatrix(matrix);
        mPlane.setProjectionMatrix(matrix);
        mCube.setProjectionMatrix(matrix);
        mObj.setProjectionMatrix(matrix);
        moon.setProjectionMatrix(matrix);
    }

    void updateViewMatrix(float[] matrix) {
        mPointCloud.updateViewMatrix(matrix);
        mPlane.setViewMatrix(matrix);
        mCube.setViewMatrix(matrix);
        mObj.setViewMatrix(matrix);
        moon.setViewMatrix(matrix);
    }

    int getTextureId() {
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }
}
