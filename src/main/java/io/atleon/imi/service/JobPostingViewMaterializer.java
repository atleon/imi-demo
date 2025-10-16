package io.atleon.imi.service;

import io.atleon.imi.api.json.JobPostingView;
import io.atleon.imi.api.json.MaterializeCommand;
import io.atleon.imi.domain.dao.CompanyDataCache;
import io.atleon.imi.domain.dao.HiringIntentDataCache;
import io.atleon.imi.domain.value.CompanyData;
import io.atleon.imi.domain.value.HiringIntentData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JobPostingViewMaterializer {

    private final HiringIntentDataCache hiringIntentDataCache;

    private final CompanyDataCache companyDataCache;

    public JobPostingViewMaterializer(HiringIntentDataCache hiringIntentDataCache, CompanyDataCache companyDataCache) {
        this.hiringIntentDataCache = hiringIntentDataCache;
        this.companyDataCache = companyDataCache;
    }

    public Collection<JobPostingView> materialize(Collection<MaterializeCommand> commands) {
        // Dereference
        Collection<String> hiringIntentIds = commands.stream()
            .map(MaterializeCommand::getHiringIntentId)
            .collect(Collectors.toSet());

        Collection<HiringIntentData> hiringIntentData = hiringIntentDataCache.findByIds(hiringIntentIds);

        Collection<String> companyIds = hiringIntentData.stream()
            .map(HiringIntentData::companyId)
            .collect(Collectors.toSet());

        Map<String, CompanyData> companyData = companyDataCache.findByIds(companyIds).stream()
            .collect(Collectors.toMap(CompanyData::id, Function.identity()));

        // Generate
        return hiringIntentData.stream()
            .filter(it -> companyData.containsKey(it.companyId()))
            .map(it -> generate(it, companyData.get(it.companyId())))
            .collect(Collectors.toList());
    }

    private JobPostingView generate(HiringIntentData hiringIntentData, CompanyData companyData) {
        return new JobPostingView(
            hiringIntentData.id(),
            companyData.name(),
            hiringIntentData.jobTitle(),
            hiringIntentData.jobDescription()
        );
    }
}
