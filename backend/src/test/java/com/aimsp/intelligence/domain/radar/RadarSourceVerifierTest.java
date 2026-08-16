package com.aimsp.intelligence.domain.radar;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RadarSourceVerifierTest {

    private final RadarSourceVerifier verifier = new RadarSourceVerifier();

    @Test
    void rejectsLoopbackAddress() {
        assertThat(verifier.check("http://127.0.0.1:8080/internal").status())
                .isEqualTo(RadarSourceVerifier.CheckStatus.REJECTED);
    }

    @Test
    void rejectsUnsupportedProtocol() {
        assertThat(verifier.check("file:///etc/passwd").status())
                .isEqualTo(RadarSourceVerifier.CheckStatus.REJECTED);
    }
}
