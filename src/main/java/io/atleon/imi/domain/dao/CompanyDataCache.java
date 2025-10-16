package io.atleon.imi.domain.dao;

import io.atleon.imi.domain.value.CompanyData;

import java.util.Collection;

public interface CompanyDataCache {

    void upsert(CompanyData data);

    Collection<CompanyData> findByIds(Collection<String> ids);
}
