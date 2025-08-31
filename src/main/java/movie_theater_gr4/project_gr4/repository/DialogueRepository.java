package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Integer> {

    @Query("SELECT d FROM Dialogue d WHERE (d.user1Id = :user1Id AND d.user2Id = :user2Id) OR (d.user1Id = :user2Id AND d.user2Id = :user1Id)")
    Optional<Dialogue> findByUserIds(@Param("user1Id") int user1Id, @Param("user2Id") int user2Id);

    @Query("SELECT d FROM Dialogue d WHERE d.user1Id = :userId OR d.user2Id = :userId ORDER BY d.createdAt DESC")
    List<Dialogue> findByUserId(@Param("userId") int userId);

    @Query("SELECT d FROM Dialogue d WHERE (d.user1Id = :userId OR d.user2Id = :userId) AND d.active = true ORDER BY d.createdAt DESC")
    List<Dialogue> findActiveDialoguesByUserId(@Param("userId") int userId);

    @Query("SELECT d FROM Dialogue d WHERE (d.user1Id = :userId OR d.user2Id = :userId) AND d.active = false ORDER BY d.createdAt DESC")
    List<Dialogue> findInactiveDialoguesByUserId(@Param("userId") int userId);
}