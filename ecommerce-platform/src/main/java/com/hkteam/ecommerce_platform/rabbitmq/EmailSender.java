package com.hkteam.ecommerce_platform.rabbitmq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hkteam.ecommerce_platform.dto.request.EmailMessageRequest;
import com.hkteam.ecommerce_platform.dto.request.SendMailAfterOrderRequest;
import com.hkteam.ecommerce_platform.dto.response.email.OrderEmail;
import com.hkteam.ecommerce_platform.dto.response.email.OrderItemEmail;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.payment.Payment;
import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import com.hkteam.ecommerce_platform.enums.PaymentMethod;
import com.hkteam.ecommerce_platform.repository.PaymentRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailSender {

    JavaMailSender emailSender;
    TemplateEngine templateEngine;
    private final PaymentRepository paymentRepository;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void sendEmail(EmailMessageRequest request) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());

        Map<String, Object> variables = new HashMap<>();
        variables.put("tokenUrl", request.getTokenUrl());

        String htmlContent = renderTemplate(request.getTemplate(), variables);

        helper.setText(htmlContent, true);

        try {
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending email", e);
        }

        log.info("Email sent to {}", request.getTo());
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    @RabbitListener(queues = RabbitMQConfig.SEND_MAIL_AFTER_ORDER_QUEUE)
    @Transactional
    public void sendMailAfterOrder(SendMailAfterOrderRequest request) throws MessagingException {
        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow();
        for (Transaction transaction : payment.getTransactions()) {
            sendMailAfterOrder(
                    request.getName(), request.getEmail(), transaction, payment.getPaymentMethod(), payment.getId());
        }
    }

    private void sendMailAfterOrder(
            String name, String email, Transaction transaction, PaymentMethod paymentMethod, String paymentId)
            throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[HK-Uptech] Đơn hàng #" + transaction.getOrder().getId() + " đặt thành công");

        Order order = transaction.getOrder();
        List<OrderItemEmail> itemEmails = new ArrayList<>();
        order.getOrderItems().forEach(orderItem -> {
            String values = "";
            if (Objects.nonNull(orderItem.getValues())) {
                for (String value : orderItem.getValues()) {
                    values += value + ", ";
                }
            }
            if (!values.isEmpty()) {
                values = values.substring(0, values.length() - 2);
            }

            OrderItemEmail itemEmail = OrderItemEmail.builder()
                    .name(orderItem.getProduct().getName())
                    .imageUrl(orderItem.getProduct().getMainImageUrl())
                    .values(values)
                    .quantity(orderItem.getQuantity())
                    .price((orderItem.getPrice().subtract(orderItem.getDiscount()))
                            .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                    .build();
            itemEmails.add(itemEmail);
        });
        LocalDateTime localDateTimeSpecificZone =
                LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.of("Asia/Ho_Chi_Minh"));

        OrderEmail orderResponse = OrderEmail.builder()
                .id(order.getId())
                .orderDate(localDateTimeSpecificZone)
                .seller(order.getStore().getName())
                .items(itemEmails)
                .subtotal(order.getTotal())
                .shopDiscount(order.getDiscount())
                .shippingFee(order.getShippingFee())
                .total(order.getGrandTotal())
                .paymentMethod(paymentMethod.name())
                .paymentStatusUrl("http://localhost:3000/status/" + paymentId)
                .build();

        Map<String, Object> variables = new HashMap<>();
        variables.putAll(Map.of(
                "name", name,
                "order", orderResponse));

        String htmlContent = renderTemplate("order-confirmation", variables);

        helper.setText(htmlContent, true);

        try {
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending email", e);
        }

        log.info("Email sent to {}", email);
    }
}
