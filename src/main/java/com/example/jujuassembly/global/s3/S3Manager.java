package com.example.jujuassembly.global.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Manager {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  private final AmazonS3 s3Client;

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public String uploadMultipartFileWithPublicRead(String prefix, MultipartFile multipartFile)
      throws Exception {
    String fileName = multipartFile.getOriginalFilename();
    String fileNameExtension = StringUtils.getFilenameExtension(fileName);
    fileName = prefix + UUID.randomUUID() + "." + fileNameExtension;//이미지파일명 중복 방지

    String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(multipartFile.getContentType());
    metadata.setContentLength(multipartFile.getSize());

    PutObjectRequest putObjectRequest =
        new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead);
    amazonS3Client.putObject(putObjectRequest);
    return fileUrl;
  }

  // MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
  public String upload(MultipartFile multipartFile, String dirName, Long id) throws IOException {
    File uploadFile = convert(multipartFile)
        .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
    return upload(uploadFile, dirName, id.toString());
  }

  private String upload(File uploadFile, String dirName, String id) {
    String fileName = dirName + "/" + id + "/" + uploadFile.getName();
    String uploadImageUrl = putS3(uploadFile, fileName);

    removeNewFile(uploadFile);  // 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

    return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void deleteImageFile(String fileUrl,String dirName) {
    amazonS3Client.deleteObject(bucket, extractKey(fileUrl,dirName));
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void deleteAllImageFiles(String id, String dirName) {
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
        .withBucketName(bucket)
        .withPrefix(dirName + "/" + id + "/");

    List<S3ObjectSummary> objectSummaries = amazonS3Client.listObjects(listObjectsRequest)
        .getObjectSummaries();

    for (S3ObjectSummary objSummary : objectSummaries) {
      amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, objSummary.getKey()));
    }
  }

  private String extractKey(String fileUrl,String dirName) {
    // "reviews/"를 포함하여 그 이후의 모든 문자열을 추출하는 정규 표현식
    String regex = "("+dirName+"/.*)";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(fileUrl);

    // 정규 표현식에 매칭되는 부분 찾기
    if (matcher.find()) {
      return matcher.group(1);
    }
    throw new IllegalArgumentException("잘못된 파일 Url 입니다.");
  }

  private String putS3(File uploadFile, String fileName) {
    s3Client.putObject(
        new PutObjectRequest(bucket, fileName, uploadFile)
            .withCannedAcl(CannedAccessControlList.PublicRead)    // PublicRead 권한으로 업로드 됨
    );
    return s3Client.getUrl(bucket, fileName).toString();
  }

  private void removeNewFile(File targetFile) {
    if (targetFile.delete()) {
      log.info("파일이 삭제되었습니다.");
    } else {
      log.info("파일이 삭제되지 못했습니다.");
    }
  }


  private Optional<File> convert(MultipartFile file) throws IOException {
    File convertFile = new File(file.getOriginalFilename());
    if (convertFile.createNewFile()) {
      try (FileOutputStream fos = new FileOutputStream(convertFile)) {
        fos.write(file.getBytes());
      }
      return Optional.of(convertFile);
    }
    return Optional.empty();
  }
}
