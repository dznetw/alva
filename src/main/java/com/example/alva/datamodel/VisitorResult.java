package com.example.alva.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VisitorResult {

    @JsonProperty("process_id")
    private String id;

    public VisitorResult(final VisitorProcess process) {
        this.id = process.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        if (this.id == null) {
            this.id = id;
        }
    }
}
