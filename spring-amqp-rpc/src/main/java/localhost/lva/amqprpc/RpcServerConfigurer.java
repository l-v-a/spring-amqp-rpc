package localhost.lva.amqprpc;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                Map<String, Object> attrs = AnnotationUtils.getAnnotationAttributes(rpcServer);
                // Assert.isTrue(serverInterface.isInterface(), "@RpcServer  must be applied to interfaced only");
                registerServer((BeanDefinitionRegistry) beanFactory, serverInterface, beanName);
            }
        }
    }


    private void registerServer(BeanDefinitionRegistry registry,
                                Class<?> serverInterface, String serverBeanName) {

        String suffix = ClassUtils.getShortName(serverInterface); // TODO: support package name
        String exchangeBeanName = "exchange"; // TODO: declare

        // queue
        // TODO: read params from config
        String queueBeanName = String.format("queue_%s", suffix);
        BeanDefinitionBuilder builder = builder = BeanDefinitionBuilder.genericBeanDefinition(Queue.class,
                () -> new Queue(serverInterface.getSimpleName(), true, false, false));
        registry.registerBeanDefinition(queueBeanName, builder.getBeanDefinition());

        // binding
        String bindingBeanName = String.format("binding_%s", suffix);
        builder = BeanDefinitionBuilder.genericBeanDefinition(Binding.class);
        builder.setFactoryMethodOnBean("newBinding", "rpcServerConfigurer");
        builder.addConstructorArgReference(queueBeanName);
        builder.addConstructorArgReference(exchangeBeanName);
        builder.addConstructorArgValue(serverInterface.getSimpleName());
        registry.registerBeanDefinition(bindingBeanName, builder.getBeanDefinition());

//        // template
//        String templateBeanName = String.format("template_%s", suffix);
//        builder = BeanDefinitionBuilder.genericBeanDefinition(RabbitTemplate.class);
//        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
//        registry.registerBeanDefinition(templateBeanName, builder.getBeanDefinition());

        // exporter
        // TODO: think about if bean with name already exists (e.g. multiple @RpcClient variables of same type)
        String exporterBeanName = String.format("exporter_%s", suffix);
        builder = BeanDefinitionBuilder.genericBeanDefinition(AmqpInvokerServiceExporter.class);
//        builder.addPropertyReference("amqpTemplate", templateBeanName);
        builder.addPropertyValue("serviceInterface", serverInterface);
        builder.addPropertyReference("service", serverBeanName);
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        registry.registerBeanDefinition(exporterBeanName, builder.getBeanDefinition());

        // listener
        String listenerBeanName = String.format("container_%s", suffix);
        builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleMessageListenerContainer.class);
        builder.addPropertyReference("queues", queueBeanName); // only name of queue is needed
        builder.addPropertyReference("messageListener", exporterBeanName);
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        registry.registerBeanDefinition(listenerBeanName, builder.getBeanDefinition());
    }

    private static Set<Class<?>> findAnnotationDeclaringInterfaces(Class<? extends Annotation> annotationType,
                                                                   Class<?> clazz) {

        Set<Class<?>> classes = new HashSet<>();
        if (clazz.isInterface() && AnnotationUtils.isAnnotationDeclaredLocally(annotationType, clazz)) {
            classes.add(clazz);
        }
        for (Class<?> itf : clazz.getInterfaces()) {
            classes.addAll(findAnnotationDeclaringInterfaces(annotationType, itf));
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
