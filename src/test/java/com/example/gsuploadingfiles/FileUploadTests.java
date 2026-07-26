package com.example.gsuploadingfiles;

import com.example.gsuploadingfiles.storage.StorageFileNotFoundException;
import com.example.gsuploadingfiles.storage.StorageService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StorageService storageService;

    // 메인 화면 파일 목록 조회
    @Test
    public void shouldListAllFiles() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mvc.perform(get("/"))
                // HTTP 200 OK 응답이 나오는지 검증
                .andExpect(status().isOk())
                // Model에 담긴 "files" 속성의 리스트 값에 가짜 URL 2개가 순서대로 포함되어 있는지 Matchers로 검증
                .andExpect(model().attribute("files",
                        Matchers.contains("http://localhost/files/first.txt",
                                "http://localhost/files/second.txt")));
    }

    // 파일 업로드 요청
    @Test
    public void shouldSaveUploadedFile() throws Exception {
        // 가짜 파일 객체 생성
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());

        // multipart("/") : multipart/form-data POST 요청을 보냄
        this.mvc.perform(multipart("/").file(multipartFile))
                // HTTP 302 Found (리다이렉트) 응답 검증
                .andExpect(status().isFound())
                // Response Header의 "Location" 값이 "/" (메인 화면)인지 검증
                .andExpect(header().string("Location", "/"));

        // storageService.store(multipartFile) 메서드가 실제로 호출되었는지 검증
        then(this.storageService).should().store(multipartFile);
    }

    // 존재하지 않는 파일 요청 테스트
    @SuppressWarnings("unchecked")
    @Test
    public void should404WhenMissingFile() throws Exception {
        // loadAsResource("test.txt") 호출 시 StorageFileNotFoundException 예외를 던지도록 시뮬레이션
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(StorageFileNotFoundException.class);

        // HTTP 404 Not Found 응답이 오는지 검증
        this.mvc.perform(get("/files/test.txt"))
                .andExpect(status().isNotFound());
    }


}
