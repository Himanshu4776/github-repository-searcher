package io.reflectoring.demo.Constants;

public enum RepositorySortingModel {
    STARS("stars"),
    FORKS("forks"),
    UPDATED("updated");

    private final String value;

    RepositorySortingModel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
