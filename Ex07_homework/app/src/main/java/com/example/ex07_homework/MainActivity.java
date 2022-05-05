package com.example.ex07_homework;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView myTextView;
    GLSurfaceView mSurfaceView;
    MainRenderer mRenderer;

    Session mSession;
    Config mConfig;

    boolean mUserRequestedInstall = true;
    boolean isPlaneDetected = false;

    float mCurrentX, mCurrentY;
    boolean mTouched = false;

    float[] modelMatrix = new float[16];

    float[] xyz = new float[3];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.glsurfaceview);
        myTextView = findViewById(R.id.myTextView);
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {

                }

                @Override
                public void onDisplayRemoved(int displayId) {

                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.mViewportChanged = true;
                    }
                }
            }, null);
        }


        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() {
                if (mRenderer.mViewportChanged) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = null;

                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer.mCamera.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.mPointCloud.update(pointCloud);
                pointCloud.release();


                // 터치하였다면
                if (mTouched) {

                    LightEstimate estimate = frame.getLightEstimate();
                    float[] colorCorrection = new float[4];
                    estimate.getColorCorrection(colorCorrection, 0);
                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                    for (HitResult result : results) {
                        Pose pose = result.getHitPose(); // 증강공간에서의 좌표

                        xyz[0] = pose.tx();
                        xyz[1] = pose.ty();
                        xyz[2] = pose.tz();

                        pose.toMatrix(modelMatrix, 0);  // 좌표를 가지고 matrix화 함.
                        Trackable trackable = result.getTrackable();

                        Matrix.scaleM(modelMatrix, 0, 0.05f, 0.05f, 0.05f);
                        if (trackable instanceof Plane &&
                                // Plane 폴리곤(면) 안에 좌표가 있는가?
                                ((Plane) trackable).isPoseInPolygon(pose)
                        ) {
                        Matrix.translateM(modelMatrix, 0, 0f, 2f, 0f);
                        mRenderer.mObj.setModelMatrix(modelMatrix);
                        }
                    }
                    mTouched = false;
                }
                Collection<Plane> planes = mSession.getAllTrackables(Plane.class);
                for (Plane plane : planes) {

                    if (plane.getTrackingState() == TrackingState.TRACKING && plane.getSubsumedBy() == null) {
                        isPlaneDetected = true;
                        // 렌더링에서 plane 정보를 갱신하여 출력
                        mRenderer.mPlane.update(plane);
                    }
                }

                if (isPlaneDetected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("평면을 찾았어욤!!!");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("평면이 어디로 간거야~~~");
                        }
                    });
                }

                Camera camera = frame.getCamera();
                float[] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);
                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);

            }
        });

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mSurfaceView.setRenderer(mRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, true)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d("메인", "ARCore session 생성");
                        break;
                    case INSTALL_REQUESTED:
                        Log.d("메인", "ARCore 설치 필요");
                        mUserRequestedInstall = false;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);
        mSession.configure(mConfig);
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        mSession.pause();

    }

    void hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    0
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouched = true;
            mCurrentX = event.getX();
            mCurrentY = event.getY();
        }

        return true;
    }
    boolean stop =true;
    float[] moonMatrix = new float[16];

    boolean earthFlag = false;
    boolean moonFlag = false;

    public void btnClick(View view) {
        switch (view.getId()){
            // 달 등장
            case R.id.btn1:
                System.arraycopy(modelMatrix, 0, moonMatrix, 0, 16);
                Matrix.translateM(moonMatrix, 0, 0f, 0f, 10f);
                mRenderer.moon.setModelMatrix(moonMatrix);
                break;
            // 지구 자전
            case R.id.btn2:
                if(!earthFlag) {
                    stop = true;

                    new Thread() {
                        @Override
                        public void run() {
                            while (stop) {
                                Matrix.rotateM(modelMatrix, 0, 1, 0f, 1f, 0f);
                                mRenderer.mObj.setModelMatrix(modelMatrix);
                                SystemClock.sleep(200);
                            }
                        }
                    }.start();
                    earthFlag = true;
                }
                break;
            // 달 공전
            case R.id.btn3:

                if(!moonFlag) {
                    stop = true;
                    float[] moonMatrix2 = new float[16];
                    new Thread() {
                        @Override
                        public void run() {
                            int angle = 0;
                            while (stop) {
                                Matrix.translateM(moonMatrix2, 0, modelMatrix, 0, 10 * (float) Math.sin(angle * Math.PI / 180), 0f, 10 * (float) Math.cos(angle * Math.PI / 180));
                                mRenderer.moon.setModelMatrix(moonMatrix2);
                                SystemClock.sleep(10);
                                angle = angle > 360 ? 0 : angle + 1;
                            }
                        }
                    }.start();
                    moonFlag = true;
                }
                break;
            // 멈추기
            case R.id.btn4:
                stop = false;
                earthFlag = false;
                moonFlag = false;
                break;
        }
    }
}