package com.ksf.job.contract.authen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;

public class Auto {

    private static final Logger logger = LogManager.getLogger();

    public void exec() {
        try {
            System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("start-maximized");
            WebDriver driver = new ChromeDriver(options);

            driver.get("https://ks-invplus.ksfinance.net");
            Thread.sleep(1000);
            driver.findElement(By.xpath("//*[@id=\"Username\"]")).sendKeys("0942990834");
            Thread.sleep(1000);
            driver.findElement(By.xpath("//*[@id=\"Password\"]")).sendKeys("12345678");
            Thread.sleep(1000);
            driver.findElement(By.xpath("/html/body/div[2]/div/div[2]/div/div[1]/section/form/div[5]/div/button")).click();
            Thread.sleep(5000);

            try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    logger.info(line);
                    driver.get("https://ks-invplus.ksfinance.net");
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("/html/body/app-root/app-dashboard/div/div[2]/app-sidebar/p-menubar/div/p-menubarsub/ul/li[2]/a")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("/html/body/app-root/app-dashboard/div/div[2]/app-sidebar/p-menubar/div/p-menubarsub/ul/li[2]/p-menubarsub/ul/li[2]/a")).click();
                    Thread.sleep(5000);
                    driver.findElement(By.xpath("//*[@id=\"filter\"]")).sendKeys(line);
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("/html/body/app-root/app-dashboard/div/div[2]/app-so-lenh/div/div/div[1]/div[2]/div[1]/p-button/button")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("//*[@id=\"myGrid\"]/div/div[2]/div[2]/div[3]/div[3]/div/div[2]/app-button-renderer/button")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("/html/body/div[2]/ul/li[1]/a")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("//*[@id=\"p-tabpanel-2-label\"]")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("//*[@id=\"p-tabpanel-2\"]/app-danh-muc-ho-so-khach-hang-ky/div[2]/app-edit-detail/form/div[1]/p-button[1]/button")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("/html/body/app-root/router-outlet/p-confirmdialog/div/div/div[3]/p-footer/button[1]")).click();
                    Thread.sleep(30000);
                    driver.findElement(By.xpath("//*[@id=\"p-tabpanel-8\"]/app-danh-muc-ho-so-khach-hang-ky/div[2]/app-edit-detail/form/div[1]/p-button[2]/button")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("/html/body/app-root/router-outlet/p-confirmdialog/div/div/div[3]/p-footer/button[1]")).click();
                    String firstResult = new WebDriverWait(driver, Duration.ofSeconds(15))
                            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/router-outlet/p-toast/div/p-toastitem/div/div/div/div[2]"))).getText();
                    logger.info(firstResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
