package lavalink.server.metrics;

import javax.annotation.Nullable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by napster on 06.05.19.
 */
public class CpuTimer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CpuTimer.class);

    static final double ERROR = -1.0;

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    public double getProcessRecentCpuUsage() {
        try {
            //between 0.0 and 1.0, 1.0 meaning all CPU cores were running threads of this JVM
            // see com.sun.management.OperatingSystemMXBean#getProcessCpuLoad and https://www.ibm.com/support/knowledgecenter/en/SSYKE2_7.1.0/com.ibm.java.api.71.doc/com.ibm.lang.management/com/ibm/lang/management/OperatingSystemMXBean.html#getProcessCpuLoad()
            Double processCpuTime = callDoubleGetter("getProcessCpuLoad", this.osBean);

            return processCpuTime != null ? processCpuTime : ERROR;
        } catch (Exception e) {
            log.debug("Could not access process cpu time", e);
            return ERROR;
        }
    }

    public double getSystemRecentCpuUsage() {
        try {
            //between 0.0 and 1.0, 1.0 meaning all CPU cores were busy
            // see com.sun.management.OperatingSystemMXBean#getSystemCpuLoad and https://www.ibm.com/support/knowledgecenter/en/SSYKE2_7.1.0/com.ibm.java.api.71.doc/com.ibm.lang.management/com/ibm/lang/management/OperatingSystemMXBean.html#getSystemCpuLoad()
            Double processCpuTime = callDoubleGetter("getSystemCpuLoad", this.osBean);

            return processCpuTime != null ? processCpuTime : ERROR;
        } catch (Exception e) {
            log.debug("Could not access system cpu time", e);
            return ERROR;
        }
    }

    // Code below copied from Prometheus's StandardExports (Apache 2.0) with slight modifications

    @Nullable
    private static Double callDoubleGetter(String getterName, Object obj)
            throws NoSuchMethodException, InvocationTargetException {
        return callDoubleGetter(obj.getClass().getMethod(getterName), obj);
    }

    /**
     * Attempts to call a method either directly or via one of the implemented interfaces.
     * <p>
     * A Method object refers to a specific method declared in a specific class. The first invocation
     * might happen with method == SomeConcreteClass.publicLongGetter() and will fail if
     * SomeConcreteClass is not public. We then recurse over all interfaces implemented by
     * SomeConcreteClass (or extended by those interfaces and so on) until we eventually invoke
     * callMethod() with method == SomePublicInterface.publicLongGetter(), which will then succeed.
     * <p>
     * There is a built-in assumption that the method will never return null (or, equivalently, that
     * it returns the primitive data type, i.e. {@code long} rather than {@code Long}). If this
     * assumption doesn't hold, the method might be called repeatedly and the returned value will be
     * the one produced by the last call.
     */
    @Nullable
    private static Double callDoubleGetter(Method method, Object obj) throws InvocationTargetException {
        try {
            return (Double) method.invoke(obj);
        } catch (IllegalAccessException e) {
            // Expected, the declaring class or interface might not be public.
        }

        // Iterate over all implemented/extended interfaces and attempt invoking the method with the
        // same name and parameters on each.
        for (Class<?> clazz : method.getDeclaringClass().getInterfaces()) {
            try {
                Method interfaceMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
                Double result = callDoubleGetter(interfaceMethod, obj);
                if (result != null) {
                    return result;
                }
            } catch (NoSuchMethodException e) {
                // Expected, class might implement multiple, unrelated interfaces.
            }
        }

        return null;
    }
}
