package modeal.util;

import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SetConsignInfo {
	
	public static void main(String[] args) {
		
		String fileFullName = "D:/02.업무/05.API/탁송/자료/옵션설명수집_탁송료_통합_20210720.xlsx";
		int minIdx = 6;
		int maxIdx = 100;
		String otherType = "스파크,쉐보레A,이쿼녹스,볼트 EV,카마로,쌍용탁송,QM6,XM3,SM6,캡쳐,마스터,조에,트위지,쌍용A,렉스턴 스포츠 칸,쉐보레B";
		String resultType = "E"; // E or Q
		
		SetConsignInfo.setConsignInfo(fileFullName, minIdx, maxIdx, otherType, resultType);
	}
	
	
	private static void setConsignInfo(String fileFullName, int minIdx, int maxIdx, String otherType, String resultType) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fileFullName));
			int sheetCnt = 0;
			int cnt = 1;
			for (Sheet sheet : workbook) {
				sheetCnt++;
				if (!(minIdx <= sheetCnt && sheetCnt <= maxIdx)) {
					continue;
				}
				String carName = sheet.getSheetName();
				String dmlQuery = "";
				if ("Q".equals(resultType)) {
					System.out.println("/************************** " + carName + " **************************/");
				}
				if (otherType.indexOf(carName) >= 0) {
					for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
						Row row = sheet.getRow(i);
						String cityNm = SetConsignInfo.getCellValue(row.getCell(1), "NULL");
						String releasePlaceNm = SetConsignInfo.getCellValue(row.getCell(2), "NULL");
						String consignAmt = SetConsignInfo.getCellValue(row.getCell(3), "'0'");
						if ("Q".equals(resultType)) {
							dmlQuery += "INSERT INTO C_CONSIGN_INFO (CONSIGN_CARNM, CITY_NM, RELEASE_PLACE_NM, CONSIGN_AMT) VALUES ('" + carName + "', " + cityNm + ", " + releasePlaceNm + ", " + consignAmt + ");\n";
						} else {
							dmlQuery += (cnt++) + "	" + carName + "	" + cityNm + "		" + releasePlaceNm + "	" + consignAmt + "\n";
							dmlQuery = dmlQuery.replaceAll("'", "").replaceAll("NULL", "");
						}
					}
				} else {
					String releaseTemp = "";
					Row releaseRow = sheet.getRow(1);
					for (int i = 3; i < releaseRow.getPhysicalNumberOfCells(); i++) {
						releaseTemp += "," + SetConsignInfo.getCellValue(releaseRow.getCell(i), "NULL");
					}
					releaseTemp = releaseTemp.substring(1);
					String[] arrReleasePlaceNm = releaseTemp.split(",");
					for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
						Row row = sheet.getRow(i);
						String cityNm = SetConsignInfo.getCellValue(row.getCell(1), "NULL");
						String cityDetailNm = SetConsignInfo.getCellValue(row.getCell(2), "NULL");
						for (int z = 0; z < arrReleasePlaceNm.length; z++) {
							String releasePlaceNm = arrReleasePlaceNm[z];
							String consignAmt = SetConsignInfo.getCellValue(row.getCell(3 + z), "'0'");
							if ("Q".equals(resultType)) {
								dmlQuery += "INSERT INTO C_CONSIGN_INFO (CONSIGN_CARNM, CITY_NM, CITY_DETAIL_NM, RELEASE_PLACE_NM, CONSIGN_AMT) VALUES ('" + carName + "', " + cityNm + ", " + cityDetailNm + ", " + releasePlaceNm + ", " + consignAmt + ");\n";
							} else {
								dmlQuery += (cnt++) + "	" + carName + "	" + cityNm + "	" + cityDetailNm + "	" + releasePlaceNm + "	" + consignAmt + "\n";
								dmlQuery = dmlQuery.replaceAll("'", "").replaceAll("NULL", "");
							}
						}
					}
				}
				System.out.print(dmlQuery);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private static String getCellValue(Cell cell, String nullValue) {
    	String retVal = "";
    	
    	if (cell == null) {
    		retVal = nullValue;
    	} else {
	    	CellType cellType = cell.getCellType();
	    	if (CellType.NUMERIC.equals(cellType)) {
	    		retVal = "'" + Integer.toString((int)cell.getNumericCellValue()) + "'";
	    	} else {
	    		retVal = "'" + cell.getStringCellValue() + "'";
	    	}
    	}
    	if ("''".equals(retVal)) {
    		retVal = nullValue;
    	}
    	return retVal;
    }
}
