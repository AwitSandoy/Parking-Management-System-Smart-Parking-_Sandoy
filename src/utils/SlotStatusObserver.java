package utils;

/*  DESIGN PATTERN — OBSERVER (Behavioral):
    -   This is the "Observer" side of the pattern. Any class that wants to be
    notified when a parking slot's status changes (a booking or a release
    happened somewhere in the app) implements this interface and
    subscribes itself to a SlotStatusPublisher.

    This decouples "something changed" from "who needs to react to it" -
    the code that performs a booking doesn't need to know or care which
    screens are currently open and need refreshing; it just announces the
    change, and every subscribed observer reacts on its own.                */

public interface SlotStatusObserver {
    //  Called by a SlotStatusPublisher whenever a slot's status changes.
    void onSlotStatusChanged();
}