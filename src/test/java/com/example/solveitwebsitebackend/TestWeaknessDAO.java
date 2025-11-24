package com.example.solveitwebsitebackend;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.solveitwebsitebackend.dao.WeaknessDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.WeaknessMapper;
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
public class TestWeaknessDAO {

    private ListAppender<ILoggingEvent> listAppender;

    private WeaknessDAO SUT;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() throws Exception {
        this.SUT = new WeaknessDAO();

        Field restField = WeaknessDAO.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(SUT, restTemplate);

        Logger logger = (Logger) LoggerFactory.getLogger(WeaknessDAO.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    public void should_return_raw_weakness_json_when_fetchRawWeaknesses_is_called() {
        String dummyUrl = "https://dummy.com";

        String dummyFilesArray = """
                [
                    {"type": "file", "name": "weakness1.json", "download_url": "https://dummy.com/weakness1.json"},
                    {"type": "file", "name": "weakness2.json", "download_url": "https://dummy.com/weakness2.json"}
                ]
                """;

        String dummyWeakness1Json = """
                    {"id": "id1", "name": "name1", "details": "details1", "INCOMP": "X", "INAC-EX": "", "INAC-EX_comment": "", "INAC-AS": "", "INAC-ALT": "", "INAC-COR": "", "MISINT": "", "mitigations": ["mitigation1", "mitigation2"], "references": ["reference1", "reference2"]}
                """;

        String dummyWeakness2Json = """
                    {"id": "id2", "name": "name2", "details": "details2", "INCOMP": "", "INAC-EX": "", "INAC-EX_comment": "", "INAC-AS": "X", "INAC-ALT": "", "INAC-COR": "", "MISINT": "", "mitigations": ["mitigation3", "mitigation4"], "references": ["reference3", "reference4"]}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyFilesArray);

        when(restTemplate.getForObject("https://dummy.com/weakness1.json", String.class)).thenReturn(dummyWeakness1Json);
        when(restTemplate.getForObject("https://dummy.com/weakness2.json", String.class)).thenReturn(dummyWeakness2Json);


        List<WeaknessMapper.RawWeakness> result = SUT.fetchRawWeaknesses(dummyUrl);

        assertThat(result, hasSize(2));

        assertThat(result.get(0), instanceOf(WeaknessMapper.RawWeakness.class));
        assertThat(result.get(0).name, equalTo("name1"));
        assertThat(result.get(0).details, equalTo("details1"));
        assertThat(result.get(0).INCOMP, equalTo("X"));

        assertThat(result.get(1), instanceOf(WeaknessMapper.RawWeakness.class));
        assertThat(result.get(1).name, equalTo("name2"));
        assertThat(result.get(1).details, equalTo("details2"));
        assertThat(result.get(1).INCOMP, equalTo(""));

        verify(restTemplate).getForObject(dummyUrl, String.class);
        verify(restTemplate).getForObject("https://dummy.com/weakness1.json", String.class);
        verify(restTemplate).getForObject("https://dummy.com/weakness2.json", String.class);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    public void should_return_fetch_exception_when_filesArray_data_is_not_fetched() {
        String invalidUrl = "not_a_URL";

        when(restTemplate.getForObject(invalidUrl, String.class)).thenThrow(new RestClientException("Failed to fetch"));

        DAOExceptions.FetchException exception = assertThrows(DAOExceptions.FetchException.class, () -> SUT.fetchRawWeaknesses(invalidUrl));

        assertThat(exception.getMessage(), equalTo("Failed to fetch Weakness filesArray JSON"));
        assertThat(exception.getCause(), instanceOf(RestClientException.class));
    }

    @Test
    public void should_return_parse_exception_when_filesArray_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String invalidFilesArray = """
                Not JSON
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidFilesArray);

        DAOExceptions.ParseException exception = assertThrows(DAOExceptions.ParseException.class, () -> SUT.fetchRawWeaknesses(dummyUrl));

        assertThat(exception.getMessage(), equalTo("Failed to parse Weakness filesArray JSON"));
        assertThat(exception.getCause(), instanceOf(JsonProcessingException.class));
    }

    @Test
    public void should_return_WARN_log_and_one_entry_when_individual_data_is_not_fetched() {
        String dummyUrl = "https://dummy.com";

        String invalidFilesArray = """
                [
                    {"type": "file", "name": "weakness1.json", "download_url": "https://invalid.com/weakness1.json"},
                    {"type": "file", "name": "weakness2.json", "download_url": "https://dummy.com/weakness2.json"}
                ]
                """;

        String dummyWeakness2Json = """
                    {"id": "id2", "name": "name2", "details": "details2", "INCOMP": "", "INAC-EX": "", "INAC-EX_comment": "", "INAC-AS": "X", "INAC-ALT": "", "INAC-COR": "", "MISINT": "", "mitigations": ["mitigation3", "mitigation4"], "references": ["reference3", "reference4"]}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidFilesArray);

        when(restTemplate.getForObject("https://invalid.com/weakness1.json", String.class)).thenThrow(new RestClientException("Failed to fetch"));
        when(restTemplate.getForObject("https://dummy.com/weakness2.json", String.class)).thenReturn(dummyWeakness2Json);

        List<WeaknessMapper.RawWeakness> result = SUT.fetchRawWeaknesses(dummyUrl);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), instanceOf(WeaknessMapper.RawWeakness.class));
        assertThat(result.get(0).name, equalTo("name2"));
        assertThat(result.get(0).details, equalTo("details2"));
        assertThat(result.get(0).INCOMP, equalTo(""));

        boolean found = listAppender.list.stream().anyMatch(event -> event.getLevel().toString().equals("WARN") && event.getMessage().contains("Skipping Weakness due to fetch error"));
        assertTrue(found);
    }

    @Test
    public void should_return_WARN_log_and_one_entry_when_individual_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String dummyFilesArray = """
                [
                    {"type": "file", "name": "weakness1.json", "download_url": "https://dummy.com/weakness1.json"},
                    {"type": "file", "name": "weakness2.json", "download_url": "https://dummy.com/weakness2.json"}
                ]
                """;

        String dummyWeakness1Json = """
                    {"id": "id1", "name": "name1", "details": "details1", "INCOMP": "X", "INAC-EX": "", "INAC-EX_comment": "", "INAC-AS": "", "INAC-ALT": "", "INAC-COR": "", "MISINT": "", "mitigations": ["mitigation1", "mitigation2"], "references": ["reference1", "reference2"]}
                """;

        String invalidWeakness2Json = """
                    {"wrong_key": "wrong_value"}
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyFilesArray);

        when(restTemplate.getForObject("https://dummy.com/weakness1.json", String.class)).thenReturn(dummyWeakness1Json);
        when(restTemplate.getForObject("https://dummy.com/weakness2.json", String.class)).thenReturn(invalidWeakness2Json);

        List<WeaknessMapper.RawWeakness> result = SUT.fetchRawWeaknesses(dummyUrl);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), instanceOf(WeaknessMapper.RawWeakness.class));
        assertThat(result.get(0).name, equalTo("name1"));
        assertThat(result.get(0).details, equalTo("details1"));
        assertThat(result.get(0).INCOMP, equalTo("X"));

        boolean found = listAppender.list.stream().anyMatch(event -> event.getLevel().toString().equals("WARN") && event.getMessage().contains("Skipping Weakness due to parse error"));
        assertTrue(found);
    }
}
