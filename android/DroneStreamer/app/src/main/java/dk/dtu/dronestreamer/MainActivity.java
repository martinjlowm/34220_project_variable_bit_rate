package dk.dtu.dronestreamer;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public static String DEBUG_TAG = "DroneStreamer";
    private static int CAMERA_ID = -1;

    private OrientationEventListener orientationListener = null;

    private static Context CONTEXT;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;

    private UDPsender udpSender = null;

    private volatile FFmpegFrameRecorder recorder;

    String stringPath = "/sdcard/samplevideo.3gp";

    HandlerThread myThread = new HandlerThread("Worker Thread");


    class MyHandler extends Handler {
        public MyHandler(Looper myLooper) {
            super(myLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    udpSender.sendData((byte[]) msg.obj);
                    break;
                case 2:
                    udpSender.sendChunks((byte[]) msg.obj, 62208);
                    break;
                default:
                    break;
            }

        }
    }

    Looper mLooper = null;
    MyHandler mHandler = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONTEXT = getApplicationContext();
        CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;

        setContentView(R.layout.activity_main);


        if (mHandler == null) {
            udpSender = new UDPsender("10.16.108.188", 10000);
            myThread.start();
            mLooper = myThread.getLooper();
            mHandler = new MyHandler(mLooper);
        }

        orientationListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int orientation) {
                if (previewing) {
                    setCameraDisplayOrientation(MainActivity.this, CAMERA_ID, camera);
                }
            }
        };

        Button buttonStartCameraPreview = (Button) findViewById(R.id.startcamerapreview);
        Button buttonStopCameraPreview = (Button) findViewById(R.id.stopcamerapreview);
        Button buttonSwitchCameraPreview = (Button) findViewById(R.id.switchcamera);
        Button buttonSendPacket = (Button) findViewById(R.id.sendpacket);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        buttonStartCameraPreview.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!previewing) {
                    camera = Camera.open(CAMERA_ID);
                    if (camera != null) {
                        try {
                            setCameraDisplayOrientation(MainActivity.this, CAMERA_ID, camera);
                            camera.setPreviewDisplay(surfaceHolder);
                            camera.setPreviewCallback(MainActivity.this);
                            camera.startPreview();
                            previewing = true;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        buttonStopCameraPreview.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (camera != null && previewing) {
                    camera.stopPreview();
                    camera.setPreviewCallbackWithBuffer(null);
                    mHandler.removeCallbacksAndMessages(null);
                    camera.release();
                    camera = null;
                    previewing = false;

                }
            }
        });

        buttonSwitchCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewing) {
                    camera.stopPreview();
                } else {
                    return;
                }
//NB: if you don't release the current camera before switching, you app will crash
                camera.release();

//swap the id of the camera to be used
                if (CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(CAMERA_ID);

                setCameraDisplayOrientation(MainActivity.this, CAMERA_ID, camera);
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
            }
        });

        buttonSendPacket.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = "LOL".getBytes(); // Some Arbitrary object
                mHandler.sendMessage(msg);
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        orientationListener.enable();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        orientationListener.disable();
    }


    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!previewing)
            return;
        /* get video data */
        Message msg = mHandler.obtainMessage();
        msg.what = 2;
        //msg.obj = data; // Some Arbitrary object
        msg.obj = data;//compressFrame(data, camera);
        mHandler.sendMessage(msg);

        //Log.v(DEBUG_TAG, String.format("Data (length: %d): %s", data.length, data.toString()));
    }

    private byte[] compressFrame(byte[] data, Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        int format = parameters.getPreviewFormat();

        //YUV formats require more conversion
        if (format == ImageFormat.NV21 /*|| format == ImageFormat.YUY2 || format == ImageFormat.NV16*/) {
            int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;
            // Get the YuV image
            YuvImage yuv_image = new YuvImage(data, format, w, h, null);
            // Convert YuV to Jpeg
            Rect rect = new Rect(0, 0, w, h);
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            yuv_image.compressToJpeg(rect, 100, output_stream);
            byte[] byt = output_stream.toByteArray();
            return byt;
        }
        return null;
    }
}
