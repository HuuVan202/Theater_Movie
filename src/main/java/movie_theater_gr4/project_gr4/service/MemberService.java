
package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.dto.AccountDTO;
import movie_theater_gr4.project_gr4.dto.EmployeeDTO;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.enums.Roles;
import movie_theater_gr4.project_gr4.mapper.MemberMapper;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Employee;
import movie_theater_gr4.project_gr4.model.Member;
import movie_theater_gr4.project_gr4.repository.AccountRepository;
import movie_theater_gr4.project_gr4.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private AccountRepository accountRepository;

    //    public List<MemberDTO> getAllMembers() {
//        List<Member> members = memberRepository.findAll();
//        return members.stream()
//                .map(memberMapper::toDTO)
//                .collect(Collectors.toList());
//    }
    public Page<MemberDTO> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(member -> memberMapper.toDTOViewMemberList(member));
    }

    public MemberDTO addMember(Account account, Member member, AccountDTO accountDTO) throws Exception {
        if (accountRepository.findAccountByUsername(accountDTO.getUsername()) != null) {
            throw new Exception("Tên đăng nhập đã tồn tại. Vui lòng thử lại.");
        }

        if (accountRepository.getAccountByEmail(accountDTO.getEmail()) != null) {
            throw new Exception("Email đã tồn tại. Vui lòng thử lại.");
        }

        if (accountRepository.getAccountByIdentityCard(accountDTO.getIdentityCard()) != null) {
            throw new Exception("CMND/CCCD đã tồn tại. Vui lòng thử lại.");
        }

        if (accountRepository.getAccountByPhoneNumber(accountDTO.getPhoneNumber()) != null) {
            throw new Exception("Số điện thoại đã tồn tại. Vui lòng thử lại.");
        }

        LocalDate today = LocalDate.now();
        LocalDate minAgeDate = today.minusYears(18);
        if (accountDTO.getDateOfBirth().isAfter(minAgeDate)) {
            throw new Exception("Thành viên phải từ 18 tuổi trở lên.");
        }

        account.setRole(Roles.MEMBER);
        account.setRegisterDate(LocalDate.now());
        account.setStatus(1);
        account.setUsername(accountDTO.getUsername());
        account.setFullName(accountDTO.getFullName());
        account.setEmail(accountDTO.getEmail());
        account.setPhoneNumber(accountDTO.getPhoneNumber());
        account.setIdentityCard(accountDTO.getIdentityCard());
        account.setGender(accountDTO.getGender());
        account.setDateOfBirth(accountDTO.getDateOfBirth());
        account.setAddress(accountDTO.getAddress());

        Account savedAccount = accountRepository.save(account);
        member.setAccount(savedAccount);
        member.setScore(0);
        Member savedMember = memberRepository.save(member);

        return memberMapper.toDTOViewMemberList(savedMember);
    }

    public Page<MemberDTO> searchMembers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMembers(pageable);
        }
        List<Roles> excludedRoleIds = List.of(Roles.ADMIN);
        Page<Member> memberPage = memberRepository.searchMembersExcludeRoles(keyword, excludedRoleIds, pageable);
        return memberPage.map(memberMapper::toDTOViewMemberList);
    }

    public Optional<Member> findById(int memberId) {
        return memberRepository.findById((long) memberId);
    }

    public void deleteMember(int memberId) {
        Member member = memberRepository.findById((long)memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên: " + memberId));
        Account account = member.getAccount();
        if (account != null) {
            account.setStatus(0);
            accountRepository.save(account);
        }
    }

    public void unlockMember(int memberId) {
        Member member = memberRepository.findById((long)memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên: " + memberId));
        Account account = member.getAccount();
        if (account != null) {
            account.setStatus(1);
            accountRepository.save(account);
        }
    }
}