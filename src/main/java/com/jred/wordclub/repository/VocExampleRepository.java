package com.jred.wordclub.repository;

import com.jred.wordclub.entity.VocExample;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VocExampleRepository extends JpaRepository<VocExample, Long> {

    List<VocExample> findByWordId(Long wordId);
}
