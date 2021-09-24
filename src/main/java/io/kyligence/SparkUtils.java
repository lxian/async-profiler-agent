package io.kyligence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SparkUtils {
    public static String appId = invokeMethodOnSparkEnv("get.conf.getAppId");
    public static String executorId = invokeMethodOnSparkEnv("get.executorId");

    public static String getAppId() {
        return invokeMethodOnSparkEnv("get.conf.getAppId");
    }

    public static String getExecutorId() {
        return invokeMethodOnSparkEnv("get.executorId");
    }

    public static String invokeMethodOnSparkEnv(String method) {
        try {
            Object result = executeStaticMethods("org.apache.spark.SparkEnv", method);
            if (result == null) {
                return null;
            }
            return result.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    public static Object executeStaticMethods(String className, String methods) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] methodArray = methods.split("\\.");
        Class clazz = Class.forName(className);
        Object clazzObject = null;
        Object result = null;
        for (String entry : methodArray) {
            Method method = clazz.getMethod(entry);
            if (method == null) {
                return null;
            }
            result = method.invoke(clazzObject);
            if (result == null) {
                return null;
            }

            clazz = result.getClass();
            clazzObject = result;
        }
        return result;
    }
}
