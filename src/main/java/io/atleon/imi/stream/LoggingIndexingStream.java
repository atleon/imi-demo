package io.atleon.imi.stream;

import io.atleon.imi.api.json.JobPostingView;
import io.atleon.spring.AutoConfigureStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@AutoConfigureStream
public class LoggingIndexingStream extends IndexingStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingIndexingStream.class);

    public LoggingIndexingStream(ApplicationContext context) {
        super(context);
    }

    @Override
    protected void index(JobPostingView view) {
        LOGGER.info("Indexing view={}", view);
    }
}
