package localhost.lva.amqprpc.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author vlitvinenko
 */
@Configuration
@ConditionalOnBean(RpcClientMarkerConfiguration.Marker.class)
public class RpcClientAutoConfiguration {
    public static final String RPC_CLIENT_TEMPLATE_BEAN_NAME = "rpcClientTemplate";

    @Bean
    @ConditionalOnMissingBean(name= RPC_CLIENT_TEMPLATE_BEAN_NAME)
    RabbitTemplate rpcClientTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setExchange("rpc");
        return template;
    }
}
