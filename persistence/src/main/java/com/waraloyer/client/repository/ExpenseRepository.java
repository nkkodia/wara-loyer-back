// src/main/java/com/waraloyer/client/repository/ExpenseRepository.java

package com.waraloyer.client.repository;

import com.waraloyer.client.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByRentalIdAndDateBetween(Long rentalId, LocalDate startDate, LocalDate endDate);
}