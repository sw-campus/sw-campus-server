package com.swcampus.domain.board;

import com.swcampus.domain.board.exception.BoardCategoryNotFoundException;
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
                .orElseThrow(() -> new BoardCategoryNotFoundException(categoryId));
    }

    public String getCategoryName(Long categoryId) {
        return getCategory(categoryId).getName();
    }

    /**
     * 특정 카테고리와 그 하위 카테고리의 ID 목록을 반환합니다.
     */
    public List<Long> getChildCategoryIds(Long categoryId) {
        List<BoardCategory> allCategories = boardCategoryRepository.findAll();
        // 전체 리스트를 순회하여 parent-child 관계를 구성하고 targetId부터 수집
        return getIdsFromMap(allCategories, categoryId);
    }

    private List<Long> getIdsFromMap(List<BoardCategory> allCategories, Long targetId) {
        // 자식 관계 구성
        Map<Long, List<Long>> parentToChildren = new HashMap<>();
        for (BoardCategory cat : allCategories) {
            if (cat.getPid() != null) {
                parentToChildren.computeIfAbsent(cat.getPid(), k -> new ArrayList<>()).add(cat.getId());
            }
        }
        
        List<Long> result = new ArrayList<>();
        collectIds(targetId, parentToChildren, result);
        return result;
    }

    private void collectIds(Long currentId, Map<Long, List<Long>> parentToChildren, List<Long> result) {
        result.add(currentId);
        List<Long> children = parentToChildren.get(currentId);
        if (children != null) {
            for (Long childId : children) {
                collectIds(childId, parentToChildren, result);
            }
        }
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
