package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService")
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private static final String EMAIL = "emp@example.com";
    private static final Pageable PAGE = PageRequest.of(0, 10);

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().email(EMAIL).build();
        employeeDTO = new EmployeeDTO();
        employeeDTO.setEmail(EMAIL);
    }

    @Nested
    @DisplayName("getAllEmployees()")
    class GetAllEmployees {
        @Test
        @DisplayName("maps every employee on the page to a DTO")
        void mapsAll() {
            Employee emp2 = Employee.builder().email("other@example.com").build();
            EmployeeDTO dto2 = new EmployeeDTO();
            Page<Employee> page = new PageImpl<>(List.of(employee, emp2));

            when(employeeRepository.findAll(PAGE)).thenReturn(page);
            when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);
            when(modelMapper.map(emp2, EmployeeDTO.class)).thenReturn(dto2);

            List<EmployeeDTO> result = employeeService.getAllEmployees(PAGE);

            assertThat(result).containsExactly(employeeDTO, dto2);
        }

        @Test
        @DisplayName("returns empty list when repository page is empty")
        void emptyPage() {
            when(employeeRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of()));

            List<EmployeeDTO> result = employeeService.getAllEmployees(PAGE);

            assertThat(result).isEmpty();
            verifyNoInteractions(modelMapper);
        }

        @Test
        @DisplayName("uses pageable supplied by caller")
        void usesPageable() {
            when(employeeRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of()));

            employeeService.getAllEmployees(PAGE);

            verify(employeeRepository).findAll(PAGE);
        }
    }

    @Nested
    @DisplayName("getEmployeeByEmail()")
    class GetEmployeeByEmail {
        @Test
        @DisplayName("returns mapped DTO for existing employee")
        void returnsDto() {
            when(employeeRepository.findByEmail(EMAIL)).thenReturn(employee);
            when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

            EmployeeDTO result = employeeService.getEmployeeByEmail(EMAIL);

            assertThat(result).isEqualTo(employeeDTO);
            verify(employeeRepository).findByEmail(EMAIL);
        }

        @Test
        @DisplayName("returns null-mapped DTO when repository returns null")
        void repositoryReturnsNull() {
            when(employeeRepository.findByEmail(EMAIL)).thenReturn(null);
            when(modelMapper.map(null, EmployeeDTO.class)).thenReturn(null);

            EmployeeDTO result = employeeService.getEmployeeByEmail(EMAIL);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("updateEmployeeByEmail()")
    class UpdateEmployeeByEmail {
        @Test
        @DisplayName("maps DTO onto existing employee, saves, and returns mapped result")
        void savesAndReturns() {
            when(employeeRepository.findByEmail(EMAIL)).thenReturn(employee);
            when(employeeRepository.save(employee)).thenReturn(employee);
            doNothing().when(modelMapper).map(any(Employee.class), any(EmployeeDTO.class));
            when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

            EmployeeDTO result = employeeService.updateEmployeeByEmail(EMAIL, employeeDTO);

            verify(modelMapper).map(employee, employeeDTO);
            verify(employeeRepository).save(employee);
            assertThat(result).isEqualTo(employeeDTO);
        }

        @Test
        @DisplayName("generates token from repository-returned instance, not the pre-save one")
        void returnsMappedSavedInstance() {
            Employee savedEmployee = Employee.builder().email(EMAIL).build();
            EmployeeDTO savedDTO = new EmployeeDTO();

            when(employeeRepository.findByEmail(EMAIL)).thenReturn(employee);
            when(employeeRepository.save(employee)).thenReturn(savedEmployee);
            doNothing().when(modelMapper).map(any(Employee.class), any(EmployeeDTO.class));
            when(modelMapper.map(savedEmployee, EmployeeDTO.class)).thenReturn(savedDTO);

            EmployeeDTO result = employeeService.updateEmployeeByEmail(EMAIL, employeeDTO);

            verify(modelMapper).map(savedEmployee, EmployeeDTO.class);
            assertThat(result).isEqualTo(savedDTO);
        }
    }

    @Nested
    @DisplayName("deleteEmployeeByEmail()")
    class DeleteEmployeeByEmail {
        @Test
        @DisplayName("delegates to employeeRepository.deleteByEmail()")
        void delegates() {
            doNothing().when(employeeRepository).deleteByEmail(EMAIL);

            employeeService.deleteEmployeeByEmail(EMAIL);

            verify(employeeRepository).deleteByEmail(EMAIL);
        }
    }

    @Nested
    @DisplayName("addEmployee()")
    class AddEmployee {
        @Test
        @DisplayName("maps DTO to new Employee, saves, and returns mapped result")
        void mapsAndSaves() {
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            doNothing().when(modelMapper).map(any(EmployeeDTO.class), any(Employee.class));
            when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

            EmployeeDTO result = employeeService.addEmployee(employeeDTO);

            verify(modelMapper).map(eq(employeeDTO), any(Employee.class));
            verify(employeeRepository).save(any(Employee.class));
            assertThat(result).isEqualTo(employeeDTO);
        }

        @Test
        @DisplayName("returns DTO mapped from repository-returned instance, not the built one")
        void tokenFromSavedInstance() {
            Employee savedEmployee = Employee.builder().email(EMAIL).build();
            EmployeeDTO savedDTO = new EmployeeDTO();

            when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);
            doNothing().when(modelMapper).map(any(EmployeeDTO.class), any(Employee.class));
            when(modelMapper.map(savedEmployee, EmployeeDTO.class)).thenReturn(savedDTO);

            EmployeeDTO result = employeeService.addEmployee(employeeDTO);

            verify(modelMapper).map(savedEmployee, EmployeeDTO.class);
            assertThat(result).isEqualTo(savedDTO);
        }
    }
}
