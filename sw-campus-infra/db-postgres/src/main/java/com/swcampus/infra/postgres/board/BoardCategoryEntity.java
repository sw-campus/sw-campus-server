package com.swcampus.infra.postgres.board;

import com.swcampus.domain.board.BoardCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_categories", schema = "swcampus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "board_categories_seq")
    @SequenceGenerator(name = "board_categories_seq", sequenceName = "swcampus.board_categories_board_category_id_seq", allocationSize = 1)
    @Column(name = "board_category_id")
    private Long id;

    @Column(name = "board_category_name", nullable = false)
    private String name;

    @Column(name = "board_pid")
    private Long pid;

    public BoardCategory toDomain() {
        return BoardCategory.builder()
                .id(this.id)
                .name(this.name)
                .pid(this.pid)
                .build();
    }

    public static BoardCategoryEntity from(BoardCategory boardCategory) {
        return BoardCategoryEntity.builder()
                .id(boardCategory.getId())
                .name(boardCategory.getName())
                .pid(boardCategory.getPid())
                .build();
    }
}
