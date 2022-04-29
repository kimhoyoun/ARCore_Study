package com.example.ex02_homework;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {
    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        setRenderer(new MyGLRenderer2());

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
