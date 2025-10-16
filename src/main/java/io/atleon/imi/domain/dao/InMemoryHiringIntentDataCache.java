package io.atleon.imi.domain.dao;

import io.atleon.imi.domain.value.HiringIntentData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryHiringIntentDataCache implements HiringIntentDataCache {

    private final Map<String, HiringIntentData> map = new ConcurrentHashMap<>();

    @Override
    public void upsert(HiringIntentData data) {
        map.put(data.id(), data);
    }

    @Override
    public Collection<HiringIntentData> findByIds(Collection<String> ids) {
        return ids.stream().distinct().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Collection<String> findIdsByCompanyId(String companyId) {
        return map.values().stream()
            .filter(it -> it.companyId().equals(companyId))
            .map(HiringIntentData::id)
            .collect(Collectors.toList());
    }
}
