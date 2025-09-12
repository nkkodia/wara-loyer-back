// src/main/java/com/waraloyer/client/service/ExpenseService.java
package com.waraloyer.client.service;

import com.waraloyer.client.dto.ExpenseDTO;
import com.waraloyer.client.model.Expense;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.ExpenseRepository;
import com.waraloyer.client.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final RentalRepository rentalRepository;
    private final RentalService rentalService;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, RentalRepository rentalRepository, RentalService rentalService) {
        this.expenseRepository = expenseRepository;
        this.rentalRepository = rentalRepository;
        this.rentalService = rentalService;
    }

    public List<ExpenseDTO> findByRentalIdAndDateBetween(Long rentalId, LocalDate startDate, LocalDate endDate, User currentUser) {
        if (!rentalService.belongsToUser(rentalId, currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Cette location n'appartient pas à cet utilisateur.");
        }
        return expenseRepository.findByRentalIdAndDateBetween(rentalId, startDate, endDate)
                .stream()
                .map(expense -> {
                    ExpenseDTO dto = new ExpenseDTO();
                    dto.setId(expense.getId());
                    dto.setAmount(expense.getAmount());
                    dto.setDescription(expense.getDescription());
                    dto.setDate(expense.getDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Expense addExpense(Long rentalId, BigDecimal amount, String description, User currentUser) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Location non trouvée."));

        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Cette location n'appartient pas à cet utilisateur.");
        }

        Expense expense = new Expense();
        expense.setRental(rental);
        expense.setDate(LocalDate.now());
        expense.setAmount(amount);
        expense.setDescription(description);

        return expenseRepository.save(expense);
    }
}