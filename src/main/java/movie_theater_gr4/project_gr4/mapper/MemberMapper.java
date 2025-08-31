package movie_theater_gr4.project_gr4.mapper;

import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public MemberDTO toDTO(Member member) {
        if (member == null) return null;
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(member.getMemberId());
        dto.setAccountId(member.getAccount() != null ? member.getAccount().getAccountId() : null);
        dto.setUsername(member.getAccount() != null ? member.getAccount().getUsername() : null);
        dto.setFullName(member.getAccount() != null ? member.getAccount().getFullName() : null);
        dto.setEmail(member.getAccount() != null ? member.getAccount().getEmail() : null);
        dto.setScore(member.getScore());
        dto.setTier(member.getTier());
        return dto;
    }

    public MemberDTO toDTOViewMemberList(Member member) {
        if (member == null) return null;
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(member.getMemberId());
        dto.setAccountId(member.getAccount() != null ? member.getAccount().getAccountId() : null);
        dto.setUsername(member.getAccount() != null ? member.getAccount().getUsername() : null);
        dto.setFullName(member.getAccount() != null ? member.getAccount().getFullName() : null);
        dto.setEmail(member.getAccount() != null ? member.getAccount().getEmail() : null);
        dto.setScore(member.getScore());
        dto.setTier(member.getTier());
        dto.setAvatarUrl(member.getAccount() != null ? member.getAccount().getAvatarUrl() : null);
        return dto;
    }

    public Member toEntity(MemberDTO dto, Account account) {
        if (dto == null) return null;
        Member member = new Member();
        member.setMemberId(dto.getMemberId());
        member.setAccount(account);
        member.setScore(dto.getScore());
        member.setTier(dto.getTier());
        return member;
    }
}