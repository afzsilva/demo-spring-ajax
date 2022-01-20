$(document).ready(function() {
		
		//Submit do fomulario para o controller
		$("#form-add-promo").submit(function(event) {
			//Bloqueio do coportamento padrão do submit
			event.preventDefault();

			var promo = {};
			promo.linkPromocao = $("#linkPromocao").val();
			promo.descricao = $("#descricao").val();
			promo.preco = $("#preco").val();
			promo.titulo = $("#titulo").val();
			promo.categoria = $("#categoria").val();
			promo.linkImagem = $("#linkImagem").attr("src");
			promo.site = $("#site").text();

			console.log('promo > ', promo);

			$.ajax({
				method: "POST",
				url: "/promocao/save",
				//cache: false,
				data: promo,
				beforeSend:function(){
					//Removendo as mensagems
					$("span").closest('.error-span').remove();
					
					//remover bordas vermelhas
					$("#categoria").removeClass('is-invalid');
					$("#preco").removeClass('is-invalid');
					$("#linkPromoção").removeClass('is-invalid');
					$("#titulo").removeClass('is-invalid');
					
					//habilita o loading
					$("#form-add-promo").hide();
					$("#loader-form").addClass("loader").show();
				},
				
				success: function() {
					$("#form-add-promo").each(function(){
						this.reset();
					});
					$("#linkImagem").attr("src","/images/promo-dark.png");
					$("#site").text("");
					
					$("#alert")
					.removeClass('alert alert-danger')
					.addClass("alert alert-success")
					.text("OK! Promoção cadastrada com sucesso");
				},
				//Retorno da validação
				statusCode:{
					422:function(xhr){
						console.log('status error',xhr.status);
						var errors = $.parseJSON(xhr.responseText);
						$.each(errors, function(key,val){							
							$("#" + key).addClass("is-invalid");
							$("#error-" + key)
							.addClass("invalid-feedback")
							.append("<span class='error-span'>"+val+"</span>")
						});
						
						
					}
				},
				
				error: function(xhr) {
					console.log("> error: ", xhr.responseText);
					$("#alert").addClass("alert alert-danger").text("Não foi possivel salvar esta promooção");
				},
				complete:function(){
					$("#loader-form").fadeOut(800,function(){
						$("#form-add-promo").fadeIn(250);
						$("#loader-form").removeClass("loader");
					});
				}
			})

		});//submit
	

	//função para capturar meta tags
	$("#linkPromocao").on('change', function() {

		var url = $(this).val();

		if (url.length > 7) {
			$.ajax({
				method: "POST",
				url: "/meta/info?url=" + url,
				cache: false,
				//Antes da instrução principal
				beforeSend: function() {
					$("#alert").removeClass("alert alert-danger alert-success").text("");
					$("#titulo").val("");
					$("#site").text("");
					$("#linkImagem").attr("src", "");
					$("#loader-img").addClass("loader");
				},

				//instrução principal
				success: function(data) {
					//console.log(data);
					$("#titulo").val(data.title);
					$("#site").text(data.site.replace("@", ""));
					$("#linkImagem").attr("src", data.image);
				},
				//Tratamentoo de erros 404 e 500
				statusCode: {
					404: function() {
						$("#alert").addClass("alert alert-danger").text("Informação não recuperada");
						$("#linkImagem").attr("src", "/images/promo-dark.png");
					}
				},
				error: function() {
					$("#alert").addClass("alert alert-danger").text("Ops...deu ruim");
					$("#linkImagem").attr("src", "/images/promo-dark.png");
				},
				//Sempre é executado por ultimo
				complete: function() {
					$("#loader-img").removeClass("loader");
				},
			})
		}
	});
});//Dcoument Ready





