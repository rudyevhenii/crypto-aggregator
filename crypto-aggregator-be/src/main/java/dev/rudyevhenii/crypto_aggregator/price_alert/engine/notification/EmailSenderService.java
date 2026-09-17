package dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendMessage(String to, PriceAlert priceAlert, BigDecimal livePrice) {
        MimeMessage message = javaMailSender.createMimeMessage();
        Context context = new Context();

        context.setVariable("alert", priceAlert);
        context.setVariable("livePrice", livePrice);

        String htmlText = templateEngine.process("alert-email", context);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setTo(to);
            helper.setSubject("🔔 Price Alert: %s reached $%s".formatted(
                    priceAlert.getTradingPair(), livePrice.stripTrailingZeros()
            ));
            helper.setText(htmlText, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.warn("Could not send email to: {} with exception {}", to, e.getMessage());
        }
    }
}
