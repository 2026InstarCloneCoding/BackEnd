package com.instagram.backend.domain.message.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
  MessageSendRequest — 메시지 전송 요청 DTO

  설계 배경:
    — 명세서(Chat.pdf) 상 dtype 값에 따라 요청 본문이 달라짐
    — dtype = TEXT  : message_text_content 필드 사용
    — dtype = IMAGE : message_image_url / message_image_type / message_image_name / message_image_uuid 사용
    — dtype = POST  : post_id 사용
    — dtype = STORY : story_visitor_id 사용 (명세서에서 이 이름을 씀 — 실제로는 story_id를 의미)
    — 모든 타입을 하나의 DTO에 통합해서 받는 방식 (flat DTO)
      → 다형 DTO(상속/polymorphic)보다 단순하고 Jackson 역직렬화가 간편

  @NotBlank 같은 Bean Validation 어노테이션을 dtype별로 달 수 없으므로
    서비스 레이어에서 dtype 분기 후 수동 검증한다.
*/
@Getter
@NoArgsConstructor
public class MessageSendRequest {

    // TEXT / IMAGE / POST / STORY 중 하나
    @JsonProperty("dtype")
    private String dtype;

    // --- TEXT 전용 ---
    @JsonProperty("message_text_content")
    private String messageTextContent;

    // --- IMAGE 전용 ---
    @JsonProperty("message_image_url")
    private String messageImageUrl;

    @JsonProperty("message_image_type")
    private String messageImageType;

    @JsonProperty("message_image_name")
    private String messageImageName;

    @JsonProperty("message_image_uuid")
    private String messageImageUuid;

    // --- POST 공유 전용 ---
    @JsonProperty("post_id")
    private Long postId;

    // --- STORY 공유 전용 ---
    // 명세서에서 story_visitor_id라는 이름을 쓰지만 서버 내부에서는 storyId로 취급
    @JsonProperty("story_visitor_id")
    private Long storyVisitorId;
}
