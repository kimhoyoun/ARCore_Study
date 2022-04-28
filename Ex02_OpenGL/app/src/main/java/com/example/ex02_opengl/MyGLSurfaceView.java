package com.example.ex02_opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {
    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        setRenderer(new MyGLRenderer(context));

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
