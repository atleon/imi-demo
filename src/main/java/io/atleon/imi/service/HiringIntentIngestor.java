package io.atleon.imi.service;

import io.atleon.imi.api.json.HiringIntentCreatedEvent;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.imi.domain.dao.HiringIntentDataCache;
import io.atleon.imi.domain.value.HiringIntentData;
import org.springframework.stereotype.Component;

@Component
public class HiringIntentIngestor {

    private final HiringIntentDataCache cache;

    public HiringIntentIngestor(HiringIntentDataCache cache) {
        this.cache = cache;
    }

    public MaterializeCommand ingest(HiringIntentCreatedEvent event) {
        HiringIntentData data = new HiringIntentData(
            event.getHiringIntentId(),
            event.getCompanyId(),
            event.getJobTitle(),
            event.getJobDescription());

        cache.upsert(data);

        return new MaterializeCommand(event.getHiringIntentId());
    }
}
