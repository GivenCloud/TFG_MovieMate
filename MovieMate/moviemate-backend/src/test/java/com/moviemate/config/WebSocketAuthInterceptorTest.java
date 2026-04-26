package com.moviemate.config;

import com.moviemate.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebSocketAuthInterceptorTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);
        interceptor = new WebSocketAuthInterceptor(jwtService, userDetailsService);
    }

    @Test
    void preSend_shouldSetPrincipalOnValidConnectToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer token");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        UserDetails userDetails = User.withUsername("ana").password("x").authorities("ROLE_USER").build();
        when(jwtService.extractUsername("token")).thenReturn("ana");
        when(userDetailsService.loadUserByUsername("ana")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token", userDetails)).thenReturn(true);

        Message<?> output = interceptor.preSend(message, mock(MessageChannel.class));
        StompHeaderAccessor outAccessor = MessageHeaderAccessor.getAccessor(output, StompHeaderAccessor.class);

        assertThat(outAccessor.getUser()).isNotNull();
        assertThat(outAccessor.getUser().getName()).isEqualTo("ana");
    }

    @Test
    void preSend_shouldIgnoreInvalidTokenWithoutThrowing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer token");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        when(jwtService.extractUsername("token")).thenThrow(new RuntimeException("bad token"));

        Message<?> output = interceptor.preSend(message, mock(MessageChannel.class));
        StompHeaderAccessor outAccessor = MessageHeaderAccessor.getAccessor(output, StompHeaderAccessor.class);

        assertThat(outAccessor.getUser()).isNull();
    }

    @Test
    void preSend_shouldSkipWhenNotConnect() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> output = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(output).isSameAs(message);
        verifyNoInteractions(jwtService, userDetailsService);
    }
}
