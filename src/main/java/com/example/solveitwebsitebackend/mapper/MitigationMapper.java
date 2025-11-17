package com.example.solveitwebsitebackend.mapper;

import java.util.List;

public class MitigationMapper {

    public static class RawMitigation {
        public String id;
        public String name;
        public String technique;
        public String techqniue; //Because of typo
        public List<String> references;

        public RawMitigation() {}

        public RawMitigation(String id, String name, String technique, String technqiue, List<String> references) {
            this.id = id;
            this.name = name;
            this.technique = technique;
            this.techqniue = technqiue;
            this.references = references;
        }
    }

    public static class NewMitigation {
        public String id;
        public String name;
        public String technique;
        public List<String> references;

        public NewMitigation(String id, String name, String technique, List<String> references) {
            this.id = id;
            this.name = name;
            this.technique = technique;
            this.references = references;
        }
    }

    public static NewMitigation map(RawMitigation raw) {
        String technique;
        if (raw.techqniue != null && raw.technique == null) {
            technique = raw.techqniue;
        } else {
            technique = raw.technique;
        }

        return new NewMitigation(
                raw.id,
                raw.name,
                technique,
                raw.references
        );
    }
}
