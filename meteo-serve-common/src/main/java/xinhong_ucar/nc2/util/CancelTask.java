// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   CancelTask.java

package xinhong_ucar.nc2.util;


public interface CancelTask
{

	public abstract boolean isCancel();

	public abstract void setError(String s);

	public abstract void setProgress(String s, int i);
}