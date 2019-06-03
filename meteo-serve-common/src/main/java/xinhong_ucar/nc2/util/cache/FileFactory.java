// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   FileFactory.java

package xinhong_ucar.nc2.util.cache;

import java.io.IOException;
import xinhong_ucar.nc2.util.CancelTask;

// Referenced classes of package xinhong_ucar.nc2.util.cache:
//			FileCacheable

public interface FileFactory
{

	public abstract FileCacheable open(String s, int i, CancelTask canceltask, Object obj)
		throws IOException;
}