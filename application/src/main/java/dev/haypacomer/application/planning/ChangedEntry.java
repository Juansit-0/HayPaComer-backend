package dev.haypacomer.application.planning;

import dev.haypacomer.domain.planning.PlanEntry;
import java.time.LocalDate;

public record ChangedEntry(PlanEntry entry, LocalDate date) {}
