package com.isamrs.tim14.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.isamrs.tim14.model.Flight;
import com.isamrs.tim14.model.FlightReservation;

public interface FlightReservationDAO {
	public ResponseEntity<String> saveReservation(List<FlightReservation> reservations);

	public ResponseEntity<String> makeQuickReservation(List<FlightReservation> reservations);

	public ResponseEntity<List<FlightReservation>> getQuickTickets(Integer id);

	public ResponseEntity<String> buyQuickTicket(Integer reservationID);
	
	public Collection<Flight> getFlightHistory();

	public ResponseEntity<FlightReservation> getQuickReservation(Integer reservationID);
}

