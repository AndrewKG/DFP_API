package com.dfp.apiextractor;

import com.dfp.apiextractor.executors.InstructionExecutor;

import org.apache.commons.collections.CollectionUtils;

import java.util.Stack;

/**
 * Created by keanguan on 7/20/15.
 */
public class InstructionTreeVisitor {
    private final InstructionExecutor executor;
    private final Stack<ExtractionInstruction> extractionInstructions;
    public InstructionTreeVisitor(ExtractionInstruction root, InstructionExecutor instructionExecutor) {
        extractionInstructions = new Stack<>();
        extractionInstructions.push(root);
        this.executor = instructionExecutor;
    }

    public void traverse () {
        while (!extractionInstructions.empty()) {
            ExtractionInstruction next = extractionInstructions.pop();
            executor.executeOn(next);
            if(CollectionUtils.isNotEmpty(next.getChildren())) {
                for (ExtractionInstruction child : next.getChildren()) {
                    extractionInstructions.push(child);
                }
            }
        }
    }
}
