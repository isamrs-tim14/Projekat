$(document).ready(function(){
	 $(document).on('click','#addAirlineAdminBtn',function(){
	        $(location).attr('href',"/newAirlineAdmin.html");
	    });
	    $(document).on('click','#addHotelAdminBtn',function(){
	        $(location).attr('href',"/newHotelAdmin.html");
	    });
	    $(document).on('click','#addRentACarAdminBtn',function(){
	        $(location).attr('href',"/newRentACarAdmin.html");
	    });
})