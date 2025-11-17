package com.example.solveitwebsitebackend.mapper;

import java.util.List;

public class TechniqueMapper {

    public static class RawTechnique {
        public String id;
        public String name;
        public String description;
        public List<String> synonyms;
        public String details;
        public List<String> subtechniques;
        public List<String> examples;
        public List<String> weaknesses;
        public List<String> CASE_output_classes;
        public List<String> references;

        public RawTechnique() {}

        public RawTechnique(String id, String name, String description, List<String> synonyms, String details, List<String> subtechniques, List<String> examples, List<String> weaknesses, List<String> CASE_output_classes, List<String> references) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.synonyms = synonyms;
            this.details = details;
            this.subtechniques = subtechniques;
            this.examples = examples;
            this.weaknesses = weaknesses;
            this.CASE_output_classes = CASE_output_classes;
            this.references = references;
        }
    }

    public static class NewTechnique {
        public String id;
        public String name;
        public String description;
        public List<String> synonyms;
        public String details;
        public List<String> subtechniques;
        public List<String> examples;
        public List<String> weaknesses;
        public List<String> CASE_output_classes;
        public List<String> references;

        public NewTechnique(String id, String name, String description, List<String> synonyms, String details, List<String> subtechniques, List<String> examples, List<String> weaknesses, List<String> CASE_output_classes, List<String> references) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.synonyms = synonyms;
            this.details = details;
            this.subtechniques = subtechniques;
            this.examples = examples;
            this.weaknesses = weaknesses;
            this.CASE_output_classes = CASE_output_classes;
            this.references = references;
        }
    }

    public static NewTechnique map(RawTechnique raw) {
        return new NewTechnique(
                raw.id,
                raw.name,
                raw.description,
                raw.synonyms,
                raw.details,
                raw.subtechniques,
                raw.examples,
                raw.weaknesses,
                raw.CASE_output_classes,
                raw.references
        );
    }
}
