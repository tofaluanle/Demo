
package cn.test;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrackInfo;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.media.SubtitleTrackInfo;
import android.media.VideoCodecInfo;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * VideoPlayView *
 * 
 * @author 罗勇 (luoyong@biaoqi.com.cn)
 * @since 1.0
 * @date 2011-11-03
 */
public class VideoPlayView extends SurfaceView {
    private String TAG = "VideoPlayView";

    // settable by the client
    private Uri mUri;

    private int mDuration;

    // all possible internal states
    private static final int STATE_ERROR = -1;

    private static final int STATE_IDLE = 0;

    private static final int STATE_PREPARING = 1;

    private static final int STATE_PREPARED = 2;

    private static final int STATE_PLAYING = 3;

    private static final int STATE_PAUSED = 4;

    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static final String MVC = "MVC";

    // mCurrentState is a VideoPlayView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoPlayView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;

    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;

    private MediaPlayer mMediaPlayer = null;

    private int mVideoWidth;

    private int mVideoHeight;

    private int mSurfaceWidth;

    private int mSurfaceHeight;

    private int mCurrentBufferPercentage;

    private playerCallback myPlayerCallback = null;

    private int mSeekWhenPrepared; // recording the seek position while
                                   // preparing

    private boolean mCanPause;

    private boolean mCanSeekBack;

    private boolean mCanSeekForward;

    private AudioManager mAudioManager = null;

    public VideoPlayView(Context context) {
        super(context);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        initVideoView();
    }

    public VideoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        initVideoView();
    }

    public VideoPlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        initVideoView();
    }

/*    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i(TAG, "onMeasure");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i(TAG, "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i(TAG, "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else {
                // Log.i(TAG, "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }
        }
        Log.i(TAG, "   onMeasure    setting size: " + width + 'x' + height);
        // setMeasuredDimension(VideoPlayActivity.screenWidth,
        // VideoPlayActivity.screenHeight);
        setMeasuredDimension(width, height);
    }*/

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /*
                 * Parent says we can be as big as we want. Just don't be larger
                 * than max size imposed on ourselves.
                 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
                /*
                 * Parent says we can be as big as we want, up to specSize.
                 * Don't be larger than specSize, and don't be larger than the
                 * max size imposed on ourselves.
                 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        openPlayer();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null && mTargetState != STATE_IDLE) {
            mMediaPlayer.stop();
            Log.i(TAG, "***********stopPlayback: next");
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }
    
    public void stopPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    private void openPlayer() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }

        // 关闭android内置的音乐服务
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        this.getContext().sendBroadcast(i);
        // 关闭用户的音乐回调接口
        if (myPlayerCallback != null)
            myPlayerCallback.onCloseMusic();

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();
            mDuration = -1;
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mMediaPlayerSeekCompleteListener);

            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(this.getContext(), mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (myPlayerCallback != null)
                myPlayerCallback.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (myPlayerCallback != null)
                myPlayerCallback.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalStateException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (myPlayerCallback != null)
                myPlayerCallback.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (SecurityException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (myPlayerCallback != null)
                myPlayerCallback.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            Log.d(TAG, "mVideoWidth: " + mVideoWidth + ", mVideoHeight " + mVideoHeight);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                // 注意:不能随便修改SurfaceView的大小,会影响PIP的效果
                // getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            }
        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
                    MediaPlayer.BYPASS_METADATA_FILTER);

            if (data != null) {
                mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
                        || data.getBoolean(Metadata.PAUSE_AVAILABLE);
                mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
                        || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
                mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
                        || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
            } else {
                mCanPause = mCanSeekBack = mCanSeekForward = true;
            }

            if (myPlayerCallback != null) {
                myPlayerCallback.onPrepared(mMediaPlayer);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
                                                    // changed after seekTo()
                                                    // call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                Log.i(TAG, "video size: " + mVideoWidth + "/" + mVideoHeight);
                // 注意:不能随便修改SurfaceView的大小,会影响PIP的效果
                //getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                // getHolder().setFixedSize(1280, 720);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the
                    // size
                    // we need), so we won't get a "surface changed" callback,
                    // so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (myPlayerCallback != null) {
                myPlayerCallback.onCompletion(mMediaPlayer);
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.e(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            /* If an error handler has been supplied, use it and finish. */
            if (myPlayerCallback != null) {
                if (myPlayerCallback.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (myPlayerCallback != null)
                myPlayerCallback.onBufferingUpdate(mp, percent);
        }
    };

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (myPlayerCallback != null) {
                myPlayerCallback.onInfo(mp, what, extra);
                return true;
            }
            return false;
        }
    };

    private MediaPlayer.OnSeekCompleteListener mMediaPlayerSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            // TODO Auto-generated method stub
            if (myPlayerCallback != null)
                myPlayerCallback.onSeekComplete(mp);
        }
    };

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openPlayer();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            Log.i(TAG, "*************surfaceDestroyed************");
            release(true);
        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
       if(mTargetState == STATE_IDLE){
    	   return;
       }
        mCurrentState = STATE_IDLE;
        if (cleartargetstate) {
        	 new Thread(new Runnable() {

                 @Override
                 public void run() {
                     // TODO Auto-generated method stub
                     if (mMediaPlayer != null) {
                         mMediaPlayer.reset();
                     }
                     if (mMediaPlayer != null) {
                    	 Log.i(TAG, "***********surfaceDestroyed");
                         mMediaPlayer.release();
                     }
                     mMediaPlayer = null;
                 }
             }).start();
        	/*if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            if (mMediaPlayer != null) {
           	 Log.i(TAG, "***********surfaceDestroyed");
                mMediaPlayer.release();
            }
            mMediaPlayer = null;*/
            mTargetState = STATE_IDLE;
        }else{
        	if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            if (mMediaPlayer != null) {
            	Log.i(TAG, "***********openPlayer");
                mMediaPlayer.release();
            }
            mMediaPlayer = null;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState()) {
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState()) {
        }
        return false;
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openPlayer();
    }

    // cache duration as mDuration for faster access
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    /**
     * 设置视频的缩放尺寸
     * 
     * @param width
     * @param height
     */
    public void setVideoScale(int leftMargin, int topMargin, int width, int height) {

        LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;

        setLayoutParams(lp);
    }

    // 在全屏和原始尺寸间切换(FrameLayout)
    public void setVideoScaleFrameLayout(int leftMargin, int topMargin, int width, int height) {

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            // 下面的这个强制装换要依据xml中的类型决定.
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)layoutParams;
            params.leftMargin = leftMargin;
            params.rightMargin = leftMargin;
            params.topMargin = topMargin;
            params.bottomMargin = topMargin;
            params.width = width;
            params.height = height;
            setLayoutParams(params);
        }
    }

    // 在全屏和原始尺寸间切换(LinearLayout)
    public void setVideoScaleLinearLayout(int leftMargin, int topMargin, int width, int height) {

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            // 下面的这个强制装换要依据xml中的类型决定.
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)layoutParams;
            params.leftMargin = leftMargin;
            params.rightMargin = leftMargin;
            params.topMargin = topMargin;
            params.bottomMargin = topMargin;
            params.width = width;
            params.height = height;

            setLayoutParams(params);
        }
    }

    public double calculateZoom(double ScrennWidth, double ScrennHeight) {
        double dRet = 1.0;
        double VideoWidth = (double)mVideoWidth;
        double VideoHeight = (double)mVideoHeight;
        double dw = ScrennWidth / VideoWidth;
        double dh = ScrennHeight / VideoHeight;
        if (dw > dh)
            dRet = dh;
        else
            dRet = dw;

        return dRet;
    }

    // 控制外面乱改内部状态.
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * 声音增大一个默认单元
     */
    public void voiceInc() {
        // 音乐音量
        // int max =
        // mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // Log.i(TAG, "StreamVolume max : " + max8 + " StreamVolume current : "
        // + current8);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current + 1,
                AudioManager.FLAG_SHOW_UI);
    }

    /**
     * 声音减小一个默认单元
     */
    public void voiceDec() {
        // 音乐音量
        // int max =
        // mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // Log.i(TAG, "StreamVolume max : " + max8 + " StreamVolume current : "
        // + current8);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current - 1,
                AudioManager.FLAG_SHOW_UI);
    }

    /**
     * Register a callback to be invoked
     * 
     * @param l The callback that will be run
     */
    public void setPlayerCallbackListener(playerCallback l) {
        myPlayerCallback = l;
    }

    // 用户回调接口.
    public interface playerCallback {
        //
        boolean onError(MediaPlayer mp, int framework_err, int impl_err);

        //
        void onCompletion(MediaPlayer mp);

        //
        boolean onInfo(MediaPlayer mp, int what, int extra);

        //
        void onBufferingUpdate(MediaPlayer mp, int percent);

        //
        void onPrepared(MediaPlayer mp);

        //
        void onSeekComplete(MediaPlayer mp);

        // 视频开始播放前，关闭音乐.
        void onCloseMusic();

    }

    /***************************************************************************************************/
    // mstar Extension APIs start

    // 设置视频播放的速度
    public boolean setPlayMode(int speed) {
        if (speed < -32 || speed > 32)
            return false;

        if (isInPlaybackState()) {
            return mMediaPlayer.setPlayMode(speed);
        }
        return false;
    }

    // 获取视频播放的速度
    public int getPlayMode() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getPlayMode();
        }
        return 64;
    }

    // 获取音轨信息
    public AudioTrackInfo getAudioTrackInfo(final boolean typeIsAudio) {
        if (isInPlaybackState()) {
            return mMediaPlayer.getAudioTrackInfo(typeIsAudio);
        }
        return null;
    }

    // 设置你要播放的音轨，数据源来自getAudioTrackInfo的返回值
    public void setAudioTrack(int track) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setAudioTrack(track);
        }
    }

    // 设置你要播放的字幕编码,一个视频可有多个字幕。比如英文字幕，汉字字幕)
    public void setSubtitleTrack(int track) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSubtitleTrack(track);
        }
    }

    // 设置附加的字幕文
    public void setSubtitleDataSource(final String Uri) {
        if (mMediaPlayer != null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mMediaPlayer.setSubtitleDataSource(Uri);
                }
            }).start();
        }
    }

    // 打开字幕
    public void onSubtitleTrack() {
        if (mMediaPlayer != null) {
            mMediaPlayer.onSubtitleTrack();
        }
    }

    // 关闭字幕
    public void offSubtitleTrack() {
        if (mMediaPlayer != null) {
            mMediaPlayer.offSubtitleTrack();
        }
    }

    // 获取字幕数据，以字符串返回，字符串编码统一为utf-8
    public String getSubtitleData() {
        String str = "";
        if (mMediaPlayer != null) {
            return mMediaPlayer.getSubtitleData();
        }
        return str;
    }

    // 是否一开始就要调用这个接口?
    // 设置字幕的绘制SurfaceHolder
    public void setSubtitleDisplay(SurfaceHolder sh) {
        if (mMediaPlayer != null && mCurrentState != STATE_IDLE) {
            mMediaPlayer.setSubtitleDisplay(sh);
        }
    }

    // divx begin
    // public native int divx_GetTitle();
    public int divx_GetTitle() {
        // String str="";
        if (mMediaPlayer != null) {
            return mMediaPlayer.divx_GetTitle();
        } else {
            return 0;
        }
        // return str;
    }

    // public native void divx_SetTitle(int u32ID);
    public void divx_SetTitle(int u32ID) {
        if (mMediaPlayer != null) {
            mMediaPlayer.divx_SetTitle(u32ID);
        }
    }

    // public native int divx_GetEdition();
    public int divx_GetEdition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.divx_GetEdition();
        } else {
            return 0;
        }
    }

    // public native void divx_SetEdition(int u32ID);
    public void divx_SetEdition(int u32ID) {
        if (mMediaPlayer != null) {
            mMediaPlayer.divx_SetEdition(u32ID);
        }
    }

    // public native int divx_GetChapter();
    public int divx_GetChapter() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.divx_GetChapter();
        } else {
            return 0;
        }
    }

    // public native void divx_SetChapter(int u32ID);
    public void divx_SetChapter(int u32ID) {
        if (mMediaPlayer != null) {
            mMediaPlayer.divx_SetChapter(u32ID);
        }
    }

    // public native int divx_GetAutochapter();
    public int divx_GetAutochapter() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.divx_GetAutochapter();
        } else {
            return 0;
        }
    }

    // public native void divx_SetAutochapter(int u32ID);
    public void divx_SetAutochapter(int u32ID) {
        if (mMediaPlayer != null) {
            mMediaPlayer.divx_SetAutochapter(u32ID);
        }
    }

    // public native int divx_GetAutochapterTime(int u32ID);
    public int divx_GetAutochapterTime(int u32ID) {
        if (mMediaPlayer != null) {
            return mMediaPlayer.divx_GetAutochapterTime(u32ID);
        } else {
            return 0;
        }
    }

    // public native int divx_GetChapterTime(int u32ID);
    public int divx_GetChapterTime(int u32ID) {
        if (mMediaPlayer != null) {
            return mMediaPlayer.divx_GetChapterTime(u32ID);
        } else {
            return 0;
        }
    }

    //
    public int getTotalTitle() {
        if (mMediaPlayer != null) {
            int count = 0;
            Metadata md;
            md = mMediaPlayer.getMetadata(MediaPlayer.METADATA_ALL,
                    MediaPlayer.BYPASS_METADATA_FILTER);
            count = md.getInt(Metadata.DIVX_TITLE_NUM);
            return count;

        } else {
            return 0;
        }
    }

    //
    public int getTotalEdition() {
        if (mMediaPlayer != null) {
            int count = 0;
            Metadata md = null;
            md = mMediaPlayer.getMetadata(MediaPlayer.METADATA_ALL,
                    MediaPlayer.BYPASS_METADATA_FILTER);
            count = md.getInt(Metadata.DIVX_EDITION_NUM);
            return count;

        } else {
            return 0;
        }
    }

    //
    public int getTotalChapter() {
        if (mMediaPlayer != null) {
            int count = 0;
            Metadata md;
            md = mMediaPlayer.getMetadata(MediaPlayer.METADATA_ALL,
                    MediaPlayer.BYPASS_METADATA_FILTER);
            count = md.getInt(Metadata.DIVX_CHAPTER_NUM);
            return count;

        } else {
            return 0;
        }
    }

    //
    public String getAudioCodecType() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getAudioCodecType();
        }
        return null;
    }

    //
    public VideoCodecInfo getVideoInfo() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getVideoInfo();
        }
        return null;
    }

    //
    public int setSubtitleSync(int time) {
        if (isInPlaybackState()) {
            return mMediaPlayer.setSubtitleSync(time);
        }
        return 0;
    }

    //
    public SubtitleTrackInfo getSubtitleTrackInfo(int subtitlePosition) {
        SubtitleTrackInfo trackInfor = new SubtitleTrackInfo(null);
        if (isInPlaybackState()) {
            return mMediaPlayer.getSubtitleTrackInfo(subtitlePosition);
        }
        return trackInfor;
    }

    public SubtitleTrackInfo getAllSubtitleTrackInfo() {
        SubtitleTrackInfo trackInfor = new SubtitleTrackInfo(null);
        if (isInPlaybackState()) {
            return mMediaPlayer.getAllSubtitleTrackInfo();
        }
        return trackInfor;
    }
    // mstar Extension APIs end
    
    /***************************************************************************************************/
}
