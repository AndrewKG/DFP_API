package com.dfp.apiextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelTools {
	
	/**
	 * get Id from excel
	 * 2003
	 * @param sheet
	 * @throws Exception 
	 */
	public String getDataById(HSSFSheet sheet) throws Exception{
		HSSFRow row = null;
		HSSFCell cell = null;
		int flag = 0;
		String content = "";
		
		for(int i=0;i<= sheet.getLastRowNum();i++){
			
			row = sheet.getRow(i);
			
			if(row == null){
				continue;
			}
			
			for(int j=0;j<row.getLastCellNum();j++){
				
				cell = row.getCell(j);
				
				if(cell == null){
					continue;
				}
				
				content = cell.getStringCellValue()==null?"":cell.getStringCellValue();
				
				if(content.contains("%%%")){
					if(content.contains(":")){
						//english:
						content = content.split(":")[1];
						flag = 1;
						break;
					}else if(content.contains("：")){
						//chinese：
						content = content.split("：")[1];
						flag = 1;
						break;
					}
				}
			}
			
			if(flag == 1){
				break;
			}
		}
		if(flag == 1){
			return content;
		}
		return "";
	}
	
	
	/**
	 * get field with %%% in excel and replace 
	 * @param sheet
	 * @param map 
	 * @throws Exception  
	 * 2003
	 */
	public void getExcel03Data(HSSFSheet sheet, Map<String,Object> map) throws Exception{
		HSSFRow row = null;
		HSSFCell cell = null;
		String content = "";
		
		for(int i=0;i<= sheet.getLastRowNum();i++){
			
			row = sheet.getRow(i);
			
			if(row == null){
				continue;
			}
			
			for(int j=0;j<row.getLastCellNum();j++){
				
				cell = row.getCell(j);
				
				if(cell == null){
					continue;
				}
				
				content = cell.getStringCellValue()==null?"":cell.getStringCellValue();
				
				if(content.contains("%%%")){
					//
					if(content.contains(":")){
						//english:
						content = content.split(":")[1];
						//replace id
						cell.setCellValue(content);
					}else{
						if(map.get(content)==null){
							
						}else{
							cell.setCellValue(map.get(content)==null?"-":map.get(content).toString());
						}
					}
				}
				
			}
			
		}		
	}
	
	/**
	 * write data into excel which xml list="sheets"  
	 * 2003
	 */
	public void getExtractionSheets03Data(HSSFWorkbook workbook, HSSFSheet sheet, Map<String,Object> map) throws Exception{

		Map<String,ArrayList<ArrayList<String>>> sheets = new HashMap<>();
		sheets = (Map<String,ArrayList<ArrayList<String>>>) map.get(ExtractionInstruction.EXTRACTION_SHEETS);
		if(sheets != null && !sheets.isEmpty()) {
			Set<String> keySet = sheets.keySet();
	    	for(String key:keySet){
	    		processHSSFSheet(workbook, key, sheets.get(key));
	    	}
		}
		
	}
	
	/**
	 * get Id from excel
	 * 2007
	 * @param sheet
	 * @return
	 * @throws Exception 
	 */
	public String getDataById(XSSFSheet sheet) throws Exception{
		XSSFRow row = null;
		XSSFCell cell = null;
		int flag = 0;
		String content = "";
		
		for(int i=0;i<= sheet.getLastRowNum();i++){
			
			row = sheet.getRow(i);
			
			if(row == null){
				continue;
			}
			
			for(int j=0;j<row.getLastCellNum();j++){
				
				cell = row.getCell(j);
				
				if(cell == null){
					continue;
				}
				
				content = cell.getStringCellValue()==null?"":cell.getStringCellValue();
				
				if(content.contains("%%%")){
					if(content.contains(":")){
						//english:
						content = content.split(":")[1];
						flag = 1;
						break;
					}else if(content.contains("：")){
						//chinese：
						content = content.split("：")[1];
						flag = 1;
						break;
					}
				}
			}
			
			if(flag == 1){
				break;
			}
		}
		if(flag == 1){
			return content;
		}
		return "";
	}
	
	/**
	 * write data into excel which xml list="sheets" 
	 * 2007
	 * @throws Exception 
	 */
	public void getExtractionSheets07Data(XSSFWorkbook workbook, XSSFSheet sheet, Map<String,Object> map) throws Exception{
	
		Map<String, List<List<String>>> sheets = new HashMap<>();
		sheets = (Map<String,List<List<String>>>) map.get(ExtractionInstruction.EXTRACTION_SHEETS);
		if(sheets != null && !sheets.isEmpty()) {
			Set<String> keySet = sheets.keySet();
	    	for(String key:keySet){
	    		System.out.println("key===："+ key);
	    		processXSSFSheet(workbook, key, sheets.get(key));
	    	}
		}
		
	}
	

	/**
	 * get field with %%% in excel and replace
	 * 2007
	 */
	public void getExcel07Data(XSSFSheet sheet, Map<String,Object> map) throws Exception{

		XSSFRow row = null;
		XSSFCell cell = null;
		String content = "";
		
		for(int i=0;i<= sheet.getLastRowNum();i++){
			
			row = sheet.getRow(i);
			
			if(row == null){
				continue;
			}
			
			for(int j=0;j<row.getLastCellNum();j++){
				
				cell = row.getCell(j);
				
				if(cell == null){
					continue;
				}
				
				content = cell.getStringCellValue()==null?"":cell.getStringCellValue();				
				
				if(content.contains("%%%")){
					//
					if(content.contains(":")){
						//english:
						content = content.split(":")[1];
						//replace id
						cell.setCellValue(content);
					}else{//in excel with %%% do not replace which belong to ExtractionSheets
						if(map.get(content)==null){
							
						}else{
							cell.setCellValue(map.get(content)==null?"-":map.get(content).toString());
						}
					}
				}
				
			}
			
		}
		
	}
		
	/**
	 * write data into excel
	 * 2007
	 * @param workbook
	 * @param key
	 * @param values
	 * @throws Exception
	 */
     private  void processXSSFSheet(XSSFWorkbook workbook, String key, List<List<String>> values) throws Exception{
    	 XSSFSheet sheet = null;
    	 XSSFRow row = null;
    	 XSSFCell cell = null;
    	 boolean fieldfindflag = false;
    	 //ExtractionSheets: exits %%% in excel
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheet = workbook.getSheetAt(i);

			if (sheet == null) {
				continue;
			}
						
			String content = "";

			for (int k = 0; k <= sheet.getLastRowNum(); k++) {

				row = sheet.getRow(k);

				if (row == null) {
					continue;
				}

				for (int j = 0; j < row.getLastCellNum(); j++) {

					cell = row.getCell(j);

					if (cell == null) {
						continue;
					}
					content = cell.getStringCellValue() == null ? "" : cell.getStringCellValue();
					System.out.println(content+"=="+key);
					// find the matched field
					if (content.equals(key)) {
						fieldfindflag = true;//exits
						for (int m = 0; m < values.size(); m++) {
							//the row will be replaced in excel
							row = sheet.getRow(k+m);
							if(row == null){
								row = sheet.createRow(k+m);
							}
							for (int n = 0; n < values.get(m).size(); n++) {
								//the col will be replaced in excel
								cell = row.getCell(j+n);
								if(cell == null){
									cell = row.createCell(j+n);
								}
								cell.setCellValue(values.get(m).get(n));
							}
						}
						break;
					}

				}
				if(fieldfindflag){
					break;
				}
			}
		}
		
		//ExtractionSheets: not exits %%% in excel
		if(!fieldfindflag){
			for(int i = 0; i < workbook.getNumberOfSheets(); i++){
				if(key.equals(workbook.getSheetName(i))){
					workbook.removeSheetAt(i);
					break;
				}					
			}
			sheet = workbook.createSheet(key);
			
			for (int m = 0; m < values.size(); m++) {
				row = sheet.createRow(m);
				for (int n = 0; n < values.get(m).size(); n++) {
					cell = row.createCell(n);
					cell.setCellValue(values.get(m).get(n));
				}
			}
		}				
     }
 	/**
 	 * write data into excel
 	 * 2003
 	 * @param workbook
 	 * @param key
 	 * @param values
 	 * @throws Exception 
 	 */
      private  void processHSSFSheet(HSSFWorkbook workbook, String key, List<ArrayList<String>> values) throws Exception{
    	 HSSFSheet sheet = null;
    	 HSSFRow row = null;
    	 HSSFCell cell = null;
     	 boolean fieldfindflag = false;
     	 
 		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
 			sheet = workbook.getSheetAt(i);

 			if (sheet == null) {
 				continue;
 			}
 			
 			
 			String content = "";

 			for (int k = 0; k <= sheet.getLastRowNum(); k++) {

 				row = sheet.getRow(k);

 				if (row == null) {
 					continue;
 				}

 				for (int j = 0; j < row.getLastCellNum(); j++) {

 					cell = row.getCell(j);

 					if (cell == null) {
 						continue;
 					}
 					content = cell.getStringCellValue() == null ? "" : cell.getStringCellValue();
 					// find the matched field
 					if (content.equals(key)) {
 						fieldfindflag = true;//exits
 						for (int m = 0; m < values.size(); m++) {
 							row = sheet.getRow(k+m);
 							if(row == null){
 								row = sheet.createRow(k+m);
 							}
 							for (int n = 0; n < values.get(m).size(); n++) {
 								cell = row.getCell(j+n);
 								if(cell == null){
 									cell = row.createCell(j+n);
 								}
 								cell.setCellValue(values.get(m).get(n));
 							}
 						}
 						break;
 					}

 				}
 				if(fieldfindflag){
 					break;
 				}
 			}
 		}
 		
 		
 		if(!fieldfindflag){
			for(int i = 0; i < workbook.getNumberOfSheets(); i++){
				if(key.equals(workbook.getSheetName(i))){
					workbook.removeSheetAt(i);
					break;
				}					
			}
 			sheet = workbook.createSheet(key);
 			
 			for (int m = 0; m < values.size(); m++) {
 				row = sheet.createRow(m);
 				for (int n = 0; n < values.get(m).size(); n++) {
 					cell = row.createCell(n);
 					cell.setCellValue(values.get(m).get(n));
 				}
 			}
 		 }
      }	  
}
