package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.CategoryViewDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryViewMap;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestCategoryViewDAO {

    private CategoryViewDAO SUT;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() throws  Exception {
        this.SUT = new CategoryViewDAO();

        Field restField = CategoryViewDAO.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(SUT, restTemplate);
    }

    @Test
    public void should_return_categoryViews_json_when_fetchCategoryViews_is_called() {
        String dummyUrl = "https://dummy.com";

        String dummyJson = """
                    [
                        {"id": "id1", "name": "name1", "short_description": "short1", "long_description": "long1", "categories": ["category1", "category2"]},
                        {"id": "id2", "name": "name2", "short_description": "short2", "long_description": "long2", "categories": ["category3", "category4"]}
                    ]
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyJson);

        List<CategoryViewMap.CategoryView> result = SUT.fetchCategoryViews(dummyUrl);

        assertThat(result, hasSize(2));

        assertThat(result.get(0), instanceOf(CategoryViewMap.CategoryView.class));
        assertThat(result.get(0).name, equalTo("name1"));
        assertThat(result.get(0).short_description, equalTo("short1"));
        assertThat(result.get(0).long_description, equalTo("long1"));
        assertThat(result.get(0).categories.get(0), equalTo("category1"));

        assertThat(result.get(1), instanceOf(CategoryViewMap.CategoryView.class));
        assertThat(result.get(1).name, equalTo("name2"));
        assertThat(result.get(1).short_description, equalTo("short2"));
        assertThat(result.get(1).long_description, equalTo("long2"));
        assertThat(result.get(1).categories.get(0), equalTo("category3"));

        verify(restTemplate).getForObject(dummyUrl, String.class);
        verify(restTemplate).getForObject(dummyUrl, String.class);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    public void should_return_fetch_exception_when_data_is_not_fetched() {
        String invalidUrl = "not_a_URL";

        when(restTemplate.getForObject(invalidUrl, String.class)).thenThrow(new RestClientException("Failed to fetch"));

        DAOExceptions.FetchException exception = assertThrows(DAOExceptions.FetchException.class, () -> SUT.fetchCategoryViews(invalidUrl));


        assertThat(exception.getMessage(), equalTo("Failed to fetch CategoryView JSON"));
        assertThat(exception.getCause(), instanceOf(RestClientException.class));
    }

    @Test
    public void should_return_parse_exception_when_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";

        String invalidJSON = """
                Not JSON
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidJSON);

        DAOExceptions.ParseException exception = assertThrows(DAOExceptions.ParseException.class, () -> SUT.fetchCategoryViews(dummyUrl));

        assertThat(exception.getMessage(), equalTo("Failed to parse CategoryView JSON"));
        assertThat(exception.getCause(), instanceOf(JsonProcessingException.class));
    }
}