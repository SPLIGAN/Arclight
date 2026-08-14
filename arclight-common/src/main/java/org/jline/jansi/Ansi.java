package org.jline.jansi;

/**
 * Minimal stub for Spigot {@code ColouredConsoleSender} on NeoForge.
 * Real jline is not shaded into the Arclight mod jar (module-info / service scan breaks);
 * console output is handled by {@code ColouredConsoleSenderMixin} → Log4j / TCA.
 */
public class Ansi {

    public enum Color {
        BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, DEFAULT
    }

    public enum Attribute {
        RESET,
        INTENSITY_BOLD,
        INTENSITY_FAINT,
        ITALIC,
        UNDERLINE,
        BLINK_SLOW,
        BLINK_FAST,
        NEGATIVE_ON,
        CONCEAL_ON,
        STRIKETHROUGH_ON,
        UNDERLINE_DOUBLE,
        INTENSITY_BOLD_OFF,
        ITALIC_OFF,
        UNDERLINE_OFF,
        BLINK_OFF,
        NEGATIVE_OFF,
        CONCEAL_OFF,
        STRIKETHROUGH_OFF
    }

    public static Ansi ansi() {
        return new Ansi();
    }

    public Ansi a(Attribute attribute) {
        return this;
    }

    public Ansi fg(Color color) {
        return this;
    }

    public Ansi fgBright(Color color) {
        return this;
    }

    public Ansi bg(Color color) {
        return this;
    }

    public Ansi bold() {
        return this;
    }

    public Ansi boldOff() {
        return this;
    }

    public Ansi reset() {
        return this;
    }

    @Override
    public String toString() {
        return "";
    }
}
