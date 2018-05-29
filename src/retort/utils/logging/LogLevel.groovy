package retort.utils.logging

import com.cloudbees.groovy.cps.NonCPS

/**
 * Enumeration for log levels
 */
enum LogLevel implements Serializable {

    ALL(0, 0),
    TRACE(2, 8),
    DEBUG(3, 12),
    INFO(4, 0),
    WARN(5, 202),
    ERROR(6, 5),
    FATAL(7, 9),
    NONE(Integer.MAX_VALUE, 0)

    Integer level

    static COLOR_CODE_PREFIX = "1;38;5;"

    Integer color

    private static final long serialVersionUID = 1L

    LogLevel(Integer level, Integer color) {
        this.level = level
        this.color = color
    }

    @NonCPS
    static LogLevel fromInteger(Integer value) {
        for (lvl in values()) {
            if (lvl.getLevel() == value) return lvl
        }
        return INFO
    }

    @NonCPS
    static LogLevel fromString(String value) {
        for (lvl in values()) {
            if (lvl.toString().equalsIgnoreCase(value)) return lvl
        }
        return INFO
    }

    @NonCPS
    public String getColorCode() {
        return COLOR_CODE_PREFIX + color.toString()
    }


}