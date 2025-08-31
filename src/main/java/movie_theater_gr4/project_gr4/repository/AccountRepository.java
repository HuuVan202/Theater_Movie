package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
//    @Query("SELECT a FROM Account a")
//    List<Account> getAccounts();

    @Query("SELECT a FROM Account a WHERE a.username = :username")
    Account findAccountByUsername(@Param("username") String username);

//    @Query("SELECT a FROM Account a WHERE a.username = :username")
//    Account getAccountByUsername(@Param("username") String username);

//    Account findAccountById(int id);


    @Query("SELECT a FROM Account a WHERE a.email = :identityCard")
    Account getAccountByIdentityCard(@Param("identityCard") String identityCard);


    @Query("SELECT a FROM Account a WHERE a.email = :phoneNumber")
    Account getAccountByPhoneNumber(@Param("phoneNumber") String phoneNumber);


    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Account getAccountByEmail(@Param("email") String email);

    boolean existsAccountByUsername(@Param("username") String username);

    boolean existsAccountByEmail(@Param("email") String email);

    Account findByUsername(String username);

    //    @Query("SELECT a FROM Account a WHERE a.isGoogle = :isGoogle")
//    boolean isAccountIsGoogle(Account account);

    @Query(value = """
    SELECT m.tier, COUNT(*)\s
                            FROM account a
                            JOIN member m ON a.account_id = m.account_id
                            GROUP BY m.tier
""", nativeQuery = true)
    List<Object[]> countAccountByRole();


}
