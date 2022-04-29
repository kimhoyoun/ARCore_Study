package com.example.ex02_homework;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class MyPicture {


    // 점
    // GPU를 이용하여 고속 계산 하여 화면 처리 하기 위한 코드
    String vertexShaderCode = "uniform mat4 uMVPMatrix;"+ // mat4 -> 4x4 형태의 상수로 지정
            "attribute vec4 vPosition;"+  // vec4 -> 3차원 좌표
            "void main () {" +
            "gl_Position = uMVPMatrix * vPosition;"+  // gl_Position : OpenGL 에 있는 변수 ::> 계산식 uMVPMatrix * vPosition
            "}";

    String fragmentShaderCode =
            "precision mediump float;"+ // 중간값으로 한다.
                    "uniform vec4 vColor;" + // color : 4개의 원소를 받겠다.
                    "void main() {"+
                    "   gl_FragColor = vColor;"+
                    "}";

//    // 직사각형 점의 좌표
//    static float[] squareCoords = {
//            // x,   y,    z
//            -0.5f, 0.5f, 0.0f,     // 왼쪽 위
//            -0.5f, -0.5f, 0.0f,    // 왼쪽 아래
//            0.5f, -0.5f, 0.0f,      // 오른쪽 위
//            0.5f, 0.5f, 0.0f      // 오른쪽 아래
//    };

    //      육각형
    float[] color;

    short[] drawOrder;

    FloatBuffer vertexBuffer;
    ShortBuffer drawBuffer;
    int mProgram;

    public MyPicture(float[] squareCoords, float[] color, short[] drawOrder){
        // 데이터를 보내줄 때 데이터 타입을 모를 수 있으므로 String으로 보내기 위한 Buffer를 만듦
        this.color = color;
        this.drawOrder = drawOrder;

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length*4); // squarCoords의 length는 12지만 flaot는 4바이트이므로 *4를함
        // 정렬하는 이유 : 리틀 엔디안, 빅 엔디안처리인지 모르니까
        // 장비에 따라아서 ordering을 하라는 뜻으로 nativeOrder()메서드를 사용함.
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        // 멘 앞으로 빼줘
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(drawOrder.length*2); // squarCoords의 length는 12지만 flaot는 4바이트이므로 *4를함
        bb.order(ByteOrder.nativeOrder());

        drawBuffer = bb.asShortBuffer();
        drawBuffer.put(drawOrder);
        drawBuffer.position(0);

        // 점위치 계산식
        // vertexShaderCode -> vertexShader
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode
        );

        // 점색상 계산식
        // fragmentShaderCode -> fragmentShader
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode
        );

        // mProgram = vertexShader + fragmentShader
        mProgram = GLES20.glCreateProgram();
        // 점위치 계산식 합치기
        GLES20.glAttachShader(mProgram,vertexShader);
        // 색상 계산식 합치기
        GLES20.glAttachShader(mProgram,fragmentShader);
        // 실행(도형 렌더링 계산식 정보 계산)
        GLES20.glLinkProgram(mProgram);
    }

    int mPositionHandle, mColorHandle, mMVPMatrixHandle;

    // 도형 그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(float[] mMVPMatrix){

        // 계산된 렌더링 정보를 사용한다.
        GLES20.glUseProgram(mProgram);

        //          vPostion
        // mProgram -> vertexShader
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(
                mPositionHandle,      // 정점 속성의 인덱스 지정
                3,              // 점속성 - 좌표계
                GLES20.GL_FLOAT,      // vertex정보 : flaot이므로 자료형을 써줌 (점의 자료형)
                false,     // normalized : 소숫점자리 처리 어떻게 할거냐? 정규화할거면 true, 직접변환 false
                3*4,          //  점 속성의 stride(간격) 바이트 단위 (x,y,z 좌표정보 12바이트)
                vertexBuffer          // 점 정보
        );

        //          vColor
        // mProgram -> fragmentShader
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        GLES20.glUniform4fv(mColorHandle,1,color,0);

        // Location
        // matrix의 값을 받아옴
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // 매개변수로 받은 구간 안에 그린다.
        // 그려지는 곳에 위치, 보이는 정보를 적용한다.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // 직사각형 그린다. (그리는 순서에 대한 값을 넣어준다)
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT,
                drawBuffer);

        // 다 그리고 닫는다.
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
