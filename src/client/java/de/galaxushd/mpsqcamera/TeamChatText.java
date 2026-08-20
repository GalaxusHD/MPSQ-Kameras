package de.galaxushd.mpsqcamera;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Turns familiar ampersand Minecraft codes into safe client chat text. */
public final class TeamChatText {
    private TeamChatText() {
    }

    public static MutableText fromAmpersandCodes(String raw, Formatting initialColor) {
        MutableText result = Text.empty();
        String text = raw == null ? "" : raw;
        Formatting color = initialColor;
        EnumSet<Formatting> styles = EnumSet.noneOf(Formatting.class);
        StringBuilder part = new StringBuilder();

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '&' && index + 1 < text.length()) {
                Formatting code = formatting(text.charAt(index + 1));
                if (code != null) {
                    append(result, part, color, styles);
                    part.setLength(0);
                    if (code == Formatting.RESET) {
                        color = initialColor;
                        styles.clear();
                    } else if (code.isColor()) {
                        color = code;
                        styles.clear();
                    } else {
                        styles.add(code);
                    }
                    index++;
                    continue;
                }
            }
            part.append(current);
        }
        append(result, part, color, styles);
        return result;
    }

    private static void append(MutableText target, StringBuilder part, Formatting color, EnumSet<Formatting> styles) {
        if (part.isEmpty()) return;
        List<Formatting> formatting = new ArrayList<>(styles.size() + 1);
        formatting.add(color);
        formatting.addAll(styles);
        target.append(Text.literal(part.toString()).formatted(formatting.toArray(Formatting[]::new)));
    }

    private static Formatting formatting(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> Formatting.BLACK;
            case '1' -> Formatting.DARK_BLUE;
            case '2' -> Formatting.DARK_GREEN;
            case '3' -> Formatting.DARK_AQUA;
            case '4' -> Formatting.DARK_RED;
            case '5' -> Formatting.DARK_PURPLE;
            case '6' -> Formatting.GOLD;
            case '7' -> Formatting.GRAY;
            case '8' -> Formatting.DARK_GRAY;
            case '9' -> Formatting.BLUE;
            case 'a' -> Formatting.GREEN;
            case 'b' -> Formatting.AQUA;
            case 'c' -> Formatting.RED;
            case 'd' -> Formatting.LIGHT_PURPLE;
            case 'e' -> Formatting.YELLOW;
            case 'f' -> Formatting.WHITE;
            case 'k' -> Formatting.OBFUSCATED;
            case 'l' -> Formatting.BOLD;
            case 'm' -> Formatting.STRIKETHROUGH;
            case 'n' -> Formatting.UNDERLINE;
            case 'o' -> Formatting.ITALIC;
            case 'r' -> Formatting.RESET;
            default -> null;
        };
    }
}
