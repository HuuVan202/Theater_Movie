package movie_theater_gr4.project_gr4.service;

import jakarta.validation.ConstraintViolation;
import movie_theater_gr4.project_gr4.dto.AccountDTO;
import movie_theater_gr4.project_gr4.dto.EmployeeDTO;
import movie_theater_gr4.project_gr4.enums.Roles;
import movie_theater_gr4.project_gr4.mapper.EmployeeMapper;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Employee;
import movie_theater_gr4.project_gr4.repository.AccountRepository;
import movie_theater_gr4.project_gr4.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper,
                           AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "employees", key = "#pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        List<Roles> excludedRoleIds = List.of(Roles.ADMIN);
        Page<Employee> employeePage = employeeRepository.findAllEmployeesExcludeRoles(excludedRoleIds, pageable);
        return employeePage.map(employeeMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "allEmployees", unless = "#result == null || #result.isEmpty()")
    public List<EmployeeDTO> getAllEmployees() {
        try {
            List<Roles> excludedRoleIds = List.of(Roles.ADMIN);
            List<EmployeeDTO> employees = employeeRepository.findAllEmployeesExcludeRoles(excludedRoleIds).stream()
                    .map(employeeMapper::toDTO)
                    .collect(Collectors.toList());
            return employees;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch employees: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "employeeSearch", key = "#keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.content.isEmpty()")
    public Page<EmployeeDTO> searchEmployees(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees(pageable);
        }
        List<Roles> excludedRoleIds = List.of(Roles.ADMIN);
        Page<Employee> employeePage = employeeRepository.searchEmployeesExcludeRoles(keyword, excludedRoleIds, pageable);
        return employeePage.map(employeeMapper::toDTO);
    }

    @CacheEvict(value = {"employees", "allEmployees", "employeeSearch"}, allEntries = true)
    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    @CacheEvict(value = {"employees", "allEmployees", "employeeSearch"}, allEntries = true)
    public void deleteEmployee(int employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + employeeId));
        Account account = employee.getAccount();
        if (account != null) {
            account.setStatus(0);
            accountRepository.save(account);
        }
    }

    @CacheEvict(value = {"employees", "allEmployees", "employeeSearch"}, allEntries = true)
    public void unlockEmployee(int employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + employeeId));
        Account account = employee.getAccount();
        if (account != null) {
            account.setStatus(1);
            accountRepository.save(account);
        }
    }

    @CacheEvict(value = {"employees", "allEmployees", "employeeSearch"}, allEntries = true)
    public Map<String, List<String>> importEmployees(List<EmployeeDTO> employees) {
        List<String> errors = new ArrayList<>();
        List<String> emailsToSend = new ArrayList<>();
        List<String> usernames = new ArrayList<>();
        List<String> rawPasswords = new ArrayList<>();

        for (int i = 0; i < employees.size(); i++) {
            EmployeeDTO dto = employees.get(i);
            // Validate DTO
            List<String> validationErrors = validateEmployeeDTO(dto);
            if (!validationErrors.isEmpty()) {
                errors.add("Dòng " + (i + 2) + ": " + String.join("; ", validationErrors));
                continue;
            }

//            // Check for duplicate email, identity card, or phone number
//            if (accountRepository.getAccountByEmail(dto.getEmail()) != null) {
//                errors.add("Dòng " + (i + 2) + ": Email đã tồn tại: " + dto.getEmail());
//                continue;
//            }
//            if (accountRepository.getAccountByIdentityCard(dto.getIdentityCard()) != null) {
//                errors.add("Dòng " + (i + 2) + ": CMND/CCCD đã tồn tại: " + dto.getIdentityCard());
//                continue;
//            }
//            if (accountRepository.getAccountByPhoneNumber(dto.getPhoneNumber()) != null) {
//                errors.add("Dòng " + (i + 2) + ": Số điện thoại đã tồn tại: " + dto.getPhoneNumber());
//                continue;
//            }

            // Generate unique username and random password
            String username = generateUniqueUsername();
            String rawPassword = generateRandomPassword();

            // Create Account
            Account account = new Account();
            account.setUsername(username);
            account.setFullName(dto.getFullName());
            account.setEmail(dto.getEmail());
            account.setIdentityCard(dto.getIdentityCard());
            account.setPhoneNumber(dto.getPhoneNumber());
            account.setAddress(dto.getAddress());
            account.setDateOfBirth(dto.getDateOfBirth()); // Thêm dòng này
            account.setGender(dto.getGender());          // Thêm dòng này
            account.setPassword(passwordEncoder.encode(rawPassword));
            account.setStatus(1);
            account.setRole(Roles.EMPLOYEE);
            account.setRegisterDate(LocalDate.now());

            Account savedAccount = accountRepository.save(account);
            try {
                savedAccount = accountRepository.save(account);
            } catch (Exception e) {
                errors.add("Dòng " + (i + 2) + ": Lỗi lưu tài khoản: " + e.getMessage());
                continue;
            }

            // Create Employee
            Employee employee = employeeMapper.toEntity(dto, savedAccount);
            try {
                employeeRepository.save(employee);
                // Collect data for email sending
                emailsToSend.add(dto.getEmail());
                usernames.add(username);
                rawPasswords.add(rawPassword);
            } catch (Exception e) {
                errors.add("Dòng " + (i + 2) + ": Lỗi lưu nhân viên: " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Lỗi khi import: " + String.join("; ", errors));
        }

        // Create the Map to return email data
        Map<String, List<String>> emailData = new HashMap<>();
        emailData.put("emails", emailsToSend);
        emailData.put("usernames", usernames);
        emailData.put("passwords", rawPasswords);

        return emailData;
    }

    private String generateUniqueUsername() {
        Random random = new Random();
        String username;
        do {
            username = "EMP" + String.format("%07d", random.nextInt(10000000));
        } while (accountRepository.existsAccountByUsername(username));
        return username;
    }

    private List<String> validateEmployeeDTO(EmployeeDTO dto) {
        List<String> errors = new ArrayList<>();

        // Validate fullName
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            errors.add("Họ và tên không được để trống");
        } else if (dto.getFullName().length() < 2 || dto.getFullName().length() > 100) {
            errors.add("Họ và tên phải từ 2 đến 100 ký tự");
        }

        // Validate identityCard
        if (dto.getIdentityCard() == null || dto.getIdentityCard().trim().isEmpty()) {
            errors.add("CMND/CCCD không được để trống");
        } else if (!Pattern.matches("^\\d{12}$", dto.getIdentityCard())) {
            errors.add("CMND/CCCD phải là 12 chữ số");
        }

        // Validate email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.add("Email không được để trống");
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", dto.getEmail())) {
            errors.add("Email không hợp lệ");
        }

        // Validate phoneNumber
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            errors.add("Số điện thoại không được để trống");
        } else if (!Pattern.matches("^\\d{10}$", dto.getPhoneNumber())) {
            errors.add("Số điện thoại phải là 10 chữ số");
        }

        // Validate address
        if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
            errors.add("Địa chỉ không được để trống");
        } else if (dto.getAddress().length() < 5 || dto.getAddress().length() > 200) {
            errors.add("Địa chỉ phải từ 5 đến 200 ký tự");
        }

        // Validate hireDate
        if (dto.getHireDate() == null) {
            errors.add("Ngày tuyển dụng không được để trống");
        } else if (dto.getHireDate().isAfter(LocalDate.now())) {
            errors.add("Ngày tuyển dụng không được sau ngày hiện tại");
        }

        // Validate position
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            errors.add("Chức vụ không được để trống");
        } else if (dto.getPosition().length() < 2 || dto.getPosition().length() > 50) {
            errors.add("Chức vụ phải từ 2 đến 50 ký tự");
        }


        return errors;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    public Optional<Employee> findById(int employeeId) {
        return employeeRepository.findById(employeeId);
    }

    @CacheEvict(value = {"employees", "allEmployees", "employeeSearch"}, allEntries = true)
    public EmployeeDTO addEmployee(Account account, Employee employee, AccountDTO accountDTO) throws Exception {

        if (accountRepository.findAccountByUsername(accountDTO.getUsername()) != null) {
            throw new Exception("Tên đăng nhập đã tồn tại. Vui lòng thử lại.");
        }

//        if (accountRepository.getAccountByEmail(accountDTO.getEmail()) != null) {
//            throw new Exception("Email đã tồn tại. Vui lòng thử lại.");
//        }
//
//        if (accountRepository.getAccountByIdentityCard(accountDTO.getIdentityCard()) != null) {
//            throw new Exception("CMND/CCCD đã tồn tại. Vui lòng thử lại.");
//        }
//
//        if (accountRepository.getAccountByPhoneNumber(accountDTO.getPhoneNumber()) != null) {
//            throw new Exception("Số điện thoại đã tồn tại. Vui lòng thử lại.");
//        }

        LocalDate today = LocalDate.now();
        LocalDate minAgeDate = today.minusYears(18);
        if (accountDTO.getDateOfBirth().isAfter(minAgeDate)) {
            throw new Exception("Nhân viên phải từ 18 tuổi trở lên.");
        }

        if (employee.getHireDate().isBefore(accountDTO.getDateOfBirth())) {
            throw new Exception("Ngày tuyển dụng không được trước ngày sinh.");
        }

        account.setRole(Roles.EMPLOYEE);
        account.setRegisterDate(LocalDate.now());
        account.setStatus(1);
        account.setUsername(accountDTO.getUsername());
        account.setFullName(accountDTO.getFullName());
        account.setEmail(accountDTO.getEmail());
        account.setPhoneNumber(accountDTO.getPhoneNumber());
        account.setIdentityCard(accountDTO.getIdentityCard());
        account.setAddress(accountDTO.getAddress());
        account.setDateOfBirth(accountDTO.getDateOfBirth());
        account.setGender(accountDTO.getGender());

        Account savedAccount = accountRepository.save(account);
        employee.setAccount(savedAccount);
        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toDTO(savedEmployee);
    }

    public EmployeeDTO editEmployee(int employeeId, Account account, Employee employee, String confirmPassword, AccountDTO accountDTO) throws Exception {
        Employee existingEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new Exception("Không tìm thấy nhân viên: " + employeeId));
        Account existingAccount = existingEmployee.getAccount();

        // Kiểm tra mật khẩu nếu được cung cấp
        if (accountDTO.getPassword() != null && !accountDTO.getPassword().isBlank()) {
            if (!accountDTO.getPassword().equals(confirmPassword)) {
                throw new Exception("Xác nhận mật khẩu không khớp");
            }
            account.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        } else {
            account.setPassword(existingAccount.getPassword());
        }

        // Kiểm tra tuổi tối thiểu (18 tuổi)
        if (accountDTO.getDateOfBirth() != null) {
            LocalDate today = LocalDate.now();
            LocalDate minAgeDate = today.minusYears(18);
            if (accountDTO.getDateOfBirth().isAfter(minAgeDate)) {
                throw new Exception("Nhân viên phải từ 18 tuổi trở lên.");
            }
        } else {
            accountDTO.setDateOfBirth(existingAccount.getDateOfBirth());
        }

        System.out.println("Hello" + accountDTO.getDateOfBirth());

        // Kiểm tra ngày tuyển dụng không trước ngày sinh
        if (employee.getHireDate() != null && accountDTO.getDateOfBirth() != null) {
            if (employee.getHireDate().isBefore(accountDTO.getDateOfBirth())) {
                throw new Exception("Ngày tuyển dụng không được trước ngày sinh.");
            }
        } else if (employee.getHireDate() == null) {
            employee.setHireDate(existingEmployee.getHireDate());
        }

        // Cập nhật thông tin tài khoản
        account.setAccountId(existingAccount.getAccountId());
        account.setUsername(existingAccount.getUsername()); // Không cho phép thay đổi username
        account.setRole(existingAccount.getRole());
        account.setRegisterDate(existingAccount.getRegisterDate());
        account.setStatus(accountDTO.getStatus() != null ? accountDTO.getStatus() : existingAccount.getStatus());
        account.setEmail(accountDTO.getEmail());
        account.setPhoneNumber(accountDTO.getPhoneNumber());
        account.setIdentityCard(accountDTO.getIdentityCard());
        account.setFullName(accountDTO.getFullName() != null && !accountDTO.getFullName().isBlank() ? accountDTO.getFullName() : existingAccount.getFullName());
        account.setAddress(accountDTO.getAddress() != null && !accountDTO.getAddress().isBlank() ? accountDTO.getAddress() : existingAccount.getAddress());
        account.setDateOfBirth(accountDTO.getDateOfBirth());
        account.setGender(accountDTO.getGender() != null && !accountDTO.getGender().isBlank() ? accountDTO.getGender() : existingAccount.getGender());

        // Cập nhật thông tin nhân viên
        employee.setEmployeeId(employeeId);
        employee.setHireDate(employee.getHireDate());
        employee.setPosition(employee.getPosition() != null && !employee.getPosition().isBlank() ? employee.getPosition() : existingEmployee.getPosition());

        // Lưu tài khoản và nhân viên
        Account savedAccount = accountRepository.save(account);
        employee.setAccount(savedAccount);
        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toDTO(savedEmployee);
    }
}
