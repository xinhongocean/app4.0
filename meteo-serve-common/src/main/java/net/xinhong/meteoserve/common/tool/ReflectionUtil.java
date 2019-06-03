package net.xinhong.meteoserve.common.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射处理
 * Created by yangwenpeng on 2015/9/7.
 */
public abstract class ReflectionUtil {

    private static final Log logger = LogFactory.getLog(ReflectionUtil.class);

    /**
     * @param target
     * @param fname
     * @param ftype
     * @param fvalue
     */
    public static void setFieldValue(Object target, String fname, Class<?> ftype, Object fvalue) {
        setFieldValue(target, target.getClass(), fname, ftype, fvalue);
    }

    /**
     * @param target
     * @param clazz
     * @param fname
     * @param ftype
     * @param fvalue
     */
    public static void setFieldValue(Object target, Class<?> clazz, String fname, Class<?> ftype, Object fvalue) {
        if (target == null || fname == null || "".equals(fname) || (fvalue != null && !ftype.isAssignableFrom(fvalue.getClass()))) {
            return;
        }

        try {
            Method method = clazz.getDeclaredMethod("set" + Character.toUpperCase(fname.charAt(0)) + fname.substring(1), ftype);
            // if (!Modifier.isPublic(method.getModifiers())) {
            method.setAccessible(true);
            // }
            method.invoke(target, fvalue);

        } catch (Exception me) {
            if (logger.isDebugEnabled()) {
                logger.debug(me);
            }

            try {
                Field field = clazz.getDeclaredField(fname);
                // if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
                // }
                field.set(target, fvalue);
            } catch (Exception fe) {
                if (logger.isDebugEnabled()) {
                    logger.debug(fe);
                }
            }
        }
    }

    /**
     * @param target
     * @param fname
     * @return
     */
    public static Object getFieldValue(Object target, String fname) {
        return getFieldValue(target, target.getClass(), fname);
    }

    /**
     * [添加说明]
     * @param target
     * @param clazz
     * @param fname
     * @return
     */
    public static Object getFieldValue(Object target, Class<?> clazz, String fname) {
        if (target == null || fname == null || "".equals(fname)) {
            return null;
        }

        boolean exCatched = false;
        try {
            String methodname = "get" + org.apache.commons.lang.StringUtils.capitalize(fname);
            Field field = clazz.getDeclaredField(fname);
            if(field.getClass().equals(boolean.class)){
                methodname = "is" + org.apache.commons.lang.StringUtils.capitalize(fname);
            }
            Method method = clazz.getDeclaredMethod(methodname);

            // if (!Modifier.isPublic(method.getModifiers())) {
            method.setAccessible(true);
            // }
            return method.invoke(target);
        } catch (NoSuchMethodException e) {
            exCatched = true;
        } catch (InvocationTargetException e) {
            exCatched = true;
        } catch (IllegalAccessException e) {
            exCatched = true;
        } catch (SecurityException e) {
            exCatched = true;
        } catch (NoSuchFieldException e) {
            exCatched = true;
        }

        if (exCatched) {
            try {
                Field field = clazz.getDeclaredField(fname);
                // if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
                // }
                return field.get(target);
            } catch (Exception fe) {
                if (logger.isDebugEnabled()) {
                    logger.debug(fe);
                }
            }
        }
        return null;
    }

    /**
     * @param clazz
     * @return
     */
    public static List<String> getDeclareFieldNameList(Class<?> clazz) {
        if (clazz == null)
            return null;
        List<String> list = null;
        try {
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null && fields.length > 0) {
                list = new ArrayList<String>();
                for (Field field : fields) {
                    list.add(field.getName());
                }
            }
        } catch (SecurityException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        }
        return list;
    }

    /**
     * @param clazz
     * @return
     */
    public static Field[] getDeclareFieldList(Class<?> clazz) {
        if (clazz == null)
            return null;
        Field[] fields = clazz.getDeclaredFields();
        return fields;
    }
}
