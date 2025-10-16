package io.atleon.imi.domain.dao;

import io.atleon.imi.domain.value.HiringIntentData;

import java.util.Collection;

public interface HiringIntentDataCache {

    void upsert(HiringIntentData data);

    Collection<HiringIntentData> findByIds(Collection<String> ids);

    Collection<String> findIdsByCompanyId(String companyId);
}
