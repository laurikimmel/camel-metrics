package org.apache.camel.metrics.histogram;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

public class HistogramProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(HistogramProducer.class);

    public HistogramProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        HistogramEndpoint endpoint = (HistogramEndpoint) getEndpoint();
        MetricRegistry registry = endpoint.getRegistry();
        String metricsName = endpoint.getMetricsName();
        Histogram histogram = registry.histogram(metricsName);
        Long value = endpoint.getValue();
        if (value != null) {
            histogram.update(value);
        }
        else {
            LOG.warn("Cannot update histogram \"{}\" with null value", metricsName);
        }
    }
}