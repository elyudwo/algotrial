package io.kr.algotrial.trial.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class CppTrialService {

    public Process runCompiledProcess(Path binaryFilePath) {
        try {
            ProcessBuilder runBuilder = new ProcessBuilder(binaryFilePath.toString());
            runBuilder.redirectErrorStream(true);
            return runBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to run C++ compile ");
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
