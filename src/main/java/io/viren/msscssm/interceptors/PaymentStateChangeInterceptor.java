package io.viren.msscssm.interceptors;

import io.viren.msscssm.domain.Payment;
import io.viren.msscssm.domain.PaymentEvent;
import io.viren.msscssm.domain.PaymentState;
import io.viren.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    public static final String PAYMENT_ID = "payment_id";
    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine)
    {
        Optional.ofNullable(message).ifPresent(msg -> extractPaymentId(state, message));
    }

    private void extractPaymentId(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message) {
        Optional.ofNullable(Long.class.cast(message.getHeaders().getOrDefault(PAYMENT_ID, -1L)))
                .ifPresent(paymentId -> updatePaymentStateLocal(state, paymentId));
    }

    private void updatePaymentStateLocal(State<PaymentState, PaymentEvent> state, Long paymentId) {
        final Payment payment = paymentRepository.getOne(paymentId);
        payment.setPaymentState(state.getId());
        paymentRepository.save(payment);
    }
}
