package localhost.lva.amqprpc;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author vlitvinenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcClientRegistrar.class)
public @interface EnableRpcClients {
    Class<?>[] clients() default {};
}
