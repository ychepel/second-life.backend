package de.ait.secondlife.repositories;

import de.ait.secondlife.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Category, Long> {
}
