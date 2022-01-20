package br.com.mballem.demoajax.web.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.mballem.demoajax.domain.Categoria;
import br.com.mballem.demoajax.domain.Promocao;
import br.com.mballem.demoajax.dto.PromocaoDTO;
import br.com.mballem.demoajax.repository.CategoriaRepository;
import br.com.mballem.demoajax.repository.PromocaoRepository;
import br.com.mballem.demoajax.service.PromocaoDataTablesService;

@Controller
@RequestMapping("/promocao")
public class PromocaoController {

	private static Logger log = LoggerFactory.getLogger(PromocaoController.class);

	@Autowired
	CategoriaRepository categoriaRepository;

	@Autowired
	PromocaoRepository promocaoRepository;

	@GetMapping("/list")
	public String listarOfertas(ModelMap model) {
		Sort sort = Sort.by(Sort.Direction.DESC, "dataCadastro");
		PageRequest pageRequest = PageRequest.of(0, 8, sort);
		model.addAttribute("promocoes", promocaoRepository.findAll(pageRequest));
		return "promo-list";
	}
	
	//=========================== < DATA TABLES > ==================================
	@GetMapping("/tabela")
	public String showTabela() {
		return "promo-datatables";
	}
	
	
	@GetMapping("/datatables/server")
	public ResponseEntity<?> datatables(HttpServletRequest request){
		Map<String, Object> data = new PromocaoDataTablesService().execute(promocaoRepository, request);
		return ResponseEntity.ok(data);
	}
	
	@GetMapping("/delete/{id}")
	public ResponseEntity<?> excluirPromocao(@PathVariable("id") Long id){
		promocaoRepository.deleteById(id);		
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/edit/{id}")
	public ResponseEntity<?> preEditarPromocao(@PathVariable("id") Long id){
		Promocao promo = promocaoRepository.findById(id).get();
		return ResponseEntity.ok(promo);
	}
	
	/**
	 * 
	 * @param dto -> recebe as informações do atributo data na requisição ajax
	 * @param result -> recebe o resultado das anotations de validação
	 * @return
	 */
	@PostMapping("/edit")
	public ResponseEntity<?> editarPromocao(@Valid PromocaoDTO dto, BindingResult result){
	
		if(result.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			for(FieldError error: result.getFieldErrors()) {
				errors.put(error.getField(),error.getDefaultMessage());
			}
			
			return ResponseEntity.unprocessableEntity().body(errors);
		}
		
		Promocao promocao = promocaoRepository.findById(dto.getId()).get();
		promocao.setCategoria(dto.getCategoria());
		promocao.setDescricao(dto.getDescricao());
		promocao.setLinkImagem(dto.getLinkImagem());
		promocao.setPreco(dto.getPreco());
		promocao.setTitulo(dto.getTitulo());
		
		promocaoRepository.save(promocao);		
		
		return ResponseEntity.ok().build();
	}
	//=========================== < / DATA TABLES > ==================================
	

	// ================Autocomplete===================
	@GetMapping("/site")
	public ResponseEntity<?> autoCompleteByTermo(@RequestParam("termo") String termo) {
		List<String> sites = promocaoRepository.findSiteByTermo(termo);

		return ResponseEntity.ok(sites);
	}

	@GetMapping("/site/list")
	public String listarPorSite(@RequestParam("site") String site, ModelMap model) {
		// ordenação dos produtos na pagina
		Sort sort = Sort.by(Sort.Direction.DESC, "dataCadastro");
		// Requisição de 0 ate 8 items por pagina
		PageRequest pageRequest = PageRequest.of(0, 8, sort);
		model.addAttribute("promocoes", promocaoRepository.findBySite(site, pageRequest));
		return "promo-card";
	}
	// ================Autocomplete===================

	
	
	// ================Likes==========================
	@PostMapping("/like/{id}")
	public ResponseEntity<?> adicionarLikes(@PathVariable("id") Long id) {
		promocaoRepository.updateSomarLikes(id);
		int likes = promocaoRepository.findLikeById(id);
		return ResponseEntity.ok(likes);
	}
	// ================Likes==========================

	/**
	 * Listagem da pagina de promoções
	 * 
	 * @param page
	 * @param model
	 * @return
	 */
	@GetMapping("/list/ajax")
	public String listarCards(@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "site", defaultValue = "") String site, ModelMap model) {

		Sort sort = Sort.by(Sort.Direction.DESC, "dataCadastro");
		PageRequest pageRequest = PageRequest.of(page, 8, sort);

		if (site.isEmpty()) {
			model.addAttribute("promocoes", promocaoRepository.findAll(pageRequest));
		} else {
			model.addAttribute("promocoes", promocaoRepository.findBySite(site, pageRequest));

		}

		return "promo-card";
	}

	/**
	 * 
	 * @param promocao com a anotion @valid para monitorar as validações feitas no
	 *                 bean Promoção
	 * @param result   -> Resultados das validações que serão verificados da
	 *                 instrução if do metodo
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<?> salvarPromocao(@Valid Promocao promocao, BindingResult result) {

		// Verificando validações na variavel result
		if (result.hasErrors()) {
			// Montando uma estrutura chave valor com as mensagem de error e campo
			Map<String, String> errors = new HashMap<>();
			for (FieldError error : result.getFieldErrors()) {
				errors.put(error.getField(), error.getDefaultMessage());
			}

			return ResponseEntity.unprocessableEntity().body(errors);
		}

		log.info("Promocao{}", promocao.toString());
		promocao.setDataCadastro(LocalDateTime.now());
		promocaoRepository.save(promocao);
		return ResponseEntity.ok().build();
	}

	@ModelAttribute("categorias")
	List<Categoria> getCategorias() {
		return categoriaRepository.findAll();
	}

	@GetMapping("/add")
	public String abrirCadastro() {
		return "promo-add";
	}

}
