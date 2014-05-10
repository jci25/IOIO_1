package com.test.ioio_1;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MjpegInputStream extends DataInputStream {
	private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
	private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
	private final String CONTENT_LENGTH = "Content-Length";
	private final static int HEADER_MAX_LENGTH = 100;
	private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
	private int mContentLength = -1;

	public static MjpegInputStream read(String url) {
		HttpResponse res;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpParams httpParams = httpclient.getParams();
        //HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
		try {
			res = httpclient.execute(new HttpGet(URI.create(url)));
			return new MjpegInputStream(res.getEntity().getContent());
		} catch (ClientProtocolException e) {
			System.out.println(e.toString());
		} catch (IOException e) {
			System.out.println(e.toString());
		} finally {

		}
		return null;
	}

	public MjpegInputStream(InputStream in) {
		super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	private int getEndOfSequence(DataInputStream in, byte[] sequence)
			throws IOException {
		int seqIndex = 0;
		byte c;
		for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
			c = (byte) in.readUnsignedByte();
			if (c == sequence[seqIndex]) {
				seqIndex++;
				if (seqIndex == sequence.length)
					return i + 1;
			} else
				seqIndex = 0;
		}
		return -1;
	}

	private int getStartOfSequence(DataInputStream in, byte[] sequence)
			throws IOException {
		int end = getEndOfSequence(in, sequence);
		return (end < 0) ? (-1) : (end - sequence.length);
	}

	private int parseContentLength(byte[] headerBytes) throws IOException,
			NumberFormatException {
		ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
		Properties props = new Properties();
		props.load(headerIn);
		return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
	}

	public Bitmap readMjpegFrame() throws IOException {
		mark(FRAME_MAX_LENGTH);
		int headerLen = getStartOfSequence(this, SOI_MARKER);
		reset();
		byte[] header = new byte[headerLen];
		readFully(header);
		try {
			mContentLength = parseContentLength(header);
		} catch (NumberFormatException nfe) {
			mContentLength = getEndOfSequence(this, EOF_MARKER);
		}
		reset();
		byte[] frameData = new byte[mContentLength];
		skipBytes(headerLen);
		//System.out.println(frameData.length);
		readFully(frameData);
		//System.out.println("read");
		return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
	}
}