// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   KMPMatch.java

package xinhong_ucar.unidata.io;

public class KMPMatch
{

	private final byte match[];
	private final int failure[];

	public KMPMatch(byte match[])
	{
		this.match = match;
		failure = computeFailure(match);
	}

	public int getMatchLength()
	{
		return match.length;
	}

	public int indexOf(byte data[], int start, int max)
	{
		int j = 0;
		if (data.length == 0)
			return -1;
		if (start + max > data.length)
			System.out.println("HEY KMPMatch");
		for (int i = start; i < start + max; i++)
		{
			for (; j > 0 && match[j] != data[i]; j = failure[j - 1]);
			if (match[j] == data[i])
				j++;
			if (j == match.length)
				return (i - match.length) + 1;
		}

		return -1;
	}

	private int[] computeFailure(byte match[])
	{
		int result[] = new int[match.length];
		int j = 0;
		for (int i = 1; i < match.length; i++)
		{
			for (; j > 0 && match[j] != match[i]; j = result[j - 1]);
			if (match[i] == match[i])
				j++;
			result[i] = j;
		}

		return result;
	}
}