package com.xinhong.mids3d.util;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author luna  
 * tranfer from gov.nasa.worldwind.util.WWUtil
 */
public class MidsUtil
{
	protected static final String MESSAGE_BUNDLE_NAME = MidsUtil.class.getPackage().getName() + ".MessageStrings";
	public static String getMessage(String property)
    {
        try
        {
            return (String) ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, Locale.getDefault()).getObject(property);
        }
        catch (Exception e)
        {
            String message = "Exception looking up message from bundle " + MESSAGE_BUNDLE_NAME;
            System.err.print(message);
            e.printStackTrace();
            return message;
        }
    }
	public static String getMessage(String property, Object... args)
    {
        String message;

        try
        {
            message = (String) ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, Locale.getDefault()).getObject(property);
        }
        catch (Exception e)
        {
            message = "Exception looking up message from bundle " + MESSAGE_BUNDLE_NAME;
            System.err.print(message);
            e.printStackTrace();
            return message;
        }

        try
        {
            // TODO: This is no longer working with more than one arg in the message string, e.g., {1}
            return args == null ? message : MessageFormat.format(message, args);
        }
        catch (IllegalArgumentException e)
        {
            message = "Message arguments do not match format string: " + property;
            System.err.print(message);
            e.printStackTrace();
            return message;
        }
    }
	public static String getMessage(String property, String arg)
    {
		return arg != null ? getMessage(property, (Object) arg) : getMessage(property);
    }
	
	
	/**
     * Converts a specified string to an integer value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return integer value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Integer convertStringToInteger(String s)
    {
        if (s == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            return Integer.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", s);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a specified string to a floating point value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return floating point value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Double convertStringToDouble(String s)
    {
        if (s == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", s);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a specified string to a long integer value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return long integer value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Long convertStringToLong(String s)
    {
        if (s == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            return Long.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", s);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a specified string to a boolean value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return boolean value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Boolean convertStringToBoolean(String s)
    {
        if (s == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        try
        {
            s = s.trim();

            if (s.length() == 0)
                return null;

            if (s.length() == 1)
                return convertNumericStringToBoolean(s);

            return Boolean.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", s);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a specified string to a boolean value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return boolean value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Boolean convertNumericStringToBoolean(String s)
    {
        if (s == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            Integer i = makeInteger(s);
            return i != null && i != 0;
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", s);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a string to an integer value if the string can be parsed as a integer. Does not log a message if the
     * string can not be parsed as an integer.
     *
     * @param s the string to parse.
     *
     * @return the integer value parsed from the string, or null if the string cannot be parsed as an integer.
     */
    public static Integer makeInteger(String s)
    {
        if (MidsUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a long value if the string can be parsed as a long. Does not log a message if the string can
     * not be parsed as a long.
     *
     * @param s the string to parse.
     *
     * @return the long value parsed from the string, or null if the string cannot be parsed as a long.
     */
    public static Long makeLong(String s)
    {
        if (MidsUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Long.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a double value if the string can be parsed as a double. Does not log a message if the string
     * can not be parsed as a double.
     *
     * @param s the string to parse.
     *
     * @return the double value parsed from the string, or null if the string cannot be parsed as a double.
     */
    public static Double makeDouble(String s)
    {
        if (MidsUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Returns a sub sequence of the specified {@link CharSequence}, with leading and trailing whitespace omitted. If
     * the CharSequence has length zero, this returns a reference to the CharSequence. If the CharSequence represents
     * and empty character sequence, this returns an empty CharSequence.
     *
     * @param charSequence the CharSequence to trim.
     *
     * @return a sub sequence with leading and trailing whitespace omitted.
     *
     * @throws IllegalArgumentException if the charSequence is null.
     */
    public static CharSequence trimCharSequence(CharSequence charSequence)
    {
        if (charSequence == null)
        {
            String message = getMessage("nullValue.CharSequenceIsNull");
            throw new IllegalArgumentException(message);
        }

        int len = charSequence.length();
        if (len == 0)
        {
            return charSequence;
        }

        int start, end;

        for (start = 0; (start < len) && charSequence.charAt(start) == ' '; start++)
        {
        }

        for (end = charSequence.length() - 1; (end > start) && charSequence.charAt(end) == ' '; end--)
        {
        }

        return charSequence.subSequence(start, end + 1);
    }


    /**
     * Generates a random {@link Color} by scaling each of the red, green and blue components of a specified color with
     * independent random numbers. The alpha component is not scaled and is copied to the new color. The returned color
     * can be any value between white (0x000000aa) and black (0xffffffaa).
     * <p/>
     * Unless there's a reason to use a specific input color, the best color to use is white.
     *
     * @param color the color to generate a random color from. If null, the color white (0x000000aa) is used.
     *
     * @return a new color with random red, green and blue components.
     */
    public static Color makeRandomColor(Color color)
    {
        if (color == null)
        {
            color = Color.WHITE;
        }

        float[] cc = color.getRGBComponents(null);

        return new Color(cc[0] * (float) Math.random(), cc[1] * (float) Math.random(), cc[2] * (float) Math.random(),
            cc[3]);
    }

    /**
     * Generates a random {@link Color} by scaling each of the red, green and blue components of a specified color with
     * independent random numbers. The alpha component is not scaled and is copied to the new color. The returned color
     * can be any value between white (0x000000aa) and a specified darkest color.
     * <p/>
     * Unless there's a reason to use a specific input color, the best color to use is white.
     *
     * @param color        the color to generate a random color from. If null, the color white (0x000000aa) is used.
     * @param darkestColor the darkest color allowed. If any of the generated color's components are less than the
     *                     corresponding component in this color, new colors are generated until one satisfies this
     *                     requirement, up to the specified maximum number of attempts.
     * @param maxAttempts  the maximum number of attempts to create a color lighter than the specified darkestColor. If
     *                     this limit is reached, the last color generated is returned.
     *
     * @return a new color with random red, green and blue components.
     */
    public static Color makeRandomColor(Color color, Color darkestColor, int maxAttempts)
    {
        Color randomColor = makeRandomColor(color);

        if (darkestColor == null)
        {
            return randomColor;
        }

        float[] dc = darkestColor.getRGBComponents(null);

        float[] rc = randomColor.getRGBComponents(null);
        for (int i = 0; i < (maxAttempts - 1) && (rc[0] < dc[0] || rc[1] < dc[1] || rc[2] < dc[2]); i++)
        {
            rc = randomColor.getRGBComponents(null);
        }

        return randomColor;
    }

    public static Color makeColorBrighter(Color color)
    {
        if (color == null)
        {
            String message = getMessage("nullValue.ColorIsNull");
            throw new IllegalArgumentException(message);
        }

        float[] hsbComponents = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
        float hue = hsbComponents[0];
        float saturation = hsbComponents[1];
        float brightness = hsbComponents[2];

        saturation /= 3f;
        brightness *= 3f;

        if (saturation < 0f)
        {
            saturation = 0f;
        }

        if (brightness > 1f)
        {
            brightness = 1f;
        }

        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);

        return new Color(rgbInt);
    }

    public static Color makeColorDarker(Color color)
    {
        if (color == null)
        {
            String message = getMessage("nullValue.ColorIsNull");
            throw new IllegalArgumentException(message);
        }

        float[] hsbComponents = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
        float hue = hsbComponents[0];
        float saturation = hsbComponents[1];
        float brightness = hsbComponents[2];

        saturation *= 3f;
        brightness /= 3f;

        if (saturation > 1f)
        {
            saturation = 1f;
        }

        if (brightness < 0f)
        {
            brightness = 0f;
        }

        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);

        return new Color(rgbInt);
    }

    public static Color computeContrastingColor(Color color)
    {
        if (color == null)
        {
            String message = getMessage("nullValue.ColorIsNull");
            throw new IllegalArgumentException(message);
        }

        float[] compArray = new float[4];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        int colorValue = compArray[2] < 0.5f ? 255 : 0;
        int alphaValue = color.getAlpha();

        return new Color(colorValue, colorValue, colorValue, alphaValue);
    }

    /**
     * Creates a hexadecimal string representation of a {@link Color} in the form 0xrrggbbaa.
     *
     * @param color Color to encode.
     *
     * @return String encoding of the specified color.
     *
     * @throws IllegalArgumentException If the specified color is null.
     * @see #decodeColorRGBA(String)
     * @see #encodeColorABGR(java.awt.Color)
     */
    public static String encodeColorRGBA(java.awt.Color color)
    {
        if (color == null)
        {
            String message = getMessage("nullValue.ColorIsNull");
            throw new IllegalArgumentException(message);
        }

        // Encode the red, green, blue, and alpha components
        int rgba = (color.getRed() & 0xFF) << 24
            | (color.getGreen() & 0xFF) << 16
            | (color.getBlue() & 0xFF) << 8
            | (color.getAlpha() & 0xFF);
        return String.format("%#08X", rgba);
    }

    /**
     * Creates a hexadecimal string representation of a {@link Color} in the form 0xaabbggrr.
     *
     * @param color Color to encode.
     *
     * @return String encoding of the specified color.
     *
     * @throws IllegalArgumentException If the specified color is null.
     * @see #decodeColorABGR(String)
     * @see #encodeColorRGBA(java.awt.Color)
     */
    public static String encodeColorABGR(java.awt.Color color)
    {
        if (color == null)
        {
            String message = getMessage("nullValue.ColorIsNull");
            throw new IllegalArgumentException(message);
        }

        // Encode the red, green, blue, and alpha components
        int rgba = (color.getRed() & 0xFF)
            | (color.getGreen() & 0xFF) << 8
            | (color.getBlue() & 0xFF) << 16
            | (color.getAlpha() & 0xFF) << 24;
        return String.format("%#08X", rgba);
    }

    /**
     * Decodes a hexadecimal string in the form <i>rrggbbaa</i>, <i>rrggbbaa</i> or <i>#rrggbbaa</i> to a color.
     *
     * @param encodedString String to decode.
     *
     * @return the decoded color, or null if the string cannot be decoded.
     *
     * @throws IllegalArgumentException If the specified string is null.
     * @see #decodeColorABGR(String) (String)
     * @see #encodeColorRGBA(java.awt.Color)
     */
    public static java.awt.Color decodeColorRGBA(String encodedString)
    {
        if (encodedString == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        if (encodedString.startsWith("#"))
        {
            encodedString = encodedString.replaceFirst("#", "0x");
        }
        else if (!encodedString.startsWith("0x") && !encodedString.startsWith("0X"))
        {
            encodedString = "0x" + encodedString;
        }

        // The hexadecimal representation for an RGBA color can result in a value larger than
        // Integer.MAX_VALUE (for example, 0XFFFF). Therefore we decode the string as a long,
        // then keep only the lower four bytes.
        Long longValue;
        try
        {
            longValue = Long.parseLong(encodedString.substring(2), 16);
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", encodedString);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }

        int i = (int) (longValue & 0xFFFFFFFFL);
        return new java.awt.Color(
            (i >> 24) & 0xFF,
            (i >> 16) & 0xFF,
            (i >> 8) & 0xFF,
            i & 0xFF);
    }

    /**
     * Decodes a hexadecimal string in the form <i>aabbggrr</i>, <i>0xaabbggrr</i> or <i>#aabbggrr</i> to a color.
     *
     * @param encodedString String to decode.
     *
     * @return the decoded color, or null if the string cannot be decoded.
     *
     * @throws IllegalArgumentException If the specified string is null.
     * @see #decodeColorRGBA(String)
     * @see #encodeColorABGR(java.awt.Color)
     */
    public static java.awt.Color decodeColorABGR(String encodedString)
    {
        if (encodedString == null)
        {
            String message = getMessage("nullValue.StringIsNull");
            throw new IllegalArgumentException(message);
        }

        if (encodedString.startsWith("#"))
        {
            encodedString = encodedString.replaceFirst("#", "0x");
        }
        else if (!encodedString.startsWith("0x") && !encodedString.startsWith("0X"))
        {
            encodedString = "0x" + encodedString;
        }

        // The hexadecimal representation for an RGBA color can result in a value larger than
        // Integer.MAX_VALUE (for example, 0XFFFF). Therefore we decode the string as a long,
        // then keep only the lower four bytes.
        Long longValue;
        try
        {
            longValue = Long.parseLong(encodedString.substring(2), 16);
        }
        catch (NumberFormatException e)
        {
            String message = getMessage("generic.ConversionError", encodedString);
            System.err.print(message);
            e.printStackTrace();
            return null;
        }

        int i = (int) (longValue & 0xFFFFFFFFL);
        return new java.awt.Color(
            i & 0xFF,
            (i >> 8) & 0xFF,
            (i >> 16) & 0xFF,
            (i >> 24) & 0xFF);
    }

    /**
     * Determine whether an object reference is null or a reference to an empty string.
     *
     * @param s the reference to examine.
     *
     * @return true if the reference is null or is a zero-length {@link String}.
     */
    public static boolean isEmpty(Object s)
    {
        return s == null || (s instanceof String && ((String) s).length() == 0);
    }

    /**
     * Determine whether an {@link List} is null or empty.
     *
     * @param list the list to examine.
     *
     * @return true if the list is null or zero-length.
     */
    public static boolean isEmpty(java.util.List<?> list)
    {
        return list == null || list.size() == 0;
    }

    /**
     * Creates a two-element array of default min and max values, typically used to initialize extreme values searches.
     *
     * @return a two-element array of extreme values. Entry 0 is the maximum double value; entry 1 is the negative of
     *         the maximum double value;
     */
    public static double[] defaultMinMix()
    {
        return new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
    }

    /**
     * Uses reflection to invoke a <i>set</i> method for a specified property. The specified class must have a method
     * named "set" + propertyName, with either a single <code>String</code> argument, a single <code>double</code>
     * argument, a single <code>int</code> argument or a single <code>long</code> argument. If it does, the method is
     * called with the specified property value argument.
     *
     * @param parent        the object on which to set the property.
     * @param propertyName  the name of the property.
     * @param propertyValue the value to give the property. Specify double, int and long values in a
     *                      <code>String</code>.
     *
     * @return the return value of the <i>set</i> method, or null if the method has no return value.
     *
     * @throws IllegalArgumentException  if the parent object or the property name is null.
     * @throws NoSuchMethodException     if no <i>set</i> method exists for the property name.
     * @throws InvocationTargetException if the <i>set</i> method throws an exception.
     * @throws IllegalAccessException    if the <i>set</i> method is inaccessible due to access control.
     */
    public static Object invokePropertyMethod(Object parent, String propertyName, String propertyValue)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        if (parent == null)
        {
            String message = getMessage("nullValue.nullValue.ParentIsNull");
            throw new IllegalArgumentException(message);
        }

        if (propertyName == null)
        {
            String message = getMessage("nullValue.PropertyNameIsNull");
            throw new IllegalArgumentException(message);
        }

        String methodName = "set" + propertyName;

        try // String arg
        {
            Method method = parent.getClass().getMethod(methodName, new Class[] {String.class});
            return method != null ? method.invoke(parent, propertyValue) : null;
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // double arg
        {
            Double d = MidsUtil.makeDouble(propertyValue);
            if (d != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {double.class});
                return method != null ? method.invoke(parent, d) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // int arg
        {
            Integer i = MidsUtil.makeInteger(propertyValue);
            if (i != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {int.class});
                return method != null ? method.invoke(parent, i) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // boolean arg
        {
            Boolean b = MidsUtil.convertStringToBoolean(propertyValue);
            if (b != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {boolean.class});
                return method != null ? method.invoke(parent, b) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // long arg
        {
            Long l = MidsUtil.makeLong(propertyValue);
            if (l != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {long.class});
                return method != null ? method.invoke(parent, l) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        throw new NoSuchMethodException();
    }

    /**
     * Eliminates all white space in a specified string. (Applies the regular expression "\\s+".)
     *
     * @param inputString the string to remove white space from.
     *
     * @return the string with white space eliminated, or null if the input string is null.
     */
    public static String removeWhiteSpace(String inputString)
    {
        if (MidsUtil.isEmpty(inputString))
        {
            return inputString;
        }

        return inputString.replaceAll("\\s+", "");
    }

    /**
     * Extracts an error message from the exception object
     *
     * @param t Exception instance
     *
     * @return A string that contains an error message
     */
    public static String extractExceptionReason(Throwable t)
    {
        if (t == null)
        {
            return getMessage("generic.Unknown");
        }

        StringBuffer sb = new StringBuffer();

        String message = t.getMessage();
        if (!MidsUtil.isEmpty(message))
            sb.append(message);

        String messageClass = t.getClass().getName();

        Throwable cause = t.getCause();
        if (null != cause && cause != t)
        {
            String causeMessage = cause.getMessage();
            String causeClass = cause.getClass().getName();

            if (!MidsUtil.isEmpty(messageClass) && !MidsUtil.isEmpty(causeClass) && !messageClass.equals(causeClass))
            {
                if (sb.length() != 0)
                {
                    sb.append(" : ");
                }
                sb.append(causeClass).append(" (").append(causeMessage).append(")");
            }
        }

        if (sb.length() == 0)
        {
            sb.append(messageClass);
        }

        return sb.toString();
    }

    /**
     * Strips leading period from a string (Example: input -> ".ext", output -> "ext")
     *
     * @param s String to test, must not be null
     *
     * @return String without leading period
     */
    public static String stripLeadingPeriod(String s)
    {
        if (null != s && s.startsWith("."))
            return s.substring(Math.min(1, s.length()), s.length());
        return s;
    }
}
