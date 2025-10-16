package io.atleon.imi.domain.value;

public final class CompanyData {

    private final String id;

    private final String name;

    public CompanyData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}
