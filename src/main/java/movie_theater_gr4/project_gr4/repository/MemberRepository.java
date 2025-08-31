
package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.enums.Roles;
import movie_theater_gr4.project_gr4.model.Employee;
import movie_theater_gr4.project_gr4.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Page<Member> findAll(Pageable pageable);

    @Query("SELECT e FROM Member e WHERE " +
            "(LOWER(e.account.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.identityCard) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "e.account.role NOT IN :excludedRoleIds")
    Page<Member> searchMembersExcludeRoles(
            @Param("keyword") String keyword,
            @Param("excludedRoleIds") List<Roles> excludedRoleIds,
            Pageable pageable);

    // Thêm phương thức mới để lấy employeeId và account.status
    @Query("SELECT m.memberId AS memberId, m.account.status AS status FROM Member m WHERE m.memberId IN :memberIds")
    List<Map<String, Object>> findStatusByMemberIds(List<Long> memberIds);

}