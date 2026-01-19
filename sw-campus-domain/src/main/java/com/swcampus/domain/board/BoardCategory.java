package com.swcampus.domain.board;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BoardCategory {
    private Long id;
    private String name;
    private Long pid;

    @Builder.Default
    private List<BoardCategory> children = new ArrayList<>();

    public void addChild(BoardCategory child) {
        this.children.add(child);
    }
}
