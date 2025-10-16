package io.atleon.imi.service;

import io.atleon.imi.api.json.CompanyCreatedEvent;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.imi.domain.dao.CompanyDataCache;
import io.atleon.imi.domain.dao.HiringIntentDataCache;
import io.atleon.imi.domain.value.CompanyData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class CompanyIngestor {

    private final CompanyDataCache companyDataCache;

    private final HiringIntentDataCache hiringIntentDataCache;

    public CompanyIngestor(CompanyDataCache companyDataCache, HiringIntentDataCache hiringIntentDataCache) {
        this.companyDataCache = companyDataCache;
        this.hiringIntentDataCache = hiringIntentDataCache;
    }

    public Collection<MaterializeCommand> ingest(CompanyCreatedEvent event) {
        CompanyData data = new CompanyData(event.getCompanyId(), event.getCompanyName());

        companyDataCache.upsert(data);

        return hiringIntentDataCache.findIdsByCompanyId(event.getCompanyId()).stream()
            .map(MaterializeCommand::new)
            .collect(Collectors.toList());
    }
}
