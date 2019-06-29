package mercedestask2;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TaskTwo { 

	public static final String SAMPLE_XLSX_FILE = "Test_Data.xlsx";
	private String emailError1;
	private String messageEmailError;
	private String email;
	private String title;
	private String firstName;
	private String lastName;
	private String number;
	private String street;
	private String town;
	private String post;
	private String baseUrl;
	private String[] inputAddressArray = new String[8]; 
	WebDriver driver;


	@Before
	public void readTestData() throws Exception {
		//Read Test Data from Excel file

		File file = new File(System.getProperty("user.dir"), SAMPLE_XLSX_FILE);
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
		XSSFSheet sheet = workbook.getSheetAt(0); // first sheet
		XSSFRow row = sheet.getRow(1); // second line

		title = row.getCell(0).getStringCellValue();
		inputAddressArray[0] = title;

		firstName = row.getCell(1).getStringCellValue();
		lastName = row.getCell(2).getStringCellValue();
		inputAddressArray[1] = firstName+" "+lastName;

		number = row.getCell(3).getNumericCellValue()+"";
		street =row.getCell(4).getStringCellValue();

		inputAddressArray[2] = street+" "+number;

		post = row.getCell(6).getStringCellValue();
		town = row.getCell(5).getStringCellValue();
		inputAddressArray[3] = post+" "+town;

		email = row.getCell(7).getStringCellValue();
		inputAddressArray[4] = email;

		emailError1 = row.getCell(8).getStringCellValue();
		messageEmailError = row.getCell(9).getStringCellValue();
		workbook.close();
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver","C:\\tools\\chromedriver2.exe");   
		driver = new ChromeDriver();
		baseUrl = "https://shop.mercedes-benz.com/en-gb/collection/";
		driver.get(baseUrl);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void addItemToBasket() throws InterruptedException {   
		// click on the 2nd element (position=1) from the products carousel
		driver.findElements(By.className("utils-product-cms-carousel-link")).get(1).click();

		// close the Cookies pop-up
		driver.findElement(By.id("button-text")).click(); 

		driver.findElements(By.cssSelector("button[class='wb-e-btn-1 dcp-pdp-buy-box-add-to-basket__cta ng-scope ng-binding']")).get(0).click();
		driver.findElements(By.cssSelector("button[class='wb-e-btn-1 dcp-modal__cta dcp-modal__cta--primary ng-binding']")).get(0).click();

		//Save the item characteristics 
		String itemName = driver.findElement(By.xpath("//span[@data-testid='co-orderline-list-product-name']")).getAttribute("innerHTML");
		String itemNumber = driver.findElement(By.xpath("//p[@data-testid='co-orderline-product-article-number']")).getAttribute("innerHTML");
		String itemColor = driver.findElement(By.xpath("//span[@data-testid='co-orderline-product-color']")).getAttribute("innerHTML");

		//Press button to "continue to address and deliver"
		driver.findElement(By.cssSelector("button[data-testid='co-func-footer-forward']")).click();

		//Enter email in "Place an order as a guest" and proceed
		WebElement emailField = driver.findElement(By.xpath("//input[@data-testid='co-order-process-login-guest-user-email']"));
		emailField.sendKeys(emailError1);
		emailField.sendKeys("\t"); // change to another field

		String errorEmailMessagePage =  driver.findElement(By.xpath("//div[@data-ng-message='pattern']")).getAttribute("innerHTML");
		Assert.assertEquals(messageEmailError, errorEmailMessagePage);

		emailField.clear();
		emailField.sendKeys(email);
		driver.findElement(By.cssSelector("button[data-testid='co-order-process-login-guest-user-cta']")).click();

		//Step 2 - Add Invoice data
		driver.findElement(By.xpath("//label[@for='co_payment_address-salutationCode-radio-id-1']")).click();
		driver.findElement(By.xpath("//input[@data-testid='dcp-schema-form-default_co_payment_address-firstName']")).sendKeys(firstName);
		driver.findElement(By.xpath("//input[@data-testid='dcp-schema-form-default_co_payment_address-lastName']")).sendKeys(lastName);

		driver.findElement(By.xpath("//input[@data-testid='dcp-schema-form-default_co_payment_address-line2']")).sendKeys(number);
		driver.findElement(By.xpath("//input[@data-testid='dcp-schema-form-default_co_payment_address-line1']")).sendKeys(street);
		driver.findElement(By.xpath("//input[@data-testid='dcp-schema-form-default_co_payment_address-town']")).sendKeys(town);
		driver.findElement(By.xpath("//input[@data-testid='dcp-schema-form-default_co_payment_address-postalCode']")).sendKeys(post);

		//Click on the button "Continue to payment type"
		WebElement button = driver.findElement(By.cssSelector("button[data-testid='co-func-footer-forward']"));
		WebDriverWait wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.elementToBeClickable(button));
		button.click();

		//Step 3 - Select Credit Card Visa
		driver.findElement(By.xpath("//label[@for='dcp-co-payment-modes_options-CREDITCARD']")).click();
		driver.findElement(By.xpath("//label[@for='visa']")).click();
		driver.findElement(By.cssSelector("button[data-testid='co-func-footer-forward']")).click();

		//Step 4 - Data verification
		String itemNameVerification = driver.findElement(By.xpath("//span[@data-testid='co-orderline-list-product-name']")).getAttribute("innerHTML");
		String itemNumberVerification = driver.findElement(By.xpath("//p[@data-testid='co-orderline-product-article-number']")).getAttribute("innerHTML");
		String itemColorVerification = driver.findElement(By.xpath("//span[@data-testid='co-orderline-product-color']")).getAttribute("innerHTML");

		//Validate the invoice address
		String invoiceAddress = driver.findElement(By.xpath("//div[@class='dcp-co-order-data-panel__content ng-binding']")).getAttribute("innerHTML");
		String[] invoiceAddressArray = invoiceAddress.split("<br>");
		for (int i = 0; i < invoiceAddressArray.length; i++) {
			Assert.assertEquals(invoiceAddressArray[i].trim(),inputAddressArray[i]);  
		}

		//Validate that the product is the same that was previously selected
		Assert.assertEquals(itemNameVerification, itemName);
		Assert.assertEquals(itemNumberVerification, itemNumber);
		Assert.assertEquals(itemColorVerification, itemColor);	  

		driver.close();
	}
}