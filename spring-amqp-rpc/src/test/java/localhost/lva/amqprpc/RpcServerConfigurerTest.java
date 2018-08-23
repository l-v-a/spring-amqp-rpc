package localhost.lva.amqprpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author vlitvinenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RpcServerConfigurerTest.Application.class)
@DirtiesContext
public class RpcServerConfigurerTest {

    @RpcServer
    interface Service {}

    @Configuration
    @EnableAutoConfiguration
    @EnableRpcServer
    static class Application {
        @Bean
        Service service() {return new Service() {};}
    }

    @Autowired
    private ApplicationContext context;

    @Test
    public void should_configure_listener() {
        SimpleMessageListenerContainer container = context.getBean(SimpleMessageListenerContainer.class);
        assertSame(context.getBean(AmqpInvokerServiceExporter.class), container.getMessageListener());
    }

    @Test
    public void should_configure_exporter() {
        AmqpInvokerServiceExporter exporter = context.getBean(AmqpInvokerServiceExporter.class);
        assertEquals(Service.class, exporter.getServiceInterface());
        assertSame(context.getBean(Service.class), exporter.getService());
    }
}