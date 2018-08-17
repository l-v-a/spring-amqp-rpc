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


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RegisterRpcClientTest.Application.class)
@DirtiesContext
public class RegisterRpcClientTest {
    @RpcClient
    interface Service {}

    interface SimpleService {}

    @Configuration
    @EnableAutoConfiguration
    @EnableRpcClients
    static class Application {

    }

    @Autowired
    private ApplicationContext context;

    @Test
    public void should_register_annotated_interfaces() throws Exception {
        Map<String, Service> beans = context.getBeansOfType(Service.class);
        assertEquals(1, beans.size());
    }

    @Test
    public void should_skip_not_annotated_interfaces() throws Exception {
        Map<String, SimpleService> beans = context.getBeansOfType(SimpleService.class);
        assertEquals(0, beans.size());
    }

}
