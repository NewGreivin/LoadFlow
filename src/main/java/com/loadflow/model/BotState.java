package com.loadflow.model;

public enum BotState {
    IDLE("En espera", "#718096"),
    CONNECTING("Conectando...", "#ECC94B"),
    LOGGING_IN("Autenticando...", "#ED8936"),
    CONFIGURATION("Configuración...", "#4299E1"),
    CONNECTED("Conectado (En Juego)", "#48BB78"),
    DISCONNECTING("Desconectando...", "#CBD5E0"),
    DISCONNECTED("Desconectado", "#A0AEC0"),
    KICKED("Expulsado", "#E53E3E"),
    ERROR("Error", "#F56565");

    private final String displayName;
    private final String hexColor;

    BotState(String displayName, String hexColor) {
        this.displayName = displayName;
        this.hexColor = hexColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHexColor() {
        return hexColor;
    }
}
