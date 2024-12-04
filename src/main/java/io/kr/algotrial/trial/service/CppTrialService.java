package io.kr.algotrial.trial.service;

import io.kr.algotrial.trial.dto.CodeReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JsonMixinModuleEntries;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class CppTrialService {

    private final TimeSpaceComplexityValidator timeSpaceComplexityValidator;
    private final JsonMixinModuleEntries jsonMixinModuleEntries;

    public Process runCompiledProcess(Path binaryFilePath, String input, CodeReqDto codeReqDto) {
        try {
            log.info("run 진입 성공");
            ProcessBuilder runBuilder = new ProcessBuilder(binaryFilePath.toString());
            runBuilder.redirectErrorStream(true);
            Process process = runBuilder.start();
            log.info("Process 시작 성공");

            try (OutputStream outputStream = process.getOutputStream()) {
                log.info("Input data being sent to the process: '{}' by CPP", input);
                outputStream.write(input.getBytes());
                outputStream.flush();
            }

            return process;
        } catch (IOException e) {
            throw new RuntimeException("Failed to run C++ compile ", e);
        }
    }

    public Process getCompileProcess(Path cppFilePath, Path binaryFilePath) {
        try {
            ProcessBuilder compileBuilder = new ProcessBuilder("g++", cppFilePath.toString(), "-o", binaryFilePath.toString());
            compileBuilder.redirectErrorStream(true);
            return compileBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get C++ compile ");
        }
    }

    public String addTimeComplexityCode(String originalCode) {
        String timeMeasureStart = getTimeMeasureStart();
        String timeMeasureEnd = getTimeMeasureEnd();

        // `main()` 함수의 시작 부분 찾기
        String modifiedCode = addingTimeComplexityStartCode(originalCode, timeMeasureStart);

        // `main()` 함수 끝 부분 찾기
        int mainEndIndex = modifiedCode.lastIndexOf("return 0;");
        modifiedCode = additionTimeComplexityEndCode(mainEndIndex, modifiedCode, timeMeasureEnd);

        log.info("modified code complete\" {}", modifiedCode);
        return modifiedCode;
    }

    private String addingTimeComplexityStartCode(String originalCode, String timeMeasureStart) {
        String modifiedCode =  originalCode.replaceFirst("(int main\\(\\)\\s*\\{)", "$1\n" + timeMeasureStart);
        if(modifiedCode.equals(originalCode)) {
            throw new RuntimeException("Main function does not exist\n");
        }
        return modifiedCode;
    }

    private String additionTimeComplexityEndCode(int mainEndIndex, String modifiedCode, String timeMeasureEnd) {
        if(mainEndIndex == -1) {
            throw new RuntimeException("No return value for main function\n");
        }

        return modifiedCode.substring(0, mainEndIndex) + timeMeasureEnd + "\n" + modifiedCode.substring(mainEndIndex);
    }

    private String getTimeMeasureStart() {
        return """
                    auto start = std::chrono::high_resolution_clock::now();
                """;
    }

    private String getTimeMeasureEnd() {
        return """
                    auto end = std::chrono::high_resolution_clock::now();
                    std::chrono::duration<double> elapsed = end - start;
                    std::cout << "Elapsed time: " << elapsed.count() << " seconds" << std::endl;
                """;
    }
}
