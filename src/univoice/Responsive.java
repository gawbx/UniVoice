package univoice.ui;

/** Screens that rearrange themselves for narrow, phone-sized windows. */
public interface Responsive {

    /** Width (in pixels) below which the app switches to mobile view. */
    int COMPACT_BELOW = 760;

    void setCompact(boolean compact);
}
