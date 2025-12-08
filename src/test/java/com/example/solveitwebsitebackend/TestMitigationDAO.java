package com.example.solveitwebsitebackend;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.solveitwebsitebackend.dao.MitigationDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.MitigationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestMitigationDAO {

    private ListAppender<ILoggingEvent> listAppender;

    private MitigationDAO SUT;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() throws Exception {
        this.SUT = new MitigationDAO();

        Field restField = MitigationDAO.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(SUT, restTemplate);

        Logger logger = (Logger) LoggerFactory.getLogger(MitigationDAO.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    public void should_return_raw_mitigation_json_when_fetchRawMitigation_is_called() {
        String dummyUrl = "https://dummy.com";

        String dummyFilesArray = """
                [
                    {"type": "file", "name": "mitigation1.json", "download_url": "https://dummy.com/mitigation1.json"},
                    {"type": "file", "name": "mitigation2.json", "download_url": "https://dummy.com/mitigation2.json"}
                ]
                """;

        String dummyMitigation1Json = """
                    {"id": "id1", "name": "name1", "technique": "technique1", "references": ["reference1", "reference2"]}
                """;

        String dummyMitigation2Json = """
                    {"id": "id2", "name": "name2", "techqniue": "technique2", "references": ["reference3", "reference4"]}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyFilesArray);

        when(restTemplate.getForObject("https://dummy.com/mitigation1.json", String.class)).thenReturn(dummyMitigation1Json);
        when(restTemplate.getForObject("https://dummy.com/mitigation2.json", String.class)).thenReturn(dummyMitigation2Json);


        List<MitigationMapper.RawMitigation> result = SUT.fetchRawMitigations(dummyUrl);

        assertThat(result, hasSize(2));

        assertThat(result.get(0), instanceOf(MitigationMapper.RawMitigation.class));
        assertThat(result.get(0).id, equalTo("id1"));
        assertThat(result.get(0).technique, equalTo("technique1"));
        assertThat(result.get(0).references.get(0), equalTo("reference1"));

        assertThat(result.get(1), instanceOf(MitigationMapper.RawMitigation.class));
        assertThat(result.get(1).id, equalTo("id2"));
        assertThat(result.get(1).techqniue, equalTo("technique2"));
        assertThat(result.get(1).references.get(1), equalTo("reference4"));

        verify(restTemplate).getForObject(dummyUrl, String.class);
        verify(restTemplate).getForObject("https://dummy.com/mitigation1.json", String.class);
        verify(restTemplate).getForObject("https://dummy.com/mitigation2.json", String.class);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    public void should_return_fetch_exception_when_filesArray_data_is_not_fetched() {
        String invalidUrl = "not_a_URL";

        when(restTemplate.getForObject(invalidUrl, String.class)).thenThrow(new RestClientException("Failed to fetch"));

        DAOExceptions.FetchException exception = assertThrows(DAOExceptions.FetchException.class, () -> SUT.fetchRawMitigations(invalidUrl));

        assertThat(exception.getMessage(), equalTo("Failed to fetch Mitigation filesArray JSON"));
        assertThat(exception.getCause(), instanceOf(RestClientException.class));
    }

    @Test
    public void should_return_parse_exception_when_filesArray_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String invalidFilesArray = """
                Not JSON
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidFilesArray);

        DAOExceptions.ParseException exception = assertThrows(DAOExceptions.ParseException.class, () -> SUT.fetchRawMitigations(dummyUrl));

        assertThat(exception.getMessage(), equalTo("Failed to parse Mitigation filesArray JSON"));
        assertThat(exception.getCause(), instanceOf(JsonProcessingException.class));
    }

    @Test
    public void should_return_WARN_log_and_one_entry_when_individual_data_is_not_fetched() {
        String dummyUrl = "https://dummy.com";

        String invalidFilesArray = """
                [
                    {"type": "file", "name": "mitigation1.json", "download_url": "https://invalid.com/mitigation1.json"},
                    {"type": "file", "name": "mitigation2.json", "download_url": "https://dummy.com/mitigation2.json"}
                ]
                """;

        String dummyMitigation2Json = """
                    {"id": "id2", "name": "name2", "techqniue": "technique2", "references": ["reference3", "reference4"]}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidFilesArray);

        when(restTemplate.getForObject("https://invalid.com/mitigation1.json", String.class)).thenThrow(new RestClientException("Failed to fetch"));
        when(restTemplate.getForObject("https://dummy.com/mitigation2.json", String.class)).thenReturn(dummyMitigation2Json);

        List<MitigationMapper.RawMitigation> result = SUT.fetchRawMitigations(dummyUrl);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), instanceOf(MitigationMapper.RawMitigation.class));
        assertThat(result.get(0).id, equalTo("id2"));
        assertThat(result.get(0).techqniue, equalTo("technique2"));
        assertThat(result.get(0).references.get(1), equalTo("reference4"));

        boolean found = listAppender.list.stream().anyMatch(event -> event.getLevel().toString().equals("WARN") && event.getMessage().contains("Skipping Mitigation due to fetch error"));
        assertTrue(found);
    }

    @Test
    public void should_return_WARN_log_and_one_entry_when_individual_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String dummyFilesArray = """
                [
                    {"type": "file", "name": "mitigation1.json", "download_url": "https://dummy.com/mitigation1.json"},
                    {"type": "file", "name": "mitigation2.json", "download_url": "https://dummy.com/mitigation2.json"}
                ]
                """;

        String dummyMitigation1Json = """
                    {"id": "id1", "name": "name1", "technique": "technique1", "references": ["reference1", "reference2"]}
                """;

        String invalidMitigation2Json = """
                    {"wrong_key": "wrong_value"}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyFilesArray);

        when(restTemplate.getForObject("https://dummy.com/mitigation1.json", String.class)).thenReturn(dummyMitigation1Json);
        when(restTemplate.getForObject("https://dummy.com/mitigation2.json", String.class)).thenReturn(invalidMitigation2Json);

        List<MitigationMapper.RawMitigation> result = SUT.fetchRawMitigations(dummyUrl);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), instanceOf(MitigationMapper.RawMitigation.class));
        assertThat(result.get(0).id, equalTo("id1"));
        assertThat(result.get(0).technique, equalTo("technique1"));
        assertThat(result.get(0).references.get(0), equalTo("reference1"));

        boolean found = listAppender.list.stream().anyMatch(event -> event.getLevel().toString().equals("WARN") && event.getMessage().contains("Skipping Mitigation due to parse error"));
        assertTrue(found);
    }
}
