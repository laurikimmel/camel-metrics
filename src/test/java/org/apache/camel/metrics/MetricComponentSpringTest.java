package org.apache.camel.metrics;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = { MetricComponentSpringTest.TestConfig.class },
        loader = CamelSpringDelegatingTestContextLoader.class)
@MockEndpoints
public class MetricComponentSpringTest {

    @EndpointInject(uri = "mock:out")
    private MockEndpoint endpoint;

    @Produce(uri = "direct:in")
    private ProducerTemplate producer;

    @Configuration
    public static class TestConfig extends SingleRouteCamelConfiguration {

        @Bean
        @Override
        public RouteBuilder route() {
            return new RouteBuilder() {

                @Override
                public void configure() throws Exception {
                    from("direct:in")
                            .to("metrics:counter:A?increment=512")
                            .to("mock:out");
                }
            };
        }

        @Bean(name = MetricsComponent.METRIC_REGISTRY_NAME)
        public MetricRegistry getMetricRegistry() {
            return Mockito.mock(MetricRegistry.class);
        }
    }

    @Test
    public void testMetricsRegistryFromCamelRegistry() throws Exception {
        // TODO - 12.05.2014, Lauri - is there any better way to set this up?
        MetricRegistry mockRegistry = endpoint.getCamelContext().getRegistry().lookupByNameAndType(MetricsComponent.METRIC_REGISTRY_NAME, MetricRegistry.class);
        Counter mockCounter = Mockito.mock(Counter.class);
        InOrder inOrder = Mockito.inOrder(mockRegistry, mockCounter);
        when(mockRegistry.counter("A")).thenReturn(mockCounter);

        endpoint.expectedMessageCount(1);
        producer.sendBody(new Object());
        endpoint.assertIsSatisfied();
        inOrder.verify(mockRegistry, times(1)).counter("A");
        inOrder.verify(mockCounter, times(1)).inc(512L);
        inOrder.verifyNoMoreInteractions();
    }
}
