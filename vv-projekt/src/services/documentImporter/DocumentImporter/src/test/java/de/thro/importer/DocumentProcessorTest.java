package de.thro.importer;

import de.thro.importer.strategies.IOfferStrategyHandler;
import org.junit.jupiter.api.*;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class DocumentProcessorTest {

    private BlockingQueue<String> testQueue;
    private IOfferStrategyHandler mockHandler;
    private DocumentProcessor processor;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        testQueue = new ArrayBlockingQueue<>(10);
        mockHandler = mock(IOfferStrategyHandler.class);
        processor = new DocumentProcessor(testQueue, mockHandler);
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    @Test
    void testSuccessfulProcessing() throws Exception {
        String testJson = "{\"test\": \"data\"}";

        // Stub handler to simulate success
        when(mockHandler.handle(testJson)).thenReturn(CompletableFuture.completedFuture(null));

        // Run processor in separate thread
        executor.submit(processor::start);

        // Push test data
        testQueue.put(testJson);

        // Wait until mock was invoked
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                verify(mockHandler, times(1)).handle(testJson)
        );
    }

    @Test
    void testExceptionDuringProcessing() throws Exception {
        String testJson = "{\"test\": \"fail\"}";

        // Simulate async failure
        when(mockHandler.handle(testJson)).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("Mock processing error"))
        );

        executor.submit(processor::start);

        testQueue.put(testJson);

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                verify(mockHandler, times(1)).handle(testJson)
        );

        // We can't directly test logger output unless using a log capture framework,
        // but at least we verify that the exception path runs.
    }

    @Test
    void testInterruption() throws Exception {
        executor.submit(() -> {
            processor.start();
        });

        // Give the thread a moment to start up
        TimeUnit.MILLISECONDS.sleep(200);

        // Interrupt processor
        executor.shutdownNow();

        boolean terminated = executor.awaitTermination(2, TimeUnit.SECONDS);
        Assertions.assertTrue(terminated, "Processor thread should shut down on interrupt.");
    }
}
