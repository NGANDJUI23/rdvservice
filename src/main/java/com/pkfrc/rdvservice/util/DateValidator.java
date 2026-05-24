package com.pkfrc.rdvservice.util;


import java.time.LocalDateTime;
import java.util.List;

public class DateValidator {

    private static final long MIN_DAYS_BEFORE = 2;
    private static final long MIN_HOURS_BEFORE = MIN_DAYS_BEFORE * 24;
    private static final int SERVICE_DURATION_HOURS = 1; // Durée fixe de 1 heure

    /**
     * Vérifie si la date du rendez-vous est valide (au moins 2 jours dans le futur)
     */
    public static boolean isValidAppointmentDate(LocalDateTime appointmentDate) {
        if (appointmentDate == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minDate = now.plusDays(MIN_DAYS_BEFORE);

        return appointmentDate.isAfter(minDate) || appointmentDate.isEqual(minDate);
    }

    /**
     * Vérifie si la date du rendez-vous est au moins 2 jours après la date actuelle
     */
    public static boolean isAtLeastTwoDaysInFuture(LocalDateTime appointmentDate) {
        if (appointmentDate == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(MIN_DAYS_BEFORE);

        return !appointmentDate.isBefore(twoDaysFromNow);
    }

    /**
     * Calcule la fin d'un rendez-vous (début + 1 heure)
     */
    public static LocalDateTime calculateEndTime(LocalDateTime startDate) {
        return startDate.plusHours(SERVICE_DURATION_HOURS);
    }

    /**
     * Calcule le nombre de jours entre maintenant et la date du rendez-vous
     */
    public static long getDaysUntilAppointment(LocalDateTime appointmentDate) {
        if (appointmentDate == null) {
            return -1;
        }

        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, appointmentDate).toDays();
    }

    /**
     * Obtient la date minimale acceptable pour un rendez-vous
     */
    public static LocalDateTime getMinAppointmentDate() {
        return LocalDateTime.now().plusDays(MIN_DAYS_BEFORE);
    }

    /**
     * Vérifie si deux plages horaires se chevauchent
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Vérifie si un nouveau rendez-vous chevauche des rendez-vous existants
     */
    public static boolean hasOverlapWithAny(LocalDateTime newStart, LocalDateTime newEnd,
                                            List<ExistingAppointment> existingAppointments) {
        for (ExistingAppointment existing : existingAppointments) {
            if (isOverlapping(newStart, newEnd, existing.startTime(), existing.endTime())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trouve le rendez-vous conflictuel
     */
    public static ExistingAppointment findOverlappingAppointment(LocalDateTime newStart, LocalDateTime newEnd,
                                                                 List<ExistingAppointment> existingAppointments) {
        for (ExistingAppointment existing : existingAppointments) {
            if (isOverlapping(newStart, newEnd, existing.startTime(), existing.endTime())) {
                return existing;
            }
        }
        return null;
    }

    /**
         * Classe pour représenter un rendez-vous existant
         */
        public record ExistingAppointment(Long id, LocalDateTime startTime, LocalDateTime endTime, String serviceNom) {

    }
}
