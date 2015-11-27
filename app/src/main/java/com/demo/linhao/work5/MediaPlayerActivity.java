package com.demo.linhao.work5;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by linhao on 15/11/24.
 */
public class MediaPlayerActivity extends Activity {
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer player;
    private  int vWidth,vHeight;
    private Timer timer;
    private ImageButton rew;//后退
    private ImageButton pause;//暂停
    private ImageButton start;//开始
    private ImageButton ff;//快速
    private TextView play_time;//播放时间
    private TextView all_time;//总播放时间
    private TextView title;//播放文件名称
    private SeekBar seekbar;//进度条
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.main);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String mPath = "/mnt/sdcard/music.mp3";
        if (uri!=null)
        {
            mPath=uri.getPath();//获得媒体路径
        }else {
            //从多媒体文件预览传来的点播文件路径
            Bundle bundle = getIntent().getExtras();
            if (bundle!=null)
            {
                String t_path = bundle.getString("path");
                if (t_path!=null && !"".equals(t_path))
                {
                    mPath=t_path;
                }
            }
        }
        title = (TextView) findViewById(R.id.titles);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        rew = (ImageButton) findViewById(R.id.rew);
        pause = (ImageButton) findViewById(R.id.pause);
        start = (ImageButton) findViewById(R.id.start);
        ff = (ImageButton) findViewById(R.id.ff);

        play_time = (TextView) findViewById(R.id.play_time);
        all_time = (TextView) findViewById(R.id.all_time);
        seekbar = (SeekBar) findViewById(R.id.seekbar);

        //给SurfaceView添加CallBack监听器
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //设置MediaPlayer在指定的Surface中进行播放
                player.setDisplay(surfaceHolder);
                //在指定了MediaPlayer播放器后，使用prepareAsync准备播放
                player.prepareAsync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //当SurfaceView中得Surface被创建时调用
            }
        });
        //为了可以播放视频或者使用Camera预览，需要指定Buffer类型
        surfaceHolder.setType(surfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //下面开始实例化MediaPlayer对象
        player = new MediaPlayer();
        //设置播放完成监听器
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        });
        //设置prepare完成监听器
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //当prepare完成后，该方法触发用于播放器
                //首先取得食品的宽和高
                vWidth = player.getVideoWidth();
                vHeight = player.getVideoHeight();
                if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()) {
                    //如果视频的宽或者高超出了屏幕的大小，则进行缩放
                    float wRadio = (float) vWidth / (float) currDisplay.getWidth();
                    float hRadio = (float) vHeight / (float) currDisplay.getHeight();
                    //选择大得比例进行缩放
                    float radio = Math.max(wRadio, hRadio);
                    vWidth = (int) Math.ceil((float) vWidth / radio);
                    vHeight = (int) Math.ceil((float) vHeight / radio);
                    //重新设置surfaceview的布局参数
                    surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));
                    //然后开始播放视频
                    player.start();
                } else {
                    player.start();
                }
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                //启动时间更新及进度更新任务，没0.5s更新一次
                timer = new Timer();
                timer.schedule(new MyTask(), 50, 500);
            }
        });
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            title.setText(mPath.substring(mPath.lastIndexOf("/") + 1));
            player.setDataSource(mPath);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        //暂停操作
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                player.pause();
                if (timer!=null)
                {
                    timer.cancel();
                    timer = null;
                }
            }
        });
        //播放操作
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //按下开始操作后，pause设为可见，start设为隐藏
                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                //启动播放
                player.start();
                if (timer!=null)
                {
                    timer.cancel();
                    timer = null;
                }
                //启动时间更新及进度条更新任务，每0.5s更新一次
                timer = new Timer();
                timer.schedule(new MyTask(),50,500);
            }
        });
        //快退操作，每次快退10秒
        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否正在播放
                if (player.isPlaying())
                {
                    int currentPosition = player.getCurrentPosition();
                    if (currentPosition-10000>0)
                    {
                        player.seekTo(currentPosition-10000);
                    }
                }
            }
        });
        //快进操作，每次快进10s
        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否在播放
                if (player.isPlaying())
                {
                    int currentPosition = player.getCurrentPosition();
                    if (currentPosition+10000<player.getDuration())
                    {
                        player.seekTo(currentPosition+10000);
                    }
                }
            }
        });
        //取得当前Display对象
        currDisplay = this.getWindowManager().getDefaultDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"文件夹");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==1)
        {
            Intent intent = new Intent(MediaPlayerActivity.this,MyFileActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }
    //进度栏分类
    public class MyTask extends TimerTask{
        @Override
        public void run() {
            Message message = new Message();
            message.what=1;
            //发生消息更新进度栏和时间显示
            handler.sendMessage(message);
        }
    }
    //处理进度栏和时间显示
    private final Handler handler = new Handler(){
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:
                    Time progeress = new Time(player.getCurrentPosition());
                    Time allTime = new Time(player.getDuration());
                    String timeStr = progeress.toString();
                    String timeStr2 = allTime.toString();
                    //已播放时间
                    play_time.setText(timeStr.substring(timeStr.indexOf(":")+1));
                    //总时间
                    all_time.setText(timeStr2.substring(timeStr.indexOf(":")+1));
                    int progressValue = 0;
                    if (player.getDuration()>0)
                    {
                        progressValue = seekbar.getMax()*player.getCurrentPosition()/player.getDuration();
                    }
                    //进度栏进度
                    seekbar.setProgress(progressValue);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
