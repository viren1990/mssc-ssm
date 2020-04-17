package io.viren.msscssm.services;

import io.viren.msscssm.domain.Payment;
import io.viren.msscssm.domain.PaymentEvent;
import io.viren.msscssm.domain.PaymentState;
import io.viren.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@SpringBootTest
public class PaymentServiceIT {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(BigDecimal.valueOf(10.0)).build();
    }

    @Test
    @DisplayName("Test PreAuth")
    @Transactional
    @RepeatedTest(10)
    void preAuth() {
        final Payment savedPayment = paymentService.newPayment(payment);
        System.out.println("Current state is " + savedPayment.getPaymentState());
        final StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuth(savedPayment.getId());

        final Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        if (stateMachine.getState().getId() == PaymentState.PRE_AUTH) {
            assertThat(preAuthPayment).extracting(Payment::getPaymentState).isEqualTo(PaymentState.PRE_AUTH);
        } else if (stateMachine.getState().getId() == PaymentState.PRE_AUTH_ERROR) {
            assertThat(preAuthPayment).extracting(Payment::getPaymentState).isEqualTo(PaymentState.PRE_AUTH_ERROR);
        }
    }
}