package com.backend.common.global.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL
            ="https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent";

    private String call(String prompt){
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY가 설정되어 있지 않습니다.");
        }

        RestClient client = RestClient.create();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts",List.of(Map.of("text",prompt)))
                )
        );

        Map response = client.post()
                .uri(API_URL+"?key="+apiKey)
                .header("Content-Type","application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        List candidates = (List) response.get("candidates");
        Map candidate = (Map) candidates.get(0);
        Map content = (Map) candidate.get("content");
        List parts = (List) content.get("parts");
        Map part = (Map) parts.get(0);
        return (String) part.get("text");
    }

    public String generateProjectDescription(String title){
        String prompt = """
                당신은 개발 프로젝트 기획을 도와주는 어시스턴트입니다.
                            프로젝트 제목: "%s"
                            위 제목을 바탕으로 프로젝트 설명을 1~3문장으로 작성해주세요.
                            배경, 해결하려는 문제, 주요 기능을 포함해서 한국어로 작성해주세요.
                            설명 텍스트만 반환하고 다른 내용은 포함하지 마세요.
                """.formatted(title);

        return call(prompt);
    }

    public String generatePortfolioIntroduction(String title, String position, List<String> techStacks){
        String prompt = """
                당신은 개발자 포트폴리오 작성을 도와주는 어시스턴트입니다.
                            포트폴리오 제목: "%s"
                            희망 포지션: "%s"
                            보유 기술스택: %s
                            위 정보를 바탕으로 포트폴리오 소개글을 1~3문장으로 작성해주세요.
                            경험, 관심사, 목표를 자연스럽게 담아 한국어로 작성해주세요.
                            소개글 텍스트만 반환하고 다른 내용은 포함하지 마세요.
                """.formatted(title,position,String.join(", ",techStacks));

        return call(prompt);
    }

    public String generateApplicationMotivation(
            String projectTitle,
            String projectDescription,
            String position,
            List<String> myTechStacks
    ){
        String prompt = """
                당신은 프로젝트 지원서 작성을 도와주는 어시스턴트입니다.
                            프로젝트 제목: "%s"
                            프로젝트 설명: "%s"
                            지원 포지션: "%s"
                            보유 기술스택: %s
                            위 정보를 바탕으로 지원 동기를 1~3문장으로 작성해주세요.
                            왜 이 프로젝트에 적합한지 구체적으로 한국어로 작성해주세요.
                            지원 동기 텍스트만 반환하고 다른 내용은 포함하지 마세요.
                """.formatted(projectTitle, projectDescription, position, String.join(", ",myTechStacks));
        return call(prompt);
    }
}
