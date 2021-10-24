package localhost.lva.amqprpc;

import org.springframework.amqp.remoting.client.AmqpProxyFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static localhost.lva.amqprpc.Formatter.formatProxyBeanName;
import static localhost.lva.amqprpc.Formatter.formatRoutingKey;
import static localhost.lva.amqprpc.config.RpcClientAutoConfiguration.RPC_CLIENT_TEMPLATE_BEAN_NAME;

/**
 * @author vlitvinenko
 */
public class RpcClientRegistrar implements ImportBeanDefinitionRegistrar,
        EnvironmentAware, ResourceLoaderAware {

    private static final AnnotationTypeFilter CLIENT_ANNOTATION_TYPE_FILTER =
            new AnnotationTypeFilter(RpcClient.class);
    private static final Class<?>[] CLASSES_EMPTY_ARRAY = new Class<?>[0];
    private Environment environment;
    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata
                .getAnnotationAttributes(EnableRpcClients.class.getName()));

        Class<?>[] clients = attributes != null ? attributes.getClassArray("clients") : CLASSES_EMPTY_ARRAY;
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        Set<String> basePackages = getBasePackages(metadata, clients, scanner);

        Stream<AnnotatedBeanDefinition> beanDefinitions = basePackages.stream()
                .flatMap(basePackage -> scanner.findCandidateComponents(basePackage).stream())
                .filter(AnnotatedBeanDefinition.class::isInstance)
                .map(AnnotatedBeanDefinition.class::cast);

        beanDefinitions.forEach((beanDefinition) -> {
            AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
            Assert.isTrue(annotationMetadata.isInterface(),
                    "@RpcClient can only be specified on an interface"); // TODO: think about

            AnnotationAttributes clientAttrs = AnnotationAttributes.fromMap(
                    annotationMetadata.getAnnotationAttributes(RpcClient.class.getName()));
            Assert.notNull(clientAttrs, "@RpcClient expected");
            registerClient(registry, annotationMetadata.getClassName(), clientAttrs);
        });
    }

    private Set<String> getBasePackages(AnnotationMetadata metadata,
                                        Class<?>[] clients,
                                        ClassPathScanningCandidateComponentProvider scanner) {
        if (clients.length == 0) {
            // auto-scan
            scanner.addIncludeFilter(CLIENT_ANNOTATION_TYPE_FILTER);
            return getBasePackages(metadata);
        }

        // TODO: think about and rewrite (we don't needed this path if we finally interested only in class names)
        Set<String> clientClasses = Arrays.stream(clients)
                .map(Class::getCanonicalName).collect(toSet());
        Set<String> basePackages = Arrays.stream(clients)
                .map(ClassUtils::getPackageName).collect(toSet());

        AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {
                String cleaned = metadata.getClassName().replaceAll("\\$", ".");
                return clientClasses.contains(cleaned);
            }
        };

        scanner.addIncludeFilter(TypeFilters.and(filter, CLIENT_ANNOTATION_TYPE_FILTER));
        return basePackages;
    }

    private static void registerClient(BeanDefinitionRegistry registry, String clientClassName,
                                       AnnotationAttributes clientAttrs) {
        // TODO: think about if bean with name already exists (e.g. multiple @RpcClient on the same getShortName in different packages)
        String routingKey = clientAttrs.getString("routingKey");
        if (StringUtils.isEmpty(routingKey)) {
            routingKey = formatRoutingKey(clientClassName);
        }
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(AmqpProxyFactoryBean.class)
                .addPropertyValue("serviceInterface", clientClassName)
                .addPropertyReference("amqpTemplate", RPC_CLIENT_TEMPLATE_BEAN_NAME)
                .addPropertyValue("routingKey", routingKey)
                .getBeanDefinition();
        registry.registerBeanDefinition(formatProxyBeanName(clientClassName), beanDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private ClassPathScanningCandidateComponentProvider getScanner() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isIndependent() && !beanDefinition.getMetadata().isAnnotation();
            }
        };
        scanner.setResourceLoader(resourceLoader);
        return scanner;
    }

    private Set<String> getBasePackages(AnnotationMetadata metadata) {
// TODO: implement
//        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata
//                .getAnnotationAttributes(EnableRpcClients.class.getName(), true));
//        if (attributes != null) {
//            addPackages(packages, attributes.getStringArray("value"));
//            addPackages(packages, attributes.getStringArray("basePackages"));
//            addClasses(packages, attributes.getStringArray("basePackageClasses"));
//            if (packages.isEmpty()) {
//                packages.add(ClassUtils.getPackageName(metadata.getClassName()));
//            }
//        }
        Set<String> packages = new HashSet<>();
        packages.add(ClassUtils.getPackageName(metadata.getClassName()));
        return packages;
    }


    interface TypeFilters {
        static TypeFilter and(TypeFilter filter1, TypeFilter filter2) {
            return (reader, readerFactory) ->
                filter1.match(reader, readerFactory) && filter2.match(reader, readerFactory);
        }
    }
}
