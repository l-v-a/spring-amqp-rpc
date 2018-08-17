package localhost.lva.amqprpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author vlitvinenko
 */
@Configuration
public class RpcServerMarkerConfiguration {
    @Bean
    public Marker rpcServerMarker() {
        return new Marker();
    }

    static class Marker {
    }
}
