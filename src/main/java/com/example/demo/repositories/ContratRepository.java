package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.example.demo.models.Contrat;
import com.example.demo.models.User;

public interface ContratRepository extends JpaRepository<Contrat, Integer>{
	
	@Query("SELECT c FROM Contrat c WHERE c.user = :user ORDER BY c.dateDebut ASC")
	List<Contrat> getOldestContratByUser(@Param("user") User user, Pageable pageable);

}
