package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.enums.Roles;
import movie_theater_gr4.project_gr4.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @EntityGraph(attributePaths = {"account"})
    @Query("SELECT e FROM Employee e WHERE e.account.role NOT IN :roles")
//    Page<Employee> findAllEmployeesExcludeRoles(List<Integer> roleIds, Pageable pageable);
    Page<Employee> findAllEmployeesExcludeRoles(@Param("roles") java.util.List<Roles> roles, Pageable pageable);

    @EntityGraph(attributePaths = {"account"})
    @Query("SELECT e FROM Employee e WHERE e.account.role NOT IN :excludedRoles")
    List<Employee> findAllEmployeesExcludeRoles(@Param("excludedRoles") List<Roles> excludedRoles);

    @EntityGraph(attributePaths = {"account"})
    Page<Employee> findByAccount_FullNameContainingIgnoreCaseOrAccount_IdentityCardContainingIgnoreCaseOrAccount_EmailContainingIgnoreCaseOrAccount_PhoneNumberContainingIgnoreCaseOrAccount_AddressContainingIgnoreCase(
            String fullName, String identityCard, String email, String phoneNumber, String address, Pageable pageable);

    // Thêm phương thức mới để lấy employeeId và account.status
    @Query("SELECT e.employeeId AS employeeId, e.account.status AS status FROM Employee e WHERE e.employeeId IN :employeeIds")
    List<Map<String, Object>> findStatusByEmployeeIds(List<Long> employeeIds);

    @EntityGraph(attributePaths = {"account"})
    @Query("SELECT e FROM Employee e WHERE " +
            "(LOWER(e.account.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.identityCard) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.account.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "e.account.role NOT IN :excludedRoleIds")
    Page<Employee> searchEmployeesExcludeRoles(
            @Param("keyword") String keyword,
            @Param("excludedRoleIds") List<Roles> excludedRoleIds,
            Pageable pageable);

    @Query("SELECT new map(e.employeeId as employeeId, e.account.status as status) FROM Employee e WHERE e.employeeId = :id")
    Map<String, Object> findStatusByEmployeeId(@Param("id") int id);


}
