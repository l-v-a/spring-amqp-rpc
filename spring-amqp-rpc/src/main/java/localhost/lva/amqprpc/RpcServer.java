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
    String routingKey() default "";

//    Class<?> forInterface() default void.class; // TODO: redundant if used for interface only
}
