package io.atleon.imi.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.atleon.imi.api.json.JobPostingView;
import io.atleon.spring.AutoConfigureStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@AutoConfigureStream
public class LoggingIndexingStream extends IndexingStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingIndexingStream.class);

    private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public LoggingIndexingStream(ApplicationContext context) {
        super(context);
    }

    @Override
    protected void index(JobPostingView view) {
        try {
            LOGGER.info("Indexing view:\n{}", OBJECT_WRITER.writeValueAsString(view));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write view as JSON", e);
        }
    }
}
