package localhost.lva.amqprpc;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import static localhost.lva.amqprpc.config.RpcServerAutoConfiguration.RPC_EXCHANGE_BEAN_NAME;
import static localhost.lva.amqprpc.config.RpcServerAutoConfiguration.RPC_TEMPLATE_BEAN_NAME;

/**
 * @author vlitvinenko
 */
public class RpcServerConfigurer implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanNamesForAnnotation(RpcServer.class);
        for (String beanName : beanNames) {
            Class<?> beanClass = beanFactory.getType(beanName);
            if (beanClass != null) {
                Set<Class<?>> candidates = findAnnotationDeclaringInterfaces(RpcServer.class, beanClass);
                Assert.isTrue(candidates.size() == 1, "Only one @RpcServer interface must be supplied in hierarchy");
                Class<?> serverInterface = candidates.iterator().next();

                RpcServer rpcServer = AnnotationUtils.findAnnotation(beanClass, RpcServer.class);
                Assert.notNull(rpcServer, "Interface must be @RpcServer annotated");

                AnnotationAttributes attrs = AnnotationAttributes.fromMap(
                        AnnotationUtils.getAnnotationAttributes(rpcServer));
                registerServer((BeanDefinitionRegistry) beanFactory, serverInterface, beanName, attrs);
            }
        }
    }


    private void registerServer(BeanDefinitionRegistry registry, Class<?> serverInterface,
                                String serverBeanName, AnnotationAttributes attrs) {


        String routingKey = attrs.getString("routingKey");
        if (StringUtils.isEmpty(routingKey)) {
            routingKey = Formatter.formatRoutingKey(serverInterface.getName());
        }

        String queueBeanName = registerQueue(registry, serverInterface);
        registerBinding(registry, serverInterface, queueBeanName, routingKey);
        String exporterBeanName = registerExporter(registry, serverInterface, serverBeanName);
        registerListener(registry, serverInterface, queueBeanName, exporterBeanName);
    }

    private static String registerQueue(BeanDefinitionRegistry registry, Class<?> serverInterface) {
        // TODO: read params from config
        String queueBeanName = Formatter.formatBeanName("queue", serverInterface);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Queue.class,
                () -> new Queue(serverInterface.getSimpleName(), true, false, false));
        registry.registerBeanDefinition(queueBeanName, builder.getBeanDefinition());
        return queueBeanName;
    }

    private static String registerBinding(BeanDefinitionRegistry registry, Class<?> serverInterface,
                                          String queueBeanName, String routingKey) {

        // TODO: think about if bean with name already exists (e.g. multiple @RpcClient variables of same type)
        String bindingBeanName = Formatter.formatBeanName("binding", serverInterface);
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(Binding.class)
                .setFactoryMethodOnBean("newBinding", "rpcServerConfigurer")
                .addConstructorArgReference(queueBeanName)
                .addConstructorArgReference(RPC_EXCHANGE_BEAN_NAME)
                .addConstructorArgValue(routingKey)
                .getBeanDefinition();

        registry.registerBeanDefinition(bindingBeanName, beanDefinition);
        return bindingBeanName;
    }

    private static String registerExporter(BeanDefinitionRegistry registry, Class<?> serverInterface,
                                           String serverBeanName) {

        String exporterBeanName = Formatter.formatBeanName("exporter", serverInterface);
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(AmqpInvokerServiceExporter.class)
                .addPropertyReference("amqpTemplate", RPC_TEMPLATE_BEAN_NAME)
                .addPropertyValue("serviceInterface", serverInterface)
                .addPropertyReference("service", serverBeanName)
                .getBeanDefinition();

        registry.registerBeanDefinition(exporterBeanName, beanDefinition);
        return exporterBeanName;

    }

    private static void registerListener(BeanDefinitionRegistry registry, Class<?> serverInterface, String queueBeanName, String exporterBeanName) {
        String listenerBeanName = Formatter.formatBeanName("container", serverInterface);
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SimpleMessageListenerContainer.class)
                .addPropertyReference("queues", queueBeanName) // only name of queue is needed
                .addPropertyReference("messageListener", exporterBeanName)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                .getBeanDefinition();
        registry.registerBeanDefinition(listenerBeanName, beanDefinition);
    }

    private static Set<Class<?>> findAnnotationDeclaringInterfaces(Class<? extends Annotation> annotationType,
                                                                   Class<?> clazz) {

        Set<Class<?>> classes = new HashSet<>();
        if (clazz.isInterface() && AnnotationUtils.isAnnotationDeclaredLocally(annotationType, clazz)) {
            classes.add(clazz);
        }
        for (Class<?> itfClass : clazz.getInterfaces()) {
            classes.addAll(findAnnotationDeclaringInterfaces(annotationType, itfClass));
        }
        return classes;
    }

    private Binding newBinding(Queue queue, Exchange exchange, String routingKey) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(routingKey)
                .noargs();
    }
}
