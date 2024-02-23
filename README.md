# 주酒총회
<br>

## 📌 목차
- [프로젝트 소개](#프로젝트소개)
- [팀원 소개](#팀원소개)
- [Ground Rules](#GroundRules)
- [목표](#Goals)
- [기술 스택](#ProjectRules)
- [우리들의 약속](#우리들의약속)
- [ERD](#ERD)
- [와이어프레임](#와이어프레임)
- [더보기](#더보기)


<p><br></p>

## ✅ 프로젝트 소개
**한 줄 정리**: 다양한 종류의 술들을 검색하고, 평점과 리뷰를 통해 원하는 상품을 찾을 수 있도록 도와주는 웹 서비스

- 내용 :
    - 주류 카테고리로 나누고 그 안에서 평점순/ 찜많은 순 /리뷰 많은 순/ (신제품순 으로 비교할 수 있고
    - 편의점 별 상품 필터
    - 찜 기능과 **인증**을 통해 마셔본 술을 등록할 수도 있다. ⇒ **배찌 기능/사용자 간 랭킹**
    - 주류를 클릭하면 주류에 대한 설명(성분정보) 과 (판매처/지역) 등이 있고, 어울리는 안주 추천, 평점과 리뷰를 등록할 수 있다.
    - 상품은 관리자가 등록하고, 사용자는 제보할 수 있다.
    - 추천 기능(판매량 정보 가져와서)
    - 무한 스크롤 페이지 네이션(커서 기반)

![사진](https://www.notion.so/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F85d47b3b-e1d5-462a-8ae4-51caf0028c4e%2F%25E1%2584%258C%25E1%2585%25AE%25E1%2584%258C%25E1%2585%25AE%25E1%2584%258E%25E1%2585%25A9%25E1%2586%25BC%25E1%2584%2592%25E1%2585%25AC_%25E1%2584%2589%25E1%2585%25A9%25E1%2584%2580%25E1%2585%25A2%25E1%2584%2591%25E1%2585%25A6%25E1%2584%258B%25E1%2585%25B5%25E1%2584%258C%25E1%2585%25B5.png?table=block&id=cb466992-ee26-4b0a-b5c1-af941d0926fd&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=2000&userId=ca0c5ffb-e774-49d1-8001-e9462600093c&cache=v2)

<p><br></p>

## 👥 팀원 소개

| |이민주|김재현|오수식|박연우|정영도|
|-- |-----|----|----|----|-----|
|블로그 |[블로그](https://velog.io/@leemj4090/posts)| [블로그](https://velog.io/@jeiho/posts)|[블로그](https://velog.io/@tntlr92)|[블로그](https://studymode.tistory.com/)|[블로그](https://velog.io/@yeong_do/posts)|
|깃퍼브 |[GitHub](https://github.com/leeminju?tab=repositories) |[GitHub](https://github.com/k-jaehyun) |[GitHub](https://github.com/susik2023) |[GitHub](https://github.com/yeonwoopark20231003)|[GitHub](https://github.com/yeongdo99) |
|담당기능 |- CI/CD 구축<br>- 도메인, HTTPS 적용<br>- 리뷰 기능<br>- Front 주도       |- Spring Security<br>- 소셜로그인<br>- 이메일 인증 | - 웹소켓 활용한 <br>채팅 기능 <br>- 주류 CRUD|- 주류 찜 기능<br>- 카트<br>- 프로필 기능<br> - 리뷰/제보 이미지 수정 |- SSE 알림 기능<br>- 주류 CRUD |

<p><br></p>

## 🏝️ Ground Rules
1. 기술면접 9시-10시 사이에 2문제씩 하고 면접 진행시 카메라 키도록 한다!
2. 자리를 비울 때나 일정이 있을 경우 팀 슬랙에 공유한다. 
(연락이 안되면 카톡으로 호출하겠습니다!!)
3. 학습을 하며 막히는게 있다면 팀원과 튜터님께 공유하며 해결한다.
4. 대화를 할 때는 캠도 켜고 화면공유도 잘 한다. 
5. 파이팅 넘치겠습니다!
6. TIL 꼭 쓰기 - 다음날 오전 검사 (벌칙: 캠 & 화면공유-> TIL 쓰기)
7. 지각은 미리 슬랙에 공유 -> 공유 없이 지각하면 패널티(1시간에 천원, 30분까진 허용)
8. 아프지 않게 컨디션 관리 잘하기

<p><br></p>

## 🚩 Goals
1. CI / CD 도입
2. JPA/AWS 강의 + 특강 1.10일까지 완강 (20시, 강의 수강 계획 및 이행 현황 공유)
3. 동시성, 트래픽 문제 해결
4. 도커 사용해 보기
5. 코드 리뷰 - PR에 댓글  
--> 각 사람에 대해 맡은 사람이 리뷰해주기
[이민주 -> 오수식 -> 김재현 -> 정영도 -> 박연우 -> 이민주]
6. 프론트 - 타임리프 적용시키기
   
<p><br></p>

## 🚦 Project Rules

### 백엔드
- GVS : Github
- IDE : IntelliJ
- **SDK : JAVA 17**
- Spring Boot 3.2.1
    - Spring Web
    - Spring Security
    - Validation
    - thymeleaf
- DB
    - Spring Data JPA
    - MySQL
    - H2
    - Redis
    - AWS RDS
- Imagae Stroage
    - AWS S3
- 배포 환경
    - ec2, S3, GithubAction , code Deploy
 
### 프론트엔드

- HTML/CSS
    - Bootstrap5
- JS
    - JQuery

<p><br></p>

## 🤝 우리들의 약속

### Code Convention
1. 구글 코드 포매터 적용
[[intellij] google code 포매터 적용](https://withhamit.tistory.com/411)
2. 구글 자바 스타일 가이드
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

### Github Rules
**깃허브 규칙**
[우린 Git-flow를 사용하고 있어요 | 우아한형제들 기술블로그](https://techblog.woowahan.com/2553/)
1. **PR 전 코드리뷰 필수!!**<br>
**(2명이상 승인 해야 merge 가능하게 지정)**<br>

2. **git branch 전략**<br>
main  : 제품으로 출시될 수 있는 브랜치<br>
dev : 다음 출시 버전을 개발하는 브랜치<br>
feature : 기능을 개발하는 브랜치<br>
release : 이번 출시 버전을 준비하는 브랜치<br>
hotfix : 출시 버전에서 발생한 버그를 수정 하는 브랜치<br>
**feature로 각자 작업하면서 dev에 합친 후 중간 출시 때 release로 복사**
**hotfix로 유지보수하면서 최종 출시 때 main으로 합치기!**<br>

3. **git commit message 작성**<br>
(타입 : 내용 으로 통일)

| 작업 타입 | 작업내용 |
| --- | --- |
| ✨ update   | 해당 파일에 새로운 기능이 생김 |
| feat | 기능 구현 |
| 🎉 add | 없던 파일을 생성함, 초기 세팅 |
| 🐛 bugfix | 버그 수정 |
| ♻️ refactor | 코드 리팩토링 |
| 🩹 fix | 코드 수정 |
| 🚚 move | 파일 옮김/정리 |
| 🔥 del | 기능/파일을 삭제 |
| 🍻 test | 테스트 코드를 작성 |
| 💄 style | css |
| 🙈 gitfix | gitignore 수정 |
| 🔨script | package.json 변경(npm 설치 등)git issue 활용 |

**작업의 버그 수정, 질문,새로운 추가될 기능, 개선해야하는 기능이  있을 때 적극 활용한다.**

 4. Git Issuse
**작업의 버그 수정, 새로운 추가될 기능, 개선해야하는 기능, 질문 등이 있을 때 git issue적극 활용**
    
<p><br></p>

## ERD
![ERD](https://www.notion.so/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F02f4f7b9-a86e-4bbe-8a8a-1467fcac4587%2F%25EC%25A3%25BC%25EC%25A3%25BC%25EC%25B4%259D%25ED%259A%258C_-_%25EC%25B5%259C%25EC%25A2%2585_%25EB%25B0%259C%25ED%2591%259C_ERD.png?table=block&id=70a5c3c8-7a79-48ab-ad9f-87a6131a7c52&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=2000&userId=ca0c5ffb-e774-49d1-8001-e9462600093c&cache=v2)

## 와이어프레임
![figma](https://www.notion.so/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F8a76fcce-a33f-41e0-a140-2158c540fa19%2F%25EC%25A3%25BC%25EC%25A3%25BC%25EC%25B4%259D%25ED%259A%258C_%25EC%2599%2580%25EC%259D%25B4%25EC%2596%25B4_%25ED%2594%2584%25EB%25A0%2588%25EC%259E%2584.png?table=block&id=f1771ec4-dfe3-4364-b522-afd3b40a7806&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=2000&userId=ca0c5ffb-e774-49d1-8001-e9462600093c&cache=v2)
<p><br></p>

## 더보기
+ **서비스 사이트:** https://jujuassembly.store/
+ **Notion Page:** https://www.notion.so/teamsparta/5216eb8a22a5478990fdfe34b34988dd
