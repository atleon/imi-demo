package io.atleon.imi.domain.value;

public final class HiringIntentData {

    private final String id;

    private final String companyId;

    private final String jobTitle;

    private final String jobDescription;

    public HiringIntentData(String id, String companyId, String jobTitle, String jobDescription) {
        this.id = id;
        this.companyId = companyId;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
    }

    public String id() {
        return id;
    }

    public String companyId() {
        return companyId;
    }

    public String jobTitle() {
        return jobTitle;
    }

    public String jobDescription() {
        return jobDescription;
    }
}
