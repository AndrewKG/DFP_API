package com.dfp.apiextractor;


import com.dfp.apiextractor.executors.ExcelMacroSummaryExecutor;
import com.dfp.apiextractor.executors.SheetHeaderExecutor;
import com.dfp.auth.GetRefreshToken;
import com.sun.org.apache.xml.internal.utils.IntStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class APIDataExtractor {
    private static Logger logger = Logger.getLogger(APIDataExtractor.class);
    static String apiPropertiesFilePath = "";

    public static String getapiPropertiesFilePath() {
        return apiPropertiesFilePath;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Parameters:");
        for (String arg : args) {
            System.out.println(arg);
        }

        System.out.println("End of parameters:");

        try {
            if(args.length == 0) {
                printHelp();
                return;
            }
            //Generate security token
            else if("getRefreshToken".equals(args[0])) {
                GetRefreshToken.main(args);
                return;
            }
            //Get sample macros from the indicated instructions
            else if("getExcelMarcoSamples".equals(args[0])) {
                if(args.length>1) {
                    String instructionPath = args[1];
                    String keyfile = (args.length>2 && args[2] != null) ? args[2] : "key.txt";
                    ExtractionInstruction root = getRootInstruction(instructionPath);
                    getExcelMarcoSamples(root, new FileWriter(keyfile));
                    createSheets(root);
                } else {
                    System.out.print("Not enough parameters. Missing instruction file path.");
                }
                return;
            }
            String file = args[0];
            String instructionPath = args[1];
            apiPropertiesFilePath = args[2];
            String keyfile = "";
            if (args.length<4 || StringUtils.isBlank(args[3])) {
                keyfile = "key.txt";
            } else {
                keyfile = args[3];
            }

            //get log config file
            PropertyConfigurator.configure("log4j.properties");

            //get id from excel
            String id = getId(file);
            System.out.println("the ID in excel:" + id);
            logger.info("the ID in excel:" + id);


            Hashtable outputs = extract(id, instructionPath);

            //write keys into txt file
            writeKeysIntoTxt(keyfile, outputs);

            // read Excel replace field with %%% and write HashTable data into excel which key not exits in excel
            readExcel(file, outputs);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

    }


    private static Hashtable extract(String id, String instructionPath) throws Exception {

        ExtractionInstruction rootInstruction = getRootInstruction(instructionPath);
        rootInstruction.processRoot(id);


        Stack<ExtractionObject> extractionObjects = rootInstruction.getExtractionObjects();
        while (!extractionObjects.empty()) {
            //when there is still more extraction object to process
            ExtractionObject nextExtractionObject = extractionObjects.pop();

            //process the next batch of instruction on the object
            //the next instructions will push new Extraction Objects into the stack for next round of processing
            nextExtractionObject.getExtractionInstruction().next(nextExtractionObject);
        }
        return rootInstruction.getOutputs();
    }

    private static ExtractionInstruction buildInstructionTree(Element root) {
        Stack<ExtractionInstruction> extractionInstructions = new Stack<>();
        ExtractionInstruction rootInstruction = new ExtractionInstruction();
        rootInstruction.setSaxElement(root);
        rootInstruction.setExtractionObjects(new Stack<>());
        rootInstruction.setOutputs(new Hashtable<>());
        extractionInstructions.push(rootInstruction);

        while (!extractionInstructions.empty()) {
            ExtractionInstruction next = extractionInstructions.pop();
            if(CollectionUtils.isNotEmpty(next.getSaxElement().getChildren())) {
                for (Element element : next.getSaxElement().getChildren()) {
                    ExtractionInstruction child = new ExtractionInstruction(next, element);
                    next.getChildren().add(child);
                    extractionInstructions.push(child);
                }

            }
        }

        createSheets(rootInstruction);
        return rootInstruction;
    }

    private static ExtractionInstruction getRootInstruction(String instructionPath) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(new File(instructionPath));
        Element root = doc.getRootElement();

        for (Element element : root.getChildren()) {
            System.out.println(element.getName());
            for (Attribute attribute : element.getAttributes()) {
                System.out.println(attribute.getName() + "   " + attribute.getValue());
                logger.info(attribute.getName() + "   " + attribute.getValue());
            }
        }

        ExtractionInstruction rootInstruction = buildInstructionTree(root);
        return rootInstruction;
    }

    private static void getExcelMarcoSamples(ExtractionInstruction rootInstruction, FileWriter outputFile) throws JDOMException, IOException {

        StringBuilder outString = new StringBuilder();
        InstructionTreeVisitor treeVisitor = new InstructionTreeVisitor(rootInstruction, new ExcelMacroSummaryExecutor(outString));
        treeVisitor.traverse();
        outputFile.write(outString.toString());
        outputFile.close();
    }


    private static void createSheets(ExtractionInstruction rootInstruction) {
        InstructionTreeVisitor treeVisitor = new InstructionTreeVisitor(rootInstruction, new SheetHeaderExecutor());
        treeVisitor.traverse();
        return;
    }

    private static void printHelp() {
        //TODO: finish writing help
        System.out.println("Parameters:");
        System.out.println("getRefreshToken  --  Start the Refresh Token generation guide");
        System.out.println("getExcelMarcoSamples instructions.xml  --  Generate sample macros from the indicated instruction XML file.");
        System.out.println("To be finished");
    }

    private static void operateExcel2003(String file, Map<String, Object> map) throws Exception {

        ExcelTools tools = new ExcelTools();
        FileInputStream fis = new FileInputStream(file);
        HSSFWorkbook workbook = new HSSFWorkbook(fis);
        HSSFSheet sheet = null;

        System.out.println(" total sheet num：" + workbook.getNumberOfSheets());
        logger.info(" total sheet num：" + workbook.getNumberOfSheets());
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);

            if (sheet == null) {
                continue;
            }

            if (map != null) {
                //read field with %%% and replace
                tools.getExcel03Data(sheet, map);
            }
        }

        if (map != null) {
            //write HashTable data into excel
            tools.getExtractionSheets03Data(workbook, sheet, map);
        }

        fis.close();

        FileOutputStream out = null;
        out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

    }

    private static void operateExcel2007(String file, Map<String, Object> map) throws Exception {
        ExcelTools tools = new ExcelTools();
        FileInputStream fis = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = null;

        System.out.println("Total # of sheets："
                + workbook.getNumberOfSheets());
        logger.info("Total # of sheets：" + workbook.getNumberOfSheets());

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);

            if (sheet == null) {
                continue;
            }

            if (map != null) {
                tools.getExcel07Data(sheet, map);
            }
        }

        if (map != null) {
            tools.getExtractionSheets07Data(workbook, sheet, map);
        }

        fis.close();

        FileOutputStream out = null;
        out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

    }

    /**
     * get ID from excel
     *
     * @param file
     * @throws Exception
     */
    private static String getId(String file) throws Exception {
        ExcelTools tools = new ExcelTools();
        String id = "";
        if (file.endsWith(".xls")) {
            //2003
            FileInputStream fis = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = null;

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet = workbook.getSheetAt(i);

                if (sheet == null) {
                    continue;
                }

                id = tools.getDataById(sheet);
                if (id != null) {
                    break;
                }
            }

        } else if (file.endsWith(".xlsx")) {
            //2007
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = null;

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet = workbook.getSheetAt(i);

                if (sheet == null) {
                    continue;
                }

                id = tools.getDataById(sheet);
                if (id != null) {
                    break;
                }
            }
        }
        return id;
    }

    private static void writeKeysIntoTxt(String keyfile, Hashtable<String, Object> outputs) throws Exception {

        //String keyfile="/Users/bait/Downloads/key.txt";
        FileWriter fw = new FileWriter(keyfile);
        Set<String> keySet = outputs.keySet();
        for (String obj : keySet) {
            System.out.println(obj);
            logger.info("key：" + obj);
            fw.write(obj);
            fw.write("\r\n");
        }
        fw.close();
    }

    private static void readExcel(String file, Hashtable<String, Object> outputs) throws Exception {
        if (file.endsWith(".xls")) {
            //2003
            operateExcel2003(file, outputs);
        } else if (file.endsWith(".xlsx")) {
            //2007
            operateExcel2007(file, outputs);
        }
    }
}