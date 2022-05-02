package com.example.ex06_painting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    //com.google.ar.core.Session;
    Session mSession;

    GLSurfaceView mySurView;
    MainRenderer mRenderer;
    CheckBox checkBox;


    Config mConfig; // ARCore session 설정정보를 받을 변수

    float displayX, displayY;
    // 새로운 선인지,         점 추가인지
    boolean mNewPath = false, mPointAdd = false;

    // 이전 점을 받을 배열
    float[] lastPoint = {0.0f, 0.0f, 0.0f};
    float same_dist = 0.001f;

    float[] projMatrix = new float[16];
    float[] viewMatrix = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySurView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        checkBox = findViewById(R.id.checkBox);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);

        if(displayManager != null){
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                   @Override
                   public void onDisplayAdded(int displayId) {}

                   @Override
                   public void onDisplayRemoved(int displayId) {}

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

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = null;

                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                mRenderer.mCamera.transformDisplayGeometry(frame);

                PointCloud pointCloud = frame.acquirePointCloud();

                mRenderer.mPointCloud.update(pointCloud);

                pointCloud.release();




                Camera camera = frame.getCamera();

                camera.getProjectionMatrix(projMatrix,0,0.1f,100.0f);
                camera.getViewMatrix(viewMatrix,0);

                mRenderer.updateProjMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);

                // 선그리기
                if(checkBox.isChecked()){ // 스크린 그리기 상태
                    float[] screenPoint = getScreenPoint(displayX, displayY,
                            mRenderer.width, mRenderer.height,
                            projMatrix, viewMatrix);

                    if(mNewPath){
                        mNewPath = false;
                        mRenderer.addLine(screenPoint[0], screenPoint[1], screenPoint[2]);
                        lastPoint[0] = screenPoint[0];
                        lastPoint[1] = screenPoint[1];
                        lastPoint[2] = screenPoint[2];

                    }else if(mPointAdd){
                        if(sameCheck(screenPoint[0], screenPoint[1], screenPoint[2])){
                            // 점 추가
                            mRenderer.addPoint(screenPoint[0], screenPoint[1], screenPoint[2]);

                            lastPoint[0] = screenPoint[0];
                            lastPoint[1] = screenPoint[1];
                            lastPoint[2] = screenPoint[2];
                        }
                        mPointAdd = false;
                    }

                }else {  // 일반 3D 좌표 상태
                    // 새로운 선 그리기
                    if (mNewPath) {
                        mNewPath = false;
                        List<HitResult> arr = frame.hitTest(displayX, displayY);

                        for (HitResult hr : arr) {
                            Pose pose = hr.getHitPose();

                            // 새로운 라인 그리기
                            mRenderer.addLine(pose.tx(), pose.ty(), pose.tz());
                            lastPoint[0] = pose.tx();
                            lastPoint[1] = pose.ty();
                            lastPoint[2] = pose.tz();
                        }

                    } else if (mPointAdd) { // 점 추가 라면

                        List<HitResult> arr = frame.hitTest(displayX, displayY);

                        for (HitResult hr : arr) {
                            Pose pose = hr.getHitPose();

                            if (sameCheck(pose.tx(), pose.ty(), pose.tz())) {
                                // 점 추가
                                mRenderer.addPoint(pose.tx(), pose.ty(), pose.tz());

                                lastPoint[0] = pose.tx();
                                lastPoint[1] = pose.ty();
                                lastPoint[2] = pose.tz();
                                break;
                            }
                        }
                        mPointAdd = false;
                    }
                }
            }
        };

        mRenderer = new MainRenderer(mr);
        mySurView.setPreserveEGLContextOnPause(true);
        mySurView.setEGLContextClientVersion(2);
        mySurView.setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        displayX = event.getX();
        displayY = event.getY();

        switch (event.getAction()){
            // 맨 처음 찍기 (선그리기 시작)
            case MotionEvent.ACTION_DOWN:
                mNewPath = true; // 새로운 선
                mPointAdd = true; // 점 추가
                break;
            // 선 그리는 중
            case MotionEvent.ACTION_MOVE:
                mPointAdd = true; // 점 추가
                break;
            // 선 그리기 끝
            case MotionEvent.ACTION_UP:
                mPointAdd = false; // 점 추가 완료
                break;
        }
        return true;
    }

    boolean sameCheck(float x, float y, float z){
        float dx = x - lastPoint[0];
        float dy = y - lastPoint[1];
        float dz = z - lastPoint[2];

        boolean res = Math.sqrt(dx*dx + dy*dy + dz*dz) > same_dist;
        Log.d("sameCheck", res+"");
        return res;
    }
    public void removeBtnGo(View view){
        mRenderer.removePath();
        Log.d("removeBtnGo", "선 삭제 해 줘");
    }

    // 스크린의 앞쪽에 배치되는 평면
    float[] getScreenPoint(float x, float y, float w, float h, float[] projMat, float [] viewMat){
        float[] position = new float[3];
        float[] direction = new float[3];

        x = x * 2 / w - 1.0f;
        y = (h - y) * 2 / h - 1.0f;

        float[] viewProjMat = new float[16];
        Matrix.multiplyMM(viewProjMat, 0, projMat, 0, viewMat, 0);

        float[] invertedMat = new float[16];
        Matrix.setIdentityM(invertedMat, 0);
        Matrix.invertM(invertedMat, 0, viewProjMat, 0);

        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];

        Matrix.multiplyMV(nearPlanePoint, 0, invertedMat, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedMat, 0, farScreenPoint, 0);

        position[0] = nearPlanePoint[0] / nearPlanePoint[3];
        position[1] = nearPlanePoint[1] / nearPlanePoint[3];
        position[2] = nearPlanePoint[2] / nearPlanePoint[3];

        direction[0] = farPlanePoint[0] / farPlanePoint[3] - position[0];
        direction[1] = farPlanePoint[1] / farPlanePoint[3] - position[1];
        direction[2] = farPlanePoint[2] / farPlanePoint[3] - position[2];

        //이건 평면을 만드는거 같다
        normalize(direction);

        position[0] += (direction[0] * 0.1f);
        position[1] += (direction[1] * 0.1f);
        position[2] += (direction[2] * 0.1f);

        return position;
    }

    //평면을 만드는 거?
    private void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }
    // pause
    @Override
    protected void onPause() {
        super.onPause();
        mySurView.onPause();
        mSession.pause();
    }

    // Resume
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
        mConfig = new Config(mSession);
        mSession.configure(mConfig);
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mySurView.onResume();
        mySurView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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

}