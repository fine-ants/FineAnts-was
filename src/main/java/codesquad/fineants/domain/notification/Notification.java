package codesquad.fineants.domain.notification;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String content;
	private Boolean isRead;
	private String type;
	private String referenceId;
	private Boolean isDeleted;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	public Notification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, String title,
		String content, Boolean isRead, String type, String referenceId, Boolean isDeleted, Member member) {
		super(createAt, modifiedAt);
		this.id = id;
		this.title = title;
		this.content = content;
		this.isRead = isRead;
		this.type = type;
		this.referenceId = referenceId;
		this.isDeleted = isDeleted;
		this.member = member;
	}

	// 알림을 읽음 처리
	public void readNotification() {
		this.isRead = true;
	}

	public void deleteNotification() {
		this.isDeleted = true;
	}
}
