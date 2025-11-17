package com.example.solveitwebsitebackend.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class WeaknessMapper {

    public class RiskNames {
        public static final String risk1 = "INCOMP";
        public static final String risk2 = "INAC-EX";
        public static final String risk3 = "INAC-AS";
        public static final String risk4 = "INAC-ALT";
        public static final String risk5 = "INAC-COR";
        public static final String risk6 = "MISINT";
    }

    public static class RawWeakness {
        public String id;
        public String name;
        public String details;
        public String INCOMP;
        @JsonProperty("INAC-EX")
        public String INACEX;
        @JsonProperty("INAC-EX_comment")
        public String INACEX_comment;
        @JsonProperty("INAC-AS")
        public String INACAS;
        @JsonProperty("INAC-ALT")
        public String INACALT;
        @JsonProperty("INAC-COR")
        public String INACCOR;
        public String MISINT;
        public List<String> mitigations;
        public List<String> references;

        public RawWeakness() {}

        public RawWeakness(String id, String name, String details, String INCOMP, String INACEX, String INACEX_comment, String INACAS, String INACALT, String INACCOR, String MISINT, List<String> mitigations, List<String> references) {
            this.id = id;
            this.name = name;
            this.details = details;
            this.INCOMP = INCOMP;
            this.INACEX = INACEX;
            this.INACEX_comment = INACEX_comment;
            this.INACAS = INACAS;
            this.INACALT = INACALT;
            this.INACCOR = INACCOR;
            this.MISINT = MISINT;
            this.mitigations = mitigations;
            this.references = references;
        }
    }

    public static class NewWeakness {
        public String id;
        public String name;
        public String details;
        public List<String> risks;
        public List<String> mitigations;
        public List<String> references;

        public NewWeakness(String id, String name, String details, List<String> risks, List<String> mitigations, List<String> references) {
            this.id = id;
            this.name = name;
            this.details = details;
            this.risks = risks;
            this.mitigations = mitigations;
            this.references = references;
        }
    }

    public static NewWeakness map(RawWeakness raw) {
        List<String> risks = new ArrayList<>();

        if ("X".equalsIgnoreCase(raw.INCOMP)) risks.add(RiskNames.risk1);
        if ("X".equalsIgnoreCase(raw.INACEX)) risks.add(RiskNames.risk2);
        if ("X".equalsIgnoreCase(raw.INACAS)) risks.add(RiskNames.risk3);
        if ("X".equalsIgnoreCase(raw.INACALT)) risks.add(RiskNames.risk4);
        if ("X".equalsIgnoreCase(raw.INACCOR)) risks.add(RiskNames.risk5);
        if ("X".equalsIgnoreCase(raw.MISINT)) risks.add(RiskNames.risk6);

        return new NewWeakness(
                raw.id,
                raw.name,
                raw.details,
                risks,
                raw.mitigations,
                raw.references
        );
    }
}
