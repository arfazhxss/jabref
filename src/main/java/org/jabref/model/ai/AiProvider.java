package org.jabref.model.ai;

import java.io.Serializable;

public enum AiProvider implements Serializable {
    OPEN_AI("OpenAI"),
    MISTRAL_AI("Mistral AI"),
    GEMINI("Gemini"),
    HUGGING_FACE("Hugging Face");

    private final String label;

    AiProvider(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return label;
    }
}

