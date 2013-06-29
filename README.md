android-app-playaudio
=====================

#概要

[android.media.MediaPlayer](http://developer.android.com/reference/android/media/MediaPlayer.html)による音楽再生と、[android.media.AudioTrack](http://developer.android.com/reference/android/media/AudioTrack.html)による音声再生が、同時に再生可能であるかどうかを確認するためのサンプルアプリである。

* MediaPlayer
	* 再生する音
		* WAVを再生

* AudioTrack
	* 再生する音 (以下から選択)
		* [android.media.AudioRecord](http://developer.android.com/reference/android/media/AudioRecord.html)でキャプチャした音を再生
		* WAVを再生
	* サンプリングレート (以下から選択)
		* 44.1kHz
		* 16kHz  
	選択されたAutioTrackのサンプリングレートをAudioRecordのサンプリングレートにも適用。
	* オーディオストリーム
		* STREAM_VOICE_CALL

