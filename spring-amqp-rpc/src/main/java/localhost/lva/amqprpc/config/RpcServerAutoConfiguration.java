package localhost.lva.amqprpc.config;

import localhost.lva.amqprpc.RpcServerConfigurer;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
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
@ConditionalOnBean(RpcServerMarkerConfiguration.Marker.class)
public class RpcServerAutoConfiguration {
    public static final String RPC_EXCHANGE_BEAN_NAME = "rpcExchange";
    public static final String RPC_TEMPLATE_BEAN_NAME = "rpcTemplate";

    @Bean
    @ConditionalOnMissingBean(name = RPC_EXCHANGE_BEAN_NAME)
    DirectExchange rpcExchange() {
        return new DirectExchange("rpc");
    }

    @Bean
    @ConditionalOnMissingBean(name = RPC_TEMPLATE_BEAN_NAME)
    AmqpTemplate rpcTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }


    @Bean
    public RpcServerConfigurer rpcServerConfigurer() {
        return new RpcServerConfigurer();
    }
}
