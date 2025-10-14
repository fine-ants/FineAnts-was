package co.fineants.member.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.Nickname;

@Repository
public class MemberJpaRepository implements MemberRepository {

	private final MemberSpringDataJpaRepository repository;

	public MemberJpaRepository(MemberSpringDataJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Member> findMemberByEmailAndProvider(MemberEmail email, String provider) {
		return repository.findMemberByEmailAndProvider(email, provider);
	}

	@Override
	public Optional<Member> findMemberByNicknameAndNotMemberId(Nickname nickname, Long memberId) {
		return repository.findMemberByNicknameAndNotMemberId(nickname, memberId);
	}

	@Override
	public Optional<Member> findMemberByNickname(Nickname nickname) {
		return repository.findMemberByNickname(nickname);
	}

	@Override
	public Optional<Member> findById(Long memberId) {
		return repository.findById(memberId);
	}

	@Override
	public List<Member> findAllById(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public List<Member> findAll() {
		return repository.findAll();
	}

	@Override
	public int modifyMemberPassword(String password, Long id) {
		return repository.modifyMemberPassword(password, id);
	}

	@Override
	public Member save(Member member) {
		return repository.save(member);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public void delete(Member member) {
		repository.delete(member);
	}
}
