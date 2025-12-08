package com.example.solveitwebsitebackend.mapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectiveMapper {

    private static final AtomicInteger COUNTER = new AtomicInteger(1000);

    private static String generateID() {
        return "C" + COUNTER.getAndIncrement();
    }

    public static class RawObjective {
        public String name;
        public String description;
        public List<String> techniques;

        public RawObjective() {}

        public RawObjective(String name, String description, List<String> techniques) {
            this.name = name;
            this.description = description;
            this.techniques = techniques;
        }
    }

    public static class NewObjective {
        public String id;
        public String name;
        public String description;
        public List<String> techniques;

        public NewObjective(String id, String name, String description, List<String> techniques) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.techniques = techniques;
        }
    }

    public static NewObjective map(RawObjective raw) {
        return new NewObjective(
                generateID(),
                raw.name,
                raw.description,
                raw.techniques
        );
    }
}
