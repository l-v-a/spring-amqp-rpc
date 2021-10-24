package localhost.lva.amqprpc;

import org.springframework.util.ClassUtils;

/**
 * @author vlitvinenko
 */
// TODO: remove
class Formatter {
    private Formatter() {}

    static String formatProxyBeanName(String className) {
        return String.format("%sProxy", ClassUtils.getShortName(className));
    }

    static String formatRoutingKey(String className) {
        return ClassUtils.getShortName(className);
    }

    static String formatBeanName(String name, Class<?> clazz) {
        return String.format("%s_%s", name, ClassUtils.getShortName(clazz)); // TODO: support package name
    }
}
