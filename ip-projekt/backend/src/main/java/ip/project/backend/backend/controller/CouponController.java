package ip.project.backend.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ip.project.backend.backend.modeldto.CouponDto;
import ip.project.backend.backend.modeldto.NewCouponDto;
import ip.project.backend.backend.service.CouponService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    private final CouponService couponService;

    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Operation(summary = "Create a new coupon")
    @ApiResponse(responseCode = "200", description = "Coupon created successfully")
    @ApiResponse(responseCode = "400", description = "Failed to create coupon. Please check the input data.")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/add")
    public ResponseEntity<String> createCoupon(@RequestBody @NotNull @Valid NewCouponDto newCouponDto) {
        if (this.couponService.createCoupon(newCouponDto)) {
            return ResponseEntity.ok("Coupon created successfully");
        }
        return ResponseEntity.badRequest().body("Failed to create coupon. Please check the input data.");
    }

    @Operation(summary = "Get all coupons")
    @ApiResponse(responseCode = "200", description = "Coupons retrieved successfully")
    @ApiResponse(responseCode = "204", description = "No coupons found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/all")
    public ResponseEntity<List<CouponDto>> getAllCoupons() {

        List<CouponDto> allCoupons = this.couponService.getAllCoupons();

        if (!allCoupons.isEmpty()) {
            return ResponseEntity.ok(allCoupons); // Return the first coupon for simplicity
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a coupon by name")
    @ApiResponse(responseCode = "200", description = "Coupon retrieved successfully")
    @ApiResponse(responseCode = "204", description = "No coupon found with the given name")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{name}")
    public ResponseEntity<CouponDto> getCouponByName(@PathVariable String name) {
        Optional<CouponDto> couponDto = this.couponService.getCouponByName(name);
        if (couponDto.isPresent()) {
            return ResponseEntity.ok(couponDto.get());
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a coupon by name")
    @ApiResponse(responseCode = "200", description = "Coupon updated successfully")
    @ApiResponse(responseCode = "400", description = "Failed to update coupon. Please check the input data.")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{name}/delete")
    public ResponseEntity<String> deleteCoupon(@PathVariable String name) {
        if (this.couponService.deleteCoupon(name)) {
            return ResponseEntity.ok("Coupon deleted successfully");
        }
        return ResponseEntity.badRequest().body("Failed to delete coupon. Please check the input data.");
    }




}