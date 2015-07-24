package com.dfp.apiextractor.executors;


import com.dfp.apiextractor.ExtractionInstruction;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by keanguan on 7/21/15.
 */
public class SheetHeaderExecutor implements InstructionExecutor {
    @Override
    public void executeOn(ExtractionInstruction instruction) {
        createSheet(instruction);
    }

    private void createSheet(ExtractionInstruction instruction) {
        if (instruction.isSheet()) {
            List sheet = new LinkedList<>();
            instruction.setSheet(sheet);

            instruction.getBook().put(instruction.getMacro(), sheet);

            //add the header row
            LinkedList<String> header = new LinkedList<>();
            sheet.add(header);
        } else if (instruction.isLeaf()) {
            ExtractionInstruction parent = instruction.getParent();
            while (parent != null) {
                if (parent.isSheet()) {
                    parent.getSheet().get(0).add(instruction.getMacro());
                }
                parent = parent.getParent();
            }
        }
    }
}
