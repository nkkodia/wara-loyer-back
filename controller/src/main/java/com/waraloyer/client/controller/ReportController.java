package com.waraloyer.client.controller;

import com.waraloyer.client.dto.TenantMessageDTO;
import com.waraloyer.client.model.TenantMessageLog;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.TenantMessageLogRepository;
import com.waraloyer.client.service.TenantMessageService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
@RestController
@RequestMapping("/api/reporting")
public class ReportController {

    private final TenantMessageLogRepository tenantMessageRepository;
    private final RentalRepository rentalRepository;

    @Autowired
    public ReportController(TenantMessageLogRepository tenantMessageRepository, RentalRepository rentalRepository) {
        this.tenantMessageRepository = tenantMessageRepository;
        this.rentalRepository = rentalRepository;
    }

    @PostMapping("/report-problem/{rentalId}")
    public ResponseEntity<?> reportProblem(@PathVariable Long rentalId, @RequestBody TenantMessageDTO messageDto) {
        return rentalRepository.findById(rentalId)
                .map(rental -> {
                    TenantMessageLog message = new TenantMessageLog();
                    message.setRental(rental);
                    message.setStatus(messageDto.getStatus());
                    message.setMessage(messageDto.getMessageContent());
                    message.setSentDate(Instant.from(LocalDateTime.now().atZone(ZoneId.systemDefault())));
                    tenantMessageRepository.save(message);
                    return new ResponseEntity<>("Message enregistré", HttpStatus.CREATED);
                })
                .orElse(new ResponseEntity<>("Location non trouvée", HttpStatus.NOT_FOUND));
    }
}
