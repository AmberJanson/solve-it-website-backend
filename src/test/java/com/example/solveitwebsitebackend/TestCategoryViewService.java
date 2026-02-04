package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.dao.CategoryViewDAO;
import com.example.solveitwebsitebackend.exceptions.DAOExceptions;
import com.example.solveitwebsitebackend.mapper.CategoryViewMap;
import com.example.solveitwebsitebackend.service.CategoryViewService;
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
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TestCategoryViewService {

    private Map<String, CategoryViewMap.CategoryView> oldCache;

    @Mock
    private CategoryViewDAO categoryViewDAO;

    @InjectMocks
    private  CategoryViewService categoryViewService;

    @BeforeEach
    public void setup() throws Exception {
        List<String> dummyCategories = List.of("category5", "category6");
        CategoryViewMap.CategoryView dummyCategoryView = new CategoryViewMap.CategoryView("id3", "name3", "short3", "long3", dummyCategories);

        oldCache = new HashMap<>();
        oldCache.put(dummyCategoryView.id, dummyCategoryView);

        Field cacheField = CategoryViewService.class.getDeclaredField("categoryViewCache");
        cacheField.setAccessible(true);
        cacheField.set(categoryViewService, oldCache);
    }

    @Test
    public void should_refresh_categoryView_cache_when_refreshCache_is_called() {
        String dummyUrl = "https://dummy.com";

        List<String> dummyCategories1 = List.of("category1", "category2");
        CategoryViewMap.CategoryView dummyCategoryView1 = new CategoryViewMap.CategoryView("id1", "name1", "short1", "long1", dummyCategories1);

        List<String> dummyCategories2 = List.of("category3", "category4");
        CategoryViewMap.CategoryView dummyCategoryView2 = new CategoryViewMap.CategoryView("id2", "name2", "short2", "long2", dummyCategories2);

        when(categoryViewDAO.fetchCategoryViews(dummyUrl)).thenReturn(Arrays.asList(dummyCategoryView1, dummyCategoryView2));

        categoryViewService.refreshCache(dummyUrl);

        verify(categoryViewDAO, times(1)).fetchCategoryViews(dummyUrl);

        assertNotNull(categoryViewService.getAllCategoryViews());
        assertEquals(2, categoryViewService.getAllCategoryViews().size());
        assertEquals(dummyCategoryView1, categoryViewService.getCategoryViewById("id1"));
        assertEquals(dummyCategoryView2, categoryViewService.getCategoryViewById("id2"));
    }

    @Test
    public void should_print_error_three_times_and_keep_old_cache_when_an_Exception_is_caught() {
        String invalidUrl = "not_a_URL";
        String exceptionMessage = "Refresh failed on attempt ";

        when(categoryViewDAO.fetchCategoryViews(invalidUrl)).thenThrow(new DAOExceptions.FetchException("Failed to fetch CategoryView JSON", new RestClientException("Failed to fetch")));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            categoryViewService.refreshCache(invalidUrl);
        } finally {
            System.setErr(originalErr);
        }

        verify(categoryViewDAO, times(3)).fetchCategoryViews(invalidUrl);

        String output = errStream.toString();
        long count = output.lines().filter(line -> line.contains(exceptionMessage)).count();
        assertEquals(3, count);
        assertTrue(output.contains("All retries failed. Keeping existing categoryViews cache."));

        assertEquals(oldCache, categoryViewService.getAllCategoryViews());
    }

    @Test
    public void should_throw_NOT_FOUND_exception_when_CategoryView_with_id_does_not_exist() {
        String invalidId = "invalid_id";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> categoryViewService.getCategoryViewById(invalidId));

        assertThat(exception.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getMessage(), containsString("Couldn't find an CategoryView with id: invalid_id"));
    }
}
