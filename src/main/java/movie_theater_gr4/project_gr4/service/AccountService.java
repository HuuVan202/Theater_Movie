package movie_theater_gr4.project_gr4.service;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.dto.AccountGGDTO;
import movie_theater_gr4.project_gr4.dto.AccountProflieDTO;
import movie_theater_gr4.project_gr4.dto.AccountRegisterDTO;
import movie_theater_gr4.project_gr4.mapper.AccountMapper;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.enums.Roles;
import movie_theater_gr4.project_gr4.model.Member;
import movie_theater_gr4.project_gr4.repository.AccountRepository;
import movie_theater_gr4.project_gr4.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper
            , PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    public Account findAccountByUsername(String username) {
        Account account = accountRepository.findAccountByUsername(username);
        System.out.println("Account: " + account);
        System.out.println("RegisterDate: " + (account != null ? account.getRegisterDate() : "Account is null"));
        return account;
    }

    public boolean isExistingAccount(String username) {
        return accountRepository.existsAccountByUsername(username.trim());
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public boolean isExistingEmail(String email) {
        return accountRepository.existsAccountByEmail(email.trim());
    }

    public boolean isAccountIsGoogle(String email) {
        Account account = findAccountByEmail(email.trim());
        return account.getIsGoogle();
    }

    public AccountService(AccountRepository accountRepository, MemberRepository memberRepository,
                          AccountMapper accountMapper, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // Phương thức chung để tạo tài khoản và thành viên
    @Transactional
    public void createAccountAndMember(Account account) {
        account = accountRepository.save(account); // Lưu tài khoản
        Member member = new Member();
        member.setAccount(account);
        member.setScore(0);
        memberRepository.save(member);
    }

    @Transactional
    public int createAccount(AccountRegisterDTO accountRegisterDTO) {
        Account account = accountMapper.toAccountFromAccountRegisterDTO(accountRegisterDTO);
        account.setPassword(passwordEncoder.encode(accountRegisterDTO.getPassword()));
        account.setRole(Roles.MEMBER);
        account.setRegisterDate(LocalDate.now());
        account.setStatus(1);
        account.setIsGoogle(false);

        // Kiểm tra email trước khi lưu
        if (isExistingEmail(account.getEmail())) {
            throw new IllegalStateException("Email " + account.getEmail() + " đã được đăng ký.");
        }

        createAccountAndMember(account);

        return account.getAccountId();
    }

    @Transactional
    public Account createAccountByGG(AccountGGDTO accountGGDTO) {
        String email = accountGGDTO.getEmail().trim();
        Account existingAccount = findAccountByEmail(email);

        if (existingAccount != null) {
            // Kiểm tra xem tài khoản hiện có là tài khoản Google không
            if (!existingAccount.getIsGoogle()) {
                throw new IllegalStateException("Email " + email + " đã được đăng ký bằng tài khoản không phải Google");
            }
            // Tài khoản đã tồn tại và là tài khoản Google, trả về để đăng nhập
            return existingAccount;
        }

        // Tạo tài khoản Google mới
        Account account = accountMapper.toAccountFromAccountGGDTO(accountGGDTO);
        account.setUsername(email);
        account.setRole(Roles.MEMBER);
        account.setRegisterDate(LocalDate.now());
        account.setStatus(1);
        account.setIsGoogle(true);

        createAccountAndMember(account);
        return account;
    }

    // Phương thức tiện ích để lấy chi tiết tài khoản đầy đủ
    public Account getFullAccountDetails(String email) {
        Account account = findAccountByEmail(email.trim());
        if (account == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
        }
        return account;
    }

    public void updateAccount(AccountProflieDTO accountProflieDTO) {
        System.out.println("Account Update: " + accountProflieDTO);
        try {
            if (accountProflieDTO == null || accountProflieDTO.getUsername() == null) {
                throw new IllegalArgumentException("AccountProflieDTO hoặc username không được để trống");
            }
            Account account = findAccountByUsername(accountProflieDTO.getUsername());
            if (account == null) {
                throw new IllegalArgumentException("Tài khoản không tồn tại với username: " + accountProflieDTO.getUsername());
            } else {
                account.setFullName(accountProflieDTO.getFullName());
                account.setAddress(accountProflieDTO.getAddress());
                account.setPhoneNumber(accountProflieDTO.getPhoneNumber());
                account.setGender(accountProflieDTO.getGender());
                account.setDateOfBirth(accountProflieDTO.getDateOfBirth());
                account.setIdentityCard(accountProflieDTO.getIdentityCard());
                accountRepository.save(account);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật tài khoản: " + e.getMessage(), e);
        }
    }

    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("User not found");
        }
//        String role = switch (account.getRole().getRoleId()) {
//            case 1 -> "ADMIN";
//            case 2 -> "EMPLOYEE";
//            case 3 -> "MEMBER";
//            default -> "MEMBER";
//        };
        return Account.builder().username(account.getUsername())
                .password(account.getPassword())
                .role(account.getRole())
                .accountId(account.getAccountId())
                .build();
    }

    public Account findAccountById(int accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    Optional<Account> findAccountByIdNotifition(int accountId) {
        return accountRepository.findById(accountId);
    }

    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Account findAccountByEmail(String email) {
        return accountRepository.getAccountByEmail(email);
    }

    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

}