package io.kr.algotrial.trial.service;

import io.kr.algotrial.trial.dto.CodeReqDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class TrialService {

    public String trialCpp(CodeReqDto codeReqDto) {
        try {
            log.info("code Data : {}", codeReqDto.getCodeData());
            // 1. 임시 디렉터리 생성
            Path dir = getDirectoryPath();
            Path cppFilePath = dir.resolve("trial.cpp");
            Path binaryFilePath = dir.resolve("trial");

            // 2. C++ 코드 파일 생성
            Files.write(cppFilePath, codeReqDto.getCodeData().getBytes());

            // 3. C++ 코드 컴파일 및 유효성 검사
            Process compileProcess = getCompileProcess(cppFilePath, binaryFilePath);
            validateAndReturnOutput(compileProcess);

            // 4. 컴파일된 실행 파일 실행
            Process runProcess = runCompiledProcess(binaryFilePath);

            // 5. 실행 결과 유효성 검사 및 결과 반환
            return validateAndReturnOutput(runProcess);

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }

    private Path getDirectoryPath() throws IOException {
        Path dir = Paths.get("cpp-trial");
        if (Files.exists(dir)) {
            log.info("Directory already exists: {}", dir);
        } else {
            Files.createDirectory(dir);
            log.info("Directory created: {}", dir);
        }
        return dir;
    }

    private Process runCompiledProcess(Path binaryFilePath) throws IOException {
        ProcessBuilder runBuilder = new ProcessBuilder(binaryFilePath.toString());
        runBuilder.redirectErrorStream(true);
        return runBuilder.start();
    }

    private Process getCompileProcess(Path cppFilePath, Path binaryFilePath) throws IOException {
        ProcessBuilder compileBuilder = new ProcessBuilder(
                "g++", cppFilePath.toString(), "-o", binaryFilePath.toString()
        );
        compileBuilder.redirectErrorStream(true);
        return compileBuilder.start();
    }

    private String validateAndReturnOutput(Process compileProcess) {
        try {
            String compileOutput = readProcessOutput(compileProcess);

            // 정상적으로 종료되면 0을 반환, 아닐 경우 0이 아닌 값 반환
            if (compileProcess.waitFor() != 0) {
                throw new RuntimeException("Compilation Error:\n" + compileOutput);
            }
            return compileOutput;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }
}
