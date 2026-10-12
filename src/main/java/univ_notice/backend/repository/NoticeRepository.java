package univ_notice.backend.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import univ_notice.backend.entity.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

  // 중복 방지를 위한 예시 메서드
  boolean existsByTitleAndDepartment(String title, String department);

  // title에 키워드가 포함된 데이터만 조회
  Page<Notice> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

  // 학과로 필터링
  Page<Notice> findByDepartmentOrderByPostedAtDesc(String department, Pageable pageable);

  // 제목+학과
  Page<Notice> findByTitleContainingIgnoreCaseAndDepartment(
      String keyword, String department, Pageable pageable);

  @Query("SELECT DISTINCT n.department FROM Notice n ORDER BY n.department")
  List<String> findDistinctDepartments();
}
