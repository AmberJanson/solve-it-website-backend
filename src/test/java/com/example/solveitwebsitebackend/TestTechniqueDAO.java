package com.example.solveitwebsitebackend;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.solveitwebsitebackend.dao.TechniqueDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.TechniqueMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestTechniqueDAO {

    private ListAppender<ILoggingEvent> listAppender;

    private TechniqueDAO SUT;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() throws Exception {
        this.SUT = new TechniqueDAO();

        Field restField = TechniqueDAO.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(SUT, restTemplate);

        Logger logger = (Logger) LoggerFactory.getLogger(TechniqueDAO.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    public void should_return_raw_techniques_json_when_fetchRawTechniques_is_called() {
        String dummyUrl = "https://dummy.com";

        String dummyFilesArray = """
                [
                    {"type": "file", "name": "technique1.json", "download_url": "https://dummy.com/technique1.json"},
                    {"type": "file", "name": "technique2.json", "download_url": "https://dummy.com/technique2.json"}
                ]
                """;

        String dummyTechnique1Json = """
                    {"id": "id1", "name": "name1", "description": "description1", "synonyms": ["synonym1", "synonym2"], "details": "details1", "subtechniques": ["subtechnique1", "subtechnique2"], "examples": ["example1", "example2"], "weaknesses": ["weakness1", "weakness2"], "CASE_output_classes": ["output_class1", "output_class2"], "references": ["reference1", "reference2"]}
                """;

        String dummyTechnique2Json = """
                    {"id": "id2", "name": "name2", "description": "description2", "synonyms": ["synonym3", "synonym4"], "details": "details2", "subtechniques": ["subtechnique3", "subtechnique4"], "examples": ["example3", "example4"], "weaknesses": ["weakness3", "weakness4"], "CASE_output_classes": ["output_class3", "output_class4"], "references": ["reference3", "reference4"]}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyFilesArray);

        when(restTemplate.getForObject("https://dummy.com/technique1.json", String.class)).thenReturn(dummyTechnique1Json);
        when(restTemplate.getForObject("https://dummy.com/technique2.json", String.class)).thenReturn(dummyTechnique2Json);


        List<TechniqueMapper.RawTechnique> result = SUT.fetchRawTechniques(dummyUrl);

        assertThat(result, hasSize(2));

        assertThat(result.get(0), instanceOf(TechniqueMapper.RawTechnique.class));
        assertThat(result.get(0).name, equalTo("name1"));
        assertThat(result.get(0).description, equalTo("description1"));
        assertThat(result.get(0).synonyms.get(0), equalTo("synonym1"));

        assertThat(result.get(1), instanceOf(TechniqueMapper.RawTechnique.class));
        assertThat(result.get(1).name, equalTo("name2"));
        assertThat(result.get(1).description, equalTo("description2"));
        assertThat(result.get(1).synonyms.get(0), equalTo("synonym3"));

        verify(restTemplate).getForObject(dummyUrl, String.class);
        verify(restTemplate).getForObject("https://dummy.com/technique1.json", String.class);
        verify(restTemplate).getForObject("https://dummy.com/technique2.json", String.class);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    public void should_return_fetch_exception_when_filesArray_data_is_not_fetched() {
        String invalidUrl = "not_a_URL";

        when(restTemplate.getForObject(invalidUrl, String.class)).thenThrow(new RestClientException("Failed to fetch"));

        DAOExceptions.FetchException exception = assertThrows(DAOExceptions.FetchException.class, () -> SUT.fetchRawTechniques(invalidUrl));

        assertThat(exception.getMessage(), equalTo("Failed to fetch Technique filesArray JSON"));
        assertThat(exception.getCause(), instanceOf(RestClientException.class));
    }

    @Test
    public void should_return_parse_exception_when_filesArray_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String invalidFilesArray = """
                Not JSON
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidFilesArray);

        DAOExceptions.ParseException exception = assertThrows(DAOExceptions.ParseException.class, () -> SUT.fetchRawTechniques(dummyUrl));

        assertThat(exception.getMessage(), equalTo("Failed to parse Technique filesArray JSON"));
        assertThat(exception.getCause(), instanceOf(JsonProcessingException.class));
    }

    @Test
    public void should_return_WARN_log_and_one_entry_when_individual_data_is_not_fetched() {
        String dummyUrl = "https://dummy.com";

        String invalidFilesArray = """
                [
                    {"type": "file", "name": "technique1.json", "download_url": "https://invalid.com/technique1.json"},
                    {"type": "file", "name": "technique2.json", "download_url": "https://dummy.com/technique2.json"}
                ]
                """;

        String dummyTechnique2Json = """
                    {"id": "id2", "name": "name2", "description": "description2", "synonyms": ["synonym3", "synonym4"], "details": "details2", "subtechniques": ["subtechnique3", "subtechnique4"], "examples": ["example3", "example4"], "weaknesses": ["weakness3", "weakness4"], "CASE_output_classes": ["output_class3", "output_class4"], "references": ["reference3", "reference4"]}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidFilesArray);

        when(restTemplate.getForObject("https://invalid.com/technique1.json", String.class)).thenThrow(new RestClientException("Failed to fetch"));
        when(restTemplate.getForObject("https://dummy.com/technique2.json", String.class)).thenReturn(dummyTechnique2Json);

        List<TechniqueMapper.RawTechnique> result = SUT.fetchRawTechniques(dummyUrl);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), instanceOf(TechniqueMapper.RawTechnique.class));
        assertThat(result.get(0).name, equalTo("name2"));
        assertThat(result.get(0).description, equalTo("description2"));
        assertThat(result.get(0).synonyms.get(0), equalTo("synonym3"));

        boolean found = listAppender.list.stream().anyMatch(event -> event.getLevel().toString().equals("WARN") && event.getMessage().contains("Skipping Technique due to fetch error"));
        assertTrue(found);
    }

    @Test
    public void should_return_WARN_log_and_one_entry_when_individual_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String dummyFilesArray = """
                [
                    {"type": "file", "name": "technique1.json", "download_url": "https://dummy.com/technique1.json"},
                    {"type": "file", "name": "technique2.json", "download_url": "https://dummy.com/technique2.json"}
                ]
                """;

        String dummyTechnique1Json = """
                    {"id": "id1", "name": "name1", "description": "description1", "synonyms": ["synonym1", "synonym2"], "details": "details1", "subtechniques": ["subtechnique1", "subtechnique2"], "examples": ["example1", "example2"], "weaknesses": ["weakness1", "weakness2"], "CASE_output_classes": ["output_class1", "output_class2"], "references": ["reference1", "reference2"]}
                """;

        String invalidTechnique2Json = """
                    {"wrong_key": "wrong_value"}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyFilesArray);

        when(restTemplate.getForObject("https://dummy.com/technique1.json", String.class)).thenReturn(dummyTechnique1Json);
        when(restTemplate.getForObject("https://dummy.com/technique2.json", String.class)).thenReturn(invalidTechnique2Json);

        List<TechniqueMapper.RawTechnique> result = SUT.fetchRawTechniques(dummyUrl);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), instanceOf(TechniqueMapper.RawTechnique.class));
        assertThat(result.get(0).name, equalTo("name1"));
        assertThat(result.get(0).description, equalTo("description1"));
        assertThat(result.get(0).synonyms.get(0), equalTo("synonym1"));

        boolean found = listAppender.list.stream().anyMatch(event -> event.getLevel().toString().equals("WARN") && event.getMessage().contains("Skipping Technique due to parse error"));
        assertTrue(found);
    }
}
