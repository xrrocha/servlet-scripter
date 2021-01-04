package net.xrrocha.service.email;

import javax.validation.constraints.NotNull;

/**
 * Simple service to send emails from a fixed email address to a single recipient w/o attachments.
 */
public interface EmailSender {

    /**
     * Send the email.
     *
     * @param to      The email recipient
     * @param subject The email title
     * @param text    The email content
     * @return A result summarizing the sending operation's outcome
     */
    EmailResult send(@NotNull String to, @NotNull String subject, @NotNull String text);
}
