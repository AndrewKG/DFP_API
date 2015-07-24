package com.dfp.apiextractor;

import org.apache.commons.beanutils.PropertyUtils;


/**
 * Created by keanguan on 7/3/15.
 */
public class ExtractionObject {
    private ExtractionObject parent = null;
    protected ExtractionInstruction extractionInstruction;
    private Object dfpObject;
    private Integer siblingIndex = null;
    private int childCount;

    public ExtractionObject() {
    }

    public ExtractionObject(ExtractionObject parent, ExtractionInstruction extractionInstruction, Object dfpObject) {
        this.parent = parent;
        this.extractionInstruction = extractionInstruction;
        this.dfpObject = dfpObject;
    }


    public ExtractionObject getParent() {
        return parent;
    }

    public void setParent(ExtractionObject parent) {
        this.parent = parent;
    }

    public ExtractionInstruction getExtractionInstruction() {
        return extractionInstruction;
    }

    public void setExtractionInstruction(ExtractionInstruction extractionInstruction) {
        this.extractionInstruction = extractionInstruction;
    }

    public Object getDfpObject() {
        return dfpObject;
    }

    public void setDfpObject(Object dfpObject) {
        this.dfpObject = dfpObject;
    }

    public Object getFieldValue(String fieldName) throws Exception {
        return PropertyUtils.getProperty(this.dfpObject, fieldName);
    }

    public Integer getSiblingIndex() {
        return siblingIndex;
    }

    public void setSiblingIndex(Integer siblingIndex) {
        this.siblingIndex = siblingIndex;
    }

    public String getMacro() {
        return (parent == null ? "" : parent.getMacro())
                + this.extractionInstruction.getMacro()
                + (siblingIndex == null ? "" : siblingIndex)
                + '_';
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getChildCount() {
        return childCount;
    }

    //This method is overridden by ExtractionSheetObject.fillSheet() if this is a sheet
    public void fillSheet(String value) {
        ExtractionObject parent = getParent();
        if(parent != null) {
            parent.fillSheet(value);
        }
    }
}
