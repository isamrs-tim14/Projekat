package com.isamrs.tim14.dao;

import java.util.List;

import com.isamrs.tim14.model.HotelService;

public interface HotelServiceDAO {
	public List<HotelService> getHotelServicesSearch(Integer hotelID);
	public HotelService save(HotelService service);
	public HotelService getHotelServiceByID(int id);
	public HotelService changeService(HotelService service);
}
