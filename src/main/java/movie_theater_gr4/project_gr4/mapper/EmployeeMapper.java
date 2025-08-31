package movie_theater_gr4.project_gr4.mapper;

import movie_theater_gr4.project_gr4.dto.EmployeeDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) return null;
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFullName(employee.getAccount() != null ? employee.getAccount().getFullName() : null);
        dto.setIdentityCard(employee.getAccount() != null ? employee.getAccount().getIdentityCard() : null);
        dto.setEmail(employee.getAccount() != null ? employee.getAccount().getEmail() : null);
        dto.setPhoneNumber(employee.getAccount() != null ? employee.getAccount().getPhoneNumber() : null);
        dto.setAddress(employee.getAccount() != null ? employee.getAccount().getAddress() : null);
        dto.setAvatarUrl(employee.getAccount() != null ? employee.getAccount().getAvatarUrl() : null);
        dto.setDateOfBirth(employee.getAccount() != null ? employee.getAccount().getDateOfBirth() : null);
        dto.setGender(employee.getAccount() != null ? employee.getAccount().getGender() : null);
        dto.setHireDate(employee.getHireDate());
        dto.setPosition(employee.getPosition());
        return dto;
    }

    public Employee toEntity(EmployeeDTO dto, Account account) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setHireDate(dto.getHireDate());
        employee.setPosition(dto.getPosition());
        employee.setAccount(account);

        if (account != null) {
            account.setDateOfBirth(dto.getDateOfBirth());
            account.setGender(dto.getGender());          
        }
        return employee;
    }

}