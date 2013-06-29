package jp.saka.playaudio;

import jp.saka.playaudio.R;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;

import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.media.AudioRecord;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.os.Handler;
import android.content.res.Resources;

import android.media.audiofx.AcousticEchoCanceler;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;

public class PlayAudio extends Activity implements Runnable
{
	private MediaPlayer mMediaPlayer = null;
	private AudioManager mAudioManager;
	private RadioGroup mSampleRateRadioGroup, mSelectModeRadioGroup;
	private TextView mStatusTextView, mLogTextView;
	private Handler mHandler;
	private Resources mResources;

	private boolean isAcousticEchoCancelerSupported() {
		return AcousticEchoCanceler.isAvailable();
	}

	private void updateStatusTextView() {
		String status = "";
		if (mStatusTextView == null) {
			return;
		}
		if (isAcousticEchoCancelerSupported()) {
			status += "AEC: Supported";
		} else {
			status += "AEC: NOT Supported";
		}
		mStatusTextView.setText(status);
	}

	private void appendLogTextView(String s) {
		if (mLogTextView == null) {
			return;
		}
		mLogTextView.append(s);
	}

	private void appendLogTextView(Handler handler, final String s) {
		if (handler == null) {
			return;
		}
		handler.post( new Runnable() {
			public void run() {
				appendLogTextView(s);
			}
		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mHandler = new Handler();

		mResources = getResources();

		mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));

		if (mAudioManager != null) {
			mAudioManager.setSpeakerphoneOn(true);
			appendLogTextView(mHandler, "set speakerphone on.\n");
		}

		mStatusTextView = (TextView)findViewById(R.id.StatusTextView);
		mLogTextView = (TextView)findViewById(R.id.LogTextView);

		mSampleRateRadioGroup = (RadioGroup)findViewById(R.id.SampleRateRadioGroup);
		mSampleRateRadioGroup.check(R.id.SampleRateRadioButton_44100);

		mSelectModeRadioGroup = (RadioGroup)findViewById(R.id.SelectModeRadioGroup);
		mSelectModeRadioGroup.check(R.id.SelectModeRadioButton_MIC);

		Button button;

		//////////////////////////////////////////////////////////////
		//
		// MediaPalyer
		//
		//////////////////////////////////////////////////////////////
		mMediaPlayer = MediaPlayer.create(this, R.raw.google_moog_dq_16000hz);

		button = (Button) findViewById(R.id.MediaPlayerPlayButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
					try {
						mMediaPlayer.prepare();
						mMediaPlayer.seekTo(0);
					} catch (Exception e) {
					}
				}
				mMediaPlayer.start();
			}
		});

		button = (Button) findViewById(R.id.MediaPlayerStopButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
					try {
						mMediaPlayer.prepare();
						mMediaPlayer.seekTo(0);
					} catch (Exception e) {
					}
				}
			}
		});


		//////////////////////////////////////////////////////////////
		//
		// AudioTrack
		//
		//////////////////////////////////////////////////////////////

		button = (Button) findViewById(R.id.AudioTrackPlayButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				playAudioTrack();
			}
		});

		button = (Button) findViewById(R.id.AudioTrackStopButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				stopAudioTrack();
			}
		});

		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

		updateStatusTextView();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer = null;
		}
		stopAudioTrack();
	}

	private static int AUDIO_SOURCE_MIC = 1;
	private static int AUDIO_SOURCE_WAVE_FILE = 2;
	private static String m44100hzWaveFile = "/mnt/sdcard/GoogleMoog-DQ-44100hz.wav";
	private static String m16000hzWaveFile = "/mnt/sdcard/GoogleMoog-DQ-16000hz.wav";

	private int getAudioSource() {
		int mode;
		int id = mSelectModeRadioGroup.getCheckedRadioButtonId();
		switch (id) {
			case R.id.SelectModeRadioButton_MIC:
				mode = AUDIO_SOURCE_MIC;
				break;

			case R.id.SelectModeRadioButton_WAVE_FILE:
			default:
				mode = AUDIO_SOURCE_WAVE_FILE;
				break;
		}
		return mode;
	}

	private int getSampleRate() {
		int rate;
		int id = mSampleRateRadioGroup.getCheckedRadioButtonId();
		switch (id) {
			case R.id.SampleRateRadioButton_44100:
				rate = 44100;
				break;

			case R.id.SampleRateRadioButton_16000:
			default:
				rate = 16000;
				break;
		}
		return rate;
	}

	private String getWaveFile() {
		if (getSampleRate() == 44100) {
			return m44100hzWaveFile;
		} else {
			return m16000hzWaveFile;
		}
	}

	private int getWaveResId() {
		if (getSampleRate() == 44100) {
			return R.raw.google_moog_dq_16000hz;
		} else {
			return R.raw.google_moog_dq_16000hz;
		}
	}

	private int getOutChannel() {
		if (getAudioSource() == AUDIO_SOURCE_MIC) {
			return AudioFormat.CHANNEL_OUT_MONO;
		} else {
			return AudioFormat.CHANNEL_OUT_STEREO;
		}
	}

	private int getInChannel() {
		if (getAudioSource() == AUDIO_SOURCE_MIC) {
			return AudioFormat.CHANNEL_IN_MONO;
		} else {
			return AudioFormat.CHANNEL_IN_STEREO;
		}
	}

	private int getEncoding() {
		return AudioFormat.ENCODING_PCM_16BIT;
	}

	private byte[] readWaveFile(String file) {
		File f = new File(file);
		byte[] data = new byte[(int) f.length()];

		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			in.read(data);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	private class WaveDataInputStream {

		private InputStream mInputStream=null;

		private void skipWaveDataHeader() throws java.io.IOException {
			if (mInputStream != null) {
				int i=0, s=0;
				byte[] head = new byte[44];
				while ((s < head.length) && ((i = mInputStream.read(head, s, head.length-s)) > -1)) {
					s += i;
				}
			}
		}

		private WaveDataInputStream(String file) {
			try {
				mInputStream = new DataInputStream(new FileInputStream(file));
				skipWaveDataHeader();
			} catch (Exception e) {
			}
		}

		private WaveDataInputStream(int id) {
			try {
				if (mResources != null) {
					mInputStream = new DataInputStream(mResources.openRawResource(id));
					skipWaveDataHeader();
				}
			} catch (Exception e) {
			}
		}

		private int read(byte[] buf, int size) {
			try {
				if (mInputStream != null) {
					return mInputStream.read(buf, 0, size);
				} else {
					return -1;
				}
			} catch (Exception e) {
				return -1;
			}
		}

		private void close() {
			try {
				if (mInputStream != null) {
					mInputStream.close();
					mInputStream = null;
				}
			} catch (Exception e) {
			}
		}
	}

	private boolean mPlay = false;
	private boolean mRunning = false;
	private PlayAudioParams mParams = null;

	void playAudioTrack() {
		synchronized (this) {
			if (!mPlay && !mRunning) {
				Thread thread = new Thread(this);
				mParams = new PlayAudioParams();
				mPlay = true;
				thread.start();
			}
		}
	}

	void stopAudioTrack() {
		synchronized (this) {
			mPlay = false;
		}
	}

	public void run() {

		synchronized (this) {
			if (!mPlay || mParams == null) {
				return;
			}
			mRunning = true;
		}

		int trackBufSize = android.media.AudioTrack.getMinBufferSize(mParams.rate, mParams.outch, mParams.encoding);
		AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, mParams.rate, mParams.outch, mParams.encoding, trackBufSize, AudioTrack.MODE_STREAM);
		track.play();

		if (mParams.source == AUDIO_SOURCE_WAVE_FILE) {
			//Log.d("sakalog", "play " + mParams.wavefile);
			//WaveDataInputStream wavein = new WaveDataInputStream(mParams.wavefile);
			Log.d("sakalog", "play " + mResources.getResourceName(mParams.waveid));
			WaveDataInputStream wavein = new WaveDataInputStream(mParams.waveid);
			byte[] wave = new byte[512];
			int size = wavein.read(wave, wave.length);
			track.write(wave, 0, size);
			while (mPlay && ((size = wavein.read(wave, wave.length)) > -1)) {
				track.write(wave, 0, size);
			}
			wavein.close();
		} else {
			Log.d("sakalog", "play ");
			AudioRecord record = startMicRecording(mParams.rate, mParams.inch, mParams.encoding);
			byte[] buf = new byte[trackBufSize];
			while (mPlay) {
				int size = record.read(buf, 0, buf.length);
				track.write(buf, 0, size);
			}
			stopMicRecording(record);
		}

		track.stop();
		track.release();

		synchronized (this) {
			mPlay = false;
			mRunning = false;
		}
	}

	private class PlayAudioParams {
		public int rate;
		public int outch;
		public int inch;
		public int encoding;
		public int source;
		//public String wavefile;
		public int waveid;

		public PlayAudioParams() {
			rate = getSampleRate();
			outch = getOutChannel();
			inch = getInChannel();
			encoding = getEncoding();
			//wavefile = getWaveFile();
			waveid = getWaveResId();
			source = getAudioSource();
		}
	}

	private AudioRecord startMicRecording(int sampleRate, int channel, int encoding) {
		appendLogTextView(mHandler, "start recording...\n");
		int recordBufSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding);
		AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, encoding, recordBufSize);
		appendLogTextView(mHandler, " -> sample rate: " + sampleRate + "\n");
		appendLogTextView(mHandler, " -> buffer size:" + recordBufSize + "\n");
		int sid = record.getAudioSessionId();
		if (false) {
			appendLogTextView(mHandler, " -> do not use AEC.\n");
		} else {
			AcousticEchoCanceler aec = AcousticEchoCanceler.create(sid);
			if (aec == null) {
				appendLogTextView(mHandler, " -> create AEC error.\n");
			} else {
				appendLogTextView(mHandler, " > create AEC ok.\n");
			}
		}
		record.startRecording();
		appendLogTextView(mHandler, " -> start recording ok.\n");
		return record;
	}

	private void stopMicRecording(AudioRecord record) {
		if (record != null) {
			record.stop();
			record.release();
			appendLogTextView(mHandler, "stop recording ok.\n");
		}
	}
}
