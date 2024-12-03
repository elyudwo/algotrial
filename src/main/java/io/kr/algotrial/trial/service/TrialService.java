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

@Service
@RequiredArgsConstructor
@Slf4j
public class TrialService {

    private final CppTrialService cppTrialService;
    private final JavaTrialService javaTrialService;
    private final InputOutputService inputOutputService;
    private final TimeSpaceComplexityValidator timeSpaceComplexityValidator;

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
            Path dir = getDirectoryPath("cpp");
            Path cppFilePath = dir.resolve("trial.cpp");
            Path binaryFilePath = dir.resolve("trial");

            // 기존 C++ 코드
            String originalCode = codeReqDto.getCodeData();

            // 시간 측정 시작 코드
            String timeMeasureStart = """
                        auto start = std::chrono::high_resolution_clock::now();
                    """;

            // 시간 측정 종료 코드
            String timeMeasureEnd = """
                        auto end = std::chrono::high_resolution_clock::now();
                        std::chrono::duration<double> elapsed = end - start;
                        std::cout << "Elapsed time: " << elapsed.count() << " seconds" << std::endl;
                    """;

            // `main()` 함수의 시작 부분 찾기
            String modifiedCode = originalCode.replaceFirst(
                    "(int main\\(\\)\\s*\\{)",
                    "$1\n" + timeMeasureStart // `{` 다음 줄에 시작 코드 추가
            );

            // `main()` 함수 끝 부분 찾기
            int mainEndIndex = modifiedCode.lastIndexOf("return 0;");
            if (mainEndIndex != -1) {
                // `return 0;` 앞에 종료 코드 추가
                modifiedCode = modifiedCode.substring(0, mainEndIndex)
                        + timeMeasureEnd + "\n"
                        + modifiedCode.substring(mainEndIndex);
            }

            // 파일 생성
            Files.write(cppFilePath, modifiedCode.getBytes());

            log.info("modified Code : \n {}", modifiedCode);

            // 3. C++ 코드 컴파일
            cppTrialService.getCompileProcess(cppFilePath, binaryFilePath);

            Thread.sleep(1000);

            // 4. 컴파일된 실행 파일 실행
            Process runProcess = cppTrialService.runCompiledProcess(binaryFilePath, input, codeReqDto);

            // 5. 실행 결과 유효성 검사 및 결과 반환
            return validateAndReturnOutput(runProcess, output, codeReqDto);

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
            Path dir = getDirectoryPath("java");
            Path javaFilePath = dir.resolve("Main.java");

            // 2. Java 코드 파일 생성
            Files.write(javaFilePath, codeReqDto.getCodeData().getBytes());

            // 3. Java 코드 컴파일 및 유효성 검사
            javaTrialService.getJavaCompileProcess(javaFilePath);

            // 4. 컴파일된 .class 파일 실행
            Process runProcess = javaTrialService.runJavaProcess(dir, "Main", input, codeReqDto.getTimeComplexity());

            // 5. 실행 결과 유효성 검사 및 결과 반환
            return validateAndReturnOutput(runProcess, output, codeReqDto);

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }

    private Path getDirectoryPath(String path) throws IOException {
        Path dir = Paths.get(path);
        if (Files.exists(dir)) {
            log.info("Directory already exists: {}", dir);
        } else {
            Files.createDirectory(dir);
            log.info("Directory created: {}", dir);
        }
        return dir;
    }

    private String validateAndReturnOutput(Process process, String output, CodeReqDto codeReqDto) {
        try {
            String compileOutput = readProcessOutput(process, output, codeReqDto);

            // 정상적으로 종료되면 0을 반환, 아닐 경우 0이 아닌 값 반환
            if (process.waitFor() != 0) {
                throw new RuntimeException("Compilation Error:\n" + compileOutput);
            }

            return compileOutput;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String readProcessOutput(Process process, String output, CodeReqDto codeReqDto) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder();
        String lastLine = "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
                lastLine = line;
            }
        }

        int lastLineIndex = result.lastIndexOf(lastLine);
        if (lastLineIndex != -1) {
            result.delete(lastLineIndex, result.length());
        }

        String finalOutput = result.toString().trim();
        log.info("output Log: '{}'", finalOutput);
        log.info("S3 output: '{}'", output);

        timeSpaceComplexityValidator.validateTimeComplexity(codeReqDto.getTimeComplexity(), lastLine);

        if (!finalOutput.equals(output)) {
            throw new RuntimeException("output does not match");
        }
        //TODO: spaceComplexity도 어떻게할지 생각

        return finalOutput;
    }
}
