package com.ksf.job.contract.authen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Auth {

    private static final Logger logger = LogManager.getLogger();

    public String exec(String baseUrl, String session) {
        try {
            System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
            WebDriver driver = new ChromeDriver();

            driver.get(baseUrl);
            Thread.sleep(1000);
            driver.findElement(By.xpath("//*[@id=\"Username\"]")).sendKeys("0947907199");
            driver.findElement(By.xpath("//*[@id=\"Password\"]")).sendKeys("Muahe2022@");
            driver.findElement(By.xpath("/html/body/div[2]/div/div[2]/div/div[1]/section/form/div[5]/div/button")).click();

            Thread.sleep(1000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String sessionStorage = (String) js.executeScript("return sessionStorage.getItem('"+ session +"');");
            JsonObject jsonObject = new JsonParser().parse(sessionStorage).getAsJsonObject();
            String token = jsonObject.get("access_token").getAsString();
            logger.info("token:" + token);
            driver.close();
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
