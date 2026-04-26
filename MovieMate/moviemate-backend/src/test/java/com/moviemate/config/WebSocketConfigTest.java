package com.moviemate.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    private WebSocketAuthInterceptor interceptor;
    private WebSocketConfig config;

    @BeforeEach
    void setUp() {
        interceptor = mock(WebSocketAuthInterceptor.class);
        config = new WebSocketConfig(interceptor);
    }

    @Test
    void configureMessageBroker_shouldConfigurePrefixes() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
        when(registry.enableSimpleBroker(any(String[].class))).thenReturn(mock(SimpleBrokerRegistration.class));

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    void registerStompEndpoints_shouldConfigureWsAndSockJs() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration endpoint = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint("/ws")).thenReturn(endpoint);
        when(endpoint.setAllowedOriginPatterns("*")).thenReturn(endpoint);
        when(endpoint.withSockJS()).thenReturn(mock(SockJsServiceRegistration.class));

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
        verify(endpoint).setAllowedOriginPatterns("*");
        verify(endpoint).withSockJS();
    }

    @Test
    void configureClientInboundChannel_shouldRegisterInterceptor() {
        ChannelRegistration registration = mock(ChannelRegistration.class);
        when(registration.interceptors(any())).thenReturn(registration);

        config.configureClientInboundChannel(registration);

        verify(registration).interceptors(interceptor);
    }
}
