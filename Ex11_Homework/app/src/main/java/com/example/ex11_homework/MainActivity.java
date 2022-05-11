package com.example.ex11_homework;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView myTextView;
    TextView myCatchView;
    GLSurfaceView mSurfaceView;
    MainRenderer mRenderer;

    Session mSession;
    Config mConfig;

    boolean mUserRequestedInstall = true,  mTouched = false, isModelInit = false, mCatched = false;
    boolean isPlaneDetected = false;

    float mCurrentX, mCurrentY, mCatchX, mCatchY;

    // 이동, 회전 이벤트 처리할 객체
    GestureDetector mGestureDetector;
    ScaleGestureDetector mScaleGestureDetector;
    float mScaleFactor = 1f;


    float [] modelMatrix = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.glsurfaceview);
        myTextView = findViewById(R.id.myTextView);
        myCatchView = findViewById(R.id.myCatchView);

        // 제스처이벤트 콜백함수 객체 생성자 매개변수로 처리 (이벤트 핸들러)
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

            // 한번터치(잡자!)
            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                mCatched = true;
                mCatchX = event.getX();
                mCatchY = event.getY();
                Log.d("클릭", event.getX()+", "+event.getY());
                return true;
            }

            // 따닥 처리 (이동)
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                mTouched = true;
                isModelInit = false;
                mCurrentX = event.getX();
                mCurrentY = event.getY();

                return true;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // 두 손가락으로 스케일 시작

                if(isModelInit) {
                    Log.d("스케일", detector.getScaleFactor()+", ");
//                    변화량을 누적하여 초기화시 변화된 총량을 회전 값으로 넣겟다.
                    mScaleFactor *= detector.getScaleFactor();
                    // 지금의 변화량만 넣겟다
                    Matrix.scaleM(modelMatrix, 0, detector.getScaleFactor(), detector.getScaleFactor(), detector.getScaleFactor());

//                    mRenderer.mObj.setModelMatrix(modelMatrix);
                }
                return true;
            }
        });

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if(displayManager != null){
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {

                }

                @Override
                public void onDisplayRemoved(int displayId) {

                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized(this){
                        mRenderer.mViewportChanged = true;
                    }
                }
            },null);
        }

        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() {
                if(mRenderer.mViewportChanged){
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = null;

                try{
                    frame = mSession.update();
                } catch (CameraNotAvailableException e){
                    e.printStackTrace();
                }

                if(frame.hasDisplayGeometryChanged()){
                    mRenderer.mCamera.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.mPointCloud.update(pointCloud);
                pointCloud.release();

                // 더블클릭하면 그린다.
                if(mTouched){

                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                    for(HitResult result : results){
                        Pose pose = result.getHitPose();
                        if(!isModelInit) {
                            isModelInit = true;
                            pose.toMatrix(modelMatrix, 0);

                            Matrix.scaleM(modelMatrix, 0, mScaleFactor, mScaleFactor,mScaleFactor);
                        }

                        Trackable trackable = result.getTrackable();

                        if(trackable instanceof Plane &&
                                ((Plane) trackable).isPoseInPolygon(pose)
                        ){
                            mRenderer.mObj.setModelMatrix(modelMatrix);
                        }
                    }
//                    mTouched = false;
                }

                Collection<Plane> planes = mSession.getAllTrackables(Plane.class);

                for (Plane plane :planes) {
                    if(plane.getTrackingState() == TrackingState.TRACKING && plane.getSubsumedBy() == null){
                        isPlaneDetected = true;
                        mRenderer.mPlane.update(plane);
                    }
                }

                if(isPlaneDetected) {
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

                if(mCatched){
                    List<HitResult> results = frame.hitTest(mCatchX, mCatchY);
                    String msg = "잡고싶다...";
                    for(HitResult result : results) {
                        Pose pose = result.getHitPose();
                        if (catchCheck(pose.tx(), pose.ty(), pose.tz())) {
                            msg = "잡아버렸어"+ pose.tx()+", "+pose.ty()+", "+pose.tz();
                            break;
                        }

                    }

                    String msgto = msg;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myCatchView.setText(msgto);
                        }
                    });
                    mCatched = false;
                }



                Camera camera = frame.getCamera();
                float [] projMatrix = new float[16];
                float[] viewMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);
                camera.getViewMatrix(viewMatrix, 0);

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);

            }
        });

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        mSurfaceView.setRenderer(mRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
        try {
            if(mSession == null){
                switch(ArCoreApk.getInstance().requestInstall(this, true)){
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d("메인", "ARCore session 생성");
                        break;
                    case INSTALL_REQUESTED:
                        Log.d("메인","ARCore 설치 필요");
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

    void hideStatusBarAndTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    0
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event); // 위임해서 받아옴
        mScaleGestureDetector.onTouchEvent(event);
//        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            mTouched = true;
//            mCurrentX = event.getX();
//            mCurrentY = event.getY();
//        }
        return true;
    }

    boolean catchCheck(float x, float y, float z){
        float[][] resAll = mRenderer.mObj.getMinMaxPoint();
        float[] minPoint = resAll[0];
        float[] maxPoint = resAll[1];


        Log.d("좌표", "minPoint : "+ Arrays.toString(minPoint) +", maxPoint : "+ Arrays.toString(maxPoint));
//        float[] minPoint = new float[]{-0.5f, -2.5f, -10.5f};
//        float[] maxPoint = new float[]{0.5f, 2.5f, 10.5f};
//        if(x>= minPoint[0]-0.1f && x<= maxPoint[0]+0.1f &&
//                y>= minPoint[1]-1.5f && y<= maxPoint[1]+1.5f &&
//                z>= minPoint[2]-3.5f && z<= maxPoint[2]+3.5f){
//            return true;
//        }
        if(x>= minPoint[0] && x<= maxPoint[0] &&
                y>= minPoint[1] && y<= maxPoint[1]&&
                z>= minPoint[2] && z<= maxPoint[2]){
            return true;
        }
        return false;
    }
}