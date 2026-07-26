package utils;

import java.util.ArrayList;
import java.util.List;

/*  DESIGN PATTERN — OBSERVER (Behavioral):
    -   This is the "Subject" (also called "Publisher") side of the pattern.
    It keeps a list of subscribed SlotStatusObserver instances, and calls
    onSlotStatusChanged() on every one of them whenever notifyObservers()
    is triggered.

    In this app, ParkingFacade holds a SlotStatusPublisher and calls
    notifyObservers() right after a booking or release succeeds.
    CustomerDashboardController subscribes itself as an observer, so it
    automatically refreshes its tables in reaction to that notification,
    instead of the button-click handler needing to manually call
    refreshAll() itself every time.                                         */
public class SlotStatusPublisher {

    private final List<SlotStatusObserver> observers = new ArrayList<>();

    public void subscribe(SlotStatusObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(SlotStatusObserver observer) {
        observers.remove(observer);
    }

    //  Notifies every currently subscribed observer that a change happened.
    public void notifyObservers() {
        for (SlotStatusObserver observer : observers) {
            observer.onSlotStatusChanged();
        }
    }
}