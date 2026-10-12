package univ_notice.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import univ_notice.backend.entity.Keyword;
import univ_notice.backend.entity.User;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

  // 현재 사용자가 등록한 키워드 전체 조회
  List<Keyword> findByUser(User user);

  // 키워드 중복확인
  boolean existsByNameAndUser(String name, User user);

  //
  Optional<Keyword> findByNameAndUser(String name, User user);

  // 유저 탈퇴하면 키워드도 삭제
  void deleteByUser(User user);
}
