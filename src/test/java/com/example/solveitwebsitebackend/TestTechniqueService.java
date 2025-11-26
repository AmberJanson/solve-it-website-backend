package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.TechniqueDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import com.example.solveitwebsitebackend.service.TechniqueService;
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
public class TestTechniqueService {

    private Map<String, TechniqueMapper.NewTechnique> oldCache;

    @Mock
    private TechniqueDAO techniqueDAO;

    @InjectMocks
    private TechniqueService techniqueService;

    @BeforeEach
    public void setup() throws Exception {
        List<String> dummySynonyms3 = List.of("synonym5", "synonym6");
        List<String> dummySubtechniques3 = List.of("subtechnique5", "subtechnique6");
        List<String> dummyExamples3 = List.of("example5", "example6");
        List<String> dummyWeaknesses3 = List.of("weakness5", "weakness6");
        List<String> dummyCASE_output_classes3 = List.of("CASE_output_class5", "CASE_output_class6");
        List<String> dummyReferences3 = List.of("reference5", "reference6");
        TechniqueMapper.NewTechnique dummyTechnique3 = new TechniqueMapper.NewTechnique("id3", "name3", "description3", dummySynonyms3, "details3", dummySubtechniques3, dummyExamples3, dummyWeaknesses3, dummyCASE_output_classes3, dummyReferences3);

        oldCache = new HashMap<>();
        oldCache.put(dummyTechnique3.id, dummyTechnique3);

        Field cacheField = TechniqueService.class.getDeclaredField("techniqueCache");
        cacheField.setAccessible(true);
        cacheField.set(techniqueService, oldCache);
    }

    @Test
    public void should_refresh_technique_cache_when_refreshCache_is_called() {
        String dummyUrl = "https://dummy.com";

        List<String> dummySynonyms1 = List.of("synonym1", "synonym2");
        List<String> dummySubtechniques1 = List.of("subtechnique1", "subtechnique2");
        List<String> dummyExamples1 = List.of("example1", "example2");
        List<String> dummyWeaknesses1 = List.of("weakness1", "weakness2");
        List<String> dummyCASE_output_classes1 = List.of("CASE_output_class1", "CASE_output_class2");
        List<String> dummyReferences1 = List.of("reference1", "reference2");
        TechniqueMapper.NewTechnique dummyTechnique1 = new TechniqueMapper.NewTechnique("id1", "name1", "description1", dummySynonyms1, "details1", dummySubtechniques1, dummyExamples1, dummyWeaknesses1, dummyCASE_output_classes1, dummyReferences1);

        List<String> dummySynonyms2 = List.of("synonym3", "synonym4");
        List<String> dummySubtechniques2 = List.of("subtechnique3", "subtechnique4");
        List<String> dummyExamples2 = List.of("example3", "example4");
        List<String> dummyWeaknesses2 = List.of("weakness3", "weakness4");
        List<String> dummyCASE_output_classes2 = List.of("CASE_output_class3", "CASE_output_class4");
        List<String> dummyReferences2 = List.of("reference3", "reference4");
        TechniqueMapper.NewTechnique dummyTechnique2 = new TechniqueMapper.NewTechnique("id2", "name2", "description2", dummySynonyms2, "details2", dummySubtechniques2, dummyExamples2, dummyWeaknesses2, dummyCASE_output_classes2, dummyReferences2);

        when(techniqueDAO.mapFetchedTechniques(dummyUrl)).thenReturn(Arrays.asList(dummyTechnique1, dummyTechnique2));

        techniqueService.refreshCache(dummyUrl);

        verify(techniqueDAO, times(1)).mapFetchedTechniques(dummyUrl);

        assertNotNull(techniqueService.getAllTechniques());
        assertEquals(2, techniqueService.getAllTechniques().size());
        assertEquals(dummyTechnique1, techniqueService.getTechniqueById("id1"));
        assertEquals(dummyTechnique2, techniqueService.getTechniqueById("id2"));
    }

    @Test
    public void should_print_error_three_times_and_keep_old_cache_when_a_DAOException_is_caught()  {
        String invalidUrl = "not_a_URL";
        String exceptionMessage = "Refresh failed on attempt ";

        when(techniqueDAO.mapFetchedTechniques(invalidUrl)).thenThrow(new DAOExceptions.FetchException("Failed to fetch Technique JSON", new RestClientException("Failed to fetch")));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            techniqueService.refreshCache(invalidUrl);
        } finally {
            System.setErr(originalErr);
        }

        verify(techniqueDAO, times(3)).mapFetchedTechniques(invalidUrl);

        String output = errStream.toString();
        long count = output.lines().filter(line -> line.contains(exceptionMessage)).count();
        assertEquals(3, count);
        assertTrue(output.contains("All retries failed. Keeping existing techniques cache."));

        assertEquals(oldCache, techniqueService.getAllTechniques());
    }

    @Test
    public void should_throw_NOT_FOUND_exception_when_Technique__with_id_does_not_exist() {
        String invalidId = "invalid_id";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> techniqueService.getTechniqueById(invalidId));

        assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getMessage(), containsString("Couldn't find a Technique with id: invalid_id"));
    }
}
