package io.kr.algotrial.trial.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class JavaTrialService {

    public Process runJavaProcess(Path dir, String className) {
        try {
            ProcessBuilder runBuilder = new ProcessBuilder(
                    "java", "-cp", dir.toString(), className);
            runBuilder.redirectErrorStream(true);
            return runBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to run java compile");
        }
    }

    public Process getJavaCompileProcess(Path javaFilePath) {
        try {
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "javac", javaFilePath.toString()
            );
            compileBuilder.redirectErrorStream(true);
            return compileBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get java compile");
        }
    }
}
