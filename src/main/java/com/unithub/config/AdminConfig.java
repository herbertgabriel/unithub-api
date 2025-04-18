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
        
        // Verifica e cria as roles padrão, se necessário
        for (Role.Values roleValue : Role.Values.values()) {
            Role role = roleRepository.findByName(roleValue.name());
            if (role == null) {
                role = new Role();
                role.setName(roleValue.name());
                roleRepository.save(role);
                System.out.println("Role " + roleValue.name() + " criada com sucesso.");
            }
        }

        // Verifica se o usuário admin já existe
        var userAdmin = userRepository.findByEmail("admin@example.com");
        if (userAdmin.isEmpty()) {
            Role roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
            if (roleAdmin == null) {
                throw new RuntimeException("Role ADMIN não encontrada. Verifique a configuração.");
            }

            // Cria o usuário admin caso não exista
            var user = new User();
            user.setEmail("admin@example.com"); // Coloque E-mail do Admin
            user.setPassword(bCryptPasswordEncoder.encode("senha123")); // Coloque uma senha provisoria
            user.setName("admin"); //Coloque nome do Usuário
            user.setRoles(Set.of(roleAdmin)); // Associa a role ADMIN ao usuário
            userRepository.save(user);
            System.out.println("Usuário admin criado com sucesso.");
        } else {
            System.out.println("Usuário admin já existe.");
        }
    }
}
