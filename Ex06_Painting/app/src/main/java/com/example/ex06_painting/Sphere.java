package com.example.ex06_painting;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sphere {
    // GPU를 이용하여 고속 계산 하여 화면 처리 하기 위한 코드
    String vertexShaderString =
            "attribute vec3 aPosition; "+
            "attribute vec4 aColor; "+
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

    float[] mColor = {1.0f, 0.5f, 0.3f, 1.0f};
    // 빨 초 파 노
    float[][] mColorArray = {
            {1.0f, 0.0f, 0f, 1.0f},
            {0.0f, 1.0f, 0f, 1.0f},
            {0.0f, 0.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 0f, 1.0f},
    };

    int colorIndex = 0;

    FloatBuffer mVertices;
    FloatBuffer mColors;
    ShortBuffer mIndices;
    int mProgram;

    final int POINT_COUNT = 20;

//    int currNO = 0;
//    float[][] mColorArr = {
//            {0.2f, 0.5f, 0.8f, 1.0f},
//            {1.0f, 0.5f, 0.2f, 1.0f},
//            {1.0f, 1.0f, 0.0f, 1.0f},
//            {0.0f, 1.0f, 0.0f, 1.0f},
//            {0.2f, 1.0f, 0.0f, 1.0f},
//    };

//    FloatBuffer[] mColorsArr = new FloatBuffer[mColorArr.length];
    FloatBuffer[] mColorsArr = new FloatBuffer[mColorArray.length];

//    void addNOCnt(){
//        currNO++;
//        currNO %= mColorArr.length;
//    }

    void colorChange(int color){
        switch (color){
            case Color.RED:
                colorIndex = 0;
                break;
            case Color.GREEN:
                colorIndex = 1;
                break;
            case Color.BLUE:
                colorIndex = 2;
                break;
            case Color.YELLOW:
                colorIndex = 3;
                break;
        }
    }

    public Sphere(){

        float radius = 0.05f;
        // POINT_COUNT * POINT_COUNT * 3 (삼각형)
        // 20 * 20 개의 삼각형으로 이루어진 구
        float[] vertices = new float[POINT_COUNT * POINT_COUNT * 3];

        // 구를 만드는 점의 정보 ---> 수학 개념 필요
        for (int i = 0; i < POINT_COUNT; i++) {
            for (int j = 0; j < POINT_COUNT; j++) {
                float theta = i * (float) Math.PI / (POINT_COUNT - 1);
                float phi = j * 2 * (float) Math.PI / (POINT_COUNT - 1);
                float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
                float y = (float) (radius * Math.cos(theta));
                float z = (float) -(radius * Math.sin(theta) * Math.sin(phi));
                int index = i * POINT_COUNT + j;
                vertices[3 * index] = x;
                vertices[3 * index + 1] = y;
                vertices[3 * index + 2] = z;
            }
        }

        float[] colors = new float[POINT_COUNT * POINT_COUNT * 4];

        // 색상정보 : POINT_COUNT * POINT_COUNT * 4
        //              면(삼각형(갯수)   *   (rgba)
        for(int i =0; i<POINT_COUNT; i++){
            for(int j =0; j<POINT_COUNT; j++){
                int index = i * POINT_COUNT;
                colors[4*index + 0] = mColor[0];
                colors[4*index + 1] = mColor[1];
                colors[4*index + 2] = mColor[2];
                colors[4*index + 3] = mColor[3];
            }
        }

        // 삼각형들 그리는 점의 순서 정보
        int numIndices = 2 * (POINT_COUNT - 1) * POINT_COUNT;
        short[] indices = new short[numIndices];
        short index = 0;
        for (int i = 0; i < POINT_COUNT - 1; i++) {
            if ((i & 1) == 0) {
                for (int j = 0; j < POINT_COUNT; j++) {
                    indices[index++] = (short) (i * POINT_COUNT + j);
                    indices[index++] = (short) ((i + 1) * POINT_COUNT + j);
                }
            } else {
                for (int j = POINT_COUNT - 1; j >= 0; j--) {
                    indices[index++] = (short) ((i + 1) * POINT_COUNT + j);
                    indices[index++] = (short) (i * POINT_COUNT + j);
                }
            }
        }

        // buffer로 전환
        // 점
        mVertices =  ByteBuffer.allocateDirect(vertices.length*4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(vertices);
        mVertices.position(0);

        mColors =  ByteBuffer.allocateDirect(colors.length*4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mColors.put(colors);
        mColors.position(0);

        mIndices =  ByteBuffer.allocateDirect(indices.length*2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(indices);
        mIndices.position(0);

//        float [][] colorsArr = new float[mColorArr.length][POINT_COUNT * POINT_COUNT * 4];
        float [][] colorsArr = new float[mColorArray.length][POINT_COUNT * POINT_COUNT * 4];

//        for (int f = 0;f< mColorArr.length; f++ ) {
        for (int f = 0;f< mColorArray.length; f++ ) {

//            float [] buf = mColorArr[f];
            float [] buf = mColorArray[f];
            for (int i = 0; i < POINT_COUNT ; i++) {
                for (int j = 0; j < POINT_COUNT; j++) {
                    int ind = i * POINT_COUNT + j;
                    colorsArr[f][4 * ind + 0 ] = buf[0];
                    colorsArr[f][4 * ind + 1 ] = buf[1];
                    colorsArr[f][4 * ind + 2 ] = buf[2];
                    colorsArr[f][4 * ind + 3 ] = buf[3];
                }
            }
            mColorsArr[f] = ByteBuffer.allocateDirect(colorsArr[f].length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mColorsArr[f].put(colorsArr[f]);
            mColorsArr[f].position(0);
        }

    }
    // 초기화화
    void init(){
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
    }


    // 도형 그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(){

        GLES20.glUseProgram(mProgram);

        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int color = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] mvpMatrix = new float[16];
        float[] mvMatrix = new float[16];

        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix,0, mModelMatrix,0);
        Matrix.multiplyMM(mvpMatrix,0, mProjMatrix, 0, mvMatrix, 0);

        // mvp 번호에 해당하는 변수에 mvpMatrix 대입
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0);

        // 점, 색 번호에 해당하는 변수에 각각 대입
        // 점 float * 3 (삼각형)
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 4 * 3, mVertices);

        // :::: 선생님 힌트 ::::
        // 구가 만들어지는 시점 draw
        // 점 float * rgba
//        GLES20.glVertexAttribPointer(color, 3, GLES20.GL_FLOAT, false, 4 * 3, mColors);
//        GLES20.glVertexAttribPointer(color, 3, GLES20.GL_FLOAT, false, 4 * 4, mColorsArr[currNO]);
        GLES20.glVertexAttribPointer(color, 3, GLES20.GL_FLOAT, false, 4 * 4, mColorsArr[colorIndex]);


        // GPU 활성화
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glEnableVertexAttribArray(color);

        // 그린다
        //                     삼각형으로 그린다.       순서의 보유량,        순서 자료형,             순서내용
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndices.capacity(), GLES20.GL_UNSIGNED_SHORT, mIndices);

        // 비활성화
        GLES20.glDisableVertexAttribArray(position);
        GLES20.glDisableVertexAttribArray(color);

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
