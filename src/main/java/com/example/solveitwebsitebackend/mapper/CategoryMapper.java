package com.example.solveitwebsitebackend.mapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryMapper {

    private static AtomicInteger COUNTER = new AtomicInteger(1000);
    private static AtomicInteger base_COUNTER = new AtomicInteger(1000);

    private static String generateID() {
        return "C" + COUNTER.getAndIncrement();
    }

    public void resetCounters() {
        COUNTER = new AtomicInteger(1000);
        base_COUNTER = new AtomicInteger(1000);
    }

    public void setCounterForNextView() {
        System.out.println("Before: " + base_COUNTER + "base and " + COUNTER + " normal");
        base_COUNTER.getAndAdd(50);
        COUNTER.set(base_COUNTER.get());
        System.out.println("After: " + base_COUNTER + "base and " + COUNTER + " normal");

    }

    public static class RawCategory {
        public String name;
        public String description;
        public List<String> techniques;

        public List<String> references;

        public RawCategory() {}

        public RawCategory(String name, String description, List<String> techniques, List<String> references) {
            this.name = name;
            this.description = description;
            this.techniques = techniques;
            this.references = references;
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
