package com.java.sms.serviceImpl;



import com.java.sms.exception.ApiException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.java.sms.service.RazorpayService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Real Razorpay implementation using official SDK.
 */
@Service
@Slf4j
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayClient client;
    private final String keySecret;
    private final String webhookSecret; // optional (if using webhook secret different from key secret)

    public RazorpayServiceImpl(@Value("${razorpay.key-id}") String keyId,
                               @Value("${razorpay.key-secret}") String keySecret,
                               @Value("${razorpay.webhook-secret:}") String webhookSecret) throws RazorpayException {
        this.client = new RazorpayClient(keyId, keySecret);
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public Map<String, Object> createOrder(long amountInPaise, String currency, String receipt) throws Exception {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1);

            Order order = client.orders.create(orderRequest);

            Map<String, Object> out = new HashMap<>();
            out.put("id", order.get("id"));
            out.put("amount", order.get("amount"));
            out.put("currency", order.get("currency"));
            out.put("receipt", order.get("receipt"));
            out.put("status", order.get("status"));
            return out;
        } catch (RazorpayException e) {
            log.error("Razorpay createOrder failed: msg={}",  e.getMessage(), e);
            throw new ApiException("Razorpay order creation failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String computed = hmacSha256Hex(payload, keySecret);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying payment signature: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String headerSignature) {
        try {
            String secret = (webhookSecret != null && !webhookSecret.isBlank()) ? webhookSecret : keySecret;
            String computed = hmacSha256Hex(payload, secret);
            return computed.equals(headerSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    private static String hmacSha256Hex(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder(2 * raw.length);
        for (byte b : raw) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
