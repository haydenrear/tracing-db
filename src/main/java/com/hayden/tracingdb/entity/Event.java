package com.hayden.tracingdb.entity;

import com.hayden.jdbc_persistence.config.PgJson;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;

@Table("event")
@NoArgsConstructor
@Data
public class Event {

    public Event(Map<String, Object> data, Map<String, Object> trace) {
        this.data = new PgJson.MapPgJson(data);
        this.trace = new PgJson.MapPgJson(trace);
    }

    @Id
    Long id;

    @Column("data")
    PgJson.MapPgJson data;
    @Column("trace")
    PgJson.MapPgJson trace;

}
