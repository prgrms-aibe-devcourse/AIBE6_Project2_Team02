package com.backend.common.domain.bookmark.repository;

import com.backend.common.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
            SELECT COUNT(b) > 0
            FROM Bookmark b
            WHERE b.member.id = :memberId
              AND b.targetType = :targetType
              AND b.targetId = :targetId
            """)
    boolean existsTarget(
            @Param("memberId") Long memberId,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );

    @Query("""
            SELECT b
            FROM Bookmark b
            WHERE b.member.id = :memberId
              AND b.targetType = :targetType
              AND b.targetId = :targetId
            """)
    Optional<Bookmark> findTarget(
            @Param("memberId") Long memberId,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );
}
