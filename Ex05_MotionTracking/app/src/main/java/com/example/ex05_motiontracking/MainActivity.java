package com.example.ex05_motiontracking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //com.google.ar.core.Session;
    Session mSession;

    GLSurfaceView mySurView;
    TextView my_textView;
    MainRenderer mRenderer;
    Button btnRED;
    Button btnGREEN;
    Button btnBLUE;
    Button btnYELLOW;
    SeekBar mySeekBar;

    Config mConfig; // ARCore session 설정정보를 받을 변수

    float displayX, displayY;
    boolean mTouched = false;
    String ttt = "";

    float lineWidth = 10.0f;
    int color= Color.RED;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_main);


        mySurView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        my_textView = findViewById(R.id.my_textView);
        mySeekBar = findViewById(R.id.mySeekBar);
        btnRED = findViewById(R.id.btnRed);
        btnGREEN = findViewById(R.id.btnGreen);
        btnBLUE = findViewById(R.id.btnBlue);
        btnYELLOW = findViewById(R.id.btnYellow);

        btnRED.setOnClickListener(this);
        btnGREEN.setOnClickListener(this);
        btnBLUE.setOnClickListener(this);
        btnYELLOW.setOnClickListener(this);

        // MainActivity의 화면 관리 매니져 --> 화면변화를 감지 :: 현재 시스템에서 서비스지원
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);



        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(seekBar.getProgress() > 10) {
                    lineWidth = 10.0f * seekBar.getProgress() / 10;
                }else{
                    lineWidth = 10.0f;
                }

                mRenderer.mLineX.changeLineWidth(lineWidth);
                mRenderer.mLineY.changeLineWidth(lineWidth);
                mRenderer.mLineZ.changeLineWidth(lineWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {



            }
        });

        // 화면 변화가 발생되면 MainRenderer의 화면변환을 실행시킨다.
        if(displayManager != null){
            // 화면에 대한 리스너 실행
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {

                }

                @Override
                public void onDisplayRemoved(int displayId) {

                }

                // 화면이 변경 되었다면
                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.onDisplayChanged();
                    }
                }
            }, null
            );
        }



        MainRenderer.RenderCallBack mr = new MainRenderer.RenderCallBack() {
            @Override
            public void preRender() {

                if(mRenderer.viewportChanged){
                    Display display = getWindowManager().getDefaultDisplay();

                    mRenderer.updateSession(mSession, display.getRotation());
                }

                // session 객체와 연결해서 화면 그리기 하기
                mSession.setCameraTextureName(mRenderer.getTextureId());

                // 화면 그리기에서 사용할 frame --> session이 업데이트 되면 새로운 프레임을 받는다.
                Frame frame = null;


                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                // 화면을 바꾸기 위한 작업
                mRenderer.mCamera.transformDisplayGeometry(frame);

                //// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ PointCloutd 설정구간
                // ARCore에 정의된 클래스
                // 현재 프레임에서 특정있는 점들에 대한 포인트 값 (3차원 좌표값)을 받을 객체
                //
                // session -> session update => frame => pointcloud => mainRenderer
                PointCloud pointCloud = frame.acquirePointCloud();

                // 포인트 값을 적용시키기위해 mainRenderer -> PointCloud update() 실행
                mRenderer.mPointCloud.update(pointCloud);

                // 사용이 끝난 포인트 자원해제
                pointCloud.release();


                // 화면 터치시 작업 시작
                if(mTouched){

                    List<HitResult> arr = frame.hitTest(displayX, displayY);

//                    Log.d("MainActivity", "건드렸다. (" + displayX+", "+displayY+")"+ "hitList : "+arr);
                    int i = 0;
                    ttt = "";
                    for(HitResult hr: arr){
                        Pose pose = hr.getHitPose();
                        float[] xx = pose.getXAxis();
                        float[] yy = pose.getYAxis();
                        float[] zz = pose.getZAxis();

                        // tx, ty, tz() : 이동값, qx, qy, qz() : 회전값
                        mRenderer.addPoint(pose.tx(), pose.ty(), pose.tz());
                        // x축 선그리기
                        mRenderer.addLineX(xx, pose.tx(), pose.ty(), pose.tz(), lineWidth);
                        mRenderer.addLineY(yy, pose.tx(), pose.ty(), pose.tz(), lineWidth);
                        mRenderer.addLineZ(zz, pose.tx(), pose.ty(), pose.tz(), lineWidth);
                        Log.d("Test" ,"i : "+i+", hr : "+ hr.toString()+", xAxis : "+xx+", yAxis : "+yy+", zAxis : "+zz);
                        ttt += pose.toString()+"\n";
                        i++;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            my_textView.setText(ttt);

                        }
                    });

                    mTouched = false;


                }

                // 화면 터치시 작업 끝
                // 카메라 frame에서 받는다.
                // 용도 : mPointCloud에서 렌더링할 때 카메라의 좌표계산을 받아서 처리
                Camera camera = frame.getCamera();

                float[] projMatrix = new float[16];
                float[] viewMatrix = new float[16];

                camera.getProjectionMatrix(projMatrix,0,0.1f,100.0f);
                camera.getViewMatrix(viewMatrix,0);

//                mRenderer.mPointCloud.updateMatrix(viewMatrix, projMatrix);
                mRenderer.updateProjMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
            }

        };


        mRenderer = new MainRenderer(mr);

        // pause 시 관련 데이터가 사라지는 것을 막는다.
        mySurView.setPreserveEGLContextOnPause(true);
        mySurView.setEGLContextClientVersion(2); // 버전을 2.0 사용

        // 화면을 그리는 Renderer를 지정한다.
        // 새로 정의한 MainRenderer를 사용한다.
        mySurView.setRenderer(mRenderer);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mySurView.onPause();
        mSession.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPerm();

        try {
            if(mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, true)){
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d("session 인감","session 생성이요!");
                        break;
                    case INSTALL_REQUESTED:
                        Log.d("session 인감","ARCore  INSTALL_REQUESTED");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 화면 갱신시 세션설정 정보를 받아서 내세션의 설정으로 올린다.
        mConfig = new Config(mSession);

        mSession.configure(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mySurView.onResume();
        // 랜더링 계속 호출
        mySurView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        displayX = event.getX();
        displayY = event.getY();
        mTouched = true;
//

        return true;
    }

    void hideStatusBar(){

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    // 카메라 퍼미션 요청
    void cameraPerm(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA},
                    0
            );
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRed:
                color = Color.RED;
                mRenderer.sphere.colorChange(color);
                break;
            case R.id.btnGreen:
                color = Color.GREEN;
                mRenderer.sphere.colorChange(color);
                break;
            case R.id.btnBlue:
                color = Color.BLUE;
                mRenderer.sphere.colorChange(color);
                break;
            case R.id.btnYellow:
                color = Color.YELLOW;
                mRenderer.sphere.colorChange(color);
                break;
        }
    }
}