package app.nzyme.core.events.actions.email;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.actions.Action;
import app.nzyme.core.events.actions.ActionExecutionResult;
import app.nzyme.core.events.types.SystemEvent;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.integrations.smtp.SMTPConfigurationRegistryKeys;
import app.nzyme.plugin.RegistryCryptoException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import javax.mail.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class EmailAction implements Action {

    private static final Logger LOG = LogManager.getLogger(EmailAction.class);

    private final EmailActionConfiguration configuration;
    private final String fromAddress;

    private final Mailer mailer;
    private final freemarker.template.Configuration templateConfig;

    public EmailAction(NzymeNode nzyme, EmailActionConfiguration configuration) {
        this.configuration = configuration;

        String transportStrategy = nzyme.getDatabaseCoreRegistry()
                .getValueOrNull(SMTPConfigurationRegistryKeys.TRANSPORT_STRATEGY.key());

        String hostname = nzyme.getDatabaseCoreRegistry()
                .getValueOrNull(SMTPConfigurationRegistryKeys.HOST.key());

        Integer port = nzyme.getDatabaseCoreRegistry()
                .getValue(SMTPConfigurationRegistryKeys.PORT.key())
                .map(Integer::parseInt)
                .orElse(null);

        String username = nzyme.getDatabaseCoreRegistry()
                .getValueOrNull(SMTPConfigurationRegistryKeys.USERNAME.key());

        String password;
        try {
            password = nzyme.getDatabaseCoreRegistry()
                    .getEncryptedValueOrNull(SMTPConfigurationRegistryKeys.PASSWORD.key());
        } catch (RegistryCryptoException e) {
            throw new RuntimeException(e);
        }

        this.fromAddress = nzyme.getDatabaseCoreRegistry()
                .getValueOrNull(SMTPConfigurationRegistryKeys.FROM_ADDRESS.key());

        if (Strings.isNullOrEmpty(transportStrategy) ||
                Strings.isNullOrEmpty(hostname) ||
                port == null ||
                Strings.isNullOrEmpty(username) ||
                Strings.isNullOrEmpty(password) ||
                Strings.isNullOrEmpty(this.fromAddress)) {
            throw new RuntimeException("Incomplete SMTP configuration. Cannot create Email action.");
        }

        TransportStrategy parsedTransportStrategy;
        switch (transportStrategy) {
            case "SMTP":
                parsedTransportStrategy = TransportStrategy.SMTP;
                break;
            case "SMTP TLS":
                parsedTransportStrategy = TransportStrategy.SMTP_TLS;
                break;
            case "SMTPS":
                parsedTransportStrategy = TransportStrategy.SMTPS;
                break;
            default:
                throw new RuntimeException("Unknown/Invalid transport strategy.");
        }

        this.mailer = MailerBuilder
                .withSMTPServer(hostname, port, username, password)
                .withTransportStrategy(parsedTransportStrategy)
                .clearEmailAddressCriteria()
                .buildMailer();

        // Set up template engine.
        this.templateConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_30);
        this.templateConfig.setClassForTemplateLoading(this.getClass(), "/");
        this.templateConfig.setDefaultEncoding("UTF-8");
        this.templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.templateConfig.setLogTemplateExceptions(false);
        this.templateConfig.setWrapUncheckedExceptions(true);
        this.templateConfig.setFallbackOnNullLoopVariable(false);
    }

    @Override
    public ActionExecutionResult execute(SystemEvent event) {
        LOG.info("Executing [{}] for event type [{}].", this.getClass().getCanonicalName(), event.type());

        try {
            List<Recipient> recipients = Lists.newArrayList();
            for (String receiverAddress : configuration.receivers()) {
                recipients.add(new Recipient(receiverAddress, receiverAddress, Message.RecipientType.TO));
            }

            SystemEventType eventType = event.type();

            Email email = EmailBuilder.startingBlank()
                    .to(recipients)
                    .from(this.fromAddress)
                    .withSubject(configuration.subjectPrefix() + " " + buildSubject(eventType))
                    .withPlainText(buildPlainTextBody(event))
                    .withHTMLText(buildHTMLTextBody(event))
                    .withEmbeddedImage("nzyme_logo", loadResourceFile("email/nzyme.png"), "image/png")
                    .withEmbeddedImage("header_top", loadResourceFile("email/header_top.png"), "image/png")
                    .withEmbeddedImage("header_top", loadResourceFile("email/header_bottom.png"), "image/png")
                    .buildEmail();

            mailer.sendMail(email);
        } catch(Exception e) {
            LOG.error("Could not send Email.", e);
        }

        return ActionExecutionResult.SUCCESS;
    }

    private String buildSubject(SystemEventType eventType) {
        return "System Event: " + eventType.getHumanReadableName();
    }

    private String buildPlainTextBody(SystemEvent event) {
        SystemEventType eventType = event.type();

        String b = "System Event: " + eventType.getHumanReadableName() + " [" + eventType.name() + "]\n\n"
                + event.details() + "\n\n"
                + "Event Timestamp: " + event.timestamp();

        return b;
    }

    @Nullable
    private String buildHTMLTextBody(SystemEvent event) {
        try {
            SystemEventType eventType = event.type();

            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("event_type_name", eventType.name());
            parameters.put("event_type_name_human_readable", eventType.getHumanReadableName());
            parameters.put("event_details", event.details());
            parameters.put("event_timestamp", event.timestamp());

            StringWriter out = new StringWriter();
            Template template = this.templateConfig.getTemplate("email/system_event.ftl");
            template.process(parameters, out);
            return out.toString();
        } catch(Exception e) {
            LOG.error("Could not build HTML text body.", e);
            return null;
        }
    }

    private byte[] loadResourceFile(String filename) throws IOException {
        //noinspection UnstableApiUsage
        InputStream resource = getClass().getClassLoader().getResourceAsStream(filename);
        if (resource == null) {
            throw new RuntimeException("Couldn't load resource file: " + filename);
        }

        //noinspection UnstableApiUsage
        return resource.readAllBytes();
    }

}
