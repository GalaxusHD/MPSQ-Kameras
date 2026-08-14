package de.galaxushd.mpsqcamera;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Client cache for the MPSQ Team API. */
public final class TeamStateStore {
    private static TeamProfile self;
    private static List<TeamProfile> members = List.of();

    private TeamStateStore() { }
    public static Optional<TeamProfile> self() { return Optional.ofNullable(self); }
    public static List<TeamProfile> members() { return members; }
    public static void setSelf(TeamProfile value) { self = value; }
    public static void setMembers(List<TeamProfile> value) { members = List.copyOf(new ArrayList<>(value)); }
}
