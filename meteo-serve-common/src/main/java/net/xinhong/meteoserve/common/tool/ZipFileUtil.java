package net.xinhong.meteoserve.common.tool;


import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/3/7 0007.
 */
public class ZipFileUtil {

    /**
     * 加压zipFile 到当前zipFile目录
     * @param zipFile 压缩文件
     */
    public static void unZip(File zipFile)throws IOException {
        unZipFile(zipFile,null);
    }

    /**
     * 解压zipFile到descDir 目录
     * @param zipFile  压缩文件
     * @param descDir  解压文件位置
     * @throws IOException
     */
    public static void unZip(File zipFile,String descDir)throws IOException{
        unZipFile(zipFile,descDir);
    }
    /**
     * @param zipFile
     * @param descDir
     * @throws IOException
     */
    private static void unZipFile(File zipFile, String descDir) throws IOException {
        if(!zipFile.exists()){
         //   logger.warn("找不到指定的文件：{}",zipFile.getPath());
            return ;
        }
        if (descDir== null  || descDir.trim().equals("")){
            descDir = zipFile.getParent();
        }else{
            File file = new File(descDir);
            if (!file.exists())
                file.mkdirs();
            descDir = file.getPath();
        }
        ZipFile zip = new ZipFile(zipFile);
        for(Enumeration entries = zip.entries();entries.hasMoreElements();){
            InputStream in = null;
            OutputStream out = null;
            try {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                String zipEntryName = entry.getName();
                in = zip.getInputStream(entry);
                String outPaht = descDir+File.separator+zipEntryName;
                out = new FileOutputStream(outPaht);
                byte[] buf = new byte[1024];
                int len;
                while ((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
            } finally {
                if(in!=null)
                  in.close();
                if(out!=null)
                 out.close();
            }
        }
    }
}
