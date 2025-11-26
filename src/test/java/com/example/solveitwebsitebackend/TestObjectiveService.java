package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.ObjectiveDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.ObjectiveMapper;
import com.example.solveitwebsitebackend.service.ObjectiveService;
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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestObjectiveService {

    private Map<String, ObjectiveMapper.NewObjective> oldCache;

    @Mock
    private ObjectiveDAO objectiveDAO;

    @InjectMocks
    private ObjectiveService objectiveService;

    @BeforeEach
    public void setup() throws Exception {
        List<String> dummyTechniques = List.of("technique5", "technique6");
        ObjectiveMapper.NewObjective dummyObjective = new ObjectiveMapper.NewObjective("id3", "name3", "description3", dummyTechniques);

        oldCache = new HashMap<>();
        oldCache.put(dummyObjective.id, dummyObjective);

        Field cacheField = ObjectiveService.class.getDeclaredField("objectiveCache");
        cacheField.setAccessible(true);
        cacheField.set(objectiveService, oldCache);
    }

    @Test
    public void should_refresh_objective_cache_when_refreshCache_is_called() {
        String dummyUrl = "https://dummy.com";

        List<String> techniques1 = List.of("technique1", "technique2");
        ObjectiveMapper.NewObjective dummyObjective1 = new ObjectiveMapper.NewObjective("id1", "name1", "description1", techniques1);

        List<String> techniques2 = List.of("technique3", "technique4");
        ObjectiveMapper.NewObjective dummyObjective2 = new ObjectiveMapper.NewObjective("id2", "name2", "description2", techniques2);

        when(objectiveDAO.mapFetchedObjectives(dummyUrl)).thenReturn(Arrays.asList(dummyObjective1, dummyObjective2));

        objectiveService.refreshCache(dummyUrl);

        verify(objectiveDAO, times(1)).mapFetchedObjectives(dummyUrl);

        assertNotNull(objectiveService.getAllObjectives());
        assertEquals(2, objectiveService.getAllObjectives().size());
        assertEquals(dummyObjective1, objectiveService.getObjectiveById("id1"));
        assertEquals(dummyObjective2, objectiveService.getObjectiveById("id2"));
    }

    @Test
    public void should_print_error_three_times_and_keep_old_cache_when_a_DAOException_is_caught()  {
        String invalidUrl = "not_a_URL";
        String exceptionMessage = "Refresh failed on attempt ";

        when(objectiveDAO.mapFetchedObjectives(invalidUrl)).thenThrow(new DAOExceptions.FetchException("Failed to fetch Objective JSON", new RestClientException("Failed to fetch")));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            objectiveService.refreshCache(invalidUrl);
        } finally {
            System.setErr(originalErr);
        }

        verify(objectiveDAO, times(3)).mapFetchedObjectives(invalidUrl);

        String output = errStream.toString();
        long count = output.lines().filter(line -> line.contains(exceptionMessage)).count();
        assertEquals(3, count);
        assertTrue(output.contains("All retries failed. Keeping existing objectives cache."));

        assertEquals(oldCache, objectiveService.getAllObjectives());
    }

    @Test
    public void should_throw_NOT_FOUND_exception_when_Objective_with_id_does_not_exist() {
        String invalidId = "invalid_id";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> objectiveService.getObjectiveById(invalidId));

        assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getMessage(), containsString("Couldn't find an Objective with id: invalid_id"));
    }
}
