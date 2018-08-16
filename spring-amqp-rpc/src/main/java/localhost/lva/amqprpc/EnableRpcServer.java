package localhost.lva.amqprpc;

import localhost.lva.amqprpc.config.RpcServerMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author vlitvinenko
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcServerMarkerConfiguration.class)
public @interface EnableRpcServer {
}
