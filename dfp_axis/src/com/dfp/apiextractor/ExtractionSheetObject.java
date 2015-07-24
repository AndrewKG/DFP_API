package com.dfp.apiextractor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by keanguan on 7/7/15.
 */
public class ExtractionSheetObject extends ExtractionObject {
    List<String> row = null;
    public ExtractionSheetObject(ExtractionObject parentObject, ExtractionInstruction instruction, Object dfpObject) {
        super(parentObject, instruction, dfpObject);
        initializeSheet();
    }

    public ExtractionSheetObject() {
    }

    @Override
    public void setExtractionInstruction(ExtractionInstruction extractionInstruction) {
        this.extractionInstruction = extractionInstruction;
        if(row == null) {
            initializeSheet();
        }
    }

    private void initializeSheet() {
        this.row = new LinkedList<>();
        this.extractionInstruction.getSheet().add(row);
    }

    @Override
    public void fillSheet(String value) {
        this.row.add(value);
        getParent().fillSheet(value);
    }
}
