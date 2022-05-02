package com.example.ex06_painting;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line {
    // GPU를 이용하여 고속 계산 하여 화면 처리 하기 위한 코드
    String vertexShaderString =
            "attribute vec3 aPosition; "+
            "uniform vec4 aColor; "+
            "uniform mat4 uMVPMatrix; "+
            "varying vec4 vColor; "+

            "void main () {" +
                    "vColor = aColor; "+
            "gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, 1.0); "+  // gl_Position : OpenGL 에 있는 변수 ::> 계산식 uMVPMatrix * vPosition
            "}";

    String fragmentShaderString =
            "precision mediump float;"+ // 중간값으로 한다.
                    "varying vec4 vColor;" + // color : 4개의 원소를 받겠다.
                    "void main() {"+
                    "   gl_FragColor = vColor;"+
                    "}";

    float[] mModelMatrix = new float[16];
    float[] mViewMatrix = new float[16];
    float[] mProjMatrix = new float[16];

    float[] mColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    // 최대 점 갯수(점의 배열 좌표 갯수와 동일)
    int maxPoints = 1000;
    // 현재 점 번호
    int mNumPoints =0;
    // 1000개의 점 * xyz
    float[] mPoint = new float[maxPoints*3];

    FloatBuffer mVertices;
    int mProgram;

    boolean isInited = false;

    float lineWidth = 50.0f;

    int[] mVbo;


    public Line(){
        this(50.0f);
    }
    public Line(float lineWidth){
        this.lineWidth = lineWidth;
    }


    // 그리기 직전에 좌표 수정
   void update(){

        // 점
        mVertices =  ByteBuffer.allocateDirect(mPoint.length*4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mPoint);
        mVertices.position(0);

       GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
       GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0, mNumPoints*3*Float.BYTES, mVertices);
       GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

       Log.d("라인","그리기전 갱신");
//        // 색
//        mColors =  ByteBuffer.allocateDirect(mColor.length*4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        mColors.put(mColor);
//        mColors.position(0);
//
//        // 순서
//        mIndices =  ByteBuffer.allocateDirect(indices.length*2)
//                .order(ByteOrder.nativeOrder()).asShortBuffer();
//        mIndices.put(indices);
//        mIndices.position(0);

    }

    // 점 추가? 점 갱신 하기
    void updatePoint(float x, float y, float z){

        // 그린 점의 갯수가 최대치이면 점 정보를 갱신하지 않는다.
        if(mNumPoints >= maxPoints -1 ){
            return;
        }
        // 현재 점 번호에 좌표 받는다.
        mPoint[mNumPoints*3 +0] = x;
        mPoint[mNumPoints*3 +1] = y;
        mPoint[mNumPoints*3 +2] = z;


        // 현재 점 번호 증가
        mNumPoints++;

        Log.d("라인", "점 추가");

    }

    // 초기화화
    void init(){
        mVbo = new int[1];
        GLES20.glGenBuffers(1, mVbo, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, maxPoints*3*Float.BYTES, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vertexShaderString);
        GLES20.glCompileShader(vShader);

        //텍스쳐
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fragmentShaderString);
        GLES20.glCompileShader(fShader);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);

        isInited = true;
        Log.d("라인", " 초기화");
    }


    // 도형 그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(){

        GLES20.glUseProgram(mProgram);

        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int color = GLES20.glGetUniformLocation(mProgram, "aColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] mvpMatrix = new float[16];
        float[] mvMatrix = new float[16];

        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix,0, mModelMatrix,0);
        Matrix.multiplyMM(mvpMatrix,0, mProjMatrix, 0, mvMatrix, 0);


        // GPU 활성화
        GLES20.glEnableVertexAttribArray(position);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
        // 점, 색 번호에 해당하는 변수에 각각 대입
        // 점 float * 3 (삼각형)
        GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT, false, 4*3, 0);
        GLES20.glUniform4f(color, mColor[0], mColor[1], mColor[2], mColor[3]);
        // 색 float * rbga
//mvp 번호에 해당하는 변수에 mvpMatrix 대입
        GLES20.glUniformMatrix4fv(mvp,1, false, mvpMatrix,0);


        GLES20.glLineWidth(lineWidth);
        // 그린다
        //                     삼각형으로 그린다.    시작점,   끝번호
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, mNumPoints);

        // 비활성화
        GLES20.glDisableVertexAttribArray(position);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    void setmModelMatrix(float [] matrix){
        System.arraycopy(matrix,0,mModelMatrix,0,16);
    }

    void updateProjMatrix(float[] projMatrix){
        System.arraycopy(projMatrix, 0, this.mProjMatrix, 0, 16);
    }

    void updateViewMatrix(float[] viewMatrix){
        System.arraycopy(viewMatrix, 0, this.mViewMatrix, 0, 16);
    }


}
