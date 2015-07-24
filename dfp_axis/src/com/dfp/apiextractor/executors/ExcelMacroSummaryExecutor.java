package com.dfp.apiextractor.executors;

import com.dfp.apiextractor.ExtractionInstruction;

/**
 * Created by keanguan on 7/20/15.
 */
public class ExcelMacroSummaryExecutor implements InstructionExecutor {


    private final StringBuilder outputString;

    public ExcelMacroSummaryExecutor(StringBuilder outputString) {

        this.outputString = outputString;
    }

    @Override
    public void executeOn(ExtractionInstruction instruction) {
        if(instruction.isLeaf()) {
            outputString.append(ExtractionInstruction.MACRO_STRING).append(instruction.getFullPathMacro()).append("\n");
        }
    }
}
