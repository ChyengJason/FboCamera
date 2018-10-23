package com.jscheng.scamera.record;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.jscheng.scamera.util.LogUtil.TAG;

/**
 * Created By Chengjunsen on 2018/9/20
 */
public class VideoEncoder {
    private static final int FRAME_RATE = 30;
    private static final int IFRAME_INTERVAL = 10;

    private Surface mInputSurface;
    private MediaMuxer mMuxer;
    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    public VideoEncoder(int width, int height, File outputFile)
            throws IOException {
        int bitRate = height * width * 3 * 8 * FRAME_RATE / 256;
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        Log.d(TAG, "format: " + format);

        mEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
        mMuxer = new MediaMuxer(outputFile.toString(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        mTrackIndex = -1;
        mMuxerStarted = false;
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void release() {
        Log.d(TAG, "releasing encoder objects");
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (endOfStream) {
            Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "MediaCodec.INFO_TRY_AGAIN_LATER");
                // no output available yet
                if (!endOfStream) {
                    break;
                } else {
                    Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "MediaCodec.INFO_OUTPUT_FORMAT_CHANGED");
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.d(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else {
                ByteBuffer encodedData = mEncoder.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" + mBufferInfo.presentationTimeUs);
                } else {
                    Log.d(TAG, "drainEncoder mBufferInfo: " + mBufferInfo.size);
                }
                mEncoder.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.d(TAG, "end of stream reached");
                    }
                    break;
                }
            }
        }
    }
}
