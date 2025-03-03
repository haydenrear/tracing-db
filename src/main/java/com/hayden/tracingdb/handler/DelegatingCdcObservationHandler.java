package com.hayden.tracingdb.handler;

import com.hayden.jdbc_persistence.config.PgJson;
import com.hayden.tracingdb.entity.Event;
import com.hayden.tracingdb.repository.EventRepository;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/// @see com.hayden.tracing_apt.Logged <- annotation for aspect
/// @see com.hayden.tracing_apt.observation_aspects.CdcObservabilityAspect <- aspect itself
/// @see com.hayden.tracing_apt.observation_aspects.ObservationBehavior <- save json context data and ObservationContext
/// @see com.hayden.tracingdb.handler.DelegatingCdcObservationHandler <- save the json context data to the CDC database (gets called by micrometer)
@Component
public class DelegatingCdcObservationHandler extends DefaultTracingObservationHandler {

    @Autowired
    private EventRepository eventRepository;

    public DelegatingCdcObservationHandler(Tracer tracer) {
        super(tracer);
    }

    public void onStart(Observation.Context context) {
        var evtBuilder = Event.builder();
        parseContextKey(context, "data")
                .map(evtBuilder::data)
                .or(() -> Optional.of(evtBuilder))
                .flatMap(evt -> parseContextKey(context, "trace")
                        .map(evtBuilder::trace))
                .map(Event.EventBuilder::build)
                .ifPresent(eventRepository::save);
        super.onStart(context);
    }

    private static @NotNull Optional<PgJson.MapPgJson> parseContextKey(Observation.Context context,
                                                                       String data) {
        return Optional.of(
                        context.getHighCardinalityKeyValue(data)
                                .getValue())
                .filter(StringUtils::isNotEmpty)
                .map(dataItem -> new PgJson.MapPgJson(Map.of(data, dataItem)));
    }

}
