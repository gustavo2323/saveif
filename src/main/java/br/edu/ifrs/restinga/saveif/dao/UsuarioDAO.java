package br.edu.ifrs.restinga.saveif.dao;

import br.edu.ifrs.restinga.saveif.modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioDAO extends PagingAndSortingRepository<Usuario, Integer> {

    Page<Usuario> findByNome(String nome, Pageable pageable);

    public Usuario findByEmail(String email);
    

}