package br.com.btihelpbot.bti_api.service;

import br.com.btihelpbot.bti_api.dto.AnalyticsDTO;
import br.com.btihelpbot.bti_api.repository.CommandLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private CommandLogRepository commandLogRepository;

    @InjectMocks
    private MetricsService metricsService;

    @Test
    void getAnalytics_mapsRowsAndFillsAll24Hours() {
        when(commandLogRepository.analyticsOverTime(any())).thenReturn(List.<Object[]>of(
                new Object[]{ Date.valueOf("2025-08-17"), 12L, 5L }
        ));
        when(commandLogRepository.analyticsByHour(any())).thenReturn(List.<Object[]>of(
                new Object[]{ 21, 100L },
                new Object[]{ 9, 50L }
        ));
        // ordem da query: [private_count, group_count]
        when(commandLogRepository.analyticsChatType(any())).thenReturn(List.<Object[]>of(
                new Object[]{ 508L, 1200L }
        ));

        AnalyticsDTO dto = metricsService.getAnalytics(0);

        // overTime
        assertEquals(1, dto.overTime().size());
        assertEquals("2025-08-17", dto.overTime().get(0).date());
        assertEquals(12L, dto.overTime().get(0).commands());
        assertEquals(5L, dto.overTime().get(0).users());

        // byHour preenchido com 24 buckets, zeros onde nao havia dado
        assertEquals(24, dto.byHour().size());
        assertEquals(21, dto.byHour().get(21).hour());
        assertEquals(100L, dto.byHour().get(21).count());
        assertEquals(50L, dto.byHour().get(9).count());
        assertEquals(0L, dto.byHour().get(0).count());

        // chatType: group e private nas posicoes certas
        assertEquals(1200L, dto.chatType().group());
        assertEquals(508L, dto.chatType().privateChats());
    }
}
