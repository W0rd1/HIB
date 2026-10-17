package org.example.service;

public final class ConsoleStyle {
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[96m";
    private static final String GREEN = "\u001B[92m";
    private static final String RED = "\u001B[91m";
    private static final String YELLOW = "\u001B[93m";
    private static final String MAGENTA = "\u001B[95m";

    private ConsoleStyle() {}

    public static String title(String text) { return CYAN + text + RESET; }
    public static String success(String text) { return GREEN + text + RESET; }
    public static String error(String text) { return RED + text + RESET; }
    public static String prompt(String text) { return YELLOW + text + RESET; }
    public static String highlight(String text) { return MAGENTA + text + RESET; }
}
