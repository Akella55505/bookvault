package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.UserDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.UnknownUserRoleException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.PasswordChangeToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.PasswordChangeTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordChangeTokenRepository passwordChangeTokenRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public User findUserByEmail(String email) {
        Client possibleClient = clientRepository.findByEmail(email).orElse(null);
        User user = (possibleClient == null) ? employeeRepository.findByEmail(email) : possibleClient;
        if (user == null) {
            throw new UsernameNotFoundException("Username not found");
        }
        return user;
    }

    @Transactional
    public User updateUser(User user, UserDTO userDTO) {
        Role role = getRole(user);

        if (role == Role.CLIENT) {
            Client client = clientRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (!userDTO.getEmail().isBlank()) client.setEmail(userDTO.getEmail());
            if (!userDTO.getName().isBlank()) client.setName(userDTO.getName());
            user = clientRepository.save(client);
        } else if (role == Role.EMPLOYEE) {
            Employee employee = employeeRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (!userDTO.getEmail().isBlank()) employee.setEmail(userDTO.getEmail());
            if (!userDTO.getName().isBlank()) employee.setName(userDTO.getName());
            user = employeeRepository.save(employee);
        }

        return user;
    }

    public UserDTO mapUserToDTO(User user) {
        UserDTO userDTO = null;
        Role role = getRole(user);

        if (role == Role.CLIENT) {
            userDTO = modelMapper.map(user, ClientDTO.class);
        } else if (role == Role.EMPLOYEE) {
            userDTO = modelMapper.map(user, EmployeeDTO.class);
        }

        return userDTO;
    }

    public Role getRole(User user) {
        if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            return Role.CLIENT;
        } else if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
            return Role.EMPLOYEE;
        } else {
            throw new UnknownUserRoleException("No existing role matches current user");
        }
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUserByEmail(username);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        Long userId = user.getId();
        Role role = getRole(user);

        if (role == Role.CLIENT) {
            clientRepository.updatePasswordById(userId, newPassword);
        } else if (role == Role.EMPLOYEE) {
            employeeRepository.updatePasswordById(userId, newPassword);
        }
    }

    @Transactional
    public void updatePasswordByToken(User user, String token) {
        PasswordChangeToken changeToken = passwordChangeTokenRepository.findByToken(token).orElseThrow(() -> new NotFoundException("Token not found"));
        Long userId = user.getId();
        Role role = getRole(user);

        if (role == Role.CLIENT) {
            clientRepository.updatePasswordById(userId, changeToken.getNewPassword());
        } else if (role == Role.EMPLOYEE) {
            employeeRepository.updatePasswordById(userId, changeToken.getNewPassword());
        }

        passwordChangeTokenRepository.delete(changeToken);
    }

    public boolean checkUserExistsByEmail(String email) {
        Client possibleClient = clientRepository.findByEmail(email).orElse(null);
        User user = (possibleClient == null) ? employeeRepository.findByEmail(email) : possibleClient;
        return user != null;
    }
}
