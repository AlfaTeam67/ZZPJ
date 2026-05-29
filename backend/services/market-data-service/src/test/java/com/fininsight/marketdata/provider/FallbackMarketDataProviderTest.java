package com.fininsight.marketdata.provider;

import com.fininsight.marketdata.exception.MarketDataUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FallbackMarketDataProviderTest {

    @Mock
    private FinnhubClient finnhubClient;

    @InjectMocks
    private FallbackMarketDataProvider provider;

    @Test
    void fetchQuote_delegatesToFinnhub() {
        FinnhubQuoteResponse expected = mock(FinnhubQuoteResponse.class);
        when(finnhubClient.fetchQuote("AAPL")).thenReturn(expected);

        assertThat(provider.fetchQuote("AAPL")).isSameAs(expected);
        verify(finnhubClient).fetchQuote("AAPL");
    }

    @Test
    void fallbackQuote_throwsMarketDataUnavailableException() {
        RuntimeException cause = new RuntimeException("connection refused");

        assertThatThrownBy(() -> provider.fallbackQuote("BTC", cause))
                .isInstanceOf(MarketDataUnavailableException.class)
                .hasMessageContaining("BTC")
                .hasCause(cause);
    }

    @Test
    void fetchQuote_whenFinnhubThrows_exceptionPropagates() {
        when(finnhubClient.fetchQuote("AAPL")).thenThrow(new FinnhubRateLimitException("AAPL"));

        assertThatThrownBy(() -> provider.fetchQuote("AAPL"))
                .isInstanceOf(FinnhubRateLimitException.class);
    }

    @Test
    void providerName_delegatesToFinnhub() {
        when(finnhubClient.providerName()).thenReturn("finnhub");

        assertThat(provider.providerName()).isEqualTo("finnhub");
    }
}
