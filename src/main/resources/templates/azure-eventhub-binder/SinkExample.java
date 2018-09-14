package {{packageName}};

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Header;

@EnableBinding(Sink.class)
public class SinkExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SinkExample.class);

    @StreamListener(Sink.INPUT)
    public void handleMessage(String message, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        LOGGER.info("New message received: '{}'", message);
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                LOGGER.info("Message '{}' successfully checkpointed", message);
            }

            return null;
        });
    }
}
