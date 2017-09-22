package org.assemblits.eru.persistence;

import org.assemblits.eru.entities.Tag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends CrudRepository<Tag, Integer> {

    List<Tag> findAllByOrderByNameAsc();
}