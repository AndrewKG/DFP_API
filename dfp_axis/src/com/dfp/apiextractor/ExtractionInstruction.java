package com.dfp.apiextractor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by keanguan on 7/2/15.
 */
public class ExtractionInstruction {
    public static final String MACRO_STRING = "%%%";
    private ExtractionInstruction parent = null;
    private List<ExtractionInstruction> children = new LinkedList<>();
    private Stack<ExtractionObject> extractionObjects = null;
    private Hashtable<String, Object> outputs;
    private Element saxElement;
    private List<List> sheet = null;
    private Hashtable<String, String> headers;

    //Internal hashtable keys
    private static final String LEAF_NODES = "#leaf_nodes";
    public static final String EXTRACTION_SHEETS = "#extraction_sheets";

    //These are all the supported schema in the configuration
    private static final String ID = "id";
    private static final String SERVICE = "service";
    private static final String FIELD = "field";
    private static final String MACRO = "macro";
    private static final String LIST = "list";
    private static final String METHOD = "method";
    private static final String ID_FIELD = "idField";
    private static final String ID_MACRO = "idMacro";
    private static final String PARENT_FIELD = "parentField";
    private static final String AGGREGATE = "aggregate";
    public static final String apiPropertiesFilePath=APIDataExtractor.getapiPropertiesFilePath();

    public ExtractionInstruction() {}

    public ExtractionInstruction(ExtractionInstruction parent, Element saxElement) {
        this.saxElement = saxElement;
        this.extractionObjects = parent.extractionObjects;
        this.outputs = parent.outputs;
        this.parent = parent;
    }


    public void process(ExtractionObject extractionObject,String id) throws Exception {
        if (extractionObject == null) {
            processRoot(id);
        }
        //Leaf node
        else if (CollectionUtils.isEmpty(saxElement.getChildren())) {
            processLeaf(extractionObject);
        }
        else {
            processSelf(extractionObject);
        }

    }


    public void processRoot(String id) throws Exception {
        this.outputs.put(LEAF_NODES, new LinkedList<ExtractionObject>());

        //Example <product idMacro="%%%proposal_id"...  the root
        if(StringUtils.isNotBlank(id)) {
            getExtractionObjectById(null, id);
        }
        //Example <product id="12345"...  the root
        else if (saxElement.getAttribute(ID) != null) {
            getExtractionObjectById(null, saxElement.getAttributeValue(ID));
        }
    }

    private void processSelf(ExtractionObject parentExtractionObject) throws Exception {

        if (parentExtractionObject == null) {
            return;
        }

        //Example <contact idField="contactIds"...
        if (saxElement.getAttribute(ID_FIELD) != null) {
            //get the id(s) from parent object
            String idField = saxElement.getAttributeValue(ID_FIELD);
            Object i = parentExtractionObject.getFieldValue(idField);
            long[] ids = null;
            if(i instanceof long[]) {
                ids = (long[]) i;
            } else if(i instanceof Long){
                ids = new long[1];
                ids[0] = (long)i;
            } else {
                return;
            }


            String listValue = saxElement.getAttributeValue(LIST);
            switch (listValue) {
                case "*":
                    listAll(ids, parentExtractionObject);
                    break;
                case "sheets":
                    listAll(ids, parentExtractionObject);
                    break;
                default:  //handle list="0,1,3"
                    listByIndex(ids, parentExtractionObject);
                    break;
            }
        }
        //Example <ProposalLineItem parentField="proposalId"
        else if (saxElement.getAttribute(PARENT_FIELD) != null) {
            List<Long> parentId = new ArrayList<>(1);
            //retrieve parent id
            //the field name "getId" is not verified, we just assume all object in the DFP domain has a "getId" method
            parentId.add((Long)parentExtractionObject.getFieldValue("id"));

            getExtractionObjectsByParentIds(parentId, parentExtractionObject);
        }
        //Example <ProposalCompanyAssociation field="Agencies">
        else if (saxElement.getAttribute(FIELD) != null) {
            Object[] childDFPObjects = (Object[]) parentExtractionObject.getFieldValue(saxElement.getAttributeValue(FIELD));
            creativeExtractionObjectFromList(Arrays.asList(childDFPObjects), parentExtractionObject);
        }
    }

    public boolean isSheet() {
        Attribute att = saxElement.getAttribute(LIST);
        return att != null && att.getValue().equals("sheets");
    }

    public boolean isLeaf() {
        return this.saxElement == null || CollectionUtils.isEmpty(this.saxElement.getChildren());
    }


    public ExtractionInstruction getParent() {
        return parent;
    }

    public Element getSaxElement() {
        return saxElement;
    }

    public void setSaxElement(Element saxElement) {
        this.saxElement = saxElement;
    }

    public Hashtable<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Hashtable<String, Object> outputs) {
        this.outputs = outputs;
    }

    public Stack<ExtractionObject> getExtractionObjects() {
        return extractionObjects;
    }

    public void setExtractionObjects(Stack<ExtractionObject> extractionObjects) {
        this.extractionObjects = extractionObjects;
    }

    public List<ExtractionInstruction> getChildren() {
        return children;
    }

    public void setChildren(List<ExtractionInstruction> children) {
        this.children = children;
    }

    public List<List> getSheet () {
        return this.sheet;
    }

    public void setSheet(List<List> sheet) {
        this.sheet = sheet;
    }

    public void next(ExtractionObject parentExtractionObject) throws Exception {
        if (CollectionUtils.isEmpty(children)) {
            if (!CollectionUtils.isEmpty(saxElement.getChildren())) {
                children = new ArrayList<>(saxElement.getChildren().size());
                for (Element element : saxElement.getChildren()) {
                    ExtractionInstruction nextInstruction = new ExtractionInstruction(parentExtractionObject.getExtractionInstruction(), element);
                    children.add(nextInstruction);
                }
            }
        }

        for (ExtractionInstruction nextInstruction : children) {
            nextInstruction.process(parentExtractionObject, null);
        }
    }

    private void processLeaf(ExtractionObject extractionObject) throws Exception {
        String fieldName = saxElement.getAttributeValue(FIELD);
        Object value = extractionObject.getFieldValue(fieldName);
        if(value != null) {
            String v = DataFormatter.format(this.saxElement.getName(), value);
            outputs.put(MACRO_STRING+extractionObject.getMacro() + this.getMacro(), v);
            ((List)outputs.get(LEAF_NODES)).add(value);
            extractionObject.fillSheet(v);
        } else {
            extractionObject.fillSheet("");
        }
    }




    private String makeCSV(List<Object> l) {
        StringBuilder stringBuffer = new StringBuilder(l.size() * 5);
        for (Object o : l) {
            stringBuffer.append(l.toString());
        }
        return stringBuffer.toString();
    }


    private ExtractionObject getExtractionObjectById(ExtractionObject parent, String id) throws Exception {
        Object dfpObject = DFPAPIAccess.getInstance().getObjectById(
                id,
                saxElement.getAttributeValue(SERVICE),
                saxElement.getAttributeValue(METHOD)
        );
        ExtractionObject eo = this.isSheet() ? new ExtractionSheetObject() : new ExtractionObject();
        eo.setParent(parent);
        eo.setExtractionInstruction(this);
        eo.setDfpObject(dfpObject);
        extractionObjects.push(eo);
        return eo;
    }

    private void getExtractionObjectsByIds(List<Long> ids, ExtractionObject parentExtractionObject) throws Exception {
        List<Object> dfpObjects = DFPAPIAccess.getInstance().getObjectsByIds(
                ids,
                saxElement.getName(),
                saxElement.getAttributeValue(SERVICE),
                saxElement.getAttributeValue(METHOD)
        );
        creativeExtractionObjectFromList(dfpObjects, parentExtractionObject);
    }

    private void getExtractionObjectsByParentIds(List<Long> parentIds, ExtractionObject parentExtractionObject) throws Exception {
        String parentFieldName = saxElement.getAttributeValue(PARENT_FIELD);

        List<Object> dfpObjects = DFPAPIAccess.getInstance().getObjectsByParentIds(
                parentFieldName,
                parentIds,
                saxElement.getName(),
                saxElement.getAttributeValue(SERVICE),
                saxElement.getAttributeValue(METHOD)
        );

        creativeExtractionObjectFromList(dfpObjects, parentExtractionObject);
    }

    private void creativeExtractionObjectFromList(List<Object> dfpObjects, ExtractionObject parentObject) {

        int siblingIndex = 0;
        for (Object dfpObject : dfpObjects) {
            ExtractionObject child = this.isSheet() ? new ExtractionSheetObject(parentObject, this, dfpObject) : new ExtractionObject(parentObject, this, dfpObject);
            child.setSiblingIndex(siblingIndex++);
            extractionObjects.push(child);
        }

        parentObject.setChildCount(siblingIndex);
    }

    //for the "*" list-all case
    private void listAll(long[] ids, ExtractionObject parentExtractionObject) throws Exception {
        ArrayList<Long> l = new ArrayList<>(ids.length);
        for (long id : ids) {
            l.add(id);
        }

        getExtractionObjectsByIds(l, parentExtractionObject);
    }

    //for the "list="0,1,2,5" case
    private void listByIndex(long[] ids, ExtractionObject parentExtractionObject) throws Exception {
        String[] pointers = saxElement.getAttributeValue(LIST).split(",");
        List<Long> newIds = new ArrayList<>(pointers.length);

        //extract indicated index pointers
        for (String pointer : pointers) {
            int ii = Integer.parseInt(pointer);
            if (ii < ids.length) {
                newIds.add(ids[ii]);
            }
        }

        getExtractionObjectsByIds(newIds, parentExtractionObject);
    }

    public String getMacro() {
        String macro = saxElement.getAttributeValue(MACRO);
        if(StringUtils.isNotBlank(macro)) {
            return macro;
        } else if(StringUtils.isNotBlank(saxElement.getAttributeValue(FIELD))) {
            return saxElement.getAttributeValue(FIELD);
        }

        return saxElement.getName();
    }

    public String getFullPathMacro() {
        if(parent == null) {
            return this.getMacro();
        }
        String m = parent.getFullPathMacro()+"_"+this.getMacro();

        if(this.saxElement.getAttribute(LIST) != null) {
            return m + "0";
        }

        return m;
    }

    public Hashtable getBook() {
        Hashtable book = (Hashtable) outputs.get(EXTRACTION_SHEETS);
        if(book == null) {
            book = new Hashtable<String, List>();
            outputs.put(EXTRACTION_SHEETS, book);
        }
        return book;
    }
}
