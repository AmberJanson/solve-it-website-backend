package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.CategoryDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryMapper;
import com.example.solveitwebsitebackend.service.CategoryService;
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
public class TestCategoryService {

    private Map<String, CategoryMapper.NewCategory> oldCache;

    @Mock
    private CategoryDAO categoryDAO;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    public void setup() throws Exception {
        List<String> dummyTechniques = List.of("technique5", "technique6");
        CategoryMapper.NewCategory dummyCategory = new CategoryMapper.NewCategory("id3", "name3", "description3", dummyTechniques);

        oldCache = new HashMap<>();
        oldCache.put(dummyCategory.id, dummyCategory);

        Field cacheField = CategoryService.class.getDeclaredField("categoryCache");
        cacheField.setAccessible(true);
        cacheField.set(categoryService, oldCache);
    }

    @Test
    public void should_retrieve_single_category_cache_when_setRawCache_is_called() {
        String dummyUrl = "https://dummy.com";

        List<String> techniques1 = List.of("technique1", "technique2");
        CategoryMapper.RawCategory dummyCategory1 = new CategoryMapper.RawCategory("name1", "description1", techniques1);

        List<String> techniques2 = List.of("technique3", "technique4");
        CategoryMapper.RawCategory dummyCategory2 = new CategoryMapper.RawCategory("name2", "description2", techniques2);

        when(categoryDAO.fetchRawCategories(dummyUrl)).thenReturn(Arrays.asList(dummyCategory1, dummyCategory2));

        Map<String, CategoryMapper.RawCategory> result = categoryService.setRawCache(dummyUrl);

        verify(categoryDAO, times(1)).fetchRawCategories(dummyUrl);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dummyCategory1, result.get("name1"));
        assertEquals(dummyCategory2, result.get("name2"));
    }

    @Test
    public void should_print_error_three_times_and_return_null_when_a_DAOException_is_caught()  {
        String invalidUrl = "not_a_URL";
        String exceptionMessage = "Refresh failed on attempt ";

        when(categoryDAO.fetchRawCategories(invalidUrl)).thenThrow(new DAOExceptions.FetchException("Failed to fetch Category JSON", new RestClientException("Failed to fetch")));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        Map<String, CategoryMapper.RawCategory> result;
        try {
            result = categoryService.setRawCache(invalidUrl);
        } finally {
            System.setErr(originalErr);
        }

        verify(categoryDAO, times(3)).fetchRawCategories(invalidUrl);

        String output = errStream.toString();
        long count = output.lines().filter(line -> line.contains(exceptionMessage)).count();
        assertEquals(3, count);
        assertTrue(output.contains("All retries failed. Continue without this categories cache."));

        assertNull(result);
    }

    @Test
    public void should_update_category_cache_when_refreshCache_is_called() {
        CategoryMapper.RawCategory rawCategory1 = new CategoryMapper.RawCategory();
        CategoryMapper.RawCategory rawCategory2 = new CategoryMapper.RawCategory();
        Map<String, CategoryMapper.RawCategory> dummyCache = new HashMap<>();
        dummyCache.put("dummyId1", rawCategory1);
        dummyCache.put("dummyId2", rawCategory2);

        List<String> techniques1 = List.of("technique1", "technique2");
        CategoryMapper.NewCategory dummyCategory1 = new CategoryMapper.NewCategory("id1", "name1", "description1", techniques1);

        List<String> techniques2 = List.of("technique3", "technique4");
        CategoryMapper.NewCategory dummyCategory2 = new CategoryMapper.NewCategory("id2", "name2", "description2", techniques2);


        when(categoryDAO.mapFetchedCategories(dummyCache)).thenReturn(Arrays.asList(dummyCategory1, dummyCategory2));

        categoryService.refreshCache(dummyCache);

        verify(categoryDAO, times(1)).mapFetchedCategories(dummyCache);

        assertNotNull(categoryService.getAllCategories());
        assertEquals(2, categoryService.getAllCategories().size());
        assertEquals(dummyCategory1, categoryService.getCategoryById("id1"));
        assertEquals(dummyCategory2, categoryService.getCategoryById("id2"));
    }

    @Test
    public void should_keep_existing_cache_when_no_single_cache_was_retrieved() {
        Map<String, CategoryMapper.RawCategory> invalidCache = new HashMap<>();

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            categoryService.refreshCache(invalidCache);
        } finally {
            System.setErr(originalErr);
        }

        String output = errStream.toString();
        assertTrue(output.contains("No single cache was retrieved. Keeping existing categories cache"));

        assertEquals(oldCache, categoryService.getAllCategories());
    }

    @Test
    public void should_throw_NOT_FOUND_exception_when_Category_with_id_does_not_exist() {
        String invalidId = "invalid_id";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> categoryService.getCategoryById(invalidId));

        assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getMessage(), containsString("Couldn't find an Category with id: invalid_id"));
    }
}
