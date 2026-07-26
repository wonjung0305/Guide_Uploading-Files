package com.example.gsuploadingfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    // 의존성 주입
    @Autowired
    public FileUploadController(StorageService storageService){
        this.storageService = storageService;
    }

    // 업로드 폼 화면 & 저장된 파일 목록 조회
    @GetMapping("/")
    public String listUploadedFiles(Model model){
        // Model에 files라는 이름으로 값을 담음
            // 저장소에 있는 모든 파일 목록을 가져온다 (storageService.loadAll()
                // map: 파일 경로를 하나씩 꺼내어 URL 문자열로 1:1 변환
                    // FileUploadController의 serveFile 메서드 정보를 기반으로 URL 틀 생성
        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(
                        FileUploadController.class, // URL 매핑 정보를 추출할 컨트롤러
                        "serveFile",    // 연결할 메서드 이름
                        path.getFileName().toString())  // 메서드의 파라미터로 전달할 실제 파일명
                        .build()    // 파일명을 URL 경로 안의 변수 자리에 실제로 치환하여 완성시킴
                        .toUri()    // 스프링 전용 객체 -> 자바 표준 네트워크 객체로 변환
                        .toString() // HTML에서 쓸 수 있게 문자열로
                ).collect(Collectors.toList()));    // List 형태로 정리
        return "uploadForm"; // templates/uploadForm.html 파일을 렌더링하도록
    }

    // 파일 다운로드/조회 요청 처리
    @GetMapping("/files/{filename:.+}")
    @ResponseBody   // 템플릿을 안거치고 바로 HTTP 응답 바디에 출력
    public ResponseEntity<Resource> serveFile(@PathVariable String filename){

        // Resource 객체로 읽어오겠다. URL 경로에서 추출한 filename을
        Resource file = storageService.loadAsResource(filename);

        // 파일 존재하지 않거나 못 읽는 경우, 404 Not Found 응답 반환
        if(file==null){
            return ResponseEntity.notFound().build();
        }

        // 정상적으로 존재하는 경우, HTTP 200 OK
            // Content_disposition: 화면에 보여주지말고 파일로 다운로드 해라
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    // 파일 업로드 요청 처리
        // @RequestParam("file")은 HTML의 name 속성값이 file로 지정된 요청 데이터를 자바 변수로 매핑
    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes){

        // 수신한 객체 file을 storageService로 전달하여 저장하겠다
        storageService.store(file);

        // 1회성으로 띄워줄 안내 메시지 추가
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");

        // 메인 화면으로 이동
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc){

        // ResponseEntity: 상태코드, 헤더, 바디를 직접 제어
        // notFound: HTTP 상태 코드를 404 Not Found로 설정하는 빌더 객체를 시작
            // build(): 추가 데이터 안넣고, ResponseEntity 객체를 최종 완성시킴
        return ResponseEntity.notFound().build();
    }

}
