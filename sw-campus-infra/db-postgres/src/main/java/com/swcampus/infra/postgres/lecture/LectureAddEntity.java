package com.swcampus.infra.postgres.lecture;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LECTURE_ADDS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LectureAddEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ADD_ID")
	private Long addId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID")
	private LectureEntity lecture;

	@Column(name = "ADD_NAME", nullable = false)
	private String addName;

	public com.swcampus.domain.lecture.LectureAdd toDomain() {
		return com.swcampus.domain.lecture.LectureAdd.builder()
			.addId(addId)
			.lectureId(lecture.getLectureId())
			.addName(addName)
			.build();
	}
}