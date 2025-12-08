package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.MitigationDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.MitigationMapper;
import com.example.solveitwebsitebackend.service.MitigationService;
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
public class TestMitigationService {

    private Map<String, MitigationMapper.NewMitigation> oldCache;

    @Mock
    private MitigationDAO mitigationDAO;

    @InjectMocks
    private MitigationService mitigationService;

    @BeforeEach
    public void setup() throws Exception {
        List<String> dummyReferences3 = List.of("reference5", "reference6");
        MitigationMapper.NewMitigation dummyMitigation3 = new MitigationMapper.NewMitigation("id3", "name3", "technique3", dummyReferences3);

        oldCache = new HashMap<>();
        oldCache.put(dummyMitigation3.id, dummyMitigation3);

        Field cacheField = MitigationService.class.getDeclaredField("mitigationCache");
        cacheField.setAccessible(true);
        cacheField.set(mitigationService, oldCache);
    }

    @Test
    public void should_refresh_mitigation_cache_when_refreshCache_is_called() {
        String dummyUrl = "https://dummy.com";

        List<String> dummyReferences1 = List.of("reference1", "reference2");
        MitigationMapper.NewMitigation dummyMitigation1 = new MitigationMapper.NewMitigation("id1", "name1", "technique1", dummyReferences1);

        List<String> dummyReferences2 = List.of("reference3", "reference4");
        MitigationMapper.NewMitigation dummyMitigation2 = new MitigationMapper.NewMitigation("id2", "name2", "technique2", dummyReferences2);

        when(mitigationDAO.mapFetchedMitigations(dummyUrl)).thenReturn(Arrays.asList(dummyMitigation1, dummyMitigation2));

        mitigationService.refreshCache(dummyUrl);

        verify(mitigationDAO, times(1)).mapFetchedMitigations(dummyUrl);

        assertNotNull(mitigationService.getAllMitigations());
        assertEquals(2, mitigationService.getAllMitigations().size());
        assertEquals(dummyMitigation1, mitigationService.getMitigationById("id1"));
        assertEquals(dummyMitigation2, mitigationService.getMitigationById("id2"));
    }

    @Test
    public void should_print_error_three_times_and_keep_old_cache_when_a_DAOException_is_caught()  {
        String invalidUrl = "not_a_URL";
        String exceptionMessage = "Refresh failed on attempt ";

        when(mitigationDAO.mapFetchedMitigations(invalidUrl)).thenThrow(new DAOExceptions.FetchException("Failed to fetch Mitigation JSON", new RestClientException("Failed to fetch")));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            mitigationService.refreshCache(invalidUrl);
        } finally {
            System.setErr(originalErr);
        }

        verify(mitigationDAO, times(3)).mapFetchedMitigations(invalidUrl);

        String output = errStream.toString();
        long count = output.lines().filter(line -> line.contains(exceptionMessage)).count();
        assertEquals(3, count);
        assertTrue(output.contains("All retries failed. Keeping existing mitigations cache."));

        assertEquals(oldCache, mitigationService.getAllMitigations());
    }

    @Test
    public void should_throw_NOT_FOUND_exception_when_Mitigation_with_id_does_not_exist() {
        String invalidId = "invalid_id";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mitigationService.getMitigationById(invalidId));

        assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getMessage(), containsString("Couldn't find a Mitigation with id: invalid_id"));
    }
}
