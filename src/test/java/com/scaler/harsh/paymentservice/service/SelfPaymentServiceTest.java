package com.scaler.harsh.paymentservice.service;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import com.scaler.harsh.paymentservice.model.PaymentRecord;
import com.scaler.harsh.paymentservice.repository.PaymentRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.scaler.harsh.paymentservice.exceptions.ServiceException;
import org.springframework.http.HttpStatus;
import java.util.Optional;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelfPaymentServiceTest {

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @Mock
    private Random random;

    private SelfPaymentService selfPaymentService;

    @BeforeEach
    void setUp() {
        selfPaymentService = new SelfPaymentService(paymentRecordRepository, random);
            lenient().when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(inv -> {
            PaymentRecord record = inv.getArgument(0);
            record.setId(1);
            return record;
        });
    }

    private CreatePaymentRequestDTO buildRequest(Integer orderId, double amount, String method, Boolean simulateFailure) {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setOrderId(orderId);
        request.setUserId(3);
        request.setAmount(amount);
        request.setPaymentMethod(method);
        request.setSimulateFailure(simulateFailure);
        return request;
    }

    @Test
    void processPayment_simulateFailureTrue_alwaysFails_regardlessOfRandom() {
        // random is never even consulted when simulateFailure short-circuits the check
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", true);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("FAILED", response.getStatus());
        verifyNoInteractions(random);
    }

    @Test
    void processPayment_organicFailure_randomBelowThreshold_fails() {
        when(random.nextDouble()).thenReturn(0.05); // < 0.10 organic failure rate
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", false);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("FAILED", response.getStatus());
    }

    @Test
    void processPayment_randomAboveThreshold_succeeds() {
        when(random.nextDouble()).thenReturn(0.50); // > 0.10
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", false);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void processPayment_randomExactlyAtThreshold_succeeds_strictLessThan() {
        // condition is `random.nextDouble() < 0.10`, so exactly 0.10 must NOT count as a failure
        when(random.nextDouble()).thenReturn(0.10);
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", false);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void processPayment_simulateFailureNull_treatedAsFalse_usesOrganicPath() {
        when(random.nextDouble()).thenReturn(0.50);
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", null);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void processPayment_paymentMethodNormalized_trimmedAndUppercased() {
        when(random.nextDouble()).thenReturn(0.50);
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "  card  ", false);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("CARD", response.getPaymentMethod());
    }

    @Test
    void processPayment_transactionIdGenerated_evenOnFailure() {
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", true);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN-"));
    }

    @Test
    void processPayment_savesRecordWithCorrectFields() {
        when(random.nextDouble()).thenReturn(0.50);
        CreatePaymentRequestDTO request = buildRequest(42, 250.75, "UPI", false);

        selfPaymentService.processPayment(request);

        ArgumentCaptor<PaymentRecord> captor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRecordRepository).save(captor.capture());
        PaymentRecord saved = captor.getValue();
        assertEquals(42, saved.getOrderId());
        assertEquals(3, saved.getUserId());
        assertEquals(250.75, saved.getAmount());
        assertEquals("UPI", saved.getPaymentMethod());
        assertEquals("SUCCESS", saved.getStatus());
    }

    @Test
    void processPayment_responseMapsFromSavedRecord_notRawRequest() {
        when(random.nextDouble()).thenReturn(0.50);
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(inv -> {
            PaymentRecord record = inv.getArgument(0);
            record.setId(99);
            record.setTransactionId("TXN-overridden-by-db");
            return record;
        });
        CreatePaymentRequestDTO request = buildRequest(1, 100.0, "CARD", false);

        PaymentResponseDTO response = selfPaymentService.processPayment(request);

        assertEquals("TXN-overridden-by-db", response.getTransactionId());
    }

    // ---------- getPaymentByOrderId ----------
    @Test
    void getPaymentByOrderId_found_returnsMappedResponse() {
        PaymentRecord record = new PaymentRecord();
        record.setId(5);
        record.setOrderId(42);
        record.setUserId(3);
        record.setAmount(250.75);
        record.setStatus("SUCCESS");
        record.setTransactionId("TXN-abc");
        record.setPaymentMethod("UPI");
        when(paymentRecordRepository.findByOrderId(42)).thenReturn(Optional.of(record));

        PaymentResponseDTO response = selfPaymentService.getPaymentByOrderId(42);

        assertEquals(42, response.getOrderId());
        assertEquals("TXN-abc", response.getTransactionId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("UPI", response.getPaymentMethod());
        assertEquals(250.75, response.getAmount());
    }

    @Test
    void getPaymentByOrderId_notFound_throws404() {
        when(paymentRecordRepository.findByOrderId(999)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> selfPaymentService.getPaymentByOrderId(999));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

}