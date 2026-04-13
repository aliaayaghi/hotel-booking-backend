package com.HotelBook.HotelBooking.Notification;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock Email Sender — active in development and test (not in production).
 *
 * For Step 1: logs the email to the console so you can verify the content
 * without needing a real SMTP server.
 *
 * For Step 2: create a RealEmailSender with @Profile("prod") that uses
 * JavaMailSender, and this mock will automatically be excluded from the
 * production build just by activating the "prod" Spring profile.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * Development testing with MailHog (optional, but recommended):
 *   1. docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog
 *   2. Change this class to use JavaMailSender pointing at localhost:1025
 *   3. Open http://localhost:8025 to see sent emails in a web UI
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@Service
@Profile("!prod")    // active in dev, test, and any non-prod profile
public class MockEmailSender {

    /**
     * "Sends" an email by logging it to the console.
     *
     * @param toEmail  recipient's email address
     * @param subject  email subject line
     * @param body     email body (HTML or plain text)
     */
    public void send(String toEmail, String subject, String body) {
        log.info("""
                
                ╔══════════════════════════════════════════════════════════╗
                ║  [MOCK EMAIL SENT]
                ║  To:      {}
                ║  Subject: {}
                ╚══════════════════════════════════════════════════════════╝
                Body:
                {}
                """,
                toEmail,
                subject,
                body
        );
    }
}