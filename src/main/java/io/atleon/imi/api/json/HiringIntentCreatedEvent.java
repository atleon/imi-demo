package io.atleon.imi.api.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class HiringIntentCreatedEvent {

    private final String hiringIntentId;

    private final String companyId;

    private final String jobTitle;

    private final String jobDescription;

    @JsonCreator
    public HiringIntentCreatedEvent(
        @JsonProperty("hiringIntentId") String hiringIntentId,
        @JsonProperty("companyId") String companyId,
        @JsonProperty("jobTitle") String jobTitle,
        @JsonProperty("jobDescription") String jobDescription
    ) {
        this.hiringIntentId = hiringIntentId;
        this.companyId = companyId;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
    }

    public String getHiringIntentId() {
        return hiringIntentId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }
}
