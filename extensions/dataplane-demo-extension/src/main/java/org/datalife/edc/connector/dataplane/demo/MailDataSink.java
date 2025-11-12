package org.datalife.edc.connector.dataplane.demo;

import jakarta.activation.DataHandler;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult;
import org.eclipse.edc.connector.dataplane.util.sink.ParallelSink;
import org.eclipse.edc.spi.EdcException;

import java.util.Date;
import java.util.List;

public class MailDataSink extends ParallelSink {

    private Session session;
    private String recipient;
    private String sender;
    private String subject;

    public MailDataSink() {

    }

    @Override
    public StreamResult<Object> transferParts(List<DataSource.Part> parts) {
        var msg = new MimeMessage(session);
        try {
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, recipient);
            msg.setSubject(subject, "UTF-8");
            msg.setFrom(sender);

            var multipart = new MimeMultipart();
            for (var part : parts) {
                multipart.addBodyPart(createBodyPart(part));
            }

            msg.setContent(multipart);
            Transport.send(msg);
            return StreamResult.success();
        } catch (Exception e) {
            return StreamResult.error(e.getMessage());
        }
    }

    private BodyPart createBodyPart(DataSource.Part part) {
        try {
            var messageBodyPart = new MimeBodyPart();
            messageBodyPart.setFileName(part.name());
            var source = new ByteArrayDataSource(part.openStream(), part.mediaType());
            messageBodyPart.setDataHandler(new DataHandler(source));
            return messageBodyPart;
        } catch (Exception e) {
            throw new EdcException(e);
        }
    }
    public static class Builder extends ParallelSink.Builder<Builder, MailDataSink> {

        private Builder() {
            super (new MailDataSink());
        }

        public static MailDataSink.Builder newInstance() {
            return new Builder();
        }

        public MailDataSink.Builder session(Session session) {
            sink.session = session;
            return this;
        }

        public MailDataSink.Builder recipient(String recipient) {
            sink.recipient = recipient;
            return this;
        }

        public MailDataSink.Builder sender(String sender) {
            sink.sender = sender;
            return this;
        }

        public MailDataSink.Builder subject(String subject) {
            sink.subject = subject;
            return this;
        }

        @Override
        protected void validate() {
        }

    }
}
