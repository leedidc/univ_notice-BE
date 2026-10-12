package univ_notice.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import univ_notice.backend.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);

  Optional<User> findById(String id);
}
