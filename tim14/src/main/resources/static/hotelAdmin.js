all_rooms = [];
selected_rooms = [];
all_hotel_services = [];
selected_hotel_services = [];

$(document).ready(function() {
	
	$.ajax({
		type: 'GET',
		url: '/auth/getInfo',
		headers: createAuthorizationTokenHeader(),
		success: function(data){
			if(data.passwordChanged){
				$('#passwordChangedTRUE').show();
				$('#passwordChangedFALSE').hide();
			}else{
				$('#passwordChangedTRUE').hide();
				$('#passwordChangedFALSE').show();
			}
		},
		error: function (jqXHR) {
        	if (jqXHR.status == 401) {
				showMessage('Login as hotel administrator!', "orange");
			}else{
				showMessage('[' + jqXHR.status + "]  ", "red");
			}
        }
	});
	
	$(document).on('click','#firstTimeChangePassword', function(){
		//var currentPass = $('#oldPasswordField').val();
		var newPassword = $('#newPasswordField').val();
		var repNewPassword = $('#reNewPasswordField').val();
		if(newPassword == "" || repNewPassword == ""){
			showMessage("Please fill all the fields!", 'orange');
			return;
		}
		if(newPassword != repNewPassword){
			showMessage('New password and repeat new password fields must be equals!', 'orange');
			return;
		}
		
		$.ajax({
			type: 'POST',
			url: '/auth/initChangePassword',
			headers: createAuthorizationTokenHeader(),
			data : JSON.stringify({
				'currentPassword': '',
				newPassword,
				repNewPassword
			}),
			success: function(check){
				if(check){
					showMessage('Login once again and enjoy!', 'green');
			        $(location).attr('href',"/logout");
				}else{
					showMessage('Old password is not correct!', 'orange');
				}
			},
			error: function (jqXHR, exception) {
				if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  " + exception, "red");
				}
			}
		});

	});
	
	
	
	$.ajax({
        type : 'GET',
        url : '/api/hotelAdmin/hotel',
        headers: createAuthorizationTokenHeader(),
        success: function(data){
			$('#hiddenPForUserHotelID').val(data.id);
            $('#pNameOfChosenHotelRR').text(data.name);
            $('#pDescriptionOfChosenHotelRR').text(data.description);
            $('#pDestinationOfChosenHotelRR').text(data.destination.name +
                ", " + data.destination.country);
            $.get({ url:'/api/getGradeForHotel',
 	    			headers: createAuthorizationTokenHeader()
 	    	},function(data){
 	    		var i = 0;
 	    		var onStar = data;
 	    		var stars = $('.li.star');
 	    		$("ul li").each(function() {
 	    			$(this).removeClass('selected');
 	   		    })  
 	    		$("ul li").each(function() {
 	    			if(i<onStar){
 	    				$(this).addClass('selected');
 	    				i++;}
 	    			else return false;
 	   		    })
 	   		    renderTableAllRoomsAndServicesOfHotel();
 	       });
            
        },
        error: function (jqXHR) {
        	if (jqXHR.status == 401) {
				showMessage('Login as hotel administrator!', "orange");
			}else{
				showMessage('[' + jqXHR.status + "]  " , "red");
			}
        }
    });
	
	
	$('#showHotelInfoBtn').css('display', 'none');
	
	$(document).on('click','#allRooms',function(){
		renderTableHotelRooms();
	 });

    $(document).on('click','#quitAllRooms',function(){
    	$("#allRoomsContainer").css("display", "none");
    });
    
    $(document).on('click', "#showReports", function(){
    	$("#chartContainer").css('display', 'block');
    });
    
    $(document).on('click', '#showHotelIncomes', function(){
    	var startCheck = $('#startIncomeHotel').val();
    	var endCheck = $('#endIncomeHotel').val();
    	if(startCheck == "" || endCheck == ""){
    		showMessage("Enter start and end date", "orange");
    		return;
    	}
    	var start = stringToDate($('#startIncomeHotel').val());
    	var end = stringToDate($('#endIncomeHotel').val());
    	if(start>end){
    		showMessage("Start date must be later then end date", "orange");
    		return;
    	}
    	var start = stringToDate($('#startIncomeHotel').val())- 24*60*60*1000
    	var end = stringToDate($('#endIncomeHotel').val()) + 24*60*60*1000
    	$.get({
    		url: '/api/getHotelIncomes/'+start+'/'+end, 
    		headers: createAuthorizationTokenHeader()
		}, 
		function(income){
			$("#hotelIncomeVal").html(income);
		});
    });
    
	$('#quitDialogHotelViewRR').css('display','none');

	$(document).on('click', '#addRoomBtn', function() {
		$(location).attr('href', "/room.html");
	});

	$(document).on('click','#logoutBtn',function(){
    	removeJwtToken();
        $(location).attr('href',"/logout");
	});

	$(document).on('click','#quitDialogHotelView',function(){
        $('#dialogHotelView').css('display', 'none');
	});

	$(document).on('click', '#makeQuickReservation', function() {
		$('#dialogHotelView').css('display', 'block');
		$.ajax({
            type : 'GET',
            url : '/api/hotelAdmin/hotel',
            headers: createAuthorizationTokenHeader(),
            success: function(data){
				$('#hotelIdField').val(data.id);
                $('#pNameOfChosenHotel').text(data.name);
                $('#pDescriptionOfChosenHotel').text(data.description);
                $('#pDestinationOfChosenHotel').text(data.destination.name +
                    ", " + data.destination.country);
                $('#roomSearchArrivalDate').val(formatDate(new Date()));
                renderHotelServiceTable(data.id);
            },
            error: function (jqXHR) {
                if(jqXHR.status == 401){
                    showMessage("You don't have permission for getting current hotel!", "red");
                }
            }
        });
	});

	$(document).on('click','#roomSearchBtn', function(){
        var hotelId = $('#hotelIdField').val();
        var start = stringToDate($('#roomSearchArrivalDate').val());
		var end = start + $('#roomSearchDayNumber').val()*24*60*60*1000;
        var TwoBedRooms = $('#roomSearch2Bed').prop('checked');
        var ThreeBedRooms = $('#roomSearch3Bed').prop('checked');
        var FourBedRooms = $('#roomSearch4Bed').prop('checked');

        renderRoomTable(hotelId, start, end, TwoBedRooms, ThreeBedRooms, FourBedRooms, $('#roomSearchDayNumber').val());
	});
	
	$(document).on('click','#makeHotelReservationBtn', function(){
		selected_rooms = [];
		selected_hotel_services = [];
        for(var i=0;i<all_rooms.length;i++){
			var red = all_rooms[i];
			if($('#roomCheckbox'+ red.id).prop('checked')){
				delete red.hotel.hibernateLazyInitializer;
				delete red.hotel.destination.hibernateLazyInitializer;
				selected_rooms.push(red);
			}
		}
		for(var i=0;i<all_hotel_services.length;i++){
			var red = all_hotel_services[i];
			if($('#hotelServiceCheckbox'+ red.id).prop('checked')){
				delete red.hotel.hibernateLazyInitializer;
				delete red.hotel.destination.hibernateLazyInitializer;
				selected_hotel_services.push(red);
			}
		}
		var start = stringToDate($('#roomSearchArrivalDate').val());
		var end = start + ($('#roomSearchDayNumber').val() - 1)*24*60*60*1000;
		if(selected_rooms.length == 0){
			showMessage("Select at least 1 room!", "orange");
			return;
		}
		if($('#discountId').val() < 0 || $('#discountId').val() > 100){
			showMessage("Discount must be between 0% and 100% ", 'orange');
			return;
		}
		var price = calculatePrice(selected_rooms, selected_hotel_services, $('#roomSearchDayNumber').val());
		var discount = $('#discountId').val()
		price = Math.round(price);
		
		var reservation = {
			"start": new Date(start),
			"end": new Date(end),
			"rooms": selected_rooms,
			"services": selected_hotel_services,
			"hotel": selected_rooms[0].hotel,
			"price": price,
			discount
		};
		$.ajax({
			type : 'POST',
			url : "/api/roomReservations",
			headers: createAuthorizationTokenHeader(),
			data : JSON.stringify(reservation),
			success: function(){
				showMessage('Quick room reservation successful!', "green");
				$(location).attr('href',"/hotelAdmin.html");
			},
			error: function (jqXHR, exception) {
				if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  " + exception, "red");
				}
			}
		})
	});
	
	$(document).on('click', '#editProfileBtn', function(){
		$.ajax({
			type : 'GET',
			url : '/auth/getInfo',
			headers: createAuthorizationTokenHeader(),
			success: function(adminData){
				$('#firstNameHotelAdmin').val(adminData.firstName);
				$('#lastNameHotelAdmin').val(adminData.lastName);
				$('#emailHotelAdmin').val(adminData.email);
				$('#cityHotelAdmin').val(adminData.city);
				$('#phoneHotelAdmin').val(adminData.phoneNumber);
				
				$('#dialogEditHotelAdminProfile').css('display', 'block');
			},
			error: function (jqXHR, exception) {
				if (jqXHR.status == 401) {
					showMessage('Login first!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  " + exception, "red");
				}
			}
		})
	});
	
	$(document).on('click', '#editHotelAdminProfile', function(){
		var password = $('#passwordHotelAdmin').val();
		if(password !== ""){
			var repeat = $('#rep_passwordHotelAdmin').val();
			if(repeat !== password){
				showMessage('Password and repeat password are not equal!', 'orange');
				return;
			}
		}
		var data = {
			password: $('#passwordHotelAdmin').val(),
			firstName: $('#firstNameHotelAdmin').val(),
			lastName: $('#lastNameHotelAdmin').val(),
			email: $('#emailHotelAdmin').val(),
			city: $('#cityHotelAdmin').val(),
			phone: $('#phoneHotelAdmin').val()
		}
		
		$.ajax({
			type : 'POST',
			url : "/api/updateHotelAdmin",
			headers: createAuthorizationTokenHeader(),
			data : JSON.stringify(data),
			success: function(){
				showMessage('Hotel admin successfully updated!', "green");
				$('#dialogEditHotelAdminProfile').css('display', 'none');
				if($('#passwordHotelAdmin').val() == ""){
					$(location).attr('href',"/hotelAdmin.html");
				}else{
					$(location).attr('href',"/logout");
				}
				
			},
			error: function (jqXHR, exception) {
				if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  " + exception, "red");
				}
			}
		})
	});
	
	$(document).on('click', '#quitDialogEditHotelAdmin', function(){
		$('#dialogEditHotelAdminProfile').css('display', 'none');
	});
	
	$(document).on('click', '#showHotelInfoBtn', function() {
		$('#showHotelInfoBtn').css('display', 'none');
		$.ajax({
            type : 'GET',
            url : '/api/hotelAdmin/hotel',
            headers: createAuthorizationTokenHeader(),
            success: function(data){
                $('#pNameOfChosenHotelRR').text(data.name);
                $('#pDescriptionOfChosenHotelRR').text(data.description);
                $('#pDestinationOfChosenHotelRR').text(data.destination.name +
                    ", " + data.destination.country);
                renderTableAllRoomsAndServicesOfHotel();
                $('#dialogHotelViewRR').css('display', 'block');
            },
            error: function (jqXHR) {
            	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  ", "red");
				}
            }
        });
	});
	
	$(document).on('click', '#quitDialogHotelViewRR', function(){
		$('#dialogHotelViewRR').css('display', 'none');
	});
	
	$(document).on('click','table button',function(e){
        if(e.target.id.startsWith("removeRoomID")){
            var id = e.target.id.substr(12);
            $.ajax({
        		type: 'DELETE',
        		url: '/api/removeRoom/'+id,
        		headers: createAuthorizationTokenHeader(),
        		success: function(){
        			showMessage('Room successfully removed!', 'green');
        			$(location).attr('href',"/hotelAdmin.html");
        		},
        		error: function (jqXHR, exception) {
        			if (jqXHR.status == 401) {
        				showMessage('Login first!', "orange");
        			}else{
        				showMessage('[' + jqXHR.status + "]  " + exception, "red");
        			}
        		}
        	});
        }else if(e.target.id.startsWith("editRoomID")){
        	var id = e.target.id.substr(10);
            $.ajax({
            	type: 'GET',
            	url: '/api/room/' + id,
            	headers: createAuthorizationTokenHeader(),
            	success: function(data){
            		$("#bedNumberEditRoom").html('');
            		var select = document.getElementById("bedNumberEditRoom");
            		var array = [2,3,4];
                    for(var i=0;i<array.length;i++){
                        select.options[select.options.length] = new Option(array[i],''+array[i]);
                    }
                    $("#bedNumberEditRoom").val("" + data.bedNumber);
                    
                    $('#priceEditRoom').val(data.price);
                    $('#roomNumberEditRoom').val(data.roomNumber);
                    
                    $('#hiddenPForUser').val(id);
                    $('#hiddenPForUserName').val(data.roomNumber);
            		$('#dialogEditHotelRoom').css('display', 'block');
            	},
            	error: function (jqXHR) {
                	if (jqXHR.status == 401) {
    					showMessage('Login as hotel administrator!', "orange");
    				}else{
    					showMessage('[' + jqXHR.status + "]  ", "red");
    				}
                }
            });
        }else if(e.target.id.startsWith("hotelServiceApplyBtn")){
        	var id = e.target.id.substr(20);
	        var price = $('#hotelServiceSetPriceField'+id).val();
	        if(price == ""){
	        	showMessage('Price cannot be empty!', 'orange');
	        	return;
	        }
	        if(price < 0){
	        	showMessage('Price must be positive number!', 'orange');
	        	return;
	        }
	        $.ajax({
            	type: 'GET',
            	url: '/api/hotelServicesByID/' + id,
            	headers: createAuthorizationTokenHeader(),
            	success: function(data){
            		data.price = price;
            		$.ajax({
            			type: 'PUT',
            			url: '/api/changeHotelService',
            			headers: createAuthorizationTokenHeader(),
            			data : JSON.stringify(data),
            			success: function(data){
        					showMessage('Successfully updated hotel service price', "green");
            				renderTableAllRoomsAndServicesOfHotel();
            			},
            			error: function (jqXHR) {
                        	if (jqXHR.status == 401) {
            					showMessage('Login as hotel administrator!', "orange");
            				}else if (jqXHR.status == 406) {
            					showMessage('Hotel service must be unique!', "orange");
            				}else{
            					showMessage('[' + jqXHR.status + "]  ", "red");
            				}
                        }
            		});
            	},
            	error: function (jqXHR) {
                	if (jqXHR.status == 401) {
    					showMessage('Login as hotel administrator!', "orange");
    				}else{
    					showMessage('[' + jqXHR.status + "]  ", "red");
    				}
                }
            });
	        
        }
	});
	
	$(document).on('click', '#quitDialogEditHotelRoom', function(){
		$('#dialogEditHotelRoom').css('display', 'none');
	});
	
	$(document).on('click', '#editHotelRoomBtn', function(){
		var price = $('#priceEditRoom').val();
		var roomNumber = $('#roomNumberEditRoom').val();
		var bedNumber = $('#bedNumberEditRoom option:selected').val();
		if(roomNumber < 0){
			showMessage('Room number must be positive number!', 'orange');
			return;
		}
		if(price < 0){
			showMessage('Price must be positive number!', 'orange');
			return;
		}
		
		$.ajax({
        	type: 'GET',
        	url: '/api/room/' + $('#hiddenPForUser').val(),
        	headers: createAuthorizationTokenHeader(),
        	success: function(roomData){
        		roomData.price = parseInt(price);
        		roomData.roomNumber = parseInt(roomNumber);
        		roomData.bedNumber = parseInt(bedNumber);
        		if($('#hiddenPForUserName').val() != roomData.roomNumber){
        			roomData.floor = -1;
        		}
        		$.ajax({
        			type: 'PUT',
        			url: '/api/changeRoom',
        			headers: createAuthorizationTokenHeader(),
        			data : JSON.stringify(roomData),
        			success: function(data){
        				showMessage('Room is successfully changed!', 'green');
        				$('#dialogEditHotelRoom').css('display', 'none');
        				renderTableAllRoomsAndServicesOfHotel();
        			},
        			error: function (jqXHR) {
                    	if (jqXHR.status == 401) {
        					showMessage('Login as hotel administrator!', "orange");
        				}else if (jqXHR.status == 406) {
        					showMessage('Room number must be unique!', "orange");
        				}else{
        					showMessage('[' + jqXHR.status + "]  ", "red");
        				}
                    }
        		});
        	},
        	error: function (jqXHR) {
            	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  ", "red");
				}
            }
        });
	});
	
	$(document).on('click','#addHotelServiceBtn', function(){
		$.ajax({
	        type : 'GET',
	        url : '/api/hotelAdmin/hotel',
	        headers: createAuthorizationTokenHeader(),
	        success: function(data){
				$('#myPServiceSave').val(data.id);
				$('#extraDiscountHotel').val(data.extraServiceDiscount);
				$('#dialogNewHotelService').css("display", "block");
	        },
	        error: function (jqXHR) {
	        	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  " , "red");
				}
	        }
	    });
		
	});
	
	$(document).on('click', '#confirmExtraDiscount', function(){
		var discount = $('#extraDiscountHotel').val();
		if(isNaN(discount) || discount == ""){
			showMessage('Discount must be a number', 'orange');
			return;
		}
		if(discount<0 || discount>100){
			showMessage('Discount is not in limit between 0% and 100%', 'orange');
			return;
		}
		
		$.ajax({
			type: 'PUT',
			url: '/api/setRoomDiscountServices',
			headers: createAuthorizationTokenHeader(),
			data : JSON.stringify({
				discount
			}),
			success: function(data){
				showMessage('Room discount on extra services is changed!', 'green');
				$('#dialogNewHotelService').hide();
			},
			error: function (jqXHR) {
            	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  ", "red");
				}
            }
		});
		
		
	});
	
	$(document).on('click','#quitDialogHotelService', function(){
		$('#dialogNewHotelService').css("display", "none");
	});
	
	$(document).on('click','#confirmAddingHotelServiceBtn', function(){
		var name = $('#newHotelServiceName').val();
		if(name == ""){
			showMessage('Name cannot be empty text!', 'orange');
			return;
		}
		var price = $('#newHotelServicePrice').val();
		if(price < 0){
			showMessage('Price must be positive number!', 'orange');
			return;
		}
		var data = {price,name}
		$.ajax({
			type: 'POST',
			url: '/api/hotelService',
			headers: createAuthorizationTokenHeader(),
			data : JSON.stringify(data),
			success: function(data){
				showMessage('Hotel service is successfully added!', 'green');
				$('#dialogNewHotelService').css('display', 'none');
			},
			error: function (jqXHR) {
            	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  ", "red");
				}
            }
		});
		
	});
	
	$(document).on('click','#editHotelBtn', function(){
		$.ajax({
			type: 'GET',
			url: '/api/hotelAdmin/hotel',
			headers: createAuthorizationTokenHeader(),
			success: function(data){
				$('#editHotelInfoName').val(data.name);
				$('#editHotelInfoAddress').val(data.destination.address);
				$('#editHotelInfoDescription').val(data.description);
				$('#dialogEditHotelInformation').css('display','block');
			},
			error: function (jqXHR) {
            	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  ", "red");
				}
            }
		});
	});
	
	$(document).on('click','#quitDialogEditHotelInfo', function(){
		$('#dialogEditHotelInformation').css('display','none');
	});
	
	$(document).on('click','#confirmChangesHotelInformationBtn', function(){
		var name = $('#editHotelInfoName').val();
		var address = $('#editHotelInfoAddress').val();
		var description = $('#editHotelInfoDescription').val();
		if(name == ""){
			showMessage('Hotel name cannot be empty!', "orange");
			return;
		}
		if(address == ""){
			showMessage('Address cannot be empty!', "orange");
			return;
		}
		if(description == ""){
			showMessage('Description cannot be empty!', "orange");
			return;
		}
		
		
		$.ajax({
			type: 'GET',
			url: '/api/hotelAdmin/hotel',
			headers: createAuthorizationTokenHeader(),
			success: function(data){
				if(data.name != name){
					data.extraServiceDiscount = -1;
				}
				data.name = name;
				data.destination.address = address;
				data.description = description;
				$.ajax({
					type: 'PUT',
					url: '/api/changeHotel',
					headers: createAuthorizationTokenHeader(),
        			data : JSON.stringify(data),
					success: function(data2){
						showMessage('Hotel is successfully changed!', "green");
						
						$("#pNameOfChosenHotelRR").text(data2.name);
						$("#pDescriptionOfChosenHotelRR").text(data2.description);
						$("#pDestinationOfChosenHotelRR").text(data2.destination.name + ", " + data2.destination.country);
						
						$('#dialogEditHotelInformation').css('display','none');
					},
					error: function (jqXHR) {
		            	if (jqXHR.status == 401) {
							showMessage('Login as hotel administrator!', "orange");
						}else if(jqXHR.status == 406){
							showMessage('Hotel name must be unique!', "orange");
						}else if(jqXHR.status == 500){
							$('#dialogEditHotelInformation').hide();
							showMessage('Another admin already changed informations!', "orange");
						}else{
							showMessage('[' + jqXHR.status + "]  ", "red");
						}
		            }
				});
			},
			error: function (jqXHR) {
            	if (jqXHR.status == 401) {
					showMessage('Login as hotel administrator!', "orange");
				}else{
					showMessage('[' + jqXHR.status + "]  ", "red");
				}
            }
		});
	});
	
	$(document).on('click', '#showGraph', function(){
    	var ctx = $("#myChart");
    	var type = $("#chartType").val();
    	if(type=="daily"){
    		$.get({url: '/api/getDailyRooms', 
    			headers: createAuthorizationTokenHeader()},
    			function(data){
			    	var myChart = new Chart(ctx, {
			    	  type: 'bar',
			    	  data: {
			    	    labels: data.x,
			    	    datasets: [{
			    	      label: 'Number of rooms',
			    	      data: data.x,
			    	      backgroundColor:[
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)',
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)',
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)',
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)',
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)',
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)',
			    	        'rgba(54, 162, 235, 0.3)',
			    	        'rgba(75, 192, 192, 0.3)'
			    	      ],
			    	      borderColor: [
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)',
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)',
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)',
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)',
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)',
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)',
			    	        'rgba(54, 162, 235, 1)',
			    	        'rgba(75, 192, 192, 1)'
			    	      ],
			    	      borderWidth: 1
			    	    }]
			    	 },
			    })
			 })
    	}else if(type=="weekly"){
    		$.get({url: '/api/getWeeklyRooms', 
    			headers: createAuthorizationTokenHeader()}, function(data){
			    	var myChart = new Chart(ctx, {
			    	  type: 'bar',
			    	  data: {
			    	    labels: data.x,
			    	    datasets: [{
			    	      label: 'Number of rooms',
			    	      data: data.y,
			    	      backgroundColor: [
			    	    	  	'rgba(54, 162, 235, 0.3)',
				    	        'rgba(75, 192, 192, 0.3)',
				    	        'rgba(54, 162, 235, 0.3)',
				    	        'rgba(75, 192, 192, 0.3)',
				    	        'rgba(54, 162, 235, 0.3)',
				    	        'rgba(75, 192, 192, 0.3)',
				    	        'rgba(54, 162, 235, 0.3)',
				    	        'rgba(75, 192, 192, 0.3)',
				    	        'rgba(54, 162, 235, 0.3)',
				    	        'rgba(75, 192, 192, 0.3)',
				    	        'rgba(54, 162, 235, 0.3)',
				    	        'rgba(75, 192, 192, 0.3)'
			    	      ],
			    	      borderColor: [
			    	    	  	'rgba(54, 162, 235, 1)',
				    	        'rgba(75, 192, 192, 1)',
				    	        'rgba(54, 162, 235, 1)',
				    	        'rgba(75, 192, 192, 1)',
				    	        'rgba(54, 162, 235, 1)',
				    	        'rgba(75, 192, 192, 1)',
				    	        'rgba(54, 162, 235, 1)',
				    	        'rgba(75, 192, 192, 1)',
				    	        'rgba(54, 162, 235, 1)',
				    	        'rgba(75, 192, 192, 1)',
				    	        'rgba(54, 162, 235, 1)',
				    	        'rgba(75, 192, 192, 1)'
			    	      ],
			    	      borderWidth: 1
			    	    }]
			    	 },
			    })
			 })
    	}else if(type=="monthly"){
    		$.get({url: '/api/getMonthlyRooms', 
    			headers: createAuthorizationTokenHeader()}, function(data){
			    	var myChart = new Chart(ctx, {
			    	  type: 'bar',
			    	  data: {
			    	    labels: data.x,
			    	    datasets: [{
			    	      label: 'Number of rooms',
			    	      data: data.y,
			    	      backgroundColor: [
			    	    	  'rgba(54, 162, 235, 0.3)',
				    	      'rgba(75, 192, 192, 0.3)',
				    	      'rgba(54, 162, 235, 0.3)',
				    	      'rgba(75, 192, 192, 0.3)',
				    	      'rgba(54, 162, 235, 0.3)',
				    	      'rgba(75, 192, 192, 0.3)',
				    	      'rgba(54, 162, 235, 0.3)',
				    	      'rgba(75, 192, 192, 0.3)',
				    	      'rgba(54, 162, 235, 0.3)',
				    	      'rgba(75, 192, 192, 0.3)',
				    	      'rgba(54, 162, 235, 0.3)',
				    	      'rgba(75, 192, 192, 0.3)'
			    	      ],
			    	      borderColor: [
			    	    	  'rgba(54, 162, 235, 1)',
				    	      'rgba(75, 192, 192, 1)',
				    	      'rgba(54, 162, 235, 1)',
				    	      'rgba(75, 192, 192, 1)',
				    	      'rgba(54, 162, 235, 1)',
				    	      'rgba(75, 192, 192, 1)',
				    	      'rgba(54, 162, 235, 1)',
				    	      'rgba(75, 192, 192, 1)',
				    	      'rgba(54, 162, 235, 1)',
				    	      'rgba(75, 192, 192, 1)',
				    	      'rgba(54, 162, 235, 1)',
				    	      'rgba(75, 192, 192, 1)'
			    	      ],
			    	      borderWidth: 1
			    	    }]
			    	 },
			    })
			 })
    	}

    });
	
	$(document).on('click', "#hideReports", function(){
    	$("#chartContainer").css('display', 'none');

    });
	
});


var renderHotelServiceTable = function(hotelId){
    $.get('/api/hotelServicesSearch/'+hotelId, function(servicesData){
		var services = servicesData;
		all_hotel_services = services;
        $('#selectedHotelServicesTable').html(`<tr><th>Name</th><th>Price</th><th>Select</th></tr>`);
        for(var i=0;i<services.length;i++){
            var red = services[i];
            checkBoxID = "hotelServiceCheckbox"+ red.id;
            $('#selectedHotelServicesTable tr:last').after(`<tr><td>${red.name}</td><td>${red.price}</td><td><input type="checkbox" id=${checkBoxID}></td></tr>`);
        }
        $('#dialogHotelView').css("display","block");
    });
}


var renderRoomTable = function(hotelId, arrivalDate, departureDate, TwoBedRooms, ThreeBedRooms, FourBedRooms, numDays){
    var text = `/${hotelId}/${arrivalDate}/${departureDate}/${TwoBedRooms}/${ThreeBedRooms}/${FourBedRooms}`;
    $.get('/api/roomsSearch'+text, function(RoomData){
		var rooms = RoomData;
		all_rooms = rooms;
		$('#selectedHotelRoomsTable').html(`<tr><th>Floor number</th><th>Number of beds</th><th>Grade</th><th>Full price</th><th>Select</th></tr>`);
		for(var i=0;i<rooms.length;i++){
			var red = rooms[i];
			checkBoxID = "roomCheckbox"+ red.id;
			$('#selectedHotelRoomsTable tr:last').after(`<tr><td>${red.floor}</td><td>${red.bedNumber}</td><td>-</td><td>${red.price*numDays}</td><td>
			<input type="checkbox" id=${checkBoxID}></td></tr>`);
		}
	});
}

var renderTableAllRoomsAndServicesOfHotel = function(){
	$.ajax({
		type: 'GET',
		url: '/api/unreservedRooms',
		headers: createAuthorizationTokenHeader(),
		success: function(rooms){
			$('#hotelRoomsTableRR').html(`<tr><th>Room number</th><th>Floor number</th><th>Number of beds</th><th>Grade</th><th>Price per day</th><th>Remove room</th><th>Change room</th></tr>`);
			rooms.sort((a, b) => (a.roomNumber > b.roomNumber) ? 1 : -1)
			for(var i=0;i<rooms.length;i++){
				var red = rooms[i];
				removeRoomID = "removeRoomID"+ red.id;
				var changeRoomID = "editRoomID" + red.id;
				$('#hotelRoomsTableRR tr:last').after(`<tr><td>${red.roomNumber}</td><td>${red.floor}</td><td>${red.bedNumber}</td><td>-</td><td>${red.price}</td><td>
				<button id=${removeRoomID}>Remove</button></td><td>
				<button id=${changeRoomID}>Change</button></td></tr>`);
			}
			
			$.get('/api/hotelServicesSearch/'+ $('#hiddenPForUserHotelID').val(), function(servicesData){
				var services = servicesData;
		        $('#hotelServicesTableRR').html(`<tr><th>Name</th><th>Price</th><th>Change</th></tr>`);
		        for(var i=0;i<services.length;i++){
		            var red = services[i];
		            var buttonSEID = "hotelServiceApplyBtn"+ red.id;
		            var inputTextService = "hotelServiceSetPriceField" + red.id;
		            $('#hotelServicesTableRR tr:last').after(`<tr><td>${red.name}</td><td><input type="number" min="1" style="text-align:center;" value=${red.price} id=${inputTextService}></td><td><button id=${buttonSEID}>Change price</button></td></tr>`);
		        }
		        $('#dialogHotelViewRR').css('display', 'block');
		    });
		},
		error: function (jqXHR, exception) {
			if (jqXHR.status == 401) {
				showMessage('Login first!', "orange");
			}else{
				showMessage('[' + jqXHR.status + "]  " + exception, "red");
			}
		}
	});
}

var renderTableHotelRooms = function(){
	$.ajax({
		type: 'GET',
		url: '/api/allRooms',
		headers: createAuthorizationTokenHeader(),
		success: function(rooms){
			$('#allRoomsTable').html(`<tr><th>Floor number</th><th>Number of beds</th><th>Grade</th><th>Price</th></tr>`);
			for(var i=0;i<rooms.length;i++){
				var red = rooms[i];
				var forGrade = `<section class='rating-widget'>
				<div class='rating-stars text-center' height="20" width="100">
				  <ul>
				      <li class='star' title='Poor' data-value='1'>
	    			  	<i class='fa fa-star fa-fw'></i>
		   			 </li>
		     		 <li class='star' title='Fair' data-value='2'>
		        		<i class='fa fa-star fa-fw'></i>
		      		 </li>
		     		 <li class='star' title='Good' data-value='3'>
		       			<i class='fa fa-star fa-fw'></i>
		      		 </li>
		     		 <li class='star' title='Excellent' data-value='4'>
		        		<i class='fa fa-star fa-fw'></i>
		      		 </li>
		      		 <li class='star' title='WOW!!!' data-value='5'>
		        	 	<i class='fa fa-star fa-fw'></i>
		     		 </li>
	    		 </ul>
				</div>	
				</section>`
				$('#allRoomsTable tr:last').after(`<tr><td>${red.floor}</td><td>${red.bedNumber}</td><td>${forGrade}</td><td>${red.price}</td></tr>`);
				$.get({url:'/api/getGradeForHotel',headers: createAuthorizationTokenHeader()}, 
					function(data){
		     	    	var i = 0;
		     	    	var onStar = data;
		     	    	var stars = $('.li.star');
		     	    	$("ul li").each(function() {
		     	    		$(this).removeClass('selected');
		     	   		})  
		     	    	$("ul li").each(function() {
		     	    		if(i<onStar){
		     	    			$(this).addClass('selected');
		     	    			i++;
		     	    		}
		     	    		else
		     	    			return false;
		     	   		 })
		     	   	$("#allRoomsContainer").show();
	     	      }
				);
				
			}
		},error: function (jqXHR, exception) {
			if (jqXHR.status == 401) {
				showMessage('Login first!', "orange");
			}else{
				showMessage('[' + jqXHR.status + "]  " + exception, "red");
			}
		}
	});
}

function formatDate(date) {
    month = '' + (date.getMonth() + 1);
    day = '' + date.getDate();
    year = date.getFullYear();
    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;
	return [year, month, day].join('-');

}

function stringToDate(displayFormat){
	myDate=displayFormat.split("-");
	var newDate = myDate[1]+"/"+myDate[2]+"/"+myDate[0];
	return new Date(newDate).getTime();
}

function calculatePrice(rooms, services, days){
	price = 0;
	for(var i=0;i<rooms.length;i++){
		price += (days * rooms[i].price);
	}
	for(var i=0;i<services.length;i++){
		price += services[i].price;
	}

	return price;
}