package com.example.ex02_opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    //    Square myBox;
    ObjRenderer myTable;

    // 밑에 두개를 가지고 처음껄 만든다
    // 한번에 처리하기 어려우니까 각각 처리를하고 만들어 줄것.
    float [] mMVPMatrix = new float[16];
    float [] mProjectionMatrix = new float[16];
    float [] mViewMatrix = new float[16];

    public MyGLRenderer(Context context){

        myTable = new ObjRenderer(context,"table.obj", "table.jpg");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        myTable.init();

//        myBox = new Square();

    }


    // 화면갱신 되면서 화면에서 배치
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        // 비율을 잡는다
        float ratio = (float) width*30/height;

        // 사각형의 중심을 어떻게해서 그리겠다.
        // 어떻게 넣어주겟느냐
        Matrix.frustumM(mProjectionMatrix, 0,-ratio,ratio,-100,100,20,100);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        // 그리기위해서 시점의 값을 잡아주고 카메라의 위치를 정한다.
        // 카메라 위치, 카메라 시선, 카메라 정방향 필요
        Matrix.setLookAtM(mViewMatrix,0, // 배열의 시작 위치
                // x, y, z
                0,0,20, // 카메라 위치 : -3이므로 위에서 뒤로 빠져있는 것
                0,0,-30, // 카메라 시선
                0,1,0 // 카메라 윗방향 : y축 정방향이 윗방향
        );

        // 합치기
        // 두개(mProjectionMatrix, mViewMatrix)를 가져와 각각 0번째값을 곱해서 mMVPMatrix의 0번째부터 저장한다.
//        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);

        Matrix.setIdentityM(mMVPMatrix, 0);
        // 그리기(정사각형 그리기)


//        myBox.draw(mMVPMatrix);

        myTable.setProjectionMatrix(mProjectionMatrix);
        myTable.setViewMatrix(mViewMatrix);
        myTable.setModelMatrix(mMVPMatrix);

        myTable.draw();
    }

    // GPU를 이용하여 그리기를 연산한다.
    static int loadShader(int type, String shaderCode){

        int res = GLES20.glCreateShader(type);

        GLES20.glShaderSource(res, shaderCode);
        GLES20.glCompileShader(res);

        return res;
    }
}
