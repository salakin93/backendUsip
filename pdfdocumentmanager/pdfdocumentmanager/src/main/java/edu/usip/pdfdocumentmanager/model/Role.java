package edu.usip.pdfdocumentmanager.model;

public enum Role {
    ROLE_ADMIN,
    ROLE_STUDENT;

    public String getDisplayName() {
        return switch (this) {
            case ROLE_ADMIN -> "Administrador";
            case ROLE_STUDENT -> "Estudiante";
        };
    }
}