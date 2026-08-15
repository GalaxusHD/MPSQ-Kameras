package de.galaxushd.mpsqcamera;

import net.minecraft.text.Text;

/** The four MPSQ Team work lists. Sr Offizier belongs to OFFICER. */
public enum TeamTodoList {
    WORKER("arbeiter"),
    SOLDIER("soldat"),
    OFFICER("offizier"),
    FRONTMAN("frontman");

    private final String id;
    TeamTodoList(String id) { this.id = id; }
    public String id() { return id; }
    public Text title() { return Text.translatable("gui.mpsqcamera.team.todo.list." + id); }

    public static TeamTodoList fromId(String id) {
        for (TeamTodoList value : values()) if (value.id.equalsIgnoreCase(id)) return value;
        return WORKER;
    }

    public static TeamTodoList forRank(TeamRank rank) {
        return switch (rank) {
            case FRONTMAN -> FRONTMAN;
            case OFFICER, SENIOR_OFFICER -> OFFICER;
            case SOLDIER -> SOLDIER;
            default -> WORKER;
        };
    }
}
