package de.ait.secondlife.repositories;

import de.ait.secondlife.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Category, Long> {
}
