package org.example.md04_ss12.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.md04_ss12.order.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final PurchaseService purchaseService;

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    /** Bai 1: trang chi tiet san pham - doc qua Redis cache. */
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/{id}/stock")
    public Map<String, Object> getStock(@PathVariable Long id) {
        return Map.of("productId", id, "stock", productService.getStock(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /** Bai 1: quan tri vien cap nhat -> @CacheEvict. */
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    /** Bai 3: mua hang co khoa phan tan. */
    @PostMapping("/{id}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public Order purchase(@PathVariable Long id, @Valid @RequestBody PurchaseRequest request) {
        return purchaseService.purchase(id, request);
    }

    /** Bai 3: mo phong N nguoi cung mua 1 luc. */
    @PostMapping("/{id}/purchase/simulate")
    public PurchaseService.SimulationResult simulate(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "100") int buyers)
            throws InterruptedException {
        return purchaseService.simulate(id, buyers);
    }
}
