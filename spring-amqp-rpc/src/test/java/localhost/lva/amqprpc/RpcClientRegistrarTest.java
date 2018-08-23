package localhost.lva.amqprpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ClassUtils;

import static localhost.lva.amqprpc.RpcClientRegistrar.formatProxyBeanName;
import static org.junit.Assert.assertEquals;

/**
 * @author vlitvinenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RpcClientRegistrarTest.Application.class)
@DirtiesContext
public class RpcClientRegistrarTest {

    @RpcClient(routingKey="someKey")
    interface Service {}

    @RpcClient
    interface ServiceDefault {}

    @Configuration
    @EnableAutoConfiguration
    @EnableRpcClients
    static class Application {

    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DefaultListableBeanFactory beanFactory;


    @Test
    public void should_configure_routing_key() {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(formatProxyBeanName(Service.class.getName()));
        assertEquals("someKey", beanDefinition.getPropertyValues().get("routingKey"));
    }

    @Test
    public void should_configure_default_routing_key() {
        String clientClassName = ServiceDefault.class.getName();
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(formatProxyBeanName(clientClassName));
        assertEquals(ClassUtils.getShortName(clientClassName), beanDefinition.getPropertyValues().get("routingKey"));
    }
}