package io.kr.algotrial.trial.dto;

import io.kr.algotrial.trial.enums.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CodeReqDto {

    private int problemId;
    private String codeData;
    private Language language;
}
