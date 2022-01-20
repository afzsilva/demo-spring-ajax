package br.com.mballem.demoajax.service;


import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import br.com.mballem.demoajax.domain.Promocao;
import br.com.mballem.demoajax.repository.PromocaoRepository;

public class PromocaoDataTablesService {
	
	private String[] cols = {"id","titulo","site","linkPromocao","descricao","linkImagem","preco","likes","dataCadastro","categoria.titulo"};
	
	
	public Map<String, Object> execute(PromocaoRepository repository, HttpServletRequest request){
		
		
		int start = Integer.parseInt(request.getParameter("start"));
		int length = Integer.parseInt(request.getParameter("length"));
		int draw = Integer.parseInt(request.getParameter("draw"));
		
		int current = currentPage(start, length);
		String column = columnName(request); 
		Sort.Direction direction = orderBy(request);
		String search = searchBy(request);
		
		//Paginação 
		Pageable pageable = PageRequest.of(current,length, direction, column);
		Page<Promocao> page =  queryBy(search,repository, pageable);
		
		LinkedHashMap<String, Object> json = new LinkedHashMap<>();
		json.put("draw", draw);
		json.put("recordsTotal", page.getTotalElements());
		json.put("recordsFiltered", page.getTotalElements());
		json.put("data", page.getContent());
		return json;
	}
	
	
	private String searchBy(HttpServletRequest request) {		
		return request.getParameter("search[value]").isEmpty()?
				"":request.getParameter("search[value]");
	}

	
	private Page<Promocao> queryBy(String search,PromocaoRepository repository, Pageable pageable) {
		
		if(search.isEmpty()) {
			return repository.findAll(pageable);			
		}		
		
		if(search.matches("^[0-9]+([.,][0-9]{2})?$")) {
			search = search.replace(",", ".");
			return repository.findByPreco(new BigDecimal(search), pageable);
		}
		
		return repository.findByTituloOrSiteOrCategoria(search,pageable);
	}

	
	/**
	 * Metodo de ordenação de uma coluna
	 * @param request
	 * @return sort
	 */
	private Direction orderBy(HttpServletRequest request) {
		String order = request.getParameter("order[0][dir]");
		Sort.Direction sort = Sort.Direction.ASC;
		if(order.equalsIgnoreCase("desc")) {
			sort = Sort.Direction.DESC;
		}
		
		return sort;
	}


	/**
	 * 
	 * @param request
	 * @return nome da coluna passado por parametro no request
	 */
	private String columnName(HttpServletRequest request) {
		int iCol = Integer.parseInt(request.getParameter("order[0][column]"));
		return cols[iCol];
	}


	/**
	 * 
	 * @param start
	 * @param length
	 * @return int da pagina atual
	 */
	private int currentPage(int start, int length) {
		//0	 	1		2
		//0-9	10-19	20-29
		return start / length;
	}
	
}
