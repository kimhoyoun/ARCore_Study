package com.example.ex10_homework;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    PointCloudRenderer mPointCloud;
    ObjRenderer mObj;


    boolean mViewportChanged;
    int mViewportWidth, mViewportHeight;
    RenderCallback mRenderCallback;

    ObjRenderer[] objArray;

    MainRenderer(Context context, RenderCallback callback){
        mRenderCallback = callback;
        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();
        objArray = new ObjRenderer[]{
                new ObjRenderer(context, "andy.obj", "andy.png"),
                new ObjRenderer(context, "doll.obj", "doll.png"),
                new ObjRenderer(context, "earth.obj", "earth.png"),
                new ObjRenderer(context, "moon.obj", "moon.png"),
        };

        mObj = objArray[0];
    }

    interface RenderCallback{
        void preRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f,1.0f, 0.0f, 1.0f);

        mCamera.init();
        mPointCloud.init();
        for(int i=0; i<objArray.length; i++){
            objArray[i].init();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderCallback.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();

        GLES20.glDepthMask(true);
        mPointCloud.draw();

        mObj.draw();
    }

    void updateSession(Session session, int displayRotation){
        if (mViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewportChanged = false;
        }
    }

    void setProjectionMatrix(float[] matrix){
        mPointCloud.updateProjMatrix(matrix);
//        for(int i=0; i<objArray.length; i++){
//            objArray[i].setProjectionMatrix(matrix);
//        }
        mObj.setProjectionMatrix(matrix);

    }
    void updateViewMatrix(float[] matrix){
        mPointCloud.updateViewMatrix(matrix);
        for(int i=0; i<objArray.length; i++){
            objArray[i].setViewMatrix(matrix);
        }
        mObj.setViewMatrix(matrix);
    }

    int getTextureId(){
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }

    void objChanged(int index,float[] modelMatrix){
        if((mObj == objArray[0])&&index!=0){
            Matrix.scaleM(modelMatrix,0,0.05f, 0.05f, 0.05f);
        }else if(index == 0&&(mObj != objArray[0])){
            Matrix.scaleM(modelMatrix,0,20f, 20f, 20f);
        }
        mObj = objArray[index];
        mObj.setModelMatrix(modelMatrix);
    }
}
