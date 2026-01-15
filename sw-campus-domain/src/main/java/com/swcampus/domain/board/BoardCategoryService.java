package com.swcampus.domain.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCategoryService {

    private final BoardCategoryRepository boardCategoryRepository;

    public List<BoardCategory> getCategoryTree() {
        List<BoardCategory> allCategories = boardCategoryRepository.findAll();
        return buildTree(allCategories);
    }

    public BoardCategory getCategory(Long categoryId) {
        return boardCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
    }

    public String getCategoryName(Long categoryId) {
        return getCategory(categoryId).getName();
    }

    private List<BoardCategory> buildTree(List<BoardCategory> categories) {
        Map<Long, BoardCategory> map = new HashMap<>();
        List<BoardCategory> roots = new ArrayList<>();

        // 1. Map에 모두 담기 (ID -> Object)
        for (BoardCategory category : categories) {
            map.put(category.getId(), category);
        }

        // 2. 부모-자식 연결
        for (BoardCategory category : categories) {
            if (category.getPid() == null) {
                roots.add(category);
            } else {
                BoardCategory parent = map.get(category.getPid());
                if (parent != null) {
                    parent.addChild(category);
                }
            }
        }

        return roots;
    }
}
