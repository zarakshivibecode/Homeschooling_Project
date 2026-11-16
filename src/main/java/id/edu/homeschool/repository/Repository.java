package id.edu.homeschool.repository;

import java.util.List;
import java.util.Optional;

/** Persistence abstraction shared by JSON repositories. */
public interface Repository<T> {
    List<T> findAll();
    Optional<T> findById(String id);
    T save(T entity);
    boolean deleteById(String id);
}
