package com.swcampus.infra.postgres.lecture;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LECTURE_QUALS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureQualEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "QUAL_ID")
	private Long qualId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID")
	private LectureEntity lecture;

	@Column(name = "TYPE", nullable = false)
	private String type;

	@Column(name = "TEXT", nullable = false)
	private String text;

	public com.swcampus.domain.lecture.LectureQual toDomain() {
		return com.swcampus.domain.lecture.LectureQual.builder()
			.qualId(qualId)
			.lectureId(lecture.getLectureId())
			.type(type)
			.text(text)
			.build();
	}
}