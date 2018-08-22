package util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionTestUtil {

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object object, String methodName, Object... arguments)
        throws Exception {
        synchronized (ReflectionTestUtil.class) {
            Method method =
                Arrays.stream(object.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(methodName)
                        && parameterSetMatches(m.getParameterTypes(), arguments))
                    .findFirst().orElseThrow(IllegalArgumentException::new);

            method.setAccessible(true);

            return (T) method.invoke(object, arguments);
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean parameterSetMatches(Class[] parameters, Object... arguments) {
        if (parameters == null || arguments == null || arguments.length != parameters.length) {
            return false;
        }

        for (int i = 0; i < parameters.length; i++) {
            if (arguments[i] != null && !parameters[i].isAssignableFrom(arguments[i].getClass())) {
                return false;
            }
        }

        return true;
    }
}
