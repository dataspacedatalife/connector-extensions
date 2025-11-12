package org.datalife.edc.connector.dataplane.demo;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataTransferExecutorServiceContainer;
import org.eclipse.edc.connector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.Properties;

@Extension(value = MyDataPlaneExtension.NAME)
public class MyDataPlaneExtension implements ServiceExtension {
    public static final String NAME = "Data Plane Demo Extension";
    @Inject
    PipelineService pipelineService;

    @Inject
    private DataTransferExecutorServiceContainer executorContainer;

    @Setting(description = "Correo electrónico del remitente para el envío de emails", key = "edc.dataplane.mail.sender")
    private  String sender;

    @Setting(description = "Contraseña del remitente para la autenticación SMTP", key = "edc.dataplane.mail.password")
    private String password;

    @Override
    public void initialize(ServiceExtensionContext context) {

        pipelineService.registerFactory(new FileDataSourceFactory());

        pipelineService.registerFactory(new MailDataSinkFactory(getSession(), sender, executorContainer.getExecutorService()));
    }

    private Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.ethereal.email");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });
    }
}