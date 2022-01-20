package br.com.mballem.demoajax.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.mballem.demoajax.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

}
