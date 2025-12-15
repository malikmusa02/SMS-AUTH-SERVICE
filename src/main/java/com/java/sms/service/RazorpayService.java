package com.java.sms.service;



import java.util.Map;


import java.util.Map;

public interface RazorpayService {
    /**
     * Create order on Razorpay. amountInPaise = INR * 100 (e.g., 100.00 -> 10000).
     * Returns order as Map (contains at least id).
     */
    Map<String, Object> createOrder(long amountInPaise, String currency, String receipt) throws Exception;

    /**
     * Verify client signature after payment: HMAC_SHA256(orderId + '|' + paymentId, secret) hex.
     */
    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);

    /**
     * Verify webhook signature: HMAC_SHA256(payload, webhookSecret) hex.
     */
    boolean verifyWebhookSignature(String payload, String headerSignature);
}

