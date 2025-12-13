package com.bakery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private Path testUploadDir;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        fileStorageService = new FileStorageService();

        // Create test file
        testUploadDir = Path.of("src/main/resources/static/uploads");
        Files.createDirectories(testUploadDir);

        mockFile = mock(MultipartFile.class);
    }

    @Test
    void testStoreFile_Success() throws IOException {
        String fileName = "test.jpg";
        byte[] content = "test content".getBytes();

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(content));

        String result = fileStorageService.storeFile(mockFile);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
        assertTrue(result.contains(fileName));

        // Cleanup
        if (result != null) {
            String storedFileName = result.substring("/uploads/".length());
            Files.deleteIfExists(testUploadDir.resolve(storedFileName));
        }
    }

    @Test
    void testStoreFile_NullFile() throws IOException {
        String result = fileStorageService.storeFile(null);

        assertNull(result);
    }

    @Test
    void testStoreFile_EmptyFile() throws IOException {
        when(mockFile.isEmpty()).thenReturn(true);

        String result = fileStorageService.storeFile(mockFile);

        assertNull(result);
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        // Create a test file first
        String testFileName = "test-delete.txt";
        Path testFilePath = testUploadDir.resolve(testFileName);
        Files.write(testFilePath, "test content".getBytes());

        String filePath = "/uploads/" + testFileName;

        boolean result = fileStorageService.deleteFile(filePath);

        assertTrue(result);
        assertFalse(Files.exists(testFilePath));
    }

    @Test
    void testDeleteFile_InvalidPath() {
        boolean result = fileStorageService.deleteFile("invalid/path");

        assertFalse(result);
    }

    @Test
    void testDeleteFile_NonExistentFile() {
        boolean result = fileStorageService.deleteFile("/uploads/nonexistent.jpg");

        assertFalse(result);
    }

    @Test
    void testDeleteFile_NullPath() {
        boolean result = fileStorageService.deleteFile(null);

        assertFalse(result);
    }

    @Test
    void testIsImageFile_Jpeg() {
        when(mockFile.getContentType()).thenReturn("image/jpeg");

        boolean result = fileStorageService.isImageFile(mockFile);

        assertTrue(result);
    }

    @Test
    void testIsImageFile_Png() {
        when(mockFile.getContentType()).thenReturn("image/png");

        boolean result = fileStorageService.isImageFile(mockFile);

        assertTrue(result);
    }

    @Test
    void testIsImageFile_Gif() {
        when(mockFile.getContentType()).thenReturn("image/gif");

        boolean result = fileStorageService.isImageFile(mockFile);

        assertTrue(result);
    }

    @Test
    void testIsImageFile_NonImage() {
        when(mockFile.getContentType()).thenReturn("text/plain");

        boolean result = fileStorageService.isImageFile(mockFile);

        assertFalse(result);
    }

    @Test
    void testIsImageFile_NullFile() {
        boolean result = fileStorageService.isImageFile(null);

        assertFalse(result);
    }

    @Test
    void testIsImageFile_NullContentType() {
        when(mockFile.getContentType()).thenReturn(null);

        boolean result = fileStorageService.isImageFile(mockFile);

        assertFalse(result);
    }

    @Test
    void testConstructor_IOException() throws IOException {
        // Create a spy to mock the protected createDirectories method
        FileStorageService spyService = spy(new FileStorageService());

        // We need to trigger the constructor logic again or simulate it.
        // Since we can't easily spy *before* construction to fail the constructor
        // itself efficiently without partial mocking frameworks complexity,
        // we will create a subclass for testing purposes inside the test or use a
        // different approach.
        // Actually, simpler: Refactoring to use a separate 'init()' method called by
        // constructor is cleaner often, but let's try direct spy if possible.
        // Wait, spying on 'new' is hard.
        // Alternative: Subclass

        class TestFileStorageService extends FileStorageService {
            @Override
            protected void createDirectories(Path path) throws IOException {
                throw new IOException("Failed to create dir");
            }
        }

        assertThrows(RuntimeException.class, () -> new TestFileStorageService());
    }

    @Test
    void testDeleteFile_IOException() throws IOException {
        FileStorageService spyService = spy(new FileStorageService());

        String filePath = "/uploads/test.jpg";

        // Stub the protected method
        doThrow(new IOException("Delete failed")).when(spyService).deleteIfExists(any(Path.class));

        boolean result = spyService.deleteFile(filePath);

        assertFalse(result);
        // Verify deleteIfExists was called
        verify(spyService).deleteIfExists(any(Path.class));
    }
}