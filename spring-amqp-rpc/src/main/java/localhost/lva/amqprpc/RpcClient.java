package localhost.lva.amqprpc;

import java.lang.annotation.*;


@Target(ElementType.TYPE)  // TODO: think about field
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcClient {
    String exchange() default "";

    String routingKey() default ""; // TODO: auto-generate when absent

    int timeout() default -1; //  TODO: think about to use NONE(0) as default
}
