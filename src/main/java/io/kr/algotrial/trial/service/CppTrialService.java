package io.kr.algotrial.trial.service;

import io.kr.algotrial.trial.dto.CodeReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class CppTrialService {

    private final TimeSpaceComplexityValidator timeSpaceComplexityValidator;

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
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "g++", cppFilePath.toString(), "-o", binaryFilePath.toString());
            compileBuilder.redirectErrorStream(true);
            return compileBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get C++ compile ");
        }
    }
}
