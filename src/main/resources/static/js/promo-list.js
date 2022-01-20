var pageNumber = 0;

$(document).ready(function() {
	$("#loader-img").hide();
	$("#fim-btn").hide();
});

//Efeito infinite scroll
$(window).scroll(function() {
	var scrollTop = $(this).scrollTop();
	var conteudo = $(document).height() - $(window).height();

	//console.log('scrollTop:', scrollTop, ' | ', 'Conteudo ', conteudo);
	if (scrollTop >= conteudo) {
		//console.log("*****action*****");
		pageNumber++;
		setTimeout(function() {
			loadByScrollBar(pageNumber);
		}, 200);
	}

});

//Paginação
function loadByScrollBar(pageNumber) {
	
	var site = $("#autocomplete-input").val();
	
	$.ajax({
		method: "GET",
		url: "/promocao/list/ajax",
		data: {
			page: pageNumber,
			site: site
		},
		beforeSend: function() {
			$("#loader-img").show();
		},
		success: function(response) {

			//console.log("Resposta ",response);
			console.log("Lista >>> ", response.length);

			if (response.length > 150) {

				$(".row").fadeIn(250, function() {
					$(this).append(response);
				});

			} else {
				$("#fim-btn").show();
				$("#loader-img").removeClass("loader");
			}
		},
		error: function(xhr) {
			alert("Ops, Ocorreu um erro " + xhr.status + " - " + xhr.statusText);
		},
		complete: function() {
			$("#loader-img").hide();
		}
	});

}

//adicionar likes
$(document).on('click', "button[id*='likes-btn-']", function() {
	var id = $(this).attr("id").split("-")[2];
	console.log("id >>> ", id);

	$.ajax({
		method: "POST",
		url: "/promocao/like/" + id,
		success: function(response) {
			$("#likes-count-" + id).text(response)
		},
		error: function(xhr) {
			alert: ("Ops, ocorreu um erro " + xhr.status + ", " + xhr.statusText);
		},
	});


});

//autocomplete
$("#autocomplete-input").autocomplete({
	source: function(request, response) {
		$.ajax({
			method: "GET",
			url: "/promocao/site",
			data: {
				termo: request.term
			},
			success: function(result) {
				response(result);
			},
		})
	}
});
/**
Padrão de seleção de elementos com js
Seleciona
Adiciona um escutador de evento
Declara uma ação para tal evento

 */
//Click para fazer a busca pelo nome do site
$("#autocomplete-submit").on("click", function() {
	
	var site = $("#autocomplete-input").val();
	
	$.ajax({
		method: "GET",
		url: "/promocao/site/list",
		data: {
			site: site
		},
		beforeSend: function() {
			
			pageNumber = 0;
			$("#fim-btn").hide();
			$(".row").fadeOut(400, function(){
				$(this).empty();
			});
		},
		success:function(response){
			$(".row").fadeIn(250,function(){
				$(this).append(response)
			});
		},
		error:function(xhr){
			alert("Ops, algo de errado: "+xhr.status+" , "+xhr.statusText);
		}

	});
});

//AJAX REVERSE
var totalOfertas = 0;

$(document).ready(function(){
	init();
});

function init(){
	console.log("dwr init...");
	
	dwr.engine.setActiveReverseAjax(true);
	dwr.engine.setErrorHandler(error);
	
	DWRAlertaPromocoes.init();
}

function error(exception){
	console.log("dwr error: ",exception);
	
}

//Recebe as informações do servidor no cliente
function showButton(count){
	totalOfertas = totalOfertas + count;
	$("#btn-alert").show(function(){
		$(this).attr("style","display: block")
		.text("Veja "+totalOfertas+" novas oferta(s)!");
	});
}

$("#btn-alert").on("click", function() {
			
	$.ajax({
		method: "GET",
		url: "/promocao/list/ajax",
		data: {
			page: 0
		},
		beforeSend: function() {			
			pageNumber = 0;
			totalOfertas = 0;
			$("#fim-btn").hide();
			$("#loader-mg").addClass("loader");
			$("#btn-alert").attr("style", "display:none;");
			$(".row").fadeOut(400, function(){
				$(this).empty();
			});
		},
		success:function(response){
			$("#loader-mg").removeClass("loader");
			$(".row").fadeIn(250,function(){
				$(this).append(response)
			});
		},
		error:function(xhr){
			alert("Ops, algo de errado: "+xhr.status+" , "+xhr.statusText);
		}

	});
});
