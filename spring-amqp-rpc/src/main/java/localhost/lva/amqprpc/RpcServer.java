package localhost.lva.amqprpc;

import java.lang.annotation.*;

/**
 * @author vlitvinenko
 */
// TODO: check auto-scan, multiple interfaces (avoid conflicts)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RpcServer {
    // TODO: auto-generate from class name if absent (will be done after forInterface auto-detection)
    String queueName() default ""; // same as routing key for client

    Class<?> forInterface() default void.class; // TODO: redundant if used for interface only
}
