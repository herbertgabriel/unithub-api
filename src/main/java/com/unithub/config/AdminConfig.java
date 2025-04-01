package com.unithub.config;

import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.RoleRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class AdminConfig implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AdminConfig(RoleRepository roleRepository, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Verifica se a role ADMIN existe
        Role roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
        if (roleAdmin == null) {
            // Cria a role ADMIN caso não exista
            roleAdmin = new Role();
            roleAdmin.setName(Role.Values.ADMIN.name());
            roleAdmin = roleRepository.save(roleAdmin);
            System.out.println("Role ADMIN criada com sucesso.");
        }

        // Verifica se o usuário admin já existe
        var userAdmin = userRepository.findByEmail("admin@example.com");
        if (userAdmin.isEmpty()) {
            // Cria o usuário admin caso não exista
            var user = new User();
            user.setEmail("admin@example.com");
            user.setPassword(bCryptPasswordEncoder.encode("senha123"));
            user.setName("admin");
            user.setTelephone("81987654321");
            user.setRoles(Set.of(roleAdmin)); // Associa a role ADMIN ao usuário
            userRepository.save(user);
            System.out.println("Usuário admin criado com sucesso.");
        } else {
            System.out.println("Usuário admin já existe.");
        }

    }
}
