package com.isamrs.tim14.dao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import com.isamrs.tim14.dto.GraphsDTO;
import com.isamrs.tim14.model.Room;
import com.isamrs.tim14.model.RoomReservation;

public interface RoomReservationDAO {
	public RoomReservation save(RoomReservation roomReservation);
	public ArrayList<RoomReservation> getQuickRoomReservations(String hotelID);
	public RoomReservation saveQuick(String roomReservationID);
	public Collection<Room> getRoomsHistory();
	public RoomReservation getOneQuickReservation(Integer id);
	public GraphsDTO getRoomsDaily() throws ParseException;
	public GraphsDTO getRoomsWeekly() throws ParseException;
	public GraphsDTO getRoomsMonthly() throws ParseException;
}
