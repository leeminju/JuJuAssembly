package com.example.jujuassembly.domain.report.service;

import static com.example.jujuassembly.domain.report.entity.StatusEnum.ADOPTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.domain.report.dto.ReportResponseDto;
import com.example.jujuassembly.domain.report.dto.ReportStatusRequestDto;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @InjectMocks
  ReportService reportService;
  @Mock
  ReportRepository reportRepository;
  @Mock
  S3Manager s3Manager;
  @Mock
  CategoryRepository categoryRepository;

  @Test
  @DisplayName("제보 상품 생성 테스트")
  void postReportTest() throws IOException {
    //given
    ReportRequestDto requestDto = ReportRequestDto.builder().name("제보상품이름").build();

    Category category = new Category(CategoryRequestDto.builder().name("카테고리1").build());
    ReflectionTestUtils.setField(category, Category.class,"id",1L, Long.class);

    User user = new User("loginId","nickname","email","password",category,category);
    ReflectionTestUtils.setField(user, User.class,"id",1L, Long.class);

    Report report = new Report(requestDto);
    ReflectionTestUtils.setField(report, Report.class,"id",1L, Long.class);

    report.updateUser(user);
    report.updateCategory(category);
    MultipartFile image = mock(MultipartFile.class);

    given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
//    given(reportRepository.findById(report.getId())).willReturn(Optional.of(report));
    given(image.getContentType()).willReturn("image/png");


    //when

    ReportResponseDto resultReport = reportService.postReport(1L, image, requestDto, user);

    //then

    ReportResponseDto expectReport = new ReportResponseDto(report);

    //user비교
    assertEquals(expectReport.getUser().getEmail(),resultReport.getUser().getEmail());
    assertEquals(expectReport.getUser().getLoginId(),resultReport.getUser().getLoginId());
    assertEquals(expectReport.getUser().getNickname(),resultReport.getUser().getNickname());
    assertEquals(expectReport.getUser().getLoginId(),resultReport.getUser().getLoginId());

    //카테고리비교
    assertEquals(expectReport.getCategory().getId(),resultReport.getCategory().getId());
    assertEquals(expectReport.getCategory().getName(),resultReport.getCategory().getName());

    //report 엔티티 비교
    assertEquals(expectReport.getName(),resultReport.getName());
    assertEquals(expectReport.getImage(),resultReport.getImage());
    assertEquals(expectReport.getStatus(),resultReport.getStatus());
  }

  @Test
  @DisplayName("제보 상품 조회 테스트")
  void getReportsTest() {
    // Mock 데이터 생성
    Long userId = 1L;
    CategoryRequestDto requestDto = CategoryRequestDto.builder().name("카테고리").build();
    Category category = new Category(requestDto);
    category.builder().image("imageurl").id(1L).build();
    User user = User.builder().id(1L).nickname("이름").email("이메일").build();
    Report report1 =  Report.builder().id(1L).name("제보이름").user(user).category(category).build();
    Report report2 =  Report.builder().id(2L).name("제보이름").user(user).category(category).build();


    // reportRepository.findAllByUserId() 메서드가 Mock 데이터 리스트를 반환하도록 설정
    when(reportRepository.findAllByUserId(userId)).thenReturn(Arrays.asList(report1, report2));
    when(reportRepository.findByUserId(userId)).thenReturn(Optional.of(report1));
    when(reportRepository.findByUserId(userId)).thenReturn(Optional.of(report2));

    // 테스트할 메서드 호출
    List<ReportResponseDto> result = reportService.getReports(userId, user);

    // 예상 결과와 실제 결과 비교
    assertEquals(2, result.size()); // 예상되는 결과 크기 확인
    assertEquals(report1.getId(), result.get(0).getId()); // 예상되는 결과 id
    assertEquals(report1.getName(), result.get(0).getName()); // 예상되는 결과 name
    assertEquals(report1.getUser().getNickname(), result.get(0).getUser().getNickname()); // 예상되는 결과 user

    assertEquals(report2.getId(), result.get(1).getId()); // 예상되는 결과 id
    assertEquals(report2.getName(), result.get(1).getName()); // 예상되는 결과 name
    assertEquals(report2.getUser().getNickname(), result.get(1).getUser().getNickname()); // 예상되는 결과 user


    // reportRepository.findAllByUserId() 메서드가 1번 호출되었는지 검증
    verify(reportRepository, times(1)).findAllByUserId(userId);
  }

  @Test
  @DisplayName("제보 상품 수정 테스트")
  void patchReportTest() throws IOException {
//given
    //기존에 존재할 제보상품 만들기.
    //카테고리초기화
    ReportRequestDto requestDto = ReportRequestDto.builder().name("제보상품이름").build();
    Category category = new Category(CategoryRequestDto.builder().name("카테고리1").build());
    ReflectionTestUtils.setField(category, Category.class,"id",1L, Long.class);
    //유저초기화
    User user = new User("loginId","nickname","email","password",category,category);
    ReflectionTestUtils.setField(user, User.class,"id",1L, Long.class);
    //리포트 초기화
    Report report = new Report(requestDto);
    ReflectionTestUtils.setField(report, Report.class,"id",1L, Long.class);
    //리포트에 user,category추가
    report.updateUser(user);
    report.updateCategory(category);
    report.builder().image("https://test.com/image.jpg").build();
    //이미지 파일 생성

    given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
    given(reportRepository.findById(report.getId())).willReturn(Optional.of(report));

    //수정할 report 생성
    //리포트 초기화
    ReportRequestDto patchrequestdto  = ReportRequestDto.builder().name("수정할 상품이름").build();
    Report updatereport = new Report(patchrequestdto);
    ReflectionTestUtils.setField(updatereport, Report.class,"id",1L, Long.class);

    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    when(image.getContentType()).thenReturn("image/jpeg");
    String imageUrl = "https://modified.com/image.jpg";
    when(s3Manager.upload(eq(image), eq("reports"), eq(updatereport.getId()))).thenReturn(imageUrl);

    //when

    ReportResponseDto resultReport = reportService.patchReport(category.getId(),updatereport.getId(), image, patchrequestdto, user);

    //then

    ReportResponseDto expectReport = new ReportResponseDto(report);

    //user비교
    assertEquals(expectReport.getUser().getEmail(),resultReport.getUser().getEmail());
    assertEquals(expectReport.getUser().getLoginId(),resultReport.getUser().getLoginId());
    assertEquals(expectReport.getUser().getNickname(),resultReport.getUser().getNickname());
    assertEquals(expectReport.getUser().getLoginId(),resultReport.getUser().getLoginId());

    //카테고리비교
    assertEquals(expectReport.getCategory().getId(),resultReport.getCategory().getId());
    assertEquals(expectReport.getCategory().getName(),resultReport.getCategory().getName());

    //report 엔티티 비교
    assertEquals(expectReport.getId(),resultReport.getId());
    assertEquals(expectReport.getName(),resultReport.getName());
    assertEquals(expectReport.getImage(),resultReport.getImage());
    assertEquals(expectReport.getStatus(),resultReport.getStatus());
  }

  @Test
  @DisplayName("제보 상품 상태 변경 테스트")
  void patchReportStatusTest() throws IOException {
//given
    //기존에 존재할 제보상품 만들기.
    //카테고리초기화
    ReportRequestDto requestDto = ReportRequestDto.builder().name("제보상품이름").build();
    Category category = new Category(CategoryRequestDto.builder().name("카테고리1").build());
    ReflectionTestUtils.setField(category, Category.class,"id",1L, Long.class);
    //유저초기화
    User user = new User("loginId","nickname","email","password",category,category);
    ReflectionTestUtils.setField(user, User.class,"id",1L, Long.class);
    //리포트 초기화
    Report report = new Report(requestDto);
    ReflectionTestUtils.setField(report, Report.class,"id",1L, Long.class);
    //리포트에 user,category추가
    report.updateUser(user);
    report.updateCategory(category);
    report.builder().image("https://test.com/image.jpg").build();

    given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
    given(reportRepository.findById(report.getId())).willReturn(Optional.of(report));

    //수정할 report 생성
    //리포트 초기화
    ReportStatusRequestDto reportStatusRequestDto  = ReportStatusRequestDto.builder().status(ADOPTED).build();


    //when

    ReportResponseDto resultReport = reportService.patchReportStatus(category.getId(),report.getId(), reportStatusRequestDto, user);

    //then

    ReportResponseDto expectReport = new ReportResponseDto(report);

    //user비교
    assertEquals(expectReport.getUser().getEmail(),resultReport.getUser().getEmail());
    assertEquals(expectReport.getUser().getLoginId(),resultReport.getUser().getLoginId());
    assertEquals(expectReport.getUser().getNickname(),resultReport.getUser().getNickname());
    assertEquals(expectReport.getUser().getLoginId(),resultReport.getUser().getLoginId());

    //카테고리비교
    assertEquals(expectReport.getCategory().getId(),resultReport.getCategory().getId());
    assertEquals(expectReport.getCategory().getName(),resultReport.getCategory().getName());

    //report 엔티티 비교
    assertEquals(expectReport.getId(),resultReport.getId());
    assertEquals(expectReport.getName(),resultReport.getName());
    assertEquals(expectReport.getImage(),resultReport.getImage());
    assertEquals(expectReport.getStatus(),resultReport.getStatus());
  }

  @Test
  @DisplayName("제보 상품 삭제 테스트")
  void deleteReportTest() {
    // Given

    //카테고리초기화
    ReportRequestDto requestDto = ReportRequestDto.builder().name("제보상품이름").build();
    Category category = new Category(CategoryRequestDto.builder().name("카테고리1").build());
    ReflectionTestUtils.setField(category, Category.class,"id",1L, Long.class);
    //유저초기화
    User user = new User("loginId","nickname","email","password",category,category);
    ReflectionTestUtils.setField(user, User.class,"id",1L, Long.class);
    //리포트 초기화
    Report report = new Report(requestDto);
    ReflectionTestUtils.setField(report, Report.class,"id",1L, Long.class);

    CategoryRequestDto categoryRequestDto =CategoryRequestDto.builder().name("ExistingCategory").build();
    Category existingCategory = new Category(categoryRequestDto);
    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(existingCategory));
    when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));

    // When
    reportService.deleteReport(category.getId(),report.getId(),user);

    // Then
    // CategoryRepository.findById() 메소드가 호출되었는지 확인
    verify(categoryRepository, times(1)).findById(category.getId());

    // CategoryRepository.delete() 메소드가 호출되었는지 확인
    verify(reportRepository, times(1)).delete(report);

    // S3Manager.deleteAllImageFiles() 메소드가 호출되었는지 확인
    verify(s3Manager, times(1)).deleteAllImageFiles(report.getId().toString(), "reports");

  }
}