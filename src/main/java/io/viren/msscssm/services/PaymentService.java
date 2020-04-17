package io.viren.msscssm.services;

import io.viren.msscssm.domain.Payment;
import io.viren.msscssm.domain.PaymentEvent;
import io.viren.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(final Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(final Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizes(final Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(final Long paymentId);
}
