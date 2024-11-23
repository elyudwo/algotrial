package io.kr.algotrial.trial.controller;

import io.kr.algotrial.trial.dto.CodeReqDto;
import io.kr.algotrial.trial.service.TrialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trial")
@RequiredArgsConstructor
public class TrialController {

    private final TrialService trialService;

    @PostMapping("/cpp")
    public ResponseEntity<String> cppTrial(@RequestBody CodeReqDto codeReqDto) {
        return ResponseEntity.ok(trialService.trialCpp(codeReqDto));
    }

}
