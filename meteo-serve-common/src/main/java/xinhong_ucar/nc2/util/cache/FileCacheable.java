// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   FileCacheable.java

package xinhong_ucar.nc2.util.cache;

import java.io.IOException;

// Referenced classes of package xinhong_ucar.nc2.util.cache:
//			FileCache

public interface FileCacheable
{

	public abstract String getLocation();

	public abstract void close()
		throws IOException;

	public abstract long getLastModified();

	public abstract void setFileCache(FileCache filecache);
}