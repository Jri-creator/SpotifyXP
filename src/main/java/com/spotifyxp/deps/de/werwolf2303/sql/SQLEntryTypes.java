package com.spotifyxp.deps.de.werwolf2303.sql;

public enum SQLEntryTypes {
    STRING("TEXT"),
    INTEGER("INTEGER"),
    BOOLEAN("BIT"),
    DATE("DATE");

    private final String realType;

    SQLEntryTypes(String realType) {
        this.realType = realType;
    }

    public String getRealType() {
        return this.realType;
    }
}
