package ca.parcplace.birdsong.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.util.Log;

public class SongRecorder extends AsyncTask<String, Integer, Object> {

	private AudioRecord recorder;
	private boolean recordablestate = false;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		recorder = findAudioRecord();

		Log.d("audio recorder", "format: " + recorder.getAudioFormat());
		Log.d("audio recorder",
				"channel config: " + recorder.getChannelConfiguration());
		Log.d("audio recorder", "channel count: " + recorder.getChannelCount());
		Log.d("audio recorder", "sample rate: " + recorder.getSampleRate());
	}

	@Override
	protected String doInBackground(String... params) {
		String filename = params[0];
		record(filename);

		return "some result";
	}

//	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
	private static int[] mSampleRates = new int[] { 44100, 22050, 11025, 8000 };

	private AudioRecord findAudioRecord() {
		for (int rate : mSampleRates) {
//			for (short audioFormat : new short[] {
//					AudioFormat.ENCODING_PCM_8BIT,
//					AudioFormat.ENCODING_PCM_16BIT }) {
			for (short audioFormat : new short[] {
					AudioFormat.ENCODING_PCM_16BIT,
					AudioFormat.ENCODING_PCM_8BIT }) {
				for (short channelConfig : new short[] {
						AudioFormat.CHANNEL_IN_STEREO,
						AudioFormat.CHANNEL_IN_MONO }) {
					try {
						Log.d("stuff", "Attempting rate " + rate + "Hz, bits: "
								+ audioFormat + ", channel: " + channelConfig);
						int bufferSize = AudioRecord.getMinBufferSize(rate,
								channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							// check if we can instantiate and have a success
							AudioRecord recorder = new AudioRecord(
									AudioSource.DEFAULT, rate, channelConfig,
									audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
								Log.d("stuff", "minumum buffer size is "
										+ bufferSize);
								return recorder;
							}
						}
					} catch (Exception e) {
						Log.e("stuff", rate + "Exception, keep trying.", e);
					}
				}
			}
		}
		return null;
	}

	void record(String filenameBase) {
		try {
			File pcmOut = new File(filenameBase + ".pcm");
			FileOutputStream fout = new FileOutputStream(pcmOut);
			byte[] buffer = new byte[12800];

			recordablestate = true;
			recorder.startRecording();
			while (recordablestate) {
				try {
					// read in up to buffer size
					int readBytes = recorder.read(buffer, 0, buffer.length);

					for (int i = 0; i < readBytes; i++) {
						fout.write(buffer[i]);
					}
				} catch (Exception t) {
					recordablestate = false;
				}
			}
			fout.close();

			Log.i("recorded ",
					"file: " + pcmOut.exists() + " " + pcmOut.length());
			copyWaveFile(filenameBase + ".pcm", filenameBase + ".wav");
		} catch (Exception e) {
			recordablestate = false;
		}
	}

	void stopRecord() {
		recordablestate = false;
		// stop recording
		recorder.stop();
		// release recording resources
		recorder.release();

	}

	private static final int RECORDER_BPP = 16;

	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = recorder.getSampleRate();
		int channels = 1;
		long byteRate = RECORDER_BPP * longSampleRate * channels / 8;

		byte[] data = new byte[1024];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + HEADER_LENGTH;

			Log.i("rec", "File size: " + totalDataLen);

//			writeWaveFileHeader(out, totalAudioLen, totalDataLen,
//					longSampleRate, channels, byteRate);
			
			writeHeader(out, totalAudioLen);

			int len = 0;
			while ((len = in.read(data)) != -1) {
				out.write(data, 0, len);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final int HEADER_LENGTH = 44;

	private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {

		byte[] header = new byte[HEADER_LENGTH];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1 (PCM)
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (channels * RECORDER_BPP / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	public static final String RIFF_HEADER = "RIFF";
	public static final String WAVE_HEADER = "WAVE";
	public static final String FMT_HEADER = "fmt ";
	public static final String DATA_HEADER = "data";
	public static final int HEADER_BYTE_LENGTH = 44;	// 44 bytes for header

	private void writeHeader(FileOutputStream fos, long subChunk2Size) {

//		int byteRate = 16000;
//		int audioFormat = 1;
//		int sampleRate = 8000;
//		int bitsPerSample = 16;
//		int channels = 1;
//		long chunkSize = 2;
//		long subChunk1Size = 16;
//		int blockAlign = 2;

		int audioFormat = 1;
		int sampleRate = recorder.getSampleRate();
		int bitsPerSample = (recorder.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT ? 16 : 8);
		int channels = recorder.getChannelCount();
		long chunkSize = 36 + subChunk2Size;
		long subChunk1Size = 16;
		int blockAlign = channels * bitsPerSample / 8;   // == NumChannels * BitsPerSample/8
		int byteRate = sampleRate * channels * bitsPerSample / 8;  // == SampleRate * NumChannels * BitsPerSample/8
		
		try {
			fos.write(RIFF_HEADER.getBytes());
			// little endian
			fos.write(new byte[] { (byte) (chunkSize), (byte) (chunkSize >> 8),
					(byte) (chunkSize >> 16), (byte) (chunkSize >> 24) });
			fos.write(WAVE_HEADER.getBytes());
			fos.write(FMT_HEADER.getBytes());
			fos.write(new byte[] { (byte) (subChunk1Size),
					(byte) (subChunk1Size >> 8), (byte) (subChunk1Size >> 16),
					(byte) (subChunk1Size >> 24) });
			fos.write(new byte[] { (byte) (audioFormat),
					(byte) (audioFormat >> 8) });
			fos.write(new byte[] { (byte) (channels), (byte) (channels >> 8) });
			fos.write(new byte[] { (byte) (sampleRate),
					(byte) (sampleRate >> 8), (byte) (sampleRate >> 16),
					(byte) (sampleRate >> 24) });
			fos.write(new byte[] { (byte) (byteRate), (byte) (byteRate >> 8),
					(byte) (byteRate >> 16), (byte) (byteRate >> 24) });
			fos.write(new byte[] { (byte) (blockAlign),
					(byte) (blockAlign >> 8) });
			fos.write(new byte[] { (byte) (bitsPerSample),
					(byte) (bitsPerSample >> 8) });
			fos.write(DATA_HEADER.getBytes());
			fos.write(new byte[] { (byte) (subChunk2Size),
					(byte) (subChunk2Size >> 8), (byte) (subChunk2Size >> 16),
					(byte) (subChunk2Size >> 24) });
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
