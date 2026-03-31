package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.dto.SearchResponseDto;
import com.meta.safepill_be.medicine.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponseDto> search(@RequestParam String keyword) {
        SearchResponseDto result = searchService.searchMedicineAndSupplement(keyword);
        return ResponseEntity.ok(result);
    }
}
