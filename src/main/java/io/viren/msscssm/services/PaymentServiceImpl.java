package io.viren.msscssm.services;

import io.viren.msscssm.domain.Payment;
import io.viren.msscssm.domain.PaymentEvent;
import io.viren.msscssm.domain.PaymentState;
import io.viren.msscssm.nterceptors.PaymentStateChangeInterceptor;
import io.viren.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentRepository paymentRepository;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        final StateMachine<PaymentState, PaymentEvent> machine = build(paymentId);
        send(machine, paymentId, PaymentEvent.PRE_AUTH);
        return machine;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizes(Long paymentId) {
        final StateMachine<PaymentState, PaymentEvent> machine = build(paymentId);
        send(machine, paymentId, PaymentEvent.AUTH_APPROVED);
        return machine;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        final StateMachine<PaymentState, PaymentEvent> machine = build(paymentId);
        send(machine, paymentId, PaymentEvent.AUTH_DECLINED);
        return machine;
    }

    private void send(final StateMachine<PaymentState, PaymentEvent> machine, final Long paymentId, PaymentEvent paymentEvent) {
        final Message<PaymentEvent> message = MessageBuilder.withPayload(paymentEvent)
                .setHeader(PaymentStateChangeInterceptor.PAYMENT_ID, paymentId)
                .build();
        machine.sendEvent(message);

    }

    private StateMachine<PaymentState, PaymentEvent> build(final Long paymentId) {
        final Payment payment = paymentRepository.getOne(paymentId);

        final StateMachine<PaymentState, PaymentEvent> stateMachine =
                stateMachineFactory.getStateMachine(String.valueOf(payment.getId()));
        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getPaymentState(), null, null, null));
                });

        stateMachine.start();
        return stateMachine;
    }
}
