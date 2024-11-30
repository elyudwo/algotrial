package io.kr.algotrial.trial.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CppTrialService {

    public Process runCompiledProcess(Path binaryFilePath, String input) {
        try {
            ProcessBuilder runBuilder = new ProcessBuilder(binaryFilePath.toString());
            runBuilder.redirectErrorStream(true);
            Process process = runBuilder.start();

            try (OutputStream outputStream = process.getOutputStream()) {
                log.info("Input data being sent to the process: '{}'", input);
                outputStream.write(input.getBytes());
                outputStream.flush();
            }

            validateTimeComplexity(process);

            return process;
        } catch (IOException e) {
            throw new RuntimeException("Failed to run C++ compile ", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Execution interrupted ", e);
        }
    }

    private void validateTimeComplexity(Process process) throws InterruptedException {
        boolean checkTimeout = process.waitFor(1, TimeUnit.SECONDS);

        if(!checkTimeout) {
            process.destroy();
            throw new RuntimeException("Execution Timeout: Process took too long to complete.");
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
