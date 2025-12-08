package com.example.solveitwebsitebackend.mapper;

import java.util.List;

public class CategoryViewMap {
    public static class CategoryView {
        public String id;
        public String name;
        public String short_description;
        public String long_description;
        public List<String> categories;

        public CategoryView() {}

        public CategoryView(String id, String name, String short_description, String long_description, List<String> categories) {
            this.id = id;
            this.name = name;
            this.short_description = short_description;
            this.long_description = long_description;
            this.categories = categories;
        }
    }
}
