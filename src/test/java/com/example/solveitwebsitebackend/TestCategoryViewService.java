package com.example.solveitwebsitebackend;

import com.example.solveitwebsitebackend.mapper.CategoryViewMap;
import com.example.solveitwebsitebackend.service.CategoryViewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TestCategoryViewService {

    private Map<String, CategoryViewMap.CategoryView> oldCache;

    @Spy
    @InjectMocks
    private CategoryViewService categoryViewService;

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
    public void should_refresh_categoryView_cache_when_refreshCache_is_called() throws Exception{
        String dummyFilepath = "/json/dummyCategoryView.json";

        categoryViewService.refreshCache(dummyFilepath);

        verify(categoryViewService, times(1)).loadCategoryViews(dummyFilepath);

        assertNotNull(categoryViewService.getAllCategoryViews());
        assertEquals(2, categoryViewService.getAllCategoryViews().size());

        var dummyCategoryView1 = categoryViewService.getCategoryViewById("id1");
        var dummyCategoryView2 = categoryViewService.getCategoryViewById("id2");

        assertEquals("category2", dummyCategoryView1.categories.getLast());
        assertEquals("category3", dummyCategoryView2.categories.getFirst());
    }

    @Test
    public void should_print_error_three_times_and_keep_old_cache_when_an_Exception_is_caught() throws Exception {
        String invalidFilepath = "not_a_filepath";
        String exceptionMessage = "Refresh failed on attempt ";

        doThrow(new Exception("Failed to fetch")).when(categoryViewService).loadCategoryViews(invalidFilepath);

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            categoryViewService.refreshCache(invalidFilepath);
        } finally {
            System.setErr(originalErr);
        }

        verify(categoryViewService, times(3)).loadCategoryViews(invalidFilepath);

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
