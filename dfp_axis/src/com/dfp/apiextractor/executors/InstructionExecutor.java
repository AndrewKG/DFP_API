package com.dfp.apiextractor.executors;

import com.dfp.apiextractor.ExtractionInstruction;

/**
 * Created by keanguan on 7/20/15.
 */
public interface InstructionExecutor {
    public void executeOn(ExtractionInstruction instruction);
}
