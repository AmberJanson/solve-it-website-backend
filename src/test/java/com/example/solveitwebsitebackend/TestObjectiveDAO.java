package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.ObjectiveDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.ObjectiveMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestObjectiveDAO {

    private ObjectiveDAO SUT;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() throws Exception {
        this.SUT = new ObjectiveDAO();

        Field restField = ObjectiveDAO.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(SUT, restTemplate);
    }

    @Test
    public void should_return_raw_objectives_json_when_fetchRawObjectives_is_called() {
        String dummyUrl = "https://dummy.com";
        String dummyJson = """
                [
                    {"name": "objectivename1", "description": "objectivedescription1", "techniques": ["technique1", "technique2"]},
                    {"name": "objectivename2", "description": "objectivedescription2", "techniques": ["technique3", "technique4"]}
                ]
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyJson);

        List<ObjectiveMapper.RawObjective> result = SUT.fetchRawObjectives(dummyUrl);

        assertThat(result, hasSize(2));

        assertThat(result.get(0), instanceOf(ObjectiveMapper.RawObjective.class));
        assertThat(result.get(0).name, equalTo("objectivename1"));
        assertThat(result.get(0).description, equalTo("objectivedescription1"));
        assertThat(result.get(0).techniques.get(0), equalTo("technique1"));

        assertThat(result.get(1), instanceOf(ObjectiveMapper.RawObjective.class));
        assertThat(result.get(1).name, equalTo("objectivename2"));
        assertThat(result.get(1).description, equalTo("objectivedescription2"));
        assertThat(result.get(1).techniques.get(0), equalTo("technique3"));
    }

    @Test
    public void should_return_fetch_exception_when_data_is_not_fetched() {
        String invalidUrl = "not_a_URL";

        when(restTemplate.getForObject(invalidUrl, String.class)).thenThrow(new RestClientException("Failed to fetch"));

        DAOExceptions.FetchException exception = assertThrows(DAOExceptions.FetchException.class, () -> SUT.fetchRawObjectives(invalidUrl));

        assertThat(exception.getMessage(), equalTo("Failed to fetch Objective JSON"));
        assertThat(exception.getCause(), instanceOf(RestClientException.class));
    }

    @Test
    public void should_return_parse_exception_when_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";
        String invalidJson = """
                [
                    {"not_name": "objectivename1", "not_description": "objectivedescription1", "not_techniques": ["technique1", "technique2"]},
                    {"not_name": "objectivename2", "not_description": "objectivedescription2", "not_techniques": ["technique3", "technique4"]}
                ]
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidJson);

        DAOExceptions.ParseException exception = assertThrows(DAOExceptions.ParseException.class, () -> SUT.fetchRawObjectives(dummyUrl));

        assertThat(exception.getMessage(), equalTo("Failed to parse Objective JSON"));
        assertThat(exception.getCause(), instanceOf(JsonProcessingException.class));
    }

//    @Test
//    public void should_map_raw_objectives_to_new_objectives_when_mapFetchedObjectives_is_called() {
//        String dummyUrl = "https://dummy.com";
//        ObjectiveMapper.RawObjective rawObjective1 = new ObjectiveMapper.RawObjective();
//        ObjectiveMapper.RawObjective rawObjective2 = new ObjectiveMapper.RawObjective();
//
//        ObjectiveDAO spyDAO = Mockito.spy(SUT);
//        doReturn(List.of(rawObjective1, rawObjective2)).when(spyDAO).fetchRawObjectives(dummyUrl);
//
//        List<ObjectiveMapper.NewObjective> result = spyDAO.mapFetchedObjectives(dummyUrl);
//
//        assertThat(result, hasSize(2));
//        result.forEach(entry -> assertThat(entry, instanceOf(ObjectiveMapper.NewObjective.class)));
//    }
}
