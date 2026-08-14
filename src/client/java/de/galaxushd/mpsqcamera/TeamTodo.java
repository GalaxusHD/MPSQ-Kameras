package de.galaxushd.mpsqcamera;

import java.util.UUID;

/** One shared, persisted event task. */
public record TeamTodo(UUID id, String text, boolean done) { }
