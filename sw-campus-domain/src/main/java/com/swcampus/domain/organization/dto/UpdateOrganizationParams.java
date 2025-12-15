package com.swcampus.domain.organization.dto;

/**
 * 기관 정보 수정 파라미터
 * 
 * @param name 기관명
 * @param description 기관 설명
 * @param homepage 홈페이지 URL
 * @param govAuth 정부 인증 정보
 * @param certificate 사업자등록증 파일 정보
 * @param logo 로고 파일 정보
 * @param facilityImage1 시설 이미지 1
 * @param facilityImage2 시설 이미지 2
 * @param facilityImage3 시설 이미지 3
 * @param facilityImage4 시설 이미지 4
 */
public record UpdateOrganizationParams(
    String name,
    String description,
    String homepage,
    String govAuth,
    FileUploadData certificate,
    FileUploadData logo,
    FileUploadData facilityImage1,
    FileUploadData facilityImage2,
    FileUploadData facilityImage3,
    FileUploadData facilityImage4
) {
    public record FileUploadData(byte[] content, String fileName, String contentType) {}
}
