package io.atleon.imi.api.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class JobPostingView {

    private final String hiringIntentId;

    private final String companyName;

    private final String jobTitle;

    private final String jobDescription;

    @JsonCreator
    public JobPostingView(
        @JsonProperty("hiringIntentId") String hiringIntentId,
        @JsonProperty("companyName") String companyName,
        @JsonProperty("jobTitle") String jobTitle,
        @JsonProperty("jobDescription") String jobDescription
    ) {
        this.hiringIntentId = hiringIntentId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
    }

    @Override
    public String toString() {
        return "JobPostingView{" +
            "hiringIntentId='" + hiringIntentId + '\'' +
            ", companyName='" + companyName + '\'' +
            ", jobTitle='" + jobTitle + '\'' +
            ", jobDescription='" + jobDescription + '\'' +
            '}';
    }

    public String getHiringIntentId() {
        return hiringIntentId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }
}
