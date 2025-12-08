package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.WeaknessDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
import com.example.solveitwebsitebackend.service.WeaknessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestWeaknessService {

    private Map<String, WeaknessMapper.NewWeakness> oldCache;

    @Mock
    private WeaknessDAO weaknessDAO;

    @InjectMocks
    private WeaknessService weaknessService;

    @BeforeEach
    public void setup() throws Exception {
        List<String> dummyRisks3 = List.of("risk5", "risk6");
        List<String> dummyMitigations3 = List.of("mitigation5", "mitigation6");
        List<String> dummyReferences3 = List.of("reference5", "reference6");
        WeaknessMapper.NewWeakness dummyWeakness3 = new WeaknessMapper.NewWeakness("id3", "name3", "details3", dummyRisks3, dummyMitigations3, dummyReferences3);

        oldCache = new HashMap<>();
        oldCache.put(dummyWeakness3.id, dummyWeakness3);

        Field cacheField = WeaknessService.class.getDeclaredField("weaknessCache");
        cacheField.setAccessible(true);
        cacheField.set(weaknessService, oldCache);
    }

    @Test
    public void should_refresh_weakness_cache_when_refreshCache_is_called() {
        String dummyUrl = "https://dummy.com";

        List<String> dummyRisks1 = List.of("risk1", "risk2");
        List<String> dummyMitigations1 = List.of("mitigation1", "mitigation2");
        List<String> dummyReferences1 = List.of("reference1", "reference2");
        WeaknessMapper.NewWeakness dummyWeakness1 = new WeaknessMapper.NewWeakness("id1", "name1", "details1", dummyRisks1, dummyMitigations1, dummyReferences1);

        List<String> dummyRisks2 = List.of("risk3", "risk4");
        List<String> dummyMitigations2 = List.of("mitigation3", "mitigation4");
        List<String> dummyReferences2 = List.of("reference3", "reference4");
        WeaknessMapper.NewWeakness dummyWeakness2 = new WeaknessMapper.NewWeakness("id2", "name2", "details2", dummyRisks2, dummyMitigations2, dummyReferences2);

        when(weaknessDAO.mapFetchedWeaknesses(dummyUrl)).thenReturn(Arrays.asList(dummyWeakness1, dummyWeakness2));

        weaknessService.refreshCache(dummyUrl);

        verify(weaknessDAO, times(1)).mapFetchedWeaknesses(dummyUrl);

        assertNotNull(weaknessService.getAllWeaknesses());
        assertEquals(2, weaknessService.getAllWeaknesses().size());
        assertEquals(dummyWeakness1, weaknessService.getWeaknessById("id1"));
        assertEquals(dummyWeakness2, weaknessService.getWeaknessById("id2"));
    }

    @Test
    public void should_print_error_three_times_and_keep_old_cache_when_a_DAOException_is_caught()  {
        String invalidUrl = "not_a_URL";
        String exceptionMessage = "Refresh failed on attempt ";

        when(weaknessDAO.mapFetchedWeaknesses(invalidUrl)).thenThrow(new DAOExceptions.FetchException("Failed to fetch Weakness JSON", new RestClientException("Failed to fetch")));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            weaknessService.refreshCache(invalidUrl);
        } finally {
            System.setErr(originalErr);
        }

        verify(weaknessDAO, times(3)).mapFetchedWeaknesses(invalidUrl);

        String output = errStream.toString();
        long count = output.lines().filter(line -> line.contains(exceptionMessage)).count();
        assertEquals(3, count);
        assertTrue(output.contains("All retries failed. Keeping existing weaknesses cache."));

        assertEquals(oldCache, weaknessService.getAllWeaknesses());
    }

    @Test
    public void should_throw_NOT_FOUND_exception_when_Weakness_with_id_does_not_exist() {
        String invalidId = "invalid_id";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> weaknessService.getWeaknessById(invalidId));

        assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getMessage(), containsString("Couldn't find a Weakness with id: invalid_id"));
    }
}
