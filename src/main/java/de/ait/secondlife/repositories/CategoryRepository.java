package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
    boolean existsByIdAndActiveTrue(Long id);
}
