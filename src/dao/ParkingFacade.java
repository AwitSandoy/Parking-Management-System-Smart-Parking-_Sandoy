package dao;

import models.ParkingSlot;
import models.Reservation;
import utils.SlotStatusPublisher;

import java.sql.SQLException;
import java.util.List;

/*  DESIGN PATTERN — FACADE (Structural):
    -   ParkingFacade sits in front of two separate DAOs (IParkingSlotDAO and
    IReservationDAO) and exposes a small, simplified set of methods that
    match what a Customer actually wants to do: see available slots, book
    one, release one, and see their own active reservations.

    Without this class, CustomerDashboardController would need to hold
    references to both DAOs directly and know the correct order of calls
    for a booking/release action. The Facade hides that coordination
    behind one simple entry point, and also takes care of notifying
    subscribed observers (see SlotStatusPublisher / SlotStatusObserver)
    whenever a booking or release actually succeeds.                            */
public class ParkingFacade {

    private final IParkingSlotDAO slotDAO;
    private final IReservationDAO reservationDAO;
    private final SlotStatusPublisher publisher;

    public ParkingFacade(IParkingSlotDAO slotDAO, IReservationDAO reservationDAO, SlotStatusPublisher publisher) {
        this.slotDAO = slotDAO;
        this.reservationDAO = reservationDAO;
        this.publisher = publisher;
    }

    public List<ParkingSlot> getAvailableSlots() throws SQLException {
        return slotDAO.getAvailableSlots();
    }

    public List<Reservation> getActiveReservationsForUser(int userId) throws SQLException {
        return reservationDAO.getActiveReservationsForUser(userId);
    }

    //  Books a slot, then notifies observers if it succeeded.
    public boolean bookSlot(int userId, int slotId) throws SQLException {
        boolean success = reservationDAO.bookSlot(userId, slotId);
        if (success) {
            publisher.notifyObservers();
        }
        return success;
    }

    //  Releases a slot, then notifies observers if it succeeded.
    public boolean releaseSlot(int reservationId) throws SQLException {
        boolean success = reservationDAO.releaseSlot(reservationId);
        if (success) {
            publisher.notifyObservers();
        }
        return success;
    }

    public SlotStatusPublisher getPublisher() {
        return publisher;
    }
}