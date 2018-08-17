package localhost.lva.amqprpc;

import localhost.lva.amqprpc.config.RpcClientMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author vlitvinenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RpcClientMarkerConfiguration.class, RpcClientRegistrar.class})
public @interface EnableRpcClients {
    Class<?>[] clients() default {};
}
