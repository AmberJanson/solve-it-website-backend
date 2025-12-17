package com.example.solveitwebsitebackend.mapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryMapper {

    private static final AtomicInteger COUNTER = new AtomicInteger(1000);

    private static String generateID() {
        return "C" + COUNTER.getAndIncrement();
    }

    public static class RawCategory {
        public String name;
        public String description;
        public List<String> techniques;

        public RawCategory() {}

        public RawCategory(String name, String description, List<String> techniques) {
            this.name = name;
            this.description = description;
            this.techniques = techniques;
        }
    }

    public static class NewCategory {
        public String id;
        public String name;
        public String description;
        public List<String> techniques;

        public NewCategory(String id, String name, String description, List<String> techniques) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.techniques = techniques;
        }
    }

    public static NewCategory map(RawCategory raw) {
        return new NewCategory(
                generateID(),
                raw.name,
                raw.description,
                raw.techniques
        );
    }
}
