
function rentacarProfil() {
	$.get("/users/test",function(data){
		renderProfile(data);
	});
}

function renderProfile(user) {
	var tabela = $('#tabeleProfile');
	tabela
			.append(
					'<tr><th>Password</th><td><input type = "text" class = "field" value = "'
					+ user.password
					+ '"></td></tr>'
					+ '<tr><th>First name</th><td><input type = "text" class = "field" value = "'
					+ user.firstName
					+ '"></td></tr>'
					+ '<tr><th>Last name</th><td><input type = "text" class = "field" value = "'
					+ user.lastName
					+ '"></td></tr>'
					+ '<tr><th>Email</th><td><input type = "text" class = "field" value = "'
					+ user.email
					+ '"></td></tr>'
					+ '<tr><th>Friends</th><td><input type = "text" class = "field" value = "'
					+ user.friendlist + '"></td></tr>'
					+ '<tr><td><input type = "submit" id = "save" value = "Save"></td></tr>');
}

$(document).ready(function() {
	rentacarProfil();

	$(document).on('submit', "#edit_profile", function(e) {
		e.preventDefault();
		var password = $(".field").eq(0).val();
		var firstName = $(".field").eq(1).val();
		var lastName = $(".field").eq(2).val();
		var email = $(".field").eq(3).val();
		var friends = $(".field").eq(4).val();
		$.ajax({
			type : 'PUT',
			url : "/users/test",
			contentType : 'application/json',
			data : JSON.stringify({
				"password" : password,
				"firstName" : firstName,
				"lastName" : lastName,
				"email" : email,
				"friendlist" : friends
			}),
			success: function(){
				$(location).attr('href',"/");
			}
		})
	})
})