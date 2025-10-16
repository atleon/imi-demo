package io.atleon.imi.domain.dao;

import io.atleon.imi.domain.value.CompanyData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryCompanyDataCache implements CompanyDataCache {

    private final Map<String, CompanyData> map = new ConcurrentHashMap<>();

    @Override
    public void upsert(CompanyData data) {
        map.put(data.id(), data);
    }

    @Override
    public Collection<CompanyData> findByIds(Collection<String> ids) {
        return ids.stream().distinct().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
