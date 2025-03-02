package com.hayden.tracingdb.entity;

import com.hayden.jdbc_persistence.config.PGJson;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("event")
@NoArgsConstructor
@Data
public class Event {

    public Event(String data, String trace) {
        this.data = new PGJson(data);
        this.trace = new PGJson(trace);
    }

    @Id
    Long id;

    @Column("data")
    PGJson data;
    @Column("trace")
    PGJson trace;

}
