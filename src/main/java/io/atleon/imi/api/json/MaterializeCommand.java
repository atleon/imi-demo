package io.atleon.imi.api.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class MaterializeCommand {

    private final String hiringIntentId;

    @JsonCreator
    public MaterializeCommand(@JsonProperty("hiringIntentId") String hiringIntentId) {
        this.hiringIntentId = hiringIntentId;
    }

    public String getHiringIntentId() {
        return hiringIntentId;
    }
}
