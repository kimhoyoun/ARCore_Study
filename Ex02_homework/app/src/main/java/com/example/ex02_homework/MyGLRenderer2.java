package com.example.ex02_homework;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer2 implements GLSurfaceView.Renderer {
    MyPicture2 myBox;
    MyPicture2 myTri;


    MyPicture2 rectangle1;
    MyPicture2 rectangle2;
    MyPicture2 rectangle3;
    MyPicture2 rectangle4;
    MyPicture2 rectangle5;
    MyPicture2 rectangle6;


    // 밑에 두개를 가지고 처음껄 만든다
    // 한번에 처리하기 어려우니까 각각 처리를하고 만들어 줄것.
    float [] mMVPMatrix = new float[16];
    float [] mProjectionMatrix = new float[16];
    float [] mViewMatrix = new float[16];


    float[] squareCoords = {
            -0.5f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, 0.5f, 0f,
            0.5f, -0.5f, 0f,
    };
    float[] color = {0.792f, 0.796f, 0.807f, 1.0f};
    short[] drawOrder = {0,1,2, 1,2,3};


    float[] squareCoords2 = {
            0f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f
    };
    float[] color2 = {1f, 1f, 1f, 1.0f};
    short[] drawOrder2 = {0,1,2, 1,2,3};


    float[] rac1 = {
            -0.5f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, 0.5f, 0f,
            0.5f, -0.5f, 0f,
    };


    float[] rac2 = {
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f
    };

    float[] rac3 = {
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f
    };

    float[] rac4 = {
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f
    };

    float[] rac5 = {
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f
    };

    float[] rac6 = {
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
    };

    float[] rColor1 = {0.792f, 0.796f, 0.807f, 1.0f};
    float[] rColor2 = {0.3f, 0.2f, 0.807f, 1.0f};
    float[] rColor3 = {0.7f, 0.8f, 0.27f, 1.0f};
    float[] rColor4 = {0.3f, 0.756f, 0.27f, 1.0f};
    float[] rColor5 = {0.72f, 0.56f, 0.1f, 1.0f};
    float[] rColor6 = {0.52f, 0.46f, 0.27f, 1.0f};


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);

        rectangle1 = new Rectangle(rac1,rColor1,drawOrder2);
        rectangle2 = new Rectangle(rac2,rColor2,drawOrder2);
        rectangle3 = new Rectangle(rac3,rColor3,drawOrder2);
        rectangle4 = new Rectangle(rac4,rColor4,drawOrder2);
        rectangle5 = new Rectangle(rac5,rColor5,drawOrder2);
        rectangle6 = new Rectangle(rac6,rColor6,drawOrder2);

    }

    // 화면갱신 되면서 화면에서 배치
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        // 비율을 잡는다
        float ratio = (float) width/height;

        // 사각형의 중심을 어떻게해서 그리겠다.
        // 어떻게 넣어주겟느냐
        Matrix.frustumM(mProjectionMatrix, 0,ratio,-ratio,-1,1,3,7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        // 그리기위해서 시점의 값을 잡아주고 카메라의 위치를 정한다.
        // 카메라 위치, 카메라 시선, 카메라 정방향 필요
        Matrix.setLookAtM(mViewMatrix,0, // 배열의 시작 위치
                // x, y, z
                4,3,-3, // 카메라 위치 : -3이므로 위에서 뒤로 빠져있는 것
                0,0,0, // 카메라 시선
                0,1,0 // 카메라 윗방향 : y축 정방향이 윗방향
        );

        // 합치기
        // 두개(mProjectionMatrix, mViewMatrix)를 가져와 각각 0번째값을 곱해서 mMVPMatrix의 0번째부터 저장한다.
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
        // 그리기(정사각형 그리기)
        rectangle1.draw(mMVPMatrix);
        rectangle2.draw(mMVPMatrix);
        rectangle3.draw(mMVPMatrix);
        rectangle4.draw(mMVPMatrix);
        rectangle5.draw(mMVPMatrix);
        rectangle6.draw(mMVPMatrix);
    }

    // GPU를 이용하여 그리기를 연산한다.
    static int loadShader(int type, String shaderCode){

        int res = GLES20.glCreateShader(type);

        GLES20.glShaderSource(res, shaderCode);
        GLES20.glCompileShader(res);

        return res;
    }

}
