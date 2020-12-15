package com.news18.stepdefinition;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.api.base.ApplicationFuncs;
import com.cucumber.listener.Reporter;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.news18.locators.English_Homepage;
import com.utilities.base.ConfigReader;
import com.utilities.base.DriverUtil;
import com.utilities.base.GlobalUtil;
import com.utilities.base.KeywordUtil;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;


public class Stepdefinitions_English extends ApplicationFuncs {

	List<String> list = new ArrayList<String>();
	List<String> mobileWebList = new ArrayList<String>();
	String sectionName;

	@Given("^User navigates to News(\\d+) mobile web home page$")
	public void user_navigates_to_News_mobile_web_home_page(int arg1) throws Throwable {
		GlobalUtil.setDriver(DriverUtil.getChromeBrowser("CHROME"));
		KeywordUtil.navigateToUrl("https://www.news18.com/");
	}

	@When("^Gets all top stories list$")
	public void gets_all_top_stories_list() throws Throwable {
		sectionName = "Top Stories";
		KeywordUtil.waitForVisible(English_Homepage.titleHeader);
		List<WebElement> homepageNewsList = KeywordUtil.getListElements(English_Homepage.homepageNews);

		String title = KeywordUtil.getElementText(English_Homepage.mainTitle);
		mobileWebList.add(title);

		List<String> homeNews = new ArrayList<String>();
		for (int k = 1; k <= homepageNewsList.size(); k++) {

			KeywordUtil.waitForVisible(By.xpath("//div[@class='mstory-thumb-wrap']//div[@class='mstory-row']["+k+"]/div[@class='text']//p"));
			title = KeywordUtil.getElementText(By.xpath("//div[@class='mstory-thumb-wrap']//div[@class='mstory-row']["+k+"]/div[@class='text']//p"));
			boolean status = KeywordUtil.isWebElementVisible(By.xpath("//div[@class='mstory-thumb-wrap']//div[@class='mstory-row']["+k+"]/div[@class='text']/a"));
			if (status) {
				homeNews.add(title);
			}
		}
		List<String> homeList = new ArrayList<String>();
		for (int j = 1; j<=3; j++) {

			KeywordUtil.waitForVisible(By.xpath("//div[@class='mtopstory-wrap']//li["+j+"]/a"));
			title = KeywordUtil.getElementText(By.xpath("//div[@class='mtopstory-wrap']//li["+j+"]/a"));
			Boolean l2SectionList = KeywordUtil.isWebElementVisible(By.xpath("//div[@class='mtopstory-wrap']//li["+j+"]/a"));
			if (l2SectionList) {
				homeList.add(title);
			}
		}
		mobileWebList.addAll(homeNews);
		mobileWebList.addAll(homeList);

		for (String str : mobileWebList) { 
			System.out.print(str + "\n"); 
		} 
		System.out.println("---------------------------------------------------");
		GlobalUtil.getDriver().close();
	}

	@When("^User hits News(\\d+) mobile app api$")
	public void user_hits_News_mobile_app_api(int arg1) throws Throwable {

		RestAssured.baseURI = ConfigReader.getValue("News18_EndPoint");

		Response response = RestAssured.given().when().get("en/get/news18:en_v1_app_homefeed/android/1/86/");
		//		System.out.println("Response Body is =>  " + response.getBody().asString());
		//		System.out.println("Response Code is =>  " + response.statusCode());

		JsonPath path = response.jsonPath();

		for (int i = 0; i <= 6; i++) {
			String storysection = path.get("node[" + i + "].storysection").toString();
			if (storysection.equalsIgnoreCase("top stories")) {
				List<String> l2 = path.get("node[" + i + "].data");
				for (int j = 0; j < l2.size(); j++) {
					List<String> l3 = path.get("node[" + i + "].data[" + j + "].data_node");
					for (int k = 0; k < l3.size(); k++) {
						String childSection = path.get("node[" + i + "].data[" + j + "].data_node.child_layout_type["+k+"]").toString();
						if(childSection.equalsIgnoreCase("top_stories") || childSection.equalsIgnoreCase("medium_horizontal_stories") || childSection.equalsIgnoreCase("vertical_list_stories"))
						{
							list.add(path.get("node[" + i + "].data[" + j + "].data_node[" + k + "].headline").toString());
						}
					}
				}
			}
		}
		for (String str : list) { 
			System.out.print(str + "\n"); 
		} 
	}

	int num=1;
	Sheet sheet = null;
	String FILE_NAME = "ExecutionReport.xlsx"; 
	InputStream inp;
	static Workbook wb;
	@Then("^Compare the stories of Mobile Web and Mobile App$")
	public void compare_the_stories_of_Mobile_Web_and_Mobile_App() throws Throwable {
		int m=0;
		inp = new FileInputStream(FILE_NAME);
		wb = WorkbookFactory.create(inp); 
		int numberOfSheets = wb.getNumberOfSheets();
		//Sheet sheet = wb.getSheetAt(0);
		//sheet = null; 
		if(sectionName.equalsIgnoreCase("Top Stories"))
			sheet = wb.getSheetAt(0);
		else
			//			sheet = wb.getSheetAt(2);
		{
			for(int i=0; i<wb.getNumberOfSheets();i++) {
				if(wb.getSheetName(i).equalsIgnoreCase("English")) {
					sheet=wb.getSheetAt(i);
				}
			}
		}
		if(sectionName.equalsIgnoreCase("Top Stories"))
		{
			for(int i=1;i<=numberOfSheets;i++)
			{
				wb.removeSheetAt(0);
			}

			sheet = wb.createSheet("English");
			Row	row = sheet.createRow(0);
			Cell c = row.getCell(0);
			if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
				row.createCell(0).setCellValue("Section Name"); 
			}
			c = row.getCell(1);
			if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
				row.createCell(1).setCellValue("Article Number"); 
			}
			c = row.getCell(2);
			if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
				row.createCell(2).setCellValue("Mobile Web"); 
			}
			c = row.getCell(3);
			if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
				row.createCell(3).setCellValue("Mobile App"); 
			}
			c = row.getCell(4);
			if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
				row.createCell(4).setCellValue("Result"); 
			}
			num=sheet.getLastRowNum()+1;
		}
		else {
			num=sheet.getLastRowNum()+2;
		}
		for(int i=0;i<list.size();i++)
		{
			Row	row = sheet.createRow(num++);

			if((list.get(i).replace(" ", "").contains(mobileWebList.get(i).replace(" ", ""))) || (mobileWebList.get(i).replace(" ", "").contains(list.get(i).replace(" ", ""))) || (list.get(i).replace(" ", "").equalsIgnoreCase(mobileWebList.get(i).replace(" ", ""))))
			{
				Reporter.addStepLog(" <font color='green'> <font color='magenta'> "+(i+1)+" </font> article is same for both Mobile web and Mobile App :  <font color='magenta'> "+list.get(i)+" </font> </font>");
				Cell c = row.getCell(0);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(0).setCellValue(sectionName); 
				}
				c = row.getCell(1);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(1).setCellValue(i+1); 
				}
				c = row.getCell(2);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(2).setCellValue(mobileWebList.get(i)); 
				}
				c = row.getCell(4);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(4).setCellValue("PASS"); 
				}
			}
			else
			{
				Reporter.addStepLog(" <font color='red'> <font color='magenta'> "+(i+1)+" </font> article in Mobile web has: <font color='magenta'> "+mobileWebList.get(i)+" </font> and Mobile App has: <font color='magenta'> "+list.get(i)+" </font> </font> " );
				m++;

				Cell c = row.getCell(0);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(0).setCellValue(sectionName); 
				}
				c = row.getCell(1);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(1).setCellValue(i+1); 
				}
				c = row.getCell(2);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(2).setCellValue(mobileWebList.get(i)); 
				}
				c = row.getCell(3);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(3).setCellValue(list.get(i)); 
				}
				c = row.getCell(4);
				if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
					row.createCell(4).setCellValue("Fail"); 
				}
			}
		}
		FileOutputStream fileOut = new FileOutputStream(FILE_NAME); 
		wb.write(fileOut); 
		fileOut.close(); 
		list.removeAll(list);
		mobileWebList.removeAll(mobileWebList);
		KeywordUtil.delay(3000);
		if(m>0)
		{
			Assert.fail("Got "+m+" mismatch");
		}
	}

	@When("^Gets top (\\d+) stories of \"([^\"]*)\" list$")
	public void gets_top_stories_of_list(int count, String section) throws Throwable {
		sectionName = section;
		if(section.equalsIgnoreCase("HOT & Trending"))
		{
			KeywordUtil.waitForVisible(English_Homepage.hotAndTrendingCount);
			List<WebElement> hotTrendingCount = KeywordUtil.getListElements(English_Homepage.hotAndTrendingCount);
			for(int i=1;i<=count+1;i++)
			{	
				if(i!=2)
				{
					KeywordUtil.waitForVisible(By.xpath("//div[@class='hntranding']//ul/li["+i+"]/a[3]"));
					String title = KeywordUtil.getElementText(By.xpath("//div[@class='hntranding']//ul/li["+i+"]/a[3]"));
					Boolean status = KeywordUtil.isWebElementVisible(By.xpath("//div[@class='hntranding']//ul/li["+i+"]/a[3]"));
					if (status) {
						mobileWebList.add(title);
					}
				}
			}
		}
		else if(section.equalsIgnoreCase("Videos"))
		{
			KeywordUtil.waitForVisible(English_Homepage.videos);
			KeywordUtil.mouseOver(English_Homepage.videos);
			KeywordUtil.scroll();
			for(int i=1;i<=count;i++)
			{	
				KeywordUtil.delay(2000);
				if(i>2)
				{
					KeywordUtil.waitForVisible(English_Homepage.videoRightButton);
					KeywordUtil.clickJS(English_Homepage.videoRightButton);
					KeywordUtil.delay(1000);
				}
				KeywordUtil.waitForVisible(By.xpath("//span[text()='Videos']/../following-sibling::div//li[not(@class='glide__slide--clone')]["+i+"]/a[2]"));
				String title = KeywordUtil.getElementText(By.xpath("//span[text()='Videos']/../following-sibling::div//li[not(@class='glide__slide--clone')]["+i+"]/a[2]"));
				Boolean status = KeywordUtil.isWebElementVisible(By.xpath("//span[text()='Videos']/../following-sibling::div//li[not(@class='glide__slide--clone')]["+i+"]/a[2]"));
				if (status) {
					mobileWebList.add(title);
				}
			}			
		}
		else if(section.equalsIgnoreCase("Photos"))
		{
			KeywordUtil.waitForVisible(English_Homepage.photoGalleries);
			KeywordUtil.mouseOver(English_Homepage.photoGalleries);
			KeywordUtil.scroll();
			for(int i=1;i<=count;i++)
			{	
				KeywordUtil.delay(2000);
				if(i>2)
				{
					KeywordUtil.waitForVisible(English_Homepage.photoGalleriesRightArrow);
					KeywordUtil.clickJS(English_Homepage.photoGalleriesRightArrow);
					KeywordUtil.delay(1000);
				}
				KeywordUtil.waitForVisible(By.xpath("//span[text()='Photogalleries']/../following-sibling::div//li[not(@class='glide__slide--clone')]["+i+"]/a[2]"));
				String title = KeywordUtil.getElementText(By.xpath("//span[text()='Photogalleries']/../following-sibling::div//li[not(@class='glide__slide--clone')]["+i+"]/a[2]"));
				Boolean status = KeywordUtil.isWebElementVisible(By.xpath("//span[text()='Photogalleries']/../following-sibling::div//li[not(@class='glide__slide--clone')]["+i+"]/a[2]"));
				if (status) {
					mobileWebList.add(title);
				}
			}			
		}
		else if(section.equalsIgnoreCase("India") || section.equalsIgnoreCase("Cricket") || section.equalsIgnoreCase("Technology") || section.equalsIgnoreCase("World"))
		{
			if(section.equalsIgnoreCase("Technology"))
				section = "Tech";
			else if (section.equalsIgnoreCase("Cricket"))
				section = "Cricketnext";
			KeywordUtil.waitForVisible(By.xpath("//h2/span[text()='" + section + "']"));
			KeywordUtil.mouseOver(By.xpath("//h2/span[text()='" + section + "']"));
			for(int i=1;i<=count;i++)
			{	
				KeywordUtil.waitForVisible(By.xpath("//span[text()='" + section + "']/parent::h2/following-sibling::ul/li[" + i + "]/a"));
				String title = KeywordUtil.getElementText(By.xpath("//span[text()='" + section + "']/parent::h2/following-sibling::ul/li[" + i + "]/a"));
				Boolean status = KeywordUtil.isWebElementVisible(By.xpath("//span[text()='" + section + "']/parent::h2/following-sibling::ul/li[" + i + "]/a"));
				if (status) {
					mobileWebList.add(title);
				}
			}			
		}
		for (String str : mobileWebList) { 
			System.out.print(str + "\n"); 
		} 
		System.out.println("---------------------------------------------------");
		GlobalUtil.getDriver().close();

	}


	@When("^User hits News(\\d+) mobile app apifor \"([^\"]*)\"$")
	public void user_hits_News_mobile_app_apifor(int arg1, String section) throws Throwable {

		RestAssured.baseURI = ConfigReader.getValue("News18_EndPoint");
		int pageNumber=1;

		if(section.equalsIgnoreCase("HOT & Trending") || section.equalsIgnoreCase("Videos"))
		{
			pageNumber =2;
		}
		else if(section.equalsIgnoreCase("India") || section.equalsIgnoreCase("Photos"))
		{
			pageNumber =3;
		}
		else if(section.equalsIgnoreCase("Cricket") || section.equalsIgnoreCase("Technology"))
		{
			pageNumber =4;
		}
		else if(section.equalsIgnoreCase("World"))
		{
			pageNumber =5;
		}
		Response response = RestAssured.given().when().get("en/get/news18:en_v1_app_homefeed/android/"+pageNumber+"/86/");
		System.out.println("Response Body is =>  " + response.getBody().asString());
		System.out.println("Response Code is =>  " + response.statusCode());
		JsonPath path = response.jsonPath();
		List<String> l1=path.get("node");
		System.out.println(l1.size());
		for (int i = 0; i < l1.size(); i++) {
			String storysection = path.get("node[" + i + "].data[0].headline").toString();

			if (storysection.equalsIgnoreCase(section)) {

				List<String> l2 = path.get("node[" + i + "].data");

				for (int j = 0; j < l2.size(); j++) {
					List<String> l3 = path.get("node[" + i + "].data[" + j + "].data_node");

					for (int k = 0; k < l3.size(); k++) {
						String childSection = path.get("node[" + i + "].data[" + j + "].data_node.child_layout_type["+k+"]").toString();

						if(childSection.equalsIgnoreCase("list_top_stories") || childSection.equalsIgnoreCase("medium_horizontal_stories") || childSection.equalsIgnoreCase("vertical_list_stories"))
						{
							list.add(path.get("node[" + i + "].data[" + j + "].data_node[" + k + "].headline").toString());
						}
					}
				}
			}
		}
		for (String str : list) { 
			System.out.print(str + "\n"); 
		} 
	}

	@When("^Gets all sections list$")
	public void gets_all_sections_list() throws Throwable {

		KeywordUtil.waitForVisible(English_Homepage.sections);
		List<WebElement> count = KeywordUtil.getListElements(English_Homepage.sections);

		for(int i=1;i<=count.size();i++)
		{
			KeywordUtil.mouseOver(By.xpath("(//div[@class='container']/div[contains(@class,'hntranding') or contains(@class,'vspacer30')]//h2/span)["+i+"]"));
			String title = KeywordUtil.getElementText(By.xpath("(//div[@class='container']/div[contains(@class,'hntranding') or contains(@class,'vspacer30')]//h2/span)["+i+"]")).toLowerCase().replace("from our", "from our shows");
			Boolean status = KeywordUtil.isWebElementVisible(By.xpath("(//div[@class='container']/div[contains(@class,'hntranding') or contains(@class,'vspacer30')]//h2/span)["+i+"]"));
			if (status) {
				if(title.equalsIgnoreCase("tech"))
					title = "technology";
				else if (title.equalsIgnoreCase("cricketnext"))
					title = "cricket";
				else if (title.equalsIgnoreCase("photogalleries"))
					title = "photos";
				mobileWebList.add(title);
			}
		}
		for (String str : mobileWebList) { 
			System.out.print(str + "\n"); 
		} 
		GlobalUtil.getDriver().close();
	}
	ArrayList<String> mobileappList=new ArrayList<String>();
	@When("^User hits News(\\d+) mobile app api for getting sections$")
	public void user_hits_News_mobile_app_api_for_getting_sections(int arg1) throws Throwable {
		Response response;
		String storysection;
		RestAssured.baseURI = ConfigReader.getValue("News18_EndPoint");
		for(int i=2;i<=5;i++)
		{
			response = RestAssured.given().when().get("en/get/news18:en_v1_app_homefeed/android/"+i+"/86/");
			JsonPath path = response.jsonPath();

			List<String> l1=path.get("node");
			for (int j = 0; j < l1.size(); j++) {
				storysection = path.get("node[" + j + "].data[0].headline").toString().toLowerCase();
				if(!storysection.equals(""))
					mobileappList.add(storysection);
			}

		}
		for (String str : mobileappList) { 
			System.out.print(str + "\n"); 
		} 
	}

	@Then("^Compare the sections in Mobile Web and Mobile App$")
	public void compare_the_sections_in_Mobile_Web_and_Mobile_App() throws Throwable {
		Collection<String> similar = new HashSet<String>( mobileWebList );
		Collection<String> different = new HashSet<String>();
		Collection<String> appList = new HashSet<String>(mobileappList);
		different.addAll( mobileWebList );
		similar.retainAll( mobileappList );
		different.removeAll( similar );
		appList.removeAll(similar);
		System.out.printf("MobileWebList:%s%nMobileAppList:%s%nExtraSectionsInWeb:%s%nExtraSectionsInApp:%s%n",mobileWebList,mobileappList,different,appList);
		//System.out.printf("One:%s%nTwo:%s%n", mobileWebList, mobileappList, similar, different);
		// wb = WorkbookFactory.create(inp);
		for(int i=0; i<wb.getNumberOfSheets();i++) {
			if(wb.getSheetName(i).equalsIgnoreCase("English")) {
				sheet=wb.getSheetAt(i);
			}
		}

		StringBuilder strbul = new StringBuilder();
		for(String str:mobileWebList) {
			strbul.append(str);
			strbul.append(",");
		}
		String mobWebList = strbul.toString();

		StringBuilder strbul1 = new StringBuilder();
		for(String str:mobileappList) {
			strbul1.append(str);
			strbul1.append(",");
		}
		String mobAppList = strbul1.toString();

		StringBuilder strbul2 = new StringBuilder();
		for(String str:different) {
			strbul2.append(str);
			strbul2.append(",");
		}
		String extraSectionsInWeb = strbul2.toString();

		StringBuilder strbul3 = new StringBuilder();
		for(String str:appList) {
			strbul3.append(str);
			strbul3.append(",");
		}
		String extraSectionsInApp = strbul3.toString();

		num=sheet.getLastRowNum()+2;
		Row	row = sheet.createRow(num++);
		Cell c = row.getCell(0);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			row.createCell(0).setCellValue("MobileWebList");
		}
		c = row.getCell(1);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			if(mobWebList.isEmpty()) {
				row.createCell(1).setCellValue("No Sections");
			}else {
				row.createCell(1).setCellValue(mobWebList);
			}
		}


		row = sheet.createRow(num++);
		c = row.getCell(0);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			row.createCell(0).setCellValue("MobileAppList");
		}
		c = row.getCell(1);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			if(mobAppList.isEmpty()) {
				row.createCell(1).setCellValue("No Sections");
			}else {
				row.createCell(1).setCellValue(mobAppList);
			}
		}


		row = sheet.createRow(num++);
		c = row.getCell(0);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			row.createCell(0).setCellValue("ExtraSectionsInWeb");
		}
		c = row.getCell(1);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			if(extraSectionsInWeb.isEmpty()) {
				row.createCell(1).setCellValue("No Extra Sections");
			}else {
				row.createCell(1).setCellValue(extraSectionsInWeb);
			}
		}


		row = sheet.createRow(num++);
		c = row.getCell(0);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {
			row.createCell(0).setCellValue("ExtraSectionsInApp");
		}
		c = row.getCell(1);
		if (!(c != null && c.getCellType() != Cell.CELL_TYPE_BLANK)) {

			if(extraSectionsInApp.isEmpty()) {
				row.createCell(1).setCellValue("No Extra Section");
			}else {
				row.createCell(1).setCellValue(extraSectionsInApp);
			}
		}

		FileOutputStream fileOut = new FileOutputStream(FILE_NAME); 
		wb.write(fileOut); 
		fileOut.close(); 
		list.removeAll(list);
		mobileWebList.removeAll(mobileWebList);
		KeywordUtil.delay(3000);

		if(different.size()==0)
		{
			Reporter.addStepLog(" <font color='green'> Both Mobile Web and Mobile App has  <font color='magenta'> "+similar.size()+" </font> sections </font>");
			Reporter.addStepLog(" <font color='magenta'> "+similar+" </font>");
		}
		else
		{
			Reporter.addStepLog(" <font color='red'> Sections which are not in Mobile App are <font color='magenta'> "+different+" </font> </font>");
			Assert.fail("Mobile Web has more sections");
		}
	}

}