package com.ashish.shopoclock.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class PaymentController {

    @Value("${spring.stripe.stripe_api_key}")
    private String stripeApiKey;

    @Value("${spring.stripe.stripe_secret_key}")
    private String stripeSecretKey;


    @PostMapping("/payment/process")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Long> payload) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntent paymentIntent = PaymentIntent.create(
                new PaymentIntentCreateParams.Builder()
                        .setAmount(payload.get("amount"))
                        .setCurrency("cad")
                        .putMetadata("company", "Ecommerce")
                        .build()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("client_secret", paymentIntent.getClientSecret());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/stripeapikey")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> sendStripeApiKey() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("stripeApiKey", stripeApiKey);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}