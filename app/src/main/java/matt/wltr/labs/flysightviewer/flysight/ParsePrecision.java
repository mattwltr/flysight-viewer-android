package matt.wltr.labs.flysightviewer.flysight;

public enum ParsePrecision {
    /**
     * parse all records
     */
    ALL,
    /**
     * skip records if they're closer than 1 second
     */
    EACH_SECOND,
    /**
     * skip records if they're closer than 1 second outside the exit-opening range
     */
    INTELLIGENT
}
