// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   GuardedBy.java

package net.jcip.annotations;

import java.lang.annotation.Annotation;

public interface GuardedBy
	extends Annotation
{

	public abstract String value();
}