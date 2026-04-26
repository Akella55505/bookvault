package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.UserDTO;
import com.epam.rd.autocode.spring.project.exception.UnknownUserRoleException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.PasswordChangeToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.PasswordChangeTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
public class UserServiceTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordChangeTokenRepository passwordChangeTokenRepository;

    private UserService userService;

    private static final Client CLIENT = Client.builder()
            .id(1L)
            .email("client@email.com")
            .name("Client")
            .build();
    private static final Employee EMPLOYEE = Employee.builder()
            .id(2L)
            .email("employee@email.com")
            .name("Employee")
            .build();

    @BeforeEach
    void setUp() {
        userService = new UserService(clientRepository, employeeRepository, passwordChangeTokenRepository, new ModelMapper());
    }

    @Nested
    @DisplayName("finds and returns user")
    class ReturnUser {
        @Test
        @DisplayName("user exists in clientRepository")
        void returnFromClientRepository() {
            String email = CLIENT.getEmail();

            when(clientRepository.findByEmail(eq(email))).thenReturn(Optional.of(CLIENT));

            User client = userService.findUserByEmail(email);

            assertEquals(CLIENT, client);
            verify(employeeRepository, times(0)).findByEmail(eq(email));
        }

        @Test
        @DisplayName("user exists in employeeRepository")
        void returnFromEmployeeRepository() {
            String email = EMPLOYEE.getEmail();

            when(clientRepository.findByEmail(eq(email))).thenReturn(Optional.empty());
            when(employeeRepository.findByEmail(eq(email))).thenReturn(EMPLOYEE);

            User employee = userService.findUserByEmail(email);

            assertEquals(EMPLOYEE, employee);
        }

        @Test
        @DisplayName("throws UsernameNotFoundException when user not found")
        void throwsException() {
            when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.findByEmail(anyString())).thenReturn(null);

            assertThrows(UsernameNotFoundException.class, () -> userService.findUserByEmail("email"));
        }
    }

    @Nested
    @DisplayName("updates user with provided UserDTO")
    class UpdateUser {
        static Stream<UserDTO> provideUserDTOs() {
            return Stream.of(new UserDTO("email", "name"),
                    new UserDTO("email", ""),
                    new UserDTO("", "name"),
                    new UserDTO("", ""));
        }

        @ParameterizedTest
        @MethodSource("provideUserDTOs")
        @DisplayName("user is a client")
        void updateClient(UserDTO userDTO) {
            Client client = Client.builder()
                    .id(1L)
                    .build();

            when(clientRepository.findById(eq(client.getId()))).thenReturn(Optional.of(client));
            when(clientRepository.save(any())).thenReturn(client);

            userService.updateUser(client, userDTO);

            if (!userDTO.getEmail().isBlank()) {
                assertEquals(userDTO.getEmail(), client.getEmail());
            }
            if (!userDTO.getName().isBlank()) {
                assertEquals(userDTO.getName(), client.getName());
            }
            verifyNoInteractions(employeeRepository);
        }

        @ParameterizedTest
        @MethodSource("provideUserDTOs")
        @DisplayName("user is an employee")
        void updateEmployee(UserDTO userDTO) {
            Employee employee = Employee.builder()
                    .id(1L)
                    .build();

            when(employeeRepository.findById(eq(employee.getId()))).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any())).thenReturn(employee);

            userService.updateUser(employee, userDTO);

            if (!userDTO.getEmail().isBlank()) {
                assertEquals(userDTO.getEmail(), employee.getEmail());
            }
            if (!userDTO.getName().isBlank()) {
                assertEquals(userDTO.getName(), employee.getName());
            }
            verifyNoInteractions(clientRepository);
        }

        @Test
        @DisplayName("throws exception when user is of unknown role")
        void throwsException() {
            assertThrows(UnknownUserRoleException.class, () -> {
                userService.updateUser(new User() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return List.of(new SimpleGrantedAuthority("ROLE_UNKNOWN"));
                    }
                }, new UserDTO());
            });
        }
    }

    @Nested
    @DisplayName("maps provided user to UserDTO")
    class MapUser {
        @Test
        @DisplayName("user is a client")
        void mapsClientToUserDTO() {
            UserDTO userDTO = userService.mapUserToDTO(CLIENT);

            assertEquals(CLIENT.getEmail(), userDTO.getEmail());
            assertEquals(CLIENT.getName(), userDTO.getName());
        }

        @Test
        @DisplayName("user is an employee")
        void mapsEmployeeToUserDTO() {
            UserDTO userDTO = userService.mapUserToDTO(EMPLOYEE);

            assertEquals(EMPLOYEE.getEmail(), userDTO.getEmail());
            assertEquals(EMPLOYEE.getName(), userDTO.getName());
        }
    }

    @Test
    @DisplayName("returns the same user as findUserByEmail() method")
    void returnsUser() {
        when(clientRepository.findByEmail(eq(CLIENT.getEmail()))).thenReturn(Optional.of(CLIENT));

        User expected = userService.findUserByEmail(CLIENT.getEmail());
        User actual = (User) userService.loadUserByUsername(CLIENT.getEmail());

        assertEquals(expected, actual);
    }

    @Nested
    @DisplayName("updates password of a provided user")
    class UpdatePassword {
        @Test
        @DisplayName("user is a client")
        void updatesClientsPassword() {
            userService.updatePassword(CLIENT, "newPassword");
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("user is an employee")
        void updatesEmployeesPassword() {
            userService.updatePassword(EMPLOYEE, "newPassword");
            verifyNoInteractions(clientRepository);
        }
    }

    @Nested
    @DisplayName("updates password by an existing password change token")
    class UpdatePasswordByToken {
        private static final String token = "token";
        private static final PasswordChangeToken changeToken = PasswordChangeToken.builder()
                .id(1L)
                .token(token)
                .userEmail("email@mail.com")
                .newPassword("newPassword")
                .build();

        @BeforeEach
        void setUp() {
            when(passwordChangeTokenRepository.findByToken(eq(token))).thenReturn(Optional.of(changeToken));
            doNothing().when(passwordChangeTokenRepository).delete(any());
        }

        @Test
        @DisplayName("user is a client")
        void updatesClientsPassword() {
            doNothing().when(clientRepository).updatePasswordById(eq(CLIENT.getId()), eq(changeToken.getNewPassword()));
            userService.updatePasswordByToken(CLIENT, token);
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("user is an employee")
        void updatesEmployeesPassword() {
            doNothing().when(employeeRepository).updatePasswordById(eq(EMPLOYEE.getId()), eq(changeToken.getNewPassword()));
            userService.updatePasswordByToken(EMPLOYEE, token);
            verifyNoInteractions(clientRepository);
        }
    }

    @Nested
    @DisplayName("checks if user exists in the db")
    class CheckUserExists {
        @Test
        @DisplayName("user exists in clientRepository")
        void returnFromClientRepository() {
            String email = CLIENT.getEmail();

            when(clientRepository.findByEmail(eq(email))).thenReturn(Optional.of(CLIENT));

            assertTrue(userService.checkUserExistsByEmail(email));
            verify(employeeRepository, times(0)).findByEmail(eq(email));
        }

        @Test
        @DisplayName("user exists in employeeRepository")
        void returnFromEmployeeRepository() {
            String email = EMPLOYEE.getEmail();

            when(clientRepository.findByEmail(eq(email))).thenReturn(Optional.empty());
            when(employeeRepository.findByEmail(eq(email))).thenReturn(EMPLOYEE);

            assertTrue(userService.checkUserExistsByEmail(email));
        }

        @Test
        @DisplayName("returns false when user not found")
        void throwsException() {
            when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.findByEmail(anyString())).thenReturn(null);

            assertFalse(userService.checkUserExistsByEmail(anyString()));
        }
    }
}
