package com.swcampus.infra.postgres.lecture;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LECTURE_ADDS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lecture")
public class LectureAddEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lecture_adds_seq")
	@SequenceGenerator(name = "lecture_adds_seq", sequenceName = "lecture_adds_add_id_seq", allocationSize = 1)
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