package ca.sqlpower.architect.profile;

public enum ProfileColumn {
    DATABASE("Database"),
    CATALOG("Catalog"),
    SCHEMA("Schema"),
    TABLE("Table"),
    COLUMN("Column"),
    RUNDATE("Run Date"),
    RECORD_COUNT("Record Count"),
    DATA_TYPE("Data Type"),
    NULL_COUNT("# Null"),
    PERCENT_NULL("% Null"),
    UNIQUE_COUNT("# Unique"),
    PERCENT_UNIQUE("% Unique"),
    MIN_LENGTH("Min Length"),
    MAX_LENGTH("Max Length"),
    AVERAGE_LENGTH("Avg. Length"),
    MIN_VALUE("Min Value"),
    MAX_VALUE("Max Value"),
    AVERAGE_VALUE("Avg. Value");

    String name;

    ProfileColumn(String name)  {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
