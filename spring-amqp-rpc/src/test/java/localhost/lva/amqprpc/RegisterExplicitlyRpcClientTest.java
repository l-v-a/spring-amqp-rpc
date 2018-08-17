package localhost.lva.amqprpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;



/**
 * @author vlitvinenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RegisterExplicitlyRpcClientTest.Application.class)
@DirtiesContext
public class RegisterExplicitlyRpcClientTest {
    @RpcClient
    interface AnnotatedService {}

    interface SimpleService {}

    @Configuration
    @EnableAutoConfiguration
    @EnableRpcClients(clients = {AnnotatedService.class, SimpleService.class})
    static class Application {

    }

    @Autowired
    private ApplicationContext context;


    @Test
    public void should_register_annotated_interface_explicitly() throws Exception {
        Map<String, AnnotatedService> beans = context.getBeansOfType(AnnotatedService.class);
        assertEquals(1, beans.size());
    }

    @Test
    public void should_skip_not_annotated_explicit_clients() throws Exception {
        Map<String, SimpleService> beans = context.getBeansOfType(SimpleService .class);
        assertEquals(0, beans.size());
    }
}
