package ip.project.backend.backend.controller;

import ip.project.backend.backend.modeldto.StockDto;
import ip.project.backend.backend.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    private StockDto stockDto;

    @BeforeEach
    void setUp() {

        stockDto = mock(StockDto.class);
    }

    @Test
    void testGetStock_found() {
        when(stockService.getStockByProductId("123")).thenReturn(Optional.of(stockDto));

        ResponseEntity<StockDto> response = stockController.getStock("123");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(stockDto, response.getBody());
    }

    @Test
    void testGetStock_notFound() {
        when(stockService.getStockByProductId("123")).thenReturn(Optional.empty());

        ResponseEntity<StockDto> response = stockController.getStock("123");

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testGetAllStock_found() {
        when(stockService.getAllStock()).thenReturn(List.of(stockDto));

        ResponseEntity<List<StockDto>> response = stockController.getAllStock();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllStock_empty() {
        when(stockService.getAllStock()).thenReturn(List.of());

        ResponseEntity<List<StockDto>> response = stockController.getAllStock();

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void testAddStock_success() {
        when(stockService.addStock(any())).thenReturn(Optional.empty());

        ResponseEntity<String> response = stockController.addStock(stockDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Stock added successfully", response.getBody());
    }

    @Test
    void testAddStock_failure() {
        when(stockService.addStock(any())).thenReturn(Optional.of("Failed to add"));

        ResponseEntity<String> response = stockController.addStock(stockDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Failed to add", response.getBody());
    }

    @Test
    void testUpdateStock_success() {
        when(stockService.updateStock(any())).thenReturn(Optional.empty());

        ResponseEntity<String> response = stockController.updateStock(stockDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Stock updated successfully", response.getBody());
    }

    @Test
    void testUpdateStock_failure() {
        when(stockService.updateStock(any())).thenReturn(Optional.of("Update error"));

        ResponseEntity<String> response = stockController.updateStock(stockDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Update error", response.getBody());
    }

    @Test
    void testDeleteStock_success() {
        when(stockService.deleteStock("123")).thenReturn(Optional.empty());

        ResponseEntity<String> response = stockController.deleteStock("123");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Stock deleted successfully", response.getBody());
    }

    @Test
    void testDeleteStock_failure() {
        when(stockService.deleteStock("123")).thenReturn(Optional.of("Delete failed"));

        ResponseEntity<String> response = stockController.deleteStock("123");

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Delete failed", response.getBody());
    }
}
