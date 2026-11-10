package kz.school.security.patterns.events;
import java.util.UUID;
public record ReviewEvent(UUID guardianId,UUID relationshipId,String status,String comment) {}
