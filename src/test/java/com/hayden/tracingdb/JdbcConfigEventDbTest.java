package com.hayden.tracingdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.jdbc_persistence.config.PGJson;
import com.hayden.tracingdb.entity.Event;
import com.hayden.tracingdb.repository.EventRepository;
import com.hayden.utilitymodule.db.DbDataSourceTrigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.TracingDbLiquibaseConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("telemetrydb")
public class JdbcConfigEventDbTest {

    @SpringBootApplication
    @ComponentScan(basePackages = "com.hayden.tracingdb")
    public static class JdbcConfigEventDbTestApplication {

        public static void main(String[] args) {
            SpringApplication.run(JdbcConfigEventDbTestApplication.class, args);
        }
    }

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private DbDataSourceTrigger trigger;
    @Autowired
    private TracingDbLiquibaseConfig.TracingDbRoutingDataSource tracingDbRoutingDataSource;

    @BeforeEach
    public void setDb() {
        trigger.doWithKey(setKey -> {
            setKey.setKey("event");
            eventRepository.deleteAll();
            setKey.setKey("main");
            eventRepository.deleteAll();
        });
    }

    @Test
    public void testEventDbConfig() throws JsonProcessingException {
        trigger.doWithKey(setKey -> {
            var firstUri = Assertions.assertDoesNotThrow(() -> tracingDbRoutingDataSource.getConnection().getMetaData().getURL());
            assertThat(setKey.curr()).isEqualTo("main");
            assertThat(eventRepository.count()).isEqualTo(0);
            var e = eventRepository.save(new Event());
            assertThat(e.getId()).isNotNull();
            setKey.setKey("event");
            var secondUri = Assertions.assertDoesNotThrow(() -> tracingDbRoutingDataSource.getConnection().getMetaData().getURL());
            assertThat(eventRepository.count()).isEqualTo(0);
            var second = eventRepository.save(new Event());
            assertThat(second.getId()).isNotNull();
            assertThat(firstUri).isNotEqualTo(secondUri);
        });


        eventRepository.deleteAll();
        Event entity = new Event();
        record First(String ok) { }

        ObjectMapper om = new ObjectMapper();
        var f = om.writeValueAsString(new First("ok"));
        entity.setData(new PGJson(f));
        var saved = eventRepository.save(entity);
        var foundAll = eventRepository.findAll();
        Optional<Event> readEvent = eventRepository.findById(saved.getId());
        assertThat(readEvent).isPresent();
        var found = readEvent.get();
        var deser = om.readValue(found.getData().value(), First.class);
        assertThat(deser.ok).isEqualTo("ok");
    }


}
