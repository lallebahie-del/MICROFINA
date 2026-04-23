package com.pfe.backend.security;

import com.pfe.backend.model.Utilisateur;
import com.pfe.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByNomutil(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        List<SimpleGrantedAuthority> authorities = utilisateur.getProfil().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                .collect(Collectors.toList());

        // Add the profile itself as a role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + utilisateur.getProfil().getIntitule()));

        return new User(utilisateur.getNomutil(), utilisateur.getPassword(), authorities);
    }
}
