package localhost.lva.amqprpc.config;

import localhost.lva.amqprpc.RpcServerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author vlitvinenko
 */
@Configuration
@ConditionalOnBean(RpcServerMarkerConfiguration.Marker.class)
public class RpcServerAutoConfiguration {
    @Bean
    public RpcServerConfigurer rpcServerConfigurer() {
        return new RpcServerConfigurer();
    }
}
