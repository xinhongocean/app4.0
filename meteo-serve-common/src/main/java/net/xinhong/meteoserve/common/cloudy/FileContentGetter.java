package net.xinhong.meteoserve.common.cloudy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * 获取数据文件内容
 * 原获取文件内容的方法，后由FileInputStream取代
 * @author sjn
 *
 */
public class FileContentGetter {

    private static final Log logger = LogFactory.getLog(FileContentGetter.class);
    public static ByteBuffer readStreamToBuffer(String filename)
    {
        File file = new File(filename);
        try {
            ByteBuffer byteBuf = ByteBuffer.allocate((int) file.length());
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            fc.read(byteBuf);//1 读取
            byteBuf.flip();
            fc.close();
            fis.close();
            return byteBuf;
        }catch (FileNotFoundException e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "readStreamToBuffer,所读取的数据文件【" + filename + "】不存在";
            logger.info(message);
            return null;
        } catch (IOException e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "readStreamToBuffer,读取数据文件【" + filename + "】时发生错误";
            logger.error(message);
            return null;
        }catch (Exception e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "readStreamToBuffer,读取数据文件【" + filename + "】时发生错误";
            logger.info(message);
            return null;
        }

    }

    public static ByteBuffer readStreamToBuffer(InputStream inputStream, boolean allocateDirect) throws IOException
    {
        if (inputStream == null)
        {
//	        String message = Logging.getMessage("nullValue.InputStreamIsNull");
//	        Logging.logger().severe(message);
//	        throw new IllegalArgumentException(message);
            String msg = "nullValue.InputStreamIsNull";
            throw new IllegalArgumentException(msg);
        }

        ReadableByteChannel channel = Channels.newChannel(inputStream);
        return readChannelToBuffer(channel, allocateDirect);
    }
    public static ByteBuffer readChannelToBuffer(ReadableByteChannel channel, boolean allocateDirect) throws IOException
    {
        if (channel == null)
        {
//	        String message = Logging.getMessage("nullValue.ChannelIsNull");
//	        Logging.logger().severe(message);
//	        throw new IllegalArgumentException(message);

            String message = "nullValue.ChannelIsNull";
        }

        final int PAGE_SIZE = (int) Math.round(Math.pow(2, 16));
//	     ByteBuffer buffer = WWBufferUtil.newByteBuffer(PAGE_SIZE, allocateDirect);
        ByteBuffer buffer = newByteBuffer(PAGE_SIZE, allocateDirect);

        int count = 0;
        while (count >= 0)
        {
            count = channel.read(buffer);
            if (count > 0 && !buffer.hasRemaining())
            {
                ByteBuffer biggerBuffer = allocateDirect ? ByteBuffer.allocateDirect(buffer.limit() + PAGE_SIZE)
                        : ByteBuffer.allocate(buffer.limit() + PAGE_SIZE);
                biggerBuffer.put((ByteBuffer) buffer.rewind());
                buffer = biggerBuffer;
            }
        }

        if (buffer != null)
            buffer.flip();

        return buffer;
    }

    //------------------------从gov.nasa.worldwind.util.WWBufferUtil转换------------------------------//
    public static ByteBuffer newByteBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
//          String message = Logging.getMessage("generic.SizeOutOfRange", size);
//          Logging.logger().severe(message);
            String msg = "所要读取的长度<0，无法读取";
            throw new IllegalArgumentException(msg);
        }

        return allocateDirect ? newDirectByteBuffer(size) : ByteBuffer.allocate(size);
    }
    protected static ByteBuffer newDirectByteBuffer(int size)
    {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
    //-----------------------------------------------------------------------------------------------//
}
