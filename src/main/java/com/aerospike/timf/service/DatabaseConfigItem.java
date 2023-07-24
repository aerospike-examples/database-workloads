package com.aerospike.timf.service;

import lombok.Data;

@Data
public class DatabaseConfigItem {
    public static enum Type {
        STRING,
        INTEGER,
        BOOLEAN,
        PASSWORD,
        SELECTION
    }
    private final String name;
    private final String label;
    private final String description;
    private final String promptText;
    private final Type type;
    private final String[] selectionOptions;
    private final boolean required;
    private final Object defaultValue;
    
    
    public DatabaseConfigItem(String name, String label, String description, Type type) {
        this(name, label, description, "", type, null, true, null);
    }

    public DatabaseConfigItem(String name, String label, String description, int defaultValue) {
        this(name, label, description, "", Type.INTEGER, null, true, defaultValue);
    }

    public DatabaseConfigItem(String name, String label, String description, String prompt, int defaultValue) {
        this(name, label, description, prompt, Type.INTEGER, null, true, defaultValue);
    }

    public DatabaseConfigItem(String name, String label, String description, String defaultValue) {
        this(name, label, description, "", Type.STRING, null, true, defaultValue);
    }

    public DatabaseConfigItem(String name, String label, String description, Type type, boolean required) {
        this(name, label, description, "", type, null, required, null);
    }

    public DatabaseConfigItem(String name, String label, String description, String prompt, Type type) {
        this(name, label, description, label, type, null, true, null);
    }

    public DatabaseConfigItem(String name, String label, String description, String prompt, Type type, boolean required) {
        this(name, label, description, prompt, type, null, required, null);
    }

    public DatabaseConfigItem(String name, String label, String description, String[] options) {
        this(name, label, description, label, Type.SELECTION, options, true, null);
    }

    public DatabaseConfigItem(String name, String label, String description, boolean defaultValue) {
        this(name, label, description, label, Type.BOOLEAN, null, true, defaultValue);
    }

    private DatabaseConfigItem(String name, String label, String description, String promptText, Type type, String[] selectionOptions,
            boolean required, Object defaultValue) {
        super();
        this.name = name;
        this.label = label;
        this.description = description;
        this.promptText = promptText;
        this.type = type;
        this.selectionOptions = selectionOptions;
        this.required = required;
        this.defaultValue = defaultValue;
    }
}
