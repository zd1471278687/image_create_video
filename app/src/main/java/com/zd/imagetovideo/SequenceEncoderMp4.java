package com.zd.imagetovideo;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Create by zhangdong 2019/9/20
 */
public class SequenceEncoderMp4 extends org.jcodec.api.android.SequenceEncoder {

    /**
     * 控制帧率缩放,时间长度调整这个值
     * 值越小,生成视频时间越长
     *
     * 如:timeScale = 50     一秒视屏采用50张图片
     */
    private int timeScale = 1;


    public SequenceEncoderMp4(File out) throws IOException {
        super(out);
        this.ch = NIOUtils.writableFileChannel(out);

        // Muxer that will store the encoded frames
        muxer = new MP4Muxer(ch, Brand.MP4);

        // Add video track to muxer
        outTrack = muxer.addTrack(TrackType.VIDEO, timeScale);

        // Allocate a buffer big enough to hold output frames
        _out = ByteBuffer.allocate(1920 * 1080 * 6);

        // Create an instance of encoder
        encoder = new H264Encoder();

        // Transform to convert between RGB and YUV
        transform = ColorUtil.getTransform(ColorSpace.RGB, encoder.getSupportedColorSpaces()[0]);

        // Encoder extra data ( SPS, PPS ) to be stored in a special place of
        // MP4
        spsList = new ArrayList<ByteBuffer>();
        ppsList = new ArrayList<ByteBuffer>();
    }

    private SeekableByteChannel ch;
    private Picture toEncode;
    private Transform transform;
    private H264Encoder encoder;
    private ArrayList<ByteBuffer> spsList;
    private ArrayList<ByteBuffer> ppsList;
    private FramesMP4MuxerTrack outTrack;
    private ByteBuffer _out;
    private int frameNo;
    private MP4Muxer muxer;


    public void encodeNativeFrame(Picture pic) throws IOException {
        if (toEncode == null) {
            toEncode = Picture.create(pic.getWidth(), pic.getHeight(), encoder.getSupportedColorSpaces()[0]);
        }

        // Perform conversion
        transform.transform(pic, toEncode);

        // Encode image into H.264 frame, the result is stored in '_out' buffer
        _out.clear();
        ByteBuffer result = encoder.encodeFrame(toEncode, _out);

        // Based on the frame above form correct MP4 packet
        spsList.clear();
        ppsList.clear();
        H264Utils.wipePS(result, spsList, ppsList);
        H264Utils.encodeMOVPacket(result);

        // Add packet to video track
        outTrack.addFrame(new MP4Packet(result, frameNo, timeScale, 1, frameNo, true, null, frameNo, 0));

        frameNo++;
    }

    public void finish() throws IOException {
        // Push saved SPS/PPS to a special storage in MP4
        outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList, 4));

        // Write MP4 header and finalize recording
        muxer.writeHeader();
        NIOUtils.closeQuietly(ch);
    }
}
