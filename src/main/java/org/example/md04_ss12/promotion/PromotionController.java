package org.example.md04_ss12.promotion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public List<Promotion> getAll() {
        return promotionService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion create(@Valid @RequestBody PromotionRequest request) {
        return promotionService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        promotionService.deactivate(id);
    }
}
