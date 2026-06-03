package com.roadmap.app.service;

import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * Adds events to macOS Calendar using AppleScript.
 * Builds the date numerically — immune to locale/language settings.
 */
public class MacCalendarService {

    public enum Result { SUCCESS, NOT_MACOS, PERMISSION_DENIED, ERROR }

    public static Result addEvent(String title, String notes,
                                  LocalDateTime startTime, String calendarName) {
        if (!isMacOS()) return Result.NOT_MACOS;

        LocalDateTime endTime = startTime.plusMinutes(30);

        String safeTitle = escapeApple(title);
        String safeNotes = escapeApple(notes);
        String safeCal   = escapeApple(calendarName);

        // Build date purely with numbers — no locale-dependent month names
        String script = String.format("""
tell application "Calendar"
    -- Build start date numerically (locale-independent)
    set startDate to current date
    set year of startDate to %d
    set month of startDate to %d
    set day of startDate to %d
    set hours of startDate to %d
    set minutes of startDate to %d
    set seconds of startDate to 0

    -- Build end date numerically
    set endDate to current date
    set year of endDate to %d
    set month of endDate to %d
    set day of endDate to %d
    set hours of endDate to %d
    set minutes of endDate to %d
    set seconds of endDate to 0

    -- Find target calendar
    set targetCal to missing value
    repeat with c in calendars
        if name of c is "%s" then
            set targetCal to c
            exit repeat
        end if
    end repeat
    if targetCal is missing value then
        repeat with c in calendars
            try
                if writable of c then
                    set targetCal to c
                    exit repeat
                end if
            end try
        end repeat
    end if
    if targetCal is missing value then
        set targetCal to first calendar
    end if

    -- Create event
    tell targetCal
        set newEvent to make new event with properties ¬
            {summary:"%s", start date:startDate, end date:endDate, description:"%s"}
        tell newEvent
            make new display alarm with properties {trigger interval:-15}
        end tell
    end tell
end tell
""",
            // start date fields
            startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth(),
            startTime.getHour(), startTime.getMinute(),
            // end date fields
            endTime.getYear(), endTime.getMonthValue(), endTime.getDayOfMonth(),
            endTime.getHour(), endTime.getMinute(),
            // strings
            safeCal, safeTitle, safeNotes
        );

        return runScript(script);
    }

    private static Result runScript(String script) {
        try {
            ProcessBuilder pb = new ProcessBuilder("osascript", "-");
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            try (OutputStream os = proc.getOutputStream()) {
                os.write(script.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            String output = new String(proc.getInputStream().readAllBytes()).trim();
            int exit = proc.waitFor();

            if (exit == 0) return Result.SUCCESS;

            String lower = output.toLowerCase();
            if (lower.contains("not authorized") || lower.contains("not allowed") ||
                lower.contains("permission") || lower.contains("1743")) {
                System.err.println("[MacCalendarService] Permission denied: " + output);
                return Result.PERMISSION_DENIED;
            }
            System.err.println("[MacCalendarService] Error (exit=" + exit + "): " + output);
            return Result.ERROR;

        } catch (Exception e) {
            System.err.println("[MacCalendarService] Exception: " + e.getMessage());
            return Result.ERROR;
        }
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    private static String escapeApple(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "");
    }
}
