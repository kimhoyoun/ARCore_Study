package com.example.ex06_painting;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Session;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer  {

//    final static String TAG = "MainRenderer :";

    RenderCallBack myCallBack;

    CameraPreView mCamera;
    PointCloudRenderer mPointCloud;


    boolean viewportChanged;

    int width, height;

    // 라인을 갖고있을 List
    List<Line> mPaths = new ArrayList<>();

    interface RenderCallBack{
        void preRender();
    }

    MainRenderer(RenderCallBack myCallBack){

        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();

        this.myCallBack = myCallBack;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("MainRenderer","onSurfaceCreated() 실행");

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glClearColor(1.0f,1.0f,0.0f,1.0f); // 1이 제일 큰거 0은 없는거

        mCamera.init();
        mPointCloud.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0,0,width,height);

        viewportChanged = true;
        this.width = width;
        this.height = height;

    }

    // 실질적으로 그리는 애
    @Override
    public void onDrawFrame(GL10 gl) {
//        Log.d("MainRenderer","onDrawFrame() 실행");

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        myCallBack.preRender();


        // 카메라로 받은 화면 그리기
        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);

        // 포인트클라우드 그리기기
//        mPointCloud.draw();

        // 점 그리기
//        if(currPath != null){
//            if(!currPath.isInited){
//                currPath.init();
//            }
//            currPath.update();
//            currPath.draw();
//        }

        // 모든 라인을 그리기
        for(Line currPath : mPaths){
            if(currPath != null){
                if(!currPath.isInited){
                    currPath.init();
                }
                currPath.update();
                currPath.draw();
            }
        }


    }

    void onDisplayChanged(){
        viewportChanged = true;
    }

    void updateSession(Session session, int rotation){
        if(viewportChanged){
            session.setDisplayGeometry(rotation, width, height);
            viewportChanged = false;
        }
    }
    int getTextureId(){
        return mCamera== null ? -1 : mCamera.mTextures[0];
    }

    void addPoint(float x, float y, float z){
//            Sphere sphere = new Sphere();
//
//
//            float[] matrix = new float[16];
//            Matrix.setIdentityM(matrix, 0);
//            Matrix.translateM(matrix, 0, x, y, z);

//            sphere.setmModelMatrix(matrix);

            // 왜 update? add?
            // add로 하면 힘들다?
        if(!mPaths.isEmpty()) { // 선이 존재한다면
            // 마지막 선을 가지고 온다.
            Line currPath = mPaths.get(mPaths.size() - 1);
            currPath.updatePoint(x, y, z);
        }
    }





    void addLine(float x, float y, float z){
        // 선 생성
        Line currPath = new Line();
        currPath.updateProjMatrix(mProjMatrix);

        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        Matrix.translateM(matrix, 0, x,y,z);

        currPath.setmModelMatrix(matrix);

        // 선 리스트에 추가
        mPaths.add(currPath);
    }

    float[] mProjMatrix = new float[16];

    void updateProjMatrix(float[] projMatrix){

        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16);

        mPointCloud.updateProjMatrix(projMatrix);


    }

    void updateViewMatrix(float[] viewMatrix){
        mPointCloud.updateViewMatrix(viewMatrix);
//        sphere.updateViewMatrix(viewMatrix);

//        if(currPath != null){
//            currPath.updateViewMatrix(viewMatrix);
//        }

        for(Line line : mPaths){
            line.updateViewMatrix(viewMatrix);
        }

    }

    void removePath(){
        if(!mPaths.isEmpty()){
            mPaths.remove(mPaths.size()-1);
        }
    }

}
