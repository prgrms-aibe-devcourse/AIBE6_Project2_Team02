package com.backend.common.global.ai;

import com.backend.common.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiService geminiService;

    @PostMapping("/projectDescription")
    public RsData<String> generateProjectDescription(
            @RequestBody ProjectDescriptionRequest  req
    ){
        String result = geminiService.generateProjectDescription(req.title());
        return  RsData.of("200","프로젝트 설명 초안이 생성되었습니다.",result);
    }

    @PostMapping("/portfolioIntroduction")
    public RsData<String> generatePortfolioIntroduction(@RequestBody PortfolioIntroductionRequest req){
        String result = geminiService.generatePortfolioIntroduction(req.title(), req.position(), req.techStacks());
        return RsData.of("200","포트폴리오 소개글 초안이 작성되었습니다.",result);
    }

    @PostMapping("/applicationMotivation")
    public RsData<String> generateApplicationMotivation(@RequestBody ApplicationMotivationRequest req){
        String result = geminiService.generateApplicationMotivation(
                req.projectTitle(), req.projectDescription(), req.position(), req.myTechStacks());
        return RsData.of("200","지원 동기 초안이 생성되었습니다.",result);
    }


    public record ProjectDescriptionRequest(String title) {}
    public record PortfolioIntroductionRequest(
            String title, String position, List<String> techStacks
    ){}

    public record ApplicationMotivationRequest(
            String projectTitle, String projectDescription,
            String position, List<String> myTechStacks
    ){}

}
