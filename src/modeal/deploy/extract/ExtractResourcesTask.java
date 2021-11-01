package modeal.deploy.extract;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class ExtractResourcesTask extends Task {
    private String excelScanPath;
    private String excelWildcardName;
	private String sourceDir;
	private String targetDir;
	private List<String> errorList;
	
	public ExtractResourcesTask() {
		this.errorList = new ArrayList<String>();
	}
	
	public String getExcelScanPath() {
		return excelScanPath;
	}

	public void setExcelScanPath(String excelScanPath) {
		this.excelScanPath = excelScanPath;
	}

	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public String getTargetDir() {
		return targetDir;
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public String getExcelWildcardName() {
		return excelWildcardName;
	}

	public void setExcelWildcardName(String excelWildcardName) {
		this.excelWildcardName = excelWildcardName;
	}

	@Override
	public void execute() throws BuildException {
		
        File scanDir = new File(this.getExcelScanPath());
        if (!scanDir.isDirectory()) {
            log(new StringBuilder().append("excelScanPath is not a directory. excelScanPath = ").append(this.getExcelScanPath()).toString());
            return;
        }
        
        if (StringUtils.isBlank(this.getSourceDir()))
        	this.setSourceDir("");
        log(new StringBuilder().append("sourceDir: ").append(this.getSourceDir()).toString());
        
        if (StringUtils.isBlank(this.getTargetDir()))
        	this.setTargetDir("");
        log(new StringBuilder().append("targetDir: ").append(this.getTargetDir()).toString());
        
        WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(this.getExcelWildcardName());
        File[] excelFiles = scanDir.listFiles((FilenameFilter) wildcardFileFilter);
        
        for (File excelFile : excelFiles) 
        {
        	try 
        	{
        		this.extractTargetFile(excelFile);
        	} 
        	catch (Exception e) 
        	{
        		this.addErrorMsg(new StringBuilder().append("Error Occur : ").append(excelFile.getName()).append(", msg:").append(e.toString()).toString());
        		e.printStackTrace();
        	} 
        	finally 
        	{
	        	if (!this.errorList.isEmpty()) {
	        		for (String errorMsg : this.errorList) {
        				log(errorMsg, Project.MSG_ERR);
	        		}
	        		throw new BuildException();
	        	}
        	}
        }
		
	}

	private void extractTargetFile(File excelFile) {
		
		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(excelFile));
		} catch (Exception e) {
			this.addErrorMsg(new StringBuilder().append("[").append(excelFile.getName()).append("] can not read the file.").toString());
			return;
		}
		
		Sheet sheet = wb.getSheetAt(0);
		int lastNum = sheet.getPhysicalNumberOfRows();
		
		Row row = null;
		String path = null;
		
		for (int i = 9; i <= lastNum; i++) {
			row = sheet.getRow(i);
			if (row == null) {
				if (i == 9) {
					this.addErrorMsg(new StringBuilder().append("[").append(excelFile.getName()).append("] deploy row not exist. ").append("path:").append(path).toString());
				} else {
					continue;
				}
			}
			
			if (row.getCell(2) == null) {
				continue;
			}
			path = row.getCell(2).toString();
			if (StringUtils.isBlank(path)) {
				continue;
			}
			path = StringUtils.replace(path, "\\", "/").trim();
			
			File srcFile = new File(new StringBuilder().append(this.getSourceDir()).append("/").append(path).toString());
	        if (!srcFile.exists()) {
	        	this.addErrorMsg(new StringBuilder().append("[").append(excelFile.getName()).append("] source file does not exist. ")
	            		.append("path:").append(path).toString());
	            continue;
	        }
	        
	        String dstDirFileName = new StringBuilder().append(this.getTargetDir()).append("/").append(path).toString();
	        String dstDirPath = StringUtils.substringBeforeLast(dstDirFileName, "/");
	        File dstDir = new File(dstDirPath);
            if (!dstDir.exists())
                dstDir.mkdirs();
            
            try 
            {
	            File dstFile = new File(dstDirFileName);
	            if (srcFile.isFile()) {
	            	FileUtils.copyFile(srcFile, dstFile, true);
	            } else if (srcFile.isDirectory()) {
	            	// 폴더배포 금지
	            	this.addErrorMsg(new StringBuilder().append("[").append(excelFile.getName()).append("] extract failure. becuase resource is folder :").append(path).toString());
	            }
	            log(new StringBuilder().append("[").append(excelFile.getName()).append("] Copy 1 File. path:").append(path).toString());
            }
            catch (Exception e) {
                this.addErrorMsg(new StringBuilder().append("[").append(excelFile.getName()).append("] extract failure. path:").append(path).append(", msg:").append(e.toString()).toString());
                continue;
            }
		}
			
	}
	
	private void addErrorMsg(String msg) {
		this.errorList.add(msg);
	}
	
	public static void main(String[] args) {
		ExtractResourcesTask task = new ExtractResourcesTask();
		
		task.setExcelScanPath("d:\\temp\\test");
		task.setExcelWildcardName("app.modeal.net_DEV_DEPLOY_REQUEST_kkuuk_01_20210813.xlsx");
		task.setSourceDir("D:\\temp\\test\\runtime");
		task.setTargetDir("D:\\temp\\test\\deploy");
		task.execute();
	}
}
