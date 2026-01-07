package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.CategoryDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCategoryDAO {

    private CategoryDAO SUT;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() throws Exception {
        this.SUT = new CategoryDAO();

        Field restField = CategoryDAO.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(SUT, restTemplate);
    }

    @Test
    public void should_return_raw_categories_json_when_fetchRawCategories_is_called() {
        String dummyUrl = "https://dummy.com";
        String dummyJson = """
                [
                    {"name": "categoryname1", "description": "categorydescription1", "techniques": ["technique1", "technique2"]},
                    {"name": "categoryname2", "description": "categorydescription2", "techniques": ["technique3", "technique4"]}
                ]
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(dummyJson);

        List<CategoryMapper.RawCategory> result = SUT.fetchRawCategories(dummyUrl);

        assertThat(result, hasSize(2));

        assertThat(result.get(0), instanceOf(CategoryMapper.RawCategory.class));
        assertThat(result.get(0).name, equalTo("categoryname1"));
        assertThat(result.get(0).description, equalTo("categorydescription1"));
        assertThat(result.get(0).techniques.get(0), equalTo("technique1"));

        assertThat(result.get(1), instanceOf(CategoryMapper.RawCategory.class));
        assertThat(result.get(1).name, equalTo("categoryname2"));
        assertThat(result.get(1).description, equalTo("categorydescription2"));
        assertThat(result.get(1).techniques.get(0), equalTo("technique3"));
    }

    @Test
    public void should_return_fetch_exception_when_data_is_not_fetched() {
        String invalidUrl = "not_a_URL";

        when(restTemplate.getForObject(invalidUrl, String.class)).thenThrow(new RestClientException("Failed to fetch"));

        DAOExceptions.FetchException exception = assertThrows(DAOExceptions.FetchException.class, () -> SUT.fetchRawCategories(invalidUrl));

        assertThat(exception.getMessage(), equalTo("Failed to fetch Category JSON"));
        assertThat(exception.getCause(), instanceOf(RestClientException.class));
    }

    @Test
    public void should_return_parse_exception_when_data_is_not_in_right_format() {
        String dummyUrl = "https://dummy.com";
        String invalidJson = """
                [
                    {"not_name": "categoryname1", "not_description": "categorydescription1", "not_techniques": ["technique1", "technique2"]},
                    {"not_name": "categoryname2", "not_description": "categorydescription2", "not_techniques": ["technique3", "technique4"]}
                ]
                """;

        when(restTemplate.getForObject(dummyUrl, String.class)).thenReturn(invalidJson);

        DAOExceptions.ParseException exception = assertThrows(DAOExceptions.ParseException.class, () -> SUT.fetchRawCategories(dummyUrl));

        assertThat(exception.getMessage(), equalTo("Failed to parse Category JSON"));
        assertThat(exception.getCause(), instanceOf(JsonProcessingException.class));
    }

    @Test
    public void should_map_raw_categories_to_new_categories_when_mapFetchedCategories_is_called() {
        CategoryMapper.RawCategory rawCategory1 = new CategoryMapper.RawCategory();
        CategoryMapper.RawCategory rawCategory2 = new CategoryMapper.RawCategory();
        Map<String, CategoryMapper.RawCategory> dummyCache = new HashMap<>();
        dummyCache.put("dummyId1", rawCategory1);
        dummyCache.put("dummyId2", rawCategory2);

        CategoryDAO spyDAO = Mockito.spy(SUT);

        List<CategoryMapper.NewCategory> result = spyDAO.mapFetchedCategories(dummyCache);

        assertThat(result, hasSize(2));
        result.forEach(entry -> assertThat(entry, instanceOf(CategoryMapper.NewCategory.class)));
    }
}
