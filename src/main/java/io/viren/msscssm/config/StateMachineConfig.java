package io.viren.msscssm.config;

import io.viren.msscssm.domain.PaymentEvent;
import io.viren.msscssm.domain.PaymentState;
import io.viren.msscssm.interceptors.PaymentStateChangeInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@EnableStateMachineFactory
@Slf4j
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(PaymentState.NEW)
                .target(PaymentState.NEW)
                .event(PaymentEvent.PRE_AUTH)
                .action(preAuthAction())
                .and()
                .withExternal()
                .source(PaymentState.NEW)
                .target(PaymentState.PRE_AUTH)
                .event(PaymentEvent.PRE_AUTH_APPROVED)
                .and()
                .withExternal()
                .source(PaymentState.NEW)
                .target(PaymentState.PRE_AUTH_ERROR)
                .event(PaymentEvent.PRE_AUTH_DECLINED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {

        final StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State changed from {} to {}", from, to);
            }
        };
        config.withConfiguration().listener(adapter);
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> setActionForApproval(context);
    }

    private void setActionForApproval(StateContext<PaymentState, PaymentEvent> context) {
        System.out.println("PreAuth was called..!!");
        if (ThreadLocalRandom.current().nextInt(10) < 8) {
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED).setHeader(PaymentStateChangeInterceptor.PAYMENT_ID, context.getMessageHeader(PaymentStateChangeInterceptor
                    .PAYMENT_ID)).build());
        } else {
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED).setHeader(PaymentStateChangeInterceptor.PAYMENT_ID, context.getMessageHeader(PaymentStateChangeInterceptor
                    .PAYMENT_ID)).build());
        }
    }
}
