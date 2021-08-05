package org.kgrid.activator.constants;

public enum CustomProfiles {
    PROFILE("profile"),
    PROFILE_MINIMAL("\"minimal\"");

    private String value;

    CustomProfiles(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
