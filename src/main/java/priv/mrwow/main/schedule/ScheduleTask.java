package priv.mrwow.main.schedule;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import priv.mrwow.main.mapper.UserMapper;
import priv.mrwow.main.model.PO.User;
import priv.mrwow.main.model.example.UserExample;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 1.1))
    public void configureTasks() throws IOException, InterruptedException {
        List<User> users = userMapper.selectByExample(new UserExample());
        System.setProperty("webdriver.gecko.driver", "/home/geckodriver");
        System.setProperty("webdriver.firefox.bin", "/home/firefox/firefox");
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        for (User user : users) {
            WebDriver driver = new FirefoxDriver(options);
            login(user, driver);
            declare(driver);
            driver.quit();
            writeToFile(user);
        }
        FileWriter fw = null;
        try {
            File f = new File("/mrwow/on_20220503_autoHealthCheck/information.txt");
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fw != null;
        PrintWriter pw = new PrintWriter(fw);
        pw.print("\n\n");
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 1.1))
    private void writeToFile(User user) {
        FileWriter fw = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File("/mrwow/on_20220503_autoHealthCheck/information.txt");
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fw != null;
        PrintWriter pw = new PrintWriter(fw);
        pw.println("时间: " + simpleDateFormat.format(new Date()) + "\t用户: " + user.getName());
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        Thread.sleep(3000);
        if (!isElementExisting(driver, By.xpath("//div[@class=\"dialog display\"]"))) {
            declare(driver);
        }
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 1.1))
    public void login(User user, WebDriver driver) throws IOException, InterruptedException {
        driver.get("https://cas.sysu.edu.cn/cas/login");
        if (isElementExisting(driver, By.xpath("//*[@id=\"root\"]/span/div/div[2]/div[1]/div/div[1]/div[2]/div/div/div/button"))) {
            driver.findElement(By.xpath("//*[@id=\"root\"]/span/div/div[2]/div[1]/div/div[1]/div[2]/div/div/div/button")).click();
        }
        driver.findElement(By.id("username")).sendKeys(user.getUsername());
        driver.findElement(By.id("password")).sendKeys(user.getPassword());
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        WebElement element = driver.findElement(By.id("captchaImg"));
        Rectangle rect = element.getRect();
        BufferedImage subImage = ImageIO.read(src).getSubimage(rect.x, rect.y, rect.getWidth(), rect.height);
        String imgPath = "/mrwow/on_20220503_autoHealthCheck/code.png";
        File verificationCodeImg = new File(imgPath);
        ImageIO.write(subImage, "png", verificationCodeImg);
        String verificationCode = identifyCode(imgPath);
        driver.findElement(By.id("captcha")).sendKeys(verificationCode);
        driver.findElement(By.xpath("//*[@id=\"fm1\"]/section[2]/input[4]")).click();
        Thread.sleep(3000);
        if (isElementExisting(driver, By.xpath("//*[@id=\"fm1\"]/div[@class=\"alert alert-danger\"]"))) {
            login(user, driver);
        }
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

    /**
     * 判断某个元素是否存在
     */
    public boolean isElementExisting(WebDriver webDriver, By by) {
        try {
            webDriver.findElement(by);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
