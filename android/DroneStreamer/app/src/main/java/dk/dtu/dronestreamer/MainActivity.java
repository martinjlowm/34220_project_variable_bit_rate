package dk.dtu.dronestreamer;

//https://github.com/vanevery/JavaCV-0.5-Stream-Test
//https://github.com/mackhowell/JavaCV-0.5-Stream-Test
//https://github.com/vanevery/Android-MJPEG-Video-Capture-FFMPEG/blob/master/src/com/mobvcasting/mjpegffmpeg/MJPEGFFMPEGTest.java
//https://code.google.com/p/spydroid-ipcamera/issues/attachmentText?id=2&aid=20000000&name=CameraStreamer.java&token=ABZ6GAdAtAJgaLbOx7ymgQyNRRfzNGBClQ%3A1421978695329

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ShortBuffer;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

public class MainActivity extends Activity {

    private final static String LOG_TAG = "MainActivity";

    private PowerManager.WakeLock mWakeLock;

    //	private String ffmpeg_link = "rtmp://username:password@xxx.xxx.xxx.xxx:1935/live/test.flv";
    //private String ffmpeg_link = "udp://10.16.101.46:10000/stream.flv";
//    private String ffmpeg_link = "/mnt/sdcard/new_stream.flv";
    private String ffmpeg_link;

    private volatile FFmpegFrameRecorder recorder;
    boolean recording = false;
    long startTime = 0;

    private int sampleAudioRateInHz = 44100;
    private int imageWidth = 320;
    private int imageHeight = 240;
    private double imageQuality = 50;
    private int frameRate = 30;

    private Thread audioThread;
    volatile boolean runAudioThread = true;
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;

    private CameraView cameraView;
    private IplImage yuvIplimage = null;

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;

    private EditText address;

    private LinearLayout mainLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        initLayout();
        //initRecorder(imageWidth,imageHeight,imageQuality,frameRate);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, LOG_TAG);
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recording = false;
    }


    private void initLayout() {

        mainLayout = (LinearLayout) this.findViewById(R.id.record_layout);
        cameraView = new CameraView(this);
        mainLayout.addView(cameraView);
        cameraView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));
        address = (EditText) findViewById(R.id.address);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);

        btn1.setText("start 320x240");
        btn2.setText("start 640x480");
        btn3.setText("start 1024x680");
        btn4.setText("start 1280x720");
        btn5.setText("start 1920x1080");

        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recording) {
                    initRecorder(320,240,50,12);
                    startRecording();
                    Log.w(LOG_TAG, "Start Button Pushed");
                    btn1.setText("Stop 320x240");
                } else {
                    stopRecording();
                    Log.w(LOG_TAG, "Stop Button Pushed");
                    btn1.setText("Start 320x240");
                }
            }
        });
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recording) {
                    //initRecorder(2048,960,100,30);
                    //initRecorder(1024,768,1.0,12);
                    initRecorder(640,480,50,12);
                    startRecording();
                    Log.w(LOG_TAG, "Start Button Pushed");
                    btn2.setText("Stop 640x480");
                } else {
                    stopRecording();
                    Log.w(LOG_TAG, "Stop Button Pushed");
                    btn2.setText("Start 640x480");
                }
            }
        });
        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recording) {
                    //initRecorder(2048,960,100,30);
                    //initRecorder(1024,768,1.0,12);
                    initRecorder(1024,680,50,12);
                    startRecording();
                    Log.w(LOG_TAG, "Start Button Pushed");
                    btn3.setText("Stop 1024x680");
                } else {
                    stopRecording();
                    Log.w(LOG_TAG, "Stop Button Pushed");
                    btn3.setText("Start 1024x680");
                }
            }
        });
        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recording) {
                    //initRecorder(2048,960,100,30);
                    //initRecorder(1024,768,1.0,12);
                    initRecorder(1280,720,50,12);
                    startRecording();
                    Log.w(LOG_TAG, "Start Button Pushed");
                    btn4.setText("Stop 1280x720");
                } else {
                    stopRecording();
                    Log.w(LOG_TAG, "Stop Button Pushed");
                    btn4.setText("Start 1280x720");
                }
            }
        });
        btn5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recording) {
                    //initRecorder(2048,960,100,30);
                    //initRecorder(1024,768,1.0,12);
                    initRecorder(1920,1080,50,12);
                    startRecording();
                    Log.w(LOG_TAG, "Start Button Pushed");
                    btn5.setText("Stop 1920x1080");
                } else {
                    stopRecording();
                    Log.w(LOG_TAG, "Stop Button Pushed");
                    btn5.setText("Start 1920x1080");
                }
            }
        });

        Log.v(LOG_TAG, "added cameraView to mainLayout");
    }

    private void initRecorder(int imgW, int imgH, double imgQ, int fraR) {
        imageWidth = imgW;
        imageHeight = imgH;
        imageQuality = imgQ;
        frameRate = fraR;
        ffmpeg_link = address.getText().toString();

        cameraView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));

        Log.w(LOG_TAG,"initRecorder");

        if (yuvIplimage == null) {
            // Recreated after frame size is set in surface change method
            yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_8U, 1);
            //yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_32S, 2);

            Log.v(LOG_TAG, "IplImage.create");
        }

        recorder = new FFmpegFrameRecorder(ffmpeg_link, imageWidth, imageHeight, 1);
        Log.v(LOG_TAG, "FFmpegFrameRecorder: " + ffmpeg_link + " imageWidth: " + imageWidth + " imageHeight " + imageHeight);

        //recorder.setVideoBitrate(10 * 1024 * 1024);

        recorder.setFormat("flv");
        Log.v(LOG_TAG, "recorder.setFormat(\"flv\")");

        recorder.setSampleRate(sampleAudioRateInHz);
        Log.v(LOG_TAG, "recorder.setSampleRate(sampleAudioRateInHz)");

        // re-set in the surface changed method as well
        recorder.setFrameRate(frameRate);
        Log.v(LOG_TAG, "recorder.setFrameRate(frameRate)");

        recorder.setVideoQuality(imageQuality);
        Log.v(LOG_TAG, "recorder.setVideoQuality(imageQuality)");

        //recorder.setAudioCodec(avcodec.AV_CODEC_ID_MPEG1VIDEO);

        // Create audio recording thread
        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);
    }

    // Start the capture
    public void startRecording() {
        try {
            recorder.start();
            startTime = System.currentTimeMillis();
            recording = true;
            audioThread.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        // This should stop the audio thread from running
        runAudioThread = false;

        if (recorder != null && recording) {
            recording = false;
            Log.v(LOG_TAG,"Finishing recording, calling stop and release on recorder");
            try {
                recorder.stop();
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }
    }

    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            // Set the thread priority
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = new short[bufferSize];

            Log.d(LOG_TAG, "audioRecord.startRecording()");
            audioRecord.startRecording();

            // Audio Capture/Encoding Loop
            while (runAudioThread) {
                // Read from audioRecord
                //bufferReadResult = 1;
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
                //bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
                if (bufferReadResult > 0) {
                    //Log.v(LOG_TAG,"audioRecord bufferReadResult: " + bufferReadResult);

                    // Changes in this variable may not be picked up despite it being "volatile"
                    if (recording) {
                        try {
                            // Write to FFmpegFrameRecorder
                            //Buffer[] buffer = {ShortBuffer.wrap(new short[1], 0, bufferReadResult)};
                            Buffer[] buffer = {ShortBuffer.wrap(audioData, 0, bufferReadResult)};
                            recorder.record(buffer);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(LOG_TAG,e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.v(LOG_TAG,"AudioThread Finished");

            /* Capture/Encoding finished, release recorder */
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v(LOG_TAG,"audioRecord released");
            }
        }
    }

    class CameraView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

        private boolean previewRunning = false;

        private SurfaceHolder holder;
        private Camera camera;

        private byte[] previewBuffer;

        long videoTimestamp = 0;

        Bitmap bitmap;
        Canvas canvas;

        public CameraView(Context _context) {
            super(_context);

            holder = this.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open();

            try {
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(this);

                Camera.Parameters currentParams = camera.getParameters();
                currentParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                Log.v(LOG_TAG,"Preview Framerate: " + currentParams.getPreviewFrameRate());
                Log.v(LOG_TAG,"Preview imageWidth: " + currentParams.getPreviewSize().width + " imageHeight: " + currentParams.getPreviewSize().height);
                camera.setParameters(currentParams);

                // Use these values
                imageWidth = currentParams.getPreviewSize().width;
                imageHeight = currentParams.getPreviewSize().height;
                frameRate = currentParams.getPreviewFrameRate();

                bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ALPHA_8);
	    		
	        	
	        	/*
				Log.v(LOG_TAG,"Creating previewBuffer size: " + imageWidth * imageHeight * ImageFormat.getBitsPerPixel(currentParams.getPreviewFormat())/8);
	        	previewBuffer = new byte[imageWidth * imageHeight * ImageFormat.getBitsPerPixel(currentParams.getPreviewFormat())/8];
				camera.addCallbackBuffer(previewBuffer);
	            camera.setPreviewCallbackWithBuffer(this);
	        	*/

                camera.startPreview();
                previewRunning = true;
            }
            catch (IOException e) {
                Log.v(LOG_TAG,e.getMessage());
                e.printStackTrace();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.v(LOG_TAG,"Surface Changed: width " + width + " height: " + height);

            // We would do this if we want to reset the camera parameters
            /*
            if (!recording) {
    			if (previewRunning){
    				camera.stopPreview();
    			}

    			try {
    				//Camera.Parameters cameraParameters = camera.getParameters();
    				//p.setPreviewSize(imageWidth, imageHeight);
    			    //p.setPreviewFrameRate(frameRate);
    				//camera.setParameters(cameraParameters);
    				
    				camera.setPreviewDisplay(holder);
    				camera.startPreview();
    				previewRunning = true;
    			}
    			catch (IOException e) {
    				Log.e(LOG_TAG,e.getMessage());
    				e.printStackTrace();
    			}	
    		}            
            */

            // Get the current parameters
            Camera.Parameters currentParams = camera.getParameters();
            Log.v(LOG_TAG,"Preview Framerate: " + currentParams.getPreviewFrameRate());
            Log.v(LOG_TAG,"Preview imageWidth: " + currentParams.getPreviewSize().width + " imageHeight: " + currentParams.getPreviewSize().height);

            // Use these values
            imageWidth = currentParams.getPreviewSize().width;
            imageHeight = currentParams.getPreviewSize().height;
            frameRate = currentParams.getPreviewFrameRate();

            // Create the yuvIplimage if needed
            yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_8U, 2);
            //yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_32S, 2);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                camera.setPreviewCallback(null);

                previewRunning = false;
                camera.release();

            } catch (RuntimeException e) {
                Log.v(LOG_TAG,e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            if (yuvIplimage != null && recording) {
                videoTimestamp = 1000 * (System.currentTimeMillis() - startTime);

                // Put the camera preview frame right into the yuvIplimage object
                yuvIplimage.getByteBuffer().put(data);

                // FAQ about IplImage:
                // - For custom raw processing of data, getByteBuffer() returns an NIO direct
                //   buffer wrapped around the memory pointed by imageData, and under Android we can
                //   also use that Buffer with Bitmap.copyPixelsFromBuffer() and copyPixelsToBuffer().
                // - To get a BufferedImage from an IplImage, we may call getBufferedImage().
                // - The createFrom() factory method can construct an IplImage from a BufferedImage.
                // - There are also a few copy*() methods for BufferedImage<->IplImage data transfers.

                // Let's try it..
                // This works but only on transparency
                // Need to find the right Bitmap and IplImage matching types
            	
            	/*
            	bitmap.copyPixelsFromBuffer(yuvIplimage.getByteBuffer());
            	//bitmap.setPixel(10,10,Color.MAGENTA);
            	
            	canvas = new Canvas(bitmap);
            	Paint paint = new Paint(); 
            	paint.setColor(Color.GREEN);
            	float leftx = 20; 
            	float topy = 20; 
            	float rightx = 50; 
            	float bottomy = 100; 
            	RectF rectangle = new RectF(leftx,topy,rightx,bottomy); 
            	canvas.drawRect(rectangle, paint);
       		 
            	bitmap.copyPixelsToBuffer(yuvIplimage.getByteBuffer());
                */
                //Log.v(LOG_TAG,"Writing Frame");

                try {

                    // Get the correct time
                    recorder.setTimestamp(videoTimestamp);

                    // Record the image into FFmpegFrameRecorder
                    recorder.record(yuvIplimage);

                } catch (FFmpegFrameRecorder.Exception e) {
                    Log.v(LOG_TAG,e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}