package com.cruz.financialcontrol.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PepperedPasswordEncoderTest {

    @Mock
    private PasswordEncoder delegate;

    private PepperedPasswordEncoder pepperedPasswordEncoder;

    private static final String PEPPER = Base64.getEncoder().encodeToString("test-pepper-secret-32-bytes!!!!!".getBytes());

    @BeforeEach
    void setUp() {
        pepperedPasswordEncoder = new PepperedPasswordEncoder(delegate, PEPPER);
    }

    @Test
    void encode_shouldDelegateToUnderlyingEncoder_withPepperedValue() {
        when(delegate.encode(anyString())).thenReturn("hashed-value");

        String result = pepperedPasswordEncoder.encode("myPassword");

        assertThat(result).isEqualTo("hashed-value");
        verify(delegate).encode(anyString());
    }

    @Test
    void encode_shouldNeverPassRawPasswordDirectlyToDelegate() {
        when(delegate.encode(anyString())).thenReturn("hashed-value");

        pepperedPasswordEncoder.encode("myPassword");

        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(delegate).encode(captor.capture());
        assertThat(captor.getValue()).isNotEqualTo("myPassword");
    }

    @Test
    void applyingPepper_shouldBeDeterministic_forSameRawPassword() {
        when(delegate.encode(anyString())).thenReturn("hashed-value");

        pepperedPasswordEncoder.encode("myPassword");
        pepperedPasswordEncoder.encode("myPassword");

        // Both calls should have produced the identical HMAC-peppered value passed to the delegate.
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(delegate, org.mockito.Mockito.times(2)).encode(captor.capture());
        assertThat(captor.getAllValues().get(0)).isEqualTo(captor.getAllValues().get(1));
    }

    @Test
    void matches_shouldDelegateToUnderlyingEncoder_withPepperedRawPassword() {
        when(delegate.matches(anyString(), anyString())).thenReturn(true);

        boolean result = pepperedPasswordEncoder.matches("myPassword", "storedHash");

        assertThat(result).isTrue();
        verify(delegate).matches(anyString(), org.mockito.ArgumentMatchers.eq("storedHash"));
    }

    @Test
    void differentPeppers_shouldProduceDifferentPepperedValues_forSamePassword() {
        String otherPepper = Base64.getEncoder().encodeToString("a-completely-different-pepper!!!".getBytes());
        PepperedPasswordEncoder otherEncoder = new PepperedPasswordEncoder(delegate, otherPepper);

        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        when(delegate.encode(anyString())).thenReturn("hashed-value");

        pepperedPasswordEncoder.encode("myPassword");
        otherEncoder.encode("myPassword");

        verify(delegate, org.mockito.Mockito.times(2)).encode(captor.capture());
        assertThat(captor.getAllValues().get(0)).isNotEqualTo(captor.getAllValues().get(1));
    }
}
