package br.edu.ifrs.restinga.saveif.controller;

import br.edu.ifrs.restinga.saveif.dao.UsuarioDAO;
import br.edu.ifrs.restinga.saveif.modelo.Usuario;
import br.edu.ifrs.restinga.saveif.aut.ForbiddenException;
import br.edu.ifrs.restinga.saveif.aut.UsuarioAut;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping(path = "/api")
public class Usuarios {

    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @RequestMapping(path = "/usuarios", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario inserir(@AuthenticationPrincipal UsuarioAut usuarioAut, @RequestBody Usuario usuario) {
        usuario.setId(0);
        usuario.setSenha(PASSWORD_ENCODER.encode(usuario.getNovaSenha()));

        if (usuarioAut == null || !usuarioAut.getUsuario().getPermissoes().contains("administrador")) {
            ArrayList<String> permissao = new ArrayList<String>();
            permissao.add("usuario");
            usuario.setPermissoes(permissao);
        }
        Usuario usuarioSalvo = usuarioDAO.save(usuario);
        return usuarioSalvo;
    }

    @Autowired
    UsuarioDAO usuarioDAO;

    @PreAuthorize("hasAuthority('administrador')")
    @RequestMapping(path = "/usuarios", method = RequestMethod.GET)
    public Iterable<Usuario> listar(@RequestParam(required = false, defaultValue = "0") int pagina) {
        PageRequest pageRequest = new PageRequest(pagina, 5);
        return usuarioDAO.findAll(pageRequest);
    }

    @RequestMapping(path = "/usuarios/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Usuario> recuperar(@AuthenticationPrincipal UsuarioAut usuarioAut, @PathVariable int id) {
        
        if (usuarioAut.getUsuario().getId() == id
                || usuarioAut.getUsuario().getPermissoes().contains("administrador")) {
            
            Optional<Usuario> findById = usuarioDAO.findById(id);
            
            if (findById.isPresent()) 
                return ResponseEntity.ok(findById.get());            
            else
                return ResponseEntity.notFound().build();
            
           
        } else
            throw new ForbiddenException("Não é permitido acessar dados de outros usuários");
        
    }
    
   
  
    

    @RequestMapping(path = "/usuarios/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void atualizar(@PathVariable int id, @RequestBody Usuario usuario) {
        if (usuarioDAO.existsById(id)) {
            usuario.setId(id);
            usuarioDAO.save(usuario);
        }
    }

    @RequestMapping(path = "/usuarios/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void apagar(@PathVariable int id) {
        if (usuarioDAO.existsById(id)) {
            usuarioDAO.deleteById(id);
        }

    }

    public static final String SEGREDO
            = "string grande para c*, usada como chave para assinatura! Queijo!";

    @RequestMapping(path = "/usuarios/login", method = RequestMethod.GET)
    public ResponseEntity<Usuario> login(@AuthenticationPrincipal UsuarioAut usuarioAut)
            throws IllegalArgumentException, UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256(SEGREDO);
        Calendar agora = Calendar.getInstance();
        agora.add(Calendar.MINUTE, 4);
        Date expira = agora.getTime();

        String token = JWT.create()
                .withClaim("id", usuarioAut.getUsuario().getId()).
                withExpiresAt(expira).
                sign(algorithm);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.set("token", token);

        return new ResponseEntity<>(usuarioAut.getUsuario(), respHeaders, HttpStatus.OK);
    }





}