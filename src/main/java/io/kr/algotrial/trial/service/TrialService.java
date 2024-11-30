package io.kr.algotrial.trial.service;

import io.kr.algotrial.trial.dto.CodeReqDto;
import io.kr.algotrial.trial.enums.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrialService {

    private final CppTrialService cppTrialService;
    private final JavaTrialService javaTrialService;
    private final InputOutputService inputOutputService;

    public String trialProblem(CodeReqDto codeReqDto) {
        if (codeReqDto.getLanguage() == Language.CPP) {
            return trialCpp(codeReqDto);
        }
        return trialJava(codeReqDto);
    }

    private String trialCpp(CodeReqDto codeReqDto) {
        // TODO: S3에서 Text 파일 조회
        String input = inputOutputService.getInputText(codeReqDto.getProblemId());
        String output = inputOutputService.getOutputText(codeReqDto.getProblemId());

        log.info("input : {}", input);
        log.info("output : {}", output);

        try {
            log.info("code Data : {}", codeReqDto.getCodeData());
            // 1. 임시 디렉터리 생성
            Path dir = getDirectoryPath();
            Path cppFilePath = dir.resolve("trial.cpp");
            Path binaryFilePath = dir.resolve("trial");

            // 2. C++ 코드 파일 생성
            Files.write(cppFilePath, codeReqDto.getCodeData().getBytes());

            // 3. C++ 코드 컴파일 및 유효성 검사
            Process compileProcess = cppTrialService.getCompileProcess(cppFilePath, binaryFilePath);
            validateAndReturnOutput(compileProcess, output);

            // 4. 컴파일된 실행 파일 실행
            Process runProcess = cppTrialService.runCompiledProcess(binaryFilePath, input);

            // 5. 실행 결과 유효성 검사 및 결과 반환
            return validateAndReturnOutput(runProcess, output);

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }

    private String trialJava(CodeReqDto codeReqDto) {
        String input = inputOutputService.getInputText(codeReqDto.getProblemId());
        String output = inputOutputService.getOutputText(codeReqDto.getProblemId());
        try {
            log.info("Code Data : {}", codeReqDto.getCodeData());

            // 1. 임시 디렉터리 생성
            Path dir = getDirectoryPath();
            Path javaFilePath = dir.resolve("Trial.java");
            Path classFilePath = dir.resolve("Trial.class");

            // 2. Java 코드 파일 생성
            Files.write(javaFilePath, codeReqDto.getCodeData().getBytes());

            // 3. Java 코드 컴파일 및 유효성 검사
            Process compileProcess = javaTrialService.getJavaCompileProcess(javaFilePath);
            validateAndReturnOutput(compileProcess, output);

            // 4. 컴파일된 .class 파일 실행
            Process runProcess = javaTrialService.runJavaProcess(dir, "Trial");

            // 5. 실행 결과 유효성 검사 및 결과 반환
            return validateAndReturnOutput(runProcess, output);

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

    private String validateAndReturnOutput(Process process, String output) {
        try {
            String compileOutput = readProcessOutput(process, output);

            // 정상적으로 종료되면 0을 반환, 아닐 경우 0이 아닌 값 반환
            if (process.waitFor() != 0) {
                throw new RuntimeException("Compilation Error:\n" + compileOutput);
            }
            return compileOutput;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String readProcessOutput(Process process, String output) throws IOException {
        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        String finalOutput = result.toString().trim();
        log.info("output Log: '{}'", finalOutput);
        log.info("S3 output: '{}'", output);

        return finalOutput;
    }
}
