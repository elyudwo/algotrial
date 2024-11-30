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

    public Process runCompiledProcess(Path binaryFilePath, String input, double timeComplexity) {
        try {
            ProcessBuilder runBuilder = new ProcessBuilder(binaryFilePath.toString());
            runBuilder.redirectErrorStream(true);
            Process process = runBuilder.start();

            try (OutputStream outputStream = process.getOutputStream()) {
                log.info("Input data being sent to the process: '{}' by CPP", input);
                outputStream.write(input.getBytes());
                outputStream.flush();
            }

            validateTimeComplexity(process, timeComplexity);

            return process;
        } catch (IOException e) {
            throw new RuntimeException("Failed to run C++ compile ", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Execution interrupted ", e);
        }
    }

    private void validateTimeComplexity(Process process, double timeComplexity) throws InterruptedException {
        long timeoutInMillis = (long) (timeComplexity * 1000);
        boolean checkTimeout = process.waitFor(timeoutInMillis, TimeUnit.MILLISECONDS);

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
