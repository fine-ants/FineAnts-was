package codesquad.fineants.domain.fcm_token;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FcmRepository extends JpaRepository<FcmToken, Long> {
	@Query("select f from FcmToken f where f.member.id = :memberId")
	List<FcmToken> findAllByMemberId(@Param("memberId") Long memberId);

	@Modifying
	@Query("delete from FcmToken f where f.token in (:tokens)")
	int deleteAllByTokens(@Param("tokens") List<String> tokens);
}
