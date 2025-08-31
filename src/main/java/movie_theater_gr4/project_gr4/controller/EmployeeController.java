package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.service.CloudinaryService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import movie_theater_gr4.project_gr4.dto.AccountDTO;
import movie_theater_gr4.project_gr4.dto.EmployeeDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Employee;
import movie_theater_gr4.project_gr4.repository.EmployeeRepository;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.EmployeeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Controller
@RequestMapping("/admin/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final PasswordEncoder passwordEncoder;


    public EmployeeController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String viewEmployees(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String sortBy,
                                @RequestParam(required = false) String direction,
                                Model model) {
//        String sortField = (sortBy != null) ? sortBy : "account.registerDate";
        String sortField = (sortBy != null) ? sortBy : "employeeId";

        String sortDirection = (direction != null) ? direction : "desc";

        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<EmployeeDTO> employeePage = employeeService.searchEmployees(keyword, pageable);

        List<Long> employeeIds = employeePage.getContent().stream()
                .map(EmployeeDTO::getEmployeeId)
                .map(Integer::longValue)
                .collect(Collectors.toList());

        List<Map<String, Object>> statusList = employeeRepository.findStatusByEmployeeIds(employeeIds);

        List<Map<String, Object>> employeeList = employeePage.getContent().stream().map(dto -> {
            Map<String, Object> employeeMap = new HashMap<>();
            employeeMap.put("employee", dto);

            String statusText = "Unknown";
            for (Map<String, Object> statusMap : statusList) {
                if (dto.getEmployeeId() == ((Number) statusMap.get("employeeId")).longValue()) {
                    Integer status = (Integer) statusMap.get("status");
                    statusText = (status != null && status == 1) ? "Active" : (status != null && status == 0) ? "Inactive" : "Unknown";
                    break;
                }
            }
            employeeMap.put("status", statusText);
            return employeeMap;
        }).collect(Collectors.toList());

        model.addAttribute("sortBy", sortField);
        model.addAttribute("direction", sortDirection);
        model.addAttribute("employees", employeeList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "admin/employee/employeesUseFrag";
    }

    @GetMapping("/add")
    public String addEmployeeForm(Model model, HttpSession session) {
        List<Account> accounts = accountService.getAllAccounts();

        model.addAttribute("account", new Account());
        model.addAttribute("employee", new Employee());
        model.addAttribute("accountDTO", new AccountDTO());

        List<String> allUsernames = accounts != null ? accounts.stream().map(Account::getUsername).toList() : List.of();
        List<String> allEmails = accounts != null ? accounts.stream().map(Account::getEmail).toList() : List.of();
        List<String> allIdentityCards = accounts != null ? accounts.stream().map(Account::getIdentityCard).toList() : List.of();
        List<String> allPhoneNumbers = accounts != null ? accounts.stream().map(Account::getPhoneNumber).toList() : List.of();

        model.addAttribute("existingUsernames", allUsernames);
        model.addAttribute("existingEmails", allEmails);
        model.addAttribute("existingIdentityCards", allIdentityCards);
        model.addAttribute("existingPhoneNumbers", allPhoneNumbers);
        session.setAttribute("lastUsernames", allUsernames);
        session.setAttribute("lastEmails", allEmails);
        session.setAttribute("lastIdentityCards", allIdentityCards);
        session.setAttribute("lastPhoneNumbers", allPhoneNumbers);

        return "admin/employee/add-employee_Frag";
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            password.append(chars.charAt(randomIndex));
        }
        return password.toString();
    }

    private void sendWelcomeEmail(String toEmail, String username, String rawPassword) throws MessagingException, UnsupportedEncodingException {
        String subject = "Tài khoản nhân viên mới tại MoonCinema";

        // Tạo context và set biến cho Thymeleaf
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("password", rawPassword);

        // Render template
        String content = templateEngine.process("/newEmployee", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setFrom("noreply@top1cinema.vn", "MoonCinema");
        helper.setText(content, true);

        mailSender.send(message);
    }

    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute("account") Account account, BindingResult accountBindingResult,
                              @Valid @ModelAttribute("employee") Employee employee, BindingResult employeeBindingResult,
                              Model model,
                              @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                              @RequestParam(value = "useDefaultAvatar", required = false) String useDefaultAvatar,
                              @Valid @ModelAttribute("accountDTO") AccountDTO accountDTO,
                              BindingResult accountDTOBindingResult,
                              RedirectAttributes redirectAttributes) {

        if (accountBindingResult.hasErrors() || employeeBindingResult.hasErrors() || accountDTOBindingResult.hasErrors()) {
            System.out.println("---- VALIDATION ERRORS ----");

            if (accountBindingResult.hasErrors()) {
                System.out.println("Account Errors:");
                accountBindingResult.getFieldErrors().forEach(err ->
                        System.out.println("Field: " + err.getField() + " - " + err.getDefaultMessage()));
            }

            if (employeeBindingResult.hasErrors()) {
                System.out.println("Employee Errors:");
                employeeBindingResult.getFieldErrors().forEach(err ->
                        System.out.println("Field: " + err.getField() + " - " + err.getDefaultMessage()));
            }

            if (accountDTOBindingResult.hasErrors()) {
                System.out.println("AccountDTO Errors:");
                accountDTOBindingResult.getFieldErrors().forEach(err ->
                        System.out.println("Field: " + err.getField() + " - " + err.getDefaultMessage()));
            }

            model.addAttribute("account", account);
            model.addAttribute("employee", employee);
            model.addAttribute("accountDTO", accountDTO);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào.");
            return "admin/employee/add-employee_Frag";
        }

        try {
            accountDTO.trimFields();

            if ("true".equals(useDefaultAvatar)) {
                account.setAvatarUrl("http://res.cloudinary.com/dycfyoh8r/image/upload/v1755106434/avatars/avatar_default.jpg");
            } else if (avatarFile != null && !avatarFile.isEmpty()) {
                Map uploadResult = cloudinaryService.uploadAvatarEmployee(avatarFile);
                String imageUrl = (String) uploadResult.get("secure_url");
                account.setAvatarUrl(imageUrl);
            } else {
                account.setAvatarUrl("http://res.cloudinary.com/dycfyoh8r/image/upload/v1755106434/avatars/avatar_default.jpg");
            }

            String rawPassword = generateRandomPassword();
            account.setPassword(passwordEncoder.encode(rawPassword));
            employeeService.addEmployee(account, employee, accountDTO);

            sendWelcomeEmail(account.getEmail(), account.getUsername(), rawPassword);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm nhân viên " + employee.getAccount().getFullName() + " thành công!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/employee/add-employee_Frag";
        }

        return "redirect:/admin/employees?page=0";
    }

    private String processAvatarFile(MultipartFile avatarFile, String username) {
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String contentType = avatarFile.getContentType();
                if (!contentType.startsWith("image/")) {
                    throw new RuntimeException("File phải là hình ảnh!");
                }

                if (avatarFile.getSize() > 5 * 1024 * 1024) {
                    throw new RuntimeException("File không được vượt quá 5MB!");
                }

                String uploadDir = "uploads/employees";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFileName = avatarFile.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String fileName = username + "_" + System.currentTimeMillis() + fileExtension;

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return "/uploads/employees/" + fileName;
            } else {
                return "/img/employees/default-avatar.png";
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public String viewEmployeeDetail(@PathVariable("id") int employeeId, Model model) {
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Account account = employee.getAccount();

        AccountDTO accountDTO = AccountDTO.builder()
                .username(account.getUsername())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .identityCard(account.getIdentityCard())
                .address(account.getAddress())
                .dateOfBirth(account.getDateOfBirth() != null ? account.getDateOfBirth() : LocalDate.now())
                .gender(account.getGender())
                .status(account.getStatus())
                .registerDate(account.getRegisterDate())
                .avatarUrl(account.getAvatarUrl())
                .build();

        model.addAttribute("account", account);
        model.addAttribute("employee", employee);
        model.addAttribute("accountDTO", accountDTO);

        return "admin/employee/employee-detail";
    }

    @GetMapping("/edit/{id}")
    public String editEmployeeForm(@PathVariable int id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Account account = employee.getAccount();

        AccountDTO accountDTO = AccountDTO.builder()
                .username(account.getUsername())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .identityCard(account.getIdentityCard())
                .address(account.getAddress())
                .dateOfBirth(account.getDateOfBirth())
                .gender(account.getGender())
                .status(account.getStatus())
                .registerDate(account.getRegisterDate())
                .avatarUrl(account.getAvatarUrl())
                .build();

        model.addAttribute("account", account);
        model.addAttribute("employee", employee);
        model.addAttribute("accountDTO", accountDTO);

        List<Account> accounts = accountService.getAllAccounts();
        List<String> allUsernames = accounts != null ? accounts.stream().map(Account::getUsername).toList() : List.of();
        List<String> allEmails = accounts != null ? accounts.stream().map(Account::getEmail).filter(Objects::nonNull).toList() : List.of();
        List<String> allIdentityCards = accounts != null ? accounts.stream().map(Account::getIdentityCard).filter(Objects::nonNull).toList() : List.of();
        List<String> allPhoneNumbers = accounts != null ? accounts.stream().map(Account::getPhoneNumber).filter(Objects::nonNull).toList() : List.of();

        model.addAttribute("existingUsernames", allUsernames);
        model.addAttribute("existingEmails", allEmails);
        model.addAttribute("existingIdentityCards", allIdentityCards);
        model.addAttribute("existingPhoneNumbers", allPhoneNumbers);

        if (accountDTO.getDateOfBirth() == null) {
            accountDTO.setDateOfBirth(LocalDate.now());
        }

        return "admin/employee/edit-employee_Frag";
    }

    @PostMapping("/edit/{id}")
    public String editEmployee(@PathVariable int id,
                               @Valid @ModelAttribute("account") Account account,
                               BindingResult accountBindingResult,
                               @Valid @ModelAttribute("employee") Employee employee,
                               BindingResult employeeBindingResult,
                               @Valid @ModelAttribute("accountDTO") AccountDTO accountDTO,
                               BindingResult accountDTOBindingResult,
                               @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                               @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                               @RequestParam(value = "useDefaultAvatar", required = false) String useDefaultAvatar,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        boolean hasErrors = false;

        if (accountBindingResult.hasErrors()) {
            model.addAttribute("accountError", "Có lỗi ở thông tin tài khoản");
            hasErrors = true;
        }

        if (employeeBindingResult.hasErrors()) {
            model.addAttribute("employeeError", "Có lỗi ở thông tin nhân viên");
            hasErrors = true;
        }

        if (accountDTOBindingResult.hasErrors()) {
            model.addAttribute("accountDTOError", "Có lỗi ở thông tin mở rộng");
            hasErrors = true;
        }

        try {
            accountDTO.trimFields();
            if ("true".equals(useDefaultAvatar)) {
                account.setAvatarUrl("http://res.cloudinary.com/dycfyoh8r/image/upload/v1755106434/avatars/avatar_default.jpg");
            } else if (avatarFile != null && !avatarFile.isEmpty()) {
                Map uploadResult = cloudinaryService.uploadAvatarEmployee(avatarFile);
                String imageUrl = (String) uploadResult.get("secure_url");
                account.setAvatarUrl(imageUrl);
            } else {
                Account oldAccount = accountService.findAccountById(account.getAccountId());
                if (oldAccount != null) {
                    account.setAvatarUrl(oldAccount.getAvatarUrl());
                }
            }
            employeeService.editEmployee(id, account, employee, confirmPassword, accountDTO);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật nhân viên " + employee.getAccount().getFullName() + " thành công!");
            return "redirect:/admin/employees?page=0";

        } catch (Exception e) {
            model.addAttribute("account", account);
            model.addAttribute("employee", employee);
            model.addAttribute("accountDTO", accountDTO);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/employee/edit-employee_Frag";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            int employeeId = employee.getEmployeeId();
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vô hiệu hóa nhân viên " + employee.getAccount().getFullName() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/employees?page=0";
    }

    @GetMapping("/unlock/{id}")
    public String unlockEmployee(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            int employeeId = employee.getEmployeeId();
            employeeService.unlockEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Kích hoạt nhân viên " + employee.getAccount().getFullName() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/employees?page=0";
    }

    @GetMapping("/export")
    public void exportEmployees(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=employees.xlsx");
            response.setCharacterEncoding("UTF-8");

            List<EmployeeDTO> employees = employeeService.getAllEmployees();

            try (Workbook workbook = new XSSFWorkbook()) {
                CreationHelper createHelper = workbook.getCreationHelper();

                CellStyle dateCellStyle = workbook.createCellStyle();
                dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

                Sheet sheet = workbook.createSheet("Employees");

                Row headerRow = sheet.createRow(0);
                String[] headers = {
                        "Email", "Full Name", "Date of Birth", "Gender", "Phone Number",
                        "Address", "Identity Card", "Hire Date", "Position"
                };
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                int rowNum = 1;
                for (EmployeeDTO employee : employees) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(employee.getEmail() != null ? employee.getEmail() : "");
                    row.createCell(1).setCellValue(employee.getFullName() != null ? employee.getFullName() : "");
                    Cell dobCell = row.createCell(2);
                    if (employee.getDateOfBirth() != null) {
                        dobCell.setCellValue(employee.getDateOfBirth());
                        dobCell.setCellStyle(dateCellStyle);
                    } else {
                        dobCell.setCellValue("");
                    }
                    row.createCell(3).setCellValue(employee.getGender() != null ?
                            (employee.getGender().equals("M") ? "Nam" : "Nữ") : "");
                    row.createCell(4).setCellValue(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");
                    row.createCell(5).setCellValue(employee.getAddress() != null ? employee.getAddress() : "");
                    row.createCell(6).setCellValue(employee.getIdentityCard() != null ? employee.getIdentityCard() : "");
                    Cell hireDateCell = row.createCell(7);
                    if (employee.getHireDate() != null) {
                        hireDateCell.setCellValue(employee.getHireDate());
                        hireDateCell.setCellStyle(dateCellStyle);
                    } else {
                        hireDateCell.setCellValue("");
                    }
                    row.createCell(8).setCellValue(employee.getPosition() != null ? employee.getPosition() : "");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (OutputStream out = response.getOutputStream()) {
                    workbook.write(out);
                    out.flush();
                }
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/import")
    public String importEmployees(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel để import.");
            return "redirect:/admin/employees?page=0";
        }

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<EmployeeDTO> employees = new ArrayList<>();

            // Validate header row
            Row headerRow = sheet.getRow(0);
            String[] expectedHeaders = {
                    "Email", "Full Name", "Date of Birth", "Gender", "Phone Number",
                    "Address", "Identity Card", "Hire Date", "Position"
            };
            for (int i = 0; i < expectedHeaders.length; i++) {
                if (headerRow == null || headerRow.getCell(i) == null ||
                        !expectedHeaders[i].equalsIgnoreCase(getCellValue(headerRow.getCell(i)))) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "File Excel không đúng định dạng. Vui lòng kiểm tra tiêu đề cột.");
                    return "redirect:/admin/employees?page=0";
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) continue;

                Account account = new Account();
                EmployeeDTO employee = new EmployeeDTO();
                employee.setEmail(getCellValue(row.getCell(0)));
                employee.setFullName(getCellValue(row.getCell(1)));
                try {
                    employee.setDateOfBirth(parseExcelDate(row.getCell(2)));
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Định dạng ngày sinh không hợp lệ tại dòng " + (i + 1) + ": " + e.getMessage());
                    return "redirect:/admin/employees?page=0";
                }
                String gender = getCellValue(row.getCell(3)).trim();
                if (gender.matches("(?i)^(Nam|Male|NAM|MALE)$")) {
                    account.setGender("M");
                } else if (gender.matches("(?i)^(Nữ|Nu|Female|NỮ|NU)$")) {
                    employee.setGender("F");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Giới tính không hợp lệ tại dòng " + (i + 1) + ": " + gender);
                    return "redirect:/admin/employees?page=0";
                }
                employee.setPhoneNumber(getCellValue(row.getCell(4)));
                employee.setAddress(getCellValue(row.getCell(5)));
                employee.setIdentityCard(getCellValue(row.getCell(6)));
                try {
                    employee.setHireDate(parseExcelDate(row.getCell(7)));
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Định dạng ngày tuyển dụng không hợp lệ tại dòng " + (i + 1) + ": " + e.getMessage());
                    return "redirect:/admin/employees?page=0";
                }
                employee.setPosition(getCellValue(row.getCell(8)));
                employees.add(employee);
            }

            try {
                Map<String, List<String>> emailData = employeeService.importEmployees(employees);
                List<String> emails = emailData.get("emails");
                List<String> usernames = emailData.get("usernames");
                List<String> passwords = emailData.get("passwords");

                int successfulEmails = 0;
                List<String> emailErrors = new ArrayList<>();
                for (int i = 0; i < emails.size(); i++) {
                    try {
                        sendWelcomeEmail(emails.get(i), usernames.get(i), passwords.get(i));
                        successfulEmails++;
                    } catch (MessagingException e) {
                        emailErrors.add("Lỗi gửi email cho " + emails.get(i) + ": " + e.getMessage());
                    }
                }

                String successMessage = "Import thành công " + employees.size() + " nhân viên.";
                if (!emailErrors.isEmpty()) {
                    successMessage += " Gửi được " + successfulEmails + "/" + emails.size() + " email.";
                    redirectAttributes.addFlashAttribute("errorMessage", String.join("; ", emailErrors));
                }
                redirectAttributes.addFlashAttribute("successMessage", successMessage);
            } catch (ConstraintViolationException e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Dữ liệu không hợp lệ (có thể trùng email, số điện thoại, hoặc CCCD) tại dòng " + (employees.size() + 1));
                return "redirect:/admin/employees?page=0";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Lỗi khi import dữ liệu: " + e.getMessage());
                return "redirect:/admin/employees?page=0";
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi đọc file Excel: " + e.getMessage());
            return "redirect:/admin/employees?page=0";
        }

        return "redirect:/admin/employees?page=0";
    }

    private LocalDate parseExcelDate(Cell cell) throws Exception {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null; // Allow null for optional dates
        }

        // Handle numeric date cells
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            try {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception e) {
                throw new Exception("Không thể đọc ngày từ định dạng số: " + cell.toString());
            }
        }

        // Handle text cells
        String raw = getCellValue(cell).trim();
        if (raw.isEmpty()) {
            return null; // Allow empty strings
        }

        // Try parsing with multiple formats
        String[] formats = {
                "dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy", "yyyy/MM/dd", "dd-MM-yyyy",
                "yyyy.MM.dd", "dd.MM.yyyy", "MM-dd-yyyy", "dd MM yyyy", "yyyy MM dd",
                "dd/MM/yy", "yy-MM-dd", "MM/dd/yy", "yy/MM/dd", "dd-MM-yy",
                "yyyyMMdd", "ddMMyyyy", "MMddyyyy", "ddMMyy", "yyMMdd",
                "d/M/yyyy", "d-M-yyyy", "M/d/yyyy", "M-d-yyyy",
                "d/M/yy", "d-M-yy", "M/d/yy", "M-d-yy",
                "dd/MMM/yyyy", "dd-MMM-yyyy", "MMM-dd-yyyy", "MMM dd yyyy",
                "dd MMMM yyyy", "dd-MMMM-yyyy", "MMMM dd yyyy"
        };

        // Try English locale first
        for (String fmt : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fmt, Locale.ENGLISH);
                return LocalDate.parse(raw, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        // Try Vietnamese locale with normalized month names
        try {
            String normalized = normalizeMonthNames(raw);
            for (String fmt : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fmt, new Locale("vi", "VN"));
                    return LocalDate.parse(normalized, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        // Log the raw value for debugging
        System.err.println("Failed to parse date: " + raw);
        throw new Exception("Không thể đọc ngày: " + raw + ". Vui lòng sử dụng định dạng hợp lệ (ví dụ: dd/MM/yyyy, yyyy-MM-dd).");
    }

    private String normalizeMonthNames(String raw) {
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("tháng 1|jan|january|thg 1", "Jan");
        monthMap.put("tháng 2|feb|february|thg 2", "Feb");
        monthMap.put("tháng 3|mar|march|thg 3", "Mar");
        monthMap.put("tháng 4|apr|april|thg 4", "Apr");
        monthMap.put("tháng 5|may|thg 5", "May");
        monthMap.put("tháng 6|jun|june|thg 6", "Jun");
        monthMap.put("tháng 7|jul|july|thg 7", "Jul");
        monthMap.put("tháng 8|aug|august|thg 8", "Aug");
        monthMap.put("tháng 9|sep|september|thg 9", "Sep");
        monthMap.put("tháng 10|oct|october|thg 10", "Oct");
        monthMap.put("tháng 11|nov|november|thg 11", "Nov");
        monthMap.put("tháng 12|dec|december|thg 12", "Dec");

        String normalized = raw.toLowerCase();
        for (Map.Entry<String, String> entry : monthMap.entrySet()) {
            String[] patterns = entry.getKey().split("\\|");
            for (String pattern : patterns) {
                normalized = normalized.replaceAll("\\b" + pattern + "\\b", entry.getValue());
            }
        }
        return normalized;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellValue(cell);
                if (val != null && !val.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };

        return value.replaceAll("[\\p{Cntrl}\\p{Space}]+", " ").trim();
    }
}