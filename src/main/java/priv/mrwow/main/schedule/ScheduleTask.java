package priv.mrwow.main.schedule;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import priv.mrwow.main.Service.IndexService;
import priv.mrwow.main.mapper.UserMapper;
import priv.mrwow.main.model.PO.User;
import priv.mrwow.main.model.example.UserExample;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Driver;
import java.util.List;

/**
 * @author Mr.Wow
 * @date 5/3/2022 7:56 PM
 */
@Configuration
@EnableScheduling
public class ScheduleTask {
    private final UserMapper userMapper;

    public ScheduleTask(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void configureTasks() throws IOException, InterruptedException {
        List<User> users = userMapper.selectByExample(new UserExample());
        System.setProperty("webdriver.gecko.driver", "/home/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        for (User user : users) {
            WebDriver driver = new FirefoxDriver(options);
            login(user, driver);
            declare(driver);
            driver.quit();
        }
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 1.1))
    public void declare(WebDriver driver) throws InterruptedException {
        driver.get("http://jksb.sysu.edu.cn/infoplus/form/XNYQSB/start");
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id=\"form_command_bar\"]/li[@class=\"command_button\"]/a")));
        Thread.sleep(3000);
        driver.findElement(By.xpath("//ul[@id=\"form_command_bar\"]/li[@class=\"command_button\"]/a")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id=\"form_command_bar\"]/li[@class=\"command_button\"]/a")));
        Thread.sleep(3000);
        driver.findElement(By.xpath("//ul[@id=\"form_command_bar\"]/li[@class=\"command_button\"]/a")).click();
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 1.1))
    public void login(User user, WebDriver driver) throws IOException {
        driver.get("https://cas.sysu.edu.cn/cas/login");
        driver.findElement(By.id("username")).sendKeys(user.getUsername());
        driver.findElement(By.id("password")).sendKeys(user.getPassword());
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        WebElement element = driver.findElement(By.id("captchaImg"));
        Rectangle rect = element.getRect();
        BufferedImage subImage = ImageIO.read(src).getSubimage(rect.x, rect.y, rect.getWidth(), rect.height);
        String imgPath = "/mrwow/autoHealthCheck/code.png";
        File verificationCodeImg = new File(imgPath);
        ImageIO.write(subImage, "png", verificationCodeImg);
        String verificationCode = identifyCode(imgPath);
        driver.findElement(By.id("captcha")).sendKeys(verificationCode);
        driver.findElement(By.xpath("//input[@class=\"btn btn-submit btn-block\"]")).click();
        //driver.findElement(By.xpath("//*[@id=\"global-header-unread\"]")).getText();
        //driver.findElement(By.xpath("//span[contains(text(), \"登 录\")]/..")).click();
    }

    public String identifyCode(String imgPath) throws IOException {
        File file = new File(imgPath);
        String resultString = Jsoup.connect("http://api.ttshitu.com/predict")
                                   .data("username", "MrWow")
                                   .data("password", "wanghao123")
                                   .data("typeid", "3")
                                   .data("image", file.getName(), Files.newInputStream(file.toPath()))
                                   .ignoreContentType(true)
                                   .timeout(120000)
                                   .post()
                                   .text();
        JSONObject jsonObject = JSONObject.parseObject(resultString);
        if (jsonObject.getObject("success", Boolean.class)) {
            return jsonObject.getJSONObject("data").getString("result");
        } else {
            return null;
        }
    }
}
