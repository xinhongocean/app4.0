// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   RandomAccessFile.java

package xinhong_ucar.unidata.io;

import xinhong_ucar.nc2.util.cache.FileCache;
import xinhong_ucar.nc2.util.cache.FileCacheable;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// Referenced classes of package xinhong_ucar.unidata.io:
//			KMPMatch

public class RandomAccessFile
	implements DataInput, DataOutput, FileCacheable
{

	public static final int BIG_ENDIAN = 0;
	public static final int LITTLE_ENDIAN = 1;
	protected static boolean debugLeaks = false;
	protected static boolean debugAccess = false;
	protected static Set allFiles = new HashSet();
	protected static List openFiles = Collections.synchronizedList(new ArrayList());
	private static AtomicInteger debug_nseeks = new AtomicInteger();
	private static AtomicLong debug_nbytes = new AtomicLong();
	protected static boolean showOpen = false;
	protected static boolean showRead = false;
	protected static final int defaultBufferSize = 8092;
	protected String location;
	protected FileCache fileCache;
	protected java.io.RandomAccessFile file;
	protected FileChannel fileChannel;
	protected long filePosition;
	protected byte buffer[];
	protected long bufferStart;
	protected long dataEnd;
	protected int dataSize;
	protected boolean endOfFile;
	protected boolean readonly;
	protected boolean bigEndian;
	boolean bufferModified;
	private long minLength;
	private boolean extendMode;

	public static boolean getDebugLeaks()
	{
		return debugLeaks;
	}

	public static void setDebugLeaks(boolean b)
	{
		debugLeaks = b;
	}

	public static List getOpenFiles()
	{
		return openFiles;
	}

	public static List getAllFiles()
	{
		List result = new ArrayList();
		if (null == allFiles)
		{
			return null;
		} else
		{
			result.addAll(allFiles);
			Collections.sort(result);
			return result;
		}
	}

	public static void setDebugAccess(boolean b)
	{
		debugAccess = b;
		if (b)
		{
			debug_nseeks = new AtomicInteger();
			debug_nbytes = new AtomicLong();
		}
	}

	public static int getDebugNseeks()
	{
		return debug_nseeks != null ? debug_nseeks.intValue() : 0;
	}

	public static long getDebugNbytes()
	{
		return debug_nbytes != null ? debug_nbytes.longValue() : 0L;
	}

	protected RandomAccessFile(int bufferSize)
	{
		fileCache = null;
		bufferModified = false;
		minLength = 0L;
		extendMode = false;
		file = null;
		readonly = true;
		init(bufferSize);
	}

	public RandomAccessFile(String location, String mode)
		throws IOException
	{
		this(location, mode, 8092);
		this.location = location;
	}

	public RandomAccessFile(String location, String mode, int bufferSize)
		throws IOException
	{
		fileCache = null;
		bufferModified = false;
		minLength = 0L;
		extendMode = false;
		this.location = location;
		if (debugLeaks)
			allFiles.add(location);
		file = new java.io.RandomAccessFile(location, mode);
		readonly = mode.equals("r");
		init(bufferSize);
		if (debugLeaks)
		{
			openFiles.add(location);
			if (showOpen)
				System.out.println((new StringBuilder()).append("  open ").append(location).toString());
		}
	}

	public java.io.RandomAccessFile getRandomAccessFile()
	{
		return file;
	}

	private void init(int bufferSize)
	{
		bufferStart = 0L;
		dataEnd = 0L;
		dataSize = 0;
		filePosition = 0L;
		buffer = new byte[bufferSize];
		endOfFile = false;
	}

	public void setBufferSize(int bufferSize)
	{
		init(bufferSize);
	}

	public int getBufferSize()
	{
		return buffer.length;
	}

	public void close()
		throws IOException
	{
		if (fileCache != null)
		{
			//fileCache.release(this);
			return;
		}
		if (debugLeaks)
		{
			openFiles.remove(location);
			if (showOpen)
				System.out.println((new StringBuilder()).append("  close ").append(location).toString());
		}
		if (file == null)
			return;
		flush();
		long fileSize = file.length();
		if (!readonly && minLength != 0L && minLength != fileSize)
			file.setLength(minLength);
		file.close();
		file = null;
	}

	public long getLastModified()
	{
		File file = new File(getLocation());
		return file.lastModified();
	}

	public void setFileCache(FileCache fileCache)
	{
		this.fileCache = fileCache;
	}

	public boolean isAtEndOfFile()
	{
		return endOfFile;
	}

	public void seek(long pos)
		throws IOException
	{
		if (pos < 0L)
			throw new IOException("Negative seek offset");
		if (pos >= bufferStart && pos < dataEnd)
		{
			filePosition = pos;
			return;
		} else
		{
			readBuffer(pos);
			return;
		}
	}

	protected void readBuffer(long pos)
		throws IOException
	{
		if (bufferModified)
			flush();
		bufferStart = pos;
		filePosition = pos;
		dataSize = read_(pos, buffer, 0, buffer.length);
		if (dataSize <= 0)
		{
			dataSize = 0;
			endOfFile = true;
		} else
		{
			endOfFile = false;
		}
		dataEnd = bufferStart + (long)dataSize;
	}

	public long getFilePointer()
		throws IOException
	{
		return filePosition;
	}

	public String getLocation()
	{
		return location;
	}

	public long length()
		throws IOException
	{
		long fileLength = file.length();
		if (fileLength < dataEnd)
			return dataEnd;
		else
			return fileLength;
	}

	public void order(int endian)
	{
		if (endian < 0)
		{
			return;
		} else
		{
			bigEndian = endian == 0;
			return;
		}
	}

	public void order(ByteOrder bo)
	{
		if (bo == null)
		{
			return;
		} else
		{
			bigEndian = bo.equals(ByteOrder.BIG_ENDIAN);
			return;
		}
	}

	public void flush()
		throws IOException
	{
		if (bufferModified)
		{
			file.seek(bufferStart);
			file.write(buffer, 0, dataSize);
			bufferModified = false;
		}
	}

	public void setMinLength(long minLength)
	{
		this.minLength = minLength;
	}

	public void setExtendMode()
	{
		extendMode = true;
	}

	public int read()
		throws IOException
	{
		if (filePosition < dataEnd)
		{
			int pos = (int)(filePosition - bufferStart);
			filePosition++;
			return buffer[pos] & 0xff;
		}
		if (endOfFile)
		{
			return -1;
		} else
		{
			seek(filePosition);
			return read();
		}
	}

	protected int readBytes(byte b[], int off, int len)
		throws IOException
	{
		if (endOfFile)
			return -1;
		int bytesAvailable = (int)(dataEnd - filePosition);
		if (bytesAvailable < 1)
		{
			seek(filePosition);
			return readBytes(b, off, len);
		}
		int copyLength = bytesAvailable < len ? bytesAvailable : len;
		System.arraycopy(buffer, (int)(filePosition - bufferStart), b, off, copyLength);
		filePosition += copyLength;
		if (copyLength < len)
		{
			int extraCopy = len - copyLength;
			if (extraCopy > buffer.length)
			{
				extraCopy = read_(filePosition, b, off + copyLength, len - copyLength);
			} else
			{
				seek(filePosition);
				if (!endOfFile)
				{
					extraCopy = extraCopy <= dataSize ? extraCopy : dataSize;
					System.arraycopy(buffer, 0, b, off + copyLength, extraCopy);
				} else
				{
					extraCopy = -1;
				}
			}
			if (extraCopy > 0)
			{
				filePosition += extraCopy;
				return copyLength + extraCopy;
			}
		}
		return copyLength;
	}

	public long readToByteChannel(WritableByteChannel dest, long offset, long nbytes)
		throws IOException
	{
		if (fileChannel == null)
			fileChannel = file.getChannel();
		long need;
		for (need = nbytes; need > 0L;)
		{
			long count = fileChannel.transferTo(offset, need, dest);
			need -= count;
			offset += count;
		}

		return nbytes - need;
	}

	protected int read_(long pos, byte b[], int offset, int len)
		throws IOException
	{
		file.seek(pos);
		int n = file.read(b, offset, len);
		if (debugAccess)
		{
			if (showRead)
				System.out.println((new StringBuilder()).append(" **read_ ").append(location).append(" = ").append(len).append(" bytes at ").append(pos).append("; block = ").append(pos / (long)buffer.length).toString());
			debug_nseeks.incrementAndGet();
			debug_nbytes.addAndGet(len);
		}
		if (extendMode && n < len)
			n = len;
		return n;
	}

	public int read(byte b[], int off, int len)
		throws IOException
	{
		return readBytes(b, off, len);
	}

	public int read(byte b[])
		throws IOException
	{
		return readBytes(b, 0, b.length);
	}

	public byte[] readBytes(int count)
		throws IOException
	{
		byte b[] = new byte[count];
		readFully(b);
		return b;
	}

	public final void readFully(byte b[])
		throws IOException
	{
		readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len)
		throws IOException
	{
		int count;
		for (int n = 0; n < len; n += count)
		{
			count = read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException((new StringBuilder()).append("Reading ").append(location).append(" at ").append(filePosition).append(" file length = ").append(length()).toString());
		}

	}

	public int skipBytes(int n)
		throws IOException
	{
		seek(getFilePointer() + (long)n);
		return n;
	}

	public long skipBytes(long n)
		throws IOException
	{
		seek(getFilePointer() + n);
		return n;
	}

	public void unread()
	{
		filePosition--;
	}

	public void write(int b)
		throws IOException
	{
		if (filePosition < dataEnd)
		{
			int pos = (int)(filePosition - bufferStart);
			buffer[pos] = (byte)b;
			bufferModified = true;
			filePosition++;
		} else
		if (dataSize != buffer.length)
		{
			int pos = (int)(filePosition - bufferStart);
			buffer[pos] = (byte)b;
			bufferModified = true;
			filePosition++;
			dataSize++;
			dataEnd++;
		} else
		{
			seek(filePosition);
			write(b);
		}
	}

	public void writeBytes(byte b[], int off, int len)
		throws IOException
	{
		if (len < buffer.length)
		{
			int spaceInBuffer = 0;
			int copyLength = 0;
			if (filePosition >= bufferStart)
				spaceInBuffer = (int)((bufferStart + (long)buffer.length) - filePosition);
			if (spaceInBuffer > 0)
			{
				copyLength = spaceInBuffer <= len ? spaceInBuffer : len;
				System.arraycopy(b, off, buffer, (int)(filePosition - bufferStart), copyLength);
				bufferModified = true;
				long myDataEnd = filePosition + (long)copyLength;
				dataEnd = myDataEnd <= dataEnd ? dataEnd : myDataEnd;
				dataSize = (int)(dataEnd - bufferStart);
				filePosition += copyLength;
			}
			if (copyLength < len)
			{
				seek(filePosition);
				System.arraycopy(b, off + copyLength, buffer, (int)(filePosition - bufferStart), len - copyLength);
				bufferModified = true;
				long myDataEnd = filePosition + (long)(len - copyLength);
				dataEnd = myDataEnd <= dataEnd ? dataEnd : myDataEnd;
				dataSize = (int)(dataEnd - bufferStart);
				filePosition += len - copyLength;
			}
		} else
		{
			if (bufferModified)
				flush();
			file.seek(filePosition);
			file.write(b, off, len);
			filePosition += len;
			bufferStart = filePosition;
			dataSize = 0;
			dataEnd = bufferStart + (long)dataSize;
		}
	}

	public void write(byte b[])
		throws IOException
	{
		writeBytes(b, 0, b.length);
	}

	public void write(byte b[], int off, int len)
		throws IOException
	{
		writeBytes(b, off, len);
	}

	public final boolean readBoolean()
		throws IOException
	{
		int ch = read();
		if (ch < 0)
			throw new EOFException();
		else
			return ch != 0;
	}

	public final byte readByte()
		throws IOException
	{
		int ch = read();
		if (ch < 0)
			throw new EOFException();
		else
			return (byte)ch;
	}

	public final int readUnsignedByte()
		throws IOException
	{
		int ch = read();
		if (ch < 0)
			throw new EOFException();
		else
			return ch;
	}

	public final short readShort()
		throws IOException
	{
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		if (bigEndian)
			return (short)((ch1 << 8) + ch2);
		else
			return (short)((ch2 << 8) + ch1);
	}

	public final void readShort(short pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			pa[start + i] = readShort();

	}

	public final int readUnsignedShort()
		throws IOException
	{
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		if (bigEndian)
			return (ch1 << 8) + ch2;
		else
			return (ch2 << 8) + ch1;
	}

	public final char readChar()
		throws IOException
	{
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		if (bigEndian)
			return (char)((ch1 << 8) + ch2);
		else
			return (char)((ch2 << 8) + ch1);
	}

	public final int readInt()
		throws IOException
	{
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		if (bigEndian)
			return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
		else
			return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1;
	}

	public final int readIntUnbuffered(long pos)
		throws IOException
	{
		byte bb[] = new byte[4];
		read_(pos, bb, 0, 4);
		int ch1 = bb[0] & 0xff;
		int ch2 = bb[1] & 0xff;
		int ch3 = bb[2] & 0xff;
		int ch4 = bb[3] & 0xff;
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		if (bigEndian)
			return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
		else
			return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1;
	}

	public final void readInt(int pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			pa[start + i] = readInt();

	}

	public final long readLong()
		throws IOException
	{
		if (bigEndian)
			return ((long)readInt() << 32) + ((long)readInt() & 0xffffffffL);
		else
			return ((long)readInt() & 0xffffffffL) + ((long)readInt() << 32);
	}

	public final void readLong(long pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			pa[start + i] = readLong();

	}

	public final float readFloat()
		throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}

	public final void readFloat(float pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			pa[start + i] = Float.intBitsToFloat(readInt());

	}

	public final double readDouble()
		throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}

	public final void readDouble(double pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			pa[start + i] = Double.longBitsToDouble(readLong());

	}

	public final String readLine()
		throws IOException
	{
		StringBuffer input = new StringBuffer();
		int c = -1;
		boolean eol = false;
		do
		{
			if (eol)
				break;
			switch (c = read())
			{
			case -1: 
			case 10: // '\n'
				eol = true;
				break;

			case 13: // '\r'
				eol = true;
				long cur = getFilePointer();
				if (read() != 10)
					seek(cur);
				break;

			default:
				input.append((char)c);
				break;
			}
		} while (true);
		if (c == -1 && input.length() == 0)
			return null;
		else
			return input.toString();
	}

	public final String readUTF()
		throws IOException
	{
		return DataInputStream.readUTF(this);
	}

	public String readString(int nbytes)
		throws IOException
	{
		byte data[] = new byte[nbytes];
		readFully(data);
		return new String(data);
	}

	public final void writeBoolean(boolean v)
		throws IOException
	{
		write(v ? 1 : 0);
	}

	public final void writeBoolean(boolean pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeBoolean(pa[start + i]);

	}

	public final void writeByte(int v)
		throws IOException
	{
		write(v);
	}

	public final void writeShort(int v)
		throws IOException
	{
		write(v >>> 8 & 0xff);
		write(v & 0xff);
	}

	public final void writeShort(short pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeShort(pa[start + i]);

	}

	public final void writeChar(int v)
		throws IOException
	{
		write(v >>> 8 & 0xff);
		write(v & 0xff);
	}

	public final void writeChar(char pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeChar(pa[start + i]);

	}

	public final void writeInt(int v)
		throws IOException
	{
		write(v >>> 24 & 0xff);
		write(v >>> 16 & 0xff);
		write(v >>> 8 & 0xff);
		write(v & 0xff);
	}

	public final void writeInt(int pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeInt(pa[start + i]);

	}

	public final void writeLong(long v)
		throws IOException
	{
		write((int)(v >>> 56) & 0xff);
		write((int)(v >>> 48) & 0xff);
		write((int)(v >>> 40) & 0xff);
		write((int)(v >>> 32) & 0xff);
		write((int)(v >>> 24) & 0xff);
		write((int)(v >>> 16) & 0xff);
		write((int)(v >>> 8) & 0xff);
		write((int)v & 0xff);
	}

	public final void writeLong(long pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeLong(pa[start + i]);

	}

	public final void writeFloat(float v)
		throws IOException
	{
		writeInt(Float.floatToIntBits(v));
	}

	public final void writeFloat(float pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeFloat(pa[start + i]);

	}

	public final void writeDouble(double v)
		throws IOException
	{
		writeLong(Double.doubleToLongBits(v));
	}

	public final void writeDouble(double pa[], int start, int n)
		throws IOException
	{
		for (int i = 0; i < n; i++)
			writeDouble(pa[start + i]);

	}

	public final void writeBytes(String s)
		throws IOException
	{
		int len = s.length();
		for (int i = 0; i < len; i++)
			write((byte)s.charAt(i));

	}

	public final void writeBytes(char b[], int off, int len)
		throws IOException
	{
		for (int i = off; i < len; i++)
			write((byte)b[i]);

	}

	public final void writeChars(String s)
		throws IOException
	{
		int len = s.length();
		for (int i = 0; i < len; i++)
		{
			int v = s.charAt(i);
			write(v >>> 8 & 0xff);
			write(v & 0xff);
		}

	}

	public final void writeUTF(String str)
		throws IOException
	{
		int strlen = str.length();
		int utflen = 0;
		for (int i = 0; i < strlen; i++)
		{
			int c = str.charAt(i);
			if (c >= 1 && c <= 127)
			{
				utflen++;
				continue;
			}
			if (c > 2047)
				utflen += 3;
			else
				utflen += 2;
		}

		if (utflen > 65535)
			throw new UTFDataFormatException();
		write(utflen >>> 8 & 0xff);
		write(utflen & 0xff);
		for (int i = 0; i < strlen; i++)
		{
			int c = str.charAt(i);
			if (c >= 1 && c <= 127)
			{
				write(c);
				continue;
			}
			if (c > 2047)
			{
				write(0xe0 | c >> 12 & 0xf);
				write(0x80 | c >> 6 & 0x3f);
				write(0x80 | c & 0x3f);
			} else
			{
				write(0xc0 | c >> 6 & 0x1f);
				write(0x80 | c & 0x3f);
			}
		}

	}

	public String toString()
	{
		return location;
	}

	public boolean searchForward(KMPMatch match, int maxBytes)
		throws IOException
	{
		long start = getFilePointer();
		long last = maxBytes >= 0 ? Math.min(length(), start + (long)maxBytes) : length();
		long needToScan = last - start;
		int bytesAvailable = (int)(dataEnd - filePosition);
		if (bytesAvailable < 1)
		{
			seek(filePosition);
			bytesAvailable = (int)(dataEnd - filePosition);
		}
		int bufStart = (int)(filePosition - bufferStart);
		int scanBytes = (int)Math.min(bytesAvailable, needToScan);
		int pos = match.indexOf(buffer, bufStart, scanBytes);
		if (pos >= 0)
		{
			seek(bufferStart + (long)pos);
			return true;
		}
		int matchLen = match.getMatchLength();
		for (needToScan -= scanBytes - matchLen; needToScan > (long)matchLen; needToScan -= scanBytes - matchLen)
		{
			readBuffer(dataEnd - (long)matchLen);
			scanBytes = (int)Math.min(buffer.length, needToScan);
			pos = match.indexOf(buffer, 0, scanBytes);
			if (pos > 0)
			{
				seek(bufferStart + (long)pos);
				return true;
			}
		}

		seek(last);
		return false;
	}

}