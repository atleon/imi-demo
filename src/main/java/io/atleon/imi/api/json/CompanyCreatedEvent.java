package io.atleon.imi.api.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CompanyCreatedEvent {

    private final String companyId;

    private final String companyName;

    @JsonCreator
    public CompanyCreatedEvent(
        @JsonProperty("companyId") String companyId,
        @JsonProperty("companyName") String companyName
    ) {
        this.companyId = companyId;
        this.companyName = companyName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }
}
