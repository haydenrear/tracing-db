package com.hayden.tracingdb.handler;

import com.hayden.tracingdb.entity.Event;
import com.hayden.tracingdb.repository.EventRepository;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class DelegatingCdcObservationHandler extends DefaultTracingObservationHandler {

    @Autowired
    private EventRepository eventRepository;

    public DelegatingCdcObservationHandler(Tracer tracer) {
        super(tracer);
    }

    public void onStart(Observation.Context context) {
        Optional.of(context.getHighCardinalityKeyValue("data"))
            .flatMap( d ->
                Optional.of(context.getHighCardinalityKeyValue("trace"))
                    .map( it -> Map.entry(it, d))
            ).ifPresent( it ->  {
                eventRepository.save(new Event(
                        it.getKey().getValue(),
                        it.getValue().getValue()));
                context.removeHighCardinalityKeyValue("data");
                context.remove("data");
        });
        super.onStart(context);
    }

}
