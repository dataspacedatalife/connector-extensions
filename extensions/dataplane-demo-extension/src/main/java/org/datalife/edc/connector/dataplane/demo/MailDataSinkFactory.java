package org.datalife.edc.connector.dataplane.demo;

import jakarta.mail.Session;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSink;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSinkFactory;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class MailDataSinkFactory implements DataSinkFactory {

    private final Session session;
    private final String sender;
    private final ExecutorService executorService;

    public MailDataSinkFactory(Session session, String sender, ExecutorService executorService) {
        this.session = session;
        this.sender = sender;
        this.executorService = executorService;
    }

    @Override
    public String supportedType() {
        return "Mail";
    }

    @Override
    public DataSink createSink(DataFlowStartMessage request) {
        var recipient = getRecipient(request);
        var subject = "File transfer %s".formatted(request.getProcessId());
        return  MailDataSink.Builder.newInstance()
                .session(session)
                .recipient(recipient)
                .sender(sender)
                .subject(subject)
                .executorService(executorService)
                .requestId(request.getId())
                .build();
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        return Optional.ofNullable(getRecipient(request))
                .map(it -> Result.success())
                .orElseGet(() -> Result.failure("Missing recipient"));
    }

    private String getRecipient(DataFlowStartMessage request) {
        var destination = request.getDestinationDataAddress();
        return destination.getStringProperty("recipient");
    }
}

