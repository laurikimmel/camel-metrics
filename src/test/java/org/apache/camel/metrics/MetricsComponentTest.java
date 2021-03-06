package org.apache.camel.metrics;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.metrics.counter.CounterEndpoint;
import org.apache.camel.metrics.histogram.HistogramEndpoint;
import org.apache.camel.metrics.meter.MeterEndpoint;
import org.apache.camel.metrics.timer.TimerEndpoint;
import org.apache.camel.spi.Registry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.MetricRegistry;

@RunWith(MockitoJUnitRunner.class)
public class MetricsComponentTest {

    @Mock
    private CamelContext camelContext;

    @Mock
    private Registry camelRegistry;

    @Mock
    private MetricRegistry metricRegistry;

    private InOrder inOrder;

    private MetricsComponent component;

    @Before
    public void setUp() throws Exception {
        component = new MetricsComponent();
        inOrder = Mockito.inOrder(camelContext, camelRegistry, metricRegistry);

    }

    @Test
    public void testCreateEndpoint() throws Exception {
        component.setCamelContext(camelContext);
        when(camelContext.getRegistry()).thenReturn(camelRegistry);
        when(camelRegistry.lookupByNameAndType(MetricsComponent.METRIC_REGISTRY_NAME, MetricRegistry.class)).thenReturn(metricRegistry);
        Map<String, Object> params = new HashMap<String, Object>();
        Long value = System.currentTimeMillis();
        params.put("mark", value);
        Endpoint result = component.createEndpoint("metrics:meter:long.meter", "meter:long.meter", params);
        assertThat(result, is(notNullValue()));
        assertThat(result, is(instanceOf(MeterEndpoint.class)));
        MeterEndpoint me = (MeterEndpoint) result;
        assertThat(me.getMark(), is(value));
        assertThat(me.getMetricsName(), is("long.meter"));
        assertThat(me.getRegistry(), is(metricRegistry));
        inOrder.verify(camelContext, times(1)).getRegistry();
        inOrder.verify(camelRegistry, times(1)).lookupByNameAndType(MetricsComponent.METRIC_REGISTRY_NAME, MetricRegistry.class);
        inOrder.verify(camelContext, times(1)).getTypeConverter();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCreateEndpoints() throws Exception {
        component.setCamelContext(camelContext);
        when(camelContext.getRegistry()).thenReturn(camelRegistry);
        when(camelRegistry.lookupByNameAndType(MetricsComponent.METRIC_REGISTRY_NAME, MetricRegistry.class)).thenReturn(metricRegistry);
        Map<String, Object> params = new HashMap<String, Object>();
        Long value = System.currentTimeMillis();
        params.put("mark", value);
        Endpoint result = component.createEndpoint("metrics:meter:long.meter", "meter:long.meter", params);
        assertThat(result, is(notNullValue()));
        assertThat(result, is(instanceOf(MeterEndpoint.class)));
        MeterEndpoint me = (MeterEndpoint) result;
        assertThat(me.getMark(), is(value));
        assertThat(me.getMetricsName(), is("long.meter"));
        assertThat(me.getRegistry(), is(metricRegistry));

        params = new HashMap<String, Object>();
        params.put("increment", value + 1);
        params.put("decrement", value - 1);

        result = component.createEndpoint("metrics:counter:long.counter", "counter:long.counter", params);
        assertThat(result, is(notNullValue()));
        assertThat(result, is(instanceOf(CounterEndpoint.class)));
        CounterEndpoint ce = (CounterEndpoint) result;
        assertThat(ce.getIncrement(), is(value + 1));
        assertThat(ce.getDecrement(), is(value - 1));
        assertThat(ce.getMetricsName(), is("long.counter"));
        assertThat(ce.getRegistry(), is(metricRegistry));

        inOrder.verify(camelContext, times(1)).getRegistry();
        inOrder.verify(camelRegistry, times(1)).lookupByNameAndType(MetricsComponent.METRIC_REGISTRY_NAME, MetricRegistry.class);
        inOrder.verify(camelContext, times(2)).getTypeConverter();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testGetMetricsName() throws Exception {
        assertThat(component.getMetricsName("meter:metric-a"), is("metric-a"));
        assertThat(component.getMetricsName("meter:metric-a:sub-b"), is("metric-a:sub-b"));
        assertThat(component.getMetricsName("metric-a"), is("metric-a"));
        assertThat(component.getMetricsName("//metric-a"), is("//metric-a"));
        assertThat(component.getMetricsName("meter://metric-a"), is("//metric-a"));
    }

    @Test
    public void testCreateNewEndpointForCounter() throws Exception {
        Endpoint endpoint = component.createNewEndpoint(metricRegistry, MetricsType.COUNTER, "a name");
        assertThat(endpoint, is(notNullValue()));
        assertThat(endpoint, is(instanceOf(CounterEndpoint.class)));
    }

    @Test
    public void testCreateNewEndpointForMeter() throws Exception {
        Endpoint endpoint = component.createNewEndpoint(metricRegistry, MetricsType.METER, "a name");
        assertThat(endpoint, is(notNullValue()));
        assertThat(endpoint, is(instanceOf(MeterEndpoint.class)));
    }

    @Test(expected = RuntimeCamelException.class)
    public void testCreateNewEndpointForGauge() throws Exception {
        component.createNewEndpoint(metricRegistry, MetricsType.GAUGE, "a name");
    }

    @Test
    public void testCreateNewEndpointForHistogram() throws Exception {
        Endpoint endpoint = component.createNewEndpoint(metricRegistry, MetricsType.HISTOGRAM, "a name");
        assertThat(endpoint, is(notNullValue()));
        assertThat(endpoint, is(instanceOf(HistogramEndpoint.class)));
    }

    @Test
    public void testCreateNewEndpointForTimer() throws Exception {
        Endpoint endpoint = component.createNewEndpoint(metricRegistry, MetricsType.TIMER, "a name");
        assertThat(endpoint, is(notNullValue()));
        assertThat(endpoint, is(instanceOf(TimerEndpoint.class)));
    }

    @Test
    public void testGetMetricsType() throws Exception {
        for (MetricsType type : EnumSet.allOf(MetricsType.class)) {
            assertThat(component.getMetricsType(type.toString() + ":metrics-name"), is(type));
        }
    }

    @Test
    public void testGetMetricsTypeNotSet() throws Exception {
        assertThat(component.getMetricsType("no-metrics-type"), is(MetricsComponent.DEFAULT_METRICS_TYPE));
    }

    @Test(expected = RuntimeCamelException.class)
    public void testGetMetricsTypeNotFound() throws Exception {
        component.getMetricsType("unknown-metrics:metrics-name");
    }

    @Test
    public void testGetOrCreateMetricRegistryFoundInCamelRegistry() throws Exception {
        when(camelRegistry.lookupByNameAndType("name", MetricRegistry.class)).thenReturn(metricRegistry);
        MetricRegistry result = component.getOrCreateMetricRegistry(camelRegistry, "name");
        assertThat(result, is(metricRegistry));
        inOrder.verify(camelRegistry, times(1)).lookupByNameAndType("name", MetricRegistry.class);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testGetOrCreateMetricRegistryNotFoundInCamelRegistry() throws Exception {
        when(camelRegistry.lookupByNameAndType("name", MetricRegistry.class)).thenReturn(null);
        MetricRegistry result = component.getOrCreateMetricRegistry(camelRegistry, "name");
        assertThat(result, is(notNullValue()));
        assertThat(result, is(not(metricRegistry)));
        inOrder.verify(camelRegistry, times(1)).lookupByNameAndType("name", MetricRegistry.class);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testGetMetricRegistryFromCamelRegistry() throws Exception {
        when(camelRegistry.lookupByNameAndType("name", MetricRegistry.class)).thenReturn(metricRegistry);
        MetricRegistry result = component.getMetricRegistryFromCamelRegistry(camelRegistry, "name");
        assertThat(result, is(metricRegistry));
        inOrder.verify(camelRegistry, times(1)).lookupByNameAndType("name", MetricRegistry.class);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCreateMetricRegistry() throws Exception {
        MetricRegistry registry = component.createMetricRegistry();
        assertThat(registry, is(notNullValue()));
    }
}
